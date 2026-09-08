package com.chat.shutup.domain.usecase

import com.chat.shutup.domain.model.Trip
import com.chat.shutup.domain.model.TripMarkerType
import com.chat.shutup.domain.model.TripRole
import com.chat.shutup.domain.repository.AuthRepository
import com.chat.shutup.domain.repository.ChatRepository
import com.chat.shutup.domain.repository.TripRepository
import javax.inject.Inject

class AlreadyMemberException(val trip: Trip) : Exception("You're already a member of this trip.")

class JoinTripUseCase @Inject constructor(
    private val tripRepository: TripRepository,
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(inviteCode: String): Result<Trip> = runCatching {
        val firebaseUser = authRepository.currentUser ?: throw Exception("User not authenticated")
        
        val code = inviteCode.trim().uppercase()
        if (code.isBlank()) {
            throw Exception("Invite code cannot be empty")
        }

        // 1. Get Trip ID first
        val tripId = tripRepository.getTripIdByInviteCode(code)
            ?: throw Exception("Trip not found")

        // Check if already a member locally first (fast)
        val localTrip = tripRepository.getTripByInviteCode(code)
        if (localTrip != null && localTrip.members.any { it.userId == firebaseUser.uid }) {
            throw AlreadyMemberException(localTrip)
        }

        // 2. Join the trip (this also handles updating local/remote membership)
        // Fetch full domain User object to get nickname and vehicle preference
        val user = chatRepository.getUserProfile(firebaseUser.uid).getOrNull()

        tripRepository.joinTrip(
            tripId = tripId,
            userId = firebaseUser.uid,
            name = user?.nickname ?: user?.name ?: firebaseUser.displayName ?: "Member",
            role = TripRole.MEMBER,
            markerType = user?.favoriteVehicle ?: TripMarkerType.DEFAULT
        )

        // 3. Now fetch and persist the full trip into Room
        val trip = tripRepository.getTripByInviteCode(code)
            ?: throw Exception("Failed to load trip details after joining")

        trip
    }
}
