package com.chat.shutup.data.mapper

import com.chat.shutup.data.local.entity.ChatMessageEntity
import com.chat.shutup.domain.model.ChatMessage

fun ChatMessageEntity.toChatMessage(): ChatMessage {
    return ChatMessage(
        id = id,
        text = text,
        senderId = senderId,
        timestamp = timestamp
    )
}

fun ChatMessage.toChatMessageEntity(): ChatMessageEntity {
    return ChatMessageEntity(
        id = id ?: 0,
        text = text,
        senderId = senderId,
        timestamp = timestamp
    )
}
