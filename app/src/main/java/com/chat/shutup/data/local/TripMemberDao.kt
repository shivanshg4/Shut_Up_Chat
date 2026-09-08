package com.chat.shutup.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chat.shutup.data.local.entity.TripMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripMemberDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: TripMemberEntity)

    @Query("SELECT * FROM trip_members WHERE tripId = :tripId")
    fun getMembersForTrip(tripId: String): Flow<List<TripMemberEntity>>

    @Query("SELECT tripId FROM trip_members WHERE userId = :userId")
    fun getTripIdsForUser(userId: String): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM trip_members WHERE tripId = :tripId AND userId = :userId)")
    suspend fun isUserMemberOfTrip(tripId: String, userId: String): Boolean

    @Query("DELETE FROM trip_members WHERE tripId = :tripId AND userId = :userId")
    suspend fun deleteMember(tripId: String, userId: String)

    @Query("DELETE FROM trip_members WHERE tripId = :tripId")
    suspend fun deleteMembersByTripId(tripId: String)
}
