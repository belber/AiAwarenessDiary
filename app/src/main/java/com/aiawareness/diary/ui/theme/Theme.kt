package com.aiawareness.diary.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = JournalTokens.Sage,
    onPrimary = Color.White,
    primaryContainer = JournalTokens.SageContainer,
    onPrimaryContainer = JournalTokens.SageDim,
    secondary = JournalTokens.Stone,
    onSecondary = Color.White,
    secondaryContainer = JournalTokens.ClayContainer,
    background = JournalTokens.Paper,
    surface = JournalTokens.SurfaceLow,
    onBackground = JournalTokens.Ink,
    onSurface = JournalTokens.Ink,
    error = Error
)

private val DarkColorScheme = darkColorScheme(
    primary = JournalTokens.Sage,
    onPrimary = Color.White,
    primaryContainer = JournalTokens.Sage,
    onPrimaryContainer = Color.White,
    secondary = JournalTokens.Clay,
    onSecondary = Color.White,
    secondaryContainer = JournalTokens.Stone,
    onSecondaryContainer = Color.White,
    background = Color(0xFF101721),
    surface = Color(0xFF172130),
    onBackground = Color(0xFFEAF0FA),
    onSurface = Color(0xFFEAF0FA),
    error = Error
)

@Composable
fun AiAwarenessDiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
