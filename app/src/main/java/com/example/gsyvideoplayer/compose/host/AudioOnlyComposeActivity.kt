package com.example.gsyvideoplayer.compose.host

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gsyvideoplayer.R
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerSurface
import com.shuyu.gsyvideoplayer.compose.native_.rememberGSYPlayerController

class AudioOnlyComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AudioOnlyScreen()
                }
            }
        }
    }
}

private data class AudioTrack(val title: String, val url: String)

private fun audioTracks(pkg: String): List<AudioTrack> = listOf(
    AudioTrack("本地 raw · test3.mp3", "android.resource://$pkg/${R.raw.test3}"),
    AudioTrack("本地 raw · test33.mp3", "android.resource://$pkg/${R.raw.test33}"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AudioOnlyScreen() {
    val ctx = LocalContext.current
    val pkg = ctx.packageName
    val tracks = remember(pkg) { audioTracks(pkg) }
    val controller = rememberGSYPlayerController()
    val snapshot by controller.snapshot
    var current by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        GSYVideoManager.instance().enableRawPlay(ctx.applicationContext)
    }

    // controller 释放由 rememberGSYPlayerController 内部 DisposableEffect 托管，无需重复调用。

    fun playAt(idx: Int) {
        current = idx
        val builder = GSYVideoOptionBuilder()
            .setUrl(tracks[idx].url)
            .setVideoTitle(tracks[idx].title)
            .setCacheWithPlay(false)
            .setLooping(false)
            .setIsTouchWiget(false)
        controller.setUp(builder, autoPlay = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Compose 纯音频播放 Demo") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "对齐 Java AudioDetailPlayer：演示 GSY 在没有 GSYPlayerSurface 的情况下，作为纯音频引擎播放 raw 资源（test3.mp3 / test33.mp3）；Compose 端只接 controller，Surface 以 1dp 隐身节点挂载（host 必须有载体），依然完整起播 + 进度更新。",
                style = MaterialTheme.typography.bodyMedium
            )

            // 隐身 host 节点：GSY 内核必须挂在一个 StandardGSYVideoPlayer View 上，
            // 即使是音频也不例外。这里给 1dp size 让 controller 拿到 host 但不占视觉空间。
            GSYPlayerSurface(
                controller = controller,
                modifier = Modifier.size(1.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(Color(0xFF6650a4), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (snapshot.isPlaying) "♪" else "▶",
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            "当前：${if (current >= 0) tracks[current].title else "无"} | ${snapshot.state}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "进度：${snapshot.currentPosition} / ${snapshot.duration} ms · buffer ${snapshot.bufferPercent}%",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            tracks.forEachIndexed { idx, track ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            track.title,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(onClick = { playAt(idx) }) {
                            Text(if (idx == current && snapshot.isPlaying) "重播" else "播放")
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = {
                    controller.withHost { player ->
                        if (snapshot.isPlaying) {
                            player.onVideoPause()
                        } else {
                            player.onVideoResume()
                        }
                    }
                }) { Text("暂停 / 继续") }

                OutlinedButton(onClick = {
                    controller.seekTo(0L)
                }) { Text("回到开头") }
            }
        }
    }
}
