package com.chat.shutup.feature.trip.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chat.shutup.domain.model.TripLocation
import com.chat.shutup.feature.trip.presentation.viewmodel.LocationPickerViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState

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
    val uiState by viewModel.uiState.collectAsState()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(initialLat, initialLng), 15f)
    }

    // Effect to update address when map stops moving
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            val target = cameraPositionState.position.target
            viewModel.reverseGeocode(target.latitude, target.longitude)
        }
    }

    // Effect to move camera when a location is selected from search
    LaunchedEffect(uiState.selectedLocation) {
        uiState.selectedLocation?.let { location ->
            val target = LatLng(location.latitude, location.longitude)
            if (cameraPositionState.position.target != target) {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(target, 15f)
                )
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

            // Search Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
            ) {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = { viewModel.onQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)),
                    placeholder = { Text("Search for a location") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                if (uiState.isSearching) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }

                if (uiState.searchResults.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        LazyColumn {
                            items(uiState.searchResults) { location ->
                                ListItem(
                                    headlineContent = { Text(location.address) },
                                    modifier = Modifier.clickable {
                                        viewModel.onLocationSelected(location)
                                    },
                                    leadingContent = {
                                        Icon(Icons.Default.LocationOn, contentDescription = null)
                                    }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                } else if (uiState.query.length >= 3 && !uiState.isSearching && uiState.searchResults.isEmpty()) {
                     // No results feedback could go here
                }
            }

            // Bottom Confirmation Card
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Selected Location", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = uiState.selectedLocation?.address ?: "Select a location on map",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            uiState.selectedLocation?.let {
                                onLocationConfirmed(it)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.selectedLocation != null
                    ) {
                        Text("Confirm Location")
                    }
                }
            }
        }
    }
}
