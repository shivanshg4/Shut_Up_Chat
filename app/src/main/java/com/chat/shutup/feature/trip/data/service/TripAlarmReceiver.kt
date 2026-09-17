package com.chat.shutup.feature.trip.data.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TripAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationManager: TripNotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        val tripId = intent.getStringExtra("tripId") ?: return
        val tripName = intent.getStringExtra("tripName") ?: "Trip"
        val origin = intent.getStringExtra("origin") ?: "Start"
        val destination = intent.getStringExtra("destination") ?: "End"

        notificationManager.showTripReminder(tripId, tripName, origin, destination)
    }
}
