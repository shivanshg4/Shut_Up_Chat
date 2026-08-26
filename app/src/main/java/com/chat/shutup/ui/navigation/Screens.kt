package com.chat.shutup.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object ChatList : Screen

    @Serializable
    data class Chat(val chatId: String) : Screen

    @Serializable
    data object Login : Screen

    @Serializable
    data object Signup : Screen

    @Serializable
    data object Profile : Screen

    @Serializable
    data object Search : Screen

    @Serializable
    data object Trips : Screen

    @Serializable
    data object CreateTrip : Screen

    @Serializable
    data object JoinTrip : Screen

    @Serializable
    data class TripDetails(val tripId: String) : Screen

    @Serializable
    data class TripMap(val tripId: String) : Screen

    @Serializable
    data class LocationPicker(val initialLat: Double, val initialLng: Double, val mode: String) : Screen
}
