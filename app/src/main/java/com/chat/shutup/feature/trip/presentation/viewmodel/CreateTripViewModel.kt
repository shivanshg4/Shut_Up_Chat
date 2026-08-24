package com.chat.shutup.feature.trip.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.shutup.domain.usecase.CreateTripUseCase
import com.chat.shutup.feature.trip.presentation.state.CreateTripUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateTripViewModel @Inject constructor(
    private val createTripUseCase: CreateTripUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTripUiState())
    val uiState = _uiState.asStateFlow()

    fun onTripNameChange(name: String) {
        _uiState.update { it.copy(tripName = name, error = null) }
    }

    fun onCreateTrip() {
        val name = _uiState.value.tripName
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Trip name cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            createTripUseCase(name)
                .onSuccess { trip ->
                    _uiState.update { it.copy(isLoading = false, createdTrip = trip) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }
}
