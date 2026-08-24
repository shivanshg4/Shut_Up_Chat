package com.chat.shutup.data.repository

import com.chat.shutup.data.local.TripDao
import com.chat.shutup.data.mapper.toTrip
import com.chat.shutup.data.mapper.toTripEntity
import com.chat.shutup.domain.model.Trip
import com.chat.shutup.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepositoryImpl @Inject constructor(
    private val tripDao: TripDao
) : TripRepository {

    override suspend fun createTrip(trip: Trip) {
        tripDao.insertTrip(trip.toTripEntity())
    }

    override fun getTrip(tripId: String): Flow<Trip?> {
        return tripDao.getTripById(tripId).map { it?.toTrip() }
    }

    override fun getTripsByCreator(creatorId: String): Flow<List<Trip>> {
        return tripDao.getTripsByCreator(creatorId).map { entities ->
            entities.map { it.toTrip() }
        }
    }

    override suspend fun getTripByInviteCode(inviteCode: String): Trip? {
        return tripDao.getTripByInviteCode(inviteCode)?.toTrip()
    }

    override fun getAllTrips(): Flow<List<Trip>> {
        return tripDao.getAllTrips().map { entities ->
            entities.map { it.toTrip() }
        }
    }
}
