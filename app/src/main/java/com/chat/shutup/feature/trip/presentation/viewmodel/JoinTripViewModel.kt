package com.chat.shutup.feature.trip.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.shutup.domain.repository.AuthRepository
import com.chat.shutup.domain.repository.TripRepository
import com.chat.shutup.domain.usecase.AlreadyMemberException
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
    private val joinTripUseCase: JoinTripUseCase,
    private val tripRepository: TripRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JoinTripUiState())
    val uiState = _uiState.asStateFlow()

    fun onInviteCodeChange(code: String) {
        _uiState.update { it.copy(inviteCode = code, error = null) }
    }

    fun startScanning() {
        _uiState.update { it.copy(isScanning = true, error = null) }
    }

    fun stopScanning() {
        _uiState.update { it.copy(isScanning = false) }
    }

    fun onCodeScanned(code: String) {
        if (_uiState.value.isLoading) return
        
        // Extract code if it's a URL (e.g. from a deep link or web QR)
        val extractedCode = if (code.contains("code=")) {
            code.substringAfter("code=").substringBefore("&").take(6).uppercase()
        } else {
            code.trim().uppercase()
        }

        if (extractedCode.length != 6) {
            _uiState.update { it.copy(isScanning = false, error = "Invalid QR code format") }
            return
        }

        // Just pre-fill the code and let the user tap Join
        _uiState.update { 
            it.copy(
                isScanning = false, 
                inviteCode = extractedCode, 
                error = null,
                showJoinConfirmation = false // Ensure we don't show the old broken confirmation
            )
        }
    }

    fun confirmJoin() {
        val code = _uiState.value.inviteCode
        _uiState.update { it.copy(showJoinConfirmation = false) }
        onJoinTrip(code)
    }

    fun dismissConfirmation() {
        _uiState.update { it.copy(showJoinConfirmation = false, scannedTrip = null) }
    }

    fun onJoinTrip(code: String = _uiState.value.inviteCode) {
        val trimmedCode = code.trim().uppercase()
        if (trimmedCode.isBlank()) {
            _uiState.update { it.copy(error = "Please enter an invite code") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isAlreadyMember = false) }
            joinTripUseCase(trimmedCode)
                .onSuccess { trip ->
                    _uiState.update { it.copy(isLoading = false, joinedTrip = trip) }
                }
                .onFailure { e ->
                    if (e is AlreadyMemberException) {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                scannedTrip = e.trip,
                                showJoinConfirmation = true,
                                isAlreadyMember = true,
                                error = "You're already a member of this trip."
                            ) 
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to join trip") }
                    }
                }
        }
    }
}
