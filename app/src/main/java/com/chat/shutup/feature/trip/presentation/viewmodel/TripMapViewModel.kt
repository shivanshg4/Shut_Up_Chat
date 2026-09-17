package com.chat.shutup.feature.trip.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import android.util.Log
import com.chat.shutup.domain.repository.AuthRepository
import com.chat.shutup.domain.repository.LocationClient
import com.chat.shutup.domain.repository.RouteRepository
import com.chat.shutup.domain.repository.TrackingRepository
import com.chat.shutup.domain.repository.TrackingStatus
import com.chat.shutup.domain.repository.TripLocationRepository
import com.chat.shutup.domain.repository.TripRepository
import com.chat.shutup.domain.model.Trip
import com.chat.shutup.domain.model.TripMarkerType
import com.chat.shutup.domain.util.RouteProgressCalculator
import com.chat.shutup.feature.trip.data.service.TripLocationForegroundService
import com.chat.shutup.feature.trip.presentation.util.VehicleSpriteProvider
import com.chat.shutup.feature.trip.presentation.state.MemberLocationState
import com.chat.shutup.feature.trip.presentation.state.RouteRequestState
import com.chat.shutup.feature.trip.presentation.state.TripMapUiState
import com.chat.shutup.domain.repository.TripChatRepository
import com.chat.shutup.domain.repository.TripPreferencesRepository
import com.chat.shutup.feature.chat.domain.model.Message
import com.chat.shutup.feature.chat.domain.model.MessageStatus
import com.chat.shutup.feature.chat.domain.model.MessageType
import com.chat.shutup.ui.navigation.Screen
import com.google.android.gms.maps.model.LatLng
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel(assistedFactory = TripMapViewModel.Factory::class)
//class TripMapViewModel @Inject constructor(
class TripMapViewModel @AssistedInject constructor(
    private val locationClient: LocationClient,
    private val tripLocationRepository: TripLocationRepository,
    private val tripRepository: TripRepository,
    private val tripChatRepository: TripChatRepository,
    private val tripPreferencesRepository: TripPreferencesRepository,
    private val routeRepository: RouteRepository,
    private val authRepository: AuthRepository,
    private val trackingRepository: TrackingRepository,
    val spriteProvider: VehicleSpriteProvider,
    @ApplicationContext private val context: Context,
//    savedStateHandle: SavedStateHandle
    @Assisted private val tripId : String
) : ViewModel() {

    @AssistedFactory
    interface Factory{
        fun create(tripId : String) : TripMapViewModel
    }
    /*val tripId: String = try {
        savedStateHandle.toRoute<Screen.TripMap>().tripId
    } catch (e: Exception) {
        ""
    }*/
    
    val currentUserId = authRepository.currentUser?.uid

    private val _uiState = MutableStateFlow(TripMapUiState())
    val uiState = _uiState.asStateFlow()

    private var locationJob: Job? = null
    private var membersJob: Job? = null
    private var tripJob: Job? = null
    private var chatJob: Job? = null
    private val offRouteStates = mutableMapOf<String, Boolean>()

    init {
        if (tripId.isNotEmpty()) {
            observeMembersAndLocations()
            observeTrip()
            observeTrackingState()
            observeChat()
        }
    }

    private fun observeChat() {
        chatJob?.cancel()
        val lastRead = tripPreferencesRepository.getLastReadChatTimestamp(tripId)
        _uiState.update { it.copy(lastReadTimestamp = lastRead) }

        chatJob = tripChatRepository.getMessages(tripId)
            .onEach { messages ->
                _uiState.update { state ->
                    val unread = messages.count { it.timestamp > state.lastReadTimestamp && it.senderId != currentUserId }
                    state.copy(
                        chatMessages = messages,
                        unreadCount = unread
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onChatExpand() {
        val now = System.currentTimeMillis()
        tripPreferencesRepository.setLastReadChatTimestamp(tripId, now)
        _uiState.update { it.copy(unreadCount = 0, lastReadTimestamp = now) }
    }

    fun onSendMessage(text: String) {
        if (text.isBlank() || currentUserId == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isChatSending = true) }
            val message = Message(
                text = text.trim(),
                senderId = currentUserId,
                timestamp = System.currentTimeMillis(),
                status = MessageStatus.SENT,
                type = MessageType.TEXT
            )
            tripChatRepository.sendMessage(tripId, message)
                .onSuccess {
                    _uiState.update { it.copy(isChatSending = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isChatSending = false, error = "Failed to send message: ${e.message}") }
                }
        }
    }

    private fun observeTrackingState() {
        combine(
            trackingRepository.activeTripId,
            trackingRepository.trackingStatus
        ) { activeId, status ->
            _uiState.update { 
                it.copy(
                    activeTrackingTripId = activeId,
                    trackingStatus = status
                )
            }
        }.launchIn(viewModelScope)
    }

    fun toggleTracking() {
        Log.d("TripTrackingDebug", "START_CLICKED: tripId=$tripId")
        val currentStatus = uiState.value.trackingStatus
        val activeId = uiState.value.activeTrackingTripId
        
        if (currentStatus == TrackingStatus.TRACKING && activeId == tripId) {
            stopTracking()
        } else if (activeId != null && activeId != tripId) {
            _uiState.update { it.copy(error = "Tracking already active for another trip") }
        } else {
            startTracking()
        }
    }

    private fun startTracking() {
        if (!_uiState.value.isPermissionGranted) {
            Log.d("TripTrackingDebug", "PERMISSION_CHECK: FAILED")
            return
        }
        Log.d("TripTrackingDebug", "PERMISSION_CHECK: SUCCESS")
        
        val intent = Intent(context, TripLocationForegroundService::class.java).apply {
            action = TripLocationForegroundService.ACTION_START
            putExtra(TripLocationForegroundService.EXTRA_TRIP_ID, tripId)
        }
        
        Log.d("TripTrackingDebug", "START_FOREGROUND_SERVICE: tripId=$tripId")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun stopTracking() {
        val intent = Intent(context, TripLocationForegroundService::class.java).apply {
            action = TripLocationForegroundService.ACTION_STOP
        }
        context.startService(intent)
    }

    fun onMarkerTypeSelected(markerType: TripMarkerType) {
        val userId = currentUserId ?: return
        if (tripId.isEmpty()) return

        viewModelScope.launch {
            try {
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

    fun onMapMoved(isUserInitiated: Boolean) {
        if (isUserInitiated) {
            _uiState.update { it.copy(isAutoCameraEnabled = false) }
        }
    }

    fun recenterCamera() {
        _uiState.update { it.copy(isAutoCameraEnabled = true) }
    }

    fun onSelectMember(memberState: MemberLocationState?) {
        _uiState.update { it.copy(selectedMember = memberState) }
    }

    fun onMapTypeSelected(mapType: Int) {
        _uiState.update { it.copy(mapType = mapType) }
    }

    private fun observeMembersAndLocations() {
        membersJob?.cancel()
        membersJob = combine(
            tripRepository.getTripMembers(tripId),
            tripLocationRepository.observeMemberLocations(tripId),
            _uiState.map { it.route }.distinctUntilChanged()
        ) { members, locations, route ->
            Log.d("TripVehicleDebug", "Observer emit: members=${members.size}, locations=${locations.keys}")
            // First, calculate current user's progress to compare others against
            val currentUserLocation = locations[currentUserId]
            val currentUserProgress = if (currentUserLocation != null && route != null) {
                RouteProgressCalculator.calculateProgress(
                    userId = currentUserId ?: "",
                    location = currentUserLocation,
                    routePoints = route.points,
                    totalDistanceMeters = route.distanceMeters
                ).progressDistanceMeters
            } else null

            members.map { member ->
                val location = locations[member.userId]
                val isStale = location?.let { 
                    System.currentTimeMillis() - it.timestamp > STALE_THRESHOLD_MS 
                } ?: true
                
                val progress = if (location != null && route != null) {
                    RouteProgressCalculator.calculateProgress(
                        userId = member.userId,
                        location = location,
                        routePoints = route.points,
                        totalDistanceMeters = route.distanceMeters,
                        currentUserProgress = if (member.userId != currentUserId) currentUserProgress else null
                    )
                } else null

                MemberLocationState(
                    member = member,
                    location = location,
                    progress = progress,
                    isStale = isStale
                )
            }
        }.onEach { memberStates ->
            val myState = memberStates.find { it.member.userId == currentUserId }
            val myProgress = myState?.progress
            val routePoints = _uiState.value.route?.points ?: emptyList()

            val decodedPoints = routePoints.map { LatLng(it.latitude, it.longitude) }

            val (completed, remaining) = if (myProgress != null && routePoints.isNotEmpty()) {
                val splitIndex = findClosestPointIndex(myProgress.progressDistanceMeters, routePoints)
                val comp = decodedPoints.take(splitIndex + 1)
                val rem = if (splitIndex < decodedPoints.size) decodedPoints.drop(splitIndex) else emptyList()
                comp to rem
            } else {
                emptyList<LatLng>() to decodedPoints
            }

            _uiState.update { state ->
                state.copy(
                    members = memberStates,
                    decodedRoutePoints = decodedPoints,
                    completedRoutePoints = completed,
                    remainingRoutePoints = remaining,
                    currentLocation = myState?.location ?: state.currentLocation
                )
            }
            
            handleOffRouteEvents(memberStates)
        }.launchIn(viewModelScope)
    }

    private fun findClosestPointIndex(progressMeters: Double, points: List<com.chat.shutup.domain.model.RoutePoint>): Int {
        var cumulative = 0.0
        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]
            val d = calculateDistance(p1.latitude, p1.longitude, p2.latitude, p2.longitude)
            if (cumulative + d >= progressMeters) return i
            cumulative += d
        }
        return points.size - 1
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return r * c
    }

    private fun handleOffRouteEvents(memberStates: List<MemberLocationState>) {
        memberStates.forEach { state ->
            val userId = state.member.userId
            val isOffRoute = state.progress?.isOffRoute == true
            val wasOffRoute = offRouteStates[userId] ?: false

            if (isOffRoute && !wasOffRoute) {
                // Member just went off route
                offRouteStates[userId] = true
                pushOffRouteEvent(state, true)
            } else if (!isOffRoute && wasOffRoute) {
                // Member just came back on route
                offRouteStates[userId] = false
                pushOffRouteEvent(state, false)
            }
        }
    }

    private fun pushOffRouteEvent(state: MemberLocationState, isOffRoute: Boolean) {
        viewModelScope.launch {
            tripRepository.pushTripEvent(
                tripId,
                com.chat.shutup.domain.model.TripNotificationData(
                    type = if (isOffRoute) com.chat.shutup.domain.model.TripNotificationType.MEMBER_OFF_ROUTE 
                           else com.chat.shutup.domain.model.TripNotificationType.MEMBER_BACK_ON_ROUTE,
                    tripId = tripId,
                    tripName = "",
                    actorUserId = state.member.userId,
                    actorName = state.member.name,
                    title = if (isOffRoute) "Off Route" else "Back on Route",
                    body = if (isOffRoute) "${state.member.name} is off the planned route" 
                           else "${state.member.name} is back on the planned route"
                )
            )
        }
    }

    private fun observeTrip() {
        tripJob?.cancel()
        tripJob = tripRepository.getTrip(tripId)
            .onEach { trip ->
                trip?.let { t ->
                    _uiState.update { it.copy(trip = t, route = t.route) }
                    
                    // Trigger fetch only if route is missing and we haven't tried/failed yet
                    if (t.origin != null && t.destination != null && 
                        t.route == null && 
                        _uiState.value.routeRequestState == RouteRequestState.IDLE) {
                        fetchRoute(t)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun retryRouteFetch() {
        _uiState.update { it.copy(routeRequestState = RouteRequestState.IDLE, routeError = null) }
        viewModelScope.launch {
            val tripNow = tripRepository.getTrip(tripId).firstOrNull()
            tripNow?.let { fetchRoute(it) }
        }
    }

    private fun fetchRoute(trip: Trip) {
        val origin = trip.origin ?: return
        val destination = trip.destination ?: return
        
        if (_uiState.value.routeRequestState == RouteRequestState.LOADING) return

        viewModelScope.launch {
            _uiState.update { it.copy(routeRequestState = RouteRequestState.LOADING, routeError = null) }
            val result = routeRepository.getRoute(origin, destination, trip.travelMode.name)
            result.onSuccess { route ->
                _uiState.update { 
                    it.copy(
                        route = route, 
                        routeRequestState = RouteRequestState.SUCCESS, 
                        routeError = null 
                    )
                }
                if (trip.creatorId == currentUserId) {
                    Log.d("TripTrackingDebug", "Attempting to update route in Firebase. TripId: $tripId, CreatorId: ${trip.creatorId}, CurrentUserId: $currentUserId")
                    viewModelScope.launch {
                        try {
                            tripRepository.updateTripRoute(tripId, route)
                            Log.d("TripTrackingDebug", "Route update successful in Firebase")
                        } catch (e: Exception) {
                            Log.e("TripTrackingDebug", "Failed to update route in Firebase: ${e.message}", e)
                        }
                    }
                } else {
                    Log.d("TripTrackingDebug", "Skipping route update in Firebase - user is not creator. CreatorId: ${trip.creatorId}, CurrentUserId: $currentUserId")
                }
            }
            .onFailure { e ->
                _uiState.update { 
                    it.copy(
                        routeRequestState = RouteRequestState.ERROR, 
                        routeError = e.message ?: "Failed to calculate route"
                    ) 
                }
            }
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        _uiState.update { it.copy(isPermissionGranted = isGranted) }
    }

    override fun onCleared() {
        super.onCleared()
        membersJob?.cancel()
        tripJob?.cancel()
        chatJob?.cancel()
    }

    companion object {
        private const val STALE_THRESHOLD_MS = 60000L // 1 minute
    }
}
