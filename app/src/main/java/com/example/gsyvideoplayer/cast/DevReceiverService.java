package com.example.gsyvideoplayer.cast;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.gsyvideoplayer.CastDemoActivity;
import com.example.gsyvideoplayer.R;

import org.jupnp.UpnpServiceImpl;
import org.jupnp.android.AndroidRouter;
import org.jupnp.android.AndroidUpnpServiceConfiguration;
import org.jupnp.model.meta.LocalDevice;
import org.jupnp.protocol.ProtocolFactory;
import org.jupnp.transport.Router;

/**
 * M7-a-5 Loopback DevReceiver：独立进程（{@code android:process=":dlna"}）foreground service。
 *
 * <p>核心动作：
 * <ol>
 *   <li>构造 {@link UpnpServiceImpl}（用 {@link AndroidUpnpServiceConfiguration} +
 *       {@link AndroidRouter}，保证在 Android 上能开 multicast lock）；</li>
 *   <li>用 {@link LoopbackDeviceFactory} 组装一台 MediaRenderer:1 {@link LocalDevice}
 *       并加入 {@code registry}；</li>
 *   <li>AVTransport action 触发时会调 {@link LoopbackAvTransportService}，后者转发到
 *       同进程内的 {@link CastReceiverFloatingWindow}（因为 :dlna 进程有自己的
 *       WindowManager + IJK 单例，跟主进程 sender 完全隔离）。</li>
 * </ol>
 *
 * <p>为什么走独立进程：主进程 {@code JupnpDlnaProvider} 已经 {@code bind}
 * {@code AndroidUpnpServiceImpl}，如果 loopback 也起在主进程会有一份"自己发现自己"的 SSDP 环，
 * 但更严重的是 jUPnP 的 UDP socket 端口在同进程只能被一份 stack 占，冲突后接收端 M-SEARCH
 * 收不到。独立进程后各拥各的 socket / Registry，天然干净。
 *
 * <p>线程契约：
 * <ul>
 *   <li>Service 生命周期在 :dlna 主线程；</li>
 *   <li>jUPnP 内部 IO 用自己的线程池；</li>
 *   <li>AVTransport action → CastReceiverFloatingWindow.post*，由后者切回主线程 UI。</li>
 * </ul>
 */
public final class DevReceiverService extends Service {

    private static final String TAG = "DevReceiverService";

    private static final String NOTIFICATION_CHANNEL_ID = "gsy_cast_dev_receiver";
    private static final int NOTIFICATION_ID = 47989;

    public static final String ACTION_START = "com.example.gsyvideoplayer.cast.ACTION_DEV_START";
    public static final String ACTION_STOP  = "com.example.gsyvideoplayer.cast.ACTION_DEV_STOP";

    // ---- 跨进程状态广播 ----
    // :dlna 进程往主进程回填 Ready/Stopped/Error，必须走 Intent。Broadcast 用显式包名 +
    // 私有 action，只有本 App 的 receiver 能收到（不用 permission 是因为 setPackage 已经把
    // 广播锁到本 UID 内）。EXTRA_INSTANCE_NAME/EXTRA_ERROR 是负载。
    public static final String ACTION_STATE_READY =
            "com.example.gsyvideoplayer.cast.ACTION_DEV_STATE_READY";
    public static final String ACTION_STATE_STOPPED =
            "com.example.gsyvideoplayer.cast.ACTION_DEV_STATE_STOPPED";
    public static final String ACTION_STATE_ERROR =
            "com.example.gsyvideoplayer.cast.ACTION_DEV_STATE_ERROR";
    public static final String EXTRA_INSTANCE_NAME = "instanceName";
    public static final String EXTRA_ERROR         = "error";

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable private UpnpServiceImpl upnpService;
    @Nullable private LocalDevice localDevice;

    // ---- UI 状态回调（跨进程无法直接调用，主进程只在自己的进程内看到 null；这里只做本进程日志） ----

    public interface Listener {
        void onLoopbackReady(String instanceName, int port);
        void onLoopbackStopped();
        void onLoopbackError(String reason);
    }

    private static volatile Listener sListener;

    /** 跨进程注意：这个 static 只在调用方的进程内生效。主进程 CastDemoActivity 挂的 listener 在 :dlna
     *  进程内是看不到的（不同进程 = 不同静态区）。M7-a-5 内 UI 状态回填暂时靠日志判断，
     *  真需要 UI 显示时应改用 LocalBroadcast / bindService。 */
    public static void setListener(@Nullable Listener listener) {
        sListener = listener;
    }

    // ---- Service lifecycle ----

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate pid=" + android.os.Process.myPid());
        ensureNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : ACTION_START;
        if (ACTION_STOP.equals(action)) {
            stopSelfClean();
            return START_NOT_STICKY;
        }
        startForegroundInternal(getString(R.string.dev_receiver_notif_starting));
        // jUPnP 的 startup + registry.addDevice 都会做网络 IO（描述文档生成等），放后台线程。
        new Thread(this::startUpnpStack, "gsy-loopback-boot").start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        stopUpnpStack();
        mainHandler.post(CastReceiverFloatingWindow::destroy);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // ---- UPnP stack ----

