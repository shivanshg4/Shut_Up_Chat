package com.chat.shutup.feature.call.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.chat.shutup.domain.model.CallInfo
import com.chat.shutup.domain.model.CallType
@Preview
@Composable
fun ActiveCallScreenPreview(){
    ActiveCallScreen(onEndCall = {}, callInfo = CallInfo(callerName = "Shivansh"))
}
@Composable
fun ActiveCallScreen(
    callInfo: CallInfo,
    onEndCall: () -> Unit
) {
    var isMicOn by remember { mutableStateOf(true) }
    var isVideoOn by remember { mutableStateOf(callInfo.type == CallType.VIDEO) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (callInfo.type == CallType.VIDEO) {
            // Placeholder for Agora Video View
            AndroidView(
                factory = { context ->
                    android.view.SurfaceView(context).apply {
                        // In real implementation, pass this to Agora setupRemoteVideo
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Small local preview
            Box(
                modifier = Modifier
                    .size(120.dp, 160.dp)
                    .padding(16.dp)
                    .background(Color.DarkGray)
                    .align(Alignment.TopEnd)
            )
        } else {
            // Voice Call UI
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = callInfo.callerName,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
                Text(
                    text = "Ongoing Call...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.LightGray
                )
            }
        }

        // Call Controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { isMicOn = !isMicOn },
                modifier = Modifier.background(if (isMicOn) Color.White.copy(0.2f) else Color.Red, CircleShape)
            ) {
                Icon(
                    if (isMicOn) Icons.Default.Mic else Icons.Default.MicOff,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            IconButton(
                onClick = onEndCall,
                modifier = Modifier.size(72.dp).background(Color.Red, CircleShape)
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = "End Call", tint = Color.White, modifier = Modifier.size(32.dp))
            }

            if (callInfo.type == CallType.VIDEO) {
                IconButton(
                    onClick = { isVideoOn = !isVideoOn },
                    modifier = Modifier.background(if (isVideoOn) Color.White.copy(0.2f) else Color.Red, CircleShape)
                ) {
                    Icon(
                        if (isVideoOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }
}
