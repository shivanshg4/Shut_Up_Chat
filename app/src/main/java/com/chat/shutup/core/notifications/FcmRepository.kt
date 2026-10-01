package com.chat.shutup.core.notifications

interface FcmRepository {
    suspend fun updateToken(token: String): Result<Unit>
}
