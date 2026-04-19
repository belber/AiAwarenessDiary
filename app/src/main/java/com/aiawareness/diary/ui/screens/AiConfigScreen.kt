package com.aiawareness.diary.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aiawareness.diary.ui.components.EditorialSurfaceCard
import com.aiawareness.diary.ui.components.EditorialTopBar
import com.aiawareness.diary.ui.theme.HeadlineFontFamily
import com.aiawareness.diary.ui.theme.JournalTokens

@Composable
fun AiConfigScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAiGuide: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var apiEndpoint by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var modelName by remember { mutableStateOf("") }
    var hour by remember { mutableIntStateOf(22) }
    var minute by remember { mutableIntStateOf(0) }
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(
        uiState.settings.apiEndpoint,
        uiState.settings.apiKey,
        uiState.settings.modelName,
        uiState.settings.diaryGenerationHour,
        uiState.settings.diaryGenerationMinute
    ) {
        apiEndpoint = uiState.settings.apiEndpoint
        apiKey = uiState.settings.apiKey
        modelName = uiState.settings.modelName
        hour = uiState.settings.diaryGenerationHour
        minute = uiState.settings.diaryGenerationMinute
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

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
                EditorialTopBar(title = "AI 配置", onBack = onNavigateBack)
            }

            item {
                EditorialSurfaceCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToAiGuide)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = JournalTokens.SageContainer.copy(alpha = 0.34f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (aiConfigGuideEntryIconName() == "help") {
                                Box(
                                    modifier = Modifier
                                        .background(JournalTokens.Sage.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.HelpOutline,
                                        contentDescription = null,
                                        tint = JournalTokens.Sage
                                    )
                                }
                            }
                            Text(
                                text = aiConfigGuideEntryLabel(),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleMedium,
                                color = JournalTokens.Ink
                            )
                            if (aiConfigGuideEntryShowsTrailingArrow()) {
                                Icon(
                                    imageVector = Icons.Outlined.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = JournalTokens.MutedInk
                                )
                            }
                        }
                    }
                }
            }

            item {
                EditorialSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = apiEndpoint,
                        onValueChange = { apiEndpoint = it },
                        label = { Text("API URL") },
                        placeholder = { Text("例如: https://api.openai.com/") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API Key") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = modelName,
                        onValueChange = { modelName = it },
                        label = { Text("模型名称") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "AI日记每日生成时间",
                        style = MaterialTheme.typography.titleMedium,
                        color = JournalTokens.Ink
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTimePicker = true },
                        shape = RoundedCornerShape(12.dp),
                        color = JournalTokens.SageContainer.copy(alpha = 0.34f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "%02d:%02d".format(hour, minute),
                                style = MaterialTheme.typography.headlineSmall,
                                color = JournalTokens.Ink,
                                fontFamily = HeadlineFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formatGenerationTimeSummary(hour, minute),
                                style = MaterialTheme.typography.bodyMedium,
                                color = JournalTokens.MutedInk,
                                lineHeight = 20.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.testAiConnection(
                                    endpoint = apiEndpoint,
                                    apiKey = apiKey,
                                    modelName = modelName
                                )
                            },
                            enabled = apiEndpoint.isNotBlank() &&
                                apiKey.isNotBlank() &&
                                modelName.isNotBlank() &&
                                !uiState.isTestingConnection &&
                                !uiState.isSaving,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = JournalTokens.SurfaceHigh,
                                contentColor = JournalTokens.Ink
                            )
                        ) {
                            Text(if (uiState.isTestingConnection) "测试中..." else "测试连接")
                        }
                        Button(
                            onClick = {
                                viewModel.saveAiConfigAndGenerationTime(
                                    apiEndpoint,
                                    apiKey,
                                    modelName,
                                    hour,
                                    minute
                                )
                            },
                            enabled = apiEndpoint.isNotBlank() &&
                                apiKey.isNotBlank() &&
                                modelName.isNotBlank() &&
                                !uiState.isSaving &&
                                !uiState.isTestingConnection,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = JournalTokens.Sage,
                                contentColor = Color.White
                            )
                        ) {
                            Text(if (uiState.isSaving) "保存中..." else "保存")
                        }
                    }
                }
            }
        }
    }

    if (showTimePicker) {
        AiGenerationTimePickerDialog(
            hour = hour,
            minute = minute,
            onHourChange = { hour = it },
            onMinuteChange = { minute = it },
            onSelectPreset = { presetHour, presetMinute ->
                hour = presetHour
                minute = presetMinute
            },
            onDismiss = { showTimePicker = false },
            onConfirm = { showTimePicker = false }
        )
    }
}

@Composable
private fun AiGenerationTimePickerDialog(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onSelectPreset: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val presets = listOf(0 to 0, 6 to 0, 12 to 0, 22 to 0)
    var hourInput by remember(hour) { mutableStateOf("%02d".format(hour)) }
    var minuteInput by remember(minute) { mutableStateOf("%02d".format(minute)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val parsedHour = hourInput.toIntOrNull()?.coerceIn(0, 23) ?: hour
                    val parsedMinute = minuteInput.toIntOrNull()?.coerceIn(0, 59) ?: minute
                    onHourChange(parsedHour)
                    onMinuteChange(parsedMinute)
                    onConfirm()
                }
            ) {
                Text("完成", color = JournalTokens.Sage)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = JournalTokens.MutedInk)
            }
        },
        title = {
            Text(
                text = "选择生成时间",
                color = JournalTokens.Ink
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AiTimeInput(
                        label = "小时",
                        value = hourInput,
                        onValueChange = { value -> hourInput = value.filter { it.isDigit() }.take(2) },
                        modifier = Modifier.weight(1f)
                    )
                    AiTimeInput(
                        label = "分钟",
                        value = minuteInput,
                        onValueChange = { value -> minuteInput = value.filter { it.isDigit() }.take(2) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "常用时刻",
                        style = MaterialTheme.typography.labelMedium,
                        color = JournalTokens.MutedInk
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        presets.take(2).forEach { (presetHour, presetMinute) ->
                            Box(modifier = Modifier.weight(1f)) {
                                AiTimePresetChip(
                                    text = "%02d:%02d".format(presetHour, presetMinute),
                                    selected = hourInput == "%02d".format(presetHour) &&
                                        minuteInput == "%02d".format(presetMinute),
                                    onClick = {
                                        hourInput = "%02d".format(presetHour)
                                        minuteInput = "%02d".format(presetMinute)
                                        onSelectPreset(presetHour, presetMinute)
                                    }
                                )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        presets.drop(2).forEach { (presetHour, presetMinute) ->
                            Box(modifier = Modifier.weight(1f)) {
                                AiTimePresetChip(
                                    text = "%02d:%02d".format(presetHour, presetMinute),
                                    selected = hourInput == "%02d".format(presetHour) &&
                                        minuteInput == "%02d".format(presetMinute),
                                    onClick = {
                                        hourInput = "%02d".format(presetHour)
                                        minuteInput = "%02d".format(presetMinute)
                                        onSelectPreset(presetHour, presetMinute)
                                    }
                                )
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
private fun AiTimePresetChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
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
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            color = if (selected) JournalTokens.Sage else JournalTokens.Ink,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AiTimeInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = JournalTokens.SurfaceLow
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = JournalTokens.MutedInk
            )
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                modifier = Modifier.width(88.dp),
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    color = JournalTokens.Ink,
                    fontFamily = HeadlineFontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )
        }
    }
}
