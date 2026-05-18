package com.example.gsyvideoplayer.compose.host

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerSurface
import com.shuyu.gsyvideoplayer.compose.native_.rememberGSYPlayerController

class AutoPlayListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AutoPlayListScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutoPlayListScreen() {
    val items = remember {
        listOf(
            DemoSamples.SampleItem("自动连播 #1 · GSY Compose Sample", DemoSamples.SAMPLE_URL),
            DemoSamples.SampleItem("自动连播 #2 · GSY Compose Sample", DemoSamples.SAMPLE_URL),
            DemoSamples.SampleItem("自动连播 #3 · GSY Compose Sample", DemoSamples.SAMPLE_URL),
            DemoSamples.SampleItem("自动连播 #4 · GSY Compose Sample", DemoSamples.SAMPLE_URL),
        )
    }

    val controller = rememberGSYPlayerController()
    var playingIndex by remember { mutableIntStateOf(-1) }
    var autoChainEnabled by remember { mutableStateOf(true) }
    var lastEvent by remember { mutableStateOf("尚未开始") }

    val listState = rememberLazyListState()

    DisposableEffect(controller) {
        controller.setOnComplete {
            lastEvent = "#${playingIndex + 1} 播放完成"
            if (!autoChainEnabled) return@setOnComplete
            val next = playingIndex + 1
            if (next in items.indices) {
                val nextItem = items[next]
                playingIndex = next
                controller.setUp(nextItem.url, false, nextItem.title, autoPlay = true)
                lastEvent = "自动切到 #${next + 1}"
            } else {
                lastEvent = "已到末尾，连播停止"
                playingIndex = -1
            }
        }
        controller.setOnError { what, extra ->
            lastEvent = "播放错误 what=$what extra=$extra"
        }
        onDispose {
            controller.setOnComplete(null)
            controller.setOnError(null)
        }
    }

    LaunchedEffect(playingIndex) {
        val idx = playingIndex
        if (idx in items.indices) {
            listState.animateScrollToItem(idx + 1)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Native 自动连播 Demo") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            state = listState,
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column {
                    Text(
                        "对齐 Java ListGSYVideoPlayer.playNext：当前段播放完成后自动 release+attach 下一段并续播。",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        "状态：$lastEvent",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(onClick = {
                            playingIndex = 0
                            controller.setUp(items[0].url, false, items[0].title, autoPlay = true)
                            lastEvent = "从 #1 开始连播"
                        }) { Text("从头连播") }
                        OutlinedButton(onClick = {
                            autoChainEnabled = !autoChainEnabled
                            lastEvent = if (autoChainEnabled) "已开启自动连播" else "已关闭自动连播"
                        }) {
                            Text(if (autoChainEnabled) "关闭连播" else "开启连播")
                        }
                        OutlinedButton(onClick = {
                            controller.pause()
                            playingIndex = -1
                            lastEvent = "已停止"
                        }) { Text("停止") }
                    }
                }
            }
            itemsIndexed(items, key = { idx, _ -> idx }) { index, item ->
                AutoPlayItem(
                    title = item.title,
                    url = item.url,
                    index = index,
                    isPlaying = playingIndex == index,
                    onClick = {
                        if (playingIndex == index) {
                            controller.togglePlayPause()
                        } else {
                            playingIndex = index
                            controller.setUp(item.url, false, item.title, autoPlay = true)
                            lastEvent = "手动切到 #${index + 1}"
                        }
                    },
                    surface = { mod ->
                        GSYPlayerSurface(controller = controller, modifier = mod)
                    },
                )
            }
        }
    }
}

@Composable
private fun AutoPlayItem(
    title: String,
    url: String,
    index: Int,
    isPlaying: Boolean,
    onClick: () -> Unit,
    surface: @Composable (Modifier) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                if (isPlaying) "▶ $title" else title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                url,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .aspectRatio(16f / 9f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                if (isPlaying) {
                    surface(Modifier.fillMaxSize())
                } else {
                    Button(onClick = onClick) { Text("从这一段开始播") }
                }
            }
            if (isPlaying) {
                Button(
                    onClick = onClick,
                    modifier = Modifier.padding(top = 8.dp),
                ) { Text("暂停 / 继续") }
            }
        }
    }
}
