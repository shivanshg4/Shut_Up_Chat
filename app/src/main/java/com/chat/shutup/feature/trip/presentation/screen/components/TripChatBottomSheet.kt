package com.chat.shutup.feature.trip.presentation.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chat.shutup.domain.repository.TrackingStatus
import com.chat.shutup.feature.trip.presentation.state.TripMapUiState
import com.chat.shutup.ui.components.chat.MessageBubble
import com.chat.shutup.ui.components.chat.MessageComposer
import java.util.*

@Composable
fun TripChatBottomSheet(
    uiState: TripMapUiState,
    currentUserId: String?,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSendMessage: (String) -> Unit,
    onToggleTracking: () -> Unit,
    onNavigateToDetails: () -> Unit
) {
    val myProgress = uiState.members.find { it.member.userId == currentUserId }?.progress
    var selectedSection by remember { mutableStateOf(BottomSheetSection.STATUS) }
    
    // Auto-switch section when expanded if needed
    LaunchedEffect(isExpanded) {
        if (!isExpanded) {
            selectedSection = BottomSheetSection.STATUS
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
    ) {
        // --- COLLAPSED CONTENT (Always visible at top) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant)
                    .align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Distance
                Column {
                    val distRemaining = myProgress?.distanceRemainingMeters ?: 0.0
                    Text(
                        text = if (distRemaining > 0) String.format(Locale.getDefault(), "%.1f km", distRemaining / 1000.0) else "-- km",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "remaining",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Right side: Active Members
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFF4CAF50), CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        val liveCount = uiState.members.count { !it.isStale }
                        Text(
                            text = if (liveCount == 1) "1 member live" else "$liveCount members live",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "Group status active",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section Tabs (Visible when expanded or as a quick-access row when collapsed)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusTab(
                    label = "Trip Chat",
                    icon = Icons.AutoMirrored.Filled.Chat,
                    isSelected = selectedSection == BottomSheetSection.CHAT && isExpanded,
                    unreadCount = uiState.unreadCount,
                    onClick = {
                        if (!isExpanded) onToggleExpand()
                        selectedSection = BottomSheetSection.CHAT
                    },
                    modifier = Modifier.weight(1f)
                )
                
                StatusTab(
                    label = "Members",
                    icon = Icons.Default.Group,
                    isSelected = selectedSection == BottomSheetSection.MEMBERS && isExpanded,
                    onClick = {
                        if (!isExpanded) onToggleExpand()
                        selectedSection = BottomSheetSection.MEMBERS
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // --- EXPANDED CONTENT ---
        if (isExpanded) {
            Column(modifier = Modifier.fillMaxHeight(0.6f)) {
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedSection) {
                        BottomSheetSection.STATUS, BottomSheetSection.CHAT -> {
                            ChatSection(
                                uiState = uiState,
                                currentUserId = currentUserId,
                                onSendMessage = onSendMessage
                            )
                        }
                        BottomSheetSection.MEMBERS -> {
                            MembersSection(
                                uiState = uiState,
                                currentUserId = currentUserId,
                                onNavigateToDetails = onNavigateToDetails,
                                onToggleTracking = onToggleTracking
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class BottomSheetSection {
    STATUS, CHAT, MEMBERS
}

@Composable
fun StatusTab(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    unreadCount: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                modifier = Modifier.size(18.dp), 
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (unreadCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(unreadCount.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatSection(
    uiState: TripMapUiState,
    currentUserId: String?,
    onSendMessage: (String) -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.chatMessages.size) {
        if (uiState.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.chatMessages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Message List
        Box(modifier = Modifier.weight(1f).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))) {
            if (uiState.chatMessages.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No messages yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Start the conversation with your group.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.chatMessages) { message ->
                        val isMe = message.senderId == currentUserId
                        MessageBubble(
                            text = message.text,
                            timestamp = message.timestamp,
                            isOutgoing = isMe,
                            status = message.status
                        )
                    }
                }
            }
        }

        // Composer
        Surface(
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            MessageComposer(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                value = messageText,
                onValueChange = { messageText = it },
                onSendClick = {
                    if (messageText.isNotBlank()) {
                        onSendMessage(messageText)
                        messageText = ""
                    }
                },
                onAttachmentClick = {},
                onCameraClick = {},
                onEmojiClick = {},
                onVoiceRecordClick = {}
            )
        }
    }
}

@Composable
fun MembersSection(
    uiState: TripMapUiState,
    currentUserId: String?,
    onNavigateToDetails: () -> Unit,
    onToggleTracking: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(uiState.members) { state ->
                val isMe = state.member.userId == currentUserId
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                        Text(state.member.name.take(1).uppercase(), style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = if (isMe) "You" else state.member.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        val statusText = if (isMe) "● Live" 
                                       else if (state.progress?.isAhead == true) "${String.format(Locale.getDefault(), "%.1f km", state.progress.aheadBehindDistanceMeters / 1000.0)} ahead"
                                       else if (state.progress?.isBehind == true) "${String.format(Locale.getDefault(), "%.1f km", state.progress.aheadBehindDistanceMeters / 1000.0)} behind"
                                       else "Nearby"
                        Text(text = statusText, style = MaterialTheme.typography.bodySmall, color = if (isMe) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (state.isStale) {
                        Text(text = "Offline", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        // Action Bar (Members section only)
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onNavigateToDetails,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("View Trip Details")
            }
            
            val isTracking = uiState.trackingStatus == TrackingStatus.TRACKING
            IconButton(
                onClick = onToggleTracking,
                modifier = Modifier.size(48.dp).background(
                    if (isTracking) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                    RoundedCornerShape(12.dp)
                )
            ) {
                Icon(
                    imageVector = if (isTracking) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = "Sharing",
                    tint = if (isTracking) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
