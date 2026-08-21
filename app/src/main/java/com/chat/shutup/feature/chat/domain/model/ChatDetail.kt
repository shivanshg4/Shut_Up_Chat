package com.chat.shutup.feature.chat.domain.model

data class ChatDetail(
    val id: String,
    val participantName: String,
    val participantImageUrl: String?,
    val online: Boolean,
    val lastSeen: String? = null
)
