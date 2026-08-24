package com.chat.shutup.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chat.shutup.data.local.entity.TripEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity)

    @Query("SELECT * FROM trips WHERE id = :tripId")
    fun getTripById(tripId: String): Flow<TripEntity?>

    @Query("SELECT * FROM trips WHERE creatorId = :creatorId")
    fun getTripsByCreator(creatorId: String): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE inviteCode = :inviteCode")
    suspend fun getTripByInviteCode(inviteCode: String): TripEntity?

    @Query("SELECT * FROM trips ORDER BY createdAt DESC")
    fun getAllTrips(): Flow<List<TripEntity>>

    @Query("DELETE FROM trips WHERE id = :tripId")
    suspend fun deleteTrip(tripId: String)
}
