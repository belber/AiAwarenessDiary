package com.aiawareness.diary.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aiawareness.diary.data.backup.ImportConflictStrategy
import com.aiawareness.diary.ui.components.EditorialSurfaceCard
import com.aiawareness.diary.ui.components.EditorialTopBar
import com.aiawareness.diary.ui.theme.JournalTokens

@Composable
fun DataManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        uri?.let(viewModel::exportBackup)
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let(viewModel::previewImport)
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
                EditorialTopBar(title = "数据管理", onBack = onNavigateBack)
            }

            item {
                EditorialSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "你的数据保存在本地",
                        style = MaterialTheme.typography.titleMedium,
                        color = JournalTokens.Ink
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "除非你主动配置 AI API，否则数据不会离开这台设备。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = JournalTokens.MutedInk
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "导出时会按每天生成可直接阅读的 Markdown 文件，并保留可重新导入的备份结构。即使不再使用这个 App，你也仍然能长期查看和归档自己的日记。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = JournalTokens.MutedInk
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                exportLauncher.launch("ai-awareness-diary-backup-${System.currentTimeMillis()}.zip")
                            },
                            enabled = !uiState.isExportingBackup && !uiState.isImportingBackup,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = JournalTokens.SurfaceHigh,
                                contentColor = JournalTokens.Ink
                            )
                        ) {
                            Text(if (uiState.isExportingBackup) "导出中..." else "导出数据")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = { importLauncher.launch(arrayOf("application/zip")) },
                            enabled = !uiState.isExportingBackup && !uiState.isImportingBackup,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = JournalTokens.Sage,
                                contentColor = Color.White
                            )
                        ) {
                            Text(if (uiState.isImportingBackup) "预检中..." else "导入数据")
                        }
                    }
                }
            }
        }
    }

    uiState.importPreview?.let { preview ->
        AlertDialog(
            onDismissRequest = viewModel::dismissImportPreview,
            title = { Text("导入预检") },
            text = {
                Text(
                    "将导入新增记录 ${preview.recordAdds} 条，新增日记 ${preview.diaryAdds} 篇；" +
                        "检测到冲突记录 ${preview.recordConflicts} 条，冲突日记 ${preview.diaryConflicts} 篇。" +
                        if (preview.hasSettings) "\n包含设置与头像数据。" else ""
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmImport(ImportConflictStrategy.Merge) },
                    enabled = !uiState.isImportingBackup
                ) {
                    Text("保留现有内容")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.confirmImport(ImportConflictStrategy.ReplaceConflicts) },
                        enabled = !uiState.isImportingBackup,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JournalTokens.SurfaceHigh,
                            contentColor = JournalTokens.Ink
                        )
                    ) {
                        Text("覆盖现有内容")
                    }
                    Button(
                        onClick = viewModel::dismissImportPreview,
                        enabled = !uiState.isImportingBackup,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = JournalTokens.MutedInk
                        )
                    ) {
                        Text("取消")
                    }
                }
            },
            containerColor = Color.White
        )
    }
}
