package com.shuyu.gsyvideoplayer.cast;

/**
 * 挂在 {@link CastSession} 上的会话级监听回调，仅关心当前活跃会话的状态、进度、错误。
 * <p>回调线程：约定所有实现应在主线程回调（provider 内部完成 postMain 切换），以便直接更新 UI。</p>
 * <p>与 media3 Player.Listener 对齐：状态即 CastState，位置更新用于替代 onPositionDiscontinuity + getCurrentPosition。</p>
 */
public interface SessionListener {

    /**
     * 会话状态发生变化。
     *
     * @param state 新的 {@link CastState}
     */
    void onStateChanged(CastState state);

    /**
     * 位置或时长刷新（provider 内部 1s 轮询 + GENA LastChange 事件驱动）。
     *
     * @param positionMs 当前播放位置，毫秒；未知给 0
     * @param durationMs 媒体总时长，毫秒；未知给 0
     */
    void onPositionChanged(long positionMs, long durationMs);

    /**
     * 会话内错误（Action 执行失败、订阅断连等）。
     *
     * @param error 错误对象
     */
    void onError(Throwable error);
}
