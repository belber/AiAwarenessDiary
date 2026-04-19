package com.aiawareness.diary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.PsychologyAlt
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.aiawareness.diary.data.model.Record
import com.aiawareness.diary.ui.components.CalendarView
import com.aiawareness.diary.ui.components.EditorialSurfaceCard
import com.aiawareness.diary.ui.components.EditorialTopBar
import com.aiawareness.diary.ui.components.PhotoPreviewDialog
import com.aiawareness.diary.ui.theme.HeadlineFontFamily
import com.aiawareness.diary.ui.theme.JournalTokens
import com.aiawareness.diary.util.DateUtil
import kotlinx.coroutines.launch
import java.time.YearMonth

@Composable
fun CalendarScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAiConfig: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var selectedDate by remember(uiState.currentDate) { mutableStateOf(uiState.currentDate) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var pickerYear by remember(uiState.currentYear) { mutableStateOf(uiState.currentYear) }
    var menuRecordId by remember { mutableLongStateOf(-1L) }
    var editingRecord by remember { mutableStateOf<Record?>(null) }
    var editingText by remember { mutableStateOf("") }
    var previewPhotoPath by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    fun jumpToMonth(year: Int, month: Int) {
        val targetDate = "%04d-%02d-01".format(year, month)
        pickerYear = year
        selectedDate = targetDate
        viewModel.updateMonth(year, month)
        viewModel.loadDatesWithRecords()
        viewModel.loadRecordsForDate(targetDate)
    }

    fun jumpToToday() {
        val today = DateUtil.getCurrentDate()
        val (year, month) = DateUtil.parseYearMonth(today)
        pickerYear = year
        selectedDate = today
        viewModel.updateMonth(year, month)
        viewModel.loadDatesWithRecords()
        viewModel.loadRecordsForDate(today)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                var totalDrag = 0f
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        totalDrag += dragAmount
                    },
                    onDragEnd = {
                        if (totalDrag < -120f) {
                            onNavigateToSettings()
                        }
                        totalDrag = 0f
                    }
                )
            }
    ) {
        Scaffold(
            containerColor = JournalTokens.Paper,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { _ ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(JournalTokens.Paper),
                contentPadding = PaddingValues(
                    start = JournalTokens.ScreenPadding,
                    end = JournalTokens.ScreenPadding,
                    top = 10.dp,
                    bottom = reviewTimelineBottomPadding()
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
            item {
                EditorialTopBar(title = "回顾", onBack = onNavigateBack)
            }

            item {
                EditorialSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = JournalTokens.SurfaceLow,
                            modifier = Modifier.clickable { showMonthPicker = true }
                        ) {
                            Text(
                                text = reviewMonthTitle(uiState.currentYear, uiState.currentMonth),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                                color = JournalTokens.Ink
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = { jumpToToday() }) {
                                Text(reviewJumpToTodayLabel(), color = JournalTokens.Sage)
                            }
                            IconButton(onClick = {
                                val previous = YearMonth.of(uiState.currentYear, uiState.currentMonth).minusMonths(1)
                                jumpToMonth(previous.year, previous.monthValue)
                            }) {
                                Icon(Icons.Outlined.ArrowBack, contentDescription = "上个月")
                            }

                            IconButton(onClick = {
                                val next = YearMonth.of(uiState.currentYear, uiState.currentMonth).plusMonths(1)
                                jumpToMonth(next.year, next.monthValue)
                            }) {
                                Icon(Icons.Outlined.ArrowForward, contentDescription = "下个月")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    CalendarView(
                        year = uiState.currentYear,
                        month = uiState.currentMonth,
                        datesWithRecords = uiState.datesWithRecords,
                        selectedDate = selectedDate,
                        onDateSelected = { date ->
                            selectedDate = date
                            viewModel.loadRecordsForDate(date)
                        }
                    )
                }
            }

            item {
                val hasAiDiary = !uiState.diary?.aiDiary.isNullOrBlank()
                val diaryText = uiState.diary?.aiDiary?.takeIf { it.isNotBlank() } ?: "这一天还没有生成 AI 日记。"
                val diaryPhotoPaths = reviewDiaryPhotoPaths(
                    hasAiDiary = hasAiDiary,
                    records = uiState.records
                )
                ReviewSectionHeader(
                    kind = ReviewSectionKind.AiDiary,
                    title = "AI日记",
                    trailingContent = {
                        ReviewDiaryAction(
                            hasApiKey = uiState.hasApiKey,
                            hasDiary = !uiState.diary?.aiDiary.isNullOrBlank(),
                            isGenerating = uiState.isGeneratingDiary,
                            onGenerate = {
                                val hint = reviewDiaryGenerationHint(uiState.records.size)
                                if (hint != null) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(hint)
                                    }
                                } else {
                                    viewModel.generateDiary()
                                }
                            },
                            onNavigateToAiConfig = onNavigateToAiConfig
                        )
                    }
                )
                Spacer(modifier = Modifier.height(14.dp))
                ReviewInsightBlock(
                    text = diaryText,
                    tone = ReviewAccentTone.Sage,
                    photoPaths = diaryPhotoPaths,
                    onPhotoClick = { previewPhotoPath = it }
                )
            }

            item {
                val insightText = uiState.diary?.aiInsight?.takeIf { it.isNotBlank() } ?: "生成后会在这里显示摘要与觉察提示。"
                ReviewSectionHeader(kind = ReviewSectionKind.AiSummary, title = reviewAiSummaryTitle())
                Spacer(modifier = Modifier.height(14.dp))
                ReviewInsightBlock(
                    text = insightText,
                    tone = ReviewAccentTone.Clay
                )
            }

            item {
                ReviewSectionHeader(
                    kind = ReviewSectionKind.RawRecords,
                    title = "觉察片段",
                    trailing = reviewEntryCountLabel(uiState.records.size)
                )
                Spacer(modifier = Modifier.height(14.dp))
                if (uiState.records.isEmpty()) {
                    ReviewEmptyBlock("今天还没有留下什么，回首页，从呼吸开始这一刻")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.records.forEach { record ->
                            ReviewRecordBlock(
                                record = record,
                                expanded = menuRecordId == record.id,
                                onLongPress = { menuRecordId = record.id },
                                onDismissMenu = { menuRecordId = -1L },
                                onPhotoClick = { previewPhotoPath = it },
                                onEdit = {
                                    editingRecord = record
                                    editingText = record.content
                                    menuRecordId = -1L
                                },
                                onDelete = {
                                    menuRecordId = -1L
                                    viewModel.deleteRecord(record.id)
                                    coroutineScope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "已删除记录",
                                            actionLabel = "撤回"
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
            }
        }
    }
    }

    if (showMonthPicker) {
        ReviewMonthPickerDialog(
            selectedYear = pickerYear,
            selectedMonth = uiState.currentMonth,
            yearOptions = reviewYearOptions(uiState.currentYear, uiState.datesWithRecords),
            onSelectYear = { pickerYear = it },
            onSelectMonth = { month ->
                showMonthPicker = false
                jumpToMonth(pickerYear, month)
            },
            onDismiss = { showMonthPicker = false }
        )
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
                            .background(JournalTokens.SurfaceLow, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        textStyle = TextStyle(
                            color = JournalTokens.Ink,
                            fontSize = 16.sp,
                            lineHeight = 24.sp
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

    previewPhotoPath?.let { path ->
        PhotoPreviewDialog(
            photoPath = path,
            onDismiss = { previewPhotoPath = null }
        )
    }
}

@Composable
private fun ReviewSectionHeader(
    kind: ReviewSectionKind,
    title: String,
    trailing: String? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val accent = reviewSectionAccent(kind)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = reviewAccentIcon(accent.iconName),
                contentDescription = null,
                tint = reviewAccentColor(accent.tone),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = JournalTokens.Ink
            )
        }
        when {
            trailingContent != null -> trailingContent()
            trailing != null -> Text(
                text = trailing,
                style = MaterialTheme.typography.labelSmall,
                color = JournalTokens.MutedInk,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(JournalTokens.SurfaceHigh)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun ReviewDiaryAction(
    hasApiKey: Boolean,
    hasDiary: Boolean,
    isGenerating: Boolean,
    onGenerate: () -> Unit,
    onNavigateToAiConfig: () -> Unit
) {
    Button(
        onClick = { if (hasApiKey) onGenerate() else onNavigateToAiConfig() },
        enabled = !isGenerating,
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = JournalTokens.Sage,
            contentColor = Color.White,
            disabledContainerColor = JournalTokens.Sage.copy(alpha = 0.65f),
            disabledContentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = reviewDiaryActionLabel(hasApiKey, hasDiary, isGenerating),
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ReviewRecordBlock(
    record: Record,
    expanded: Boolean,
    onLongPress: () -> Unit,
    onDismissMenu: () -> Unit,
    onPhotoClick: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongPress
                ),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 0.5.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 3.dp, height = 48.dp)
                        .background(reviewRecordAccent(record), RoundedCornerShape(999.dp))
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = record.time,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = reviewRecordTimeTextColor(),
                            fontFamily = HeadlineFontFamily,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(reviewRecordTimeContainerColor())
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                        Icon(
                            imageVector = reviewRecordIcon(record),
                            contentDescription = null,
                            tint = reviewRecordAccent(record),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    ReviewBodyText(record.content)
                    if (reviewRecordShowsPhoto(record)) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier
                                .size(width = 112.dp, height = 84.dp)
                                .clickable { onPhotoClick(record.photoPath) },
                            shape = RoundedCornerShape(12.dp),
                            color = JournalTokens.SurfaceHigh
                        ) {
                            SubcomposeAsyncImage(
                                model = record.photoPath,
                                contentDescription = "记录图片",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissMenu
        ) {
            DropdownMenuItem(
                text = { Text("编辑") },
                onClick = onEdit
            )
            DropdownMenuItem(
                text = { Text("删除") },
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun ReviewMonthPickerDialog(
    selectedYear: Int,
    selectedMonth: Int,
    yearOptions: List<Int>,
    onSelectYear: (Int) -> Unit,
    onSelectMonth: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭", color = JournalTokens.MutedInk)
            }
        },
        title = {
            Text(
                text = "快速切换年月",
                color = JournalTokens.Ink
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "年份",
                        style = MaterialTheme.typography.labelMedium,
                        color = JournalTokens.MutedInk
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        yearOptions.forEach { year ->
                            ReviewPickerChip(
                                text = year.toString(),
                                selected = year == selectedYear,
                                onClick = { onSelectYear(year) }
                            )
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "月份",
                        style = MaterialTheme.typography.labelMedium,
                        color = JournalTokens.MutedInk
                    )
                    repeat(3) { rowIndex ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            repeat(4) { columnIndex ->
                                val month = rowIndex * 4 + columnIndex + 1
                                Box(modifier = Modifier.weight(1f)) {
                                    ReviewPickerChip(
                                        text = reviewMonthChipLabel(month),
                                        selected = month == selectedMonth,
                                        onClick = { onSelectMonth(month) },
                                        centered = true
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color.White
    )
}

@Composable
private fun ReviewPickerChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    centered: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) JournalTokens.SageContainer else JournalTokens.SurfaceLow
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            color = if (selected) JournalTokens.Sage else JournalTokens.Ink,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = if (centered) TextAlign.Center else TextAlign.Start
        )
    }
}

@Composable
private fun ReviewInsightBlock(
    text: String,
    tone: ReviewAccentTone,
    photoPaths: List<String> = emptyList(),
    onPhotoClick: (String) -> Unit = {}
) {
    val containerColor = when (tone) {
        ReviewAccentTone.Sage -> JournalTokens.SurfaceLow
        ReviewAccentTone.Clay -> JournalTokens.ClayContainer.copy(alpha = 0.25f)
        ReviewAccentTone.Stone -> JournalTokens.SurfaceHigh
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = containerColor
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (reviewInsightHasPattern(tone)) {
                ReviewInsightPattern(
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(modifier = Modifier.padding(14.dp)) {
                if (tone == ReviewAccentTone.Sage) {
                    val diaryDisplay = reviewDiaryDisplay(text)
                    if (diaryDisplay.title.isNotBlank()) {
                        Text(
                            text = diaryDisplay.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = JournalTokens.Ink,
                            lineHeight = 21.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (diaryDisplay.body.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = diaryDisplay.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = JournalTokens.Ink,
                            lineHeight = 21.sp
                        )
                    }
                } else {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = JournalTokens.Ink,
                        lineHeight = 21.sp
                    )
                }
                if (photoPaths.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    ReviewPhotoGrid(
                        photoPaths = photoPaths,
                        onPhotoClick = onPhotoClick
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewPhotoGrid(
    photoPaths: List<String>,
    onPhotoClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        photoPaths.chunked(4).forEach { rowPaths ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(4) { index ->
                    val path = rowPaths.getOrNull(index)
                    if (path != null) {
                        Box(modifier = Modifier.weight(1f)) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clickable { onPhotoClick(path) },
                                shape = RoundedCornerShape(10.dp),
                                color = JournalTokens.SurfaceHigh
                            ) {
                                SubcomposeAsyncImage(
                                    model = path,
                                    contentDescription = "AI日记关联图片",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewInsightPattern(modifier: Modifier = Modifier) {
    val diagonalColor = JournalTokens.Sage.copy(alpha = 0.08f)
    val glowColor = JournalTokens.SageContainer.copy(alpha = 0.35f)

    Canvas(
        modifier = modifier.background(
            brush = Brush.linearGradient(
                colors = listOf(
                    glowColor,
                    Color.Transparent,
                    JournalTokens.SageContainer.copy(alpha = 0.14f)
                ),
                start = Offset.Zero,
                end = Offset.Infinite
            )
        )
    ) {
        val spacing = 28.dp.toPx()
        val width = size.width
        val height = size.height
        var startX = -height
        while (startX < width + height) {
            drawLine(
                color = diagonalColor,
                start = Offset(startX, height),
                end = Offset(startX + height, 0f),
                strokeWidth = 1.dp.toPx()
            )
            startX += spacing
        }
    }
}

@Composable
private fun ReviewBodyText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = JournalTokens.Ink
    )
}

@Composable
private fun ReviewEmptyBlock(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = JournalTokens.SurfaceLow
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = JournalTokens.MutedInk,
            modifier = Modifier.padding(14.dp)
        )
    }
}

private fun reviewAccentIcon(name: String): ImageVector =
    when (name) {
        "auto_awesome" -> Icons.Outlined.AutoAwesome
        "analytics" -> Icons.Outlined.Analytics
        else -> Icons.Outlined.MenuBook
    }

private fun reviewAccentColor(tone: ReviewAccentTone): Color =
    when (tone) {
        ReviewAccentTone.Sage -> JournalTokens.Sage
        ReviewAccentTone.Clay -> JournalTokens.Clay
        ReviewAccentTone.Stone -> JournalTokens.Stone
    }

private fun reviewRecordIcon(record: Record): ImageVector =
    when {
        record.content.contains("风") || record.content.contains("空气") -> Icons.Outlined.Air
        record.content.contains("呼吸") || record.content.contains("平静") -> Icons.Outlined.SelfImprovement
        else -> Icons.Outlined.PsychologyAlt
    }

private fun reviewRecordAccent(record: Record): Color =
    when (reviewRecordIcon(record)) {
        Icons.Outlined.PsychologyAlt -> JournalTokens.Stone
        Icons.Outlined.Air -> JournalTokens.Sage
        else -> JournalTokens.Clay
    }

internal fun reviewRecordTimeTextColor(): Color = HomePrimary

internal fun reviewRecordTimeContainerColor(): Color = HomePrimarySoft

internal fun reviewRecordTimeUsesAccentColor(): Boolean = false
