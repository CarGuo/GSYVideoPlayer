package com.example.gsyvideoplayer.compose.host

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayState
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerSnapshot
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerSurface
import com.shuyu.gsyvideoplayer.compose.native_.rememberGSYPlayerController
import java.util.Locale
import kotlin.math.abs

class FullFeatureNativeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FullFeatureNativeScreen()
                }
            }
        }
    }
}

private val SPEEDS = listOf(0.5f, 1.0f, 1.25f, 1.5f, 2.0f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullFeatureNativeScreen() {
    val controller = rememberGSYPlayerController(
        url = DemoSamples.SAMPLE_URL,
        title = "Compose Native Full",
        autoPlay = true,
    )
    val snap by controller.snapshot

    var hasError by remember { mutableStateOf(false) }
    LaunchedEffect(controller) {
        controller.setOnError { _, _ -> hasError = true }
        controller.setOnPrepared { hasError = false }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Native 完整控件 Demo") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
            ) {
                GSYPlayerSurface(controller, Modifier.fillMaxSize())

                if (snap.isLocked) {
                    IconButton(
                        onClick = { controller.setLocked(false) },
                        modifier = Modifier.align(Alignment.CenterEnd).padding(8.dp),
                    ) {
                        Icon(Icons.Filled.Lock, "Locked", tint = Color.White)
                    }
                } else {
                    OverlayControls(
                        snap = snap,
                        hasError = hasError,
                        onTogglePlay = { controller.togglePlayPause() },
                        onSeekTo = { controller.seekTo(it) },
                        onSeekBack = { controller.seekRelative(-15_000L) },
                        onSeekForward = { controller.seekRelative(15_000L) },
                        onRetry = {
                            hasError = false
                            controller.retry()
                        },
                        onLock = { controller.setLocked(true) },
                    )
                }
            }

            Text(
                "状态: ${snap.state} | 位置: ${formatTime(snap.currentPosition)} / ${formatTime(snap.duration)} | 倍速: ${snap.speed}x",
                style = MaterialTheme.typography.bodyMedium,
            )
            LinearProgressIndicator(
                progress = { (snap.bufferPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(4.dp),
            )

            Text("倍速", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SPEEDS.forEach { s ->
                    val selected = abs(snap.speed - s) < 0.001f
                    AssistChip(
                        onClick = { controller.setSpeed(s) },
                        label = { Text("${s}x") },
                        colors = if (selected) {
                            AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                            )
                        } else AssistChipDefaults.assistChipColors(),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "操作说明：中央按钮播放/暂停；左右两侧 ±15s；右上角小锁可锁定手势；播放出错时会出现 Retry。",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun BoxScope.OverlayControls(
    snap: GSYPlayerSnapshot,
    hasError: Boolean,
    onTogglePlay: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onRetry: () -> Unit,
    onLock: () -> Unit,
) {
    Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
        when {
            hasError || snap.state == GSYPlayState.Error -> {
                IconButton(onClick = onRetry) {
                    Icon(Icons.Filled.Refresh, "Retry", tint = Color.White)
                }
            }
            snap.state == GSYPlayState.Preparing || snap.state == GSYPlayState.Buffering -> {
                CircularProgressIndicator(color = Color.White)
            }
            else -> {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(onClick = onSeekBack) { Text("-15s") }
                    IconButton(onClick = onTogglePlay) {
                        Icon(
                            imageVector = if (snap.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                        )
                    }
                    Button(onClick = onSeekForward) { Text("+15s") }
                }
            }
        }
    }
    Button(
        onClick = onLock,
        modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
    ) {
        Text("锁定")
    }
    Row(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .background(Color(0x99000000))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(formatTime(snap.currentPosition), color = Color.White)
        val durationMs = snap.duration.coerceAtLeast(0L)
        val progress = if (durationMs > 0L) {
            (snap.currentPosition.toDouble() / durationMs.toDouble()).toFloat().coerceIn(0f, 1f)
        } else 0f
        Slider(
            value = progress,
            onValueChange = { v ->
                if (durationMs > 0L) {
                    val target = (v.coerceIn(0f, 1f).toDouble() * durationMs).toLong()
                    onSeekTo(target.coerceIn(0L, durationMs))
                }
            },
            valueRange = 0f..1f,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
        )
        Text(formatTime(snap.duration), color = Color.White)
    }
}

private fun formatTime(ms: Long): String {
    if (ms <= 0L) return "00:00"
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, m, s)
    else String.format(Locale.US, "%02d:%02d", m, s)
}
