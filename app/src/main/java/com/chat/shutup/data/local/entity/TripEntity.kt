package com.chat.shutup.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey val id: String,
    val name: String,
    val creatorId: String,
    val inviteCode: String,
    val createdAt: Long
)
