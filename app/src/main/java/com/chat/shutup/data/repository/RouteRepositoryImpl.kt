package com.chat.shutup.data.repository

import com.chat.shutup.BuildConfig
import com.chat.shutup.data.mapper.PolylineDecoder
import com.chat.shutup.data.remote.RoutesApi
import com.chat.shutup.data.remote.dto.*
import com.chat.shutup.domain.model.RoutePoint
import com.chat.shutup.domain.model.TripLocation
import com.chat.shutup.domain.model.TripRoute
import com.chat.shutup.domain.repository.RouteRepository
import javax.inject.Inject

class RouteRepositoryImpl @Inject constructor(
    private val routesApi: RoutesApi
) : RouteRepository {

    override suspend fun getRoute(
        origin: TripLocation,
        destination: TripLocation,
        travelMode: String
    ): Result<TripRoute> {
        return try {
            val request = RoutesRequest(
                origin = Waypoint(Location(LatLngDto(origin.latitude, origin.longitude))),
                destination = Waypoint(Location(LatLngDto(destination.latitude, destination.longitude))),
                travelMode = travelMode
            )

            val response = routesApi.computeRoutes(
                apiKey = BuildConfig.MAPS_API_KEY,
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
            Result.failure(e)
        }
    }
}
