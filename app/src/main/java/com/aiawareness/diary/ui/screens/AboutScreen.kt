package com.aiawareness.diary.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aiawareness.diary.R
import com.aiawareness.diary.ui.components.EditorialSurfaceCard
import com.aiawareness.diary.ui.components.EditorialTopBar
import com.aiawareness.diary.ui.theme.JournalTokens

data class AboutScreenContent(
    val bannerCaption: String,
    val title: String,
    val philosophyParagraphs: List<String>,
    val ownershipHighlight: String,
    val privacyEyebrow: String,
    val privacyIntro: String,
    val privacyBullets: List<String>,
    val privacyFooter: String,
    val footerBlessing: String,
    val versionLabel: String
)

fun aboutScreenContent(): AboutScreenContent =
    AboutScreenContent(
        bannerCaption = "致每一位寻求宁静的旅者",
        title = "呼吸之间，自见本心",
        philosophyParagraphs = listOf(
            "我们身处一个喧嚣的时代，信息如潮水般涌入，却鲜有片刻留给内心的呼吸。AI Awareness Journal 的诞生，并非为了增加你的数字负担，而是希望在数字荒原中为你开辟一处“数字避风港”（The Digital Sanctuary）。",
            "我们推崇 Yutori —— 这是一种关于“余白”的智慧。在这里，留白不仅仅是视觉上的空隙，更是心灵得以喘息的空间。我们不追求繁杂的数据分析，只愿通过最柔和的笔触，协助你捕捉那一抹转瞬即逝的觉察。",
            "从呼吸到自我（From breath to self），我们相信记录的力量不在于长度，而在于那一刻的专注与诚实。"
        ),
        ownershipHighlight = "你的内容不应被锁在某个产品里。我们支持将日记按每天导出为可直接阅读的 Markdown 文件，并保留可恢复的备份结构，让你随时带走、归档、迁移。",
        privacyEyebrow = "隐私承诺 / DATA PRIVACY",
        privacyIntro = "你的思考应当是私密的，如同锁在抽屉里的日记本。我们深刻理解数据主权的重要性，因此在设计之初就确立了最严格的隐私边界：",
        privacyBullets = listOf(
            "所有的文字与情感数据默认仅存储于你的本地设备。我们不设中心化服务器，不收集任何形式的用户日记内容。",
            "只有在你主动配置第三方 AI 或对象存储服务时，相关内容才会由客户端直接发送到你指定的服务提供商。",
            "在你同意隐私政策后，应用会启用 Aliyun APM 用于崩溃、性能、网络、日志与内存诊断；我们也建议你审慎选择第三方服务并保管好自己的接口密钥。"
        ),
        privacyFooter = "在这里，你的文字只属于你自己，现在如此，未来亦然。",
        footerBlessing = "愿你在每一次记录中，都能遇见更轻盈的自己。",
        versionLabel = "Version 1.0.4 • The Breath of Silence Edition"
    )

@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit
) {
    val content = aboutScreenContent()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(JournalTokens.Paper),
        contentPadding = PaddingValues(horizontal = JournalTokens.ScreenPadding, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            EditorialTopBar(title = "关于", onBack = onNavigateBack)
        }

        item {
            Image(
                painter = painterResource(id = R.drawable.about_banner),
                contentDescription = "关于页横幅",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 168.dp)
                    .clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop
            )
            Text(
                text = content.bannerCaption,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                style = MaterialTheme.typography.bodySmall,
                color = JournalTokens.MutedInk
            )
        }

        item {
            Text(
                text = content.title,
                style = MaterialTheme.typography.headlineMedium,
                color = JournalTokens.Ink
            )
            content.philosophyParagraphs.forEach { paragraph ->
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = paragraph,
                    style = MaterialTheme.typography.bodyMedium,
                    color = JournalTokens.Ink
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            EditorialSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "可带走的日记",
                    style = MaterialTheme.typography.titleMedium,
                    color = JournalTokens.Ink
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = content.ownershipHighlight,
                    style = MaterialTheme.typography.bodyMedium,
                    color = JournalTokens.MutedInk
                )
            }
        }

        item {
            EditorialSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = content.privacyEyebrow,
                    style = MaterialTheme.typography.labelSmall,
                    color = JournalTokens.MutedInk,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = content.privacyIntro,
                    style = MaterialTheme.typography.bodyMedium,
                    color = JournalTokens.MutedInk
                )
                Spacer(modifier = Modifier.height(14.dp))
                content.privacyBullets.forEachIndexed { index, bullet ->
                    PrivacyBullet(
                        text = bullet,
                        icon = when (index) {
                            0 -> Icons.Rounded.Security
                            1 -> Icons.Rounded.CloudOff
                            else -> Icons.Rounded.Key
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                Text(
                    text = content.privacyFooter,
                    style = MaterialTheme.typography.bodyMedium,
                    color = JournalTokens.MutedInk
                )
            }
        }

        item {
            AboutPrivacyPolicyEntry(onClick = onNavigateToPrivacyPolicy)
        }

        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = JournalTokens.SageContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Spa,
                                contentDescription = null,
                                tint = JournalTokens.Sage
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = content.footerBlessing,
                        style = MaterialTheme.typography.titleMedium,
                        color = JournalTokens.MutedInk
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = content.versionLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = JournalTokens.MutedInk
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutPrivacyPolicyEntry(
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.5.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = JournalTokens.SageContainer.copy(alpha = 0.55f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Description,
                        contentDescription = null,
                        tint = JournalTokens.Sage
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.privacy_policy_about_entry_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = JournalTokens.Ink
                )
                Text(
                    text = stringResource(R.string.privacy_policy_about_entry_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = JournalTokens.MutedInk
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = JournalTokens.MutedInk
            )
        }
    }
}

@Composable
private fun PrivacyBullet(
    text: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = JournalTokens.Sage,
            modifier = Modifier.padding(top = 2.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = JournalTokens.Ink
        )
    }
}
