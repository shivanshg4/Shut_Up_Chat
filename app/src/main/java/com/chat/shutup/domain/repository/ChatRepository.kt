package com.chat.shutup.domain.repository

import com.chat.shutup.domain.model.Chat
import com.chat.shutup.feature.chat.domain.model.Message
import com.chat.shutup.domain.model.User
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getChats(): Flow<List<Chat>>
    fun getChat(chatId: String): Flow<Chat?>
    fun getMessages(chatId: String): Flow<List<Message>>
    suspend fun sendMessage(chatId: String, message: Message): Result<Unit>
    suspend fun getUserProfile(userId: String): Result<User?>
    suspend fun updateCurrentUserProfile(user: User): Result<Unit>
    suspend fun searchUsers(query: String): Result<List<User>>
    suspend fun getOrCreateChat(targetUserId: String): Result<String>
    suspend fun getAllUsers(): Result<List<User>>
    
    // Call Signaling
    suspend fun initiateCall(targetUserId: String, type: com.chat.shutup.domain.model.CallType): Result<com.chat.shutup.domain.model.CallInfo>
    suspend fun updateCallStatus(callId: String, status: com.chat.shutup.domain.model.CallStatus): Result<Unit>
    fun observeIncomingCalls(): Flow<com.chat.shutup.domain.model.CallInfo>
    fun observeCallStatus(callId: String): Flow<com.chat.shutup.domain.model.CallStatus>
}
