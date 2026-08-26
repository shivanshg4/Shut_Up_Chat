package com.chat.shutup.data.repository

import com.chat.shutup.data.mapper.toFirebaseDto
import com.chat.shutup.data.mapper.toLocationPoint
import com.chat.shutup.data.remote.source.FirebaseLocationDataSource
import com.chat.shutup.domain.model.LocationPoint
import com.chat.shutup.domain.repository.TripLocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TripLocationRepositoryImpl @Inject constructor(
    private val dataSource: FirebaseLocationDataSource
) : TripLocationRepository {

    override suspend fun updateMyLocation(tripId: String, userId: String, location: LocationPoint) {
        dataSource.updateMyLocation(tripId, userId, location.toFirebaseDto())
    }

    override fun observeMemberLocations(tripId: String): Flow<Map<String, LocationPoint>> {
        return dataSource.observeTripLocations(tripId).map { dtoMap ->
            dtoMap.mapValues { it.value.toLocationPoint() }
        }
    }

    override suspend fun removeMyLocation(tripId: String, userId: String) {
        dataSource.removeMyLocation(tripId, userId)
    }
}
