package com.chat.shutup.ui.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.chat.shutup.feature.chat.domain.model.MessageStatus
import com.chat.shutup.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun MessageBubblePreview() {
    MessageBubble(
        "PEW PEW",
        timestamp = 12345,
        isOutgoing = true,
        MessageStatus.READ,
        reaction = "😊"
    )
}


@Composable
fun MessageBubble(
    text: String,
    timestamp: Long,
    isOutgoing: Boolean,
    status: MessageStatus,
    reaction: String? = null,
    onReactionSelected: (String) -> Unit = {}
) {
    var showReactionPicker by remember {
        mutableStateOf(false)
    }

    val bubbleColor = if (isOutgoing) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isOutgoing) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val alignment = if (isOutgoing) {
        Alignment.End
    } else {
        Alignment.Start
    }

    val shape = if (isOutgoing) {
        RoundedCornerShape(
            16.dp,
            16.dp,
            2.dp,
            16.dp
        )
    } else {
        RoundedCornerShape(
            16.dp,
            16.dp,
            16.dp,
            2.dp
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = AppTheme.spacing.extraSmall
            ),
        horizontalAlignment = alignment
    ) {

        Box {

            Column(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(shape)
                    .combinedClickable(
                        onClick = {
                            // Normal click
                        },
                        onLongClick = {
                            showReactionPicker = true
                        }
                    )
                    .background(bubbleColor)
                    .padding(AppTheme.spacing.small)
            ) {

                Text(
                    text = text,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge
                )

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = SimpleDateFormat(
                            "h:mm a",
                            Locale.getDefault()
                        ).format(Date(timestamp)),
                        color = textColor.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp
                        )
                    )

                    if (isOutgoing) {
                        Spacer(
                            modifier = Modifier.width(4.dp)
                        )

                        MessageStatusIcon(
                            status = status,
                            tint = textColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Reaction popup
            if (showReactionPicker) {

                ReactionPicker(
                    isOutgoing = isOutgoing,
                    onReactionSelected = { emoji ->

                        onReactionSelected(emoji)

                        showReactionPicker = false
                    },
                    onDismiss = {
                        showReactionPicker = false
                    }
                )
            }

            // Selected reaction
            if (reaction != null) {

                Text(
                    text = reaction,
                    modifier = Modifier
                        .align(
                            if (isOutgoing) {
                                Alignment.BottomStart
                            } else {
                                Alignment.BottomEnd
                            }
                        )
                        .offset(y = 8.dp)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(10.dp)
                        )
                        .padding(
                            horizontal = 5.dp,
                            vertical = 2.dp
                        )
                )
            }
        }
    }
}

/*@Composable
fun MessageBubble(
    text: String,
    timestamp: Long,
    isOutgoing: Boolean,
    status: MessageStatus,
    reaction: String? = null
) {
    val bubbleColor = if (isOutgoing) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isOutgoing) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val alignment = if (isOutgoing) Alignment.End else Alignment.Start
    val shape = if (isOutgoing) {
        RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppTheme.spacing.extraSmall),
        horizontalAlignment = alignment
    ) {
        Box {
            Column(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(shape)
                    .background(bubbleColor)
                    .padding(AppTheme.spacing.small)
            ) {
                Text(
                    text = text,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge
                )

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = SimpleDateFormat(
                            "h:mm a",
                            Locale.getDefault()
                        ).format(Date(timestamp)),
                        color = textColor.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
                    )

                    if (isOutgoing) {
                        Spacer(modifier = Modifier.width(4.dp))
                        MessageStatusIcon(status = status, tint = textColor.copy(alpha = 0.7f))
                    }
                }
            }

            if (reaction != null) {
                Text(
                    text = reaction,
                    modifier = Modifier
                        .align(if (isOutgoing) Alignment.BottomStart else Alignment.BottomEnd)
                        .offset(y = 8.dp) // Use offset instead of negative padding
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                        .padding(2.dp)
                )
            }
        }
    }
}*/

@Composable
private fun MessageStatusIcon(status: MessageStatus, tint: Color) {
    val icon = when (status) {
        MessageStatus.SENDING -> Icons.Default.Done
        MessageStatus.SENT -> Icons.Default.Done
        MessageStatus.DELIVERED -> Icons.Default.DoneAll
        MessageStatus.READ -> Icons.Default.DoneAll
    }

    val iconTint = if (status == MessageStatus.READ) MaterialTheme.colorScheme.secondary else tint

    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(12.dp),
        tint = iconTint
    )
}

@Preview(showBackground = true)
@Composable
private fun MessageStatusIconPreview() {
    MessageStatusIcon(MessageStatus.READ, MaterialTheme.colorScheme.onSurfaceVariant)
}
private val reactionEmojis = listOf(
    "❤️",
    "😂",
    "😮",
    "😢",
    "😡",
    "👍",
    "👎",
    "🔥",
    "👏",
    "🎉",
    "😍",
    "🙏",
    "🤣",
    "💯",
    "🥰"
)


@Preview(showBackground = true)
@Composable
private fun ReactionPickerPreview(){
    ReactionPicker(false, {}, {})
}

@Composable
private fun ReactionPicker(
    isOutgoing: Boolean,
    onReactionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    Popup(
        alignment = if (isOutgoing) {
            Alignment.TopEnd
        } else {
            Alignment.TopStart
        },
        offset = androidx.compose.ui.unit.IntOffset(
            x = if (isOutgoing) -20 else 20,
            y = -70
        ),
        onDismissRequest = onDismiss,
        properties = PopupProperties(
            focusable = true
        )
    ) {

        Row(
            modifier = Modifier
                .widthIn(
                    min = 180.dp,
                    max = 330.dp
                )
                .clip(
                    RoundedCornerShape(28.dp)
                )
                .background(
                    MaterialTheme.colorScheme.surface
                )
                .padding(
                    horizontal = 8.dp,
                    vertical = 6.dp
                )
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically
        ) {

            reactionEmojis.forEach { emoji ->

                Text(
                    text = emoji,
                    fontSize = 27.sp,
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .combinedClickable(
                            onClick = {
                                onReactionSelected(emoji)
                            },
                            onLongClick = {}
                        )
                )
            }
        }
    }
}
