package com.example.gsyvideoplayer.compose.host

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerController
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerSurface
import com.shuyu.gsyvideoplayer.compose.native_.rememberGSYPlayerController

class MultiWindowActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MultiWindowScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MultiWindowScreen() {
    val items = remember { DemoSamples.SAMPLE_LIST.take(3) }
    val controllers = items.mapIndexed { idx, item ->
        key(idx) {
            rememberGSYPlayerController(
                url = item.url,
                title = item.title,
                autoPlay = false,
            )
        }
    }
    var activeIndex by remember { mutableIntStateOf(-1) }

    fun activate(idx: Int) {
        if (activeIndex == idx) {
            controllers[idx].togglePlayPause()
            return
        }
        if (activeIndex in controllers.indices) {
            controllers[activeIndex].pause()
        }
        activeIndex = idx
        controllers[idx].play()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Native 多窗口 Demo") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "对齐 Java MultiSampleVideo：同屏 3 个 Native Surface 各自独立。" +
                    "受单 GSYVideoManager 限制，同一时刻仅一个真正在播，激活其它窗口会自动暂停前一个。",
                style = MaterialTheme.typography.bodyMedium,
            )

            items.forEachIndexed { idx, item ->
                MultiVideoCard(
                    title = "窗口 ${idx + 1} · ${item.title}",
                    isActive = activeIndex == idx,
                    controller = controllers[idx],
                    onActivateClick = { activate(idx) },
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    controllers.forEach { it.pause() }
                    activeIndex = -1
                }) { Text("全部暂停") }
            }
        }
    }
}

@Composable
private fun MultiVideoCard(
    title: String,
    isActive: Boolean,
    controller: GSYPlayerController,
    onActivateClick: () -> Unit,
) {
    val snapshot by controller.snapshot
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
            ) {
                GSYPlayerSurface(controller = controller, modifier = Modifier.fillMaxSize())
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = onActivateClick) {
                    Text(if (isActive) "暂停 / 继续" else "激活播放")
                }
                OutlinedButton(onClick = { controller.seekRelative(-5_000) }) { Text("-5s") }
                OutlinedButton(onClick = { controller.seekRelative(5_000) }) { Text("+5s") }
            }
            Text(
                "状态: ${snapshot.state} | ${snapshot.currentPosition} / ${snapshot.duration} ms",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
