package com.chat.shutup.feature.trip.presentation.screen

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.chat.shutup.domain.model.TripMarkerType
import com.chat.shutup.feature.trip.presentation.screen.components.AnimatedMemberMarker
import com.chat.shutup.feature.trip.presentation.screen.components.ShareTripBottomSheet
import com.chat.shutup.feature.trip.presentation.screen.components.TripChatContent
import com.chat.shutup.feature.trip.presentation.screen.components.TripChatHeader
import com.chat.shutup.feature.trip.presentation.util.TripMarkerAssetProvider
import com.chat.shutup.feature.trip.presentation.viewmodel.TripMapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

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
    
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded
        )
    )

    // Trigger mark as read when expanded
    LaunchedEffect(scaffoldState.bottomSheetState.currentValue) {
        if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
            viewModel.onChatExpand()
        }
    }

    var showRationale by remember { mutableStateOf(false) }
    var showMarkerPicker by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }

    if (showShareSheet && uiState.trip != null) {
        ShareTripBottomSheet(
            trip = uiState.trip!!,
            onDismiss = { showShareSheet = false }
        )
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(MapConstants.DELHI, 12f)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onPermissionResult(isLocationGranted)
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        permissionLauncher.launch(permissions.toTypedArray())
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
            currentType = uiState.members.find { it.member.userId == viewModel.currentUserId }?.member?.markerType
                ?: TripMarkerType.DEFAULT,
            onTypeSelected = {
                viewModel.onMarkerTypeSelected(it)
                showMarkerPicker = false
            },
            onDismiss = { showMarkerPicker = false }
        )
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            Column(modifier = Modifier.fillMaxHeight(0.85f)) {
                TripChatHeader(
                    uiState = uiState,
                    currentUserId = viewModel.currentUserId,
                    isExpanded = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded,
                    onToggleExpand = {
                        scope.launch {
                            if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                                scaffoldState.bottomSheetState.partialExpand()
                            } else {
                                scaffoldState.bottomSheetState.expand()
                            }
                        }
                    }
                )
                
                TripChatContent(
                    uiState = uiState,
                    currentUserId = viewModel.currentUserId,
                    onSendMessage = { viewModel.onSendMessage(it) },
                    onToggleTracking = { viewModel.toggleTracking() }
                )
            }
        },
        sheetPeekHeight = 120.dp,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetContentColor = MaterialTheme.colorScheme.onSurface,
        sheetTonalElevation = 8.dp,
        sheetDragHandle = null
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
                Log.d("TripVehicleDebug", "GoogleMap content drawing. Members count: ${uiState.members.size}")
                // Draw route polyline
                uiState.route?.let { route ->
                    Polyline(
                        points = route.points.map { LatLng(it.latitude, it.longitude) },
                        color = Color.Black,
                        width = 12f
                    )
                }

                uiState.members.forEach { memberState ->
                    memberState.location?.let { location ->
                        val isCurrentUser = memberState.member.userId == viewModel.currentUserId

                        Log.d("TripVehicleDebug", "Rendering marker for member: ${memberState.member.name}, at: ${location.latitude}, ${location.longitude}")
                        AnimatedMemberMarker(
                            member = memberState.member,
                            location = location,
                            isCurrentUser = isCurrentUser,
                            spriteProvider = viewModel.spriteProvider,
                            onClick = {
                                // existing click handling if any
                            }
                        )
                    }
                }
            }

            // Floating Action Button for Recenter (Manual Overlay)
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
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 32.dp, start = 16.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Recenter")
                }
            }

            // Transparent Header Overlay (Back, Trip Name, Share, Icon)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    onClick = onNavigateBack,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.trip?.name ?: "Trip",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        onClick = { showShareSheet = true },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        tonalElevation = 2.dp
                    ) {
                        Box(modifier = Modifier.padding(10.dp)) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Trip",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Surface(
                        onClick = { showMarkerPicker = true },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        tonalElevation = 2.dp
                    ) {
                        Box(modifier = Modifier.padding(10.dp)) {
                            Icon(
                                imageVector = Icons.Default.EditLocationAlt,
                                contentDescription = "Change Icon",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            uiState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp, start = 16.dp, end = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
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