    private void startUpnpStack() {
        try {
            final Context appContext = getApplicationContext();
            AndroidUpnpServiceConfiguration configuration = new AndroidUpnpServiceConfiguration() {
                @Override
                public int getRegistryMaintenanceIntervalMillis() {
                    return 7000;
                }
            };
            upnpService = new UpnpServiceImpl(configuration) {
                @Override
                protected Router createRouter(ProtocolFactory protocolFactory,
                                              org.jupnp.registry.Registry registry) {
                    return new AndroidRouter(getConfiguration(), protocolFactory, appContext);
                }
            };
            // UpnpServiceImpl 默认构造不会 startup —— 需要显式调
            upnpService.startup();

            String friendly = getString(R.string.loopback_renderer_friendly_name, Build.MODEL);
            String manufacturer = getString(R.string.loopback_renderer_manufacturer);
            String modelName = getString(R.string.loopback_renderer_model_name);
            String modelDesc = getString(R.string.loopback_renderer_model_description);

            localDevice = LoopbackDeviceFactory.create(appContext,
                    friendly, manufacturer, modelName, modelDesc);
            upnpService.getRegistry().addDevice(localDevice);

            Log.i(TAG, "LocalDevice registered udn=" + localDevice.getIdentity().getUdn()
                    + " friendly=" + friendly);
            updateNotification(getString(R.string.dev_receiver_notif_ready,
                    friendly, "SSDP"));
            broadcastState(ACTION_STATE_READY, EXTRA_INSTANCE_NAME, friendly);
            Listener l = sListener;
            if (l != null) l.onLoopbackReady(friendly, 0);
        } catch (Throwable t) {
            Log.e(TAG, "startUpnpStack failed", t);
            emitError(t.getClass().getSimpleName() + ": " + t.getMessage());
            stopSelf();
        }
    }

    private void stopUpnpStack() {
        try {
            if (localDevice != null && upnpService != null) {
                upnpService.getRegistry().removeDevice(localDevice);
                localDevice = null;
            }
            if (upnpService != null) {
                upnpService.shutdown();
                upnpService = null;
            }
        } catch (Throwable t) {
            Log.w(TAG, "stopUpnpStack error: " + t);
        }
        Listener l = sListener;
        if (l != null) l.onLoopbackStopped();
        broadcastState(ACTION_STATE_STOPPED, null, null);
    }

    private void stopSelfClean() {
        stopUpnpStack();
        mainHandler.post(CastReceiverFloatingWindow::destroy);
        stopForeground(true);
        stopSelf();
    }

    private void emitError(String reason) {
        Log.w(TAG, "loopback error: " + reason);
        updateNotification(getString(R.string.dev_receiver_notif_error, reason));
        Listener l = sListener;
        if (l != null) l.onLoopbackError(reason);
        broadcastState(ACTION_STATE_ERROR, EXTRA_ERROR, reason);
    }

    /**
     * 跨进程往主进程广播状态。用 setPackage 锁定同 App，
     * 无需声明 permission。参数是 payload key/value，可为 null。
     */
    private void broadcastState(String action, @Nullable String extraKey, @Nullable String extraValue) {
        try {
            Intent i = new Intent(action);
            i.setPackage(getPackageName());
            if (extraKey != null) i.putExtra(extraKey, extraValue == null ? "" : extraValue);
            sendBroadcast(i);
        } catch (Throwable t) {
            Log.w(TAG, "broadcastState failed action=" + action, t);
        }
    }

    // ---- Notification ----

    private void ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) return;
            NotificationChannel ch = nm.getNotificationChannel(NOTIFICATION_CHANNEL_ID);
            if (ch != null) return;
            NotificationChannel c = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    getString(R.string.dev_receiver_notif_channel_name),
                    NotificationManager.IMPORTANCE_LOW);
            c.setDescription(getString(R.string.dev_receiver_notif_channel_desc));
            nm.createNotificationChannel(c);
        }
    }

    private void startForegroundInternal(String text) {
        Notification n = buildNotification(text);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, n,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(NOTIFICATION_ID, n);
        }
    }

    private void updateNotification(String text) {
        NotificationManager nm = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        nm.notify(NOTIFICATION_ID, buildNotification(text));
    }

    private Notification buildNotification(String text) {
        Intent open = new Intent(this, CastDemoActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pi = PendingIntent.getActivity(this, 0, open, flags);

        Intent stop = new Intent(this, DevReceiverService.class);
        stop.setAction(ACTION_STOP);
        PendingIntent stopPi = PendingIntent.getService(this, 1, stop, flags);

        NotificationCompat.Builder b = new NotificationCompat.Builder(this,
                NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.dev_receiver_notif_title))
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setSmallIcon(android.R.drawable.stat_notify_sync_noanim)
                .setContentIntent(pi)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .addAction(0, getString(R.string.dev_receiver_notif_action_stop), stopPi);
        return b.build();
    }

    // ---- Static helpers ----

    public static void startService(Context context) {
        Intent i = new Intent(context, DevReceiverService.class);
        i.setAction(ACTION_START);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(i);
        } else {
            context.startService(i);
        }
    }

    public static void stopService(Context context) {
        Intent i = new Intent(context, DevReceiverService.class);
        i.setAction(ACTION_STOP);
        context.startService(i);
    }
}
