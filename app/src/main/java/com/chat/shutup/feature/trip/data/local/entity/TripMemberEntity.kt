package com.chat.shutup.feature.trip.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

import com.chat.shutup.feature.trip.domain.model.TripRole

@Entity(
    tableName = "trip_members",
    primaryKeys = ["tripId", "userId"]
)
data class TripMemberEntity(
    val tripId: String,
    val userId: String,
    val name: String,
    val role: TripRole,
    val joinedAt: Long,
    @ColumnInfo(defaultValue = "DEFAULT")
    val markerType: String = "DEFAULT"
)
