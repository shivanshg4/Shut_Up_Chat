package com.chat.shutup.data.repository

import android.content.Context
import android.location.Geocoder
import com.chat.shutup.domain.model.TripLocation
import com.chat.shutup.domain.repository.LocationSearchRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

class LocationSearchRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationSearchRepository {

    private val geocoder = Geocoder(context, Locale.getDefault())

    override suspend fun search(query: String): Result<List<TripLocation>> = withContext(Dispatchers.IO) {
        try {
            val addresses = geocoder.getFromLocationName(query, 5)
            val locations = addresses?.map { address ->
                TripLocation(
                    latitude = address.latitude,
                    longitude = address.longitude,
                    address = address.getAddressLine(0) ?: address.featureName ?: "Unknown"
                )
            } ?: emptyList()
            Result.success(locations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reverseGeocode(latitude: Double, longitude: Double): Result<String> = withContext(Dispatchers.IO) {
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            val address = addresses?.firstOrNull()?.getAddressLine(0) ?: "Unknown Location"
            Result.success(address)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
