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
 * @param setUpKey            如果非 null，当其变化时会再次调用 [setUp]（典型用法：传 url）。
 *                            为 null（默认）时仅在 factory 阶段调用一次 [setUp]，
 *                            兼容老用法、避免每次 recomposition 触发 builder.build 重置播放。
 * @param autoReleaseOnDispose 离屏时是否自动释放（默认 true）
 * @param autoPauseResume     是否自动桥接生命周期（默认 true）
 * @param onPlayerCreated     player 实例创建后回调，可用于持有引用
 */
@Composable
fun GSYVideoPlayerView(
    modifier: Modifier = Modifier,
    setUp: (StandardGSYVideoPlayer) -> Unit,
    setUpKey: Any? = null,
    autoReleaseOnDispose: Boolean = true,
    autoPauseResume: Boolean = true,
    onPlayerCreated: (StandardGSYVideoPlayer) -> Unit = {},
) {
    val playerHolder = remember { arrayOfNulls<StandardGSYVideoPlayer>(1) }
    // 用 rememberUpdatedState 保证 update 拿到的永远是最新 lambda，
    // 避免老 lambda 闭包引用过期外部变量。
    val currentSetUp by rememberUpdatedState(setUp)
    val currentOnPlayerCreated by rememberUpdatedState(onPlayerCreated)
    // 记录上一次已应用的 setUpKey，用 single-element 数组当可变 box，
    // 避免每次 recomposition 触发 setUp（factory 已调过一次）。
    val lastAppliedKey = remember { arrayOfNulls<Any>(1) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            StandardGSYVideoPlayer(ctx).also { player ->
                playerHolder[0] = player
                currentSetUp(player)
                currentOnPlayerCreated(player)
                lastAppliedKey[0] = setUpKey
            }
        },
        update = { player ->
            // setUpKey 由调用方决定是否需要触发重 build。
            // 注意：我们在 factory 已经调过一次 setUp，所以这里只在 setUpKey 真正"变了"时再调；
            // 用 setUpKey == null 时整个 update 等价 no-op，不会破坏现有 demo 行为。
            if (setUpKey != null && lastAppliedKey[0] != setUpKey) {
                lastAppliedKey[0] = setUpKey
                currentSetUp(player)
            }
        }
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
