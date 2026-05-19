package com.shuyu.gsyvideoplayer.compose.native_

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.compose.common.LifecycleEffect

/**
 * 创建/记忆一个 Compose 原生模式控制器。
 *
 * @param url            可空。若提供则等同于调用 controller.setUp(url)
 * @param cacheWithPlay  是否边播边存
 * @param title          视频标题
 * @param autoPlay       attach 完成后是否自动 startPlayLogic
 * @param autoPauseResume **R3 P1-2** 是否在宿主 lifecycle ON_PAUSE/ON_RESUME 时自动调用
 *                       [GSYVideoManager.onPause] / [GSYVideoManager.onResume]，与 Wrapper
 *                       模式行为对齐。默认 `true`：HOME / 切到后台 → 自动暂停；回前台 → 自动恢复。
 *                       少数业务（如悬浮窗 / PiP / 自定义后台播放）需要保持后台播放，可显式传 `false`。
 */
@Composable
fun rememberGSYPlayerController(
    url: String? = null,
    cacheWithPlay: Boolean = false,
    title: String = "",
    autoPlay: Boolean = false,
    autoPauseResume: Boolean = true,
): GSYPlayerController {
    val context = LocalContext.current
    val controller = remember { GSYPlayerController(context) }
    if (url != null) {
        // 关键约束：不能在 remember 的 calculation lambda 里执行 controller.setUp(...)，
        // 那是把"副作用"放在了"读 state"的位置——重组期会被多次调用，
        // 也无法保证只在 key 变化时触发一次。改用 LaunchedEffect：
        //   - key 变化时取消上一次协程并重新执行
        //   - 离开 composition 时自动清理
        //   - 在主线程的协程作用域内执行，避免 race
        LaunchedEffect(controller, url, cacheWithPlay, title, autoPlay) {
            controller.setUp(url, cacheWithPlay, title, autoPlay)
        }
    }
    if (autoPauseResume) {
        // 与 Wrapper 行为对齐：ON_PAUSE → GSYVideoManager.onPause()，ON_RESUME → onResume()。
        // 走 GSYVideoManager 全局门面，与 Java 端 [DetailPlayer] 等保持一致；
        // 内核会判别"当前是否处于播放态"——pause 不会破坏 Idle/Completed 等状态，安全。
        LifecycleEffect { event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    runCatching { GSYVideoManager.onPause() }
                }
                Lifecycle.Event.ON_RESUME -> {
                    runCatching { GSYVideoManager.onResume() }
                }
                else -> Unit
            }
        }
    }
    DisposableEffect(controller) {
        onDispose { controller.release() }
    }
    return controller
}

/**
 * 渲染层：仅承载视频画面，不绘制任何控制 UI。
 * 用户可在它之上叠加任意 Compose 控制层。
 */
@Composable
fun GSYPlayerSurface(
    controller: GSYPlayerController,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            GSYComposeHostPlayer(ctx).also { player ->
                controller.attachHost(player)
            }
        },
        onRelease = {
            // AndroidView 真正离开 composition 时，解绑 host，
            // 避免 controller 仍持有已被 detach 的 view，造成 leak。
            controller.detachHost()
        }
    )
}
