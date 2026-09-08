package com.chat.shutup.domain.usecase

import com.chat.shutup.domain.repository.TripChatRepository
import com.chat.shutup.feature.chat.domain.model.Message
import javax.inject.Inject

class SendTripChatMessageUseCase @Inject constructor(
    private val repository: TripChatRepository
) {
    suspend operator fun invoke(tripId: String, message: Message): Result<Unit> {
        return repository.sendMessage(tripId, message)
    }
}
