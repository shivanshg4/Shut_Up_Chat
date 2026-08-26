package com.chat.shutup.feature.trip.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.shutup.domain.repository.AuthRepository
import com.chat.shutup.domain.repository.TripRepository
import com.chat.shutup.feature.trip.presentation.state.TripDetailsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripDetailsViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId: String = checkNotNull(savedStateHandle["tripId"])

    private val _uiState = MutableStateFlow(TripDetailsUiState())
    val uiState = _uiState.asStateFlow()

    val currentUserId: String?
        get() = authRepository.currentUser?.uid

    init {
        loadTripDetails()
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
        // Observe local trip details
        tripRepository.getTrip(tripId)
            .onStart { _uiState.update { it.copy(isLoading = true) } }
            .onEach { trip ->
                _uiState.update { it.copy(trip = trip, isLoading = false) }
            }
            .catch { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
            .launchIn(viewModelScope)

        // Observe remote member updates and sync to local
        tripRepository.getTripMembers(tripId)
            .catch { /* Handle silent error for sync */ }
            .launchIn(viewModelScope)
    }
}
