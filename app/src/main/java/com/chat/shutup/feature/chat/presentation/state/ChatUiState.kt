package com.chat.shutup.feature.chat.presentation.state

import com.chat.shutup.feature.chat.domain.model.ChatDetail
import com.chat.shutup.feature.chat.domain.model.Message

data class ChatUiState(
    val isLoading: Boolean = false,
    val chatDetail: ChatDetail? = null,
    val messages: List<Message> = emptyList(),
    val messageInput: String = "",
    val isTyping: Boolean = false,
    val currentUserId: String = "",
    val error: String? = null
)
