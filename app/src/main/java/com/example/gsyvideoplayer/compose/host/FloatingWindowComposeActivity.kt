package com.example.gsyvideoplayer.compose.host

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.animation.BounceInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.gsyvideoplayer.EmptyActivity
import com.example.gsyvideoplayer.utils.floatUtil.FloatWindow
import com.example.gsyvideoplayer.utils.floatUtil.MoveType
import com.example.gsyvideoplayer.utils.floatUtil.Screen
import com.example.gsyvideoplayer.utils.floatUtil.Util
import com.example.gsyvideoplayer.view.FloatPlayerView
import com.shuyu.gsyvideoplayer.GSYVideoManager

class FloatingWindowComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FloatingWindowScreen()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.instance().releaseMediaPlayer()
        FloatWindow.destroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FloatingWindowScreen() {
    val context = LocalContext.current
    var hasOverlayPermission by remember {
        mutableStateOf(Build.VERSION.SDK_INT < 23 || Util.hasPermission(context))
    }
    var floatingShown by remember { mutableStateOf(FloatWindow.get() != null) }

    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        hasOverlayPermission = Build.VERSION.SDK_INT < 23 || Util.hasPermission(context)
    }

    DisposableEffect(Unit) {
        onDispose {
            // Activity 销毁时不清理 FloatWindow，让"跳到 EmptyActivity 时画中画继续显示"
            // 真正清理放在 Activity.onDestroy（FloatWindow.destroy()）
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Compose 悬浮窗（画中画）Demo") }) }
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
                "对齐 Java WindowActivity：通过 SYSTEM_ALERT_WINDOW 权限拉起跨 Activity 的悬浮窗 " +
                    "(FloatWindow + FloatPlayerView)，演示 Compose Activity 上同样可以驱动" +
                    "Wrapper 模式的全局画中画 Player。",
                style = MaterialTheme.typography.bodyMedium,
            )

            Text(
                "权限状态：${if (hasOverlayPermission) "✅ 已授予" else "⚠️ 未授予"}\n" +
                    "悬浮窗状态：${if (floatingShown) "▶ 已显示" else "⏸ 未显示"}",
                style = MaterialTheme.typography.bodySmall,
            )

            if (!hasOverlayPermission) {
                Button(onClick = {
                    if (Build.VERSION.SDK_INT >= 23) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}"),
                        )
                        overlayPermissionLauncher.launch(intent)
                    }
                }) { Text("申请悬浮窗权限") }
            }

            Button(
                enabled = hasOverlayPermission && !floatingShown,
                onClick = {
                    if (FloatWindow.get() != null) return@Button
                    val floatPlayerView = FloatPlayerView(context.applicationContext)
                    FloatWindow
                        .with(context.applicationContext)
                        .setView(floatPlayerView)
                        .setWidth(Screen.width, 0.4f)
                        .setHeight(Screen.width, 0.4f)
                        .setX(Screen.width, 0.8f)
                        .setY(Screen.height, 0.3f)
                        .setMoveType(MoveType.slide)
                        .setFilter(false)
                        .setMoveStyle(500, BounceInterpolator())
                        .build()
                    FloatWindow.get()?.show()
                    floatingShown = true
                }
            ) { Text("启动悬浮窗") }

            OutlinedButton(
                enabled = floatingShown,
                onClick = {
                    FloatWindow.destroy()
                    floatingShown = false
                }
            ) { Text("关闭悬浮窗") }

            OutlinedButton(onClick = {
                context.startActivity(Intent(context, EmptyActivity::class.java))
            }) { Text("跳到其他页面（验证悬浮窗常驻）") }
        }
    }
}
