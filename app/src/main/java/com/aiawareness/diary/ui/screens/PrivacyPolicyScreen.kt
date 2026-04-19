package com.aiawareness.diary.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.aiawareness.diary.R
import com.aiawareness.diary.ui.components.EditorialSurfaceCard
import com.aiawareness.diary.ui.components.EditorialTopBar
import com.aiawareness.diary.ui.theme.JournalTokens

fun privacyPolicyFallbackText(): String =
    """
    AI日记本隐私政策

    生效日期：2026-04-19

    1. 基本原则
    本应用不提供开发者自建或运营的中心化内容后端。你的日记文本、图片引用、AI 与对象存储配置、导入导出备份文件等数据默认保存在设备本地，不会默认上传到开发者服务器。

    2. 本地存储
    根据你的使用方式，本应用可能在本地保存日记文本、标题、回顾内容、图片路径、导入导出文件、备份文件，以及 AI 与对象存储相关设置。

    3. 第三方 AI 服务
    当你主动配置并使用 OpenAI-compatible 模型服务时，你选择发送的提示词、日记文本、模型请求内容、上下文内容、AI 生成结果，以及必要的第三方端点与密钥材料，会由客户端直接发送到你配置的第三方模型服务提供商。

    4. 第三方对象存储
    当你主动配置并使用 S3-compatible 对象存储服务时，你选择同步或备份的图片、导出文件、备份文件，以及连接该服务所需的端点、Bucket 与密钥材料，会由客户端直接发送到你配置的第三方对象存储服务。

    5. Aliyun APM 监控
    本应用集成 Aliyun APM，用于崩溃分析、性能监控、网络监控、远程日志与内存监控。你同意隐私政策后，相关监控能力才会启动，以帮助识别运行异常和稳定性问题。

    6. 信息共享与披露
    除你主动配置并使用的 OpenAI-compatible 模型服务、S3-compatible 对象存储服务，以及你同意后启用的 Aliyun APM 所需的技术处理外，开发者不会主动向第三方披露你的数据；法律法规另有要求的除外。

    7. 你的控制权
    你可以选择不配置任何第三方 AI 或对象存储服务，以保持数据仅在本地使用；你也可以自行导出、备份、迁移或删除本地文件。

    8. 政策更新
    如果隐私政策发生实质性变化，应用内展示内容和公开文档会同步更新，以届时展示的版本为准。
    """.trimIndent()

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit
) {
    val hostedUrl = stringResource(R.string.privacy_policy_url).trim()
    var isLoading by remember(hostedUrl) { mutableStateOf(hostedUrl.isNotBlank()) }
    var fallbackReasonResId by remember(hostedUrl) {
        mutableStateOf(
            if (hostedUrl.isBlank()) {
                R.string.privacy_policy_fallback_reason_no_url
            } else {
                null
            }
        )
    }
    var webView by remember { mutableStateOf<WebView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            webView?.destroy()
            webView = null
        }
    }

    Scaffold(
        containerColor = JournalTokens.Paper,
        topBar = {
            EditorialTopBar(
                title = stringResource(R.string.privacy_policy_title),
                onBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(JournalTokens.Paper)
                .padding(paddingValues)
        ) {
            if (fallbackReasonResId != null) {
                PrivacyPolicyFallbackContent(reasonResId = fallbackReasonResId!!)
            } else {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.loadsImagesAutomatically = true
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView,
                                    request: WebResourceRequest
                                ): Boolean {
                                    val scheme = request.url.scheme.orEmpty()
                                    return scheme != "http" && scheme != "https"
                                }

                                override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                                    isLoading = true
                                }

                                override fun onPageFinished(view: WebView, url: String?) {
                                    isLoading = false
                                }

                                override fun onReceivedError(
                                    view: WebView,
                                    request: WebResourceRequest,
                                    error: WebResourceError
                                ) {
                                    if (request.isForMainFrame) {
                                        fallbackReasonResId = R.string.privacy_policy_fallback_reason_load_failed
                                        isLoading = false
                                    }
                                }
                            }
                            loadUrl(hostedUrl)
                            webView = this
                        }
                    },
                    update = { currentWebView ->
                        webView = currentWebView
                    }
                )

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = JournalTokens.Sage)
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacyPolicyFallbackContent(reasonResId: Int) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = JournalTokens.ScreenPadding, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            EditorialSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = reasonResId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = JournalTokens.MutedInk
                )
            }
        }

        item {
            EditorialSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = privacyPolicyFallbackText(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = JournalTokens.Ink
                )
            }
        }
    }
}
