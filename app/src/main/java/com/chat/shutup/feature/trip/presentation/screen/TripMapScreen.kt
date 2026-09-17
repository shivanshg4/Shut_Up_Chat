package com.chat.shutup.feature.trip.presentation.screen

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.chat.shutup.R
import com.chat.shutup.domain.model.TripMarkerType
import com.chat.shutup.feature.trip.presentation.screen.components.ShareTripBottomSheet
import com.chat.shutup.feature.trip.presentation.screen.components.TripChatBottomSheet
import com.chat.shutup.feature.trip.presentation.util.TripMarkerAssetProvider
import com.chat.shutup.feature.trip.presentation.viewmodel.TripMapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit

val LatLngConverter =
    androidx.compose.animation.core.TwoWayConverter<LatLng, androidx.compose.animation.core.AnimationVector2D>(
        convertToVector = {
            androidx.compose.animation.core.AnimationVector2D(
                it.latitude.toFloat(),
                it.longitude.toFloat()
            )
        },
        convertFromVector = { LatLng(it.v1.toDouble(), it.v2.toDouble()) }
    )

object MapConstants {
    val DELHI = LatLng(28.6139, 77.2090)
    const val DEFAULT_ZOOM = 15f
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripMapScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (String) -> Unit,
    tripId: String
) {
    val viewModel: TripMapViewModel =
        hiltViewModel(creationCallback = { factory: TripMapViewModel.Factory ->
            factory.create(tripId = tripId)
        })
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded
        )
    )

    val isDarkTheme = isSystemInDarkTheme()
    val mapStyle = remember(isDarkTheme) {
        if (isDarkTheme) {
            MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark)
        } else {
            MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_light)
        }
    }

    // Colors for the route
    val routeBorderColor =
        if (isDarkTheme) Color(0xFF102A43).copy(alpha = 0.6f) else Color(0xFFB0BEC5).copy(alpha = 0.5f)
    val routeColor = if (isDarkTheme) Color(0xFF00B0FF) else Color(0xFF2196F3)
    val completedRouteColor = if (isDarkTheme) Color(0xFF00E676) else Color(0xFF4CAF50)

    var showRationale by remember { mutableStateOf(false) }
    var showMarkerPicker by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    var showMapTypeSelector by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(MapConstants.DELHI, 12f)
    }

    // Observe user movement to disable auto-camera
    LaunchedEffect(cameraPositionState.isMoving) {
        if (cameraPositionState.isMoving && cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE) {
            viewModel.onMapMoved(true)
        }
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

    // Effect to center map when the first location is received OR when re-center is clicked
    LaunchedEffect(uiState.currentLocation, uiState.isAutoCameraEnabled) {
        if (uiState.isAutoCameraEnabled) {
            uiState.currentLocation?.let { location ->
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(location.latitude, location.longitude),
                        MapConstants.DEFAULT_ZOOM
                    )
                )
            }
        }
    }

    // Effect to fit route when it's loaded
    var hasFitRoute by remember { mutableStateOf(false) }

    fun fitTrip() {
        uiState.route?.let { route ->
            if (route.points.isNotEmpty()) {
                val bounds = LatLngBounds.Builder().apply {
                    route.points.forEach { include(LatLng(it.latitude, it.longitude)) }
                    uiState.members.forEach { m ->
                        m.location?.let { include(LatLng(it.latitude, it.longitude)) }
                    }
                }.build()

                scope.launch {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngBounds(bounds, 200)
                    )
                    viewModel.onMapMoved(true)
                }
            }
        }
    }

    LaunchedEffect(uiState.route) {
        uiState.route?.let { route ->
            if (!hasFitRoute && route.points.isNotEmpty()) {
                fitTrip()
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

    if (showShareSheet && uiState.trip != null) {
        ShareTripBottomSheet(
            trip = uiState.trip!!,
            onDismiss = { showShareSheet = false }
        )
    }

    if (showMapTypeSelector) {
        MapTypeSelectorDialog(
            currentType = uiState.mapType,
            onTypeSelected = { type ->
                viewModel.onMapTypeSelected(type)
                showMapTypeSelector = false
            },
            onDismiss = { showMapTypeSelector = false }
        )
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            TripChatBottomSheet(
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
                },
                onSendMessage = { viewModel.onSendMessage(it) },
                onToggleTracking = { viewModel.toggleTracking() },
                onNavigateToDetails = {
                    uiState.trip?.id?.let { id -> onNavigateToDetails(id) }
                }
            )
        },
        sheetPeekHeight = 110.dp,
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
                    isMyLocationEnabled = uiState.isPermissionGranted,
                    mapStyleOptions = mapStyle,
                    mapType = when(uiState.mapType) {
                        2 -> MapType.SATELLITE
                        3 -> MapType.TERRAIN
                        else -> MapType.NORMAL
                    }
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = false,
                    zoomControlsEnabled = false,
                    mapToolbarEnabled = false,
                    compassEnabled = false
                )
            ) {
                // Draw route border (Outer Polyline)
                if (uiState.decodedRoutePoints.isNotEmpty()) {
                    Polyline(
                        points = uiState.decodedRoutePoints,
                        color = routeBorderColor,
                        width = 14f,
                        zIndex = 1f,
                        jointType = JointType.ROUND,
                        startCap = RoundCap(),
                        endCap = RoundCap()
                    )
                }

                // Draw completed route (Green)
                if (uiState.completedRoutePoints.isNotEmpty()) {
                    Polyline(
                        points = uiState.completedRoutePoints,
                        color = completedRouteColor,
                        width = 8f,
                        zIndex = 2f,
                        jointType = JointType.ROUND,
                        startCap = RoundCap(),
                        endCap = RoundCap()
                    )
                }

                // Draw remaining route (Blue)
                if (uiState.remainingRoutePoints.isNotEmpty()) {
                    Polyline(
                        points = uiState.remainingRoutePoints,
                        color = routeColor,
                        width = 8f,
                        zIndex = 2f,
                        jointType = JointType.ROUND,
                        startCap = RoundCap(),
                        endCap = RoundCap()
                    )
                }

                // Origin & Destination Markers
                uiState.trip?.origin?.let { origin ->
                    Marker(
                        state = MarkerState(position = LatLng(origin.latitude, origin.longitude)),
                        title = "Start: ${origin.address}",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }

                uiState.trip?.destination?.let { dest ->
                    Marker(
                        state = MarkerState(position = LatLng(dest.latitude, dest.longitude)),
                        title = "Destination: ${dest.address}",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                }

                uiState.members.forEach { memberState ->
                    memberState.location?.let { location ->
                        val isCurrentUser = memberState.member.userId == viewModel.currentUserId
                        MemberMarker(
                            memberState = memberState,
                            isCurrentUser = isCurrentUser,
                            spriteProvider = viewModel.spriteProvider,
                            onMarkerClick = { viewModel.onSelectMember(memberState) }
                        )
                    }
                }
            }

            // Compact Floating Header
            TripMapHeader(
                trip = uiState.trip,
                membersCount = uiState.members.size,
                onBackClick = onNavigateBack,
                onDetailsClick = { uiState.trip?.id?.let(onNavigateToDetails) },
                onShareClick = { showShareSheet = true },
                onMarkerPickerClick = { showMarkerPicker = true },
                isLive = uiState.trackingStatus == com.chat.shutup.domain.repository.TrackingStatus.TRACKING
            )

            // Right side compact controls
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallMapControl(
                    icon = Icons.Default.Layers,
                    contentDescription = "Layers",
                    onClick = { showMapTypeSelector = true }
                )
                SmallMapControl(
                    Icons.Default.CenterFocusStrong,
                    "Fit Trip",
                    onClick = { fitTrip() })

                if (!uiState.isAutoCameraEnabled && uiState.currentLocation != null) {
                    SmallMapControl(
                        icon = Icons.Default.MyLocation,
                        contentDescription = "Recenter",
                        onClick = { viewModel.recenterCamera() },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                }
            }

            // Member Detail Card (when marker is tapped)
            uiState.selectedMember?.let { member ->
                MemberDetailCard(
                    memberState = member,
                    onDismiss = { viewModel.onSelectMember(null) },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 100.dp)
                )
            }

            // Error Overlay
            if (uiState.routeRequestState == com.chat.shutup.feature.trip.presentation.state.RouteRequestState.ERROR) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Unable to calculate route",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        TextButton(onClick = { viewModel.retryRouteFetch() }) {
                            Text("Retry")
                        }
                    }
                }
            }

            // Loading state
            if (uiState.routeRequestState == com.chat.shutup.feature.trip.presentation.state.RouteRequestState.LOADING) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 100.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Calculating route...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TripMapHeader(
    trip: com.chat.shutup.domain.model.Trip?,
    membersCount: Int,
    onBackClick: () -> Unit,
    onDetailsClick: () -> Unit,
    onShareClick: () -> Unit,
    onMarkerPickerClick: () -> Unit,
    isLive: Boolean
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)) {
                Text(
                    text = trip?.let {
                        "${
                            it.origin?.address?.split(",")?.first() ?: "Start"
                        } → ${it.destination?.address?.split(",")?.first() ?: "End"}"
                    } ?: "Trip",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLive) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF4CAF50), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = "${trip?.name ?: "Trip"} • $membersCount members",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Trip details") },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                        onClick = { showMenu = false; onDetailsClick() }
                    )
                    DropdownMenuItem(
                        text = { Text("Share trip") },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                        onClick = { showMenu = false; onShareClick() }
                    )
                    DropdownMenuItem(
                        text = { Text("Change icon") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.EditLocationAlt,
                                contentDescription = null
                            )
                        },
                        onClick = { showMenu = false; onMarkerPickerClick() }
                    )
                }
            }
        }
    }
}

