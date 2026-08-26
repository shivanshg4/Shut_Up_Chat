package com.chat.shutup.domain.repository

import com.chat.shutup.domain.model.LocationPoint
import kotlinx.coroutines.flow.Flow

interface TripLocationRepository {
    suspend fun updateMyLocation(tripId: String, userId: String, location: LocationPoint)
    fun observeMemberLocations(tripId: String): Flow<Map<String, LocationPoint>>
    suspend fun removeMyLocation(tripId: String, userId: String)
}
