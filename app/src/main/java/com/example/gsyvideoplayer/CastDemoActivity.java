package com.example.gsyvideoplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gsyvideoplayer.cast.DevReceiverService;
import com.example.gsyvideoplayer.databinding.ActivityCastDemoBinding;
import com.example.gsyvideoplayer.utils.DemoVideoUrls;
import com.example.gsyvideoplayer.utils.floatUtil.Util;
import com.example.gsyvideoplayer.video.SampleCastControlVideo;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.cast.CastCapability;
import com.shuyu.gsyvideoplayer.cast.dlna.JupnpDlnaProvider;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;

/**
 * M7-a-5 Cast demo activity（迁移到内核 SPI）。
 *
 * <p>本 Activity 负责：
 * <ul>
 *     <li>注册 {@link JupnpDlnaProvider} 并启动 DLNA 发现；</li>
 *     <li>提供"启用回环接收器" toggle，拉起独立 :dlna 进程内的
 *         {@link DevReceiverService}（内部通过 jUPnP 注册一台 LocalDevice + 悬浮窗渲染）；</li>
 *     <li>销毁时 stopDiscovery + disconnect，把 provider 从主进程 clean up。</li>
 * </ul>
 *
 * <p>M0 红线：本文件只在 app 层，不触碰内核源码；不改 publish.gradle / video_layout_standard.xml。
 */
public class CastDemoActivity extends AppCompatActivity {

    private static final int REQ_OVERLAY_PERMISSION = 42;

    private boolean isPlay;
    private boolean isPause;
    private OrientationUtils orientationUtils;
    private ActivityCastDemoBinding binding;

    /** UI 提示位；:dlna 进程内的服务状态才是真相，这里只是一个显示提示。 */
    private boolean loopbackReceiverEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PlayerFactory.setPlayManager(IjkPlayerManager.class);

