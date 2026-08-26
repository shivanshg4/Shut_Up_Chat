package com.chat.shutup.feature.trip.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.chat.shutup.domain.repository.AuthRepository
import com.chat.shutup.domain.repository.LocationClient
import com.chat.shutup.domain.repository.RouteRepository
import com.chat.shutup.domain.repository.TripLocationRepository
import com.chat.shutup.domain.repository.TripRepository
import com.chat.shutup.domain.model.Trip
import com.chat.shutup.domain.model.TripMarkerType
import com.chat.shutup.feature.trip.presentation.state.MemberLocationState
import com.chat.shutup.feature.trip.presentation.state.TripMapUiState
import com.chat.shutup.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripMapViewModel @Inject constructor(
    private val locationClient: LocationClient,
    private val tripLocationRepository: TripLocationRepository,
    private val tripRepository: TripRepository,
    private val routeRepository: RouteRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId: String = try {
        savedStateHandle.toRoute<Screen.TripMap>().tripId
    } catch (e: Exception) {
        ""
    }
    
    val currentUserId = authRepository.currentUser?.uid

    private val _uiState = MutableStateFlow(TripMapUiState())
    val uiState = _uiState.asStateFlow()

    private var locationJob: Job? = null
    private var membersJob: Job? = null
    private var tripJob: Job? = null

    init {
        if (tripId.isNotEmpty()) {
            observeMembersAndLocations()
            observeTrip()
        }
    }

    fun onMarkerTypeSelected(markerType: TripMarkerType) {
        val userId = currentUserId ?: return
        if (tripId.isEmpty()) return

        viewModelScope.launch {
            try {
                // Find current member to get their details
                val currentMember = _uiState.value.members.find { it.member.userId == userId }?.member 
                    ?: return@launch

                tripRepository.joinTrip(
                    tripId = tripId,
                    userId = userId,
                    name = currentMember.name,
                    role = currentMember.role,
                    markerType = markerType
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to update icon: ${e.message}") }
            }
        }
    }

    private fun observeMembersAndLocations() {
        membersJob?.cancel()
        membersJob = combine(
            tripRepository.getTripMembers(tripId),
            tripLocationRepository.observeMemberLocations(tripId)
        ) { members, locations ->
            members.map { member ->
                val location = locations[member.userId]
                val isStale = location?.let { 
                    System.currentTimeMillis() - it.timestamp > STALE_THRESHOLD_MS 
                } ?: true
                
                MemberLocationState(
                    member = member,
                    location = location,
                    isStale = isStale
                )
            }
        }.onEach { memberStates ->
            _uiState.update { it.copy(members = memberStates) }
        }.launchIn(viewModelScope)
    }

    private fun observeTrip() {
        tripJob?.cancel()
        tripJob = tripRepository.getTrip(tripId)
            .onEach { trip ->
                trip?.let { t ->
                    _uiState.update { it.copy(route = t.route) }
                    
                    // If trip has origin and destination but no route, or if route points are empty, fetch it
                    if (t.origin != null && t.destination != null && (t.route == null || t.route.points.isEmpty())) {
                        fetchRoute(t)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun fetchRoute(trip: Trip) {
        val origin = trip.origin ?: return
        val destination = trip.destination ?: return
        
        if (_uiState.value.isRouteLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isRouteLoading = true) }
            val result = routeRepository.getRoute(origin, destination, trip.travelMode.name)
            result.onSuccess { route ->
                _uiState.update { it.copy(route = route, isRouteLoading = false, error = null) }
                
                // Only the creator should update the route in Firebase
                if (trip.creatorId == currentUserId) {
                    tripRepository.updateTripRoute(tripId, route)
                }
            }
            .onFailure { e ->
                _uiState.update { it.copy(isRouteLoading = false, error = "Failed to calculate route: ${e.message}") }
            }
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        _uiState.update { it.copy(isPermissionGranted = isGranted) }
        if (isGranted) {
            startLocationUpdates()
        } else {
            _uiState.update { it.copy(error = "Location permission is required to show your position.") }
        }
    }

    fun startLocationUpdates() {
        if (!_uiState.value.isPermissionGranted) return
        
        locationJob?.cancel()
        _uiState.update { it.copy(isLoading = true, isTracking = true) }
        
        locationJob = locationClient.getLocationUpdates(LOCATION_UPDATE_INTERVAL_MS)
            .onEach { location ->
                _uiState.update { 
                    it.copy(
                        currentLocation = location,
                        isLoading = false,
                        error = null
                    )
                }
                if (currentUserId != null && tripId.isNotEmpty()) {
                    // Update location with bearing for marker rotation
                    tripLocationRepository.updateMyLocation(tripId, currentUserId, location)
                }
            }
            .catch { e ->
                _uiState.update { 
                    it.copy(
                        error = e.message ?: "Unknown location error",
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun stopLocationUpdates() {
        locationJob?.cancel()
        locationJob = null
        _uiState.update { it.copy(isTracking = false) }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
        membersJob?.cancel()
        tripJob?.cancel()
    }

    companion object {
        private const val LOCATION_UPDATE_INTERVAL_MS = 5000L
        private const val STALE_THRESHOLD_MS = 60000L // 1 minute
    }
}
