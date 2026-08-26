package com.chat.shutup.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey val id: String,
    val name: String,
    val creatorId: String,
    val inviteCode: String,
    val createdAt: Long,
    val originLat: Double?,
    val originLng: Double?,
    val originAddress: String?,
    val destLat: Double?,
    val destLng: Double?,
    val destAddress: String?,
    @ColumnInfo(defaultValue = "DRIVING")
    val travelMode: String,
    val routeDistanceMeters: Int? = null,
    val routeDurationSeconds: Long? = null,
    val routePolyline: String? = null
)
