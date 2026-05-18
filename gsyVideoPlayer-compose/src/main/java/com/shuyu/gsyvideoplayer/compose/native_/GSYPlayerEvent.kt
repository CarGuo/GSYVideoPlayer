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
 */
sealed class GSYPlayerEvent {
    /** 内核 onPrepared，已可获取 duration/videoSize 等元信息。 */
    data object Prepared : GSYPlayerEvent()

    /** 自然播放结束（不包含手动 seek 到末尾后的状态）。 */
    data object AutoComplete : GSYPlayerEvent()

    /**
     * 播放失败。
     * @param what  原始 MediaPlayer.OnError what
     * @param extra 原始 MediaPlayer.OnError extra
     */
    data class Error(val what: Int, val extra: Int) : GSYPlayerEvent()
}
