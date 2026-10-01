package com.chat.shutup.feature.chat.domain.repository

import com.chat.shutup.feature.chat.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface TripChatRepository {
    fun getMessages(tripId: String): Flow<List<Message>>
    suspend fun sendMessage(tripId: String, message: Message): Result<Unit>
}
