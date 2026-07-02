package com.shuyu.gsyvideoplayer.cast;

import java.util.List;

/**
 * 挂在 {@link CastCapability} 上的全局监听器，关心的是设备列表变化、当前活跃会话状态、跨会话的错误。
 * <p>回调线程：约定所有实现应在主线程回调（由 {@link CastCapability} 内部完成 postMain 切换）。</p>
 * <p>与 {@link SessionListener} 的区别：CastListener 关心的是 capability 级、全局层面的事件，
 * SessionListener 更贴近某一次投屏会话的进度／状态刷新。</p>
 */
public interface CastListener {

    /**
     * 可用设备列表更新。列表可能来源于多个 provider 聚合。
     *
     * @param devices 最新的设备快照，不可修改
     */
    void onDeviceListChanged(List<CastDevice> devices);

    /**
     * 当前活跃会话状态发生变化。若目前没有活跃会话，session 可能为 null 且 state=IDLE。
     *
     * @param session 关联的会话对象，可为 null
     * @param state   新的 {@link CastState}
     */
    void onSessionStateChanged(CastSession session, CastState state);

    /**
     * 顶层错误（包括 provider 发现失败、连接失败等）。
     *
     * @param error 错误对象
     */
    void onError(Throwable error);
}
