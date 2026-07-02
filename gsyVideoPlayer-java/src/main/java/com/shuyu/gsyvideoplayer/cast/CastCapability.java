package com.shuyu.gsyvideoplayer.cast;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 内核层投屏能力入口（final 单例）。
 * <p>该类只做「聚合与分发」，不包含任何具体协议（DLNA/Chromecast/…）的实现代码；
 * 协议实现由外部注册的 {@link CastProvider} 提供。</p>
 * <p>典型使用流程（app 层）：
 * <pre>
 *     CastCapability cap = GSYVideoManager.instance().getCastCapability();
 *     cap.registerProvider(new JupnpDlnaProvider());
 *     cap.addListener(myListener);
 *     // 拿到 devices，选一个后：
 *     cap.connect(device, connectCallback);
 * </pre>
 * <p>线程规约：本类所有 public 方法都可以在主线程调用；provider 负责在自己维护的后台线程执行
 * 网络 IO，然后 postMain 回调到 CastCapability 聚合再向外分发。CastCapability 不做主线程切换，
 * 因为约定 provider 就已经保证在主线程回调。</p>
 * <p>与 androidx.media3.common.Player 语义的对齐：CastCapability ≈ 「Player 池」，
 * ActiveSession ≈ 「当前 Player」，Provider ≈ 「渲染后端」，CastListener/SessionListener ≈ Player.Listener。</p>
 */
public final class CastCapability {

    private static volatile CastCapability sInstance;

    /** DCL 单例，首次调用不会自动注册任何 provider。 */
    public static CastCapability getInstance() {
        if (sInstance == null) {
            synchronized (CastCapability.class) {
                if (sInstance == null) {
                    sInstance = new CastCapability();
                }
            }
        }
        return sInstance;
    }

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /** protocol -> provider（LinkedHashMap 保证遍历顺序稳定）。 */
    private final Map<String, CastProvider> providers = new LinkedHashMap<>();

    /** provider protocol -> 最近一次上报的设备列表，用于聚合。 */
    private final Map<String, List<CastDevice>> providerDevices = new LinkedHashMap<>();

    private final List<CastListener> listeners = new CopyOnWriteArrayList<>();

    private volatile CastSession activeSession;

    private final SessionListener sessionRelayListener = new SessionListener() {
        @Override
        public void onStateChanged(CastState state) {
            dispatchSessionState(activeSession, state);
        }

        @Override
        public void onPositionChanged(long positionMs, long durationMs) {
            // capability 层不上报进度，交给 SessionListener 直接观察 session
        }

        @Override
        public void onError(Throwable error) {
            dispatchError(error);
        }
    };

    private CastCapability() {
    }

    /**
     * 注册一个 provider（例如 DLNA）。同一 protocol 重复注册会覆盖旧值。
     * <p>该方法本身不启动发现；调用方需要再调用 {@link #startDiscovery(Context)}。</p>
     */
    public synchronized void registerProvider(CastProvider provider) {
        if (provider == null || provider.getProtocol() == null) {
            throw new IllegalArgumentException("provider or protocol null");
        }
        providers.put(provider.getProtocol(), provider);
    }

    /** 反注册 provider，同时停止其发现。 */
    public synchronized void unregisterProvider(String protocol) {
        CastProvider p = providers.remove(protocol);
        if (p != null) {
            try {
                p.stopDiscovery();
            } catch (Throwable ignored) {
            }
        }
        providerDevices.remove(protocol);
        notifyDeviceListChanged();
    }

    /** 触发所有已注册 provider 开始发现。 */
    public synchronized void startDiscovery(final Context context) {
        for (final CastProvider provider : providers.values()) {
            final String protocol = provider.getProtocol();
            provider.startDiscovery(context, new CastProvider.DiscoveryListener() {
                @Override
                public void onDeviceListChanged(List<CastDevice> devices) {
                    synchronized (CastCapability.this) {
                        providerDevices.put(protocol,
                                devices == null ? Collections.<CastDevice>emptyList() : new ArrayList<>(devices));
                    }
                    notifyDeviceListChanged();
                }

                @Override
                public void onError(Throwable error) {
                    dispatchError(error);
                }
            });
        }
    }

    /** 停止所有 provider 的发现。 */
    public synchronized void stopDiscovery() {
        for (CastProvider provider : providers.values()) {
            try {
                provider.stopDiscovery();
            } catch (Throwable ignored) {
            }
        }
    }

    /** 获取当前所有 provider 聚合后的设备列表（不可修改快照）。 */
    public synchronized List<CastDevice> getAvailableDevices() {
        List<CastDevice> out = new ArrayList<>();
        for (List<CastDevice> list : providerDevices.values()) {
            if (list != null) {
                out.addAll(list);
            }
        }
        return Collections.unmodifiableList(out);
    }

    /**
     * 连接指定设备。会自动 disconnect 掉当前活跃会话。
     *
     * @param device   目标设备
     * @param callback 用户业务回调，主线程
     */
    public void connect(final CastDevice device, final CastProvider.ConnectCallback callback) {
        if (device == null) {
            if (callback != null) callback.onError(new IllegalArgumentException("device null"));
            return;
        }
        final CastProvider provider;
        synchronized (this) {
            provider = providers.get(device.getProtocol());
        }
        if (provider == null) {
            Throwable e = new IllegalStateException("no provider for protocol " + device.getProtocol());
            if (callback != null) callback.onError(e);
            dispatchError(e);
            return;
        }
        disconnect();
        provider.connect(device, new CastProvider.ConnectCallback() {
            @Override
            public void onConnected(CastSession session) {
                setActiveSession(session);
                if (callback != null) callback.onConnected(session);
            }

            @Override
            public void onError(Throwable error) {
                if (callback != null) callback.onError(error);
                dispatchError(error);
            }
        });
    }

    /** 断开当前活跃会话（release）。无活跃会话时无操作。 */
    public synchronized void disconnect() {
        if (activeSession != null) {
            try {
                activeSession.removeListener(sessionRelayListener);
                activeSession.release();
            } catch (Throwable ignored) {
            }
            activeSession = null;
            dispatchSessionState(null, CastState.IDLE);
        }
    }

    /** 当前活跃会话，可能为 null。 */
    public CastSession getActiveSession() {
        return activeSession;
    }

    public void addListener(CastListener listener) {
        if (listener != null) listeners.add(listener);
    }

    public void removeListener(CastListener listener) {
        listeners.remove(listener);
    }

    private synchronized void setActiveSession(CastSession session) {
        activeSession = session;
        if (session != null) {
            session.addListener(sessionRelayListener);
        }
    }

    private void notifyDeviceListChanged() {
        final List<CastDevice> snapshot = getAvailableDevices();
        runOnMain(new Runnable() {
            @Override
            public void run() {
                for (CastListener l : listeners) {
                    try {
                        l.onDeviceListChanged(snapshot);
                    } catch (Throwable ignored) {
                    }
                }
            }
        });
    }

    private void dispatchSessionState(final CastSession session, final CastState state) {
        runOnMain(new Runnable() {
            @Override
            public void run() {
                for (CastListener l : listeners) {
                    try {
                        l.onSessionStateChanged(session, state);
                    } catch (Throwable ignored) {
                    }
                }
            }
        });
    }

    private void dispatchError(final Throwable error) {
        runOnMain(new Runnable() {
            @Override
            public void run() {
                for (CastListener l : listeners) {
                    try {
                        l.onError(error);
                    } catch (Throwable ignored) {
                    }
                }
            }
        });
    }

    private void runOnMain(Runnable r) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            r.run();
        } else {
            mainHandler.post(r);
        }
    }
}
