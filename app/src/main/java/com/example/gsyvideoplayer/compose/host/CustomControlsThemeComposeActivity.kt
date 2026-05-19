package com.example.gsyvideoplayer.compose.host

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerSurface
import com.shuyu.gsyvideoplayer.compose.native_.rememberGSYPlayerController

class CustomControlsThemeComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CustomControlsThemeScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomControlsThemeScreen() {
    val controller = rememberGSYPlayerController()
    val snapshot by controller.snapshot
    var overlayVisible by remember { mutableStateOf(true) }
    var dragging by remember { mutableStateOf(false) }
    var dragValue by remember { mutableFloatStateOf(0f) }
    var theme by remember { mutableStateOf(ControlTheme.Neon) }

    LaunchedEffect(Unit) {
        val builder = GSYVideoOptionBuilder()
            .setUrl(DemoSamples.SAMPLE_URL)
            .setVideoTitle("Custom Controls Demo")
            .setCacheWithPlay(false)
            .setIsTouchWiget(false)
        controller.setUp(builder, autoPlay = true)
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { controller.release() }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Compose 自定义主题 Controls Demo") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "演示完全用 Compose 自绘控件替代 GSYDefaultControls：渐变浮层 + 自定义播放按钮 + Slider 拖拽 seek + 主题切换；点画面切换浮层显隐。",
                style = MaterialTheme.typography.bodyMedium
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black)
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                overlayVisible = !overlayVisible
                            })
                        }
                ) {
                    GSYPlayerSurface(
                        controller = controller,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (overlayVisible) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(theme.bgBrush())
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${theme.label} | ${snapshot.state}",
                                    color = theme.fg,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(theme.fg.copy(alpha = 0.85f))
                                            .pointerInput(Unit) {
                                                detectTapGestures(onTap = {
                                                    controller.withHost { p ->
                                                        if (snapshot.isPlaying) p.onVideoPause()
                                                        else p.onVideoResume()
                                                    }
                                                })
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            if (snapshot.isPlaying) "❚❚" else "▶",
                                            color = theme.bg,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Column {
                                    val maxMs = snapshot.duration.coerceAtLeast(1L).toFloat()
                                    val sliderValue = if (dragging) dragValue
                                    else snapshot.currentPosition.toFloat().coerceIn(0f, maxMs)
                                    Slider(
                                        value = sliderValue,
                                        onValueChange = {
                                            dragging = true
                                            dragValue = it
                                        },
                                        onValueChangeFinished = {
                                            controller.seekTo(dragValue.toLong())
                                            dragging = false
                                        },
                                        valueRange = 0f..maxMs,
                                        colors = SliderDefaults.colors(
                                            thumbColor = theme.fg,
                                            activeTrackColor = theme.fg,
                                            inactiveTrackColor = theme.fg.copy(alpha = 0.3f)
                                        )
                                    )
                                    Text(
                                        "${formatMs(sliderValue.toLong())} / ${formatMs(snapshot.duration)}",
                                        color = theme.fg,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Text("主题：", style = MaterialTheme.typography.bodyLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ControlTheme.entries.forEach { t ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(t.bgBrush())
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = { theme = t })
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            t.label,
                            color = t.fg,
                            fontWeight = if (theme == t) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "进度：${snapshot.currentPosition}/${snapshot.duration} ms · buffer ${snapshot.bufferPercent}%",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private enum class ControlTheme(
    val label: String,
    val fg: Color,
    val bg: Color,
) {
    Neon("Neon", Color(0xFF00E5FF), Color(0xCC0A0A23)),
    Sunset("Sunset", Color(0xFFFFFFFF), Color(0xAAFF6B35)),
    Mono("Mono", Color(0xFFFFFFFF), Color(0xAA000000));

    fun bgBrush(): Brush = Brush.verticalGradient(
        colors = listOf(Color.Transparent, bg, bg)
    )
}

private fun formatMs(ms: Long): String {
    val s = (ms / 1000).coerceAtLeast(0)
    val m = s / 60
    val sec = s % 60
    return "%02d:%02d".format(m, sec)
}
