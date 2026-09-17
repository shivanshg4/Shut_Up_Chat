package com.chat.shutup.feature.trip.presentation.screen.components

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.chat.shutup.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeEnvironment(
    modifier: Modifier = Modifier,
    isAnimated: Boolean = true,
    isInteractive: Boolean = true,
    shakeCount: Int = 0 // Used to trigger shake reaction
) {
    // 1. Shake reaction states
    val cloudSpeedMultiplier = remember { Animatable(1f) }
    val birdAgitation = remember { Animatable(0f) }

    // React to shakeCount changes
    LaunchedEffect(shakeCount) {
        if (shakeCount > 0 && isInteractive && isAnimated) {
            Log.d("NatureInteractionDebug", "SHAKE_EFFECT_STARTED")
            
            // Quickly accelerate clouds and agitate birds
            launch {
                cloudSpeedMultiplier.animateTo(
                    targetValue = 15f,
                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                )
                // Smoothly return to normal
                cloudSpeedMultiplier.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(3000, easing = LinearOutSlowInEasing)
                )
            }
            
            launch {
                birdAgitation.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(300, easing = FastOutLinearInEasing)
                )
                delay(1000)
                birdAgitation.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(2000, easing = LinearOutSlowInEasing)
                )
                Log.d("NatureInteractionDebug", "SHAKE_EFFECT_FINISHED")
            }
        }
    }

    Box(modifier = modifier.fillMaxWidth().height(300.dp)) {
        // 2. Base Nature Image
        Image(
            painter = painterResource(id = R.drawable.bg_img),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 3. Soft Gradient Overlay
        val bgColor = MaterialTheme.colorScheme.background
        val isDark = isSystemInDarkTheme()
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            if (isDark) Color.Black.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.15f),
                            Color.Transparent,
                            Color.Transparent,
                            if (isDark) bgColor.copy(alpha = 0.9f) else bgColor.copy(alpha = 0.7f),
                            bgColor
                        )
                    )
                )
        )

        // 4. Animated Elements
        if (isAnimated) {
            val birdColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            val infiniteTransition = rememberInfiniteTransition(label = "nature")
            
            // Base cloud offset (continuous)
            val baseCloudOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 2000f,
                animationSpec = infiniteRepeatable(
                    animation = tween(120000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "clouds"
            )
            
            // Bird flight path
            val birdFlightProgress by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(25000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "birds"
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                // Apply speed multiplier to clouds
                val currentCloudOffset = baseCloudOffset * cloudSpeedMultiplier.value
                
                // Draw Clouds
                val cloudColor = Color.White.copy(alpha = 0.2f)
                for (i in 0..2) {
                    val x = ((currentCloudOffset + i * 800) % (size.width + 500)) - 250
                    val y = 40f + i * 35f
                    drawCircle(cloudColor, radius = 40f, center = Offset(x, y))
                    drawCircle(cloudColor, radius = 60f, center = Offset(x + 50f, y + 15f))
                    drawCircle(cloudColor, radius = 45f, center = Offset(x + 100f, y))
                }

                // Draw Birds
                drawBirds(birdFlightProgress, birdAgitation.value, birdColor)
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBirds(
    progress: Float,
    agitation: Float,
    color: Color
) {
    // Draw bird position based on progress and agitation
    for (i in 0..2) {
        val birdOffset = i * 0.1f
        val currentProgress = (progress + birdOffset) % 1.0f
        
        // Horizontal path across screen
        var x = currentProgress * (size.width + 200f) - 100f
        var y = 60f + i * 25f
        
        // Agitation effect: birds move up/faster and wobble
        if (agitation > 0f) {
            y -= agitation * 40f
            x += agitation * 100f // Dart forward
            
            // Random wobble
            val wobble = (Math.sin(System.currentTimeMillis() * 0.02).toFloat()) * 10f * agitation
            y += wobble
        }

        // Draw simple "V" bird
        val birdPath = Path().apply {
            moveTo(x, y)
            lineTo(x - 10f, y - 5f)
            moveTo(x, y)
            lineTo(x + 10f, y - 5f)
        }
        drawPath(birdPath, color, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))
    }
}
