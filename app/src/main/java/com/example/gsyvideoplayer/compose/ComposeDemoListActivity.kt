package com.example.gsyvideoplayer.compose

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.gsyvideoplayer.compose.host.AutoPlayListActivity
import com.example.gsyvideoplayer.compose.host.BasicWrapperActivity
import com.example.gsyvideoplayer.compose.host.CacheDownloadComposeActivity
import com.example.gsyvideoplayer.compose.host.DetailFilterComposeActivity
import com.example.gsyvideoplayer.compose.host.DetailNativeActivity
import com.example.gsyvideoplayer.compose.host.FullFeatureNativeActivity
import com.example.gsyvideoplayer.compose.host.ListPlayNativeActivity
import com.example.gsyvideoplayer.compose.host.ListWithFullscreenActivity
import com.example.gsyvideoplayer.compose.host.MultiWindowActivity
import com.example.gsyvideoplayer.compose.host.SubtitleComposeActivity
import com.example.gsyvideoplayer.compose.host.SwitchSeamlessComposeActivity
import com.example.gsyvideoplayer.compose.host.SwitchUrlActivity

class ComposeDemoListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DemoListScreen()
                }
            }
        }
    }
}

private data class DemoEntry(
    val title: String,
    val subtitle: String,
    val target: Class<*>,
)

private val DEMO_ENTRIES = listOf(
    DemoEntry(
        title = "P0 · Wrapper 基础",
        subtitle = "AndroidView 包装 StandardGSYVideoPlayer，复用全部内置 UI（手势/全屏/旋转）",
        target = BasicWrapperActivity::class.java,
    ),
    DemoEntry(
        title = "P0 · Native 详情",
        subtitle = "Compose 详情页：顶部播放器 + 下方文字简介，支持全屏切换与锁定",
        target = DetailNativeActivity::class.java,
    ),
    DemoEntry(
        title = "P0 · Native 完整控件层",
        subtitle = "纯 Compose 自绘控件：播放/进度/倍速/快进 ±15s/锁定/错误重试",
        target = FullFeatureNativeActivity::class.java,
    ),
    DemoEntry(
        title = "P1 · Native 列表",
        subtitle = "对齐 Java ListNormalAdapter：LazyColumn + 单实例 Native 播放器，滚动出屏自动暂停",
        target = ListPlayNativeActivity::class.java,
    ),
    DemoEntry(
        title = "P1 · Native 切换 URL",
        subtitle = "对齐 Java SwitchVideo：同一 controller 切换不同 URL，记忆每段位置",
        target = SwitchUrlActivity::class.java,
    ),
    DemoEntry(
        title = "P1 · Native 多窗口",
        subtitle = "对齐 Java MultiSampleVideo：同屏 3 个 Native Surface，互斥激活",
        target = MultiWindowActivity::class.java,
    ),
    DemoEntry(
        title = "P1 · Native 自动连播",
        subtitle = "对齐 Java ListGSYVideoPlayer.playNext：当前段播完自动 release+attach 下一段",
        target = AutoPlayListActivity::class.java,
    ),
    DemoEntry(
        title = "P1 · Native 列表 + 内层全屏",
        subtitle = "对齐 Java DetailListPlayer：列表卡片直接进入全屏，返回回到原列表位（同 Activity）",
        target = ListWithFullscreenActivity::class.java,
    ),
    DemoEntry(
        title = "P5 · Native 滤镜",
        subtitle = "对齐 Java DetailFilterActivity：通过 withHost 注入 setEffectFilter，循环切换 6 种 GLSL 滤镜",
        target = DetailFilterComposeActivity::class.java,
    ),
    DemoEntry(
        title = "P5 · Native 缓存 / 下载",
        subtitle = "对齐 Java DetailDownloadPlayer：ProxyCacheManager 代理 + isCacheReady 状态 + 清缓存",
        target = CacheDownloadComposeActivity::class.java,
    ),
    DemoEntry(
        title = "P5 · Native 字幕",
        subtitle = "对齐 Java SubtitleDetailPlayer：3 字幕源切换 + 字号/开关，IJK 内核",
        target = SubtitleComposeActivity::class.java,
    ),
    DemoEntry(
        title = "P5 · Native Seamless 切换",
        subtitle = "对齐 Java SwitchDetailActivity：同一 controller 跨 list/detail 复用，不重 setUp 不重拉流",
        target = SwitchSeamlessComposeActivity::class.java,
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DemoListScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Compose Demo 大全") })
        }
    ) { padding ->
        val context = LocalContext.current
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(DEMO_ENTRIES) { entry ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            context.startActivity(Intent(context, entry.target))
                        }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(entry.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            entry.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}
