package com.chat.shutup.feature.trip.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.shutup.domain.model.TripLocation
import com.chat.shutup.domain.repository.LocationSearchRepository
import com.chat.shutup.feature.trip.presentation.state.LocationPickerUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationPickerViewModel @Inject constructor(
    private val locationSearchRepository: LocationSearchRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(LocationPickerUiState())
    val uiState = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query, error = null) }
        
        searchJob?.cancel()
        if (query.length < 3) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(500) // Debounce
            _uiState.update { it.copy(isSearching = true) }
            locationSearchRepository.search(query)
                .onSuccess { results ->
                    _uiState.update { it.copy(searchResults = results, isSearching = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isSearching = false) }
                }
        }
    }

    fun onLocationSelected(location: TripLocation) {
        _uiState.update { 
            it.copy(
                selectedLocation = location,
                query = location.address,
                searchResults = emptyList()
            ) 
        }
    }

    fun reverseGeocode(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            locationSearchRepository.reverseGeocode(latitude, longitude)
                .onSuccess { address ->
                    _uiState.update { 
                        it.copy(
                            selectedLocation = TripLocation(latitude, longitude, address)
                        ) 
                    }
                }
        }
    }
}
