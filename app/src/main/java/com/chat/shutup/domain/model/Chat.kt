package com.chat.shutup.domain.model

data class Chat(
    val id: String = "",
    val participantId: String = "",
    val participantName: String = "",
    val participantImageUrl: String? = null,
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0L,
    val unreadCount: Int = 0,
    val online: Boolean = false
)
