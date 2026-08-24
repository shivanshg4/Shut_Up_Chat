package com.chat.shutup.feature.trip.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.shutup.domain.repository.LocationClient
import com.chat.shutup.feature.trip.presentation.state.TripMapUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripMapViewModel @Inject constructor(
    private val locationClient: LocationClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripMapUiState())
    val uiState = _uiState.asStateFlow()

    private var locationJob: Job? = null

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
        _uiState.update { it.copy(isLoading = true) }
        
        locationJob = locationClient.getLocationUpdates(5000L)
            .onEach { location ->
                _uiState.update { 
                    it.copy(
                        currentLocation = location,
                        isLoading = false,
                        error = null
                    )
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
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}
