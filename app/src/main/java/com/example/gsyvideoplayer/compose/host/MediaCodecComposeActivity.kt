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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.compose.native_.GSYDefaultControls
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerSurface
import com.shuyu.gsyvideoplayer.compose.native_.rememberGSYPlayerController
import com.shuyu.gsyvideoplayer.utils.GSYVideoType

class MediaCodecComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MediaCodecScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediaCodecScreen() {
    val controller = rememberGSYPlayerController()
    val snapshot by controller.snapshot
    val originalHw = remember { GSYVideoType.isMediaCodec() }
    var hwOn by remember { mutableStateOf(originalHw) }
    var lastEnabledAt by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        onDispose {
            // 还原全局硬解开关，避免污染后续页面（GSYVideoType 是 process 级状态）。
            runCatching {
                if (originalHw) {
                    GSYVideoType.enableMediaCodec()
                    GSYVideoType.enableMediaCodecTexture()
                } else {
                    GSYVideoType.disableMediaCodec()
                    GSYVideoType.disableMediaCodecTexture()
                }
            }
            // controller 释放由 rememberGSYPlayerController 内部 DisposableEffect 托管。
        }
    }

    fun applyHw(enable: Boolean) {
        hwOn = enable
        if (enable) {
            GSYVideoType.enableMediaCodec()
            GSYVideoType.enableMediaCodecTexture()
            lastEnabledAt = "已开启 MediaCodec + Texture（下次 setUp 生效）"
        } else {
            GSYVideoType.disableMediaCodec()
            GSYVideoType.disableMediaCodecTexture()
            lastEnabledAt = "已关闭硬解（下次 setUp 生效）"
        }
    }

    fun startPlay() {
        val builder = GSYVideoOptionBuilder()
            .setUrl(DemoSamples.SAMPLE_URL)
            .setVideoTitle("MediaCodec Demo")
            .setCacheWithPlay(false)
            .setIsTouchWiget(false)
        controller.setUp(builder, autoPlay = true)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Compose 硬解（MediaCodec）切换 Demo") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "对齐 Java RecyclerView3Activity 的核心 API：通过 GSYVideoType.enableMediaCodec() / disableMediaCodec() 全局切换硬解；本 Demo 提供 Switch 实时切换并重新 setUp，验证 Compose host 同样适配硬解流水线。",
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

            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("硬解（MediaCodec）", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            if (hwOn) "状态：✅ 已启用（GSYVideoType.isMediaCodec() = true）"
                            else "状态：⏸ 已关闭（默认 IJK 软解）",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(checked = hwOn, onCheckedChange = { applyHw(it) })
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { startPlay() }) { Text("▶ 用当前模式起播") }
                OutlinedButton(onClick = {
                    applyHw(!hwOn)
                    controller.release()
                    startPlay()
                }) { Text("切换 + Release + 重 setUp") }
            }

            Text(
                "状态：${snapshot.state} | ${snapshot.currentPosition}/${snapshot.duration} ms · ${snapshot.videoWidth}x${snapshot.videoHeight}",
                style = MaterialTheme.typography.bodySmall
            )
            if (lastEnabledAt.isNotEmpty()) {
                Text(
                    lastEnabledAt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
