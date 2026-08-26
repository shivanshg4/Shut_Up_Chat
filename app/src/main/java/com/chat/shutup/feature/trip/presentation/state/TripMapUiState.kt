package com.chat.shutup.feature.trip.presentation.state

import com.chat.shutup.domain.model.LocationPoint
import com.chat.shutup.domain.model.TripMember
import com.chat.shutup.domain.model.TripRoute

data class MemberLocationState(
    val member: TripMember,
    val location: LocationPoint? = null,
    val isStale: Boolean = false
)

data class TripMapUiState(
    val isLoading: Boolean = false,
    val isTracking: Boolean = false,
    val isRouteLoading: Boolean = false,
    val currentLocation: LocationPoint? = null,
    val members: List<MemberLocationState> = emptyList(),
    val route: TripRoute? = null,
    val error: String? = null,
    val isPermissionGranted: Boolean = false,
    val shouldShowRationale: Boolean = false
)
