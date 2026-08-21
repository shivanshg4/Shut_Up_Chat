package com.chat.shutup.feature.chat.presentation.effect

sealed interface ChatUiEffect {
    data object NavigateBack : ChatUiEffect
    data class ShowError(val message: String) : ChatUiEffect
    data object OpenImagePicker : ChatUiEffect
    data object OpenCamera : ChatUiEffect
}
