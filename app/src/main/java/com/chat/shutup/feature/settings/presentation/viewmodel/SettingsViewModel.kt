package com.chat.shutup.feature.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.shutup.domain.model.TripMarkerType
import com.chat.shutup.domain.model.User
import com.chat.shutup.domain.repository.AuthRepository
import com.chat.shutup.domain.repository.ChatRepository
import com.chat.shutup.domain.repository.TripPreferencesRepository
import com.chat.shutup.feature.settings.presentation.state.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val tripPreferencesRepository: TripPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserProfile()
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
        val currentUser = authRepository.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            chatRepository.getUserProfile(currentUser.uid)
                .onSuccess { user ->
                    _uiState.update { it.copy(user = user, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    fun onUpdateNickname(nickname: String) {
        _uiState.update { it.copy(user = it.user?.copy(nickname = nickname)) }
    }

    fun onUpdateContactNumber(contact: String) {
        _uiState.update { it.copy(user = it.user?.copy(contactNumber = contact)) }
    }

    fun onUpdateFavoriteVehicle(vehicle: TripMarkerType) {
        _uiState.update { it.copy(user = it.user?.copy(favoriteVehicle = vehicle)) }
    }

    fun onToggleAnimation(enabled: Boolean) {
        tripPreferencesRepository.setBackgroundAnimationEnabled(enabled)
        _uiState.update { it.copy(isBackgroundAnimationEnabled = enabled) }
    }

    fun onToggleInteractiveNature(enabled: Boolean) {
        tripPreferencesRepository.setInteractiveNatureEnabled(enabled)
        _uiState.update { it.copy(isInteractiveNatureEnabled = enabled) }
    }

    fun saveSettings() {
        val user = _uiState.value.user ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            chatRepository.updateCurrentUserProfile(user)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSaving = false, error = e.message) }
                }
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onSignedOut()
        }
    }
}
