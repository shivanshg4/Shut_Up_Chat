package com.chat.shutup.domain.model

data class Trip(
    val id: String,
    val name: String,
    val creatorId: String,
    val inviteCode: String,
    val createdAt: Long = System.currentTimeMillis(),
    val members: List<TripMember> = emptyList(),
    val origin: TripLocation? = null,
    val destination: TripLocation? = null,
    val travelMode: TravelMode = TravelMode.DRIVING,
    val route: TripRoute? = null
)

data class TripLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String
)

enum class TravelMode {
    DRIVING,
    WALKING,
    BICYCLING,
    TRANSIT
}

data class TripMember(
    val userId: String,
    val name: String,
    val role: TripRole,
    val joinedAt: Long = System.currentTimeMillis(),
    val markerType: TripMarkerType = TripMarkerType.DEFAULT
)

enum class TripMarkerType {
    DEFAULT,
    CAR,
    BUS,
    TRUCK,
    MOTORCYCLE,
    BICYCLE,
    ANIMAL
}

enum class TripRole {
    CREATOR,
    MEMBER
}
