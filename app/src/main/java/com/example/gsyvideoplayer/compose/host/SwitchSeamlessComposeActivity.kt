package com.example.gsyvideoplayer.compose.host

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.compose.native_.GSYDefaultControls
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerSurface
import com.shuyu.gsyvideoplayer.compose.native_.rememberGSYPlayerController

class SwitchSeamlessComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SwitchSeamlessScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwitchSeamlessScreen() {
    // Compose 端 seamless 切换的精髓：同一 GSYPlayerController 在 list / detail 两个 Composable
    // 之间复用——不重 setUp、不释放，借助 GSYPlayerSurface 在不同布局位置 attach 同一 host
    // （这部分由 controller 在 attach 时调用 setSurfaceToPlay 接管），实现"无缝换位"。
    // 与 Java SwitchUtil.savePlayState/clonePlayState 桥接 surface 的语义在 Compose 中被压缩为
    // "同一 controller 跨 Composable 移动"，无需手动 saveState/cloneState。
    val controller = rememberGSYPlayerController()
    val snap by controller.snapshot

    var currentIndex by remember { mutableIntStateOf(-1) }
    var inDetail by remember { mutableStateOf(false) }

    BackHandler(enabled = inDetail) { inDetail = false }

    LaunchedEffect(currentIndex) {
        if (currentIndex >= 0) {
            val entry = DemoSamples.SAMPLE_LIST[currentIndex]
            // 仅在切换 item 时 setUp；点击同一 item 进入 detail 不会重新 setUp，
            // 这正是"无缝衔接"的体现。
            val builder = GSYVideoOptionBuilder()
                .setUrl(entry.url)
                .setVideoTitle(entry.title)
                .setCacheWithPlay(true)
            controller.setUp(builder, autoPlay = true)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Native Seamless Switch Demo") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "对齐 Java SwitchDetailActivity + SwitchUtil 思路：" +
                    "Compose 端通过同一 controller 在 list 缩略区与 detail 全屏区间复用——切换布局不重建播放器、" +
                    "不重新拉流。点击列表项进入 mini 播放，再点击「展开详情」进入 detail；返回保持播放状态。",
                style = MaterialTheme.typography.bodyMedium,
            )

            AnimatedVisibility(
                visible = !inDetail,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(DemoSamples.SAMPLE_LIST) { entry ->
                        val idx = DemoSamples.SAMPLE_LIST.indexOf(entry)
                        val isCurrent = idx == currentIndex
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (idx != currentIndex) currentIndex = idx
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(120.dp, 68.dp)
                                        .background(Color.Black),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (isCurrent) {
                                        // 同一 controller 在列表缩略图位置 attach：seamless 起点
                                        GSYPlayerSurface(
                                            controller = controller,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                    } else {
                                        Text("#${idx + 1}", color = Color.White)
                                    }
                                }
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(entry.title, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        if (isCurrent) "正在播放：${snap.state} | ${snap.currentPosition}ms"
                                        else "点击切换",
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                    if (isCurrent) {
                                        Button(onClick = { inDetail = true }) { Text("展开详情") }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = inDetail,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .background(Color.Black),
                    ) {
                        // 同一 controller 在 detail 大屏位置 attach：seamless 终点
                        // 切换过程不调用 setUp、不释放，状态/进度连续
                        GSYPlayerSurface(
                            controller = controller,
                            modifier = Modifier.fillMaxSize(),
                        )
                        GSYDefaultControls(controller = controller, modifier = Modifier.fillMaxSize())
                    }
                    Text(
                        "Detail 区位置接管同一 controller。可以观察到：进度未回零、" +
                            "也未触发 onStartPrepared，证明无重新拉流。",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { inDetail = false }) { Text("回到列表") }
                    }
                    Box(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
