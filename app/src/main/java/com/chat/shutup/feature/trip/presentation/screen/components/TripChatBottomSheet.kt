package com.chat.shutup.feature.trip.presentation.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chat.shutup.domain.repository.TrackingStatus
import com.chat.shutup.feature.chat.domain.model.Message
import com.chat.shutup.feature.trip.presentation.state.MemberLocationState
import com.chat.shutup.feature.trip.presentation.state.TripMapUiState
import com.chat.shutup.ui.components.chat.MessageBubble
import com.chat.shutup.ui.components.chat.MessageComposer
import java.util.*

@Composable
fun TripChatHeader(
    uiState: TripMapUiState,
    currentUserId: String?,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val members = uiState.members
    val route = uiState.route
    val myMemberState = members.find { it.member.userId == currentUserId }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() }
            .padding(16.dp)
    ) {
        // Drag Handle
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
                .align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Trip Chat",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (!isExpanded && uiState.unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        modifier = Modifier.size(20.dp),
                        shape = CircleShape,
                        color = Color.Red
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = uiState.unreadCount.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                contentDescription = if (isExpanded) "Collapse" else "Expand"
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Compact Progress & Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${members.size} members" + (route?.let { " • ${String.format(Locale.getDefault(), "%.1f", it.distanceMeters / 1000.0)} km total" } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                myMemberState?.progress?.let { p ->
                    Text(
                        text = "You: ${String.format(Locale.getDefault(), "%.1f", p.progressDistanceMeters / 1000.0)} km (${(p.progressPercentage * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            if (uiState.trackingStatus == TrackingStatus.TRACKING) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.height(24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(Color(0xFF4CAF50), CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Live", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
                    }
                }
            }
        }
    }
}

@Composable
fun TripChatContent(
    uiState: TripMapUiState,
    currentUserId: String?,
    onSendMessage: (String) -> Unit,
    onToggleTracking: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.chatMessages.size) {
        if (uiState.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.chatMessages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Members List (Horizontal)
        if (uiState.members.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(uiState.members) { memberState ->
                    MemberCompactCard(memberState = memberState, isMe = memberState.member.userId == currentUserId)
                }
            }
        }

        // Tracking Toggle Button (Moved here to save map space)
        TrackingToggleButton(
            trackingStatus = uiState.trackingStatus,
            isActiveForThisTrip = uiState.activeTrackingTripId == uiState.trip?.id,
            onToggle = onToggleTracking,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

        // Message List
        Box(modifier = Modifier.weight(1f)) {
            if (uiState.chatMessages.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Start the conversation",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Send a message to your trip group.",
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
                        val senderName = uiState.members.find { it.member.userId == message.senderId }?.member?.name ?: "Unknown"
                        
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (!isMe) {
                                Text(
                                    text = senderName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                                )
                            }
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
        }

        // Composer
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.navigationBarsPadding()
        ) {
            MessageComposer(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding(),
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
fun MemberCompactCard(memberState: MemberLocationState, isMe: Boolean) {
    val progress = memberState.progress
    Card(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (memberState.isStale) Color.Yellow else Color(0xFF4CAF50),
                            CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isMe) "You" else memberState.member.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            if (progress != null) {
                if (progress.isOffRoute) {
                    Text(text = "Off route", style = MaterialTheme.typography.labelSmall, color = Color.Red)
                } else if (!isMe) {
                    val statusText = when {
                        progress.isNear -> "Near you"
                        progress.isAhead -> "${String.format(Locale.getDefault(), "%.1f", progress.aheadBehindDistanceMeters / 1000.0)} km ahead"
                        progress.isBehind -> "${String.format(Locale.getDefault(), "%.1f", progress.aheadBehindDistanceMeters / 1000.0)} km behind"
                        else -> ""
                    }
                    Text(text = statusText, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun TrackingToggleButton(
    trackingStatus: TrackingStatus,
    isActiveForThisTrip: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTracking = trackingStatus == TrackingStatus.TRACKING && isActiveForThisTrip
    
    Button(
        onClick = onToggle,
        modifier = modifier.fillMaxWidth(),
        colors = if (isTracking) {
            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        } else {
            ButtonDefaults.buttonColors()
        },
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        if (isTracking) {
            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Stop Sharing Location")
        } else {
            Text("Start Sharing Location")
        }
    }
}
