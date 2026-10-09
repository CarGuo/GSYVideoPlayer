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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.cache.CacheFactory
import com.shuyu.gsyvideoplayer.cache.ICacheManager
import com.shuyu.gsyvideoplayer.cache.ProxyCacheManager
import com.shuyu.gsyvideoplayer.compose.native_.GSYDefaultControls
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerSurface
import com.shuyu.gsyvideoplayer.compose.native_.rememberGSYPlayerController
import com.shuyu.gsyvideoplayer.player.IPlayerManager
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.shuyu.gsyvideoplayer.utils.Debuger
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager
import tv.danmaku.ijk.media.exo2.ExoPlayerCacheManager

class CacheDownloadComposeActivity : ComponentActivity() {
    private var originalPlayManager: Class<*>? = null
    private var originalCacheManager: Class<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        originalPlayManager = runCatching {
            val f = PlayerFactory::class.java.getDeclaredField("sPlayerManager")
            f.isAccessible = true
            f.get(null) as? Class<*>
        }.getOrNull()
        originalCacheManager = runCatching {
            val f = CacheFactory::class.java.getDeclaredField("sICacheManager")
            f.isAccessible = true
            f.get(null) as? Class<*>
        }.getOrNull()
        val initialExo = intent?.getBooleanExtra(
            "use_exo_cache",
            PlayerFactory.getPlayManager() is Exo2PlayerManager ||
                CacheFactory.getCacheManager() is ExoPlayerCacheManager,
        ) ?: false
        val initialUrl = intent?.getStringExtra("sample_url") ?: DemoSamples.SAMPLE_URL
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CacheDownloadScreen(initialExo = initialExo, sampleUrl = initialUrl)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        @Suppress("UNCHECKED_CAST")
        originalPlayManager?.let {
            runCatching {
                PlayerFactory.setPlayManager(it as Class<out IPlayerManager>)
            }
        }
        @Suppress("UNCHECKED_CAST")
        originalCacheManager?.let {
            runCatching {
                CacheFactory.setCacheManager(it as Class<out ICacheManager>)
            }
        }
    }
}

private const val SHORT_CACHE_SAMPLE_URL =
    "https://pointshow.oss-cn-hangzhou.aliyuncs.com/McTk51586843620689.mp4"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CacheDownloadScreen(
    initialExo: Boolean = false,
    sampleUrl: String = DemoSamples.SAMPLE_URL,
) {
    val context = LocalContext.current
    val appContext = remember(context) { context.applicationContext }
    val controller = rememberGSYPlayerController()
    val snap by controller.snapshot

    var useExoCache by remember { mutableStateOf(initialExo) }
    var currentUrl by remember { mutableStateOf(sampleUrl) }
    var statusText by remember { mutableStateOf("等待开始") }

    LaunchedEffect(useExoCache, snap.state, snap.bufferPercent, snap.isCacheReady) {
        val modeLabel = if (useExoCache) "EXO2+ExoCache" else "IJK+ProxyCache"
        Debuger.printfLog(
            "CacheDownloadCompose: mode=$modeLabel state=${snap.state} " +
                "pos=${snap.currentPosition}/${snap.duration} buffer=${snap.bufferPercent}% cacheReady=${snap.isCacheReady}",
        )
    }

    LaunchedEffect(useExoCache, currentUrl) {
        if (useExoCache) {
            PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)
            CacheFactory.setCacheManager(ExoPlayerCacheManager::class.java)
        } else {
            PlayerFactory.setPlayManager(IjkPlayerManager::class.java)
            CacheFactory.setCacheManager(ProxyCacheManager::class.java)
        }
        val builder = GSYVideoOptionBuilder()
            .setUrl(currentUrl)
            .setCacheWithPlay(true)
            .setVideoTitle("Cache + Download Demo")
        controller.setUp(builder, autoPlay = true)
        val modeLabel = if (useExoCache) "EXO2 + ExoPlayerCacheManager" else "IJK + ProxyCacheManager"
        statusText = "已启用 $modeLabel：$currentUrl"
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Native 缓存 / 下载 Demo") }) }
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
                "对齐 Java DetailDownloadPlayer / DetailDownloadExoPlayer：开启 setCacheWithPlay(true) 交由 " +
                    "CacheFactory 当前缓存管理器（ProxyCacheManager 或 ExoPlayerCacheManager）接管缓存，下方显示 " +
                    "bufferPercent 与 isCacheReady 状态。注：EXO 默认缓冲上限约 50s，在 92 分钟长样片下约占 1%，" +
                    "可点击「切 30s 短视频」快速观察 0% → 100% 完整缓存与二次秒开命中。",
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
                buildString {
                    val modeLabel = if (useExoCache) "EXO2 + ExoPlayerCache" else "IJK + ProxyCache"
                    append("模式：$modeLabel\n")
                    append("状态：${snap.state} | ${snap.currentPosition} / ${snap.duration} ms\n")
                    append("缓冲：${snap.bufferPercent}% | 网速：${snap.netSpeedText}\n")
                    append("缓存命中：${if (snap.isCacheReady) "✅ 已命中本地缓存" else "⏳ 流式加载中"}\n")
                    append(statusText)
                },
                style = MaterialTheme.typography.bodySmall,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    GSYVideoManager.instance().clearAllDefaultCache(appContext)
                    statusText = "已调用 clearAllDefaultCache(ctx)"
                }) { Text("清缓存") }
                OutlinedButton(onClick = {
                    val builder = GSYVideoOptionBuilder()
                        .setUrl(currentUrl)
                        .setCacheWithPlay(true)
                        .setVideoTitle("Cache + Download Demo")
                    controller.setUp(builder, autoPlay = true)
                    statusText = "已重新 setUp 触发重新加载"
                }) { Text("重新加载") }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    useExoCache = !useExoCache
                }) {
                    Text(if (useExoCache) "切回 IJK+Proxy" else "切到 EXO+Cache")
                }
                OutlinedButton(onClick = {
                    currentUrl = if (currentUrl == SHORT_CACHE_SAMPLE_URL) {
                        DemoSamples.SAMPLE_URL
                    } else {
                        SHORT_CACHE_SAMPLE_URL
                    }
                }) {
                    Text(if (currentUrl == SHORT_CACHE_SAMPLE_URL) "切 92min 长样片" else "切 30s 短视频")
                }
            }
        }
    }
}
