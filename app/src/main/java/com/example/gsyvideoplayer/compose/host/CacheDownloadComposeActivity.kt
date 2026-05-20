package com.example.gsyvideoplayer.compose.host

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.cache.ProxyCacheManager
import com.shuyu.gsyvideoplayer.compose.native_.GSYDefaultControls
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerSurface
import com.shuyu.gsyvideoplayer.compose.native_.rememberGSYPlayerController

class CacheDownloadComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CacheDownloadScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CacheDownloadScreen() {
    val context = LocalContext.current
    val appContext = remember(context) { context.applicationContext }
    val controller = rememberGSYPlayerController()
    val snap by controller.snapshot

    var statusText by remember { mutableStateOf("等待开始") }
    val proxyUrl = remember(appContext) {
        // 用 ProxyCacheManager 套一层代理，命中缓存时第二次播放本地秒开。
        // 与 Java DetailDownloadPlayer 同 API 路径。
        // 注：不能用 derivedStateOf —— newProxy(...) 是带副作用的 IO 调用（会启动 HTTP 代理 server）。
        // derivedStateOf 的 calculation 只能是纯派生函数，不允许有副作用。
        val server = ProxyCacheManager.instance().newProxy(appContext)
        server.getProxyUrl(DemoSamples.SAMPLE_URL)
    }

    LaunchedEffect(proxyUrl) {
        val builder = GSYVideoOptionBuilder()
            .setUrl(proxyUrl)
            .setCacheWithPlay(true)
            .setVideoTitle("Cache + Download Demo")
        controller.setUp(builder, autoPlay = true)
        statusText = "已对接代理：$proxyUrl"
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Native 缓存 / 下载 Demo") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "对齐 Java DetailDownloadPlayer：用 ProxyCacheManager.newProxy + getProxyUrl 套一层缓存代理，" +
                    "下方显示 isCacheReady 状态（来自 R3 P1-4 扩展的 Snapshot 字段）。点击「清缓存」会调用 " +
                    "GSYVideoManager.instance().clearAllDefaultCache(ctx)。",
                style = MaterialTheme.typography.bodyMedium,
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black),
            ) {
                GSYPlayerSurface(controller = controller, modifier = Modifier.fillMaxSize())
                GSYDefaultControls(controller = controller, modifier = Modifier.fillMaxSize())
            }

            Text(
                buildString {
                    append("状态：${snap.state} | ${snap.currentPosition} / ${snap.duration} ms\n")
                    append("缓冲：${snap.bufferPercent}% | 网速：${snap.netSpeedText}\n")
                    append("缓存命中：${if (snap.isCacheReady) "✅ 已命中本地缓存" else "⏳ 流式加载中"}\n")
                    append(statusText)
                },
                style = MaterialTheme.typography.bodySmall,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    // GSYVideoManager 是单例，所有 Compose / Java demo 都共享缓存目录。
                    GSYVideoManager.instance().clearAllDefaultCache(appContext)
                    statusText = "已调用 clearAllDefaultCache(ctx)"
                }) { Text("清缓存") }
                OutlinedButton(onClick = {
                    // 重新 setUp 同 url，演示"清缓存后重新拉流"的反馈循环
                    val builder = GSYVideoOptionBuilder()
                        .setUrl(proxyUrl)
                        .setCacheWithPlay(true)
                        .setVideoTitle("Cache + Download Demo")
                    controller.setUp(builder, autoPlay = true)
                    statusText = "已重新 setUp 触发重新加载"
                }) { Text("重新加载") }
            }
        }
    }
}
