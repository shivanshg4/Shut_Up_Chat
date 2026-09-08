package com.chat.shutup.feature.settings.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.chat.shutup.domain.model.TripMarkerType
import com.chat.shutup.feature.settings.presentation.viewmodel.SettingsViewModel
import com.chat.shutup.feature.trip.presentation.util.TripMarkerAssetProvider
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onSignedOut: () -> Unit,
    bottomPadding: Dp = 0.dp,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            TopAppBar(
                title = { Text("Travel Profile", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = { viewModel.saveSettings() }, enabled = !uiState.isSaving) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Text("Save", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = bottomPadding + 32.dp)
        ) {
            // 1. Profile Header Card
            ProfileHeaderCard(
                nickname = uiState.user?.nickname ?: "Explorer",
                name = uiState.user?.name ?: "Traveler",
                imageUrl = uiState.user?.imageUrl
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Identity Section
            SettingsSection(title = "Trip Identity") {
                Column(modifier = Modifier.padding(16.dp)) {
                    IdentityTextField(
                        value = uiState.user?.nickname ?: "",
                        label = "Nickname",
                        placeholder = "e.g. RoadRunner",
                        onValueChange = { viewModel.onUpdateNickname(it) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    IdentityTextField(
                        value = uiState.user?.contactNumber ?: "",
                        label = "Contact Number",
                        placeholder = "+1 234 567 890",
                        onValueChange = { viewModel.onUpdateContactNumber(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Vehicle Preference Section
            SettingsSection(title = "Favorite Vehicle") {
                VehicleSelector(
                    selectedType = uiState.user?.favoriteVehicle ?: TripMarkerType.DEFAULT,
                    onTypeSelected = { viewModel.onUpdateFavoriteVehicle(it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

                // 4. Appearance Section
                SettingsSection(title = "Appearance") {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Animated Background", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text("Subtle movement in Trips home", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Switch(
                                checked = uiState.isBackgroundAnimationEnabled,
                                onCheckedChange = { viewModel.onToggleAnimation(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF2E7D32)
                                )
                            )
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color(0xFFF0F0F0))

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Interactive Nature", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text("Shake phone to interact with hero", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Switch(
                                checked = uiState.isInteractiveNatureEnabled,
                                onCheckedChange = { viewModel.onToggleInteractiveNature(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF2E7D32)
                                )
                            )
                        }
                    }
                }

            Spacer(modifier = Modifier.height(32.dp))

            // 5. Sign Out
            TextButton(
                onClick = { viewModel.signOut(onSignedOut) },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFC62828))
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out of Account", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileHeaderCard(
    nickname: String,
    name: String,
    imageUrl: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = Color(0xFFF5F5F5),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE8F5E9))
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(text = nickname, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Text(text = name, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp),
                    onClick = { /* Future edit flow */ }
                ) {
                    Text(
                        text = "Edit Profile",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = Color.DarkGray.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            content()
        }
    }
}

@Composable
fun IdentityTextField(
    value: String,
    label: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFF0F0F0),
                focusedBorderColor = Color(0xFF2E7D32)
            )
        )
    }
}

@Composable
fun VehicleSelector(
    selectedType: TripMarkerType,
    onTypeSelected: (TripMarkerType) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Preview Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color(0xFFF9F9F9), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = TripMarkerAssetProvider.getIcon(selectedType),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = Color(0xFF2E7D32)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Grid/Row Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val vehicles = listOf(TripMarkerType.CAR, TripMarkerType.MOTORCYCLE, TripMarkerType.BUS, TripMarkerType.SCOOTER)
            vehicles.forEach { type ->
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedType == type) Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
                    onClick = { onTypeSelected(type) },
                    border = if (selectedType == type) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32)) else null
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = TripMarkerAssetProvider.getIcon(type),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = if (selectedType == type) Color(0xFF2E7D32) else Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedType == type) Color(0xFF2E7D32) else Color.Gray,
                            fontWeight = if (selectedType == type) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
