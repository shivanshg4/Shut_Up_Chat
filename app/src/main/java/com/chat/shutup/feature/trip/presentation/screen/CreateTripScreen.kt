package com.chat.shutup.feature.trip.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chat.shutup.feature.trip.presentation.viewmodel.CreateTripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripScreen(
    onBackClick: () -> Unit,
    onPickLocation: (Double, Double, String) -> Unit,
    onTripCreated: (String) -> Unit,
    viewModel: CreateTripViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Trip") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (uiState.createdTrip != null) {
                TripCreatedSuccess(
                    tripName = uiState.createdTrip!!.name,
                    inviteCode = uiState.createdTrip!!.inviteCode,
                    onContinue = { onTripCreated(uiState.createdTrip!!.id) }
                )
            } else {
                Text(
                    text = "Plan your adventure",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = uiState.tripName,
                    onValueChange = { viewModel.onTripNameChange(it) },
                    label = { Text("Trip Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.error != null
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                LocationSelectionSection(
                    label = "START",
                    location = uiState.origin,
                    onSelectClick = {
                        onPickLocation(
                            uiState.origin?.latitude ?: 28.6139,
                            uiState.origin?.longitude ?: 77.2090,
                            "Origin"
                        )
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                LocationSelectionSection(
                    label = "DESTINATION",
                    location = uiState.destination,
                    onSelectClick = {
                        onPickLocation(
                            uiState.destination?.latitude ?: uiState.origin?.latitude ?: 28.6139,
                            uiState.destination?.longitude ?: uiState.origin?.longitude ?: 77.2090,
                            "Destination"
                        )
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Travel Mode", style = MaterialTheme.typography.labelLarge, modifier = Modifier.fillMaxWidth())
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    com.chat.shutup.domain.model.TravelMode.entries.forEach { mode ->
                        FilterChip(
                            selected = uiState.travelMode == mode,
                            onClick = { viewModel.onTravelModeChange(mode) },
                            label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }

                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { viewModel.onCreateTrip() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Create Trip")
                    }
                }
            }
        }
    }
}

@Composable
fun LocationSelectionSection(
    label: String,
    location: com.chat.shutup.domain.model.TripLocation?,
    onSelectClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onSelectClick
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (location != null) Icons.Default.LocationOn else Icons.Default.AddLocation,
                    contentDescription = null,
                    tint = if (location != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = location?.address ?: "Not selected",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (location != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                TextButton(onClick = onSelectClick) {
                    Text(if (location == null) "Select" else "Change")
                }
            }
        }
    }
}

@Composable
fun TripCreatedSuccess(
    tripName: String,
    inviteCode: String,
    onContinue: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFF4CAF50)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Trip Created!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = tripName,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Invite Code", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = inviteCode,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue to Trip")
        }
    }
}
