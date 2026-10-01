package com.chat.shutup.core.database.util

import androidx.room.TypeConverter
import com.chat.shutup.feature.trip.domain.model.TripRole

class DatabaseConverters {
    @TypeConverter
    fun fromTripRole(value: TripRole): String {
        return value.name
    }

    @TypeConverter
    fun toTripRole(value: String): TripRole {
        return try {
            TripRole.valueOf(value)
        } catch (e: Exception) {
            TripRole.MEMBER
        }
    }
}
