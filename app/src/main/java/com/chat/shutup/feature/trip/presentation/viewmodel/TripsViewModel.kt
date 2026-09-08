package com.chat.shutup.feature.trip.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.shutup.domain.model.MemberRouteProgress
import com.chat.shutup.domain.model.Trip
import com.chat.shutup.domain.model.User
import com.chat.shutup.domain.repository.AuthRepository
import com.chat.shutup.domain.repository.ChatRepository
import com.chat.shutup.domain.repository.TrackingRepository
import com.chat.shutup.domain.repository.TripLocationRepository
import com.chat.shutup.domain.repository.TripPreferencesRepository
import com.chat.shutup.domain.repository.TripRepository
import com.chat.shutup.domain.usecase.LeaveTripUseCase
import com.chat.shutup.domain.util.RouteProgressCalculator
import com.chat.shutup.feature.trip.data.service.TripLocationForegroundService
import com.chat.shutup.feature.trip.presentation.state.TripsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TripsViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val authRepository: AuthRepository,
    private val trackingRepository: TrackingRepository,
    private val chatRepository: ChatRepository,
    private val tripLocationRepository: TripLocationRepository,
    private val tripPreferencesRepository: TripPreferencesRepository,
    private val leaveTripUseCase: LeaveTripUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripsUiState())
    val uiState = _uiState.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _activeTripProgress = MutableStateFlow<MemberRouteProgress?>(null)
    val activeTripProgress = _activeTripProgress.asStateFlow()

    val activeTripId = trackingRepository.activeTripId

    val currentUserId: String?
        get() = authRepository.currentUser?.uid

    init {
        loadTrips()
        loadUserProfile()
        observeActiveTripProgress()
        loadPreferences()
    }

    private fun loadPreferences() {
        _uiState.update { 
            it.copy(
                isBackgroundAnimationEnabled = tripPreferencesRepository.isBackgroundAnimationEnabled(),
                isInteractiveNatureEnabled = tripPreferencesRepository.isInteractiveNatureEnabled()
            )
        }
    }

    private fun loadUserProfile() {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            chatRepository.getUserProfile(uid).onSuccess { u ->
                _user.value = u
            }
        }
    }

    private fun observeActiveTripProgress() {
        activeTripId.flatMapLatest { id ->
            if (id == null) flowOf(null)
            else {
                combine(
                    tripRepository.getTrip(id),
                    tripLocationRepository.observeMemberLocations(id)
                ) { trip, locations ->
                    val uid = currentUserId ?: return@combine null
                    val location = locations[uid] ?: return@combine null
                    val route = trip?.route ?: return@combine null
                    
                    RouteProgressCalculator.calculateProgress(
                        userId = uid,
                        location = location,
                        routePoints = route.points,
                        totalDistanceMeters = route.distanceMeters
                    )
                }
            }
        }.onEach { progress ->
            _activeTripProgress.value = progress
        }.launchIn(viewModelScope)
    }

    private fun loadTrips() {
        val user = authRepository.currentUser ?: return
        
        tripRepository.getUserTrips(user.uid)
            .onStart { _uiState.update { it.copy(isLoading = true) } }
            .onEach { trips ->
                _uiState.update { it.copy(trips = trips, isLoading = false) }
            }
            .catch { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun onDeleteTrip(tripId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            tripRepository.deleteTrip(tripId)
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                }
        }
    }

    fun onLeaveTrip(tripId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // 1. If leaving active trip, stop tracking
            if (tripId == trackingRepository.activeTripId.value) {
                val intent = Intent(context, TripLocationForegroundService::class.java).apply {
                    action = TripLocationForegroundService.ACTION_STOP
                }
                context.startService(intent)
            }
            
            // 2. Leave trip
            leaveTripUseCase(tripId)
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                }
        }
    }
}
