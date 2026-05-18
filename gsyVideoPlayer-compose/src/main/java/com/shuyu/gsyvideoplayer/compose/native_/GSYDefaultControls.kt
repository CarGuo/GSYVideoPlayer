package com.shuyu.gsyvideoplayer.compose.native_

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.util.Locale

/**
 * 顶层封装：画面 + 默认控制条。
 *
 * 用户也可以拆开使用：
 *   Box {
 *      GSYPlayerSurface(controller, ...)
 *      MyCustomControls(controller, ...)
 *   }
 */
@Composable
fun GSYComposePlayer(
    controller: GSYPlayerController,
    modifier: Modifier = Modifier,
    showDefaultControls: Boolean = true,
) {
    Box(modifier) {
        GSYPlayerSurface(controller, Modifier.matchParentSize())
        if (showDefaultControls) {
            GSYDefaultControls(controller, Modifier.matchParentSize())
        }
    }
}

/**
 * 默认控制条：顶部空、中间播放按钮（暂停/缓冲）、底部进度 + 时长。
 * 完全 Compose 实现，可作为参考；用户可替换为自己的实现。
 */
@Composable
fun GSYDefaultControls(
    controller: GSYPlayerController,
    modifier: Modifier = Modifier,
) {
    val snap by controller.snapshot

    Box(modifier) {
        // 中央按钮 / loading
        Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
            when (snap.state) {
                GSYPlayState.Preparing, GSYPlayState.Buffering -> {
                    CircularProgressIndicator(color = Color.White)
                }
                GSYPlayState.Idle, GSYPlayState.Paused, GSYPlayState.Error -> {
                    IconButton(onClick = { controller.togglePlayPause() }) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                        )
                    }
                }
                GSYPlayState.Completed -> {
                    IconButton(onClick = { controller.togglePlayPause() }) {
                        Icon(
                            imageVector = Icons.Filled.Replay,
                            contentDescription = "Replay",
                            tint = Color.White,
                        )
                    }
                }
                GSYPlayState.Playing -> {
                    // 播放中不显示中央按钮，留白即可
                }
            }
        }

        // 底部控制条
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0x99000000))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(onClick = { controller.togglePlayPause() }) {
                Icon(
                    imageVector = if (snap.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                )
            }
            Text(formatTime(snap.currentPosition), color = Color.White)
            Box(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                val durationMs: Long = snap.duration.coerceAtLeast(0L)
                val progress: Float = if (durationMs > 0L) {
                    // 用 Double 计算，避免 Long 很大时 Float 精度丢失
                    (snap.currentPosition.toDouble() / durationMs.toDouble())
                        .toFloat()
                        .coerceIn(0f, 1f)
                } else 0f
                val bufferProgress: Float = (snap.bufferPercent / 100f).coerceIn(0f, 1f)

                // 缓冲进度条放在 Slider 之下作为背景，避免遮挡 Slider 的拖拽手势
                LinearProgressIndicator(
                    progress = { bufferProgress },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .height(2.dp)
                        .fillMaxWidth(),
                    color = Color(0x66FFFFFF),
                    trackColor = Color.Transparent,
                )
                Slider(
                    value = progress,
                    onValueChange = { v ->
                        if (durationMs > 0L) {
                            // Long * Double 防溢出；先 clamp 比例再换算
                            val ratio = v.coerceIn(0f, 1f).toDouble()
                            val target = (ratio * durationMs).toLong().coerceIn(0L, durationMs)
                            controller.seekTo(target)
                        }
                    },
                    valueRange = 0f..1f,
                )
            }
            Text(formatTime(snap.duration), color = Color.White)
        }
    }
}

private fun formatTime(ms: Long): String {
    if (ms < 0) return "00:00"
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, m, s)
    else String.format(Locale.US, "%02d:%02d", m, s)
}
