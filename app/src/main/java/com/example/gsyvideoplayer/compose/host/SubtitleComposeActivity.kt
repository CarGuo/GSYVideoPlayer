package com.example.gsyvideoplayer.compose.host

import android.graphics.Color as AndroidColor
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.gsyvideoplayer.R
import com.example.gsyvideoplayer.utils.DemoVideoUrls
import com.shuyu.gsyvideoplayer.compose.native_.GSYDefaultControls
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerSurface
import com.shuyu.gsyvideoplayer.compose.native_.rememberGSYPlayerController
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.shuyu.gsyvideoplayer.subtitle.GSYSubtitleSource
import com.shuyu.gsyvideoplayer.subtitle.GSYSubtitleStyle

class SubtitleComposeActivity : ComponentActivity() {
    private var originalPlayManager: Class<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 通用字幕路径需要 IJK 内核（与 Java SubtitleDetailPlayer 同条件）。
        // 进入页面前通过反射记录原 PlayManager 类，离开时还原，避免污染后续 demo
        // （PlayerFactory.sPlayerManager 是 process 级单例）。
        originalPlayManager = runCatching {
            val f = PlayerFactory::class.java.getDeclaredField("sPlayerManager")
            f.isAccessible = true
            f.get(null) as? Class<*>
        }.getOrNull()
        PlayerFactory.setPlayManager(IjkPlayerManager::class.java)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SubtitleScreen()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        @Suppress("UNCHECKED_CAST")
        originalPlayManager?.let {
            runCatching {
                PlayerFactory.setPlayManager(it as Class<out com.shuyu.gsyvideoplayer.player.IPlayerManager>)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubtitleScreen() {
    val context = LocalContext.current
    val packageName = remember(context) { context.packageName }

    val controller = rememberGSYPlayerController(
        url = DemoSamples.SAMPLE_URL,
        title = "Compose Subtitle Demo",
        autoPlay = true,
    )
    val snap by controller.snapshot

    val sources = remember(packageName) {
        listOf(
            GSYSubtitleSource.Builder("android.resource://$packageName/${R.raw.demo_subtitle}")
                .setId("local-srt")
                .setLabel("SRT 本地")
                .setLanguage("zh")
                .setDefault(true)
                .build(),
            GSYSubtitleSource.Builder("android.resource://$packageName/${R.raw.demo_subtitle_vtt}")
                .setId("local-vtt")
                .setLabel("VTT 本地")
                .setLanguage("en")
                .build(),
            GSYSubtitleSource.Builder(DemoVideoUrls.SUBTITLE_SRT)
                .setId("network-srt")
                .setLabel("SRT 网络")
                .setLanguage("zh")
                .build(),
        )
    }

    var sourceIndex by remember { mutableIntStateOf(0) }
    var enabled by remember { mutableStateOf(true) }
    var large by remember { mutableStateOf(false) }

    fun makeStyle(sizeSp: Int): GSYSubtitleStyle =
        GSYSubtitleStyle.Builder()
            .setTextColor(AndroidColor.WHITE)
            .setTextSizeSp(sizeSp.toFloat())
            .setShadow(AndroidColor.BLACK, 3f, 1f, 1f)
            .setBottomMarginDp(56)
            .build()

    // setSubtitleSources / setSubtitleStyle 都是 host 直访 API，Compose 端通过 withHost 注入。
    // 这里在 controller attach 完成后通过 LaunchedEffect 一次性注入；后续切换通过单独 effect。
    LaunchedEffect(controller) {
        controller.withHost { player ->
            player.setSubtitleSources(sources)
            player.setSubtitleStyle(makeStyle(16))
        }
    }
    LaunchedEffect(sourceIndex) {
        val src = sources[sourceIndex]
        controller.withHost { player -> player.selectSubtitle(src.id) }
    }
    LaunchedEffect(enabled) {
        controller.withHost { player -> player.setSubtitleEnabled(enabled) }
    }
    LaunchedEffect(large) {
        controller.withHost { player ->
            player.setSubtitleStyle(makeStyle(if (large) 22 else 16))
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Native 字幕 Demo (IJK)") }) }
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
                "对齐 Java SubtitleDetailPlayer：通过 controller.withHost { player -> player.setSubtitleSources(...) } " +
                    "注入 3 个字幕源（SRT 本地 / VTT 本地 / SRT 网络）；通过 selectSubtitle 切换、setSubtitleEnabled " +
                    "开关、setSubtitleStyle 调整字号。",
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
                "状态：${snap.state} | ${snap.currentPosition} / ${snap.duration} ms\n" +
                    "字幕源：${sources[sourceIndex].label} | 开关：${if (enabled) "ON" else "OFF"} | " +
                    "字号：${if (large) 22 else 16} sp",
                style = MaterialTheme.typography.bodySmall,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { enabled = !enabled }) {
                    Text(if (enabled) "关闭字幕" else "开启字幕")
                }
                Button(onClick = { large = !large }) {
                    Text(if (large) "字号 16" else "字号 22")
                }
            }

            Text("切换字幕源：", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                sources.forEachIndexed { idx, src ->
                    if (idx == sourceIndex) {
                        Button(onClick = {}) { Text("● ${src.label}") }
                    } else {
                        OutlinedButton(onClick = { sourceIndex = idx }) { Text(src.label ?: "#${idx + 1}") }
                    }
                }
            }
        }
    }
}
