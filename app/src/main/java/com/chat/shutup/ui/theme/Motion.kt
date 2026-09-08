package com.chat.shutup.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween

object ShutUpMotion {
    val StandardEasing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    val EmphasizedEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    
    val DurationShort = 150
    val DurationMedium = 300
    val DurationLong = 500

    fun <T> standardTween(duration: Int = DurationMedium) = tween<T>(
        durationMillis = duration,
        easing = StandardEasing
    )

    fun <T> emphasizedTween(duration: Int = DurationLong) = tween<T>(
        durationMillis = duration,
        easing = EmphasizedEasing
    )
}
