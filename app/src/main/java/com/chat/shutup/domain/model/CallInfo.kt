package com.chat.shutup.domain.model

data class CallInfo(
    val callId: String = "",
    val callerId: String = "",
    val callerName: String = "",
    val callerImageUrl: String? = null,
    val receiverId: String = "",
    val receiverName: String = "",
    val receiverImageUrl: String? = null,
    val type: CallType = CallType.VOICE,
    val status: CallStatus = CallStatus.RINGING,
    val channelId: String = "",
    val token: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class CallType {
    VOICE, VIDEO
}

enum class CallStatus {
    RINGING, ACCEPTED, REJECTED, ENDED
}
