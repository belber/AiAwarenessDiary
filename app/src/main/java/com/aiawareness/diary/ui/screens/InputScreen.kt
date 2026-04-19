package com.aiawareness.diary.ui.screens

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ModeEdit
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.PsychologyAlt
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.aiawareness.diary.R
import com.aiawareness.diary.data.model.Record
import com.aiawareness.diary.ui.components.PhotoPreviewDialog
import com.aiawareness.diary.data.model.defaultDisplayNickname
import com.aiawareness.diary.ui.theme.BodyFontFamily
import com.aiawareness.diary.ui.theme.JournalTokens
import kotlinx.coroutines.launch
import java.io.File

internal val HomeBackground = Color(0xFFF8F9FB)
internal val HomeSurface = Color(0xFFFFFFFF)
internal val HomeSurfaceMuted = Color(0xFFF3F4F6)
internal val HomePromptSurface = Color(0xFFF8F1E8)
internal val HomeOutline = Color(0xFFE1E2E4)
internal val HomePrimary = Color(0xFF1E52CC)
internal val HomePrimarySoft = Color(0xFFDBE1FF)
internal val HomeTextPrimary = Color(0xFF191C1E)
internal val HomeTextSecondary = Color(0xFF434654)
internal val HomeRestTint = Color(0xFF4E5C8C)

