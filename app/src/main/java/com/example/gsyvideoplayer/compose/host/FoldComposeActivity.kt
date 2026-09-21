package com.example.gsyvideoplayer.compose.host

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.Layout
import com.shuyu.gsyvideoplayer.compose.native_.GSYDefaultControls
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
    val controller = rememberGSYPlayerController(
        url = DemoSamples.SAMPLE_URL,
        title = "Fold Compose Demo",
        autoPlay = true,
    )

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

    val isBook = foldFeature?.orientation == FoldingFeature.Orientation.VERTICAL
    val isTabletop = foldFeature?.orientation == FoldingFeature.Orientation.HORIZONTAL

    val postureText = when {
        isBook -> "左右折叠 · BOOK 竖向折痕"
        isTabletop -> "上下折叠 · TABLETOP 横向折痕"
        else -> "展开态"
    }

    val hingeSize = foldFeature?.let {
        val t = minOf(it.bounds.width(), it.bounds.height())
        if (t > 0) t else with(LocalDensity.current) { 24.dp.roundToPx() }
    } ?: 0

    Scaffold(
        topBar = { TopAppBar(title = { Text("折叠屏 Compose Demo") }) }
    ) { padding ->
        FoldSplit(
            isBook = isBook,
            isTabletop = isTabletop,
            hingePx = hingeSize,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            player = {
                Box(modifier = it.background(Color.Black)) {
                    GSYPlayerSurface(controller, Modifier.fillMaxSize())
                    GSYDefaultControls(controller, Modifier.fillMaxSize())
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

@Composable
private fun FoldSplit(
    isBook: Boolean,
    isTabletop: Boolean,
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

        val (pC, hC, iC) = when {
            isBook -> {
                val avail = (w - hingePx).coerceAtLeast(0)
                val pw = avail / 2
                Triple(
                    Constraints.fixed(pw, h),
                    Constraints.fixed(hingePx, h),
                    Constraints.fixed(w - pw - hingePx, h),
                )
            }
            isTabletop -> {
                val avail = (h - hingePx).coerceAtLeast(0)
                val ph = avail / 2
                Triple(
                    Constraints.fixed(w, ph),
                    Constraints.fixed(w, hingePx),
                    Constraints.fixed(w, h - ph - hingePx),
                )
            }
            else -> {
                val ph = (w * 9f / 16f).toInt().coerceAtMost(h)
                Triple(
                    Constraints.fixed(w, ph),
                    Constraints.fixed(0, 0),
                    Constraints.fixed(w, h - ph),
                )
            }
        }

        val placeables = listOf(
            pMeas[0].measure(pC),
            hMeas[0].measure(hC),
            iMeas[0].measure(iC),
        )

        layout(w, h) {
            if (isBook) {
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
