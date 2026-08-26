package com.chat.shutup.feature.trip.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.shutup.domain.repository.AuthRepository
import com.chat.shutup.domain.repository.TrackingRepository
import com.chat.shutup.domain.repository.TrackingStatus
import com.chat.shutup.domain.repository.TripRepository
import com.chat.shutup.feature.trip.data.service.TripLocationForegroundService
import com.chat.shutup.feature.trip.presentation.state.TripDetailsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripDetailsViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val authRepository: AuthRepository,
    private val trackingRepository: TrackingRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val tripId: String = checkNotNull(savedStateHandle["tripId"])

    private val _uiState = MutableStateFlow(TripDetailsUiState())
    val uiState = _uiState.asStateFlow()

    val currentUserId: String?
        get() = authRepository.currentUser?.uid

    init {
        loadTripDetails()
        observeTrackingState()
    }
    
    private fun observeTrackingState() {
        combine(
            trackingRepository.activeTripId,
            trackingRepository.trackingStatus
        ) { activeId, status ->
            _uiState.update { 
                it.copy(
                    isTrackingActive = status == TrackingStatus.TRACKING && activeId == tripId,
                    activeTrackingTripId = activeId
                )
            }
        }.launchIn(viewModelScope)
    }

    fun toggleTracking() {
        val activeId = uiState.value.activeTrackingTripId
        val isTrackingCurrent = uiState.value.isTrackingActive
        
        if (isTrackingCurrent) {
            stopTracking()
        } else if (activeId != null && activeId != tripId) {
            _uiState.update { it.copy(error = "Tracking already active for another trip") }
        } else {
            startTracking()
        }
    }

    private fun startTracking() {
        val intent = Intent(context, TripLocationForegroundService::class.java).apply {
            action = TripLocationForegroundService.ACTION_START
            putExtra(TripLocationForegroundService.EXTRA_TRIP_ID, tripId)
        }
        
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

    fun onDeleteTrip(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            tripRepository.deleteTrip(tripId)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    private fun loadTripDetails() {
        tripRepository.getTrip(tripId)
            .onStart { _uiState.update { it.copy(isLoading = true) } }
            .onEach { trip ->
                _uiState.update { it.copy(trip = trip, isLoading = false) }
            }
            .catch { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
            .launchIn(viewModelScope)

        tripRepository.getTripMembers(tripId)
            .catch { }
            .launchIn(viewModelScope)
    }
}
