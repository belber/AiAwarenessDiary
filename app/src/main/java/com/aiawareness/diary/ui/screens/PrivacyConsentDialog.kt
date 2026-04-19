package com.aiawareness.diary.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.aiawareness.diary.R
import com.aiawareness.diary.ui.theme.JournalTokens

@Composable
fun PrivacyConsentDialog(
    onAgree: () -> Unit,
    onDisagree: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        containerColor = JournalTokens.Paper,
        title = {
            Text(
                text = stringResource(R.string.privacy_policy_title),
                style = MaterialTheme.typography.titleLarge,
                color = JournalTokens.Ink
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.privacy_policy_consent_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = JournalTokens.MutedInk
                )
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = onOpenPrivacyPolicy,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(text = stringResource(R.string.privacy_policy_view_action))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onAgree) {
                Text(text = stringResource(R.string.privacy_policy_agree_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDisagree) {
                Text(text = stringResource(R.string.privacy_policy_disagree_action))
            }
        }
    )
}
