package com.example.gsyvideoplayer.compose.host

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.compose.wrapper.GSYVideoPlayerView
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer

class BasicWrapperActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BasicWrapperScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BasicWrapperScreen() {
    var url by remember { mutableStateOf(DemoSamples.SAMPLE_URL) }
    var title by remember { mutableStateOf(DemoSamples.SAMPLE_TITLE) }
    var cacheWithPlay by remember { mutableStateOf(false) }
    var rotateAuto by remember { mutableStateOf(false) }
    var playToken by remember { mutableStateOf(0) }
    var playerRef by remember { mutableStateOf<StandardGSYVideoPlayer?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Wrapper 基础 Demo") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Wrapper 模式直接复用 StandardGSYVideoPlayer，全部内置 UI、手势、全屏、缓存、字幕等能力都可用。",
                style = MaterialTheme.typography.bodyMedium,
            )

            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                key(playToken) {
                    GSYVideoPlayerView(
                        modifier = Modifier.fillMaxSize(),
                        setUp = { player ->
                            GSYVideoOptionBuilder()
                                .setUrl(url)
                                .setCacheWithPlay(cacheWithPlay)
                                .setVideoTitle(title)
                                .setIsTouchWiget(true)
                                .setRotateViewAuto(rotateAuto)
                                .setLockLand(rotateAuto)
                                .setShowFullAnimation(false)
                                .setNeedLockFull(true)
                                .build(player)
                        },
                        onPlayerCreated = { playerRef = it },
                    )
                }
            }

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("视频 URL") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("视频标题") },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = cacheWithPlay, onCheckedChange = { cacheWithPlay = it })
                Text("  边播边存", modifier = Modifier.padding(start = 8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = rotateAuto, onCheckedChange = { rotateAuto = it })
                Text("  自动旋转横屏全屏", modifier = Modifier.padding(start = 8.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { playerRef?.startPlayLogic() }) { Text("开始播放") }
                Button(onClick = { playerRef?.onVideoPause() }) { Text("暂停") }
                Button(onClick = { playerRef?.onVideoResume() }) { Text("恢复") }
            }

            Button(onClick = { playToken += 1 }) { Text("应用上方 URL/标题（重新初始化）") }
        }
    }
}
