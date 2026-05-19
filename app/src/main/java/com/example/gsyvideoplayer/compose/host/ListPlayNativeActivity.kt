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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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

/**
 * ΔD4 R6 升级：在原"滚出屏自动暂停"基础上，演示
 *  - 离屏行为切换：pause（保留 surface / 内存常驻）vs setUp 重置（释放 surface + 数据，下次重新拉流）
 *  - setShowPauseCover：暂停时是否保留最后一帧画面
 *  - 卡片未播放时绘制一个 Compose 占位封面（标题 + 渐变色块），等价 setThumbImageView 在 Native 路径上的 Compose 自由替代方案
 */
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
    var releaseOffscreen by remember { mutableStateOf(false) }
    var showPauseCover by remember { mutableStateOf(true) }

    LaunchedEffect(showPauseCover) {
        controller.setShowPauseCover(showPauseCover)
    }

    val items = remember { DemoSamples.SAMPLE_LIST }
    val listState = rememberLazyListState()

    LaunchedEffect(listState, playingIndex, releaseOffscreen) {
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
                        if (releaseOffscreen) {
                            // setUp 重置：等价"离屏 release"语义 —— 释放 surface 与数据源，
                            // 重新滑回时需要再点一次播放重新拉流
                            controller.setUp("", false, "", autoPlay = false)
                        } else {
                            controller.pause()
                        }
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
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("ΔD4 演示开关", style = MaterialTheme.typography.titleSmall)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = releaseOffscreen,
                                onCheckedChange = { releaseOffscreen = it },
                            )
                            Text(
                                "  离屏 setUp 重置（关 = 仅 pause，开 = 释放 surface/数据）",
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = showPauseCover,
                                onCheckedChange = { showPauseCover = it },
                            )
                            Text(
                                "  setShowPauseCover（暂停保留最后一帧）",
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                }
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
                    // Compose 自绘占位封面：等价 setThumbImageView 在 Native 路径的轻量替代方案
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF1F2933)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                title,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                            )
                            Button(onClick = onPlayClick) { Text("点击播放") }
                        }
                    }
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
