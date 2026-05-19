package com.example.gsyvideoplayer.compose.host

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerEvent
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerSurface
import com.shuyu.gsyvideoplayer.compose.native_.rememberGSYPlayerController

class ListWithFullscreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ListWithFullscreenScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListWithFullscreenScreen() {
    val items = remember { DemoSamples.SAMPLE_LIST }
    val controller = rememberGSYPlayerController()
    var playingIndex by remember { mutableIntStateOf(-1) }
    var fullscreen by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val activity = remember(context) { context as android.app.Activity }

    // R2 P0-3：列表+全屏路径切换到 controller.enterFullscreen / exitFullscreen 由内核接管。
    // 全屏时内核会反射克隆出第二个 host 接管渲染，并自动旋转/隐藏系统栏；
    // Compose 端只需要订阅 events.EnterFull / QuitFull 同步本地 fullscreen 标志位即可。
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
        topBar = { TopAppBar(title = { Text("Native 列表 + 内层全屏 Demo") }) }
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
                Text(
                    "对齐 Java DetailListPlayer：列表卡片可直接『进入全屏』，由 GSY 内核接管旋转/系统栏隐藏，" +
                        "返回键或调用 controller.exitFullscreen() 即可回到列表（同 Activity，不跳转）。",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            itemsIndexed(items, key = { idx, _ -> idx }) { index, item ->
                ListFsItem(
                    title = item.title,
                    url = item.url,
                    isPlaying = playingIndex == index,
                    onPlayClick = {
                        if (playingIndex == index) {
                            controller.togglePlayPause()
                        } else {
                            controller.setUp(item.url, false, item.title, autoPlay = true)
                            playingIndex = index
                        }
                    },
                    onFullscreenClick = {
                        if (playingIndex != index) {
                            controller.setUp(item.url, false, item.title, autoPlay = true)
                            playingIndex = index
                        }
                        controller.enterFullscreen(activity)
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
private fun ListFsItem(
    title: String,
    url: String,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onFullscreenClick: () -> Unit,
    surface: @Composable (Modifier) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
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
                    Button(onClick = onPlayClick) { Text("点击播放") }
                }
            }
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (isPlaying) {
                    Button(onClick = onPlayClick) { Text("暂停 / 继续") }
                }
                OutlinedButton(onClick = onFullscreenClick) { Text("进入全屏") }
            }
        }
    }
}
