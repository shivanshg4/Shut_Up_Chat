package com.chat.shutup.feature.trip.data.service

import com.chat.shutup.domain.repository.AuthRepository
import com.chat.shutup.domain.repository.TripRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripMembershipMonitor @Inject constructor(
    private val tripRepository: TripRepository,
    private val authRepository: AuthRepository,
    private val notificationManager: TripNotificationManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var monitorJob: Job? = null
    private val monitoredTrips = mutableSetOf<String>()

    fun startMonitoring() {
        if (monitorJob != null) return

        monitorJob = scope.launch {
            val userId = authRepository.currentUser?.uid ?: return@launch
            
            tripRepository.getUserTrips(userId).collectLatest { trips ->
                trips.forEach { trip ->
                    if (!monitoredTrips.contains(trip.id)) {
                        monitoredTrips.add(trip.id)
                        launch {
                            observeTripMembers(trip.id, trip.name)
                        }
                    }
                }
            }
        }
    }

    private suspend fun observeTripMembers(tripId: String, tripName: String) {
        val currentUserId = authRepository.currentUser?.uid ?: return
        
        // First, get the current members from local DB to avoid notifying for existing members
        // Actually, TripRepository.getTripMembers handles syncing to local DB.
        // We need to detect the DIFFERENCE.
        
        var isFirstEmisson = true
        
        tripRepository.getTripMembers(tripId).collect { members ->
            if (isFirstEmisson) {
                isFirstEmisson = false
                return@collect
            }

            // Detect new members that are not the current user
            // We'll compare with local DB before the update, but getTripMembers updates it immediately.
            // So we might need a different approach or check the joinedAt timestamp.
            
            val now = System.currentTimeMillis()
            members.forEach { member ->
                if (member.userId != currentUserId && (now - member.joinedAt) < 60000) { // Joined in last minute
                    // This is still a bit fuzzy. 
                    // Better: The Repository should emit the new members or we should track it here.
                }
            }
        }
    }

    fun stopMonitoring() {
        monitorJob?.cancel()
        monitorJob = null
        monitoredTrips.clear()
    }
}
