package com.chat.shutup.ui.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chat.shutup.ui.theme.AppTheme

@Composable
fun MessageComposer(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAttachmentClick: () -> Unit,
    onCameraClick: () -> Unit,
    onEmojiClick: () -> Unit,
    onVoiceRecordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onAttachmentClick) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(com.chat.shutup.R.string.add_attachment)
            )
        }

        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp)),
            placeholder = { Text(text = stringResource(com.chat.shutup.R.string.chat_placeholder)) },
            trailingIcon = {
                Row {
                    IconButton(onClick = onEmojiClick) {
                        Icon(
                            imageVector = Icons.Default.Mood,
                            contentDescription = stringResource(com.chat.shutup.R.string.emoji)
                        )
                    }
                    IconButton(onClick = onCameraClick) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = stringResource(com.chat.shutup.R.string.camera)
                        )
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        IconButton(
            onClick = if (value.isNotBlank()) onSendClick else onVoiceRecordClick,
            modifier = Modifier
                .padding(start = AppTheme.spacing.extraSmall)
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        ) {
            Icon(
                imageVector = if (value.isNotBlank()) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                contentDescription = stringResource(
                    if (value.isNotBlank()) com.chat.shutup.R.string.send else com.chat.shutup.R.string.voice_message
                ),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
