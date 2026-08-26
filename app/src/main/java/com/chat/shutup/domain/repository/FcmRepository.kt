package com.chat.shutup.domain.repository

interface FcmRepository {
    suspend fun updateToken(token: String): Result<Unit>
}
