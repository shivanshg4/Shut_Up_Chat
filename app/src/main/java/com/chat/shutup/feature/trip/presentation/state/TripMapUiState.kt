package com.chat.shutup.feature.trip.presentation.state

import com.chat.shutup.domain.model.LocationPoint
import com.chat.shutup.domain.model.MemberRouteProgress
import com.chat.shutup.domain.model.TripMember
import com.chat.shutup.domain.model.TripRoute
import com.chat.shutup.domain.repository.TrackingStatus

data class MemberLocationState(
    val member: TripMember,
    val location: LocationPoint? = null,
    val progress: MemberRouteProgress? = null,
    val isStale: Boolean = false
)

enum class RouteRequestState {
    IDLE, LOADING, SUCCESS, ERROR
}

data class TripMapUiState(
    val isLoading: Boolean = false,
    val trackingStatus: TrackingStatus = TrackingStatus.IDLE,
    val activeTrackingTripId: String? = null,
    val routeRequestState: RouteRequestState = RouteRequestState.IDLE,
    val routeError: String? = null,
    val currentLocation: LocationPoint? = null,
    val members: List<MemberLocationState> = emptyList(),
    val route: TripRoute? = null,
    val error: String? = null,
    val isPermissionGranted: Boolean = false,
    val shouldShowRationale: Boolean = false
)
