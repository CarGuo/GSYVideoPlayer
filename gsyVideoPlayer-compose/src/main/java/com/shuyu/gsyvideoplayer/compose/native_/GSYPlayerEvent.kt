package com.shuyu.gsyvideoplayer.compose.native_

/**
 * 响应式事件总线（与 [GSYPlayerSnapshot] 互补）。
 *
 * 设计取舍：
 * - [GSYPlayerSnapshot] 描述"当前状态"——是 [kotlinx.coroutines.flow.StateFlow] / Compose State
 *   的天然形态，订阅者拿到的永远是最新值。
 * - 一些"瞬时事件"用状态描述并不合适：例如 onPrepared / onAutoComplete 这样的"边沿事件"，
 *   错过了就错过了；或者像 onError(what, extra) 这样的一次性带数据的信号——把它们塞进
 *   data class 字段会让消费方需要做"事件去重"，反而更复杂。这一类事件用
 *   [kotlinx.coroutines.flow.SharedFlow] 表达更自然。
 *
 * 因此 [GSYPlayerController] 同时暴露：
 * - `stateFlow: StateFlow<GSYPlayerSnapshot>`：状态读取（包括 Slider 进度等）
 * - `events: SharedFlow<GSYPlayerEvent>`：边沿事件订阅
 *
 * **R3 P1-3 全量映射**：覆盖 [com.shuyu.gsyvideoplayer.listener.VideoAllCallBack] 22 项中
 * 全部"语义边沿事件"，并补上来自 [com.shuyu.gsyvideoplayer.listener.GSYMediaPlayerListener]
 * 的 [BufferingProgress] / [SeekComplete] 两项底层事件。
 */
sealed class GSYPlayerEvent {
    /** [com.shuyu.gsyvideoplayer.listener.VideoAllCallBack.onStartPrepared] - 内核 setUp 后开始预备。 */
    data object StartPrepared : GSYPlayerEvent()

    /** 内核 onPrepared，已可获取 duration/videoSize 等元信息。 */
    data object Prepared : GSYPlayerEvent()

    /** 自然播放结束（不包含手动 seek 到末尾后的状态）。 */
    data object AutoComplete : GSYPlayerEvent()

    /** [com.shuyu.gsyvideoplayer.listener.VideoAllCallBack.onComplete] - 退出/重置触发的非自然结束。 */
    data object Complete : GSYPlayerEvent()

    /**
     * 播放失败。
     * @param what  原始 MediaPlayer.OnError what
     * @param extra 原始 MediaPlayer.OnError extra
     */
    data class Error(val what: Int, val extra: Int) : GSYPlayerEvent()

    /**
     * 进入"窗口层全屏"。由 [GSYPlayerController.enterFullscreen] 触发，
     * 也可能由 Java 层（直接调用 host.startWindowFullscreen）触发。
     *
     * 全屏后内核会通过反射克隆出**第二个** Player 接管渲染，原 host 暂时停泊。
     * Compose 端无需关心克隆体，Controller 已自动把内部 callback dispatcher
     * 搬到克隆体上；订阅者只需要根据本事件切换 UI 状态（隐藏小窗 UI / 显示返回按钮等）。
     */
    data object EnterFull : GSYPlayerEvent()

    /**
     * 退出"窗口层全屏"。可由 [GSYPlayerController.exitFullscreen] / 物理返回键 /
     * 系统旋转回竖屏 触发。订阅者通常据此把 `fullscreen` 标志位重置为 false。
     */
    data object QuitFull : GSYPlayerEvent()

    /** 列表"小窗模式"激活（GSY 内置悬浮小窗）。Compose 端通常不主动启用，但保留事件以便监听。 */
    data object EnterSmall : GSYPlayerEvent()

    /** 列表"小窗模式"退出。 */
    data object QuitSmall : GSYPlayerEvent()

    /** [com.shuyu.gsyvideoplayer.listener.VideoAllCallBack.onClickStartIcon] - 点击 surface 上的开始按钮。 */
    data object ClickStartIcon : GSYPlayerEvent()

