package com.chat.shutup.feature.trip.presentation.state

import com.chat.shutup.domain.model.LocationPoint

data class TripMapUiState(
    val isLoading: Boolean = false,
    val currentLocation: LocationPoint? = null,
    val error: String? = null,
    val isPermissionGranted: Boolean = false,
    val shouldShowRationale: Boolean = false
)
