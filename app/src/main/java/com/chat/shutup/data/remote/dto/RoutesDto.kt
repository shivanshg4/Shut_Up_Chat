package com.chat.shutup.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RoutesRequest(
    val origin: Waypoint,
    val destination: Waypoint,
    val travelMode: String = "DRIVE",
    val routingPreference: String = "TRAFFIC_AWARE",
    val computeAlternativeRoutes: Boolean = false,
    val languageCode: String = "en-US",
    val units: String = "METRIC"
)

@Serializable
data class Waypoint(
    val location: Location
)

@Serializable
data class Location(
    val latLng: LatLngDto
)

@Serializable
data class LatLngDto(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class RoutesResponse(
    val routes: List<RouteDto> = emptyList()
)

@Serializable
data class RouteDto(
    val distanceMeters: Int? = null,
    val duration: String? = null,
    val polyline: PolylineDto? = null
)

@Serializable
data class PolylineDto(
    val encodedPolyline: String? = null
)
