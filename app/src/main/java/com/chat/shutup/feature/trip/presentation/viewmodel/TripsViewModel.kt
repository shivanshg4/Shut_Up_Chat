package com.chat.shutup.feature.trip.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.shutup.domain.repository.AuthRepository
import com.chat.shutup.domain.repository.TripRepository
import com.chat.shutup.feature.trip.presentation.state.TripsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class TripsViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadTrips()
    }

    private fun loadTrips() {
        val user = authRepository.currentUser ?: return
        
        tripRepository.getTripsByCreator(user.uid)
            .onStart { _uiState.update { it.copy(isLoading = true) } }
            .onEach { trips ->
                _uiState.update { it.copy(trips = trips, isLoading = false) }
            }
            .catch { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }
}
