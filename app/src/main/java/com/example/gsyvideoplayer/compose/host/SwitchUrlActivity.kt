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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerSurface
import com.shuyu.gsyvideoplayer.compose.native_.rememberGSYPlayerController

class SwitchUrlActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SwitchUrlScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwitchUrlScreen() {
    val items = remember { DemoSamples.SAMPLE_LIST }
    val controller = rememberGSYPlayerController()
    val snapshot by controller.snapshot

    var currentIndex by remember { mutableIntStateOf(0) }
    var inputUrl by remember { mutableStateOf("") }

    val rememberedPositions = remember { HashMap<Int, Long>() }

    LaunchedEffect(currentIndex) {
        val item = items[currentIndex]
        controller.setUp(item.url, false, item.title, autoPlay = true)
    }

    fun switchTo(newIndex: Int) {
        if (newIndex == currentIndex) return
        rememberedPositions[currentIndex] = snapshot.currentPosition
        controller.pause()
        currentIndex = newIndex.coerceIn(0, items.lastIndex)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Native 切换 URL Demo") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "对齐 Java SwitchVideo：同一 player 实例切换不同 URL，记忆每段上一次播放位置。",
                style = MaterialTheme.typography.bodyMedium,
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
            ) {
                GSYPlayerSurface(controller = controller, modifier = Modifier.fillMaxSize())
            }

            Text(
                "当前：${items[currentIndex].title}\n" +
                    "状态：${snapshot.state} | ${snapshot.currentPosition} / ${snapshot.duration} ms",
                style = MaterialTheme.typography.bodySmall,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { switchTo(currentIndex - 1) },
                    enabled = currentIndex > 0,
                ) { Text("上一段") }
                Button(
                    onClick = { switchTo(currentIndex + 1) },
                    enabled = currentIndex < items.lastIndex,
                ) { Text("下一段") }
                OutlinedButton(onClick = {
                    val pos = rememberedPositions[currentIndex] ?: 0L
                    if (pos > 0L) controller.seekTo(pos)
                }) { Text("回到记忆点") }
            }

            Text("快速切换：", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEachIndexed { idx, item ->
                    if (idx == currentIndex) {
                        Button(onClick = {}) { Text("#${idx + 1}") }
                    } else {
                        OutlinedButton(onClick = { switchTo(idx) }) { Text("#${idx + 1}") }
                    }
                }
            }

            OutlinedTextField(
                value = inputUrl,
                onValueChange = { inputUrl = it },
                label = { Text("自定义 URL") },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = {
                    val u = inputUrl.trim()
                    if (u.isNotEmpty()) {
                        rememberedPositions[currentIndex] = snapshot.currentPosition
                        controller.setUp(u, false, "Custom URL", autoPlay = true)
                    }
                },
                enabled = inputUrl.isNotBlank(),
            ) { Text("跳转到自定义 URL") }
        }
    }
}
