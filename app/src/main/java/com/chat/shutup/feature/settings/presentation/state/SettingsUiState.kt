package com.chat.shutup.feature.settings.presentation.state

import com.chat.shutup.feature.auth.domain.model.User
import com.chat.shutup.feature.trip.domain.repository.AppThemeMode

data class SettingsUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isBackgroundAnimationEnabled: Boolean = true,
    val isInteractiveNatureEnabled: Boolean = true,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val units: String = "km"
)
