package com.aiawareness.diary.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.aiawareness.diary.ui.theme.JournalTokens

@Composable
fun DecorativeBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        JournalTokens.Paper,
                        JournalTokens.SurfaceLow
                    )
                )
            )

            drawCircle(
                color = JournalTokens.SageContainer.copy(alpha = 0.36f),
                radius = size.minDimension * 0.34f,
                center = Offset(size.width * 0.14f, size.height * 0.16f)
            )

            drawCircle(
                color = JournalTokens.ClayContainer.copy(alpha = 0.45f),
                radius = size.minDimension * 0.28f,
                center = Offset(size.width * 0.9f, size.height * 0.2f)
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.42f),
                radius = size.minDimension * 0.42f,
                center = Offset(size.width * 0.55f, size.height * 0.92f)
            )
        }

        content()
    }
}
