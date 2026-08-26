package com.chat.shutup.feature.trip.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.shutup.domain.repository.AuthRepository
import com.chat.shutup.domain.repository.TripRepository
import com.chat.shutup.feature.trip.presentation.state.TripsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripsViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripsUiState())
    val uiState = _uiState.asStateFlow()

    val currentUserId: String?
        get() = authRepository.currentUser?.uid

    init {
        loadTrips()
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
}
