package com.chat.shutup.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.chat.shutup.domain.repository.TrackingRepository
import com.chat.shutup.domain.repository.TrackingStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class PreferenceTrackingRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : TrackingRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences("tracking_prefs", Context.MODE_PRIVATE)

    private val _activeTripId = MutableStateFlow(prefs.getString(KEY_ACTIVE_TRIP_ID, null))
    override val activeTripId: StateFlow<String?> = _activeTripId.asStateFlow()

    private val _trackingStatus = MutableStateFlow(TrackingStatus.IDLE)
    override val trackingStatus: StateFlow<TrackingStatus> = _trackingStatus.asStateFlow()

    override fun startTracking(tripId: String) {
        prefs.edit { putString(KEY_ACTIVE_TRIP_ID, tripId) }
        _activeTripId.value = tripId
        _trackingStatus.value = TrackingStatus.TRACKING
    }

    override fun stopTracking() {
        prefs.edit { remove(KEY_ACTIVE_TRIP_ID) }
        _activeTripId.value = null
        _trackingStatus.value = TrackingStatus.IDLE
    }

    override fun setStatus(status: TrackingStatus) {
        _trackingStatus.value = status
    }

    companion object {
        private const val KEY_ACTIVE_TRIP_ID = "active_trip_id"
    }
}
