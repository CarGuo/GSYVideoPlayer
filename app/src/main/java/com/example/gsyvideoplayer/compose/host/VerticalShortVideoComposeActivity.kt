package com.example.gsyvideoplayer.compose.host

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.compose.native_.GSYDefaultControls
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerSurface
import com.shuyu.gsyvideoplayer.compose.native_.rememberGSYPlayerController
import kotlinx.coroutines.flow.distinctUntilChanged

class VerticalShortVideoComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    VerticalShortVideoScreen()
                }
            }
        }
    }
}

@Composable
private fun VerticalShortVideoScreen() {
    val items = remember { DemoSamples.SAMPLE_LIST }
    val controller = rememberGSYPlayerController()
    val snap by controller.snapshot

    val pagerState = rememberPagerState(pageCount = { items.size })

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                val entry = items[page]
                val builder = GSYVideoOptionBuilder()
                    .setUrl(entry.url)
                    .setVideoTitle(entry.title)
                    .setLooping(true)
                    .setCacheWithPlay(true)
                controller.setUp(builder, autoPlay = true)
            }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            val entry = items[page]
            val isCurrent = page == pagerState.currentPage
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                if (isCurrent) {
                    GSYPlayerSurface(controller = controller, modifier = Modifier.fillMaxSize())
                    GSYDefaultControls(controller = controller, modifier = Modifier.fillMaxSize())
                } else {
                    Text(
                        "#${page + 1}\n${entry.title}",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            Text(
                "竖屏短视频 (Compose VerticalPager)",
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                "对齐 Java ViewPager2Activity：垂直滑动逐条切换；同一 controller 跨页 setUp，" +
                    "不释放、不 saveState；当前 ${pagerState.currentPage + 1}/${items.size} | ${snap.state}",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
