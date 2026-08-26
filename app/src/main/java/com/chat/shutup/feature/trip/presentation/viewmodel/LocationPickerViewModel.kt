package com.chat.shutup.feature.trip.presentation.viewmodel

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LocationPickerViewModel @Inject constructor(): ViewModel() {

    private var _uiState = MutableStateFlow(TextFieldState(initialText = "Hello User!!"))
    val uiState = _uiState.asStateFlow()

    init{

    }
}