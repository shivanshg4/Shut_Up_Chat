package com.chat.shutup.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.chat.shutup.domain.repository.TripPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : TripPreferencesRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences("trip_prefs", Context.MODE_PRIVATE)

    override fun getLastReadChatTimestamp(tripId: String): Long {
        return prefs.getLong("${KEY_LAST_READ}_$tripId", 0L)
    }

    override fun setLastReadChatTimestamp(tripId: String, timestamp: Long) {
        prefs.edit().putLong("${KEY_LAST_READ}_$tripId", timestamp).apply()
    }

    override fun isBackgroundAnimationEnabled(): Boolean {
        return prefs.getBoolean(KEY_BG_ANIM, true)
    }

    override fun setBackgroundAnimationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BG_ANIM, enabled).apply()
    }

    override fun isInteractiveNatureEnabled(): Boolean {
        return prefs.getBoolean(KEY_INTERACTIVE_NATURE, true)
    }

    override fun setInteractiveNatureEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_INTERACTIVE_NATURE, enabled).apply()
    }

    companion object {
        private const val KEY_LAST_READ = "last_read_chat"
        private const val KEY_BG_ANIM = "bg_animation_enabled"
        private const val KEY_INTERACTIVE_NATURE = "interactive_nature_enabled"
    }
}
