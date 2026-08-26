package com.chat.shutup.feature.trip.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chat.shutup.feature.trip.presentation.viewmodel.JoinTripViewModel

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Join Trip") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Enter invite code",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ask your friend for the 6-character code of their trip.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = uiState.inviteCode,
                onValueChange = { viewModel.onInviteCodeChange(it.uppercase()) },
                label = { Text("Invite Code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.error != null,
                placeholder = { Text("e.g. ABC123") }
            )
            
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
                onClick = { viewModel.onJoinTrip() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Join Trip")
                }
            }
        }
    }
}
