package com.chat.shutup.feature.trip.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.chat.shutup.feature.trip.presentation.viewmodel.CreateTripViewModel
import com.chat.shutup.ui.components.ShutUpPrimaryButton
import com.chat.shutup.ui.components.ShutUpTextField
import com.chat.shutup.ui.components.ShutUpTopBar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripScreen(
    onBackClick: () -> Unit,
    onPickLocation: (Double, Double, String) -> Unit,
    onTripCreated: (String) -> Unit,
    viewModel: CreateTripViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    showTimePicker = true
                }) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val calendar = Calendar.getInstance()
                    datePickerState.selectedDateMillis?.let { calendar.timeInMillis = it }
                    calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    calendar.set(Calendar.MINUTE, timePickerState.minute)
                    viewModel.onStartTimeChange(calendar.timeInMillis)
                    showTimePicker = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            title = { Text("Select Start Time") },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    Scaffold(
        topBar = {
            ShutUpTopBar(title = "Create Trip", onBackClick = onBackClick)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
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
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Plan your adventure",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                ShutUpTextField(
                    value = uiState.tripName,
                    onValueChange = { viewModel.onTripNameChange(it) },
                    label = "Trip Name"
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

                Text("Start Date & Time (Optional)", style = MaterialTheme.typography.labelLarge, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = if (uiState.startTime != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = uiState.startTime?.let {
                                SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date(it))
                            } ?: "Select date and time",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (uiState.startTime != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (uiState.startTime != null) {
                            IconButton(onClick = { viewModel.onStartTimeChange(null) }) {
                                Icon(Icons.Default.Schedule, contentDescription = "Clear", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

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
                
                ShutUpPrimaryButton(
                    text = "Create Trip",
                    onClick = { viewModel.onCreateTrip() },
                    enabled = !uiState.isLoading
                )
                
                if (uiState.isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }
                
                Spacer(modifier = Modifier.height(32.dp))
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
            onClick = onSelectClick,
            shape = RoundedCornerShape(16.dp)
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
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 32.dp)) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Invite Code", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = inviteCode,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
        ShutUpPrimaryButton(
            text = "Continue to Trip",
            onClick = onContinue
        )
    }
}
