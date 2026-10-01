package com.chat.shutup.feature.trip.domain.repository

import com.chat.shutup.feature.trip.domain.model.TripLocation

interface LocationSearchRepository {
    suspend fun search(query: String): Result<List<TripLocation>>
    suspend fun reverseGeocode(latitude: Double, longitude: Double): Result<String>
}
