package com.chat.shutup.domain.model

data class User(
    val id: String = "",
    val name: String = "",
    val nickname: String? = null,
    val email: String = "",
    val contactNumber: String? = null,
    val imageUrl: String? = null,
    val fcmToken: String? = null,
    val favoriteVehicle: TripMarkerType = TripMarkerType.DEFAULT,
    val online: Boolean = false,
    val lastSeen: Long = 0L
)
