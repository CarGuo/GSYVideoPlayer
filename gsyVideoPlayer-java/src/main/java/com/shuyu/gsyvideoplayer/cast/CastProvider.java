package com.shuyu.gsyvideoplayer.cast;

import android.content.Context;

import java.util.List;

/**
 * 具体投屏协议的 SPI，例如 DLNA、Chromecast、AirPlay 各自实现一个 Provider。
 * <p>Provider 内部负责所有与协议相关的网络 IO，全部约定跑在自己维护的后台线程／线程池，
 * 严禁在主线程调用 http/socket；对外的 {@link DiscoveryListener} 与
 * {@link ConnectCallback} 回调必须 postMain。</p>
 * <p>Provider 由 app 层显式注册到 {@link CastCapability}，便于按需 bind/unbind 系统服务（例如
 * DLNA 需要 bind AndroidUpnpService），从而精细控制生命周期。</p>
 */
public interface CastProvider {

    /** 协议标识，全小写，例如 "dlna"、"chromecast"，与 {@link CastDevice#getProtocol()} 对齐。 */
    String getProtocol();

    /**
     * 启动设备发现。实现方在后台线程订阅协议事件（SSDP/mDNS/…），
     * 每次设备增减都在后台线程整理后 postMain 触发 {@link DiscoveryListener#onDeviceListChanged}。
     *
     * @param context  Android Context，用于 bind Service 等系统能力
     * @param listener 设备列表回调，主线程
     */
    void startDiscovery(Context context, DiscoveryListener listener);

    /** 停止设备发现，释放订阅、解绑 Service。 */
    void stopDiscovery();

    /**
     * 与目标设备建立会话。实现方内部完成 subscribe/handshake 之后回调 ConnectCallback，
     * 建立过程可能包含网络 IO，全部走 provider 后台线程；回调走主线程。
     *
     * @param device   目标设备（必须来自本 provider 发现的列表）
     * @param callback 连接回调，主线程
     * @return 会话对象，具体实现负责状态迁移
     */
    CastSession connect(CastDevice device, ConnectCallback callback);

    /** 设备发现监听（主线程回调）。 */
    interface DiscoveryListener {
        void onDeviceListChanged(List<CastDevice> devices);

        void onError(Throwable error);
    }

    /** 连接建立回调（主线程回调）。 */
    interface ConnectCallback {
        void onConnected(CastSession session);

        void onError(Throwable error);
    }
}
