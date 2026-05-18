package com.shuyu.gsyvideoplayer.compose.wrapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.compose.common.LifecycleEffect
import com.shuyu.gsyvideoplayer.video.NormalGSYVideoPlayer
import androidx.lifecycle.Lifecycle

/**
 * 通用包装：用户可传入任意 [NormalGSYVideoPlayer] 子类（StandardGSYVideoPlayer、
 * ListGSYVideoPlayer、SampleControlVideo 等）。
 *
 * 注意：本通用版本不直接依赖 app 模块的子类，仅依赖 [NormalGSYVideoPlayer]，
 * 调用方通过 [factory] 决定具体类型。
 */
@Composable
fun <T : NormalGSYVideoPlayer> GSYAnyVideoPlayerView(
    modifier: Modifier = Modifier,
    factory: (android.content.Context) -> T,
    setUp: (T) -> Unit,
    autoReleaseOnDispose: Boolean = true,
    autoPauseResume: Boolean = true,
    onPlayerCreated: (T) -> Unit = {},
) {
    val holder = remember { arrayOfNulls<NormalGSYVideoPlayer>(1) }
    val currentSetUp by rememberUpdatedState(setUp)
    val currentOnPlayerCreated by rememberUpdatedState(onPlayerCreated)

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            factory(ctx).also { player ->
                holder[0] = player
                currentSetUp(player)
                currentOnPlayerCreated(player)
            }
        },
        update = { /* setUp 仅在 factory 时调用一次，避免重复 build 重置播放状态 */ }
    )

    if (autoPauseResume) {
        LifecycleEffect { event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> GSYVideoManager.onPause()
                Lifecycle.Event.ON_RESUME -> GSYVideoManager.onResume()
                else -> Unit
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (autoReleaseOnDispose) {
                holder[0]?.release()
                holder[0] = null
            }
        }
    }
}
