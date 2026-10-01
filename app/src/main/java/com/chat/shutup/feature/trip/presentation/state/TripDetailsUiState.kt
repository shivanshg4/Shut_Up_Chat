package com.chat.shutup.feature.trip.presentation.state

import com.chat.shutup.feature.trip.domain.model.Trip

data class TripDetailsUiState(
    val trip: Trip? = null,
    val isLoading: Boolean = false,
    val isTrackingActive: Boolean = false,
    val activeTrackingTripId: String? = null,
    val error: String? = null
)
