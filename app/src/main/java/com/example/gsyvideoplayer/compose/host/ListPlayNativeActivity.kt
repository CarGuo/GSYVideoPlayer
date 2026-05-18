package com.example.gsyvideoplayer.compose.host

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerSurface
import com.shuyu.gsyvideoplayer.compose.native_.rememberGSYPlayerController
import kotlinx.coroutines.flow.distinctUntilChanged

class ListPlayNativeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ListPlayNativeScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListPlayNativeScreen() {
    val controller = rememberGSYPlayerController()
    var playingIndex by remember { mutableIntStateOf(-1) }

    val items = remember { DemoSamples.SAMPLE_LIST }
    val listState = rememberLazyListState()

    LaunchedEffect(listState, playingIndex) {
        snapshotFlow {
            val first = listState.firstVisibleItemIndex
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: first
            first to last
        }
            .distinctUntilChanged()
            .collect { (first, last) ->
                val idx = playingIndex
                if (idx in 0 until items.size) {
                    if (idx < first || idx > last) {
                        controller.pause()
                        playingIndex = -1
                    }
                }
            }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Native 列表 Demo") }) }
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
                    "对齐 Java ListNormalAdapter：列表内单实例 Native 播放器，滚动出屏自动暂停，点击其他卡片自动切换。",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            itemsIndexed(items, key = { idx, _ -> idx }) { index, item ->
                ListVideoItem(
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
                    surface = { mod ->
                        GSYPlayerSurface(controller = controller, modifier = mod)
                    },
                )
            }
        }
    }
}

@Composable
private fun ListVideoItem(
    title: String,
    url: String,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
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
            if (isPlaying) {
                Button(
                    onClick = onPlayClick,
                    modifier = Modifier.padding(top = 8.dp),
                ) { Text("暂停 / 继续") }
            }
        }
    }
}
