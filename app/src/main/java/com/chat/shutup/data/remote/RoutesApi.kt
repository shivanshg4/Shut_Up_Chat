package com.chat.shutup.data.remote

import com.chat.shutup.data.remote.dto.RoutesRequest
import com.chat.shutup.data.remote.dto.RoutesResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface RoutesApi {

    @POST("directions/v2:computeRoutes")
    suspend fun computeRoutes(
        @Header("X-Goog-Api-Key") apiKey: String,
        @Header("X-Goog-FieldMask") fieldMask: String = "routes.distanceMeters,routes.duration,routes.polyline.encodedPolyline",
        @Header("X-Android-Package") packageName: String,
        @Header("X-Android-Cert") certFingerprint: String,
        @Body request: RoutesRequest
    ): RoutesResponse

    companion object {
        const val BASE_URL = "https://routes.googleapis.com/"
    }
}
