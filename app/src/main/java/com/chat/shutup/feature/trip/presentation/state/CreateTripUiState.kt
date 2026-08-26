package com.chat.shutup.feature.trip.presentation.state

import com.chat.shutup.domain.model.TravelMode
import com.chat.shutup.domain.model.Trip
import com.chat.shutup.domain.model.TripLocation

data class CreateTripUiState(
    val tripId: String = "",
    val inviteCode: String = "",
    val tripName: String = "",
    val origin: TripLocation? = null,
    val destination: TripLocation? = null,
    val travelMode: TravelMode = TravelMode.DRIVING,
    val isLoading: Boolean = false,
    val error: String? = null,
    val createdTrip: Trip? = null,
    val isSynced: Boolean = false
)
