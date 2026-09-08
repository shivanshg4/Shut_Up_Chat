package com.chat.shutup.domain.usecase

import com.chat.shutup.domain.repository.AuthRepository
import com.chat.shutup.domain.repository.TripRepository
import javax.inject.Inject

class LeaveTripUseCase @Inject constructor(
    private val tripRepository: TripRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(tripId: String): Result<Unit> {
        val userId = authRepository.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
        return tripRepository.leaveTrip(tripId, userId)
    }
}
