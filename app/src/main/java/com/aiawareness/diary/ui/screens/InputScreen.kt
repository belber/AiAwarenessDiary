package com.aiawareness.diary.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.ModeEdit
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.PsychologyAlt
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.aiawareness.diary.data.model.Record
import com.aiawareness.diary.ui.theme.BodyFontFamily
import com.aiawareness.diary.ui.theme.HeadlineFontFamily
import com.aiawareness.diary.ui.theme.JournalTokens
import com.aiawareness.diary.util.DateUtil
import kotlinx.coroutines.launch
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun InputScreen(
    onNavigateToCalendar: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAiConfig: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    var inputText by remember { mutableStateOf("") }
    var isInputExpanded by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<Record?>(null) }
    var editingText by remember { mutableStateOf("") }
    var menuRecordId by remember { mutableLongStateOf(-1L) }

    LaunchedEffect(uiState.error) {
        val error = uiState.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(error, duration = SnackbarDuration.Short)
        viewModel.clearError()
    }

    Scaffold(
        containerColor = JournalTokens.Paper,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = JournalTokens.Paper.copy(alpha = 0.88f),
                tonalElevation = 0.dp
            ) {
                HomeTopBar(
                    nickname = uiState.userSettings.nickname.ifBlank { "用户" },
                    avatarPath = uiState.userSettings.avatarPath,
                    dateText = DateUtil.getCurrentDisplayDateWithWeekday(),
                    onNavigateToReview = onNavigateToCalendar,
                    onNavigateToSettings = onNavigateToSettings
                )
            }
        },
        bottomBar = {
            HomeInputBar(
                value = inputText,
                expanded = isInputExpanded,
                focusRequester = focusRequester,
                onValueChange = { inputText = it },
                onFocusChanged = { isInputExpanded = it },
                onMicClick = {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                },
                onSendClick = {
                    val trimmed = inputText.trim()
                    if (trimmed.isBlank()) return@HomeInputBar
                    viewModel.saveRecord(trimmed)
                    inputText = ""
                    isInputExpanded = false
                    keyboardController?.hide()
                }
            )
        }
    ) { paddingValues ->
        val timelineSections = buildHomeTimelineSections(uiState.records)
        val showAiCard = uiState.records.isNotEmpty()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(JournalTokens.Paper),
            contentPadding = PaddingValues(
                start = JournalTokens.ScreenPadding,
                end = JournalTokens.ScreenPadding,
                top = paddingValues.calculateTopPadding() + 12.dp,
                bottom = paddingValues.calculateBottomPadding() + 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                DailyProgress(recordCount = uiState.records.size)
            }

            if (timelineSections.isEmpty()) {
                item {
                    EmptyStateMessage()
                }
            } else {
                timelineSections.forEach { section ->
                    item {
                        TimelineSectionHeader(title = section.title)
                    }

                    items(section.records, key = { it.id }) { record ->
                        JournalEntryCard(
                            record = record,
                            icon = iconForHomeRecord(record).imageVector(),
                            expanded = menuRecordId == record.id,
                            onLongPress = { menuRecordId = record.id },
                            onDismissMenu = { menuRecordId = -1L },
                            onEdit = {
                                editingRecord = record
                                editingText = record.content
                                menuRecordId = -1L
                            },
                            onDelete = {
                                menuRecordId = -1L
                                viewModel.deleteRecord(record.id)
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "已删除记录",
                                        actionLabel = "撤回",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result.name == "ActionPerformed") {
                                        viewModel.restoreRecord(record)
                                    }
                                }
                            }
                        )
                    }
                }
            }

            if (showAiCard) {
                item {
                    AiDiaryActionCard(
                        hasApiKey = uiState.hasApiKey,
                        hasDiary = !uiState.diary?.aiDiary.isNullOrBlank(),
                        isGenerating = uiState.isGeneratingDiary,
                        onGenerate = viewModel::generateDiary,
                        onNavigateToAiConfig = onNavigateToAiConfig
                    )
                }
            }
        }
    }

    if (editingRecord != null) {
        AlertDialog(
            onDismissRequest = { editingRecord = null },
            title = { Text("编辑记录") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = editingRecord?.time.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = JournalTokens.MutedInk
                    )
                    BasicTextField(
                        value = editingText,
                        onValueChange = { editingText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp)
                            .background(JournalTokens.SurfaceLow, RoundedCornerShape(20.dp))
                            .padding(16.dp),
                        textStyle = TextStyle(
                            color = JournalTokens.Ink,
                            fontSize = 16.sp,
                            lineHeight = 26.sp
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val target = editingRecord ?: return@TextButton
                        val trimmed = editingText.trim()
                        if (trimmed.isNotBlank()) {
                            viewModel.updateRecord(target.copy(content = trimmed))
                        }
                        editingRecord = null
                    }
                ) {
                    Text("保存", color = JournalTokens.Sage)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingRecord = null }) {
                    Text("取消", color = JournalTokens.MutedInk)
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun HomeTopBar(
    nickname: String,
    avatarPath: String,
    dateText: String,
    onNavigateToReview: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = JournalTokens.SurfaceHigh
            ) {
                if (avatarPath.isNotBlank()) {
                    AsyncImage(
                        model = avatarPath,
                        contentDescription = "头像",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(JournalTokens.Sage, JournalTokens.SageDim)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = nickname.take(1),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = nickname, color = JournalTokens.Ink, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = dateText,
                    color = JournalTokens.MutedInk,
                    fontFamily = HeadlineFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onNavigateToReview,
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JournalTokens.SurfaceHigh,
                    contentColor = JournalTokens.Sage
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text("回顾")
            }
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = JournalTokens.SurfaceHigh,
                onClick = onNavigateToSettings
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "设置",
                        tint = JournalTokens.MutedInk
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyProgress(recordCount: Int) {
    val filledDots = progressDots(recordCount)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "今天已记录 $recordCount 次",
            color = JournalTokens.MutedInk,
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 2.sp
        )
        Row(
            modifier = Modifier.padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filledDots.forEach { filled ->
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (filled) JournalTokens.Sage else JournalTokens.SurfaceHighest)
                )
            }
        }
    }
}

