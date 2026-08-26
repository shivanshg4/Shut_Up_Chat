package com.chat.shutup.data.remote.dto

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class FirebaseTripDto(
    val id: String = "",
    val name: String = "",
    val creatorId: String = "",
    val inviteCode: String = "",
    val createdAt: Long = 0L,
    val members: Map<String, FirebaseTripMemberDto> = emptyMap(),
    val origin: FirebaseTripLocationDto? = null,
    val destination: FirebaseTripLocationDto? = null,
    val travelMode: String = "DRIVING",
    val route: FirebaseTripRouteDto? = null
)

@IgnoreExtraProperties
data class FirebaseTripLocationDto(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = ""
)

@IgnoreExtraProperties
data class FirebaseTripMemberDto(
    val userId: String = "",
    val name: String = "",
    val role: String = "",
    val joinedAt: Long = 0L,
    val markerType: String = "DEFAULT"
)
