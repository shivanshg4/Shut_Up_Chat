package com.chat.shutup.core.location

import com.chat.shutup.core.location.model.LocationPoint
import kotlinx.coroutines.flow.Flow

interface LocationClient {
    fun getLocationUpdates(interval: Long): Flow<LocationPoint>

    class LocationException(message: String): Exception(message)
}
