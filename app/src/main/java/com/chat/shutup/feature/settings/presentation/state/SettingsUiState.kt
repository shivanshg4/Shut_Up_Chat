package com.chat.shutup.feature.settings.presentation.state

import com.chat.shutup.domain.model.User

data class SettingsUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isBackgroundAnimationEnabled: Boolean = true,
    val isInteractiveNatureEnabled: Boolean = true,
    val units: String = "km"
)
