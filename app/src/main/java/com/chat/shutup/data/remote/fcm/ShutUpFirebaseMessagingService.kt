package com.chat.shutup.data.remote.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.chat.shutup.R
import com.chat.shutup.di.ChatAnnotations
import com.chat.shutup.domain.repository.FcmRepository
import com.chat.shutup.feature.trip.data.service.TripNotificationManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class ShutUpFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmRepository: FcmRepository

    @Inject
    lateinit var tripNotificationManager: TripNotificationManager

    @Inject
    lateinit var serviceScope : CoroutineScope

    /**
     * FUTURE BACKEND NOTE:
     * Server-side cross-device push for trip events will use Firebase Cloud Functions + FCM 
     * when the Firebase project is upgraded to the Blaze plan.
     * The current implementation is designed to be compatible with that future flow.
     */

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        serviceScope.launch {
            fcmRepository.updateToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val type = message.data["type"]
        val tripId = message.data["tripId"]
        
        if (type == "TRIP_TOGETHER" && tripId != null) {
            val title = message.data["title"] ?: "Trip Update"
            val body = message.data["body"] ?: ""
            tripNotificationManager.showTripActivity(title, body, tripId)
            return
        }

        val title = message.notification?.title ?: message.data["title"]
        val body = message.notification?.body ?: message.data["body"]
        Log.d("FCM", "Message received - Title: $title, Body: $body")

        if (title == null || body == null) {
            return
        }

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = Random.nextInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "chat_messages",
                "Chat Messages",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = intent?.let {
            PendingIntent.getActivity(
                this, 0, it, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(this, "chat_messages")
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.logo)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()
        notificationManager.notify(notificationId, notification)
    }
}
