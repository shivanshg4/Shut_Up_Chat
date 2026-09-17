package com.chat.shutup.feature.trip.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.chat.shutup.feature.trip.presentation.screen.components.QRScanner
import com.chat.shutup.feature.trip.presentation.viewmodel.JoinTripViewModel
import com.chat.shutup.ui.components.ShutUpPrimaryButton
import com.chat.shutup.ui.components.ShutUpSecondaryButton
import com.chat.shutup.ui.components.ShutUpTextField
import com.chat.shutup.ui.components.ShutUpTopBar

@Composable
fun JoinTripScreen(
    onBackClick: () -> Unit,
    onTripJoined: (String) -> Unit,
    viewModel: JoinTripViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.joinedTrip) {
        uiState.joinedTrip?.let { trip ->
            onTripJoined(trip.id)
        }
    }

    if (uiState.isScanning) {
        Scaffold(
            topBar = {
                ShutUpTopBar(title = "Scan QR Code", onBackClick = { viewModel.stopScanning() })
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                QRScanner(
                    onCodeScanned = { viewModel.onCodeScanned(it) },
                    onDismiss = { viewModel.stopScanning() }
                )
                
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp),
                    color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.8f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "Center the QR code in the frame",
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        return
    }

    if (uiState.showJoinConfirmation && uiState.scannedTrip != null) {
        val trip = uiState.scannedTrip!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissConfirmation() },
            title = { Text("Join Trip?") },
            text = {
                Column {
                    Text(
                        text = trip.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "${trip.origin?.address} → ${trip.destination?.address}")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "${trip.members.size} members",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        if (uiState.isAlreadyMember) {
                            onTripJoined(trip.id)
                        } else {
                            viewModel.confirmJoin() 
                        }
                    }
                ) {
                    Text(if (uiState.isAlreadyMember) "Open Trip" else "Join Trip")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissConfirmation() }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            ShutUpTopBar(title = "Join Trip", onBackClick = onBackClick)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Enter invite code",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ask your friend for the 6-character code of their trip.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            ShutUpTextField(
                value = uiState.inviteCode,
                onValueChange = { viewModel.onInviteCodeChange(it.uppercase()) },
                label = "Invite Code"
            )
            
            if (uiState.error != null && !uiState.showJoinConfirmation) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            ShutUpPrimaryButton(
                text = "Join Trip",
                onClick = { viewModel.onJoinTrip() },
                enabled = !uiState.isLoading
            )

            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "OR", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(24.dp))

            ShutUpSecondaryButton(
                text = "Scan QR Code",
                onClick = { viewModel.startScanning() },
                icon = Icons.Default.QrCodeScanner
            )
        }
    }
}