        binding = ActivityCastDemoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.xxx1);

        binding.castPlayer.getTitleTextView().setVisibility(View.VISIBLE);
        binding.castPlayer.getBackButton().setVisibility(View.GONE);

        orientationUtils = new OrientationUtils(this, binding.castPlayer);
        orientationUtils.setEnable(false);

        new GSYVideoOptionBuilder()
                .setThumbImageView(imageView)
                .setIsTouchWiget(true)
                .setRotateViewAuto(false)
                .setLockLand(false)
                .setShowFullAnimation(false)
                .setNeedLockFull(true)
                .setUrl(DemoVideoUrls.SAMPLE_GSY)
                .setCacheWithPlay(false)
                .setVideoTitle("Cast Demo (M7-a-5)")
                .setVideoAllCallBack(new GSYSampleCallBack() {
                    @Override
                    public void onPrepared(String url, Object... objects) {
                        super.onPrepared(url, objects);
                        Debuger.printfError("***** cast demo onPrepared **** " + objects[0]);
                        orientationUtils.setEnable(binding.castPlayer.isRotateWithSystem());
                        isPlay = true;
                    }

                    @Override
                    public void onQuitFullscreen(String url, Object... objects) {
                        super.onQuitFullscreen(url, objects);
                        if (orientationUtils != null) {
                            orientationUtils.backToProtVideo();
                        }
                    }
                })
                .setLockClickListener(new LockClickListener() {
                    @Override
                    public void onClick(View view, boolean lock) {
                        if (orientationUtils != null) {
                            orientationUtils.setEnable(!lock);
                        }
                    }
                })
                .build(binding.castPlayer);

        binding.castPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orientationUtils.resolveByClick();
                binding.castPlayer.startWindowFullscreen(CastDemoActivity.this, true, true);
            }
        });

        // === M7-a-5：注册 DLNA provider + 启动发现 ===
        CastCapability cap = GSYVideoManager.instance().getCastCapability();
        cap.registerProvider(new JupnpDlnaProvider());
        cap.startDiscovery(getApplicationContext());

        setupLoopbackToggle();
    }

    /** 回环接收器 toggle，走独立 :dlna 进程 Service。 */
    private void setupLoopbackToggle() {
        binding.devReceiverToggle.setOnClickListener(v -> {
            if (loopbackReceiverEnabled) {
                DevReceiverService.stopService(CastDemoActivity.this);
                return;
            }
            // 悬浮窗跑在 :dlna 进程里，但 SYSTEM_ALERT_WINDOW 是 UID 级授权，
            // 与主进程共享。主进程先检查/引导授权，:dlna 起来后能直接用。
            if (Build.VERSION.SDK_INT >= 23 && !Util.hasPermission(this)) {
                Toast.makeText(this,
                        R.string.dev_receiver_toggle_need_overlay,
                        Toast.LENGTH_LONG).show();
                requestOverlayPermission();
                return;
            }
            DevReceiverService.startService(CastDemoActivity.this);
        });
    }

    private void requestOverlayPermission() {
        // MIUI 上 Settings.ACTION_MANAGE_OVERLAY_PERMISSION 只会打开"应用列表"而不是
        // 当前包的授权页，用户很容易迷路。检测到小米 ROM 时优先走 MIUI 私有意图
        // miui.intent.action.APP_PERM_EDITOR 直达当前包的权限编辑器；失败再退到官方 intent。
        if (isMiui() && tryOpenMiuiPermissionEditor()) {
            return;
        }
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQ_OVERLAY_PERMISSION);
        } catch (Exception e) {
            Toast.makeText(this,
                    getString(R.string.dev_receiver_toggle_status_error, e.getMessage()),
                    Toast.LENGTH_LONG).show();
        }
    }

    private boolean isMiui() {
        String manufacturer = android.os.Build.MANUFACTURER;
        return manufacturer != null && manufacturer.toLowerCase().contains("xiaomi");
    }

    private boolean tryOpenMiuiPermissionEditor() {
        try {
            Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            intent.setClassName("com.miui.securitycenter",
                    "com.miui.permcenter.permissions.PermissionsEditorActivity");
            intent.putExtra("extra_pkgname", getPackageName());
            startActivityForResult(intent, REQ_OVERLAY_PERMISSION);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_OVERLAY_PERMISSION
                && Build.VERSION.SDK_INT >= 23
                && Util.hasPermission(this)) {
            DevReceiverService.startService(this);
        }
    }

    @Override
    protected void onPause() {
        // 会话中不驱动本地 player onVideoPause，避免与远端音频双开。
        if (!binding.castPlayer.isCastSessionActive()) {
            binding.castPlayer.onVideoPause();
        }
        super.onPause();
        isPause = true;
    }

    /**
     * 接收 :dlna 进程内 {@link DevReceiverService} 的状态广播。
     * <p>为什么走 Broadcast：Service 跑在 :dlna 进程，跟主进程是两份 JVM，static Listener 字段
     * 互不可见。Broadcast 是 Android 官方推荐的"轻量级单向状态推送"跨进程通道。
     * 用 setPackage 锁定同 App 就够安全，不需要额外声明 permission。</p>
     */
    private final android.content.BroadcastReceiver mDevReceiverStateReceiver =
            new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) return;
            switch (intent.getAction()) {
                case DevReceiverService.ACTION_STATE_READY: {
                    String name = intent.getStringExtra(DevReceiverService.EXTRA_INSTANCE_NAME);
                    loopbackReceiverEnabled = true;
                    binding.devReceiverToggle.setText(R.string.dev_receiver_toggle_stop);
                    binding.devReceiverStatus.setText(getString(
                            R.string.dev_receiver_toggle_status_ready,
                            name == null ? "" : name, "0"));
                    break;
                }
                case DevReceiverService.ACTION_STATE_STOPPED: {
                    loopbackReceiverEnabled = false;
                    binding.devReceiverToggle.setText(R.string.dev_receiver_toggle_start);
                    binding.devReceiverStatus.setText(R.string.dev_receiver_toggle_status_stopped);
                    break;
                }
                case DevReceiverService.ACTION_STATE_ERROR: {
                    String reason = intent.getStringExtra(DevReceiverService.EXTRA_ERROR);
                    loopbackReceiverEnabled = false;
                    binding.devReceiverToggle.setText(R.string.dev_receiver_toggle_start);
                    binding.devReceiverStatus.setText(getString(
                            R.string.dev_receiver_toggle_status_error,
                            reason == null ? "?" : reason));
                    break;
                }
                default:
                    // ignore
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        // 用广播接收 :dlna 进程回填的 Ready/Stopped/Error 状态。
        android.content.IntentFilter f = new android.content.IntentFilter();
        f.addAction(DevReceiverService.ACTION_STATE_READY);
        f.addAction(DevReceiverService.ACTION_STATE_STOPPED);
        f.addAction(DevReceiverService.ACTION_STATE_ERROR);
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(mDevReceiverStateReceiver, f,
                    android.content.Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(mDevReceiverStateReceiver, f);
        }
    }

    @Override
    protected void onStop() {
        try {
            unregisterReceiver(mDevReceiverStateReceiver);
        } catch (Throwable ignored) {
            // 没注册过时会抛 IllegalArgumentException，忽略即可
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        if (!binding.castPlayer.isCastSessionActive()) {
            binding.castPlayer.onVideoResume(false);
        }
        super.onResume();
        isPause = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isPlay) {
            binding.castPlayer.release();
        }
        if (orientationUtils != null) {
            orientationUtils.releaseListener();
        }
        // M7-a-5：Activity 销毁时把 DLNA provider 收敛掉，避免 Registry 泄漏。
        try {
            CastCapability cap = GSYVideoManager.instance().getCastCapability();
            cap.disconnect();
            cap.stopDiscovery();
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (isPlay && !isPause) {
            binding.castPlayer.onConfigurationChanged(this, newConfig, orientationUtils, true, true);
        }
    }
}