enum class HomeAiFabHorizontalAlignment { Start, End }
enum class HomeAiFabVerticalAlignment { Top, Bottom }

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun InputScreen(
    onNavigateToCalendar: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var showPhotoEntrySheet by remember { mutableStateOf(false) }
    var pendingCameraTempFilePath by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCameraPhotoUriString by rememberSaveable { mutableStateOf<String?>(null) }
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.importPendingPhoto(uri)
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val tempFile = pendingCameraTempFilePath?.let(::File)
        val tempUri = pendingCameraPhotoUriString?.let(Uri::parse)
        if (success && tempFile != null && tempUri != null) {
            viewModel.setPendingCapturedPhoto(tempFile)
        } else if (!success) {
            tempFile?.takeIf(File::exists)?.delete()
        }
        pendingCameraTempFilePath = null
        pendingCameraPhotoUriString = null
    }

    var inputText by remember { mutableStateOf("") }
    var editingRecord by remember { mutableStateOf<Record?>(null) }
    var editingText by remember { mutableStateOf("") }
    var menuRecordId by remember { mutableLongStateOf(-1L) }
    var pendingGenerateHint by remember { mutableStateOf<String?>(null) }
    var previewPhotoPath by remember { mutableStateOf<String?>(null) }
    val hasPendingPhoto = uiState.pendingPhotoUri != null || uiState.pendingCapturedPhotoFile != null

    LaunchedEffect(uiState.error) {
        val error = uiState.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(error, duration = SnackbarDuration.Short)
        viewModel.clearError()
    }

    LaunchedEffect(
        uiState.generatedDiaryReadyDate,
        pendingGenerateHint,
        uiState.autoDiaryGenerationHint
    ) {
        val message = homeAiSnackbarMessage(
            generatedDiaryReadyDate = uiState.generatedDiaryReadyDate,
            inlineHint = pendingGenerateHint,
            autoGenerationHint = uiState.autoDiaryGenerationHint
        ) ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
        pendingGenerateHint = null
        viewModel.clearGeneratedDiaryReadyDate()
        viewModel.clearAutoDiaryGenerationHint()
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
                            onNavigateToCalendar()
                        }
                        totalDrag = 0f
                    }
                )
            }
    ) {
        Scaffold(
            containerColor = HomeBackground,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = HomeBackground.copy(alpha = 0.97f),
                    tonalElevation = 0.dp
                ) {
                    HomeTopBar(
                        nickname = uiState.userSettings.nickname.ifBlank { defaultDisplayNickname() },
                        avatarPath = uiState.userSettings.avatarPath,
                        currentDate = uiState.currentDate,
                        onNavigateToReview = onNavigateToCalendar,
                        onNavigateToSettings = onNavigateToSettings
                    )
                }
            },
            bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = HomeBackground.copy(alpha = 0.97f)
                ) {
                    HomeInputBar(
                        value = inputText,
                        hasPendingPhoto = hasPendingPhoto,
                        pendingPhotoPreviewModel = uiState.pendingPhotoUri ?: uiState.pendingCapturedPhotoFile?.absolutePath,
                        focusRequester = focusRequester,
                        onValueChange = { inputText = it },
                        onFocusChanged = {},
                        onPickPhoto = { showPhotoEntrySheet = true },
                        onSendClick = {
                            val trimmed = inputText.trim()
                            viewModel.saveRecord(trimmed)
                            inputText = ""
                            keyboardController?.hide()
                        }
                    )
                }
            }
        ) { paddingValues ->
            val timelineSections = buildHomeTimelineSections(uiState.records)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(HomeBackground),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = paddingValues.calculateTopPadding() + 4.dp,
                    bottom = paddingValues.calculateBottomPadding() + homeTimelineBottomPadding(hasPendingPhoto = hasPendingPhoto)
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (timelineSections.isNotEmpty()) {
                    timelineSections.forEachIndexed { index, section ->
                        item { TimelineSectionHeader(title = section.title, isFirstSection = index == 0) }

                        itemsIndexed(section.records, key = { _, record -> record.id }) { _, record ->
                            JournalEntryCard(
                                record = record,
                                icon = iconForHomeRecord(record).imageVector(),
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
                } else {
                    item {
                        HomeEmptyStateCard()
                    }
                }
            }
        }

        HomeAiFab(
            recordCount = uiState.records.size,
            hasApiKey = uiState.hasApiKey,
            hasDiary = !uiState.diary?.aiDiary.isNullOrBlank(),
            hasPendingPhoto = hasPendingPhoto,
            isGenerating = uiState.isGeneratingDiary,
            onClick = {
                when (homeAiEntryAction(
                    recordCount = uiState.records.size,
                    hasApiKey = uiState.hasApiKey,
                    isGenerating = uiState.isGeneratingDiary
                )) {
                    HomeAiEntryAction.ShowRecordHint -> {
                        pendingGenerateHint = "先记录今天的内容，再生成 AI 日记"
                    }
                    HomeAiEntryAction.ShowAiConfigHint -> {
                        pendingGenerateHint = homeAiConfigRequiredSnackbarMessage()
                    }
                    HomeAiEntryAction.GenerateDiary -> {
                        pendingGenerateHint = null
                        viewModel.generateDiary()
                    }
                    HomeAiEntryAction.None -> Unit
                }
            }
        )
    }

    if (showPhotoEntrySheet) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoEntrySheet = false },
            containerColor = HomeSurface
        ) {
            ListItem(
                headlineContent = { Text(homePhotoSheetPickLabel()) },
                modifier = Modifier.clickable {
                    showPhotoEntrySheet = false
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
            ListItem(
                headlineContent = { Text(homePhotoSheetCaptureLabel()) },
                modifier = Modifier.clickable {
                    showPhotoEntrySheet = false
                    val tempFile = viewModel.createPendingCameraTempFileOrNull()
                    if (tempFile == null) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                homePhotoCaptureFailureMessage(),
                                duration = SnackbarDuration.Short
                            )
                        }
                        return@clickable
                    }

                    try {
                        val photoUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            tempFile
                        )
                        pendingCameraTempFilePath = tempFile.absolutePath
                        pendingCameraPhotoUriString = photoUri.toString()
                        cameraLauncher.launch(photoUri)
                    } catch (_: ActivityNotFoundException) {
                        tempFile.takeIf(File::exists)?.delete()
                        pendingCameraTempFilePath = null
                        pendingCameraPhotoUriString = null
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                homePhotoNoCameraMessage(),
                                duration = SnackbarDuration.Short
                            )
                        }
                    } catch (_: Exception) {
                        tempFile.takeIf(File::exists)?.delete()
                        pendingCameraTempFilePath = null
                        pendingCameraPhotoUriString = null
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                homePhotoCaptureFailureMessage(),
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
            )
            Spacer(
                modifier = Modifier
                    .navigationBarsPadding()
                    .height(8.dp)
            )
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
                            .background(HomePrimarySoft, RoundedCornerShape(18.dp))
                            .padding(16.dp),
                        textStyle = TextStyle(
                            color = HomeTextPrimary,
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
                    Text("保存", color = HomePrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingRecord = null }) {
                    Text("取消", color = HomeTextSecondary)
                }
            },
            containerColor = HomeSurface
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
private fun HomeTopBar(
    nickname: String,
    avatarPath: String,
    currentDate: String,
    onNavigateToReview: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(homeTitleBrandIconRes()),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = homeTitleLabel(),
                    color = HomeTextPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = BodyFontFamily,
                        fontSize = 18.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onNavigateToReview,
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HomeSurfaceMuted,
                        contentColor = HomeTextSecondary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = homeReviewButtonLabel(),
                        fontFamily = BodyFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                FilledTonalIconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.size(36.dp),
                    colors = androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = HomeSurfaceMuted,
                        contentColor = HomeTextSecondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "设置",
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = HomeSurface,
            shadowElevation = 0.5.dp,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            color = HomeSurfaceMuted
                        ) {
                            if (avatarPath.isNotBlank()) {
                                SubcomposeAsyncImage(
                                    model = avatarPath,
                                    contentDescription = "头像",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    error = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.radialGradient(
                                                        colors = listOf(HomePrimary, Color(0xFF406CE7))
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = nickname.take(1),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                        }
                                    }
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(HomePrimary, Color(0xFF406CE7))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = nickname.take(1),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = nickname,
                            color = HomeTextPrimary,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = BodyFontFamily,
                                fontSize = 18.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = homeProgressSubtitle(),
                            color = HomeTextPrimary,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = BodyFontFamily,
                                fontSize = 15.sp,
                                lineHeight = 21.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(top = 6.dp, end = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))
                Surface(
                    modifier = Modifier.size(78.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = HomeSurfaceMuted
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = homeDateMonthDay(currentDate),
                            color = HomePrimary,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = BodyFontFamily,
                                fontSize = 16.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = homeDateWeekday(currentDate),
                            color = HomeTextSecondary,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = BodyFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(top = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineSectionHeader(title: String, isFirstSection: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (isFirstSection) 0.dp else 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            color = HomePrimary.copy(alpha = 0.72f),
            fontFamily = BodyFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            letterSpacing = 0.4.sp
        )
    }
}

@Composable
private fun HomeEmptyStateCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (homeEmptyStateUsesAlternateSurface()) HomePromptSurface else HomeSurface,
        shadowElevation = 0.5.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (homeEmptyStateShowsIcon()) {
                Icon(
                    painter = painterResource(homeTitleBrandIconRes()),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = homeEmptyStateTitle(),
                color = HomeTextPrimary,
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Text(
                text = homeEmptyStateBody(),
                color = HomeTextSecondary,
                fontFamily = BodyFontFamily,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun BoxScope.HomeAiFab(
    recordCount: Int,
    hasApiKey: Boolean,
    hasDiary: Boolean,
    hasPendingPhoto: Boolean,
    isGenerating: Boolean,
    onClick: () -> Unit
) {
    val action = homeAiEntryAction(recordCount = recordCount, hasApiKey = hasApiKey, isGenerating = isGenerating)
    val muted = action == HomeAiEntryAction.ShowRecordHint
    val alignment = when (homeAiFabHorizontalAlignment() to homeAiFabVerticalAlignment()) {
        HomeAiFabHorizontalAlignment.Start to HomeAiFabVerticalAlignment.Top -> Alignment.TopStart
        HomeAiFabHorizontalAlignment.End to HomeAiFabVerticalAlignment.Top -> Alignment.TopEnd
        HomeAiFabHorizontalAlignment.Start to HomeAiFabVerticalAlignment.Bottom -> Alignment.BottomStart
        HomeAiFabHorizontalAlignment.End to HomeAiFabVerticalAlignment.Bottom -> Alignment.BottomEnd
        else -> Alignment.BottomEnd
    }

    FilledTonalIconButton(
        onClick = onClick,
        enabled = !isGenerating,
        modifier = Modifier
            .align(alignment)
            .navigationBarsPadding()
            .padding(end = homeAiFabEndPadding(), bottom = homeAiFabBottomPadding(hasPendingPhoto = hasPendingPhoto))
            .size(homeAiFabSize()),
        shape = CircleShape,
        colors = androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = if (muted) HomePromptSurface else Color(0xFFF2F4F7),
            contentColor = if (muted) HomeTextSecondary else HomeTextPrimary,
            disabledContainerColor = Color(0xFFE7E9EE),
            disabledContentColor = HomeTextSecondary
        )
    ) {
        if (isGenerating) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = HomePrimary,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                painter = painterResource(homeInputBrandIconRes()),
                contentDescription = homeAiEntryButtonLabel(
                    recordCount = recordCount,
                    hasApiKey = hasApiKey,
                    hasDiary = hasDiary,
                    isGenerating = isGenerating
                ),
                tint = Color.Unspecified,
                modifier = Modifier.size(22.dp)
            )
        }
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
            color = HomeSurface,
            shadowElevation = 0.5.dp,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = record.time,
                        color = HomePrimary,
                        fontFamily = BodyFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(HomePrimarySoft)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTintForHomeRecord(record).copy(alpha = 0.42f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = record.content,
                    color = HomeTextPrimary,
                    fontFamily = BodyFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium
                )
                if (homeRecordShowsPhoto(record)) {
                    Surface(
                        modifier = Modifier
                            .size(width = 112.dp, height = 84.dp)
                            .clickable { onPhotoClick(record.photoPath) },
                        shape = RoundedCornerShape(12.dp),
                        color = HomeSurfaceMuted
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

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissMenu,
            modifier = Modifier.background(HomeSurface)
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
private fun HomeInputBar(
    value: String,
    hasPendingPhoto: Boolean,
    pendingPhotoPreviewModel: Any?,
    focusRequester: FocusRequester,
    onValueChange: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    onPickPhoto: () -> Unit,
    onSendClick: () -> Unit
) {
    Surface(
        color = HomeSurface.copy(alpha = 0.94f),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 6.dp,
        tonalElevation = 0.dp,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = homeInputBarVerticalPadding())
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = homeInputBarMinHeight())
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(homeInputBrandIconRes()),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp)
                )
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .onFocusChanged { onFocusChanged(it.isFocused) },
                    textStyle = TextStyle(
                        color = HomeTextPrimary,
                        fontSize = 14.sp,
                        fontFamily = BodyFontFamily
                    ),
                    maxLines = 1,
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSend = { onSendClick() }),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 5.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (value.isBlank()) {
                                Text(
                                    text = homeInputPlaceholderText(),
                                    color = HomeTextSecondary.copy(alpha = 0.7f),
                                    fontSize = 13.sp,
                                    fontFamily = BodyFontFamily
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                FilledTonalIconButton(
                    onClick = onPickPhoto,
                    modifier = Modifier.size(38.dp),
                    colors = androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (hasPendingPhoto) HomePrimarySoft else HomeSurfaceMuted,
                        contentColor = if (hasPendingPhoto) HomePrimary else HomeTextSecondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoLibrary,
                        contentDescription = homeAttachPhotoButtonLabel(),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Button(
                    onClick = onSendClick,
                    enabled = value.isNotBlank() || hasPendingPhoto,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HomePrimary,
                        contentColor = Color.White,
                        disabledContainerColor = HomeOutline,
                        disabledContentColor = HomeTextSecondary
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Send,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = homeSendButtonLabel(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = BodyFontFamily
                        )
                    }
                }
            }
            if (hasPendingPhoto) {
                Box(
                    modifier = Modifier
                        .padding(start = 42.dp, end = 12.dp, bottom = 10.dp),
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = HomeSurfaceMuted,
                        border = BorderStroke(1.dp, HomeOutline)
                    ) {
                        SubcomposeAsyncImage(
                            model = pendingPhotoPreviewModel,
                            contentDescription = homePendingPhotoPreviewLabel(),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    if (homePendingPhotoShowsLabels()) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = homePendingPhotoStatusLabel(),
                                color = HomePrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = BodyFontFamily
                            )
                            Text(
                                text = homePendingPhotoPreviewLabel(),
                                color = HomeTextSecondary,
                                fontSize = 11.sp,
                                fontFamily = BodyFontFamily
                            )
                        }
                    }
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
    HomeRecordIcon.Rest -> HomeRestTint
    else -> HomePrimary
}

internal fun homeReviewButtonLabel(): String = "回顾"

internal fun homeTitleLabel(): String = "AI日记本"

internal fun homeAiFabHorizontalAlignment(): HomeAiFabHorizontalAlignment = HomeAiFabHorizontalAlignment.End

internal fun homeAiFabVerticalAlignment(): HomeAiFabVerticalAlignment = HomeAiFabVerticalAlignment.Bottom

internal fun homeAiFabEndPadding() = 20.dp

internal fun homeAiFabSize() = 46.dp

internal fun homeInputBarVerticalPadding() = 6.dp

internal fun homeInputBarMinHeight() = 60.dp

internal fun homeAiFabBottomPadding(hasPendingPhoto: Boolean) =
    homeInputBarMinHeight() + homeInputBarVerticalPadding() + if (hasPendingPhoto) 52.dp else 16.dp

internal fun homeTimelineBottomPadding(hasPendingPhoto: Boolean) =
    if (hasPendingPhoto) 56.dp else 12.dp

internal fun homeTitleBrandIconRes(): Int = R.drawable.ic_home_title_spark

internal fun homeInputBrandIconRes(): Int = R.drawable.ic_home_input_spark

internal fun homeInputPlaceholderText(): String = "推荐豆包语音输入更快"

internal fun homeAttachPhotoButtonLabel(): String = "上传图片"

internal fun homePendingPhotoStatusLabel(): String = "已添加图片"

internal fun homePendingPhotoPreviewLabel(): String = "图片预览"

internal fun homeSendButtonLabel(): String = "发送"

internal fun homeProgressSubtitle(): String = "回到呼吸，心里浮现什么？"

internal fun homeProgressEyebrow(): String = "觉察提示"

internal fun homeDateMonthDay(date: String): String = homeHeadlineDate(date).substringBefore(" ")

internal fun homeDateWeekday(date: String): String = homeHeadlineDate(date).substringAfter(" ")
