package com.chat.shutup.domain.usecase

import com.chat.shutup.domain.repository.TripChatRepository
import com.chat.shutup.feature.chat.domain.model.Message
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTripChatUseCase @Inject constructor(
    private val repository: TripChatRepository
) {
    operator fun invoke(tripId: String): Flow<List<Message>> {
        return repository.getMessages(tripId)
    }
}
