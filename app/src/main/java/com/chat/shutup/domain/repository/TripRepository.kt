package com.chat.shutup.domain.repository

import com.chat.shutup.domain.model.Trip
import kotlinx.coroutines.flow.Flow

interface TripRepository {
    suspend fun createTrip(trip: Trip)
    fun getTrip(tripId: String): Flow<Trip?>
    fun getTripsByCreator(creatorId: String): Flow<List<Trip>>
    suspend fun getTripByInviteCode(inviteCode: String): Trip?
    fun getAllTrips(): Flow<List<Trip>>
}
