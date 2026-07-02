package com.example.gsyvideoplayer.video;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.gsyvideoplayer.R;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.cast.CastCapability;
import com.shuyu.gsyvideoplayer.cast.CastDevice;
import com.shuyu.gsyvideoplayer.cast.CastListener;
import com.shuyu.gsyvideoplayer.cast.CastMediaInfo;
import com.shuyu.gsyvideoplayer.cast.CastProvider;
import com.shuyu.gsyvideoplayer.cast.CastSession;
import com.shuyu.gsyvideoplayer.cast.CastState;
import com.shuyu.gsyvideoplayer.cast.SessionListener;
import com.shuyu.gsyvideoplayer.cast.dlna.JupnpDlnaProvider;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * M7-a-5 Cast demo player, ported to the internal Cast SPI
 * ({@link CastCapability} + {@link JupnpDlnaProvider}). All legacy private-protocol
 * plumbing (GSYCastModule / mDNS / manual host+port + polling) is gone; the entry now
 * pops a DLNA renderer list sourced from {@link CastCapability#getAvailableDevices()} and
 * status flows in via {@link SessionListener} — no more sender-side polling.
 *
 * <p>Manual IP fallback is preserved for LAN debugging: we synthesise a {@link CastDevice}
 * whose protocol is {@link JupnpDlnaProvider#PROTOCOL} so the same provider handles it.
 */
public class SampleCastControlVideo extends StandardGSYVideoPlayer {

    private static final String TAG = "SampleCastControlVideo";

    public static final String VERSION = "M7-a-5";

    private TextView mCastEntry;
    private TextView mCastStatusChip;

    /** Devices reported by CastCapability, refreshed via {@link CastListener}. */
    // 注意：GSY 父类的 super() 会先调 init(Context)，那时子类的 instance field 初始化还没跑。
    // 因此这里不能给 final 初始值，而是在 init() 里再 new。全项目 SampleXxxVideo 都遵循此约定。
    private List<CastDevice> mDevices;

    /** Reflects the active DLNA session lifecycle. Only true between successful setMediaItem and disconnect. */
    private volatile boolean mCastSession;
    private volatile CastDevice mActiveDevice;
    private volatile CastState mLastState = CastState.IDLE;

    private static final ExecutorService CAST_EXECUTOR =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "gsy-cast-sender");
                t.setDaemon(true);
                return t;
            });

    private Handler mMain;

    private CastListener mCapListener;

    private SessionListener mSessionListener;

    /**
     * 当前活跃 session 的强引用。仅供 detach / disconnect 时 removeListener 使用，防止匿名内部类
     * 通过 CopyOnWriteArrayList 持 Activity Context 泄漏。connect 成功时赋值，release 时清空。
     */
    private CastSession mBoundSession;

    // ------------------------------------------------------------------
    // Remote-control overlay: sender collapses into a pure remote UI once cast starts.
    // 本地 IJK 全部 release()，surface_container 隐藏，overlay VISIBLE。
    // ------------------------------------------------------------------
    private View mCastRemoteOverlay;
    private TextView mCastRemoteHeader;
    private TextView mCastRemoteDevice;
    private ImageView mCastRemotePlayPause;
    private SeekBar mCastRemoteProgress;
    private TextView mCastRemoteCurrent;
    private TextView mCastRemoteTotal;
    private SeekBar mCastRemoteVolume;

    /** Playback progress recorded when the user started casting, used to resume local play on disconnect. */
    private volatile long mLastLocalPositionMs = 0L;
    /** Latest remote progress polled from the receiver via jUPnP (1Hz). Used as seek target on resume. */
    private volatile long mRemoteLastPositionMs = 0L;
    private volatile long mRemoteLastDurationMs = 0L;
    /** Guard flag: while user is dragging the remote seekbar we suppress the 1Hz auto write-back. */
    private volatile boolean mRemoteSeekbarUserDragging = false;
    /** Set to true after we release local IJK, so onDisconnected knows it needs to rebuild + seek. */
    private volatile boolean mLocalReleasedForCast = false;

    /**
     * 用户点 play/pause 按钮后的 icon "乐观翻转抑制窗口" 到期时间戳（uptimeMillis）。
     * <p>问题：点了 pause，我们本地立刻把 icon 画成 play；但下一次 1Hz 轮询回来时远端可能
     * 还没应用 Pause，状态仍是 PLAYING，会把 icon 又翻回去。用户看到"闪一下"。
     * <p>方案：本地手动翻转后开 1500ms 抑制窗口，期间轮询回调只更新进度、不覆盖 icon；
     * 之后远端权威状态生效。</p>
     */
    private volatile long mIconOptimisticUntilMs = 0L;
    private static final long ICON_OPTIMISTIC_WINDOW_MS = 1500L;

    private void ensureCastFields() {
        // 首次调用（父类 init 期间）把 non-final 字段全部实例化，避免 super() → init(Context) NPE。
        if (mDevices == null) {
            mDevices = new ArrayList<>();
        }
        if (mMain == null) {
            mMain = new Handler(Looper.getMainLooper());
        }
        if (mCapListener == null) {
            mCapListener = new CastListener() {
                @Override
                public void onDeviceListChanged(List<CastDevice> devices) {
                    mDevices.clear();
                    if (devices != null) {
                        mDevices.addAll(devices);
                    }
                }

                @Override
                public void onSessionStateChanged(CastSession session, CastState state) {
                    mLastState = state == null ? CastState.IDLE : state;
                    if (mLastState == CastState.IDLE || mLastState == CastState.STOPPED
                            || mLastState == CastState.ERROR) {
                        // 远端主动结束 / 播完 / 出错：如果本实例正处于塌陷态，必须把 overlay 撤下
                        // 并把本地播放器接回来。否则 surface_container 会露出裸黑框。
                        final boolean wasCollapsed = mLocalReleasedForCast;
                        mCastSession = false;
                        if (wasCollapsed) {
                            mMain.post(SampleCastControlVideo.this::handleUnexpectedRemoteEnd);
                        } else {
                            mMain.post(SampleCastControlVideo.this::applyCastEntryUi);
                        }
                    } else {
                        applyCastEntryUi();
                    }
                }

                @Override
                public void onError(Throwable error) {
                    if (getContext() == null) return;
                    Toast.makeText(getContext().getApplicationContext(),
                            getContext().getString(R.string.cast_toast_error,
                                    error == null ? "?" : error.getMessage()),
                            Toast.LENGTH_LONG).show();
                }
            };
        }
        if (mSessionListener == null) {
            mSessionListener = new SessionListener() {
                @Override
                public void onStateChanged(CastState state) {
                    mLastState = state == null ? CastState.IDLE : state;
                    applyCastEntryUi();
                }

                @Override
                public void onPositionChanged(long positionMs, long durationMs) {
                    // 缓存最近一次远端进度，供断开时 seek 恢复本地播放
                    mRemoteLastPositionMs = positionMs;
                    mRemoteLastDurationMs = durationMs;
                    final CastState s = mLastState;
                    final String label = stateLabel(s) + " · "
                            + (positionMs / 1000) + "/" + (durationMs / 1000) + "s";
                    mMain.post(() -> {
                        if (mCastStatusChip != null && mCastSession) {
                            mCastStatusChip.setVisibility(View.VISIBLE);
                            mCastStatusChip.setText(label);
                        }
                        // 遥控 overlay 进度回写（用户拖动时不回写，避免抢焦点）
                        if (mCastRemoteOverlay != null
                                && mCastRemoteOverlay.getVisibility() == View.VISIBLE
                                && !mRemoteSeekbarUserDragging) {
                            updateRemoteOverlayProgress(positionMs, durationMs);
                        }
                    });
                }

                @Override
                public void onError(Throwable error) {
                    // 会话级错误：会话置无效，如果处于塌陷态必须走完整恢复流程，不能只清 flag。
                    Log.e(TAG, "SessionListener.onError", error);
                    final boolean wasCollapsed = mLocalReleasedForCast;
                    mCastSession = false;
                    if (wasCollapsed) {
                        mMain.post(SampleCastControlVideo.this::handleUnexpectedRemoteEnd);
                    } else {
                        mMain.post(SampleCastControlVideo.this::applyCastEntryUi);
                    }
                }
            };
        }
    }

    public SampleCastControlVideo(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public SampleCastControlVideo(Context context) {
        super(context);
    }

    public SampleCastControlVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        ensureCastFields();
        super.init(context);
        initCastEntry();
        initCastRemoteOverlay();
        CastCapability cap = GSYVideoManager.instance().getCastCapability();
        cap.addListener(mCapListener);
        // 冷启动时若外部已经在跑 discovery，把当前设备列表拉一次。
        mDevices.clear();
        mDevices.addAll(cap.getAvailableDevices());
    }

    private void initCastEntry() {
        mCastEntry = (TextView) findViewById(R.id.cast_entry);
        mCastStatusChip = (TextView) findViewById(R.id.cast_status_chip);
        if (mCastEntry == null) {
            return;
        }
        mCastEntry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCastSession) {
                    showCastSessionMenu();
                } else {
                    showCastEntryDialog();
                }
            }
        });
        applyCastEntryUi();
    }

    /**
     * 遥控 UI 初始化。所有控件默认 gone，直到 {@link #enterRemoteMode(CastDevice)} 触发才 visible。
     * 交互约定（对齐 Google Cast SDK 的 ExpandedController）：
     *   - 中央大按钮：play/pause，走 {@link #forwardPlay()}/{@link #forwardPause()} → session.play()/pause()
     *   - 进度条 max=1000：用户拖动 → forwardSeek(progress * durationMs / 1000)
     *   - 音量条 max=100：走 session.setVolume(progress / 100f) → RenderingControl.SetVolume SOAP
     *   - 断开按钮：走 disconnectFromReceiver(true)
     */
    private void initCastRemoteOverlay() {
        mCastRemoteOverlay = findViewById(R.id.cast_remote_overlay);
        if (mCastRemoteOverlay == null) {
            return;
        }
        mCastRemoteHeader = (TextView) findViewById(R.id.cast_remote_header);
        mCastRemoteDevice = (TextView) findViewById(R.id.cast_remote_device);
        mCastRemotePlayPause = (ImageView) findViewById(R.id.cast_remote_play_pause);
        mCastRemoteProgress = (SeekBar) findViewById(R.id.cast_remote_progress);
        mCastRemoteCurrent = (TextView) findViewById(R.id.cast_remote_current);
        mCastRemoteTotal = (TextView) findViewById(R.id.cast_remote_total);
        mCastRemoteVolume = (SeekBar) findViewById(R.id.cast_remote_volume);
        View disconnect = findViewById(R.id.cast_remote_disconnect);

        if (mCastRemotePlayPause != null) {
            mCastRemotePlayPause.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 目前用 mLastState 来判定是否处于播放态；点一下就翻转。
                    // 同时开抑制窗口——避免刚点完立刻被 1Hz stale 轮询覆盖回原状态。
                    mIconOptimisticUntilMs = android.os.SystemClock.uptimeMillis()
                            + ICON_OPTIMISTIC_WINDOW_MS;
                    if (mLastState == CastState.PLAYING) {
                        forwardPause();
                        mCastRemotePlayPause.setImageResource(com.shuyu.gsyvideoplayer.R.drawable.video_click_play_selector);
                    } else {
                        forwardPlay();
                        mCastRemotePlayPause.setImageResource(com.shuyu.gsyvideoplayer.R.drawable.video_click_pause_selector);
                    }
                }
            });
        }

        if (mCastRemoteProgress != null) {
            mCastRemoteProgress.setMax(1000);
            mCastRemoteProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser && mRemoteLastDurationMs > 0 && mCastRemoteCurrent != null) {
                        long previewMs = (long) progress * mRemoteLastDurationMs / 1000L;
                        mCastRemoteCurrent.setText(formatDurationMs(previewMs));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    mRemoteSeekbarUserDragging = true;
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mRemoteSeekbarUserDragging = false;
                    if (mRemoteLastDurationMs > 0) {
                        long targetMs = (long) seekBar.getProgress() * mRemoteLastDurationMs / 1000L;
                        forwardSeek(targetMs);
                    }
                }
            });
        }

        if (mCastRemoteVolume != null) {
            mCastRemoteVolume.setMax(100);
            // 初始值：50%（RenderingControl.GetVolume 目前未接，稍后可补）
            mCastRemoteVolume.setProgress(50);
            mCastRemoteVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (!fromUser) return;
                    forwardVolume(progress / 100f);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { }
            });
        }

        if (disconnect != null) {
            disconnect.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    disconnectFromReceiver(true);
                }
            });
        }

        // fullscreen 切换或 Activity 重建时，塌陷字段已迁移到本实例。若上一份实例正处于
        // 遥控态（mLocalReleasedForCast=true），我们要立刻把 overlay 顶起来，不让 surface
        // container 露出黑框——本地 IJK 已经被上一份实例 release 掉了。
        if (mLocalReleasedForCast && mCastSession) {
            Log.i(TAG, "initCastRemoteOverlay: inheriting collapsed state, re-attaching overlay");
            View surfaceContainer = findViewById(R.id.surface_container);
            if (surfaceContainer != null) surfaceContainer.setVisibility(View.GONE);
            mCastRemoteOverlay.setVisibility(View.VISIBLE);
            if (mCastRemoteDevice != null && mActiveDevice != null) {
                mCastRemoteDevice.setText(mActiveDevice.getName() == null ? "" : mActiveDevice.getName());
            }
            updateRemoteOverlayProgress(mRemoteLastPositionMs, mRemoteLastDurationMs);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.sample_cast_video;
    }

    @Override
    public GSYBaseVideoPlayer startWindowFullscreen(Context context, boolean actionBar, boolean statusBar) {
        SampleCastControlVideo fullscreen = (SampleCastControlVideo) super.startWindowFullscreen(context, actionBar, statusBar);
        // 会话级字段：让全屏实例继承本实例的全部投屏视图状态。
        // 未来任何新增的 cast-related field 都必须在此处补齐——否则全屏后断投必黑屏。
        migrateCastStateTo(fullscreen);
        return fullscreen;
    }

    @Override
    protected void resolveNormalVideoShow(View oldF, ViewGroup vp, GSYVideoPlayer gsyVideoPlayer) {
        super.resolveNormalVideoShow(oldF, vp, gsyVideoPlayer);
        if (gsyVideoPlayer instanceof SampleCastControlVideo) {
            SampleCastControlVideo from = (SampleCastControlVideo) gsyVideoPlayer;
            from.migrateCastStateTo(this);
        }
    }

    /**
     * 把当前实例上所有与"投屏塌陷/恢复"相关的可变字段迁移到目标实例。
     * 一次性完成，避免历史上"少拷一个字段就黑屏"的类型 bug 反复复发。
     * <p>迁移后：本实例的塌陷标志被清零（因为塌陷所有权已交给 target），target 会在
     * onAttachedToWindow 后根据这些字段决定是否 enterRemoteMode。</p>
     */
    private void migrateCastStateTo(SampleCastControlVideo target) {
        if (target == null) return;
        target.mCastSession = mCastSession;
        target.mActiveDevice = mActiveDevice;
        target.mLastState = mLastState;
        target.mLastLocalPositionMs = mLastLocalPositionMs;
        target.mRemoteLastPositionMs = mRemoteLastPositionMs;
        target.mRemoteLastDurationMs = mRemoteLastDurationMs;
        target.mLocalReleasedForCast = mLocalReleasedForCast;
        // session 强引用要一并交接，避免旧实例 detach 时把还在用的 listener 摘掉
        // ——同时旧实例把引用清零，防止旧 Activity 走 detach 时误 remove target 的 listener。
        if (mBoundSession != null) {
            // 迁移 listener 归属：从旧实例摘掉，挂到新实例的 mSessionListener 上。
            try {
                mBoundSession.removeListener(mSessionListener);
            } catch (Throwable t) {
                Log.w(TAG, "migrate: removeListener on old instance failed", t);
            }
            target.mBoundSession = mBoundSession;
            try {
                mBoundSession.addListener(target.mSessionListener);
            } catch (Throwable t) {
                Log.w(TAG, "migrate: addListener on new instance failed", t);
            }
            mBoundSession = null;
        }
        // 本实例交出塌陷所有权，避免 detach 时对已迁走的 overlay 再做一次 exit。
        mLocalReleasedForCast = false;
    }

    /** Discovery dialog. Shows CastCapability's current devices; last option is a manual IP fallback. */
    private void showCastEntryDialog() {
        // Snapshot 最新一次设备列表——CastCapability 已经保证在主线程回调，我们本地缓存即可。
        final List<CastDevice> devices = new ArrayList<>(mDevices);
        final String scanning = getContext().getString(R.string.cast_dialog_scanning);
        final String manualLabel = getContext().getString(R.string.cast_dialog_manual_entry);

        final int itemCount = devices.size() + 1;
        final CharSequence[] items = new CharSequence[itemCount];
        for (int i = 0; i < devices.size(); i++) {
            CastDevice d = devices.get(i);
            String name = d.getName() == null ? d.getId() : d.getName();
            String addr = d.getAddress() == null ? "" : d.getAddress();
            items[i] = name + "\n" + addr;
        }
        items[itemCount - 1] = manualLabel;

        AlertDialog.Builder b = new AlertDialog.Builder(getContext())
                .setTitle(R.string.cast_dialog_title);
        if (devices.isEmpty()) {
            b.setMessage(R.string.cast_dialog_no_devices);
        } else {
            // 打点当前是在扫的
            Log.i(TAG, "showCastEntryDialog devices=" + devices.size() + " scanning=" + scanning);
        }
        b.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == itemCount - 1) {
                    showManualIpDialog();
                } else {
                    connectTo(devices.get(which));
                }
            }
        })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showManualIpDialog() {
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(R.string.cast_manual_dialog_hint);

        new AlertDialog.Builder(getContext())
                .setTitle(R.string.cast_manual_dialog_title)
                .setView(input)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String ip = input.getText() == null ? "" : input.getText().toString().trim();
                        if (!isPlausibleIpv4(ip)) {
                            Toast.makeText(getContext(),
                                    getContext().getString(R.string.cast_manual_invalid_ipv4, ip),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // 用手动 IP 合成一个 CastDevice。此路径下 provider.connect 里
                        // 由于 raw 不是 RemoteDevice 会直接失败，manual 输入只作为最后
                        // 的诊断入口，等真机验证 DLNA 电视时才有意义。
                        CastDevice manual = new CastDevice(
                                "manual:" + ip,
                                getContext().getString(R.string.cast_manual_device_name, ip),
                                ip,
                                0,
                                JupnpDlnaProvider.PROTOCOL,
                                null);
                        connectTo(manual);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void connectTo(final CastDevice device) {
        final String url = getUrl();
        if (url == null || url.isEmpty()) {
            Toast.makeText(getContext(), R.string.cast_toast_no_url, Toast.LENGTH_SHORT).show();
            return;
        }
        final String title = resolveTitle();
        final Context ctx = getContext().getApplicationContext();
        final CastCapability cap = GSYVideoManager.instance().getCastCapability();
        // 主线程先把本地当前播放位置抓一份：投屏时把它作为起播位置带给远端（对齐 Chromecast
        // LOAD.currentTime 与 Media3 MediaItem.ClippingConfiguration.startPositionMs），
        // 断开塌陷时也用它来 seek 回本地。必须在 releaseAllVideos 之前读，否则 IJK 已释放
        // 会拿到 0，等同于"从头播 + 回本地也从头"的双重回退。
        long localPositionMs = 0L;
        try {
            localPositionMs = Math.max(0L, getCurrentPositionWhenPlaying());
        } catch (Throwable t) {
            Log.e(TAG, "connectTo: read local position failed", t);
        }
        mLastLocalPositionMs = localPositionMs;
        final long startPositionMs = localPositionMs;
        mActiveDevice = device;
        mLastState = CastState.CONNECTING;
        applyCastEntryUi();
        cap.connect(device, new CastProvider.ConnectCallback() {
            @Override
            public void onConnected(final CastSession session) {
                // capability 已经切主线程了；session 挂 listener，然后进 CAST_EXECUTOR 做 IO。
                // 强引用留一份，供 detach/disconnect 时 removeListener 用（否则匿名 listener 会
                // 通过 CopyOnWriteArrayList 泄漏 Activity Context）。
                if (mBoundSession != null && mBoundSession != session) {
                    try {
                        mBoundSession.removeListener(mSessionListener);
                    } catch (Throwable t) {
                        Log.w(TAG, "connectTo: stale removeListener failed", t);
                    }
                }
                mBoundSession = session;
                session.addListener(mSessionListener);
                mCastSession = true;
                Toast.makeText(ctx,
                        ctx.getString(R.string.cast_toast_casting_to,
                                device.getName() == null ? device.getAddress() : device.getName()),
                        Toast.LENGTH_SHORT).show();
                CAST_EXECUTOR.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            CastMediaInfo media = new CastMediaInfo(
                                    url,
                                    title == null ? "" : title,
                                    "video/mp4",
                                    0L,
                                    startPositionMs);
                            session.setMediaItem(media);
                        } catch (Throwable t) {
                            mMain.post(() -> {
                                mCastSession = false;
                                Toast.makeText(ctx,
                                        ctx.getString(R.string.cast_toast_connect_failed,
                                                t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage()),
                                        Toast.LENGTH_LONG).show();
                                applyCastEntryUi();
                            });
                        }
                    }
                });
                // sender-变遥控器：从"本地一起播"改成"本地塌陷成纯遥控 UI"
                // - 记住当前本地位置，供断开时恢复
                // - 释放 IJK：不解码、不出声、不占内存、不吃流量
                // - 挂 overlay UI，用户后续所有操作都是发 SOAP 遥控远端
                enterRemoteMode(device);
                applyCastEntryUi();
            }

            @Override
            public void onError(Throwable error) {
                mCastSession = false;
                mLastState = CastState.ERROR;
                Toast.makeText(ctx,
                        ctx.getString(R.string.cast_toast_connect_failed,
                                error == null ? "?" : error.getMessage()),
                        Toast.LENGTH_LONG).show();
                applyCastEntryUi();
            }
        });
    }

    // ------------------------------------------------------------------
    // Reverse-bridge: local play/pause/seek forwarded to remote session
    // ------------------------------------------------------------------

    @Override
    public void onVideoPause() {
        super.onVideoPause();
        if (mCastSession) {
            forwardPause();
        }
    }

    @Override
    public void onVideoResume() {
        super.onVideoResume();
        if (mCastSession) {
            forwardPlay();
        }
    }

    @Override
    public void onCompletion() {
        super.onCompletion();
        if (mCastSession) {
            forwardStop();
            mCastSession = false;
            applyCastEntryUi();
        }
    }

    @Override
    public void seekTo(long position) {
        super.seekTo(position);
        if (mCastSession && position >= 0) {
            forwardSeek(position);
        }
    }

    private void forwardPause() {
        final CastSession s = GSYVideoManager.instance().getCastCapability().getActiveSession();
        if (s == null) return;
        CAST_EXECUTOR.execute(() -> {
            try {
                Log.i(TAG, "forwardPause -> session.pause()");
                s.pause();
            } catch (Throwable t) {
                Log.e(TAG, "forwardPause failed", t);
            }
        });
    }

    private void forwardPlay() {
        final CastSession s = GSYVideoManager.instance().getCastCapability().getActiveSession();
        if (s == null) return;
        CAST_EXECUTOR.execute(() -> {
            try {
                Log.i(TAG, "forwardPlay -> session.play()");
                s.play();
            } catch (Throwable t) {
                Log.e(TAG, "forwardPlay failed", t);
            }
        });
    }

    private void forwardStop() {
        final CastSession s = GSYVideoManager.instance().getCastCapability().getActiveSession();
        if (s == null) return;
        CAST_EXECUTOR.execute(() -> {
            try {
                Log.i(TAG, "forwardStop -> session.stop()");
                s.stop();
            } catch (Throwable t) {
                Log.e(TAG, "forwardStop failed", t);
            }
        });
    }

    private void forwardSeek(final long positionMs) {
        final CastSession s = GSYVideoManager.instance().getCastCapability().getActiveSession();
        if (s == null) return;
        CAST_EXECUTOR.execute(() -> {
            try {
                Log.i(TAG, "forwardSeek -> session.seekTo(" + positionMs + ")");
                s.seekTo(positionMs);
            } catch (Throwable t) {
                Log.e(TAG, "forwardSeek failed", t);
            }
        });
    }

    /**
     * 遥控 UI 上的音量条 → 发 RenderingControl.SetVolume SOAP 到远端渲染设备。
     * volume 归一化到 [0f, 1f]，jUPnP 内部会转成 UPnP 定义的 0-100。
     */
    private void forwardVolume(final float volume) {
        final CastSession s = GSYVideoManager.instance().getCastCapability().getActiveSession();
        if (s == null) return;
        CAST_EXECUTOR.execute(() -> {
            try {
                Log.i(TAG, "forwardVolume -> session.setVolume(" + volume + ")");
                s.setVolume(volume);
            } catch (Throwable t) {
                Log.e(TAG, "forwardVolume failed", t);
            }
        });
    }

    // ------------------------------------------------------------------
    // Sender 塌陷 / 恢复：类比 Google Cast SDK 的 CastContext.getSessionManager()
    // ------------------------------------------------------------------

    /**
     * 进入遥控模式：
     *   1. 记录当前本地进度（供断开后恢复）
     *   2. releaseAllVideos() —— 停 IJK、释放解码器、断 socket、释 buffer
     *   3. 隐藏 surface_container，overlay 上台
     *   4. 刷设备名 / 播放态图标 / 进度 / 时长
     */
    private void enterRemoteMode(CastDevice device) {
        // 本地位置优先用 connectTo 在 releaseAllVideos 之前就抓好的值（>0 表示已赋值）。
        // 兜底：如果调用方跳过了 connectTo（例如未来接入其他入口），这里再读一次；
        // 但正常路径不应走到这兜底分支，因为此时可能 IJK 还没 release，也可能已经 release。
        if (mLastLocalPositionMs <= 0L) {
            try {
                mLastLocalPositionMs = Math.max(0L, getCurrentPositionWhenPlaying());
            } catch (Throwable t) {
                Log.e(TAG, "enterRemoteMode: read local position failed", t);
                mLastLocalPositionMs = 0L;
            }
        }
        try {
            GSYVideoManager.releaseAllVideos();
            mLocalReleasedForCast = true;
        } catch (Throwable t) {
            Log.e(TAG, "enterRemoteMode: releaseAllVideos failed", t);
        }
        if (mCastRemoteOverlay == null) return;
        View surfaceContainer = findViewById(R.id.surface_container);
        if (surfaceContainer != null) {
            surfaceContainer.setVisibility(View.GONE);
        }
        mCastRemoteOverlay.setVisibility(View.VISIBLE);
        if (mCastRemoteDevice != null) {
            mCastRemoteDevice.setText(device == null || device.getName() == null
                    ? "" : device.getName());
        }
        if (mCastRemotePlayPause != null) {
            // 初始 icon 按当前会话状态决定：PAUSED 显示 play（意为"点我继续播"），
            // 其它情形（PLAYING/LOADING/BUFFERING/CONNECTING）都用 pause icon（意为"点我暂停"）。
            boolean showPlayIcon = (mLastState == CastState.PAUSED)
                    || (mLastState == CastState.STOPPED);
            mCastRemotePlayPause.setImageResource(showPlayIcon
                    ? com.shuyu.gsyvideoplayer.R.drawable.video_click_play_selector
                    : com.shuyu.gsyvideoplayer.R.drawable.video_click_pause_selector);
        }
        if (mCastRemoteProgress != null) {
            mCastRemoteProgress.setProgress(0);
        }
        if (mCastRemoteCurrent != null) {
            mCastRemoteCurrent.setText(formatDurationMs(0));
        }
        if (mCastRemoteTotal != null) {
            mCastRemoteTotal.setText(formatDurationMs(0));
        }
    }

    /**
     * 退出遥控模式：overlay 隐藏，surface_container 显回。真正的"重建 IJK"由
     * {@link #resumeLocalPlaybackAt(long)} 负责。
     */
    private void exitRemoteMode() {
        if (mCastRemoteOverlay != null) {
            mCastRemoteOverlay.setVisibility(View.GONE);
        }
        View surfaceContainer = findViewById(R.id.surface_container);
        if (surfaceContainer != null) {
            surfaceContainer.setVisibility(View.VISIBLE);
        }
        mRemoteSeekbarUserDragging = false;
    }

    /**
     * 断开投屏后，本地 IJK 已经被 release 掉了。这里 rebuild：走 startPlayLogic()
     * 让 GSY 走完 setup → prepare → start，然后 seek 到远端最后进度。
     */
    private void resumeLocalPlaybackAt(final long positionMs) {
        if (!mLocalReleasedForCast) {
            // 从未 release（理论上不该发生），走保守分支
            try {
                super.onVideoResume();
            } catch (Throwable ignored) {
            }
            return;
        }
        mLocalReleasedForCast = false;
        try {
            // GSY 提供的 helper：设置 seek 起点，然后触发 startPlayLogic
            setSeekOnStart(positionMs);
            startPlayLogic();
        } catch (Throwable t) {
            Log.e(TAG, "resumeLocalPlaybackAt failed", t);
        }
    }

    private void updateRemoteOverlayProgress(long positionMs, long durationMs) {
        if (mCastRemoteCurrent != null) {
            mCastRemoteCurrent.setText(formatDurationMs(positionMs));
        }
        if (mCastRemoteTotal != null && durationMs > 0) {
            mCastRemoteTotal.setText(formatDurationMs(durationMs));
        }
        if (mCastRemoteProgress != null && durationMs > 0) {
            int progress = (int) (positionMs * 1000L / durationMs);
            if (progress < 0) progress = 0;
            if (progress > 1000) progress = 1000;
            mCastRemoteProgress.setProgress(progress);
        }
        // 同步 play/pause 图标——但要跳过两种情形：
        //  1) 抑制窗口未过期：用户刚点过按钮，我们已经乐观翻转；这里不能被 stale 状态覆盖回去。
        //  2) mLastState ∈ {BUFFERING, LOADING, CONNECTING}：这是过渡态，用户视觉上还在
        //     "播放中"，此时把 icon 切成 play 反而误导（会以为播放停了）。保持既有 icon。
        if (mCastRemotePlayPause != null) {
            long now = android.os.SystemClock.uptimeMillis();
            if (now < mIconOptimisticUntilMs) return;
            if (mLastState == CastState.BUFFERING
                    || mLastState == CastState.LOADING
                    || mLastState == CastState.CONNECTING) {
                return;
            }
            mCastRemotePlayPause.setImageResource(mLastState == CastState.PLAYING
                    ? com.shuyu.gsyvideoplayer.R.drawable.video_click_pause_selector
                    : com.shuyu.gsyvideoplayer.R.drawable.video_click_play_selector);
        }
    }

    private static String formatDurationMs(long ms) {
        if (ms < 0) ms = 0;
        long totalSec = ms / 1000L;
        long h = totalSec / 3600L;
        long m = (totalSec % 3600L) / 60L;
        long s = totalSec % 60L;
        if (h > 0) {
            return String.format(java.util.Locale.US, "%d:%02d:%02d", h, m, s);
        }
        return String.format(java.util.Locale.US, "%02d:%02d", m, s);
    }

    /**
     * Sender-变遥控器模式下把本地 IJK 静音 / 恢复。走 GSYVideoBaseManager.setNeedMute(boolean)。
     */
    private void applyLocalMute(boolean mute) {
        try {
            com.shuyu.gsyvideoplayer.video.base.GSYVideoViewBridge mgr = getGSYVideoManager();
            if (mgr instanceof com.shuyu.gsyvideoplayer.GSYVideoBaseManager) {
                ((com.shuyu.gsyvideoplayer.GSYVideoBaseManager) mgr).setNeedMute(mute);
            }
        } catch (Throwable ignored) {
        }
    }

    private String getUrl() {
        try {
            GSYBaseVideoPlayer current = getCurrentPlayer();
            if (current instanceof SampleCastControlVideo) {
                return ((SampleCastControlVideo) current).mOriginUrl;
            }
            return mOriginUrl;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private String resolveTitle() {
        try {
            GSYBaseVideoPlayer current = getCurrentPlayer();
            if (current instanceof SampleCastControlVideo) {
                return ((SampleCastControlVideo) current).mTitle;
            }
            return mTitle;
        } catch (Throwable ignored) {
            return mTitle;
        }
    }

    // ------------------------------------------------------------------
    // Session menu + UI helpers
    // ------------------------------------------------------------------

    private void showCastSessionMenu() {
        final String name = mActiveDevice == null || mActiveDevice.getName() == null
                ? getContext().getString(R.string.cast_entry_active_unknown_host)
                : mActiveDevice.getName();
        String[] items = new String[] {
                getContext().getString(R.string.cast_session_menu_switch),
                getContext().getString(R.string.cast_session_menu_disconnect),
                getContext().getString(R.string.cast_session_menu_resume_local),
        };
        new AlertDialog.Builder(getContext())
                .setTitle(getContext().getString(R.string.cast_session_menu_title, name))
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                disconnectFromReceiver(false);
                                showCastEntryDialog();
                                break;
                            case 1:
                                disconnectFromReceiver(false);
                                break;
                            case 2:
                                disconnectFromReceiver(true);
                                break;
                            default:
                                break;
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * 主动断开投屏。契约（M7-a-5 塌陷模型下的硬性要求）：
     * <ul>
     *   <li>无论 resumeLocal 参数如何，只要曾经 {@link #enterRemoteMode} 塌陷过，就必须把本地
     *       播放器重建回来——否则 surface_container 会露出黑框</li>
     *   <li>resumeLocal=true：重建后 seek 到远端最后进度，用户体感是"从远端接着播"</li>
     *   <li>resumeLocal=false：重建后从 0 开始（例如"switch device"场景，用户马上会选新设备）</li>
     * </ul>
     */
    protected void disconnectFromReceiver(final boolean resumeLocal) {
        final Context ctx = getContext().getApplicationContext();
        // 先记录一份断开前的远端进度，稍后用于本地 seek
        final long resumePositionMs = mRemoteLastPositionMs;
        final boolean wasCollapsed = mLocalReleasedForCast;
        Log.i(TAG, "disconnectFromReceiver resumeLocal=" + resumeLocal
                + " wasCollapsed=" + wasCollapsed + " remotePos=" + resumePositionMs);
        mCastSession = false;
        // 主动 disconnect：session 已进入死亡窗口，先把 listener 摘掉再走 capability.disconnect()，
        // 避免 disconnect 后期回调的 STATE=IDLE 再次触发 handleUnexpectedRemoteEnd 递归。
        if (mBoundSession != null) {
            try {
                mBoundSession.removeListener(mSessionListener);
            } catch (Throwable t) {
                Log.w(TAG, "disconnect: pre removeListener failed", t);
            }
            mBoundSession = null;
        }
        applyCastEntryUi();
        CAST_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    GSYVideoManager.instance().getCastCapability().disconnect();
                } catch (Throwable t) {
                    Log.e(TAG, "capability.disconnect() failed", t);
                }
                mMain.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ctx, R.string.cast_toast_disconnected, Toast.LENGTH_SHORT).show();
                        // 隐藏遥控 overlay，恢复本地播放器视图层
                        exitRemoteMode();
                        applyCastEntryUi();
                        if (wasCollapsed) {
                            // 塌陷过：本地 IJK 已 release，必须重建。resumeLocal 只决定要不要 seek。
                            resumeLocalPlaybackAt(resumeLocal ? resumePositionMs : 0L);
                        }
                    }
                });
            }
        });
    }

    /**
     * 远端"意外"结束（播完 / 网络错 / 设备主动 stop）时的塌陷退出。
     * 与 {@link #disconnectFromReceiver} 不同：不再触发 capability.disconnect()（对端已经断了），
     * 仅做本地视图/播放器恢复。默认 seek 到远端最后进度。
     */
    private void handleUnexpectedRemoteEnd() {
        Log.i(TAG, "handleUnexpectedRemoteEnd: exit overlay + rebuild IJK");
        final long resumePositionMs = mRemoteLastPositionMs;
        final boolean wasCollapsed = mLocalReleasedForCast;
        exitRemoteMode();
        applyCastEntryUi();
        if (wasCollapsed) {
            resumeLocalPlaybackAt(resumePositionMs);
        }
    }

    private void applyCastEntryUi() {
        if (mCastEntry == null) {
            return;
        }
        if (!mCastSession) {
            mCastEntry.setText(R.string.cast_entry_label);
            if (mCastStatusChip != null) {
                mCastStatusChip.setVisibility(View.GONE);
                mCastStatusChip.setText("");
            }
            return;
        }
        String tag = (mActiveDevice == null || mActiveDevice.getName() == null)
                ? getContext().getString(R.string.cast_entry_active_unknown_host)
                : mActiveDevice.getName();
        mCastEntry.setText(getContext().getString(R.string.cast_entry_active_prefix, tag));
        if (mCastStatusChip != null) {
            mCastStatusChip.setVisibility(View.VISIBLE);
            mCastStatusChip.setText(stateLabel(mLastState));
        }
    }

    private String stateLabel(CastState state) {
        int resId;
        if (state == null) state = CastState.IDLE;
        switch (state) {
            case CONNECTING: resId = R.string.cast_state_connecting; break;
            case LOADING:    resId = R.string.cast_state_loading; break;
            case PLAYING:    resId = R.string.cast_state_playing; break;
            case PAUSED:     resId = R.string.cast_state_paused; break;
            case BUFFERING:  resId = R.string.cast_state_buffering; break;
            case STOPPED:    resId = R.string.cast_state_stopped; break;
            case ERROR:      resId = R.string.cast_state_error; break;
            case IDLE:
            default:         resId = R.string.cast_state_idle; break;
        }
        return getContext().getString(resId);
    }

    public boolean isCastSessionActive() {
        return mCastSession;
    }

    @Override
    protected void onDetachedFromWindow() {
        try {
            GSYVideoManager.instance().getCastCapability().removeListener(mCapListener);
        } catch (Throwable t) {
            Log.w(TAG, "detach: removeListener(cap) failed", t);
        }
        // session listener 泄漏第一防线：任何 detach 路径都必须 remove 一次。
        // 若是 fullscreen 切换，migrateCastStateTo 已经把 mBoundSession 置 null（listener 归属已迁走），
        // 这里的 remove 会自动跳过；若是 Activity 真正销毁，则会真正摘掉。
        if (mBoundSession != null) {
            try {
                mBoundSession.removeListener(mSessionListener);
            } catch (Throwable t) {
                Log.w(TAG, "detach: removeListener(session) failed", t);
            }
            mBoundSession = null;
        }
        super.onDetachedFromWindow();
    }

    /**
     * Lightweight IPv4 sanity check for the manual-entry dialog.
     */
    public static boolean isPlausibleIpv4(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        String[] parts = s.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        for (String part : parts) {
            if (part.isEmpty() || part.length() > 3) {
                return false;
            }
            for (int i = 0; i < part.length(); i++) {
                char c = part.charAt(i);
                if (c < '0' || c > '9') {
                    return false;
                }
            }
            int v;
            try {
                v = Integer.parseInt(part);
            } catch (NumberFormatException e) {
                return false;
            }
            if (v < 0 || v > 255) {
                return false;
            }
        }
        return true;
    }
}
