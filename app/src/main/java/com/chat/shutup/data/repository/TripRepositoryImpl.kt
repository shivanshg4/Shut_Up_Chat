package com.chat.shutup.data.repository

import com.chat.shutup.data.local.TripDao
import com.chat.shutup.data.local.TripMemberDao
import com.chat.shutup.data.local.entity.TripMemberEntity
import com.chat.shutup.data.remote.source.FirebaseTripDataSource
import com.chat.shutup.data.mapper.toFirebaseDto
import com.chat.shutup.data.mapper.toTrip
import com.chat.shutup.data.mapper.toTripEntity
import com.chat.shutup.data.mapper.toTripMember
import com.chat.shutup.data.mapper.toTripMemberEntity
import com.chat.shutup.domain.model.Trip
import com.chat.shutup.domain.model.TripMarkerType
import com.chat.shutup.domain.model.TripMember
import com.chat.shutup.domain.model.TripRole
import com.chat.shutup.domain.repository.TripRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class TripRepositoryImpl @Inject constructor(
    private val tripDao: TripDao,
    private val tripMemberDao: TripMemberDao,
    private val firebaseDataSource: FirebaseTripDataSource
) : TripRepository {

    override suspend fun createTrip(trip: Trip) {
        // 1. Save locally (Always overwrite with latest Trip data)
        tripDao.insertTrip(trip.toTripEntity())
        trip.members.forEach { member ->
            tripMemberDao.insertMember(member.toTripMemberEntity(trip.id))
        }
        
        // 2. Upload to Firebase (Split operations to satisfy security rules)
        firebaseDataSource.createTrip(trip.toFirebaseDto())
    }

    override fun getTrip(tripId: String): Flow<Trip?> {
        return tripDao.getTripById(tripId).combine(
            tripMemberDao.getMembersForTrip(tripId)
        ) { entity, memberEntities ->
            entity?.toTrip(memberEntities.map { it.toTripMember() })
        }
    }

    override fun getTripsByCreator(creatorId: String): Flow<List<Trip>> {
        return tripDao.getTripsByCreator(creatorId).map { entities ->
            entities.map { it.toTrip() }
        }
    }

    override suspend fun getTripIdByInviteCode(inviteCode: String): String? {
        val localTrip = tripDao.getTripByInviteCode(inviteCode.uppercase())
        if (localTrip != null) return localTrip.id
        
        return firebaseDataSource.getTripIdByInviteCode(inviteCode)
    }

    override suspend fun getTripByInviteCode(inviteCode: String): Trip? {
        // 1. Check local first
        val localTrip = tripDao.getTripByInviteCode(inviteCode.uppercase())
        if (localTrip != null) return localTrip.toTrip()
        
        // 2. Check Firebase
        val tripId = getTripIdByInviteCode(inviteCode) ?: return null
        return try {
            val trip = firebaseDataSource.getTrip(tripId)?.toTrip()
            if (trip != null) {
                ensureTripLocal(trip)
            }
            trip
        } catch (e: Exception) {
            // Log or handle permission denied
            null
        }
    }

    override fun getAllTrips(): Flow<List<Trip>> {
        return tripDao.getAllTrips().map { entities ->
            entities.map { it.toTrip() }
        }
    }

    override fun getUserTrips(userId: String): Flow<List<Trip>> {
        return tripMemberDao.getTripIdsForUser(userId).flatMapLatest { tripIds ->
            if (tripIds.isEmpty()) return@flatMapLatest flowOf(emptyList<Trip>())
            
            val tripFlows = tripIds.map { tripId -> getTrip(tripId) }
            combine(tripFlows) { trips ->
                trips.filterNotNull()
            }
        }
    }

    override suspend fun joinTrip(
        tripId: String,
        userId: String,
        name: String,
        role: TripRole,
        markerType: TripMarkerType
    ) {
        // 1. Save locally
        val member = TripMember(userId, name, role, markerType = markerType)
        tripMemberDao.insertMember(member.toTripMemberEntity(tripId))
        
        // 2. Upload membership to Firebase
        try {
            firebaseDataSource.addMember(tripId, member.toFirebaseDto())
        } catch (e: Exception) {
            android.util.Log.e("TripTrackingDebug", "Failed to upload membership: ${e.message}")
        }

        // 3. Push Event
        pushTripEvent(
            tripId,
            com.chat.shutup.domain.model.TripNotificationData(
                type = com.chat.shutup.domain.model.TripNotificationType.MEMBER_JOINED,
                tripId = tripId,
                tripName = "", // To be filled by backend or using current context
                actorUserId = userId,
                actorName = name,
                title = "New Member",
                body = "$name joined the trip"
            )
        )
    }

    override suspend fun updateTripRoute(tripId: String, route: com.chat.shutup.domain.model.TripRoute) {
        // 1. Update locally
        val trip = tripDao.getTripById(tripId).firstOrNull()?.toTrip() ?: return
        val updatedTrip = trip.copy(route = route)
        tripDao.insertTrip(updatedTrip.toTripEntity())

        // 2. Update Firebase
        try {
            firebaseDataSource.updateTripRoute(tripId, route.toFirebaseDto())
        } catch (e: Exception) {
            android.util.Log.e("TripTrackingDebug", "Failed to update trip route: ${e.message}")
        }
    }
    
    override suspend fun ensureTripLocal(trip: Trip) {
        tripDao.insertTrip(trip.toTripEntity())
        trip.members.forEach { member ->
            tripMemberDao.insertMember(member.toTripMemberEntity(trip.id))
        }
    }

    override fun getTripMembers(tripId: String): Flow<List<TripMember>> {
        return firebaseDataSource.observeTripMembers(tripId)
            .map { dtos -> dtos.map { it.toTripMember() } }
            .onEach { members ->
                // Keep local cache updated
                members.forEach { member ->
                    tripMemberDao.insertMember(member.toTripMemberEntity(tripId))
                }
            }
    }

    override suspend fun isUserMemberOfTrip(tripId: String, userId: String): Boolean {
        return tripMemberDao.isUserMemberOfTrip(tripId, userId)
    }

    override suspend fun deleteTrip(tripId: String): Result<Unit> {
        return try {
            val trip = tripDao.getTripById(tripId).firstOrNull() ?: return Result.failure(Exception("Trip not found"))
            
            // 1. Delete from Firebase
            firebaseDataSource.deleteTrip(tripId, trip.inviteCode)
            
            // 2. Delete locally
            tripDao.deleteTrip(tripId)
            tripMemberDao.deleteMembersByTripId(tripId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun pushTripEvent(tripId: String, event: com.chat.shutup.domain.model.TripNotificationData) {
        try {
            firebaseDataSource.pushTripEvent(tripId, event)
        } catch (e: Exception) {
            android.util.Log.e("TripTrackingDebug", "Failed to push trip event: ${e.message}")
        }
    }
}
