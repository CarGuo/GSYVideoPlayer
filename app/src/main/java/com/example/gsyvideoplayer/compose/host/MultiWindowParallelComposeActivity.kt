package com.example.gsyvideoplayer.compose.host

import android.os.Bundle
import android.view.View
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.gsyvideoplayer.utils.DemoVideoUrls
import com.example.gsyvideoplayer.video.MultiSampleVideo
import com.example.gsyvideoplayer.video.manager.CustomManager

class MultiWindowParallelComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MultiWindowParallelScreen()
                }
            }
        }
    }
}

private const val PLAY_TAG = "ParallelComposeMulti"

private data class ParallelEntry(
    val title: String,
    val url: String,
    val position: Int,
)

private val PARALLEL_ENTRIES: List<ParallelEntry> = listOf(
    ParallelEntry("窗口 #1 · big_buck_bunny", DemoVideoUrls.MP4_BBB, 0),
    ParallelEntry("窗口 #2 · GSY 默认样片", DemoSamples.SAMPLE_URL, 1),
    ParallelEntry("窗口 #3 · IMG_0382", DemoSamples.SAMPLE_URL_2, 2),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MultiWindowParallelScreen() {
    // D7：演示 CustomManager + Wrapper 模式实现"真并行"。
    //
    // 关键差异：
    // - MultiWindowActivity (P1) 用 Native controller 共享单 GSYVideoManager，互斥（同一时刻只
    //   能播一个）。
    // - 本 demo 用 AndroidView 包装 MultiSampleVideo（继承 StandardGSYVideoPlayer），其
    //   getGSYVideoManager() override 路由到 CustomManager.getCustomManager(getKey())，
    //   每个 (PlayTag + PlayPosition) 组合获得一个独立的 GSYVideoBaseManager 实例 →
    //   真正多源同时播放。
    //
    // 这印证了 Compose 端不必为 CustomManager 单独建模——直接用 Wrapper 模式即可，
    // Native controller 留给单实例场景。
    DisposableEffect(Unit) {
        onDispose {
            // 关闭所有 CustomManager 实例（key = TAG + position + playTag）
            PARALLEL_ENTRIES.forEach { e ->
                CustomManager.releaseAllVideos("MultiSampleVideo${e.position}$PLAY_TAG")
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Wrapper 真并行多窗口 Demo") }) }
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
                "对齐 Java ListMultiNormalAdapter / MultiSampleVideo：每个 player 路由到独立 " +
                    "CustomManager.getCustomManager(getKey())，可同时播放（区别于 Native controller " +
                    "下的 MultiWindow demo——后者必须互斥）。Compose 端用 AndroidView 完整复用 GSY 原生路径。",
                style = MaterialTheme.typography.bodyMedium,
            )

            PARALLEL_ENTRIES.forEach { entry ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(entry.title, style = MaterialTheme.typography.titleSmall)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .background(Color.Black),
                        ) {
                            ParallelVideoView(entry)
                        }
                    }
                }
            }

            Text(
                "提示：点击每个窗口播放按钮启动对应 player。各窗口由独立 CustomManager 驱动，互不影响。",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ParallelVideoView(entry: ParallelEntry) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            MultiSampleVideo(ctx).apply {
                setPlayTag(PLAY_TAG)
                setPlayPosition(entry.position)
                setUpLazy(entry.url, false, null, null, entry.title)
                titleTextView.visibility = View.GONE
                backButton.visibility = View.GONE
                setRotateViewAuto(false)
                setLockLand(false)
                setReleaseWhenLossAudio(false)
                setShowFullAnimation(false)
                setIsTouchWiget(false)
            }
        },
    )
}
