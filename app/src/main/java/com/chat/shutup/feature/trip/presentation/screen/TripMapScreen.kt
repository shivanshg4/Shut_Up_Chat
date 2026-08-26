package com.chat.shutup.feature.trip.presentation.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.chat.shutup.domain.model.TripMarkerType
import com.chat.shutup.feature.trip.presentation.util.TripMarkerAssetProvider
import com.chat.shutup.feature.trip.presentation.viewmodel.TripMapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch

object MapConstants {
    val DELHI = LatLng(28.6139, 77.2090)
    const val DEFAULT_ZOOM = 15f
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripMapScreen(
    onNavigateBack: () -> Unit,
    viewModel: TripMapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    
    var showRationale by remember { mutableStateOf(false) }
    var showMarkerPicker by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(MapConstants.DELHI, 12f)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onPermissionResult(isGranted)
    }

    LaunchedEffect(Unit) {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation || hasCoarseLocation) {
            viewModel.onPermissionResult(true)
        } else {
            showRationale = true
        }
    }

    // Effect to center map when the first location is received
    var hasCenteredOnUser by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.currentLocation) {
        uiState.currentLocation?.let { location ->
            if (!hasCenteredOnUser) {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(location.latitude, location.longitude),
                        MapConstants.DEFAULT_ZOOM
                    )
                )
                hasCenteredOnUser = true
            }
        }
    }

    // Effect to fit route when it's loaded
    var hasFitRoute by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.route) {
        uiState.route?.let { route ->
            if (!hasFitRoute && route.points.isNotEmpty()) {
                val bounds = LatLngBounds.Builder().apply {
                    route.points.forEach { include(LatLng(it.latitude, it.longitude)) }
                }.build()
                
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(bounds, 100)
                )
                hasFitRoute = true
            }
        }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("Location Permission") },
            text = { Text("TripTogether uses your location to show where you are on the trip map.") },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }) {
                    Text("Grant")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showMarkerPicker) {
        MarkerPickerStoreDialog(
            currentType = uiState.members.find { it.member.userId == viewModel.currentUserId }?.member?.markerType ?: TripMarkerType.DEFAULT,
            onTypeSelected = { 
                viewModel.onMarkerTypeSelected(it)
                showMarkerPicker = false
            },
            onDismiss = { showMarkerPicker = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TripTogether") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showMarkerPicker = true }) {
                        Icon(Icons.Default.EditLocationAlt, contentDescription = "Change Icon")
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.currentLocation != null) {
                FloatingActionButton(
                    onClick = {
                        uiState.currentLocation?.let { location ->
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(location.latitude, location.longitude),
                                        MapConstants.DEFAULT_ZOOM
                                    )
                                )
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Recenter")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = uiState.isPermissionGranted
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = false // We use our own Recenter FAB
                )
            ) {
                // Draw route polyline
                uiState.route?.let { route ->
                    Polyline(
                        points = route.points.map { LatLng(it.latitude, it.longitude) },
                        color = MaterialTheme.colorScheme.primary,
                        width = 12f
                    )
                }

                uiState.members.forEach { memberState ->
                    memberState.location?.let { location ->
                        val isCurrentUser = memberState.member.userId == viewModel.currentUserId
                        
                        Marker(
                            state = remember(location.latitude, location.longitude) {
                                MarkerState(position = LatLng(location.latitude, location.longitude))
                            },
                            title = if (isCurrentUser) "You" else memberState.member.name,
                            snippet = "${memberState.member.role} • Updated ${formatLastUpdated(location.timestamp)}",
                            icon = TripMarkerAssetProvider.getMarkerIcon(
                                type = memberState.member.markerType,
                                isCurrentUser = isCurrentUser,
                                isStale = memberState.isStale
                            ),
                            rotation = if (memberState.member.markerType in listOf(
                                    TripMarkerType.CAR, TripMarkerType.BUS, TripMarkerType.TRUCK,
                                    TripMarkerType.MOTORCYCLE, TripMarkerType.BICYCLE
                                )
                            ) {
                                location.bearing
                            } else 0f,
                            flat = memberState.member.markerType != TripMarkerType.DEFAULT && 
                                   memberState.member.markerType != TripMarkerType.ANIMAL
                        )
                    }
                }
            }
            
            // Route information card
            uiState.route?.let { route ->
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Route to Destination",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${String.format(Locale.getDefault(), "%.1f", route.distanceMeters / 1000.0)} km",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formatDuration(route.durationSeconds),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            uiState.error?.let { error ->
                Text(
                    text = error,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 80.dp),
                    color = androidx.compose.ui.graphics.Color.Red
                )
            }
        }
    }
}

fun formatLastUpdated(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
    return when {
        seconds < 10 -> "just now"
        seconds < 60 -> "$seconds seconds ago"
        seconds < 3600 -> "${TimeUnit.SECONDS.toMinutes(seconds)} minutes ago"
        else -> "${TimeUnit.SECONDS.toHours(seconds)} hours ago"
    }
}

fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
}

@Composable
fun MarkerPickerStoreDialog(
    currentType: TripMarkerType,
    onTypeSelected: (TripMarkerType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose your trip icon") },
        text = {
            LazyColumn {
                items(TripMarkerType.entries) { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = type == currentType,
                            onClick = { onTypeSelected(type) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = TripMarkerAssetProvider.getIcon(type),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = if (type == currentType) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = type.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
