package com.chat.shutup.domain.repository

interface TripPreferencesRepository {
    fun getLastReadChatTimestamp(tripId: String): Long
    fun setLastReadChatTimestamp(tripId: String, timestamp: Long)
    
    // Global visual preferences
    fun isBackgroundAnimationEnabled(): Boolean
    fun setBackgroundAnimationEnabled(enabled: Boolean)
    
    fun isInteractiveNatureEnabled(): Boolean
    fun setInteractiveNatureEnabled(enabled: Boolean)
}
