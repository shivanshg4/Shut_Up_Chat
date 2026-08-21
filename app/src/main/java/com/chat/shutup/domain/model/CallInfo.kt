package com.chat.shutup.domain.model

data class CallInfo(
    val callId: String = "",
    val callerId: String = "",
    val callerName: String = "",
    val callerImageUrl: String? = null,
    val receiverId: String = "",
    val type: CallType = CallType.VOICE,
    val status: CallStatus = CallStatus.RINGING,
    val timestamp: Long = System.currentTimeMillis()
)

enum class CallType {
    VOICE, VIDEO
}

enum class CallStatus {
    RINGING, ACCEPTED, REJECTED, ENDED
}
