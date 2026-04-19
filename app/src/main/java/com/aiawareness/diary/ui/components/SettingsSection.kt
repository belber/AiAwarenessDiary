package com.aiawareness.diary.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aiawareness.diary.ui.theme.JournalTokens

data class SettingsRowModel(
    val title: String,
    val subtitle: String,
    val onClick: (() -> Unit)? = null
)

@Composable
fun SettingsSection(
    title: String,
    items: List<SettingsRowModel>,
    modifier: Modifier = Modifier
) {
    EditorialSurfaceCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = JournalTokens.Ink
        )
        Column(modifier = Modifier.padding(top = 8.dp)) {
            items.forEachIndexed { index, item ->
                SettingsRow(
                    model = item,
                    modifier = if (index > 0) Modifier.padding(top = 4.dp) else Modifier
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(
    model: SettingsRowModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = model.onClick != null) { model.onClick?.invoke() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = model.title,
                style = MaterialTheme.typography.titleMedium,
                color = JournalTokens.Ink
            )
            Text(
                text = model.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = JournalTokens.MutedInk
            )
        }

        if (model.onClick != null) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = JournalTokens.MutedInk
            )
        }
    }
}
