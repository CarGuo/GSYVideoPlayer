package com.shuyu.gsyvideoplayer.cast;

/**
 * 已建立的一次投屏会话，对齐 androidx.media3.common.Player 的核心控制方法子集。
 * <p>所有控制方法（setMediaItem/play/pause/seekTo/stop/setVolume）在调用后立即返回，
 * 具体的网络 IO 由 provider 内部丢到后台线程执行；调用方不需要额外切线程。</p>
 * <p>状态/进度更新通过 {@link SessionListener} 回调；查询方法（getState/getPositionMs/getDurationMs）
 * 返回的是最近一次轮询/事件缓存到本地的值，非阻塞、线程安全。</p>
 * <p>会话结束必须调用 {@link #release()} 释放订阅与后台任务。</p>
 */
public interface CastSession {

    /**
     * 加载并开始播放媒体（等价于 SetAVTransportURI + Play）。
     *
     * @param media 媒体信息
     */
    void setMediaItem(CastMediaInfo media);

    /** 恢复播放（Play, Speed=1）。 */
    void play();

    /** 暂停。 */
    void pause();

    /**
     * 跳转到指定位置。
     *
     * @param positionMs 目标位置，毫秒
     */
    void seekTo(long positionMs);

    /** 停止播放并保留会话。 */
    void stop();

    /**
     * 设置远端音量。
     *
     * @param volume [0f, 1f]
     */
    void setVolume(float volume);

    /** 当前状态（本地缓存，非阻塞）。 */
    CastState getState();

    /** 当前位置，毫秒；未知返回 0。 */
    long getPositionMs();

    /** 媒体时长，毫秒；未知返回 0。 */
    long getDurationMs();

    /** 添加会话监听。 */
    void addListener(SessionListener listener);

    /** 移除会话监听。 */
    void removeListener(SessionListener listener);

    /** 释放会话资源，取消订阅、停止轮询。释放后不可再用。 */
    void release();
}
