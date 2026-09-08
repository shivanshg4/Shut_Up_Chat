package com.chat.shutup.feature.trip.presentation.state

import com.chat.shutup.domain.model.Trip

data class TripsUiState(
    val trips: List<Trip> = emptyList(),
    val isLoading: Boolean = false,
    val isBackgroundAnimationEnabled: Boolean = true,
    val isInteractiveNatureEnabled: Boolean = true,
    val error: String? = null
)
