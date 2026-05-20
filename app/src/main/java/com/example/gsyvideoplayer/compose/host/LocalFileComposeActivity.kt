package com.example.gsyvideoplayer.compose.host

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.gsyvideoplayer.R
import com.example.gsyvideoplayer.utils.DemoVideoUrls
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.compose.native_.GSYDefaultControls
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerSurface
import com.shuyu.gsyvideoplayer.compose.native_.rememberGSYPlayerController

class LocalFileComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LocalFileScreen()
                }
            }
        }
    }
}

private data class UrlPreset(val label: String, val url: String, val needsRawPlay: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocalFileScreen() {
    val ctx = LocalContext.current
    val pkg = ctx.packageName
    val presets = remember(pkg) {
        listOf(
            UrlPreset("HTTP 远程", DemoVideoUrls.MP4_BBB, false),
            UrlPreset("本地 raw · test.mp4", "android.resource://$pkg/${R.raw.test}", true),
            UrlPreset("本地 raw · test4.mp4", "android.resource://$pkg/${R.raw.test4}", true),
        )
    }
    val controller = rememberGSYPlayerController()
    val snapshot by controller.snapshot
    var input by remember { mutableStateOf(presets[0].url) }
    var cache by remember { mutableStateOf(false) }
    var lastInfo by remember { mutableStateOf("尚未起播") }

    LaunchedEffect(Unit) {
        GSYVideoManager.instance().enableRawPlay(ctx.applicationContext)
    }

    // controller 释放由 rememberGSYPlayerController 内部 DisposableEffect 托管，无需重复调用。

    fun start(url: String) {
        if (url.isBlank()) return
        val builder = GSYVideoOptionBuilder()
            .setUrl(url)
            .setVideoTitle("LocalFile Demo")
            .setCacheWithPlay(cache)
            .setIsTouchWiget(false)
        controller.setUp(builder, autoPlay = true)
        lastInfo = "已 setUp：$url\ncache=$cache"
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Compose 本地 / 自定义 URL Demo") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "对齐 Java InputUrlDetailActivity：演示 Compose 端用任意 URL 起播——HTTP 远程、本地 raw 资源（android.resource://）、自定义文件路径。打开 cache 时启用 ProxyCache 缓存。",
                style = MaterialTheme.typography.bodyMedium
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                ) {
                    GSYPlayerSurface(
                        controller = controller,
                        modifier = Modifier.fillMaxSize()
                    )
                    GSYDefaultControls(
                        controller = controller,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("视频 URL") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(checked = cache, onCheckedChange = { cache = it })
                Text("启用 ProxyCache 缓存")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { start(input) }) { Text("▶ 起播") }
                OutlinedButton(onClick = { controller.release(); lastInfo = "已 release" }) { Text("Release") }
            }

            Text("预设源：", style = MaterialTheme.typography.bodyLarge)
            presets.forEach { preset ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(preset.label, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                preset.url,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(onClick = {
                            input = preset.url
                        }) { Text("填入") }
                    }
                }
            }

            Text(
                "状态：${snapshot.state} | ${snapshot.currentPosition}/${snapshot.duration} ms · ${snapshot.videoWidth}x${snapshot.videoHeight}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                lastInfo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
