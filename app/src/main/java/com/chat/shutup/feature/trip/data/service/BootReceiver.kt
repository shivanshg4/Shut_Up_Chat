package com.chat.shutup.feature.trip.data.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chat.shutup.domain.repository.TripRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var tripRepository: TripRepository

    @Inject
    lateinit var alarmScheduler: TripAlarmScheduler

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            scope.launch {
                val trips = tripRepository.getAllTrips().first()
                trips.forEach { trip ->
                    if (trip.startTime != null && trip.startTime > System.currentTimeMillis()) {
                        alarmScheduler.scheduleReminder(trip)
                    }
                }
            }
        }
    }
}
