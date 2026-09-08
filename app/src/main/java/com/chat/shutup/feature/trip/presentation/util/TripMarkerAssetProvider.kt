package com.chat.shutup.feature.trip.presentation.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.RenderVectorGroup
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.chat.shutup.domain.model.TripMarkerType
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.core.content.ContextCompat
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import com.chat.shutup.R

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
            TripMarkerType.SCOOTER -> Icons.Default.ElectricScooter
        }
    }

    fun getMarkerIcon(
        context: Context,
        type: TripMarkerType,
        isCurrentUser: Boolean,
        isStale: Boolean
    ): BitmapDescriptor {
        val color = if (isCurrentUser) {
            Color(0xFF007BFF) // Azure
        } else if (isStale) {
            Color(0xFFFFC107) // Yellow
        } else {
            Color(0xFFDC3545) // Red
        }

        return when (type) {
            TripMarkerType.DEFAULT -> BitmapDescriptorFactory.defaultMarker(
                if (isCurrentUser) BitmapDescriptorFactory.HUE_AZURE 
                else if (isStale) BitmapDescriptorFactory.HUE_YELLOW 
                else BitmapDescriptorFactory.HUE_RED
            )
            else -> {
                val iconRes = when(type) {
                    TripMarkerType.CAR -> R.drawable.ic_car
                    TripMarkerType.BUS -> R.drawable.ic_bus
                    TripMarkerType.TRUCK -> R.drawable.ic_truck
                    TripMarkerType.MOTORCYCLE -> R.drawable.ic_motorcycle
                    TripMarkerType.BICYCLE -> R.drawable.ic_bicycle
                    TripMarkerType.ANIMAL -> R.drawable.ic_animal
                    TripMarkerType.SCOOTER -> R.drawable.ic_scooter
                    else -> null
                }
                
                if (iconRes != null) {
                    bitmapDescriptorFromVector(context, iconRes, color.toArgb())
                } else {
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)
                }
            }
        }
    }

    private fun bitmapDescriptorFromVector(
        context: Context,
        @DrawableRes vectorResId: Int,
        color: Int
    ): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable?.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        vectorDrawable?.setTint(color)
        val bitmap = Bitmap.createBitmap(
            vectorDrawable!!.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
