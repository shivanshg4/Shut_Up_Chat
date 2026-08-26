package com.chat.shutup.domain.repository

import kotlinx.coroutines.flow.StateFlow

enum class TrackingStatus {
    IDLE, STARTING, TRACKING, STOPPING, ERROR
}

interface TrackingRepository {
    val activeTripId: StateFlow<String?>
    val trackingStatus: StateFlow<TrackingStatus>
    
    fun startTracking(tripId: String)
    fun stopTracking()
    fun setStatus(status: TrackingStatus)
}
