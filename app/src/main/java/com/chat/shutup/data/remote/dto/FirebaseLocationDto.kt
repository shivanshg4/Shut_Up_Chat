package com.chat.shutup.data.remote.dto

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class FirebaseLocationDto(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val accuracy: Float = 0f,
    val speed: Float = 0f,
    val bearing: Float = 0f,
    val timestamp: Long = 0L
)
