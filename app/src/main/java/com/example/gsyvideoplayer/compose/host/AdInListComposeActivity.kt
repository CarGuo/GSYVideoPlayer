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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.compose.native_.GSYDefaultControls
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerEvent
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerSurface
import com.shuyu.gsyvideoplayer.compose.native_.rememberGSYPlayerController

class AdInListComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AdInListScreen()
                }
            }
        }
    }
}

private const val AD_URL = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
private const val FEATURE_URL = "https://www.w3schools.com/html/mov_bbb.mp4"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdInListScreen() {
    // D3 简化版（对齐 Java ListADVideoActivity 的 startAdPlay → onAutoComplete → 切正片）：
    // Compose 端用单个 controller，先 setUp 广告 URL 自动播放；监听 events.AutoComplete，
    // 当广告播完后 setUp 正片 URL。这是 controller setUp 链 + AutoComplete 边沿事件的协同样例。
    val controller = rememberGSYPlayerController()
    val snap by controller.snapshot

    var phase by remember { mutableStateOf("ad") } // "ad" | "feature" | "done"
    var skipAd by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // 起始播广告
        val builder = GSYVideoOptionBuilder()
            .setUrl(AD_URL)
            .setVideoTitle("广告片头 (Compose AD Demo)")
            .setLooping(false)
        controller.setUp(builder, autoPlay = true)
    }

    LaunchedEffect(Unit) {
        controller.events.collect { ev ->
            // 关键事件：广告播完 → 切正片
            if (ev is GSYPlayerEvent.AutoComplete && phase == "ad") {
                phase = "feature"
                val builder = GSYVideoOptionBuilder()
                    .setUrl(FEATURE_URL)
                    .setVideoTitle("正片 (Compose AD Demo)")
                    .setLooping(false)
                controller.setUp(builder, autoPlay = true)
            } else if (ev is GSYPlayerEvent.AutoComplete && phase == "feature") {
                phase = "done"
            }
        }
    }

    LaunchedEffect(skipAd) {
        if (skipAd && phase == "ad") {
            phase = "feature"
            val builder = GSYVideoOptionBuilder()
                .setUrl(FEATURE_URL)
                .setVideoTitle("正片 (Compose AD Demo)")
                .setLooping(false)
            controller.setUp(builder, autoPlay = true)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Native 前贴片广告 Demo") }) }
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
                "对齐 Java ListADVideoActivity 的 ad → feature 切换思路：" +
                    "Compose 端无需额外 GSYADVideoPlayer，直接借助 controller.events.AutoComplete 边沿事件 " +
                    "在广告 onAutoComplete 时调 setUp(featureUrl) 重启播放管道。",
                style = MaterialTheme.typography.bodyMedium,
            )

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
                "阶段：${
                    when (phase) {
                        "ad" -> "▶ 广告播放中"
                        "feature" -> "🎬 正片播放中"
                        else -> "✅ 全部完成"
                    }
                }\n状态：${snap.state} | ${snap.currentPosition} / ${snap.duration} ms",
                style = MaterialTheme.typography.bodySmall,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { skipAd = true }, enabled = phase == "ad") {
                    Text("跳过广告")
                }
                OutlinedButton(onClick = {
                    phase = "ad"
                    skipAd = false
                    val builder = GSYVideoOptionBuilder()
                        .setUrl(AD_URL)
                        .setVideoTitle("广告片头 (Compose AD Demo)")
                        .setLooping(false)
                    controller.setUp(builder, autoPlay = true)
                }) { Text("重置（重新看广告）") }
            }
        }
    }
}
