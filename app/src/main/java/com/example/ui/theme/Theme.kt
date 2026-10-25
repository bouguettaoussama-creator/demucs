package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonViolet,
    onPrimary = Color.White,
    primaryContainer = NeonVioletDark,
    onPrimaryContainer = Color(0xFFEDE9FE),
    secondary = NeonCyan,
    onSecondary = Color(0xFF082F49),
    secondaryContainer = NeonCyanDark,
    onSecondaryContainer = Color(0xFFE0F2FE),
    tertiary = NeonRose,
    onTertiary = Color.White,
    background = StudioBackground,
    onBackground = TextPrimary,
    surface = StudioSurface,
    onSurface = TextPrimary,
    surfaceVariant = StudioCard,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
    error = NeonRose
)

private val LightColorScheme = DarkColorScheme // Studio look is optimized dark-first

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to studio dark theme
    dynamicColor: Boolean = false, // Keep consistent studio neon aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
