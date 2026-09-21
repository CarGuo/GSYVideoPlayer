package com.example.gsyvideoplayer.compose.host

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.Layout
import com.shuyu.gsyvideoplayer.compose.native_.GSYDefaultControls
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerEvent
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerSurface
import com.shuyu.gsyvideoplayer.compose.native_.rememberGSYPlayerController
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo

class FoldComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FoldScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoldScreen() {
    val context = LocalContext.current
    val activity = remember(context) { context as? Activity }
    val controller = rememberGSYPlayerController(
        url = DemoSamples.SAMPLE_URL,
        title = "Fold Compose Demo",
        autoPlay = true,
    )

    // 姿态注入（仅矩阵验证用）：-1 不注入，走真实 FoldingFeature；
    // 0 FLAT / 1 BOOK / 2 TABLETOP。
    val injectedPosture = remember(activity) {
        activity?.intent?.getIntExtra("extra_posture", -1) ?: -1
    }

    var fullscreen by remember { mutableStateOf(false) }
    LaunchedEffect(controller) {
        controller.events.collect { ev ->
            when (ev) {
                GSYPlayerEvent.EnterFull -> fullscreen = true
                GSYPlayerEvent.QuitFull -> fullscreen = false
                else -> {}
            }
        }
    }
    BackHandler(enabled = fullscreen && activity != null) {
        controller.exitFullscreen(activity!!)
    }

    val layoutInfo by produceState<WindowLayoutInfo?>(initialValue = null, context) {
        val activity = context as? Activity
        if (activity != null) {
            WindowInfoTracker.getOrCreate(context)
                .windowLayoutInfo(activity)
                .collect { value = it }
        }
    }

    val foldFeature = remember(layoutInfo) {
        layoutInfo?.displayFeatures
            ?.filterIsInstance<FoldingFeature>()
            ?.firstOrNull { it.state == FoldingFeature.State.HALF_OPENED }
    }

    val isBook = injectedPosture == 1
        || (injectedPosture < 0
            && foldFeature?.orientation == FoldingFeature.Orientation.VERTICAL)
    val isTabletop = injectedPosture == 2
        || (injectedPosture < 0
            && foldFeature?.orientation == FoldingFeature.Orientation.HORIZONTAL)

    val isLandscape = LocalConfiguration.current.orientation ==
        Configuration.ORIENTATION_LANDSCAPE
    // 横屏完全展开：大屏左右分栏
    val isLandscapeFlat = isLandscape && !isBook && !isTabletop

    val postureText = when {
        isBook -> "左右折叠 · BOOK 竖向折痕"
        isTabletop -> "上下折叠 · TABLETOP 横向折痕"
        isLandscapeFlat -> "横向正向 · 大屏左右分栏"
        else -> "展开态"
    }

    val realHinge = foldFeature?.let {
        val t = minOf(it.bounds.width(), it.bounds.height())
        if (t > 0) t else with(LocalDensity.current) { 24.dp.roundToPx() }
    } ?: 0
    val hingeSize = if (injectedPosture >= 0) {
        with(LocalDensity.current) { 24.dp.roundToPx() }
    } else {
        realHinge
    }

    val mode = when {
        isBook -> FoldMode.BOOK
        isTabletop -> FoldMode.TABLETOP
        isLandscapeFlat -> FoldMode.LANDSCAPE_FLAT
        else -> FoldMode.PORTRAIT_FLAT
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("折叠屏 Compose Demo") }) }
    ) { padding ->
        FoldSplit(
            mode = mode,
            hingePx = hingeSize,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            player = {
                Box(modifier = it.background(Color.Black)) {
                    GSYPlayerSurface(controller, Modifier.fillMaxSize())
                    if (!fullscreen) {
                        GSYDefaultControls(controller, Modifier.fillMaxSize())
                        if (activity != null) {
                            Button(
                                onClick = { controller.enterFullscreen(activity) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp),
                            ) {
                                Text("全屏")
                            }
                        }
                    }
                }
            },
            hinge = {
                Box(modifier = it.background(Color(0xFF202020)))
            },
            info = {
                InfoColumn(postureText, modifier = it)
            },
        )
    }
}

private enum class FoldMode { BOOK, TABLETOP, LANDSCAPE_FLAT, PORTRAIT_FLAT }

@Composable
private fun FoldSplit(
    mode: FoldMode,
    hingePx: Int,
    modifier: Modifier = Modifier,
    player: @Composable (Modifier) -> Unit,
    hinge: @Composable (Modifier) -> Unit,
    info: @Composable (Modifier) -> Unit,
) {
    Layout(
        modifier = modifier,
        contents = listOf(
            @Composable { player(Modifier) },
            @Composable { hinge(Modifier) },
            @Composable { info(Modifier) },
        ),
    ) { (pMeas, hMeas, iMeas), constraints ->
        val w = constraints.maxWidth
        val h = constraints.maxHeight

        val horizontal = mode == FoldMode.BOOK || mode == FoldMode.LANDSCAPE_FLAT
        val showHinge = mode == FoldMode.BOOK || mode == FoldMode.TABLETOP
        val dividerPx = if (showHinge) hingePx else 0

        val (pC, dC, iC) = if (horizontal) {
            val avail = (w - dividerPx).coerceAtLeast(0)
            val pw = avail / 2
            Triple(
                Constraints.fixed(pw, h),
                Constraints.fixed(dividerPx, h),
                Constraints.fixed((w - pw - dividerPx).coerceAtLeast(0), h),
            )
        } else if (mode == FoldMode.TABLETOP) {
            val avail = (h - dividerPx).coerceAtLeast(0)
            val ph = avail / 2
            Triple(
                Constraints.fixed(w, ph),
                Constraints.fixed(w, dividerPx),
                Constraints.fixed(w, (h - ph - dividerPx).coerceAtLeast(0)),
            )
        } else {
            val ph = (w * 9f / 16f).toInt().coerceAtMost(h)
            Triple(
                Constraints.fixed(w, ph),
                Constraints.fixed(0, 0),
                Constraints.fixed(w, (h - ph).coerceAtLeast(0)),
            )
        }

        val placeables = listOf(
            pMeas[0].measure(pC),
            hMeas[0].measure(dC),
            iMeas[0].measure(iC),
        )

        layout(w, h) {
            if (horizontal) {
                placeables[0].placeRelative(0, 0)
                placeables[1].placeRelative(placeables[0].width, 0)
                placeables[2].placeRelative(placeables[0].width + placeables[1].width, 0)
            } else {
                placeables[0].placeRelative(0, 0)
                placeables[1].placeRelative(0, placeables[0].height)
                placeables[2].placeRelative(0, placeables[0].height + placeables[1].height)
            }
        }
    }
}

@Composable
private fun InfoColumn(
    postureText: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            postureText,
            color = Color(0xFF6200EE),
            style = MaterialTheme.typography.labelLarge,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "折叠屏适配 Demo（Compose）",
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "竖屏半折进入 BOOK（竖向折痕）：视频在左、信息在右；" +
                "横屏半折进入 TABLETOP（横向折痕）：视频在上、信息在下。" +
                "两种姿态下播放器与文字均不跨越铰链，展开后恢复单列 16:9。",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(16.dp))
        repeat(8) { index ->
            Surface(
                tonalElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "段落 ${index + 1}",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        "Compose 详情区说明文字，演示折叠分栏后信息区的纵向滚动。",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
