package com.chat.shutup.domain.model

data class Trip(
    val id: String,
    val name: String,
    val creatorId: String,
    val inviteCode: String,
    val createdAt: Long = System.currentTimeMillis(),
    val members: List<TripMember> = emptyList()
)

data class TripMember(
    val userId: String,
    val name: String,
    val role: TripRole
)

enum class TripRole {
    CREATOR,
    MEMBER
}
