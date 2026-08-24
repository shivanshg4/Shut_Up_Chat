package com.chat.shutup.feature.trip.presentation.state

import com.chat.shutup.domain.model.Trip

data class TripDetailsUiState(
    val trip: Trip? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
