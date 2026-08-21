package com.chat.shutup.domain.model

data class ChatMessage(
    val id: Int? = null,
    val text: String,
    val senderId: String,
    val timestamp: Long
)
