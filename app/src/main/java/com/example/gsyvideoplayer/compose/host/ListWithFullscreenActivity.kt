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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.shuyu.gsyvideoplayer.compose.native_.GSYDefaultControls
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerSurface
import com.shuyu.gsyvideoplayer.compose.native_.rememberGSYPlayerController

class ListWithFullscreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ListWithFullscreenScreen(
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
private fun ListWithFullscreenScreen(
    onRequestOrientation: (Boolean) -> Unit,
) {
    val items = remember { DemoSamples.SAMPLE_LIST }
    val controller = rememberGSYPlayerController()
    var playingIndex by remember { mutableIntStateOf(-1) }
    var fullscreen by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val view = LocalView.current
    LaunchedEffect(fullscreen) {
        val window = (view.context as? android.app.Activity)?.window ?: return@LaunchedEffect
        val controllerCompat = WindowInsetsControllerCompat(window, view)
        if (fullscreen) {
            controllerCompat.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controllerCompat.hide(WindowInsetsCompat.Type.systemBars())
            onRequestOrientation(true)
        } else {
            controllerCompat.show(WindowInsetsCompat.Type.systemBars())
            onRequestOrientation(false)
        }
    }

    BackHandler(enabled = fullscreen) {
        fullscreen = false
    }

    if (fullscreen && playingIndex in items.indices) {
        FullscreenLayer(
            title = items[playingIndex].title,
            controller = controller,
            onExit = { fullscreen = false },
        )
        return
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
                    "对齐 Java DetailListPlayer：列表卡片可直接『进入全屏』，全屏返回回到原列表位（同 Activity，不跳转）。",
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
                        fullscreen = true
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

@Composable
private fun FullscreenLayer(
    title: String,
    controller: com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerController,
    onExit: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        GSYPlayerSurface(controller = controller, modifier = Modifier.fillMaxSize())
        GSYDefaultControls(controller = controller, modifier = Modifier.fillMaxSize())
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            OutlinedButton(onClick = onExit) { Text("退出全屏") }
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }
    }
}
