package com.chat.shutup.feature.trip.presentation.state

import com.chat.shutup.domain.model.TripLocation

data class LocationPickerUiState(
    val query: String = "",
    val searchResults: List<TripLocation> = emptyList(),
    val isSearching: Boolean = false,
    val selectedLocation: TripLocation? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
