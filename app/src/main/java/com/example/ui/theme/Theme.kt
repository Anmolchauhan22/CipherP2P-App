package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MonochromeDarkColorScheme = darkColorScheme(
    primary = MonoWhite,
    onPrimary = MonoBlack,
    primaryContainer = MonoSurfaceElevated,
    onPrimaryContainer = MonoWhite,
    secondary = MonoOffWhite,
    onSecondary = MonoBlack,
    secondaryContainer = MonoSurfaceVariant,
    onSecondaryContainer = MonoWhite,
    tertiary = MonoTextSecondary,
    onTertiary = MonoBlack,
    background = MonoBackground,
    onBackground = MonoWhite,
    surface = MonoSurface,
    onSurface = MonoWhite,
    surfaceVariant = MonoSurfaceVariant,
    onSurfaceVariant = MonoTextSecondary,
    error = Color(0xFFF43F5E),
    onError = MonoWhite,
    outline = MonoBorderSubtle,
    outlineVariant = MonoBorderMedium
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Professional minimal black and white theme
    MaterialTheme(
        colorScheme = MonochromeDarkColorScheme,
        typography = Typography,
        content = content
    )
}
