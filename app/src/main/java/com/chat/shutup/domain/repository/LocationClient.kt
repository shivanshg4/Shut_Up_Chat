package com.chat.shutup.domain.repository

import com.chat.shutup.domain.model.LocationPoint
import kotlinx.coroutines.flow.Flow

interface LocationClient {
    fun getLocationUpdates(interval: Long): Flow<LocationPoint>
    
    class LocationException(override val message: String): Exception(message)
}
