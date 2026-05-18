package com.example.gsyvideoplayer.compose.host

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shuyu.gsyvideoplayer.compose.native_.GSYDefaultControls
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerSurface
import com.shuyu.gsyvideoplayer.compose.native_.rememberGSYPlayerController

class DetailNativeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DetailNativeScreen(
                        onRequestOrientation = { landscape ->
                            requestedOrientation = if (landscape) {
                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            } else {
                                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailNativeScreen(onRequestOrientation: (Boolean) -> Unit) {
    val controller = rememberGSYPlayerController(
        url = DemoSamples.SAMPLE_URL,
        title = "Compose Native Detail",
        autoPlay = true,
    )
    val snap by controller.snapshot
    var fullscreen by remember { mutableStateOf(false) }

    BackHandler(enabled = fullscreen) {
        fullscreen = false
        onRequestOrientation(false)
    }

    if (fullscreen) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            GSYPlayerSurface(controller, Modifier.fillMaxSize())
            if (!snap.isLocked) {
                GSYDefaultControls(controller, Modifier.fillMaxSize())
            }
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = { controller.setLocked(!snap.isLocked) }) {
                    Text(if (snap.isLocked) "解锁" else "锁定")
                }
                Button(onClick = {
                    fullscreen = false
                    onRequestOrientation(false)
                }) {
                    Text("退出全屏")
                }
            }
        }
    } else {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Native 详情 Demo") }) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black),
                ) {
                    GSYPlayerSurface(controller, Modifier.fillMaxSize())
                    GSYDefaultControls(controller, Modifier.fillMaxSize())
                    Button(
                        onClick = {
                            fullscreen = true
                            onRequestOrientation(true)
                        },
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    ) {
                        Text("全屏")
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Native Detail 示例",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "上方播放器使用 Compose 原生控件层，下方为 Compose 详情区。点击右上角『全屏』进入旋转全屏模式，全屏内可锁定/解锁手势。",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(16.dp))
                    repeat(8) { index ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("段落 ${index + 1}", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "这是 Compose 详情区中的一段说明文字，演示在视频与 Compose UI 同屏滚动时的体验。播放器位置: ${snap.currentPosition} ms",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.fillMaxHeight().height(32.dp))
                }
            }
        }
    }
}
