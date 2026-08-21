package com.chat.shutup.data.repository

import com.chat.shutup.domain.model.Chat
import com.chat.shutup.feature.chat.domain.model.Message
import com.chat.shutup.domain.model.User
import com.chat.shutup.domain.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val db: FirebaseDatabase,
    private val auth: FirebaseAuth,
) : ChatRepository {

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    override fun getChats(): Flow<List<Chat>> = callbackFlow {
        val uid = currentUserId ?: return@callbackFlow
        val chatsRef = db.getReference("users").child(uid).child("chats")
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chats = snapshot.children.mapNotNull { it.getValue(Chat::class.java) }
                trySend(chats.sortedByDescending { it.lastMessageTimestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("ChatRepositoryImpl", "getChats cancelled: ${error.message}")
                close(error.toException())
            }
        }
        chatsRef.addValueEventListener(listener)
        awaitClose { chatsRef.removeEventListener(listener) }
    }

    override fun getChat(chatId: String): Flow<Chat?> = callbackFlow {
        val uid = currentUserId ?: return@callbackFlow
        val chatRef = db.getReference("users").child(uid).child("chats").child(chatId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Chat::class.java))
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        chatRef.addValueEventListener(listener)
        awaitClose { chatRef.removeEventListener(listener) }
    }

    override fun getMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val messagesRef = db.getReference("messages").child(chatId)
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { it.getValue(Message::class.java) }
                trySend(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("ChatRepositoryImpl", "getMessages cancelled: ${error.message}")
                close(error.toException())
            }
        }
        messagesRef.addValueEventListener(listener)
        awaitClose { messagesRef.removeEventListener(listener) }
    }

    override suspend fun sendMessage(chatId: String, message: Message): Result<Unit> {
        return try {
            val messagesRef = db.getReference("messages").child(chatId).push()
            val messageWithId = message.copy(id = messagesRef.key ?: "")
            
            messagesRef.setValue(messageWithId).await()
            
            // Atomic update for last message in both users' chat lists
            updateChatLastMessageAtomic(chatId, messageWithId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateChatLastMessageAtomic(chatId: String, message: Message) {
        val chatRef = db.getReference("chats").child(chatId)
        val chatSnapshot = chatRef.get().await()
        val participants = chatSnapshot.child("participants").children.mapNotNull { it.key }
        
        val updates = hashMapOf<String, Any>()
        participants.forEach { participantId ->
            updates["/users/$participantId/chats/$chatId/lastMessage"] = message.text
            updates["/users/$participantId/chats/$chatId/lastMessageTimestamp"] = message.timestamp
        }
        db.reference.updateChildren(updates).await()
    }

    override suspend fun getUserProfile(userId: String): Result<User?> {
        return try {
            val snapshot = db.getReference("users").child(userId).get().await()
            Result.success(snapshot.getValue(User::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCurrentUserProfile(user: User): Result<Unit> {
        return try {
            val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
            db.getReference("users").child(uid).setValue(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchUsers(query: String): Result<List<User>> {
        return try {
            val snapshot = db.getReference("users")
                .orderByChild("email")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get().await()
            
            val users = snapshot.children.asSequence()
                .mapNotNull { it.getValue(User::class.java) }
                .filter { it.id != currentUserId }
                .toList()
            
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = db.getReference("users").get().await()
            val users = snapshot.children.mapNotNull { it.getValue(User::class.java) }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOrCreateChat(targetUserId: String): Result<String> {
        if (targetUserId.isBlank()) return Result.failure(Exception("Target user ID is empty"))

        return try {
            val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
            
            // Log for debugging
            android.util.Log.d("ChatRepositoryImpl", "Creating/getting chat between $uid and $targetUserId")

            val existingChatSnapshot = db.getReference("users").child(uid).child("chats")
                .orderByChild("participantId")
                .equalTo(targetUserId)
                .get().await()
            
            if (existingChatSnapshot.exists()) {
                val chatId = existingChatSnapshot.children.first().key ?: ""
                android.util.Log.d("ChatRepositoryImpl", "Found existing chat: $chatId")
                return Result.success(chatId)
            }

            val chatId = db.getReference("chats").push().key ?: ""
            android.util.Log.d("ChatRepositoryImpl", "Creating new chat with ID: $chatId")

            val participants = mapOf(uid to true, targetUserId to true)
            
            // Get user profiles first to ensure they exist and we have the data
            val targetUser = getUserProfile(targetUserId).getOrNull()
            val currentUser = getUserProfile(uid).getOrNull()

            val updates = hashMapOf<String, Any>()
            
            // 1. Create the global chat entry
            updates["/chats/$chatId/participants"] = participants
            
            // 2. Add chat to current user's list
            updates["/users/$uid/chats/$chatId"] = Chat(
                id = chatId,
                participantId = targetUserId,
                participantName = targetUser?.name ?: "Unknown",
                participantImageUrl = targetUser?.imageUrl
            )

            // 3. Add chat to target user's list
            updates["/users/$targetUserId/chats/$chatId"] = Chat(
                id = chatId,
                participantId = uid,
                participantName = currentUser?.name ?: "Unknown",
                participantImageUrl = currentUser?.imageUrl
            )

            db.reference.updateChildren(updates).await()
            android.util.Log.d("ChatRepositoryImpl", "Successfully created chat and updated user nodes")

            Result.success(chatId)
        } catch (e: Exception) {
            android.util.Log.e("ChatRepositoryImpl", "Error in getOrCreateChat", e)
            Result.failure(e)
        }
    }

    override suspend fun initiateCall(targetUserId: String, type: com.chat.shutup.domain.model.CallType): Result<com.chat.shutup.domain.model.CallInfo> {
        return try {
            val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
            val currentUser = getUserProfile(uid).getOrNull()
            
            val callRef = db.getReference("calls").push()
            val callId = callRef.key ?: ""
            
            val callInfo = com.chat.shutup.domain.model.CallInfo(
                callId = callId,
                callerId = uid,
                callerName = currentUser?.name ?: "Unknown",
                callerImageUrl = currentUser?.imageUrl,
                receiverId = targetUserId,
                type = type,
                status = com.chat.shutup.domain.model.CallStatus.RINGING
            )
            
            callRef.setValue(callInfo).await()
            
            // Notify receiver
            db.getReference("users").child(targetUserId).child("incomingCall").setValue(callInfo).await()
            
            Result.success(callInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCallStatus(callId: String, status: com.chat.shutup.domain.model.CallStatus): Result<Unit> {
        return try {
            val callRef = db.getReference("calls").child(callId)
            val callSnapshot = callRef.get().await()
            val callInfo = callSnapshot.getValue(com.chat.shutup.domain.model.CallInfo::class.java) ?: return Result.failure(Exception("Call not found"))
            
            callRef.child("status").setValue(status).await()
            
            // Clean up signaling node if ended/rejected
            if (status == com.chat.shutup.domain.model.CallStatus.ENDED || status == com.chat.shutup.domain.model.CallStatus.REJECTED) {
                db.getReference("users").child(callInfo.receiverId).child("incomingCall").removeValue().await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeIncomingCalls(): Flow<com.chat.shutup.domain.model.CallInfo> = callbackFlow {
        val uid = currentUserId ?: return@callbackFlow
        val incomingCallRef = db.getReference("users").child(uid).child("incomingCall")
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(com.chat.shutup.domain.model.CallInfo::class.java)?.let {
                    trySend(it)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("ChatRepositoryImpl", "observeIncomingCalls cancelled: ${error.message}")
                close(error.toException())
            }
        }
        incomingCallRef.addValueEventListener(listener)
        awaitClose { incomingCallRef.removeEventListener(listener) }
    }

    override fun observeCallStatus(callId: String): Flow<com.chat.shutup.domain.model.CallStatus> = callbackFlow {
        val callStatusRef = db.getReference("calls").child(callId).child("status")
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(com.chat.shutup.domain.model.CallStatus::class.java)?.let {
                    trySend(it)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("ChatRepositoryImpl", "observeCallStatus cancelled: ${error.message}")
                close(error.toException())
            }
        }
        callStatusRef.addValueEventListener(listener)
        awaitClose { callStatusRef.removeEventListener(listener) }
    }
}
