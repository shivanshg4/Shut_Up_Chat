package com.chat.shutup.feature.trip.data.remote.dto

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class FirebaseTripRouteDto(
    val distanceMeters: Int = 0,
    val durationSeconds: Long = 0,
    val encodedPolyline: String = ""
)
