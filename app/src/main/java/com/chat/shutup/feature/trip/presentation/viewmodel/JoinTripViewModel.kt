package com.chat.shutup.feature.trip.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.shutup.domain.usecase.JoinTripUseCase
import com.chat.shutup.feature.trip.presentation.state.JoinTripUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JoinTripViewModel @Inject constructor(
    private val joinTripUseCase: JoinTripUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(JoinTripUiState())
    val uiState = _uiState.asStateFlow()

    fun onInviteCodeChange(code: String) {
        _uiState.update { it.copy(inviteCode = code, error = null) }
    }

    fun onJoinTrip() {
        val code = _uiState.value.inviteCode
        if (code.isBlank()) {
            _uiState.update { it.copy(error = "Please enter an invite code") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            joinTripUseCase(code)
                .onSuccess { trip ->
                    _uiState.update { it.copy(isLoading = false, joinedTrip = trip) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }
}
