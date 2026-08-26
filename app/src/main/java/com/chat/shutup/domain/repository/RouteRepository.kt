package com.chat.shutup.domain.repository

import com.chat.shutup.domain.model.TripLocation
import com.chat.shutup.domain.model.TripRoute

interface RouteRepository {
    suspend fun getRoute(
        origin: TripLocation,
        destination: TripLocation,
        travelMode: String = "DRIVE"
    ): Result<TripRoute>
}
