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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.aiawareness.diary.ui.components.EditorialSurfaceCard
import com.aiawareness.diary.ui.components.EditorialTopBar
import com.aiawareness.diary.ui.theme.JournalTokens

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AiGuideWebViewScreen(
    onNavigateBack: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var reloadToken by remember { mutableIntStateOf(0) }
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
                title = aiConfigGuideScreenTitle(),
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
            if (hasError) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = JournalTokens.ScreenPadding, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        EditorialSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = aiConfigGuideLoadErrorMessage(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = JournalTokens.Ink
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    hasError = false
                                    isLoading = true
                                    reloadToken += 1
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(aiConfigGuideRetryLabel(), color = Color.White)
                            }
                        }
                    }
                }
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
                                    return !(scheme == "http" || scheme == "https")
                                }

                                override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                                    isLoading = true
                                    hasError = false
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
                                        hasError = true
                                        isLoading = false
                                    }
                                }
                            }
                            loadUrl(aiConfigGuideUrl())
                            webView = this
                        }
                    },
                    update = { currentWebView ->
                        if (currentWebView.tag != reloadToken) {
                            currentWebView.tag = reloadToken
                            currentWebView.loadUrl(aiConfigGuideUrl())
                        }
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
