package com.chat.shutup.feature.trip.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chat.shutup.feature.trip.presentation.screen.components.ShareTripBottomSheet
import com.chat.shutup.feature.trip.presentation.viewmodel.TripDetailsViewModel
import com.chat.shutup.ui.components.ShutUpPrimaryButton
import com.chat.shutup.ui.components.ShutUpSecondaryButton
import com.chat.shutup.ui.components.ShutUpTopBar

@Composable
fun TripDetailsScreen(
    onBackClick: () -> Unit,
    onOpenMapClick: (String) -> Unit,
    onTripDeleted: () -> Unit,
    viewModel: TripDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showLeaveConfirm by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }

    if (showShareSheet && uiState.trip != null) {
        ShareTripBottomSheet(
            trip = uiState.trip!!,
            onDismiss = { showShareSheet = false }
        )
    }

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

    if (showLeaveConfirm) {
        AlertDialog(
            onDismissRequest = { showLeaveConfirm = false },
            title = { Text("Leave Trip?") },
            text = { 
                Text("Are you sure you want to leave \"${uiState.trip?.name}\"?\n\nYou will no longer receive trip updates or see live member locations.") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeaveConfirm = false
                        viewModel.onLeaveTrip { onTripDeleted() }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Leave Trip")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            ShutUpTopBar(
                title = "Trip Details",
                onBackClick = onBackClick,
                actions = {
                    if (uiState.trip?.creatorId == viewModel.currentUserId) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    } else if (uiState.trip != null) {
                        IconButton(onClick = { showLeaveConfirm = true }) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Leave")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
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
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Synced",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val trackingColor = if (uiState.isTrackingActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(trackingColor, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isTrackingActive) "Location sharing: Active" else "Location sharing: Inactive",
                            style = MaterialTheme.typography.labelSmall,
                            color = trackingColor
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📍", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = trip.origin?.address ?: "Not set", style = MaterialTheme.typography.bodyLarge)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🏁", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = trip.destination?.address ?: "Not set", style = MaterialTheme.typography.bodyLarge)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val modeIcon = when(trip.travelMode) {
                                    com.chat.shutup.domain.model.TravelMode.DRIVING -> "🚗"
                                    com.chat.shutup.domain.model.TravelMode.WALKING -> "🚶"
                                    com.chat.shutup.domain.model.TravelMode.BICYCLING -> "🚲"
                                    com.chat.shutup.domain.model.TravelMode.TRANSIT -> "🚌"
                                }
                                Text(modeIcon, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = trip.travelMode.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Invite Code", style = MaterialTheme.typography.labelLarge)
                            Text(
                                text = trip.inviteCode,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
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
                    
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        trip.members.forEach { member ->
                            val roleText = if (member.role == com.chat.shutup.domain.model.TripRole.CREATOR) "Creator" else "Member"
                            val isMe = member.userId == viewModel.currentUserId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                                    Text(member.name.take(1).uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if(isMe) "${member.name} (You)" else member.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(text = roleText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ShutUpPrimaryButton(
                            text = if (uiState.isTrackingActive) "Stop Sharing" else "Start Trip",
                            onClick = { viewModel.toggleTracking() },
                            modifier = Modifier.weight(1f),
                            containerColor = if (uiState.isTrackingActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )

                        ShutUpPrimaryButton(
                            text = "Open Map",
                            onClick = { onOpenMapClick(trip.id) },
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Map,
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    ShutUpSecondaryButton(
                        text = "Share Trip",
                        onClick = { showShareSheet = true },
                        icon = Icons.Default.Share
                    )
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