    /** [com.shuyu.gsyvideoplayer.listener.VideoAllCallBack.onClickStartError] - 错误态下点击重试。 */
    data object ClickStartError : GSYPlayerEvent()

    /** [com.shuyu.gsyvideoplayer.listener.VideoAllCallBack.onClickStartThumb] - 点击封面图触发起播。 */
    data object ClickStartThumb : GSYPlayerEvent()

    /** [com.shuyu.gsyvideoplayer.listener.VideoAllCallBack.onClickResume] - 暂停态点击恢复播放。 */
    data object ClickResume : GSYPlayerEvent()

    /** [com.shuyu.gsyvideoplayer.listener.VideoAllCallBack.onClickResumeFullscreen] - 全屏内点击恢复。 */
    data object ClickResumeFullscreen : GSYPlayerEvent()

    /** [com.shuyu.gsyvideoplayer.listener.VideoAllCallBack.onClickStop] - 播放态点击暂停。 */
    data object ClickStop : GSYPlayerEvent()

    /** [com.shuyu.gsyvideoplayer.listener.VideoAllCallBack.onClickStopFullscreen] - 全屏内点击暂停。 */
    data object ClickStopFullscreen : GSYPlayerEvent()

    /** [com.shuyu.gsyvideoplayer.listener.VideoAllCallBack.onClickSeekbar] - 拖动 seekbar 完成。 */
    data object ClickSeekbar : GSYPlayerEvent()

    /** [com.shuyu.gsyvideoplayer.listener.VideoAllCallBack.onClickSeekbarFullscreen] - 全屏内拖动 seekbar 完成。 */
    data object ClickSeekbarFullscreen : GSYPlayerEvent()

    /** [com.shuyu.gsyvideoplayer.listener.VideoAllCallBack.onClickBlank] - 点击非按钮空白区域。 */
    data object ClickBlank : GSYPlayerEvent()

    /** [com.shuyu.gsyvideoplayer.listener.VideoAllCallBack.onClickBlankFullscreen] - 全屏内点击空白。 */
    data object ClickBlankFullscreen : GSYPlayerEvent()

    /** [com.shuyu.gsyvideoplayer.listener.VideoAllCallBack.onTouchScreenSeekVolume] - 手势调节音量起始。 */
    data object TouchScreenSeekVolume : GSYPlayerEvent()

    /** [com.shuyu.gsyvideoplayer.listener.VideoAllCallBack.onTouchScreenSeekPosition] - 手势调节进度起始。 */
    data object TouchScreenSeekPosition : GSYPlayerEvent()

    /** [com.shuyu.gsyvideoplayer.listener.VideoAllCallBack.onTouchScreenSeekLight] - 手势调节亮度起始。 */
    data object TouchScreenSeekLight : GSYPlayerEvent()

    /**
     * 实时缓冲百分比（来自 [com.shuyu.gsyvideoplayer.listener.GSYMediaPlayerListener.onBufferingUpdate]）。
     *
     * 与 [GSYPlayerSnapshot.bufferPercent] 的区别：
     * - Snapshot 是 500ms 轮询低频读取（适合 UI Slider 的 secondary track），可能错过抖动；
     * - 本事件是底层"边沿"实时上抛，每次内核回调一次就 emit 一次，适合需要精细缓冲监控的业务。
     *
     * @param percent 缓冲进度百分比 [0, 100]
     */
    data class BufferingProgress(val percent: Int) : GSYPlayerEvent()

    /**
     * Seek 完成（来自 [com.shuyu.gsyvideoplayer.listener.GSYMediaPlayerListener.onSeekComplete]）。
     *
     * 一个完整的 seek 序列是：UI 拖动 → controller.seekTo → 内核 mediaPlayer.seekTo →
     * onBufferingUpdate(0..100) → SeekComplete。订阅本事件可以"在 seek 完成后再做某些动作"
     * （例如统计上报、关闭 loading 浮层）。
     */
    data object SeekComplete : GSYPlayerEvent()
}
