package com.chat.shutup.feature.trip.domain.repository

import com.chat.shutup.feature.trip.domain.model.TripLocation
import com.chat.shutup.feature.trip.domain.model.TripRoute

interface RouteRepository {
    suspend fun getRoute(
        origin: TripLocation,
        destination: TripLocation,
        travelMode: String = "DRIVE"
    ): Result<TripRoute>
}
