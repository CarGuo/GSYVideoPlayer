package com.example.gsyvideoplayer.compose.host

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.shuyu.gsyvideoplayer.compose.native_.GSYDefaultControls
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerSurface
import com.shuyu.gsyvideoplayer.compose.native_.rememberGSYPlayerController

class DanmakuComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DanmakuScreen()
                }
            }
        }
    }
}

private data class DanmakuItem(
    val text: String,
    val timeMs: Long,
    val track: Int,
    val color: Color,
)

private val MOCK_DANMAKUS: List<DanmakuItem> = listOf(
    DanmakuItem("Compose 弹幕来一发！", 1000, 0, Color.White),
    DanmakuItem("自绘 Canvas 同步进度", 2500, 1, Color(0xFFFFE082)),
    DanmakuItem("无需 BiliDanmukuParser", 4000, 2, Color(0xFF80D8FF)),
    DanmakuItem("animateFloat 不香吗", 5500, 0, Color(0xFFB9F6CA)),
    DanmakuItem("可与 snapshot.currentPosition 联动", 7000, 1, Color(0xFFFF8A80)),
    DanmakuItem("纯 Compose 弹幕层", 8500, 2, Color(0xFFEA80FC)),
    DanmakuItem("控件层依然 Compose 自绘", 10000, 0, Color.White),
    DanmakuItem("seek 也能保持同步", 12000, 1, Color(0xFFFFE082)),
    DanmakuItem("性能比 SurfaceView 好", 14000, 2, Color(0xFF80D8FF)),
    DanmakuItem("拒绝 master.flame 依赖", 16000, 0, Color(0xFFB9F6CA)),
    DanmakuItem("Native 模式 + Compose 叠加", 18000, 1, Color(0xFFFF8A80)),
    DanmakuItem("withHost 留作大招", 20000, 2, Color(0xFFEA80FC)),
)

private const val DANMAKU_DURATION_MS = 6000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DanmakuScreen() {
    // D5：Compose 端自绘弹幕。不依赖 master.flame.danmaku（B 站 SDK），
    // 用 Canvas + textMeasurer 把弹幕项按 (currentPositionMs - itemTimeMs) 线性映射到 X 坐标，
    // 与 snapshot.currentPosition 自动同步——seek 时弹幕跟着回滚或前进，是 Java SDK 路径下
    // 比较吃力的能力。
    val controller = rememberGSYPlayerController(
        url = DemoSamples.SAMPLE_URL,
        title = "Compose Danmaku Demo",
        autoPlay = true,
    )
    val snap by controller.snapshot

    var enabled by remember { mutableStateOf(true) }
    var speedFactor by remember { mutableIntStateOf(1) } // 1 / 2

    Scaffold(
        topBar = { TopAppBar(title = { Text("Native 自绘弹幕 Demo") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "对齐 Java DanmakuVideoPlayer 思路，但 Compose 化：用 snapshot.currentPosition + Canvas " +
                    "自绘弹幕轨道。每条弹幕从 timeMs 开始持续 ${DANMAKU_DURATION_MS}ms 横向滑过；seek 时弹幕自动 " +
                    "回滚/前进，无需手动 BaseDanmakuParser。",
                style = MaterialTheme.typography.bodyMedium,
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black),
            ) {
                GSYPlayerSurface(controller = controller, modifier = Modifier.fillMaxSize())
                if (enabled) {
                    DanmakuOverlay(
                        currentMs = snap.currentPosition,
                        modifier = Modifier.fillMaxSize(),
                        speedFactor = speedFactor,
                    )
                }
                GSYDefaultControls(controller = controller, modifier = Modifier.fillMaxSize())
            }

            Text(
                "状态：${snap.state} | ${snap.currentPosition} / ${snap.duration} ms\n" +
                    "弹幕开关：${if (enabled) "ON" else "OFF"} | 弹幕速度：${speedFactor}x",
                style = MaterialTheme.typography.bodySmall,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { enabled = !enabled }) {
                    Text(if (enabled) "关闭弹幕" else "开启弹幕")
                }
                OutlinedButton(onClick = {
                    speedFactor = if (speedFactor == 1) 2 else 1
                }) { Text("弹幕速度 x$speedFactor") }
                OutlinedButton(onClick = {
                    controller.seekTo(0)
                }) { Text("回到 0s") }
                OutlinedButton(onClick = {
                    controller.seekTo(10_000L)
                }) { Text("跳到 10s") }
            }
        }
    }
}

@Composable
private fun DanmakuOverlay(
    currentMs: Long,
    modifier: Modifier = Modifier,
    speedFactor: Int = 1,
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val textStyle = remember { TextStyle(fontSize = 14.sp, color = Color.White) }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val trackH = h / 4f
        val durationMs = (DANMAKU_DURATION_MS / speedFactor).coerceAtLeast(1500L)
        MOCK_DANMAKUS.forEach { dm ->
            val elapsed = currentMs - dm.timeMs
            if (elapsed < 0 || elapsed > durationMs) return@forEach
            // 0 → 1 进度，对应 X 从 width 滑到 -textWidth
            val progress = elapsed.toFloat() / durationMs.toFloat()
            val measured = textMeasurer.measure(AnnotatedString(dm.text), textStyle)
            val textW = measured.size.width.toFloat()
            val x = w - progress * (w + textW)
            val y = trackH * (dm.track + 1) - measured.size.height / 2f
            // 阴影：先画黑色描边
            drawText(
                textMeasurer = textMeasurer,
                text = dm.text,
                topLeft = Offset(x + 2f, y + 2f),
                style = textStyle.copy(color = Color.Black),
            )
            drawText(
                textMeasurer = textMeasurer,
                text = dm.text,
                topLeft = Offset(x, y),
                style = textStyle.copy(color = dm.color),
            )
        }
    }
}
