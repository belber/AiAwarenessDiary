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
    onPrimary = Color(0xFFECFDED),
    primaryContainer = JournalTokens.SageContainer,
    onPrimaryContainer = Color(0xFF465549),
    secondary = JournalTokens.Clay,
    onSecondary = Color(0xFFFFF7F6),
    secondaryContainer = JournalTokens.ClayContainer,
    background = JournalTokens.Paper,
    surface = Color.White,
    onBackground = JournalTokens.Ink,
    onSurface = JournalTokens.Ink,
    error = Error
)

private val DarkColorScheme = darkColorScheme(
    primary = JournalTokens.SageContainer,
    onPrimary = JournalTokens.SageDim,
    primaryContainer = JournalTokens.Sage,
    onPrimaryContainer = Color(0xFFECFDED),
    secondary = JournalTokens.ClayContainer,
    onSecondary = JournalTokens.Clay,
    secondaryContainer = JournalTokens.Clay,
    onSecondaryContainer = Color(0xFFFFF7F6),
    background = JournalTokens.Ink,
    surface = JournalTokens.SageDim,
    onBackground = JournalTokens.Paper,
    onSurface = JournalTokens.Paper,
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
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
