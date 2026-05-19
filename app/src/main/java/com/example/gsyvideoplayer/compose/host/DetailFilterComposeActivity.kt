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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shuyu.gsyvideoplayer.compose.native_.GSYDefaultControls
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerSurface
import com.shuyu.gsyvideoplayer.compose.native_.rememberGSYPlayerController
import com.shuyu.gsyvideoplayer.render.effect.BlackAndWhiteEffect
import com.shuyu.gsyvideoplayer.render.effect.GammaEffect
import com.shuyu.gsyvideoplayer.render.effect.GaussianBlurEffect
import com.shuyu.gsyvideoplayer.render.effect.InvertColorsEffect
import com.shuyu.gsyvideoplayer.render.effect.NoEffect
import com.shuyu.gsyvideoplayer.render.effect.SepiaEffect
import com.shuyu.gsyvideoplayer.render.view.GSYVideoGLView
import com.shuyu.gsyvideoplayer.utils.GSYVideoType

class DetailFilterComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DetailFilterScreen()
                }
            }
        }
    }
}

private data class FilterEntry(
    val name: String,
    val factory: () -> GSYVideoGLView.ShaderInterface,
)

private val FILTERS: List<FilterEntry> = listOf(
    FilterEntry("无滤镜") { NoEffect() },
    FilterEntry("Gamma") { GammaEffect(0.8f) },
    FilterEntry("黑白") { BlackAndWhiteEffect() },
    FilterEntry("反色") { InvertColorsEffect() },
    FilterEntry("棕褐") { SepiaEffect() },
    FilterEntry("高斯模糊") { GaussianBlurEffect(6.0f, GaussianBlurEffect.TYPEXY) },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailFilterScreen() {
    val backupRender = remember { GSYVideoType.getRenderType() }
    DisposableEffect(Unit) {
        // 切到 GLSurface 才能支持 GLSL 滤镜（与 Java 版 DetailFilterActivity 同路径）。
        // 这是全局设置，演示退出时还原，避免影响其他 demo。
        GSYVideoType.setRenderType(GSYVideoType.GLSURFACE)
        onDispose { GSYVideoType.setRenderType(backupRender) }
    }

    val controller = rememberGSYPlayerController(
        url = DemoSamples.SAMPLE_URL,
        title = "Compose Filter Demo",
        autoPlay = true,
    )
    val snap by controller.snapshot
    var filterIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(filterIndex) {
        val effect = FILTERS[filterIndex].factory()
        // Native 模式滤镜必须 setEffectFilter 直接 attach 到 GLSurfaceView。
        // 这是典型的"Compose 端尚未封装"能力，通过 withHost 逃生口注入。
        controller.withHost { player -> player.setEffectFilter(effect) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Native 滤镜 Demo") }) }
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
                "对齐 Java DetailFilterActivity：通过 controller.withHost { player -> player.setEffectFilter(...) } " +
                    "切换 GLSL 滤镜，演示 Compose 逃生口与 GSY 内核 GLSurface 渲染管线的协同。",
                style = MaterialTheme.typography.bodyMedium,
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black),
            ) {
                GSYPlayerSurface(controller = controller, modifier = Modifier.fillMaxSize())
                GSYDefaultControls(controller = controller, modifier = Modifier.fillMaxSize())
            }

            Text(
                "当前滤镜：${FILTERS[filterIndex].name}\n" +
                    "状态：${snap.state} | ${snap.currentPosition} / ${snap.duration} ms",
                style = MaterialTheme.typography.bodySmall,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    filterIndex = (filterIndex + 1) % FILTERS.size
                }) { Text("下一个滤镜") }
                OutlinedButton(onClick = {
                    filterIndex = 0
                }) { Text("重置") }
            }

            Text("快速选择：", style = MaterialTheme.typography.labelLarge)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FILTERS.forEachIndexed { idx, entry ->
                    if (idx == filterIndex) {
                        Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                            Text("● ${entry.name}")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { filterIndex = idx },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(entry.name) }
                    }
                }
            }
        }
    }
}
