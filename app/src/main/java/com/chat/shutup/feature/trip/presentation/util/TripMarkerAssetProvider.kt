package com.chat.shutup.feature.trip.presentation.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.ui.graphics.vector.ImageVector
import com.chat.shutup.domain.model.TripMarkerType
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

object TripMarkerAssetProvider {

    fun getIcon(type: TripMarkerType): ImageVector {
        return when (type) {
            TripMarkerType.DEFAULT -> Icons.Default.LocationOn
            TripMarkerType.CAR -> Icons.Default.DirectionsCar
            TripMarkerType.BUS -> Icons.Default.DirectionsBus
            TripMarkerType.TRUCK -> Icons.Default.LocalShipping
            TripMarkerType.MOTORCYCLE -> Icons.Default.TwoWheeler
            TripMarkerType.BICYCLE -> Icons.Default.DirectionsBike
            TripMarkerType.ANIMAL -> Icons.Default.Agriculture
        }
    }

    fun getMarkerIcon(type: TripMarkerType, isCurrentUser: Boolean, isStale: Boolean): BitmapDescriptor {
        // For now using colors while assets are missing, but following architectural requirement
        return when (type) {
            TripMarkerType.DEFAULT -> BitmapDescriptorFactory.defaultMarker(
                if (isCurrentUser) BitmapDescriptorFactory.HUE_AZURE 
                else if (isStale) BitmapDescriptorFactory.HUE_YELLOW 
                else BitmapDescriptorFactory.HUE_RED
            )
            // Ideally these would be BitmapDescriptorFactory.fromResource(R.drawable.trip_marker_car)
            else -> BitmapDescriptorFactory.defaultMarker(
                if (isCurrentUser) BitmapDescriptorFactory.HUE_AZURE 
                else if (isStale) BitmapDescriptorFactory.HUE_YELLOW 
                else BitmapDescriptorFactory.HUE_MAGENTA
            )
        }
    }
}
