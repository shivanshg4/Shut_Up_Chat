package com.chat.shutup.domain.repository

import com.chat.shutup.domain.model.Trip
import com.chat.shutup.domain.model.TripMarkerType
import com.chat.shutup.domain.model.TripMember
import com.chat.shutup.domain.model.TripRole
import kotlinx.coroutines.flow.Flow

interface TripRepository {
    suspend fun createTrip(trip: Trip)
    fun getTrip(tripId: String): Flow<Trip?>
    fun getTripsByCreator(creatorId: String): Flow<List<Trip>>
    suspend fun getTripIdByInviteCode(inviteCode: String): String?
    suspend fun getTripByInviteCode(inviteCode: String): Trip?
    fun getAllTrips(): Flow<List<Trip>>
    fun getUserTrips(userId: String): Flow<List<Trip>>
    suspend fun joinTrip(
        tripId: String,
        userId: String,
        name: String,
        role: TripRole,
        markerType: TripMarkerType = TripMarkerType.DEFAULT
    )
    suspend fun leaveTrip(tripId: String, userId: String): Result<Unit>
    fun getTripMembers(tripId: String): Flow<List<TripMember>>
    suspend fun isUserMemberOfTrip(tripId: String, userId: String): Boolean
    suspend fun updateTripRoute(tripId: String, route: com.chat.shutup.domain.model.TripRoute)
    suspend fun ensureTripLocal(trip: Trip)
    suspend fun deleteTrip(tripId: String): Result<Unit>
    suspend fun pushTripEvent(tripId: String, event: com.chat.shutup.domain.model.TripNotificationData)
    suspend fun cancelAllAlarms()
}
