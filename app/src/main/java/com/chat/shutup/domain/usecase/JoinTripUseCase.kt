package com.chat.shutup.domain.usecase

import com.chat.shutup.domain.model.Trip
import com.chat.shutup.domain.model.TripRole
import com.chat.shutup.domain.repository.AuthRepository
import com.chat.shutup.domain.repository.TripRepository
import javax.inject.Inject

class JoinTripUseCase @Inject constructor(
    private val tripRepository: TripRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(inviteCode: String): Result<Trip> = runCatching {
        val user = authRepository.currentUser ?: throw Exception("User not authenticated")
        
        val code = inviteCode.trim().uppercase()
        if (code.isBlank()) {
            throw Exception("Invite code cannot be empty")
        }

        // 1. Get Trip ID first
        val tripId = tripRepository.getTripIdByInviteCode(code)
            ?: throw Exception("Trip not found")

        // 2. Join the trip (this also handles updating local/remote membership)
        tripRepository.joinTrip(
            tripId = tripId,
            userId = user.uid,
            name = user.displayName ?: "Member",
            role = TripRole.MEMBER
            // markerType defaults to current/default if not passed
        )

        // 3. Now fetch and persist the full trip into Room
        val trip = tripRepository.getTripByInviteCode(code)
            ?: throw Exception("Failed to load trip details after joining")

        trip
    }
}
