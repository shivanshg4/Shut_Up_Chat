package com.chat.shutup.data.repository

import android.content.Context
import android.util.Log
import com.chat.shutup.BuildConfig
import com.chat.shutup.data.mapper.PolylineDecoder
import com.chat.shutup.data.remote.RoutesApi
import com.chat.shutup.data.remote.dto.*
import com.chat.shutup.domain.model.RoutePoint
import com.chat.shutup.domain.model.TripLocation
import com.chat.shutup.domain.model.TripRoute
import com.chat.shutup.domain.repository.RouteRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.HttpException
import javax.inject.Inject

class RouteRepositoryImpl @Inject constructor(
    private val routesApi: RoutesApi,
    @ApplicationContext private val context: Context
) : RouteRepository {

    override suspend fun getRoute(
        origin: TripLocation,
        destination: TripLocation,
        travelMode: String
    ): Result<TripRoute> {
        return try {
            val mappedTravelMode = when (travelMode.uppercase()) {
                "DRIVING" -> "DRIVE"
                "WALKING" -> "WALK"
                "BICYCLING" -> "BICYCLE"
                "TRANSIT" -> "TRANSIT"
                else -> "DRIVE"
            }

            val request = RoutesRequest(
                origin = Waypoint(Location(LatLngDto(origin.latitude, origin.longitude))),
                destination = Waypoint(Location(LatLngDto(destination.latitude, destination.longitude))),
                travelMode = mappedTravelMode
            )

            // Identity headers for Android-restricted API keys
            val packageName = context.packageName
            // Use your local SHA-1 fingerprint here (from ./gradlew signingReport)
            val certFingerprint = "595323a9dd079b4264132ce48c069549de29d714"
            
            // In a real production app, this fingerprint should probably also be in local.properties
            // or handled by a secure backend that generates the request.

            val response = routesApi.computeRoutes(
                apiKey = BuildConfig.Routes_API_KEY,
                packageName = packageName,
                certFingerprint = certFingerprint,
                request = request
            )

            val routeDto = response.routes.firstOrNull()
            if (routeDto != null && routeDto.polyline?.encodedPolyline != null) {
                val encodedPolyline = routeDto.polyline.encodedPolyline
                val points = PolylineDecoder.decode(encodedPolyline)
                
                // Parse duration: "96s" -> 96
                val durationSeconds = routeDto.duration?.removeSuffix("s")?.toLongOrNull() ?: 0L
                
                Result.success(
                    TripRoute(
                        distanceMeters = routeDto.distanceMeters ?: 0,
                        durationSeconds = durationSeconds,
                        encodedPolyline = encodedPolyline,
                        points = points
                    )
                )
            } else {
                Result.failure(Exception("No route found"))
            }
        } catch (e: Exception) {
            if (e is HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("TripTrackingDebug", "Routes API error: HTTP ${e.code()}")
                Log.e("TripTrackingDebug", "code=${e.code()}")
                Log.e("TripTrackingDebug", "message=${e.message()}")
                Log.e("TripTrackingDebug", "body=$errorBody")
                
                // Construct a more descriptive error message from the body if possible
                val detailedMessage = "Routes API: HTTP ${e.code()} - $errorBody"
                Result.failure(Exception(detailedMessage))
            } else {
                Log.e("TripTrackingDebug", "Routes API error: ${e.message}")
                Result.failure(e)
            }
        }
    }
}
