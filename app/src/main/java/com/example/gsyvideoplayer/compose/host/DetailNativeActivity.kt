package com.example.gsyvideoplayer.compose.host

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.shuyu.gsyvideoplayer.compose.native_.GSYDefaultControls
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerEvent
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerSurface
import com.shuyu.gsyvideoplayer.compose.native_.rememberGSYPlayerController

class DetailNativeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DetailNativeScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailNativeScreen() {
    val controller = rememberGSYPlayerController(
        url = DemoSamples.SAMPLE_URL,
        title = "Compose Native Detail",
        autoPlay = true,
    )
    val snap by controller.snapshot
    var fullscreen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = remember(context) { context as android.app.Activity }

    // R2 P0-3：全屏完全交给 controller.enterFullscreen / exitFullscreen 由内核接管，
    // Compose 端只通过 events.EnterFull / QuitFull 同步本地 UI 标志位。
    // 这样能拿到底层的 OrientationUtils + WindowDecor 联动 + 克隆播放器接管渲染，
    // 与 Java 版 DetailPlayer.startWindowFullscreen 走同一条路径。
    LaunchedEffect(controller) {
        controller.events.collect { ev ->
            when (ev) {
                GSYPlayerEvent.EnterFull -> fullscreen = true
                GSYPlayerEvent.QuitFull -> fullscreen = false
                else -> {}
            }
        }
    }

    BackHandler(enabled = fullscreen) {
        controller.exitFullscreen(activity)
    }

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
                if (!fullscreen) {
                    GSYDefaultControls(controller, Modifier.fillMaxSize())
                    Button(
                        onClick = { controller.enterFullscreen(activity) },
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    ) {
                        Text("全屏")
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Native Detail 示例",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "上方播放器使用 Compose 原生控件层，下方为 Compose 详情区。点击右上角『全屏』" +
                        "进入由 GSY 内核接管的窗口层全屏（与 Java 版 startWindowFullscreen 同路径），" +
                        "返回键或调用 controller.exitFullscreen() 退出。",
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
