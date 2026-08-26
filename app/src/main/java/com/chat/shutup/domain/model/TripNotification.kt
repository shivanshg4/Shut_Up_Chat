package com.chat.shutup.domain.model

enum class TripNotificationType {
    MEMBER_JOINED,
    MEMBER_LEFT,
    LOCATION_SHARING_STARTED,
    LOCATION_SHARING_STOPPED,
    MEMBER_OFF_ROUTE,
    MEMBER_BACK_ON_ROUTE
}

data class TripNotificationData(
    val type: TripNotificationType,
    val tripId: String,
    val tripName: String,
    val actorUserId: String,
    val actorName: String,
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis()
)
