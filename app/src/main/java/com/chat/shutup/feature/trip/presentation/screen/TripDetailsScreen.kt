package com.chat.shutup.feature.trip.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chat.shutup.feature.trip.presentation.viewmodel.TripDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsScreen(
    onBackClick: () -> Unit,
    onOpenMapClick: (String) -> Unit,
    onTripDeleted: () -> Unit,
    viewModel: TripDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Trip?") },
            text = { Text("Are you sure you want to delete \"${uiState.trip?.name}\"? This will remove the trip for all members.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.onDeleteTrip { onTripDeleted() }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trip Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.trip?.creatorId == viewModel.currentUserId) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.trip != null) {
                val trip = uiState.trip!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = trip.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Synced",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CAF50)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📍", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = trip.origin?.address ?: "Not set", style = MaterialTheme.typography.bodyLarge)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🏁", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = trip.destination?.address ?: "Not set", style = MaterialTheme.typography.bodyLarge)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val modeIcon = when(trip.travelMode) {
                                    com.chat.shutup.domain.model.TravelMode.DRIVING -> "🚗"
                                    com.chat.shutup.domain.model.TravelMode.WALKING -> "🚶"
                                    com.chat.shutup.domain.model.TravelMode.BICYCLING -> "🚲"
                                    com.chat.shutup.domain.model.TravelMode.TRANSIT -> "🚌"
                                }
                                Text(modeIcon, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = trip.travelMode.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Invite Code", style = MaterialTheme.typography.labelLarge)
                            Text(
                                text = trip.inviteCode,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        text = "Members (${trip.members.size})",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    trip.members.forEach { member ->
                        val roleText = if (member.role == com.chat.shutup.domain.model.TripRole.CREATOR) "Creator" else "Member"
                        val isMe = member.userId == viewModel.currentUserId
                        Text(
                            text = "${member.name}${if(isMe) " (You)" else ""} ($roleText)",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedButton(
                            onClick = {
                                val shareText = "🚗 Join my trip on ShutUp!\n\nTrip: ${trip.name}\nFrom: ${trip.origin?.address}\nTo: ${trip.destination?.address}\n\nInvite Code: ${trip.inviteCode}\n\nOpen ShutUp and use this code to join."
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, "Share Trip Invite"))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Share")
                        }
                        
                        Button(
                            onClick = { onOpenMapClick(trip.id) },
                            modifier = Modifier.weight(1.4f)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Map")
                        }
                    }
                }
            } else {
                Text(
                    text = uiState.error ?: "Trip not found",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
