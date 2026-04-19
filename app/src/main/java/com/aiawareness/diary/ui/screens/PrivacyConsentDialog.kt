package com.aiawareness.diary.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.aiawareness.diary.R
import com.aiawareness.diary.ui.components.EditorialSurfaceCard
import com.aiawareness.diary.ui.theme.JournalTokens

data class PrivacyConsentSectionCopy(
    val heading: String,
    val body: String
)

fun privacyConsentDialogTitle(
    stringResolver: (Int) -> String
): String = stringResolver(R.string.privacy_consent_title)

fun privacyConsentDialogIntro(
    stringResolver: (Int) -> String
): String = stringResolver(R.string.privacy_consent_intro)

fun privacyConsentDialogSections(
    stringResolver: (Int) -> String
): List<PrivacyConsentSectionCopy> = listOf(
    PrivacyConsentSectionCopy(
        heading = stringResolver(R.string.privacy_consent_section_local_heading),
        body = stringResolver(R.string.privacy_consent_section_local_body)
    ),
    PrivacyConsentSectionCopy(
        heading = stringResolver(R.string.privacy_consent_section_ai_heading),
        body = stringResolver(R.string.privacy_consent_section_ai_body)
    ),
    PrivacyConsentSectionCopy(
        heading = stringResolver(R.string.privacy_consent_section_runtime_heading),
        body = stringResolver(R.string.privacy_consent_section_runtime_body)
    )
)

fun privacyConsentDialogPolicyLinkLabel(
    stringResolver: (Int) -> String
): String = stringResolver(R.string.privacy_consent_policy_link)

fun privacyConsentDialogAgreeLabel(
    stringResolver: (Int) -> String
): String = stringResolver(R.string.privacy_consent_agree_action)

fun privacyConsentDialogDisagreeLabel(
    stringResolver: (Int) -> String
): String = stringResolver(R.string.privacy_consent_disagree_action)

@Composable
private fun PrivacyConsentSectionBlock(
    heading: String,
    body: String
) {
    EditorialSurfaceCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = heading,
                style = MaterialTheme.typography.titleSmall,
                color = JournalTokens.Ink
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = JournalTokens.MutedInk
            )
        }
    }
}

@Composable
fun PrivacyConsentDialog(
    onAgree: () -> Unit,
    onDisagree: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit
) {
    val resources = LocalContext.current.resources
    val stringResolver: (Int) -> String = { resId -> resources.getString(resId) }

    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        containerColor = JournalTokens.Paper,
        title = {
            Text(
                text = privacyConsentDialogTitle(stringResolver),
                style = MaterialTheme.typography.titleLarge,
                color = JournalTokens.Ink
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = privacyConsentDialogIntro(stringResolver),
                    style = MaterialTheme.typography.bodyMedium,
                    color = JournalTokens.MutedInk
                )
                privacyConsentDialogSections(stringResolver).forEach { section ->
                    PrivacyConsentSectionBlock(
                        heading = section.heading,
                        body = section.body
                    )
                }
                TextButton(
                    onClick = onOpenPrivacyPolicy,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = privacyConsentDialogPolicyLinkLabel(stringResolver),
                        style = MaterialTheme.typography.labelLarge,
                        color = JournalTokens.Stone
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onAgree) {
                Text(text = privacyConsentDialogAgreeLabel(stringResolver))
            }
        },
        dismissButton = {
            TextButton(onClick = onDisagree) {
                Text(text = privacyConsentDialogDisagreeLabel(stringResolver))
            }
        }
    )
}
