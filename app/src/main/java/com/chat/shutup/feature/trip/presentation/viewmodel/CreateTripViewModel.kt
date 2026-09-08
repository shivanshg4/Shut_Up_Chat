package com.chat.shutup.feature.trip.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.shutup.domain.model.TravelMode
import com.chat.shutup.domain.model.Trip
import com.chat.shutup.domain.model.TripLocation
import com.chat.shutup.domain.model.TripMember
import com.chat.shutup.domain.model.TripRole
import com.chat.shutup.domain.model.TripMarkerType
import com.chat.shutup.domain.repository.AuthRepository
import com.chat.shutup.domain.repository.ChatRepository
import com.chat.shutup.domain.repository.TripRepository
import com.chat.shutup.feature.trip.presentation.state.CreateTripUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateTripViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTripUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Pre-generate trip ID and invite code for this session
        val id = UUID.randomUUID().toString()
        val code = generateInviteCode()
        _uiState.update { it.copy(tripId = id, inviteCode = code) }
    }

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    fun onTripNameChange(name: String) {
        _uiState.update { it.copy(tripName = name, error = null) }
    }

    fun onOriginChange(location: TripLocation) {
        _uiState.update { it.copy(origin = location, error = null) }
    }

    fun onDestinationChange(location: TripLocation) {
        _uiState.update { it.copy(destination = location, error = null) }
    }

    fun onTravelModeChange(mode: TravelMode) {
        _uiState.update { it.copy(travelMode = mode) }
    }

    fun onCreateTrip() {
        val state = _uiState.value
        if (state.tripName.isBlank()) {
            _uiState.update { it.copy(error = "Trip name cannot be empty") }
            return
        }
        if (state.origin == null) {
            _uiState.update { it.copy(error = "Please select a start location") }
            return
        }
        if (state.destination == null) {
            _uiState.update { it.copy(error = "Please select a destination") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val firebaseUser = authRepository.currentUser
            if (firebaseUser == null) {
                _uiState.update { it.copy(isLoading = false, error = "User not authenticated") }
                return@launch
            }

            // Fetch full domain User object
            val user = chatRepository.getUserProfile(firebaseUser.uid).getOrNull()

            val trip = Trip(
                id = state.tripId,
                name = state.tripName.trim(),
                creatorId = firebaseUser.uid,
                inviteCode = state.inviteCode,
                origin = state.origin,
                destination = state.destination,
                travelMode = state.travelMode,
                members = listOf(
                    TripMember(
                        userId = firebaseUser.uid,
                        name = user?.nickname ?: user?.name ?: firebaseUser.displayName ?: "Creator",
                        role = TripRole.CREATOR,
                        markerType = user?.favoriteVehicle ?: TripMarkerType.DEFAULT
                    )
                )
            )

            try {
                tripRepository.createTrip(trip)
                _uiState.update { it.copy(isLoading = false, createdTrip = trip, isSynced = true) }
            } catch (e: Exception) {
                // Trip is saved locally by repo even if Firebase fails
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        createdTrip = trip,
                        isSynced = false,
                        error = "Saved locally. Firebase error: ${e.message}"
                    ) 
                }
            }
        }
    }
}
