package com.chat.shutup.feature.trip.presentation.screen.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import com.chat.shutup.domain.model.LocationPoint
import com.chat.shutup.domain.model.TripMember
import com.chat.shutup.feature.trip.presentation.util.VehicleSpriteProvider
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState

val LatLngConverter = TwoWayConverter<LatLng, AnimationVector2D>(
    convertToVector = { AnimationVector2D(it.latitude.toFloat(), it.longitude.toFloat()) },
    convertFromVector = { LatLng(it.v1.toDouble(), it.v2.toDouble()) }
)

@Composable
fun AnimatedMemberMarker(
    member: TripMember,
    location: LocationPoint,
    isCurrentUser: Boolean,
    spriteProvider: VehicleSpriteProvider,
    onClick: () -> Unit = {}
) {
    val targetLatLng = LatLng(location.latitude, location.longitude)
    val animatedLatLng = remember { Animatable(targetLatLng, LatLngConverter) }

    // Smooth movement animation
    LaunchedEffect(targetLatLng) {
        if (animatedLatLng.value != targetLatLng) {
            android.util.Log.d("TripVehicleDebug", "Animating movement for ${member.name} to $targetLatLng")
            animatedLatLng.animateTo(
                targetValue = targetLatLng,
                animationSpec = tween(durationMillis = 3000, easing = LinearEasing)
            )
        }
    }

    // Sprite selection based on bearing
    val spriteRow = 0 // CAR prototype row
    
    val sprite = remember(location.bearing, member.markerType) {
        val s = spriteProvider.getSpriteForBearing(spriteRow, location.bearing)
        android.util.Log.d("TripVehicleDebug", "Updating sprite for ${member.name}, bearing: ${location.bearing}, result: ${if (s != null) "SUCCESS" else "NULL"}")
        s
    }

    if (sprite != null) {
        val markerState = rememberMarkerState(position = animatedLatLng.value)
        
        // Update marker state position directly to sync with animation
        LaunchedEffect(animatedLatLng.value) {
            markerState.position = animatedLatLng.value
        }

        Marker(
            state = markerState,
            title = if (isCurrentUser) "You" else member.name,
            icon = sprite,
            anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f), // Center anchor for vehicles
            onClick = {
                onClick()
                false
            }
        )
    } else {
        // Fallback to standard marker if sprite is missing
        val markerState = rememberMarkerState(position = animatedLatLng.value)
        LaunchedEffect(animatedLatLng.value) {
            markerState.position = animatedLatLng.value
        }
        Marker(
            state = markerState,
            title = (if (isCurrentUser) "You" else member.name) + " (No Sprite)",
            onClick = {
                onClick()
                false
            }
        )
    }
}
