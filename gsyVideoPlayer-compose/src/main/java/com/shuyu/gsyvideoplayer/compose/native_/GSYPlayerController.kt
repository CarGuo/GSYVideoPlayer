package com.shuyu.gsyvideoplayer.compose.native_

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.listener.VideoAllCallBack
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class GSYPlayerController internal constructor() {

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
     * 一次性"边沿事件"流：onPrepared / onAutoComplete / onPlayError /
     * onEnterFullscreen / onQuitFullscreen。
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

    /**
     * 用户注入的 [VideoAllCallBack]，与内部 dispatcher 链式分发（详见 [installInternalCallback]）。
     *
     * 设计要点：内部 dispatcher 永远占据 host 的 `mVideoAllCallBack` 槽位，
     * 用户回调通过 [setUserVideoAllCallBack] 走入本字段，由 dispatcher 转发。
     * 这样既保留了 events SharedFlow 的事件流，又避免互相覆盖。
     */
    @Volatile
    private var userCallback: VideoAllCallBack? = null

    /**
     * 与 [installInternalCallback] 配对的稳定 dispatcher 实例。
     * 一次构造、跨 attach/detach 复用——避免 host 被克隆（全屏）后丢失内部 callback。
     */
    private val internalDispatcher: VideoAllCallBack = object : GSYSampleCallBack() {
        override fun onStartPrepared(url: String?, vararg objects: Any?) {
            host?.let { syncFromHost(it) }
            _events.tryEmit(GSYPlayerEvent.StartPrepared)
            try { userCallback?.onStartPrepared(url, *objects) } catch (_: Throwable) {}
        }

        override fun onPrepared(url: String?, vararg objects: Any?) {
            host?.let { syncFromHost(it) }
            _events.tryEmit(GSYPlayerEvent.Prepared)
            onPreparedListener?.invoke()
            try { userCallback?.onPrepared(url, *objects) } catch (_: Throwable) {}
        }

        override fun onAutoComplete(url: String?, vararg objects: Any?) {
            host?.let { syncFromHost(it) }
            _events.tryEmit(GSYPlayerEvent.AutoComplete)
            onCompleteListener?.invoke()
            try { userCallback?.onAutoComplete(url, *objects) } catch (_: Throwable) {}
        }

        override fun onPlayError(url: String?, vararg objects: Any?) {
            host?.let { syncFromHost(it) }
            val what = (objects.getOrNull(0) as? Int) ?: 0
            val extra = (objects.getOrNull(1) as? Int) ?: 0
            _events.tryEmit(GSYPlayerEvent.Error(what, extra))
            onErrorListener?.invoke(what, extra)
            try { userCallback?.onPlayError(url, *objects) } catch (_: Throwable) {}
        }

        override fun onEnterFullscreen(url: String?, vararg objects: Any?) {
            _events.tryEmit(GSYPlayerEvent.EnterFull)
            try { userCallback?.onEnterFullscreen(url, *objects) } catch (_: Throwable) {}
        }

        override fun onQuitFullscreen(url: String?, vararg objects: Any?) {
            _events.tryEmit(GSYPlayerEvent.QuitFull)
            try { userCallback?.onQuitFullscreen(url, *objects) } catch (_: Throwable) {}
        }

        override fun onClickStartIcon(url: String?, vararg objects: Any?) {
            _events.tryEmit(GSYPlayerEvent.ClickStartIcon)
            try { userCallback?.onClickStartIcon(url, *objects) } catch (_: Throwable) {}
        }

        override fun onClickStartError(url: String?, vararg objects: Any?) {
            _events.tryEmit(GSYPlayerEvent.ClickStartError)
            try { userCallback?.onClickStartError(url, *objects) } catch (_: Throwable) {}
        }

        override fun onClickStop(url: String?, vararg objects: Any?) {
            _events.tryEmit(GSYPlayerEvent.ClickStop)
            try { userCallback?.onClickStop(url, *objects) } catch (_: Throwable) {}
        }

        override fun onClickStopFullscreen(url: String?, vararg objects: Any?) {
            _events.tryEmit(GSYPlayerEvent.ClickStopFullscreen)
            try { userCallback?.onClickStopFullscreen(url, *objects) } catch (_: Throwable) {}
        }

        override fun onClickResume(url: String?, vararg objects: Any?) {
            _events.tryEmit(GSYPlayerEvent.ClickResume)
            try { userCallback?.onClickResume(url, *objects) } catch (_: Throwable) {}
        }

        override fun onClickResumeFullscreen(url: String?, vararg objects: Any?) {
            _events.tryEmit(GSYPlayerEvent.ClickResumeFullscreen)
            try { userCallback?.onClickResumeFullscreen(url, *objects) } catch (_: Throwable) {}
        }

        override fun onClickSeekbar(url: String?, vararg objects: Any?) {
            _events.tryEmit(GSYPlayerEvent.ClickSeekbar)
            try { userCallback?.onClickSeekbar(url, *objects) } catch (_: Throwable) {}
        }

        override fun onClickSeekbarFullscreen(url: String?, vararg objects: Any?) {
            _events.tryEmit(GSYPlayerEvent.ClickSeekbarFullscreen)
            try { userCallback?.onClickSeekbarFullscreen(url, *objects) } catch (_: Throwable) {}
        }

        override fun onQuitSmallWidget(url: String?, vararg objects: Any?) {
            _events.tryEmit(GSYPlayerEvent.QuitSmall)
            try { userCallback?.onQuitSmallWidget(url, *objects) } catch (_: Throwable) {}
        }

        override fun onEnterSmallWidget(url: String?, vararg objects: Any?) {
            _events.tryEmit(GSYPlayerEvent.EnterSmall)
            try { userCallback?.onEnterSmallWidget(url, *objects) } catch (_: Throwable) {}
        }

        override fun onTouchScreenSeekVolume(url: String?, vararg objects: Any?) {
            _events.tryEmit(GSYPlayerEvent.TouchScreenSeekVolume)
            try { userCallback?.onTouchScreenSeekVolume(url, *objects) } catch (_: Throwable) {}
        }

        override fun onTouchScreenSeekPosition(url: String?, vararg objects: Any?) {
            _events.tryEmit(GSYPlayerEvent.TouchScreenSeekPosition)
            try { userCallback?.onTouchScreenSeekPosition(url, *objects) } catch (_: Throwable) {}
        }

        override fun onTouchScreenSeekLight(url: String?, vararg objects: Any?) {
            _events.tryEmit(GSYPlayerEvent.TouchScreenSeekLight)
            try { userCallback?.onTouchScreenSeekLight(url, *objects) } catch (_: Throwable) {}
        }

        override fun onClickStartThumb(url: String?, vararg objects: Any?) {
            _events.tryEmit(GSYPlayerEvent.ClickStartThumb)
            try { userCallback?.onClickStartThumb(url, *objects) } catch (_: Throwable) {}
        }

        override fun onClickBlank(url: String?, vararg objects: Any?) {
            _events.tryEmit(GSYPlayerEvent.ClickBlank)
            try { userCallback?.onClickBlank(url, *objects) } catch (_: Throwable) {}
        }

        override fun onClickBlankFullscreen(url: String?, vararg objects: Any?) {
            _events.tryEmit(GSYPlayerEvent.ClickBlankFullscreen)
            try { userCallback?.onClickBlankFullscreen(url, *objects) } catch (_: Throwable) {}
        }

        override fun onComplete(url: String?, vararg objects: Any?) {
            _events.tryEmit(GSYPlayerEvent.Complete)
            try { userCallback?.onComplete(url, *objects) } catch (_: Throwable) {}
        }
    }

    fun setUp(builder: GSYVideoOptionBuilder, autoPlay: Boolean = false) {
        pendingBuilder = builder
        this.autoPlay = autoPlay
        // 若已有缓存的 cachePath，覆盖到 builder（与 P1-5 setCachePath 语义一致）
        pendingCachePath?.let { builder.setCachePath(it) }
        host?.let { player ->
            applyBuilder(player)
            installInternalCallback(player)
            installLowLevelHooks(player)
            reapplyPendingSetters(player)
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

    /**
     * 注入用户级 [VideoAllCallBack]，与内部 dispatcher **链式分发**：
     * - events SharedFlow 仍照常 emit；
     * - [setOnPrepared] / [setOnComplete] / [setOnError] 三个老 setter 仍照常触发；
     * - 你这里给的回调会在内部回调之后被调用，能拿到原始 22 项语义事件。
     *
     * 与历史 API 的差异：**禁止**直接在 host 上 `setVideoAllCallBack(yourCb)`——那会把
     * dispatcher 顶掉，导致 events / setOnXxx 全部失效。请改用本入口。
     *
     * 传 `null` 表示卸载用户回调，dispatcher 自身仍然挂在 host 上。
     *
     * @param callback 用户实现的 [VideoAllCallBack]（可继承 [GSYSampleCallBack] 只重写关心的项）
     */
    fun setUserVideoAllCallBack(callback: VideoAllCallBack?) {
        userCallback = callback
    }

    /**
     * **逃生口（Escape Hatch）**：以"主线程 + released 安全 + null 安全"的方式
     * 直接访问底层 [com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer] 实例，
     * 调用 Compose 端尚未封装的方法（字幕 / 镜像 / 滤镜 / setSeekOnStart / 列表小窗 / 截图 / GIF 等）。
     *
     * **使用约束**：
     * - 必须在主线程调用（内部不再切线程，避免与 GSY 内核线程模型冲突）；
     * - controller 已 `release()` 后调用直接返回 `null`，block 不会执行；
     * - **禁止**在 block 里调用 `player.setVideoAllCallBack(...)`——会把链式 dispatcher
     *   顶掉，导致 events / setOnXxx 全部失效。需要回调请改用 [setUserVideoAllCallBack]。
     *
     * 用法示例：
     * ```kotlin
     * // 设置外挂字幕（Compose 端尚未封装直 setter）
     * controller.withHost { player ->
     *     player.setSubTitle("https://example.com/sub.srt")
     * }
     *
     * // 拍快照
     * val bmp = controller.withHost { player -> player.bitmap }
     * ```
     *
     * @return block 的返回值；若 controller 已 released 或 host 尚未 attach 则返回 `null`
     */
    fun <R> withHost(block: (com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer) -> R): R? {
        if (Looper.myLooper() !== Looper.getMainLooper()) {
            throw IllegalStateException(
                "GSYPlayerController.withHost 必须在主线程调用：" +
                    "GSYVideoView 内核 (Surface / Handler / OrientationUtils) 都在主线程操作。",
            )
        }
        if (released) return null
        val player = host ?: return null
        return block(player)
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
            try {
                old.setBufferingHook(null)
                old.setSeekCompleteHook(null)
            } catch (_: Throwable) {
            }
        }
        host = player
        applyBuilder(player)
        installInternalCallback(player)
        installLowLevelHooks(player)
        reapplyPendingSetters(player)
        startTicking()
        if (autoPlay) {
            handler.post {
                if (!released) host?.startPlayLogic()
            }
        }
    }

    /**
     * 把稳定的 [internalDispatcher] 挂到 host 上。
     *
     * 注意：dispatcher 是单例字段，既不会在 `setUp` / `attachHost` 反复 new，
     * 也不会因为外部调 `host.setVideoAllCallBack(null)` 而被重建——
     * 只是会被剥离一次后等下一次 `attachHost` / `setUp` 重新挂回。
     */
    private fun installInternalCallback(player: GSYComposeHostPlayer) {
        player.setVideoAllCallBack(internalDispatcher)
    }

    /**
     * 安装 [GSYComposeHostPlayer] 的两个底层 hook，把 [com.shuyu.gsyvideoplayer.listener.GSYMediaPlayerListener]
     * 中 VideoAllCallBack 不覆盖的 buffer / seek 边沿信号，转成 [GSYPlayerEvent.BufferingProgress] /
     * [GSYPlayerEvent.SeekComplete] 两类事件 emit 到 [events]。
     *
     * Hook 在 [attachHost] 时安装，[detachHost] 时清理；全屏克隆体由 GSY 内核走 attach 接管，
     * 因此克隆期间 hook 自动跟随 host 切换、不会丢事件。
     */
    private fun installLowLevelHooks(player: GSYComposeHostPlayer) {
        player.setBufferingHook { percent ->
            _events.tryEmit(GSYPlayerEvent.BufferingProgress(percent.coerceIn(0, 100)))
        }
        player.setSeekCompleteHook {
            _events.tryEmit(GSYPlayerEvent.SeekComplete)
        }
    }

    internal fun detachHost() {
        stopTicking()
        host?.let { old ->
            try {
                old.setVideoAllCallBack(null)
            } catch (_: Throwable) {
            }
            try {
                old.setBufferingHook(null)
                old.setSeekCompleteHook(null)
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
        // P1-4 扩展字段：SAR / 网速 / 缓存命中。这些 API 在 host detached / 内核未就绪时
        // 可能抛或返回 0，以 runCatching 兜底，保证 tick 不被 player 内部状态错位带崩。
        val sarNum = runCatching { player.videoSarNum }.getOrDefault(0)
        val sarDen = runCatching { player.videoSarDen }.getOrDefault(0)
        val netSpeed = runCatching { player.netSpeed }.getOrDefault(0L)
        val netSpeedText = runCatching { player.netSpeedText }.getOrDefault("")
        val cacheReady = runCatching {
            GSYVideoManager.instance()?.isCacheFile == true
        }.getOrDefault(false)
        val next = GSYPlayerSnapshot(
            state = state,
            currentPosition = cur,
            duration = duration,
            bufferPercent = buffer,
            isPlaying = state == GSYPlayState.Playing || state == GSYPlayState.Buffering,
            videoWidth = player.currentVideoWidth,
            videoHeight = player.currentVideoHeight,
            videoSarNum = sarNum,
            videoSarDen = sarDen,
            netSpeed = netSpeed,
            netSpeedText = netSpeedText,
            isCacheReady = cacheReady,
            speed = currentSpeed,
            isLocked = locked,
        )
        _snapshot.value = next
        // 双写守恒：stateFlow 与 snapshot 在同一行同步赋值（且整个 syncFromHost 由
        // tickRunnable 在主线程 post 调用），二者数据值永远 ===；订阅模型的 commit
        // 时机略有差异（Compose snapshot 在下一帧提交，StateFlow 立即可见），属语义
        // 而非数据漂移。详见 README "Compose 状态读取" 一节。
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

    /**
     * 进入"窗口层全屏"——复用 GSY 内核的 [com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer.startWindowFullscreen]。
     *
     * 内核会反射克隆出**第二个** [GSYComposeHostPlayer] 作为全屏播放器，原 host 暂时停泊；
     * 由于 [installInternalCallback] 注入的是稳定 dispatcher 实例，且 Java 全屏管线会自动
     * 把 `mVideoAllCallBack` 一并复制到克隆体上，所以全屏期间 events / userCallback 仍然能
     * 正常触发。
     *
     * **使用约束**：必须在主线程；`activity` 必须是当前持有 [GSYPlayerSurface] 的 Activity。
     *
     * @param activity     宿主 Activity，用来挂全屏 ContentView
     * @param hideActionBar 是否隐藏 ActionBar
     * @param hideStatusBar 是否隐藏 StatusBar
     */
    fun enterFullscreen(activity: Activity, hideActionBar: Boolean = true, hideStatusBar: Boolean = true) {
        if (released) return
        val p = host ?: return
        if (p.isIfCurrentIsFullscreen) return
        p.startWindowFullscreen(activity, hideActionBar, hideStatusBar)
    }

    /**
     * 退出"窗口层全屏"——委托给 [GSYVideoManager.backFromWindowFull]，与原生
     * [com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer.backFromFull] 走同一通路。
     *
     * 退出后 [GSYPlayerEvent.QuitFull] 会自动 emit 到 [events]。
     *
     * @return 是否真正执行了退出（false 表示当前并不在全屏态）
     */
    fun exitFullscreen(activity: Activity): Boolean {
        if (released) return false
        return GSYVideoManager.backFromWindowFull(activity)
    }

    /** 当前是否处于全屏态（直接读取 host）。 */
    val isFullscreen: Boolean
        get() = host?.isIfCurrentIsFullscreen == true

    // ==================== R3 P1-5 ｜ 直 setter（builder-only 字段的 controller 直访入口） ====================
    //
    // GSY 内核很多字段历史上仅在 [GSYVideoOptionBuilder] 中以"链式 builder"方式暴露——这意味着：
    // - setUp 之后想动态调整（比如 prepared 后再切换循环），只能通过 host 直访 / withHost 闭包；
    // - 这些字段语义稳定（与 mediaPlayer 配置 + UI 语义一一对应），适合在 controller 上提供
    //   一组幂等、null-safe、released-safe 的直 setter，免去用户每次都 withHost 的样板。
    //
    // 设计准则：
    // - 主线程门：与 [withHost] 一致，避免内核 Handler 与 OrientationUtils 混线程；
    // - released no-op：release 后所有 setter 不抛异常、不报错，保持组件解构安全；
    // - 字段值同步缓存到 controller 上，attach 新 host 时（含全屏克隆体）自动 reapply，避免丢配置。

    private var pendingHeaders: Map<String, String>? = null
    private var pendingCachePath: java.io.File? = null
    private var pendingSeekOnStart: Long = -1L
    private var pendingLooping: Boolean? = null
    private var pendingStartAfterPrepared: Boolean? = null
    private var pendingOverrideExtension: String? = null
    private var pendingShowPauseCover: Boolean? = null
    private var pendingReleaseWhenLossAudio: Boolean? = null

    private fun requireMainThread(name: String) {
        if (Looper.myLooper() !== Looper.getMainLooper()) {
            throw IllegalStateException(
                "GSYPlayerController.$name 必须在主线程调用：" +
                    "GSYVideoView 内核 (Surface / Handler / OrientationUtils) 都在主线程操作。",
            )
        }
    }

    /**
     * 设置 HTTP 请求头，对应 [com.shuyu.gsyvideoplayer.video.base.GSYVideoView.setMapHeadData]。
     * 用于鉴权 token / referer / UA 等场景。
     */
    fun setHeaders(headers: Map<String, String>?) {
        requireMainThread("setHeaders")
        if (released) return
        pendingHeaders = headers
        host?.setMapHeadData(headers)
    }

    /**
     * 设置缓存目录，对应 [com.shuyu.gsyvideoplayer.video.base.GSYVideoView.setUp] 第三参数 `cachePath`。
     * 已 setUp 时调用本 setter 会立即用同 url + cacheWithPlay + cachePath 重新 setUp。
     *
     * 注意：cachePath 在 GSY 内核上是"setUp 时一次性传入"——不是动态字段。本 setter 仅缓存值，
     * 下一次 [setUp] 自动写入；若已经 attach 且有 builder，则用同 builder + 新 cachePath 重新 setUp。
     */
    fun setCachePath(cachePath: java.io.File?) {
        requireMainThread("setCachePath")
        if (released) return
        pendingCachePath = cachePath
        // builder 已经存在的话，把字段写回 builder 让下一次 setUp 生效
        pendingBuilder?.setCachePath(cachePath)
    }

    /**
     * 设置 prepared 之后自动 seek 到的位置，对应 [com.shuyu.gsyvideoplayer.video.base.GSYVideoView.setSeekOnStart]。
     * 列表分页 / 历史进度恢复场景常用。
     */
    fun setSeekOnStart(positionMs: Long) {
        requireMainThread("setSeekOnStart")
        if (released) return
        pendingSeekOnStart = positionMs
        host?.setSeekOnStart(positionMs)
    }

    /**
     * 设置循环播放，对应 [com.shuyu.gsyvideoplayer.video.base.GSYVideoView.setLooping]。
     */
    fun setLooping(looping: Boolean) {
        requireMainThread("setLooping")
        if (released) return
        pendingLooping = looping
        host?.setLooping(looping)
    }

    /**
     * 设置 prepared 后是否自动起播，对应 [com.shuyu.gsyvideoplayer.video.base.GSYVideoView.setStartAfterPrepared]。
     * 默认 true；置为 false 可实现"预加载 + 待用户点击"模式。
     */
    fun setStartAfterPrepared(startAfterPrepared: Boolean) {
        requireMainThread("setStartAfterPrepared")
        if (released) return
        pendingStartAfterPrepared = startAfterPrepared
        host?.setStartAfterPrepared(startAfterPrepared)
    }

    /**
     * 设置 ExoPlayer 内核覆盖的扩展名（如 hls / dash），对应
     * [com.shuyu.gsyvideoplayer.video.base.GSYVideoView.setOverrideExtension]。
     * 仅 ExoPlayer 内核生效。
     */
    fun setOverrideExtension(overrideExtension: String?) {
        requireMainThread("setOverrideExtension")
        if (released) return
        pendingOverrideExtension = overrideExtension
        host?.setOverrideExtension(overrideExtension)
    }

    /**
     * 设置暂停后是否显示最后一帧封面，对应 [com.shuyu.gsyvideoplayer.video.base.GSYVideoView.setShowPauseCover]。
     */
    fun setShowPauseCover(showPauseCover: Boolean) {
        requireMainThread("setShowPauseCover")
        if (released) return
        pendingShowPauseCover = showPauseCover
        host?.setShowPauseCover(showPauseCover)
    }

    /**
     * 设置是否在丢失音频焦点时 release 内核，对应
     * [com.shuyu.gsyvideoplayer.video.base.GSYVideoView.setReleaseWhenLossAudio]。
     */
    fun setReleaseWhenLossAudio(releaseWhenLossAudio: Boolean) {
        requireMainThread("setReleaseWhenLossAudio")
        if (released) return
        pendingReleaseWhenLossAudio = releaseWhenLossAudio
        host?.setReleaseWhenLossAudio(releaseWhenLossAudio)
    }

    /**
     * 把 [P1-5] 缓存的 setter 值在 attach 新 host（含全屏克隆体）时一次性 reapply。
     * 由 [attachHost] 内部调用，对外不暴露。
     */
    private fun reapplyPendingSetters(player: GSYComposeHostPlayer) {
        pendingHeaders?.let { player.setMapHeadData(it) }
        if (pendingSeekOnStart >= 0) player.setSeekOnStart(pendingSeekOnStart)
        pendingLooping?.let { player.setLooping(it) }
        pendingStartAfterPrepared?.let { player.setStartAfterPrepared(it) }
        pendingOverrideExtension?.let { player.setOverrideExtension(it) }
        pendingShowPauseCover?.let { player.setShowPauseCover(it) }
        pendingReleaseWhenLossAudio?.let { player.setReleaseWhenLossAudio(it) }
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
                old.setBufferingHook(null)
                old.setSeekCompleteHook(null)
            } catch (_: Throwable) {
            }
            try {
                old.release()
            } catch (_: Throwable) {
            }
        }
        host = null
        userCallback = null
        onErrorListener = null
        onCompleteListener = null
        onPreparedListener = null
    }
}
