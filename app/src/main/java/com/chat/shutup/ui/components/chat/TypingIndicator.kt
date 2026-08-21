package com.chat.shutup.ui.components.chat

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.chat.shutup.ui.theme.AppTheme

@Composable
fun TypingIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(vertical = AppTheme.spacing.extraSmall)
            .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(AppTheme.spacing.small)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Dot(delay = 0)
            Dot(delay = 300)
            Dot(delay = 600)
        }
    }
}

@Composable
private fun Dot(delay: Int) {
    val transition = rememberInfiniteTransition(label = "dotTransition")
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, delayMillis = delay, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    Box(
        modifier = Modifier
            .size(6.dp)
            .graphicsLayer { this.alpha = alpha }
            .background(MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
    )
}
