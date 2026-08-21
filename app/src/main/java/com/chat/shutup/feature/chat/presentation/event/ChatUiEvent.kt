package com.chat.shutup.feature.chat.presentation.event

sealed interface ChatUiEvent {
    data class OnMessageChange(val message: String) : ChatUiEvent
    data object OnSendMessage : ChatUiEvent
    data object OnBackClick : ChatUiEvent
    data object OnCallClick : ChatUiEvent
    data object OnVideoCallClick : ChatUiEvent
    data object OnMoreClick : ChatUiEvent
    data class OnReactionClick(val messageId: String, val reaction: String) : ChatUiEvent
    data object OnAttachmentClick : ChatUiEvent
    data object OnCameraClick : ChatUiEvent
    data object OnEmojiClick : ChatUiEvent
    data object OnVoiceMessageRecord : ChatUiEvent
}
