package com.chat.shutup.domain.repository

import kotlinx.coroutines.flow.Flow

interface TripPreferencesRepository {
    fun getLastReadChatTimestamp(tripId: String): Long
    fun setLastReadChatTimestamp(tripId: String, timestamp: Long)
    
    // Global visual preferences
    fun isBackgroundAnimationEnabled(): Boolean
    fun setBackgroundAnimationEnabled(enabled: Boolean)
    
    fun isInteractiveNatureEnabled(): Boolean
    fun setInteractiveNatureEnabled(enabled: Boolean)

    fun getThemeMode(): AppThemeMode
    fun setThemeMode(mode: AppThemeMode)
    val themeMode: Flow<AppThemeMode>
}

enum class AppThemeMode {
    SYSTEM, LIGHT, DARK
}
