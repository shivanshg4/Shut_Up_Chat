package com.chat.shutup.feature.trip.domain.model

data class MemberRouteProgress(
    val userId: String,
    val progressDistanceMeters: Double,
    val progressPercentage: Float,
    val distanceRemainingMeters: Double,
    val isAhead: Boolean = false,
    val isBehind: Boolean = false,
    val isNear: Boolean = false,
    val aheadBehindDistanceMeters: Double = 0.0,
    val isOffRoute: Boolean = false,
    val distanceFromRouteMeters: Double = 0.0,
    val lastUpdated: Long
)
