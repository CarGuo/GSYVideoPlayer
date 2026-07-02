package com.shuyu.gsyvideoplayer.cast.dlna;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.shuyu.gsyvideoplayer.cast.CastDevice;
import com.shuyu.gsyvideoplayer.cast.CastProvider;
import com.shuyu.gsyvideoplayer.cast.CastSession;

import org.jupnp.android.AndroidUpnpService;
import org.jupnp.android.AndroidUpnpServiceImpl;
import org.jupnp.model.message.header.STAllHeader;
import org.jupnp.model.meta.Device;
import org.jupnp.model.meta.LocalDevice;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.types.UDAServiceType;
import org.jupnp.registry.Registry;
import org.jupnp.registry.RegistryListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * DLNA / UPnP provider（基于 jUPnP 3.0.3）。
 * <p>使用 {@link AndroidUpnpServiceImpl} 通过 bindService 挂起 UPnP 后台栈，
 * 用 {@link RegistryListener} 观察 UDA1.0 MediaRenderer 设备（filter 采用 AVTransport service type）。</p>
 * <p>所有网络 IO、设备解析都在 jUPnP 内部工作线程；本 provider 再通过 {@link ExecutorService} 收敛为
 * 单线程处理设备列表增删，最后 postMain 触发 DiscoveryListener。</p>
 * <p>protocol 标识固定为 "dlna"。</p>
 */
public class JupnpDlnaProvider implements CastProvider {

    private static final String TAG = "JupnpDlnaProvider";
    public static final String PROTOCOL = "dlna";

    private static final UDAServiceType AV_TRANSPORT = new UDAServiceType("AVTransport");

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor();

    /** UDN string -> CastDevice snapshot */
    private final Map<String, CastDevice> known = new LinkedHashMap<>();

    private Context appContext;
    private AndroidUpnpService upnpService;
    private DiscoveryListener listener;
    private boolean bound;

    private final RegistryListener registryListener = new RegistryListener() {
        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
            notifyError(ex);
        }

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            onDeviceAdded(device);
        }

        @Override
        public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
            onDeviceAdded(device);
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            onDeviceRemoved(device);
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
        }

        @Override
        public void beforeShutdown(Registry registry) {
        }

        @Override
        public void afterShutdown() {
        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            worker.submit(new Runnable() {
                @Override
                public void run() {
                    upnpService = (AndroidUpnpService) service;
                    // AndroidUpnpServiceImpl 的 onCreate 只构造 UpnpServiceImpl 不 startup，
                    // 需要主动 startup 才会开 SSDP UDP 1900 + 拿 multicast lock。
                    try {
                        upnpService.get().startup();
                        Log.i(TAG, "UpnpService startup() ok");
                    } catch (Throwable t) {
                        Log.e(TAG, "UpnpService startup() FAILED", t);
                        notifyError(new RuntimeException("UpnpService startup failed", t));
                        return;
                    }
                    upnpService.getRegistry().addListener(registryListener);
                    // 提前把已经在 registry 里的设备灌进来
                    for (Device<?, ?, ?> d : upnpService.getRegistry().getDevices()) {
                        if (d instanceof RemoteDevice) {
                            onDeviceAdded((RemoteDevice) d);
                        }
                    }
                    // 主动发一次 M-SEARCH
                    try {
                        upnpService.getControlPoint().search(new STAllHeader());
                        Log.i(TAG, "M-SEARCH sent");
                    } catch (Throwable t) {
                        Log.e(TAG, "M-SEARCH failed", t);
                    }
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            upnpService = null;
        }
    };

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public void startDiscovery(Context context, DiscoveryListener listener) {
        this.appContext = context.getApplicationContext();
        this.listener = listener;
        if (!bound) {
            bound = appContext.bindService(
                    new Intent(appContext, AndroidUpnpServiceImpl.class),
                    serviceConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void stopDiscovery() {
        worker.submit(new Runnable() {
            @Override
            public void run() {
                if (upnpService != null) {
                    try {
                        upnpService.getRegistry().removeListener(registryListener);
                    } catch (Throwable ignored) {
                    }
                }
                if (bound && appContext != null) {
                    try {
                        appContext.unbindService(serviceConnection);
                    } catch (Throwable ignored) {
                    }
                    bound = false;
                }
                upnpService = null;
                known.clear();
            }
        });
    }

    @Override
    public CastSession connect(CastDevice device, ConnectCallback callback) {
        Log.i(TAG, "connect() called for device " + (device == null ? "null" : device.getName())
                + " upnpService=" + (upnpService == null ? "NULL" : "OK"));
        if (upnpService == null) {
            if (callback != null) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(new IllegalStateException("UPnP service not bound"));
                    }
                });
            }
            return null;
        }
        Object raw = device.getRaw();
        Log.i(TAG, "connect() raw type=" + (raw == null ? "null" : raw.getClass().getSimpleName()));
        if (!(raw instanceof RemoteDevice)) {
            if (callback != null) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(new IllegalArgumentException("device raw is not RemoteDevice"));
                    }
                });
            }
            return null;
        }
        final JupnpDlnaSession session = new JupnpDlnaSession(upnpService, (RemoteDevice) raw);
        Log.i(TAG, "connect() session created, posting onConnected");
        if (callback != null) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onConnected(session);
                }
            });
        }
        return session;
    }

    // ---- helpers ----

    private void onDeviceAdded(final RemoteDevice device) {
        worker.submit(new Runnable() {
            @Override
            public void run() {
                if (device.findService(AV_TRANSPORT) == null) {
                    return;
                }
                String udn = device.getIdentity().getUdn().getIdentifierString();
                String name = device.getDetails() != null && device.getDetails().getFriendlyName() != null
                        ? device.getDetails().getFriendlyName()
                        : device.getDisplayString();
                String host = "";
                int port = 0;
                if (device.getIdentity().getDescriptorURL() != null) {
                    host = device.getIdentity().getDescriptorURL().getHost();
                    port = device.getIdentity().getDescriptorURL().getPort();
                }
                CastDevice cast = new CastDevice(udn, name, host, port, PROTOCOL, device);
                known.put(udn, cast);
                notifyListChanged();
            }
        });
    }

    private void onDeviceRemoved(final RemoteDevice device) {
        worker.submit(new Runnable() {
            @Override
            public void run() {
                String udn = device.getIdentity().getUdn().getIdentifierString();
                if (known.remove(udn) != null) {
                    notifyListChanged();
                }
            }
        });
    }

    private void notifyListChanged() {
        final List<CastDevice> snapshot = new ArrayList<>(known.values());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onDeviceListChanged(Collections.unmodifiableList(snapshot));
                }
            }
        });
    }

    private void notifyError(final Exception ex) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onError(ex);
                }
            }
        });
    }
}
