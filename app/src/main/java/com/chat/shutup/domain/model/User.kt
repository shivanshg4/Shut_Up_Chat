package com.chat.shutup.domain.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val imageUrl: String? = null,
    val fcmToken: String? = null,
    val online: Boolean = false,
    val lastSeen: Long = 0L
)
