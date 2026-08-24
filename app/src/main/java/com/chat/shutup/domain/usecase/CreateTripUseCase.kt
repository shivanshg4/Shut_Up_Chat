package com.chat.shutup.domain.usecase

import com.chat.shutup.domain.model.Trip
import com.chat.shutup.domain.model.TripMember
import com.chat.shutup.domain.model.TripRole
import com.chat.shutup.domain.repository.AuthRepository
import com.chat.shutup.domain.repository.TripRepository
import java.util.*
import javax.inject.Inject

class CreateTripUseCase @Inject constructor(
    private val tripRepository: TripRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(tripName: String): Result<Trip> {
        val user = authRepository.currentUser ?: return Result.failure(Exception("User not authenticated"))
        
        if (tripName.isBlank()) {
            return Result.failure(Exception("Trip name cannot be empty"))
        }

        val tripId = UUID.randomUUID().toString()
        val inviteCode = generateInviteCode()
        
        val trip = Trip(
            id = tripId,
            name = tripName.trim(),
            creatorId = user.uid,
            inviteCode = inviteCode,
            members = listOf(
                TripMember(
                    userId = user.uid,
                    name = user.displayName ?: "Creator",
                    role = TripRole.CREATOR
                )
            )
        )

        return try {
            tripRepository.createTrip(trip)
            Result.success(trip)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // Excluded I, O, 0, 1
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }
}
