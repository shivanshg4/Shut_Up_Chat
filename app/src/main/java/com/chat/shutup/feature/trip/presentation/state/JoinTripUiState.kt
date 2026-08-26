package com.chat.shutup.feature.trip.presentation.state

import com.chat.shutup.domain.model.Trip

data class JoinTripUiState(
    val inviteCode: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val joinedTrip: Trip? = null
)
