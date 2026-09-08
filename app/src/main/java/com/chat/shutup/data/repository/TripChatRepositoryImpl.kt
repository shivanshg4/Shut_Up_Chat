package com.chat.shutup.data.repository

import com.chat.shutup.domain.repository.TripChatRepository
import com.chat.shutup.feature.chat.domain.model.Message
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripChatRepositoryImpl @Inject constructor(
    private val db: FirebaseDatabase
) : TripChatRepository {

    override fun getMessages(tripId: String): Flow<List<Message>> = callbackFlow {
        val messagesRef = db.getReference("trips").child(tripId).child("chat").child("messages")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { it.getValue(Message::class.java) }
                trySend(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                if (error.code == DatabaseError.PERMISSION_DENIED) {
                    android.util.Log.e("TripChatRepositoryImpl", "Permission denied for chat in trip: $tripId")
                    close()
                } else {
                    close(error.toException())
                }
            }
        }
        messagesRef.addValueEventListener(listener)
        awaitClose { messagesRef.removeEventListener(listener) }
    }

    override suspend fun sendMessage(tripId: String, message: Message): Result<Unit> {
        return try {
            val messagesRef = db.getReference("trips").child(tripId).child("chat").child("messages").push()
            val messageWithId = message.copy(id = messagesRef.key ?: "")
            messagesRef.setValue(messageWithId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