@Composable
fun SmallMapControl(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(42.dp),
        shape = CircleShape,
        color = containerColor.copy(alpha = 0.9f),
        shadowElevation = 2.dp,
        tonalElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(20.dp),
                tint = contentColor
            )
        }
    }
}

@Composable
fun MemberDetailCard(
    memberState: com.chat.shutup.feature.trip.presentation.state.MemberLocationState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.padding(horizontal = 32.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        memberState.member.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = memberState.member.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                memberState.progress?.let { p ->
                    val status =
                        if (p.isAhead) "${formatDistance(p.aheadBehindDistanceMeters)} ahead"
                        else if (p.isBehind) "${formatDistance(p.aheadBehindDistanceMeters)} behind"
                        else "Nearby"
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                memberState.location?.let { l ->
                    Text(
                        text = "Updated ${formatLastUpdated(l.timestamp)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
    }
}

@Composable
fun MemberMarker(
    memberState: com.chat.shutup.feature.trip.presentation.state.MemberLocationState,
    isCurrentUser: Boolean,
    spriteProvider: com.chat.shutup.feature.trip.presentation.util.VehicleSpriteProvider,
    onMarkerClick: () -> Unit
) {
    val location = memberState.location ?: return
    val targetLatLng = LatLng(location.latitude, location.longitude)
    val animatedLatLng =
        remember { androidx.compose.animation.core.Animatable(targetLatLng, LatLngConverter) }

    LaunchedEffect(targetLatLng) {
        if (animatedLatLng.value != targetLatLng) {
            animatedLatLng.animateTo(
                targetValue = targetLatLng,
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 3000,
                    easing = androidx.compose.animation.core.LinearEasing
                )
            )
        }
    }

    val markerState = rememberMarkerState(position = animatedLatLng.value)
    LaunchedEffect(animatedLatLng.value) { markerState.position = animatedLatLng.value }

    val spriteRow = when (memberState.member.markerType) {
        TripMarkerType.CAR -> 0
        TripMarkerType.BUS -> 1
        TripMarkerType.TRUCK -> 2
        TripMarkerType.MOTORCYCLE -> 3
        TripMarkerType.BICYCLE -> 4
        TripMarkerType.ANIMAL -> 5
        TripMarkerType.SCOOTER -> 6
        else -> 0
    }

    val vehicleBitmap = remember(location.bearing, memberState.member.markerType) {
        spriteProvider.getSpriteBitmapForBearing(spriteRow, location.bearing)
    }

    MarkerComposable(
        state = markerState,
        anchor = Offset(0.5f, 0.5f),
        zIndex = if (isCurrentUser) 10f else 8f,
        onClick = { onMarkerClick(); true }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (!isCurrentUser) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(4.dp),
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = memberState.member.name,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(contentAlignment = Alignment.BottomCenter) {
                if (vehicleBitmap != null) {
                    Image(
                        bitmap = vehicleBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(if (isCurrentUser) 32.dp else 28.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (isCurrentUser) MaterialTheme.colorScheme.primary else Color.Red
                    )
                }

                if (isCurrentUser) {
                    Box(
                        modifier = Modifier
                            .offset(y = 2.dp)
                            .size(8.dp)
                            .background(Color.White, CircleShape)
                            .padding(1.5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF2196F3), CircleShape)
                        )
                    }
                }
            }
        }
    }
}

fun formatDistance(meters: Double): String {
    return if (meters < 1000) {
        "${meters.toInt()} m"
    } else {
        String.format(Locale.getDefault(), "%.1f km", meters / 1000.0)
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
                            .height(56.dp)
                            .clickable { onTypeSelected(type) },
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

@Composable
fun MapTypeSelectorDialog(
    currentType: Int,
    onTypeSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Map Type") },
        text = {
            Column {
                MapTypeOption("Default", 1, currentType, onTypeSelected)
                MapTypeOption("Satellite", 2, currentType, onTypeSelected)
                MapTypeOption("Terrain", 3, currentType, onTypeSelected)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun MapTypeOption(label: String, type: Int, currentType: Int, onTypeSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onTypeSelected(type) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = type == currentType, onClick = { onTypeSelected(type) })
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label)
    }
}
