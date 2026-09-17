package com.chat.shutup.feature.trip.data.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.chat.shutup.domain.model.Trip
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReminder(trip: Trip) {
        val startTime = trip.startTime ?: return
        val reminderTime = startTime - (30 * 60 * 1000) // 30 minutes before

        if (reminderTime <= System.currentTimeMillis()) return

        val intent = Intent(context, TripAlarmReceiver::class.java).apply {
            putExtra("tripId", trip.id)
            putExtra("tripName", trip.name)
            putExtra("origin", trip.origin?.address ?: "Unknown")
            putExtra("destination", trip.destination?.address ?: "Unknown")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            trip.id.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
        }
    }

    fun cancelReminder(tripId: String) {
        val intent = Intent(context, TripAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            tripId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
