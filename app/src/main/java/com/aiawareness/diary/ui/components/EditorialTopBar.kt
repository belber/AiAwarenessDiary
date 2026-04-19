package com.aiawareness.diary.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiawareness.diary.ui.theme.JournalTokens

private fun editorialBackArrowIcon(): ImageVector = Icons.Rounded.ArrowBack

@Composable
fun EditorialTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val startPadding = if (onBack != null) 6.dp else JournalTokens.ScreenPadding

    Row(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .padding(
                PaddingValues(
                    start = startPadding,
                    top = 10.dp,
                    end = JournalTokens.ScreenPadding,
                    bottom = 10.dp
                )
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                FilledTonalIconButton(
                    onClick = onBack,
                    modifier = Modifier.size(34.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = JournalTokens.SurfaceHigh,
                        contentColor = JournalTokens.Sage
                    )
                ) {
                    Icon(
                        imageVector = editorialBackArrowIcon(),
                        contentDescription = "返回"
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                color = JournalTokens.Ink
            )
        }
        Row(content = actions)
    }
}
