package com.chat.shutup.feature.trip.presentation.state

import com.chat.shutup.domain.model.Trip

data class TripsUiState(
    val trips: List<Trip> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
