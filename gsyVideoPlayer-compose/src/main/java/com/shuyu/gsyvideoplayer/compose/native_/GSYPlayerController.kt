package com.shuyu.gsyvideoplayer.compose.native_

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class GSYPlayerController internal constructor(
    private val context: Context,
) {

    internal var host: GSYComposeHostPlayer? = null
        private set

    private val handler = Handler(Looper.getMainLooper())

    @Volatile
    private var ticking: Boolean = false

    @Volatile
    private var released: Boolean = false

    private val tickRunnable = object : Runnable {
        override fun run() {
            if (!ticking || released) return
            host?.let { syncFromHost(it) }
            if (ticking && !released) {
                handler.postDelayed(this, 500)
            }
        }
    }

    private val _snapshot = mutableStateOf(GSYPlayerSnapshot())

    /**
     * Compose 友好的状态读取入口（保留兼容性）。
     *
     * 该字段同时被 [stateFlow] 镜像。两者数据来源同一处 [syncFromHost]，
     * 在 Compose 上下文里按需直接消费 `snapshot`；在协程上下文里推荐 [stateFlow]。
     */
    val snapshot: State<GSYPlayerSnapshot> = _snapshot

    private val _stateFlow = MutableStateFlow(GSYPlayerSnapshot())

    /**
     * 与 [snapshot] 等价的 Coroutine 形态。订阅者总能拿到最新状态，
     * 适合在 ViewModel / 业务层用 `collect` 跟踪播放状态。
     */
    val stateFlow: StateFlow<GSYPlayerSnapshot> = _stateFlow.asStateFlow()

    /**
     * 一次性"边沿事件"流：onPrepared / onAutoComplete / onPlayError。
     *
     * - replay = 0：订阅前发生的事件不会重放（错过即错过，这是边沿事件的语义）。
     * - extraBufferCapacity = 16 + DROP_OLDEST：避免 emit 阻塞，极端情况只会丢老的。
     * - 调用 `controller.events.collect { event -> ... }` 即可订阅。
     *
     * 与已废弃的 [setOnError] / [setOnComplete] / [setOnPrepared] 互不冲突，
     * 同一事件会同时触发"setter 单 listener"与"flow 多订阅者"两边。
     */
    private val _events = MutableSharedFlow<GSYPlayerEvent>(
        replay = 0,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<GSYPlayerEvent> = _events.asSharedFlow()

    private var pendingBuilder: GSYVideoOptionBuilder? = null
    private var currentSpeed: Float = 1f
    private var locked: Boolean = false
    private var autoPlay: Boolean = false

    private var onErrorListener: ((what: Int, extra: Int) -> Unit)? = null
    private var onCompleteListener: (() -> Unit)? = null
    private var onPreparedListener: (() -> Unit)? = null

    fun setUp(builder: GSYVideoOptionBuilder, autoPlay: Boolean = false) {
        pendingBuilder = builder
        this.autoPlay = autoPlay
        host?.let { player ->
            applyBuilder(player)
            installInternalCallback(player)
            if (autoPlay) {
                handler.post {
                    if (!released) host?.startPlayLogic()
                }
            }
        }
    }

    fun setUp(
        url: String,
        cacheWithPlay: Boolean = false,
        title: String = "",
        autoPlay: Boolean = false,
    ) {
        val b = GSYVideoOptionBuilder()
            .setUrl(url)
            .setCacheWithPlay(cacheWithPlay)
            .setVideoTitle(title)
        setUp(b, autoPlay)
    }

    @Deprecated(
        message = "改用响应式 events: SharedFlow<GSYPlayerEvent>。" +
            "示例：LaunchedEffect(controller) { controller.events.collect { if (it is GSYPlayerEvent.Error) ... } }",
        replaceWith = ReplaceWith("events.collect { if (it is GSYPlayerEvent.Error) listener(it.what, it.extra) }"),
    )
    fun setOnError(listener: ((what: Int, extra: Int) -> Unit)?) {
        onErrorListener = listener
    }

    @Deprecated(
        message = "改用响应式 events: SharedFlow<GSYPlayerEvent>。" +
            "示例：LaunchedEffect(controller) { controller.events.collect { if (it is GSYPlayerEvent.AutoComplete) ... } }",
        replaceWith = ReplaceWith("events.collect { if (it is GSYPlayerEvent.AutoComplete) listener?.invoke() }"),
    )
    fun setOnComplete(listener: (() -> Unit)?) {
        onCompleteListener = listener
    }

    @Deprecated(
        message = "改用响应式 events: SharedFlow<GSYPlayerEvent>。" +
            "示例：LaunchedEffect(controller) { controller.events.collect { if (it is GSYPlayerEvent.Prepared) ... } }",
        replaceWith = ReplaceWith("events.collect { if (it is GSYPlayerEvent.Prepared) listener?.invoke() }"),
    )
    fun setOnPrepared(listener: (() -> Unit)?) {
        onPreparedListener = listener
    }

    internal fun attachHost(player: GSYComposeHostPlayer) {
        if (released) return
        if (host === player) {
            startTicking()
            return
        }
        host?.let { old ->
            try {
                old.setVideoAllCallBack(null)
            } catch (_: Throwable) {
            }
        }
        host = player
        applyBuilder(player)
        installInternalCallback(player)
        startTicking()
        if (autoPlay) {
            handler.post {
                if (!released) host?.startPlayLogic()
            }
        }
    }

    private fun installInternalCallback(player: GSYComposeHostPlayer) {
        player.setVideoAllCallBack(object : GSYSampleCallBack() {
            override fun onAutoComplete(url: String?, vararg objects: Any?) {
                host?.let { syncFromHost(it) }
                _events.tryEmit(GSYPlayerEvent.AutoComplete)
                onCompleteListener?.invoke()
            }

            override fun onPrepared(url: String?, vararg objects: Any?) {
                host?.let { syncFromHost(it) }
                _events.tryEmit(GSYPlayerEvent.Prepared)
                onPreparedListener?.invoke()
            }

            override fun onPlayError(url: String?, vararg objects: Any?) {
                host?.let { syncFromHost(it) }
                val what = (objects.getOrNull(0) as? Int) ?: 0
                val extra = (objects.getOrNull(1) as? Int) ?: 0
                _events.tryEmit(GSYPlayerEvent.Error(what, extra))
                onErrorListener?.invoke(what, extra)
            }
        })
    }

    internal fun detachHost() {
        stopTicking()
        host?.let { old ->
            try {
                old.setVideoAllCallBack(null)
            } catch (_: Throwable) {
            }
        }
        host = null
    }

    private fun applyBuilder(player: GSYComposeHostPlayer) {
        pendingBuilder?.build(player)
    }

    private fun startTicking() {
        if (released) return
        if (ticking) return
        ticking = true
        handler.removeCallbacks(tickRunnable)
        handler.post(tickRunnable)
    }

    private fun stopTicking() {
        ticking = false
        handler.removeCallbacks(tickRunnable)
    }

    private fun syncFromHost(player: GSYComposeHostPlayer) {
        val state = when (player.currentState) {
            GSYVideoView.CURRENT_STATE_NORMAL -> GSYPlayState.Idle
            GSYVideoView.CURRENT_STATE_PREPAREING -> GSYPlayState.Preparing
            GSYVideoView.CURRENT_STATE_PLAYING -> GSYPlayState.Playing
            GSYVideoView.CURRENT_STATE_PLAYING_BUFFERING_START -> GSYPlayState.Buffering
            GSYVideoView.CURRENT_STATE_PAUSE -> GSYPlayState.Paused
            GSYVideoView.CURRENT_STATE_AUTO_COMPLETE -> GSYPlayState.Completed
            GSYVideoView.CURRENT_STATE_ERROR -> GSYPlayState.Error
            else -> GSYPlayState.Idle
        }
        val duration = player.duration.coerceAtLeast(0L)
        val cur = player.currentPositionWhenPlaying.coerceAtLeast(0L).coerceAtMost(duration)
        val buffer = player.buffterPoint.coerceIn(0, 100)
        val next = GSYPlayerSnapshot(
            state = state,
            currentPosition = cur,
            duration = duration,
            bufferPercent = buffer,
            isPlaying = state == GSYPlayState.Playing || state == GSYPlayState.Buffering,
            videoWidth = player.currentVideoWidth,
            videoHeight = player.currentVideoHeight,
            speed = currentSpeed,
            isLocked = locked,
        )
        _snapshot.value = next
        _stateFlow.value = next
    }

    fun play() {
        host?.startPlayLogic()
    }

    fun togglePlayPause() {
        val p = host ?: return
        when (p.currentState) {
            GSYVideoView.CURRENT_STATE_PLAYING -> p.onVideoPause()
            GSYVideoView.CURRENT_STATE_PAUSE -> p.onVideoResume()
            GSYVideoView.CURRENT_STATE_AUTO_COMPLETE -> p.startPlayLogic()
            GSYVideoView.CURRENT_STATE_NORMAL,
            GSYVideoView.CURRENT_STATE_ERROR -> p.startPlayLogic()
            else -> {}
        }
    }

    fun pause() {
        host?.onVideoPause()
    }

    fun resume() {
        host?.onVideoResume()
    }

    fun seekTo(positionMs: Long) {
        val p = host ?: return
        val duration = p.duration.coerceAtLeast(0L)
        val target = if (duration > 0L) positionMs.coerceIn(0L, duration) else positionMs.coerceAtLeast(0L)
        p.seekTo(target)
    }

    fun seekRelative(deltaMs: Long) {
        val p = host ?: return
        val cur = p.currentPositionWhenPlaying
        seekTo(cur + deltaMs)
    }

    fun setSpeed(speed: Float, soundTouch: Boolean = true) {
        currentSpeed = speed
        host?.setSpeedPlaying(speed, soundTouch)
        host?.let { syncFromHost(it) }
    }

    fun setLocked(locked: Boolean) {
        this.locked = locked
        host?.let { syncFromHost(it) }
    }

    fun retry() {
        host?.startPlayLogic()
    }

    fun release() {
        if (released) return
        released = true
        stopTicking()
        handler.removeCallbacksAndMessages(null)
        host?.let { old ->
            try {
                old.setVideoAllCallBack(null)
            } catch (_: Throwable) {
            }
            try {
                old.release()
            } catch (_: Throwable) {
            }
        }
        host = null
        onErrorListener = null
        onCompleteListener = null
        onPreparedListener = null
    }
}
