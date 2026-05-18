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
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import androidx.lifecycle.Lifecycle

/**
 * AndroidView 包装模式：
 * 直接复用 [StandardGSYVideoPlayer]，保留全部内置能力（全屏、手势、缓存、字幕、滤镜等）。
 *
 * @param modifier            布局 Modifier
 * @param setUp               用于配置 player（等价于 GSYVideoOptionBuilder.build(player)）
 * @param autoReleaseOnDispose 离屏时是否自动释放（默认 true）
 * @param autoPauseResume     是否自动桥接生命周期（默认 true）
 * @param onPlayerCreated     player 实例创建后回调，可用于持有引用
 */
@Composable
fun GSYVideoPlayerView(
    modifier: Modifier = Modifier,
    setUp: (StandardGSYVideoPlayer) -> Unit,
    autoReleaseOnDispose: Boolean = true,
    autoPauseResume: Boolean = true,
    onPlayerCreated: (StandardGSYVideoPlayer) -> Unit = {},
) {
    val playerHolder = remember { arrayOfNulls<StandardGSYVideoPlayer>(1) }
    val currentSetUp by rememberUpdatedState(setUp)
    val currentOnPlayerCreated by rememberUpdatedState(onPlayerCreated)

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            StandardGSYVideoPlayer(ctx).also { player ->
                playerHolder[0] = player
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
                playerHolder[0]?.release()
                playerHolder[0] = null
            }
        }
    }
}
