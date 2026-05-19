package com.example.gsyvideoplayer.compose.host

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.gsyvideoplayer.utils.DemoVideoUrls
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.compose.native_.GSYDefaultControls
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerSurface
import com.shuyu.gsyvideoplayer.compose.native_.rememberGSYPlayerController

class WebDetailComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WebDetailScreen()
                }
            }
        }
    }
}

private const val WEB_URL = "https://www.baidu.com"
private const val VIDEO_URL = DemoVideoUrls.MP4_BBB

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WebDetailScreen() {
    // D14 思路：上方 Compose host 播放器 + 下方 AndroidView 嵌入 WebView，
    // 演示 Compose 区域内的 GSY native player 与传统 View 体系（WebView）双栈共存：
    // - 顶部 16:9 区域是 Compose GSYPlayerSurface
    // - 中部 WebView 通过 AndroidView 互操作
    // - 整页用 Scaffold + Column 布局，验证 Compose Activity 也能承载图文 + 视频混合页
    val controller = rememberGSYPlayerController()
    val snap by controller.snapshot

    LaunchedEffect(Unit) {
        val builder = GSYVideoOptionBuilder()
            .setUrl(VIDEO_URL)
            .setVideoTitle("WebDetail Compose Demo")
            .setCacheWithPlay(false)
        controller.setUp(builder, autoPlay = true)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Compose 图文混排（视频 + WebView）Demo") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                GSYPlayerSurface(controller = controller, modifier = Modifier.fillMaxSize())
                GSYDefaultControls(controller = controller, modifier = Modifier.fillMaxSize())
            }
            Text(
                "对齐 Java WebDetailActivity：上方播放器 + 下方网页内容；本 Compose 版用 AndroidView 包 WebView，" +
                    "证明 Compose host 可与 View 体系混合布局。当前：${snap.state} | ${snap.currentPosition}/${snap.duration} ms",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(8.dp),
            )
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        webViewClient = WebViewClient()
                        loadUrl(WEB_URL)
                    }
                }
            )
        }
    }
}
