package com.shuyu.gsyvideoplayer.compose.native_

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

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
    val snapshot: State<GSYPlayerSnapshot> = _snapshot

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

    fun setOnError(listener: ((what: Int, extra: Int) -> Unit)?) {
        onErrorListener = listener
    }

    fun setOnComplete(listener: (() -> Unit)?) {
        onCompleteListener = listener
    }

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
                onCompleteListener?.invoke()
            }

            override fun onPrepared(url: String?, vararg objects: Any?) {
                host?.let { syncFromHost(it) }
                onPreparedListener?.invoke()
            }

            override fun onPlayError(url: String?, vararg objects: Any?) {
                host?.let { syncFromHost(it) }
                val what = (objects.getOrNull(0) as? Int) ?: 0
                val extra = (objects.getOrNull(1) as? Int) ?: 0
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
        val state = when (player.currentStatePublic) {
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
        _snapshot.value = GSYPlayerSnapshot(
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
    }

    fun play() {
        host?.startPlayLogic()
    }

    fun togglePlayPause() {
        val p = host ?: return
        when (p.currentStatePublic) {
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
