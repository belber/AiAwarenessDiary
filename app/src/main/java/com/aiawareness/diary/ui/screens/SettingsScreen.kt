package com.aiawareness.diary.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.aiawareness.diary.data.model.defaultDisplayNickname
import com.aiawareness.diary.ui.components.EditorialSurfaceCard
import com.aiawareness.diary.ui.components.EditorialTopBar
import com.aiawareness.diary.ui.theme.HeadlineFontFamily
import com.aiawareness.diary.ui.theme.JournalTokens

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAiConfig: () -> Unit,
    onNavigateToDataManagement: () -> Unit,
    onNavigateToAbout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var nicknameDraft by remember { mutableStateOf("") }
    var isEditingNickname by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.settings.nickname) {
        if (!isEditingNickname) {
            nicknameDraft = uiState.settings.nickname.ifBlank { defaultDisplayNickname() }
        }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        viewModel.savePersonalInfo(
            nickname = nicknameDraft.ifBlank { uiState.settings.nickname.ifBlank { defaultDisplayNickname() } },
            avatarUri = uri
        )
    }

    val menuEntries = settingsMenuEntries()

    Scaffold(
        containerColor = JournalTokens.Paper,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { _ ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(JournalTokens.Paper),
            contentPadding = PaddingValues(horizontal = JournalTokens.ScreenPadding, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                EditorialTopBar(title = "设置", onBack = onNavigateBack)
            }

            item {
                ProfileHero(
                    nickname = uiState.settings.nickname.ifBlank { defaultDisplayNickname() },
                    avatarPath = uiState.settings.avatarPath,
                    avatarRefreshVersion = uiState.avatarRefreshVersion,
                    nicknameDraft = nicknameDraft,
                    isEditingNickname = isEditingNickname,
                    isSaving = uiState.isSaving,
                    onEditAvatar = { imagePicker.launch(arrayOf("image/*")) },
                    onNicknameDraftChange = { nicknameDraft = it },
                    onToggleNicknameEditor = { isEditingNickname = !isEditingNickname },
                    onSaveNickname = {
                        viewModel.savePersonalInfo(
                            nickname = nicknameDraft,
                            avatarUri = null
                        )
                        isEditingNickname = false
                    }
                )
            }

            item {
                SettingsOverview(stats = uiState.stats)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    menuEntries.forEach { entry ->
                        SettingsMenuRow(
                            entry = entry,
                            icon = when (entry.title) {
                                "AI 配置" -> Icons.Rounded.AutoAwesome
                                "数据管理" -> Icons.Rounded.Storage
                                else -> Icons.Rounded.Info
                            },
                            onClick = {
                                when (entry.title) {
                                    "AI 配置" -> onNavigateToAiConfig()
                                    "数据管理" -> onNavigateToDataManagement()
                                    "关于" -> onNavigateToAbout()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsOverview(stats: SettingsOverviewStats) {
    val cards = settingsOverviewCards(stats)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            cards.take(2).forEach { card ->
                SettingsStatCard(card = card, modifier = Modifier.weight(1f))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            cards.drop(2).forEach { card ->
                SettingsStatCard(card = card, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SettingsStatCard(
    card: SettingsOverviewCard,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = card.containerColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = card.borderColor,
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = card.title,
                style = MaterialTheme.typography.labelMedium,
                color = JournalTokens.MutedInk
            )
            Text(
                text = card.value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = card.accentColor,
                fontFamily = HeadlineFontFamily,
                fontSize = 19.sp
            )
        }
    }
}

@Composable
private fun ProfileHero(
    nickname: String,
    avatarPath: String,
    avatarRefreshVersion: Int,
    nicknameDraft: String,
    isEditingNickname: Boolean,
    isSaving: Boolean,
    onEditAvatar: () -> Unit,
    onNicknameDraftChange: (String) -> Unit,
    onToggleNicknameEditor: () -> Unit,
    onSaveNickname: () -> Unit
) {
    val context = LocalContext.current
    val avatarModel = remember(avatarPath, avatarRefreshVersion) {
        if (avatarPath.isBlank()) {
            null
        } else {
            ImageRequest.Builder(context)
                .data(avatarPath)
                .memoryCacheKey("$avatarPath#${avatarRefreshVersion}")
                .diskCacheKey("$avatarPath#${avatarRefreshVersion}")
                .build()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(CircleShape)
                .clickable(onClick = onEditAvatar)
                .border(
                    width = 3.dp,
                    color = Color.White,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (avatarModel != null) {
                SubcomposeAsyncImage(
                    model = avatarModel,
                    contentDescription = "头像",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(JournalTokens.SageContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profileAvatarFallbackLabel(nickname),
                                color = JournalTokens.Sage,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp
                                )
                            )
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(JournalTokens.SageContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profileAvatarFallbackLabel(nickname),
                        color = JournalTokens.Sage,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                    )
                }
            }

            FilledTonalIconButton(
                onClick = onEditAvatar,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(34.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = JournalTokens.Sage,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.PhotoCamera,
                    contentDescription = "编辑头像",
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        if (isEditingNickname) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nicknameDraft,
                    onValueChange = onNicknameDraftChange,
                    textStyle = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                    singleLine = true,
                    label = { Text("昵称") }
                )
                FilledTonalIconButton(
                    onClick = onSaveNickname,
                    enabled = !isSaving && nicknameDraft.isNotBlank(),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "保存昵称"
                    )
                }
            }
        } else {
            Text(
                text = nickname,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = JournalTokens.Ink,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                ),
                modifier = Modifier.clickable(onClick = onToggleNicknameEditor)
            )
        }
    }
}

@Composable
private fun SettingsMenuRow(
    entry: SettingsMenuEntry,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
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
                        imageVector = icon,
                        contentDescription = null,
                        tint = JournalTokens.Sage
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = JournalTokens.Ink
                )
                Text(
                    text = entry.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 14.sp),
                    color = JournalTokens.MutedInk
                )
            }
            entry.trailingText?.let { trailing ->
                Text(
                    text = trailing,
                    style = MaterialTheme.typography.labelMedium,
                    color = JournalTokens.Sage,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
