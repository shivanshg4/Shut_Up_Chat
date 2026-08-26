package com.chat.shutup.feature.trip.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chat.shutup.domain.model.Trip
import com.chat.shutup.feature.trip.presentation.viewmodel.TripsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsScreen(
    onBackClick: () -> Unit,
    onCreateTripClick: () -> Unit,
    onJoinTripClick: () -> Unit,
    onTripClick: (String) -> Unit,
    viewModel: TripsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    var tripToDelete by remember { mutableStateOf<Trip?>(null) }

    if (tripToDelete != null) {
        AlertDialog(
            onDismissRequest = { tripToDelete = null },
            title = { Text("Delete Trip?") },
            text = { Text("Are you sure you want to delete \"${tripToDelete?.name}\"? This will remove the trip for all members.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = tripToDelete?.id
                        tripToDelete = null
                        id?.let { viewModel.onDeleteTrip(it) }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { tripToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Trips") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(
                    onClick = onJoinTripClick,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.Default.GroupAdd, contentDescription = "Join Trip")
                }
                Spacer(modifier = Modifier.height(16.dp))
                FloatingActionButton(onClick = onCreateTripClick) {
                    Icon(Icons.Default.Add, contentDescription = "Create Trip")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.trips.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No trips yet", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onCreateTripClick, modifier = Modifier.fillMaxWidth()) {
                        Text("Create Your First Trip")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = onJoinTripClick, modifier = Modifier.fillMaxWidth()) {
                        Text("Join with Invite Code")
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.trips) { trip ->
                        TripItem(
                            trip = trip,
                            onOpen = { onTripClick(trip.id) },
                            onDelete = { tripToDelete = trip },
                            onShare = {
                                val shareText = "🚗 Join my trip on ShutUp!\n\nTrip: ${trip.name}\nInvite Code: ${trip.inviteCode}\n\nOpen ShutUp and use this code to join."
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, "Share Trip Invite"))
                            },
                            isCreator = trip.creatorId == viewModel.currentUserId
                        )
                    }
                }
            }

            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun TripItem(
    trip: Trip,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    isCreator: Boolean
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onOpen)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = trip.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Open") },
                            onClick = {
                                showMenu = false
                                onOpen()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share") },
                            onClick = {
                                showMenu = false
                                onShare()
                            }
                        )
                        if (isCreator) {
                            DropdownMenuItem(
                                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            if (trip.origin != null && trip.destination != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = trip.origin.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Red
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = trip.destination.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Code: ${trip.inviteCode}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "${trip.members.size} members",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
