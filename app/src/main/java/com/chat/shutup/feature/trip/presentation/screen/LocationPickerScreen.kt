package com.chat.shutup.feature.trip.presentation.screen

import android.graphics.drawable.Icon
import android.location.Geocoder
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.chat.shutup.domain.model.TripLocation
import com.chat.shutup.feature.trip.presentation.viewmodel.LocationPickerViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel

/*@Preview(showSystemUi = true, showBackground = true)
@Composable
fun LocationPickerScreenPreview() {

    val DELHI = LatLng(28.6139, 77.2090)
    LocationPickerScreen(
        DELHI.latitude,
        DELHI.longitude,
        mode = "Car",
        onLocationConfirmed = {},
        onBackClick = {},
        viewModel = hiltViewModel<LocationPickerViewModel>()
    )
}*/


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerScreen(
    initialLat: Double,
    initialLng: Double,
    mode: String,
    onLocationConfirmed: (TripLocation) -> Unit,
    onBackClick: () -> Unit,
    viewModel: LocationPickerViewModel
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(initialLat, initialLng), 15f)
    }

    var address by remember { mutableStateOf("Searching...") }
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            val target = cameraPositionState.position.target
            withContext(Dispatchers.IO) {
                try {
                    val addresses = geocoder.getFromLocation(target.latitude, target.longitude, 1)
                    val result = addresses?.firstOrNull()?.getAddressLine(0) ?: "Unknown Location"
                    withContext(Dispatchers.Main) {
                        address = result
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        address = "Selected Location"
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select $mode") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            )

            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(6.dp)
            ) {
                StatefulSearchView(viewModel = viewModel)
            }

            // Center Pin
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center)
                    .offset(y = (-24).dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Selected Location", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val target = cameraPositionState.position.target
                            onLocationConfirmed(
                                TripLocation(
                                    latitude = target.latitude,
                                    longitude = target.longitude,
                                    address = address
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Confirm Location")
                    }
                }
            }
        }
    }
}

// 1. STATEFUL WRAPPER: Use this in your actual app screens
@Composable
fun StatefulSearchView(
    viewModel: LocationPickerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    SearchView(state = uiState)
}

// 2. STATELESS VIEW: Pure UI, completely safe for previews
@Composable
fun SearchView(
    state: TextFieldState,
    modifier: Modifier = Modifier
) {
    TextField(
        state = state,
        modifier = modifier
            .fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        trailingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
        label = { Text("Search location") },
        lineLimits = TextFieldLineLimits.SingleLine
    )
}

// 3. PREVIEW: Mock the state instantly without Hilt
@Preview(showBackground = true)
@Composable
fun SearchViewPreview() {
    // Simply pass a mock TextFieldState directly
    SearchView(state = TextFieldState())
}