@Composable
private fun EmptyStateMessage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 96.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "回到呼吸里，感受一下此刻心里有什么",
            color = JournalTokens.MutedInk,
            fontFamily = HeadlineFontFamily,
            fontStyle = FontStyle.Italic,
            fontSize = 24.sp,
            lineHeight = 34.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun TimelineSectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(1.dp)
                .background(JournalTokens.SurfaceHighest)
        )
        Text(
            text = title,
            color = JournalTokens.MutedInk,
            fontFamily = HeadlineFontFamily,
            fontStyle = FontStyle.Italic,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(1.dp)
                .background(JournalTokens.SurfaceHighest)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun JournalEntryCard(
    record: Record,
    icon: ImageVector,
    expanded: Boolean,
    onLongPress: () -> Unit,
    onDismissMenu: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongPress
                ),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = record.time,
                        color = JournalTokens.MutedInk,
                        fontFamily = HeadlineFontFamily,
                        fontStyle = FontStyle.Italic,
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTintForHomeRecord(record).copy(alpha = 0.45f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = record.content,
                    color = JournalTokens.Ink,
                    fontFamily = BodyFontFamily,
                    fontSize = 18.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissMenu,
            modifier = Modifier.background(Color.White)
        ) {
            DropdownMenuItem(
                text = { Text("编辑") },
                onClick = onEdit,
                leadingIcon = {
                    Icon(Icons.Outlined.ModeEdit, contentDescription = null)
                }
            )
            DropdownMenuItem(
                text = { Text("删除") },
                onClick = onDelete,
                leadingIcon = {
                    Icon(Icons.Outlined.MoreHoriz, contentDescription = null)
                }
            )
        }
    }
}

@Composable
private fun AiDiaryActionCard(
    hasApiKey: Boolean,
    hasDiary: Boolean,
    isGenerating: Boolean,
    onGenerate: () -> Unit,
    onNavigateToAiConfig: () -> Unit
) {
    val buttonLabel = when {
        isGenerating -> "生成中..."
        hasDiary -> "重新生成 AI日记"
        else -> "生成今日 AI日记"
    }
    val cardColor = if (hasApiKey) JournalTokens.SageContainer.copy(alpha = 0.35f) else JournalTokens.SurfaceLow

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 16.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            shape = RoundedCornerShape(32.dp),
            color = cardColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = JournalTokens.Sage,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "今日觉察之旅接近尾声",
                    color = JournalTokens.Ink,
                    fontFamily = HeadlineFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center
                )
                if (!hasApiKey) {
                    Text(
                        text = "完成 AI 配置后，就可以生成今天的 AI日记",
                        color = JournalTokens.MutedInk,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
                Button(
                    onClick = { if (hasApiKey) onGenerate() else onNavigateToAiConfig() },
                    enabled = !isGenerating,
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JournalTokens.Sage,
                        contentColor = Color.White,
                        disabledContainerColor = JournalTokens.Sage.copy(alpha = 0.72f),
                        disabledContentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text(buttonLabel, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeInputBar(
    value: String,
    expanded: Boolean,
    focusRequester: FocusRequester,
    onValueChange: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    onMicClick: () -> Unit,
    onSendClick: () -> Unit
) {
    val barHeight by animateDpAsState(if (expanded) 88.dp else 68.dp, label = "home-input-height")

    Surface(
        color = Color.White.copy(alpha = 0.9f),
        shape = RoundedCornerShape(999.dp),
        shadowElevation = 2.dp,
        tonalElevation = 1.dp,
        modifier = Modifier
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = barHeight)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onMicClick) {
                Icon(
                    imageVector = Icons.Outlined.Mic,
                    contentDescription = "语音输入",
                    tint = JournalTokens.SageDim
                )
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { onFocusChanged(it.isFocused) },
                textStyle = TextStyle(
                    color = JournalTokens.Ink,
                    fontSize = 14.sp,
                    fontFamily = BodyFontFamily
                ),
                maxLines = if (expanded) 3 else 1,
                singleLine = !expanded,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSend = { onSendClick() }),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (value.isBlank()) {
                            Text(
                                text = "此刻感觉到什么？用语音输入会更快",
                                color = JournalTokens.MutedInk.copy(alpha = 0.6f),
                                fontSize = 14.sp,
                                fontFamily = BodyFontFamily
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Button(
                onClick = onSendClick,
                enabled = value.isNotBlank(),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JournalTokens.Sage,
                    contentColor = Color.White,
                    disabledContainerColor = JournalTokens.SurfaceHigh,
                    disabledContentColor = JournalTokens.MutedInk
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("发送")
                }
            }
        }
    }
}

private fun HomeRecordIcon.imageVector(): ImageVector = when (this) {
    HomeRecordIcon.Breath -> Icons.Outlined.Eco
    HomeRecordIcon.Air -> Icons.Outlined.Air
    HomeRecordIcon.Rest -> Icons.Outlined.NightsStay
    HomeRecordIcon.Heart -> Icons.Outlined.FavoriteBorder
    HomeRecordIcon.Reflection -> Icons.Outlined.PsychologyAlt
}

private fun iconTintForHomeRecord(record: Record): Color = when (iconForHomeRecord(record)) {
    HomeRecordIcon.Rest -> JournalTokens.Clay
    else -> JournalTokens.Sage
}
