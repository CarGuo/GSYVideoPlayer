package com.shuyu.gsyvideoplayer.cast;

/**
 * 投屏状态枚举，对齐 androidx.media3.common.Player 的状态语义：
 * <ul>
 *   <li>IDLE ── 未连接 / 未加载媒体，Player.STATE_IDLE 语义</li>
 *   <li>CONNECTING ── 正在建立与远端设备的会话（DLNA subscribe / handshake）</li>
 *   <li>LOADING ── SetAVTransportURI 已发出但尚未 PLAYING</li>
 *   <li>PLAYING ── 远端正在播放，Player.STATE_READY + playWhenReady=true 语义</li>
 *   <li>PAUSED ── 远端已暂停，Player.STATE_READY + playWhenReady=false 语义</li>
 *   <li>BUFFERING ── 远端正在缓冲，Player.STATE_BUFFERING 语义</li>
 *   <li>STOPPED ── 显式 Stop / 播放结束，Player.STATE_ENDED 语义</li>
 *   <li>ERROR ── 会话失败，需查看 CastListener.onError</li>
 * </ul>
 * <p>状态迁移全部由 provider 实现在后台线程更新，回调时 postMain 到主线程。</p>
 */
public enum CastState {
    IDLE,
    CONNECTING,
    LOADING,
    PLAYING,
    PAUSED,
    BUFFERING,
    STOPPED,
    ERROR
}
