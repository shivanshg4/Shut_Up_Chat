package com.chat.shutup.feature.trip.data.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat
import com.chat.shutup.domain.repository.AuthRepository
import com.chat.shutup.domain.repository.LocationClient
import com.chat.shutup.domain.repository.TrackingRepository
import com.chat.shutup.domain.repository.TrackingStatus
import com.chat.shutup.domain.repository.TripLocationRepository
import com.chat.shutup.domain.repository.TripRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TripLocationForegroundService : Service() {

    @Inject lateinit var locationClient: LocationClient
    @Inject lateinit var tripLocationRepository: TripLocationRepository
    @Inject lateinit var tripRepository: TripRepository
    @Inject lateinit var trackingRepository: TrackingRepository
    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var notificationManager: TripNotificationManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var locationJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TripTrackingDebug", "SERVICE_ON_START_COMMAND: action=${intent?.action}")
        
        if (intent == null) {
            val activeTripId = trackingRepository.activeTripId.value
            if (activeTripId != null) {
                startTracking(activeTripId)
            } else {
                stopSelf()
            }
            return START_STICKY
        }

        when (intent.action) {
            ACTION_START -> {
                val tripId = intent.getStringExtra(EXTRA_TRIP_ID) ?: return START_NOT_STICKY
                startTracking(tripId)
            }
            ACTION_STOP -> {
                stopTracking()
            }
        }
        return START_STICKY
    }

    private fun startTracking(tripId: String) {
        Log.d("TripTrackingDebug", "SERVICE_START_TRACKING: tripId=$tripId")
        val userId = authRepository.currentUser?.uid ?: return stopTracking().also {
            Log.d("TripTrackingDebug", "START_TRACKING_FAILED: No user")
        }

        // CRITICAL: startForeground must be called IMMEDIATELY on the main thread
        // We show a generic notification first, then update it once trip details are loaded
        val initialNotification = notificationManager.buildTrackingNotification("Loading trip details...", tripId)
        try {
            ServiceCompat.startForeground(
                this,
                TripNotificationManager.NOTIFICATION_ID,
                initialNotification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                } else 0
            )
            Log.d("TripTrackingDebug", "START_FOREGROUND_SUCCESS: Initial")
        } catch (e: Exception) {
            Log.e("TripTrackingDebug", "START_FOREGROUND_CRASH", e)
            trackingRepository.setStatus(TrackingStatus.ERROR)
            stopSelf()
            return
        }

        trackingRepository.setStatus(TrackingStatus.STARTING)

        serviceScope.launch {
            try {
                // Verify membership (Wait for it since we are in foreground now)
                val isMember = tripRepository.isUserMemberOfTrip(tripId, userId)
                if (!isMember) {
                    Log.d("TripTrackingDebug", "MEMBERSHIP_CHECK_FAILED: tripId=$tripId, userId=$userId")
                    stopTracking()
                    return@launch
                }
                Log.d("TripTrackingDebug", "MEMBERSHIP_CHECK_SUCCESS")

                // Update notification with real trip name
                val trip = tripRepository.getTrip(tripId).firstOrNull()
                trip?.let {
                    val updatedNotification = notificationManager.buildTrackingNotification(it.name, it.id)
                    notificationManager.updateNotification(TripNotificationManager.NOTIFICATION_ID, updatedNotification)
                    Log.d("TripTrackingDebug", "NOTIFICATION_UPDATED: ${it.name}")
                }
            } catch (e: Exception) {
                Log.d("TripTrackingDebug", "START_TRACKING_ASYNC_FAILED: ${e.message}")
                trackingRepository.setStatus(TrackingStatus.ERROR)
                stopTracking()
            }
        }

        // Start location updates
        locationJob?.cancel()
        locationJob = locationClient.getLocationUpdates(LOCATION_UPDATE_INTERVAL_MS)
            .onEach { location ->
                Log.d("TripTrackingDebug", "LOCATION_CALLBACK_RECEIVED: lat=${location.latitude}, lng=${location.longitude}")
                tripLocationRepository.updateMyLocation(tripId, userId, location)
            }
            .catch { e ->
                Log.d("TripTrackingDebug", "LOCATION_UPDATE_ERROR: ${e.message}")
                trackingRepository.setStatus(TrackingStatus.ERROR)
            }
            .launchIn(serviceScope)
            
        trackingRepository.startTracking(tripId)
        Log.d("TripTrackingDebug", "SHARING_STATE_ACTIVE")

        // Push Sharing Started Event
        serviceScope.launch {
            tripRepository.pushTripEvent(
                tripId,
                com.chat.shutup.domain.model.TripNotificationData(
                    type = com.chat.shutup.domain.model.TripNotificationType.LOCATION_SHARING_STARTED,
                    tripId = tripId,
                    tripName = "",
                    actorUserId = userId,
                    actorName = "",
                    title = "Location Sharing",
                    body = "A member started sharing their location"
                )
            )
        }
    }

    private fun stopTracking() {
        Log.d("TripTrackingDebug", "SERVICE_STOPPED")
        val tripId = trackingRepository.activeTripId.value
        val userId = authRepository.currentUser?.uid
        
        if (tripId != null && userId != null) {
            serviceScope.launch {
                tripLocationRepository.removeMyLocation(tripId, userId)
                
                tripRepository.pushTripEvent(
                    tripId,
                    com.chat.shutup.domain.model.TripNotificationData(
                        type = com.chat.shutup.domain.model.TripNotificationType.LOCATION_SHARING_STOPPED,
                        tripId = tripId,
                        tripName = "",
                        actorUserId = userId,
                        actorName = "",
                        title = "Location Sharing",
                        body = "A member stopped sharing their location"
                    )
                )
            }
        }
        
        locationJob?.cancel()
        trackingRepository.stopTracking()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_TRIP_ID = "EXTRA_TRIP_ID"
        private const val LOCATION_UPDATE_INTERVAL_MS = 5000L
    }
}
