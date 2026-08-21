package com.chat.shutup.feature.call.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chat.shutup.domain.model.CallInfo
import com.chat.shutup.ui.components.Avatar

@Preview
@Composable
fun IncomingCallScreenPreview(){
    IncomingCallScreen(onAccept = {}, callInfo = CallInfo(callerName = "Shivansh"), onReject= {})
}

@Composable
fun IncomingCallScreen(
    callInfo: CallInfo,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(64.dp))
                Avatar(
                    imageUrl = callInfo.callerImageUrl,
                    name = callInfo.callerName,
                    size = 120.dp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = callInfo.callerName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Incoming ${callInfo.type.name.lowercase()} call...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 64.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = onReject,
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color.Red, CircleShape)
                ) {
                    Icon(
                        Icons.Default.CallEnd,
                        contentDescription = "Reject",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(
                    onClick = onAccept,
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color.Green, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Call,
                        contentDescription = "Accept",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
