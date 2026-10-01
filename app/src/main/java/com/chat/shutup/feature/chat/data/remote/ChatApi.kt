package com.chat.shutup.feature.chat.data.remote

import retrofit2.http.GET

interface ChatApi {
    @GET("messages")
    suspend fun getMessages(): List<String> // Placeholder DTO

    companion object {
        const val BASE_URL = "https://placeholder-api.com/"
    }
}
