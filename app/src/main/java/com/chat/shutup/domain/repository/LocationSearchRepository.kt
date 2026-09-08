package com.chat.shutup.domain.repository

import com.chat.shutup.domain.model.TripLocation

interface LocationSearchRepository {
    suspend fun search(query: String): Result<List<TripLocation>>
    suspend fun reverseGeocode(latitude: Double, longitude: Double): Result<String>
}
