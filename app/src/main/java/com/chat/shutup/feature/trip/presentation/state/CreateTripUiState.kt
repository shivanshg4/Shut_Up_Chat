package com.chat.shutup.feature.trip.presentation.state

import com.chat.shutup.domain.model.Trip

data class CreateTripUiState(
    val tripName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val createdTrip: Trip? = null
)
