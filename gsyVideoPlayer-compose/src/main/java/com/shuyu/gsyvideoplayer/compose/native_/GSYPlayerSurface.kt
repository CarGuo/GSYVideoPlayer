package com.shuyu.gsyvideoplayer.compose.native_

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext

/**
 * 创建/记忆一个 Compose 原生模式控制器。
 *
 * @param url 可空。若提供则等同于调用 controller.setUp(url)
 * @param cacheWithPlay 是否边播边存
 * @param title 视频标题
 */
@Composable
fun rememberGSYPlayerController(
    url: String? = null,
    cacheWithPlay: Boolean = false,
    title: String = "",
    autoPlay: Boolean = false,
): GSYPlayerController {
    val context = LocalContext.current
    val controller = remember { GSYPlayerController(context) }
    if (url != null) {
        remember(controller, url, cacheWithPlay, title, autoPlay) {
            controller.setUp(url, cacheWithPlay, title, autoPlay)
            url
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
