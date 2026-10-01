package com.chat.shutup.feature.trip.domain.model

data class TripRoute(
    val distanceMeters: Int,
    val durationSeconds: Long,
    val points: List<RoutePoint>,
    val encodedPolyline: String
)

data class RoutePoint(
    val latitude: Double,
    val longitude: Double
)
