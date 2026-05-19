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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gsyvideoplayer.utils.DemoVideoUrls
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.compose.native_.GSYDefaultControls
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerSurface
import com.shuyu.gsyvideoplayer.compose.native_.rememberGSYPlayerController
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager

class ExoSwitchSourceComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 切换到 EXO2 内核（与 Java DetailExoListPlayer 同条件）
        // 这是 GSY 的核心多内核能力之一：无需切换 Player 实现，UI 层完全复用。
        PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ExoSwitchScreen()
                }
            }
        }
    }
}

private data class ExoSource(
    val title: String,
    val url: String,
)

private val EXO_SOURCES = listOf(
    ExoSource("MP4 · big_buck_bunny", DemoVideoUrls.MP4_BBB),
    ExoSource("HLS · m3u8 测试流", DemoVideoUrls.HLS_MUX),
    ExoSource("MP4 · GSY 默认样片", DemoSamples.SAMPLE_URL),
    ExoSource("MP4 · IMG_0382", DemoSamples.SAMPLE_URL_2),
)

private val SPEED_OPTIONS = floatArrayOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExoSwitchScreen() {
    val controller = rememberGSYPlayerController()
    val snap by controller.snapshot

    var sourceIndex by remember { mutableIntStateOf(0) }
    var speedIndex by remember { mutableIntStateOf(1) }

    LaunchedEffect(sourceIndex) {
        val src = EXO_SOURCES[sourceIndex]
        // 切源走 setUp 重置整个播放管道——EXO 内部会复用 player 实例但重新构建 MediaSource
        val builder = GSYVideoOptionBuilder()
            .setUrl(src.url)
            .setVideoTitle(src.title)
            .setCacheWithPlay(false)
        controller.setUp(builder, autoPlay = true)
    }

    LaunchedEffect(speedIndex) {
        controller.setSpeed(SPEED_OPTIONS[speedIndex])
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Native EXO 多源切换 Demo") }) }
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
                "对齐 Java DetailExoListPlayer：通过 PlayerFactory.setPlayManager(Exo2PlayerManager.class) 切到 EXO 内核，" +
                    "演示 MP4 / HLS 多协议混合源切换 + 5 档倍速。Compose 端实现：每次 setUp 提交新 builder。",
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
                "内核：EXO2 (Exo2PlayerManager) | 当前源：${EXO_SOURCES[sourceIndex].title}\n" +
                    "状态：${snap.state} | ${snap.currentPosition} / ${snap.duration} ms | 倍速：${SPEED_OPTIONS[speedIndex]}x",
                style = MaterialTheme.typography.bodySmall,
            )

            Text("切换源：", style = MaterialTheme.typography.labelLarge)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                EXO_SOURCES.forEachIndexed { idx, s ->
                    if (idx == sourceIndex) {
                        Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("● ${s.title}") }
                    } else {
                        OutlinedButton(
                            onClick = { sourceIndex = idx },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(s.title) }
                    }
                }
            }

            Text("倍速：", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SPEED_OPTIONS.forEachIndexed { idx, sp ->
                    if (idx == speedIndex) {
                        Button(onClick = {}) { Text("●${sp}x") }
                    } else {
                        OutlinedButton(onClick = { speedIndex = idx }) { Text("${sp}x") }
                    }
                }
            }
        }
    }
}
