package com.chat.shutup.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = SurfaceLight,
    primaryContainer = SageGreen,
    onPrimaryContainer = DarkForestGreen,
    secondary = DeepSkyBlue,
    onSecondary = SurfaceLight,
    secondaryContainer = SkyBlue,
    onSecondaryContainer = DeepSkyBlue,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = BackgroundLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    error = ErrorRed,
    onError = SurfaceLight
)

private val DarkColorScheme = darkColorScheme(
    primary = LightForestGreen,
    onPrimary = BackgroundDark,
    primaryContainer = ForestGreen.copy(alpha = 0.2f),
    onPrimaryContainer = LightForestGreen,
    secondary = SkyBlue,
    onSecondary = BackgroundDark,
    secondaryContainer = DeepSkyBlue.copy(alpha = 0.2f),
    onSecondaryContainer = SkyBlue,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    error = ErrorRed,
    onError = SurfaceDark
)

object AppTheme {
    val spacing: Spacing
        @Composable
        get() = LocalSpacing.current

    val dimensions: Dimensions
        @Composable
        get() = LocalDimensions.current
}

@Composable
fun ShutUpChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalDimensions provides Dimensions()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
