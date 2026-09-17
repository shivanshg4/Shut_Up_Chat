package com.chat.shutup.feature.trip.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.chat.shutup.MainActivity
import com.chat.shutup.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * LIMITATION DOCUMENTATION:
     * This local notification architecture is a workaround for the Firebase Spark plan (no Cloud Functions).
     * Member join notifications are generated locally by observing Firebase Realtime Database membership changes
     * while the app is active in the foreground or has an active foreground service.
     *
     * IMPORTANT: It DOES NOT provide guaranteed background push delivery when the app process is completely stopped.
     * Reliable cross-device push for trip events will require upgrading to the Firebase Blaze plan and
     * implementing Firebase Cloud Functions + FCM on the server side.
     */

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val trackingChannel = NotificationChannel(
                TRACKING_CHANNEL_ID,
                "Trip Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Used for real-time trip location sharing"
            }

            val reminderChannel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Trip Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for upcoming trips"
            }

            val activityChannel = NotificationChannel(
                ACTIVITY_CHANNEL_ID,
                "Trip Activity",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for members joining or leaving trips"
            }

            notificationManager.createNotificationChannel(trackingChannel)
            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(activityChannel)
        }
    }

    fun buildTrackingNotification(tripName: String, tripId: String): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("tripId", tripId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, TRACKING_CHANNEL_ID)
            .setContentTitle("TripTogether")
            .setContentText("Sharing location for: $tripName")
            .setSmallIcon(R.drawable.logo)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    /**
     * Shows a reminder for an upcoming trip.
     * Scheduled via AlarmManager.
     */
    fun showTripReminder(tripId: String, tripName: String, origin: String, destination: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("tripId", tripId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            tripId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setContentTitle("⏰ Trip reminder")
            .setContentText("$origin → $destination starts soon.\nAre we ready, guys? 🚗")
            .setSmallIcon(R.drawable.logo)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(tripId.hashCode(), notification)
    }

    /**
     * Shows a local notification when a member joins the trip.
     * Note: This is triggered by local observation of Firebase and only works while app is active.
     * Future Blaze upgrade will move this to Cloud Functions + FCM for background delivery.
     */
    fun showMemberJoined(tripId: String, memberName: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("tripId", tripId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (tripId + memberName).hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, ACTIVITY_CHANNEL_ID)
            .setContentTitle("🚗 $memberName joined the trip")
            .setContentText("Are we ready, guys? 😄")
            .setSmallIcon(R.drawable.logo)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify((tripId + memberName).hashCode(), notification)
    }

    /**
     * General trip activity notification (e.g. from FCM or local events).
     */
    fun showTripActivity(title: String, body: String, tripId: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("tripId", tripId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, ACTIVITY_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.logo)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    fun updateNotification(id: Int, notification: Notification) {
        notificationManager.notify(id, notification)
    }

    companion object {
        const val TRACKING_CHANNEL_ID = "trip_tracking_channel"
        const val REMINDER_CHANNEL_ID = "trip_reminders_channel"
        const val ACTIVITY_CHANNEL_ID = "trip_activity_channel"
        const val NOTIFICATION_ID = 1001
    }
}
