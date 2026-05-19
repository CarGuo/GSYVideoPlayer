package com.example.gsyvideoplayer.compose.host

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.compose.native_.GSYDefaultControls
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerSurface
import com.shuyu.gsyvideoplayer.compose.native_.rememberGSYPlayerController

class MoreTypeComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MoreTypeScreen()
                }
            }
        }
    }
}

private enum class CellType { Normal, Ad, Cover, Unknown }

private data class MoreTypeEntry(
    val type: CellType,
    val title: String,
    val url: String? = null,
    val coverHint: String? = null,
)

private val MORE_TYPE_LIST = listOf(
    MoreTypeEntry(CellType.Normal, "样片 #1 · 普通视频", DemoSamples.SAMPLE_URL),
    MoreTypeEntry(CellType.Ad, "广告位 · 跳过广告倒计时", DemoSamples.SAMPLE_URL_2),
    MoreTypeEntry(CellType.Cover, "封面位 · 静态海报（暂不播放）", coverHint = "POSTER"),
    MoreTypeEntry(CellType.Normal, "样片 #2 · IMG_0382", DemoSamples.SAMPLE_URL_2),
    MoreTypeEntry(CellType.Unknown, "未知卡片 · 占位文本", coverHint = "UNKNOWN"),
    MoreTypeEntry(CellType.Normal, "样片 #3 · GSY 默认", DemoSamples.SAMPLE_URL),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoreTypeScreen() {
    // D11 思路：单 controller 配合 LazyColumn 多 type 渲染——每个 cell 根据 type 走不同 UI 路径，
    // 但只有「当前激活索引」才 attach 播放 surface；其它 cell 退化为封面/占位，避免多 player 冲突。
    val controller = rememberGSYPlayerController()
    val snap by controller.snapshot

    var activeIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(activeIndex) {
        if (activeIndex < 0) return@LaunchedEffect
        val entry = MORE_TYPE_LIST[activeIndex]
        val url = entry.url ?: return@LaunchedEffect
        val builder = GSYVideoOptionBuilder()
            .setUrl(url)
            .setVideoTitle(entry.title)
            .setCacheWithPlay(true)
        controller.setUp(builder, autoPlay = true)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Compose 多类型列表 Demo") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "对齐 Java DetailMoreTypeActivity 的「多类型 cell」思路：" +
                    "Compose 端用 LazyColumn 渲染 4 类卡片（Normal/Ad/Cover/Unknown），" +
                    "单 controller 仅 attach 到当前激活索引。点击非空 url 卡片即可起播。",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                "当前激活：${if (activeIndex < 0) "无" else "#${activeIndex + 1} ${MORE_TYPE_LIST[activeIndex].title}"} | " +
                    "${snap.state} ${snap.currentPosition}/${snap.duration} ms",
                style = MaterialTheme.typography.bodySmall,
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(MORE_TYPE_LIST) { entry ->
                    val idx = MORE_TYPE_LIST.indexOf(entry)
                    val isActive = idx == activeIndex
                    when (entry.type) {
                        CellType.Normal -> NormalCell(entry, isActive, controller) {
                            if (!isActive) activeIndex = idx
                        }
                        CellType.Ad -> AdCell(entry, isActive, controller) {
                            if (!isActive) activeIndex = idx
                        }
                        CellType.Cover -> CoverCell(entry)
                        CellType.Unknown -> UnknownCell(entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun NormalCell(
    entry: MoreTypeEntry,
    isActive: Boolean,
    controller: com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerController,
    onClickPlay: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("[Normal] ${entry.title}", style = MaterialTheme.typography.titleSmall)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                if (isActive) {
                    GSYPlayerSurface(controller = controller, modifier = Modifier.fillMaxSize())
                    GSYDefaultControls(controller = controller, modifier = Modifier.fillMaxSize())
                } else {
                    TextButton(onClick = onClickPlay) {
                        Text("▶ 播放此条", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdCell(
    entry: MoreTypeEntry,
    isActive: Boolean,
    controller: com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerController,
    onClickPlay: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().background(Color(0xFFFFF3E0))) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("[AD] ${entry.title}", style = MaterialTheme.typography.titleSmall, color = Color(0xFFE65100))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color(0xFF333333)),
                contentAlignment = Alignment.Center,
            ) {
                if (isActive) {
                    GSYPlayerSurface(controller = controller, modifier = Modifier.fillMaxSize())
                    GSYDefaultControls(controller = controller, modifier = Modifier.fillMaxSize())
                } else {
                    TextButton(onClick = onClickPlay) {
                        Text("▶ 播放广告", color = Color(0xFFFFC107))
                    }
                }
            }
            Text("（广告 cell：与 Normal 共享 controller，UI 主题区分）", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun CoverCell(entry: MoreTypeEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("[Cover] ${entry.title}", style = MaterialTheme.typography.titleSmall)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color(0xFF607D8B)),
                contentAlignment = Alignment.Center,
            ) {
                Text("📷 ${entry.coverHint ?: ""}", color = Color.White)
            }
            Text("（封面位：不接 controller、不消耗解码器）", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun UnknownCell(entry: MoreTypeEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text("[Unknown] ${entry.title}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
