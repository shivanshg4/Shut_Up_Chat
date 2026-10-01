package com.chat.shutup.feature.chat.domain.model

data class ChatMessage(
    val id: Int? = null,
    val text: String,
    val senderId: String,
    val timestamp: Long
)
