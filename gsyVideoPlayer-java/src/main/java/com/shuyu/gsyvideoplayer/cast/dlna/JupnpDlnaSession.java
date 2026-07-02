package com.shuyu.gsyvideoplayer.cast.dlna;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.shuyu.gsyvideoplayer.cast.CastMediaInfo;
import com.shuyu.gsyvideoplayer.cast.CastSession;
import com.shuyu.gsyvideoplayer.cast.CastState;
import com.shuyu.gsyvideoplayer.cast.SessionListener;

import org.jupnp.android.AndroidUpnpService;
import org.jupnp.controlpoint.ControlPoint;
import org.jupnp.model.action.ActionInvocation;
import org.jupnp.model.message.UpnpResponse;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.Service;
import org.jupnp.model.types.UDAServiceType;
import org.jupnp.support.avtransport.callback.GetPositionInfo;
import org.jupnp.support.avtransport.callback.GetTransportInfo;
import org.jupnp.support.avtransport.callback.Pause;
import org.jupnp.support.avtransport.callback.Play;
import org.jupnp.support.avtransport.callback.Seek;
import org.jupnp.support.avtransport.callback.SetAVTransportURI;
import org.jupnp.support.avtransport.callback.Stop;
import org.jupnp.support.model.PositionInfo;
import org.jupnp.support.model.TransportInfo;
import org.jupnp.support.model.TransportState;
import org.jupnp.support.renderingcontrol.callback.SetVolume;

import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DLNA/UPnP 会话实现。
 * <p>通过 jUPnP 的 ControlPoint 提交 AVTransport / RenderingControl 系列 Action：
 * SetAVTransportURI / Play / Pause / Stop / Seek(REL_TIME) / SetVolume。
 * jUPnP 自身用后台线程执行 Action，本类只在 caller 线程构造 callback 后 execute，不阻塞。</p>
 * <p>本类内置一个单线程 {@link ScheduledExecutorService}，每 1s 轮询 GetPositionInfo + GetTransportInfo，
 * 把远端 state / position / duration 缓存到本地，并向 {@link SessionListener} 派发（postMain）。</p>
 * <p>状态映射：TransportState.PLAYING → PLAYING；PAUSED_* → PAUSED；TRANSITIONING → BUFFERING；
 * STOPPED/NO_MEDIA_PRESENT → STOPPED；其它 → IDLE。</p>
 */
public class JupnpDlnaSession implements CastSession {

    private static final String TAG = "JupnpDlnaSession";
    private static final UDAServiceType AV_TRANSPORT = new UDAServiceType("AVTransport");
    private static final UDAServiceType RENDERING_CONTROL = new UDAServiceType("RenderingControl");
    private static final long POLL_INTERVAL_MS = 1000L;
    /** 连续失败到达此阈值即视为链路不可用，派发 ERROR 让 UI 走 handleUnexpectedRemoteEnd。 */
    private static final int POLL_FAILURE_ERROR_THRESHOLD = 5;
    /** 单次 tick 的最大 backoff 上限（30s）。 */
    private static final long POLL_BACKOFF_CAP_MS = 30_000L;

    private final AndroidUpnpService upnpService;
    private final RemoteDevice device;
    private final ControlPoint controlPoint;

    private final Service<?, ?> avTransport;
    private final Service<?, ?> renderingControl;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ScheduledExecutorService poller = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> pollingTask;

    private final CopyOnWriteArrayList<SessionListener> listeners = new CopyOnWriteArrayList<>();

    private volatile CastState state = CastState.IDLE;
    private volatile long positionMs;
    private volatile long durationMs;
    private volatile boolean released;

    /**
     * 单次 tick 里还有几个 SOAP 请求没回来。每 tick 有 2 个（GetPositionInfo + GetTransportInfo），
     * 只有归零后才允许调度下一次 tick——避免 wifi 卡时未完成请求堆积成 retry storm。
     */
    private final AtomicInteger pollInflight = new AtomicInteger(0);
    /** 连续失败计数：任一请求 failure 就 +1，任一 success 就归零。 */
    private final AtomicInteger consecutivePollFailures = new AtomicInteger(0);
    /** 当前 tick 期间是否已经派发过一次 ERROR，避免熔断阈值触发后每 tick 都刷 error。 */
    private volatile boolean pollCircuitTripped;

    public JupnpDlnaSession(AndroidUpnpService upnpService, RemoteDevice device) {
        this.upnpService = upnpService;
        this.device = device;
        this.controlPoint = upnpService.getControlPoint();
        this.avTransport = device.findService(AV_TRANSPORT);
        this.renderingControl = device.findService(RENDERING_CONTROL);
        Log.i(TAG, "session ctor device=" + device.getIdentity().getUdn()
                + " avTransport=" + (avTransport == null ? "NULL" : "OK")
                + " renderingControl=" + (renderingControl == null ? "NULL" : "OK"));
    }

    @Override
    public void setMediaItem(CastMediaInfo media) {
        Log.i(TAG, "setMediaItem url=" + media.getUrl()
                + " title=" + media.getTitle()
                + " startPositionMs=" + media.getStartPositionMs());
        if (avTransport == null) {
            Log.e(TAG, "setMediaItem: AVTransport service NULL, abort");
            dispatchError(new IllegalStateException("AVTransport not available"));
            return;
        }
        setState(CastState.LOADING);
        durationMs = media.getDurationMs();
        final long startPositionMs = media.getStartPositionMs();
        String metadata = DidlLite.build(media);
        controlPoint.execute(new SetAVTransportURI(avTransport, media.getUrl(), metadata) {
            @Override
            public void success(ActionInvocation invocation) {
                super.success(invocation);
                Log.i(TAG, "SetAVTransportURI success, call Play (startPositionMs=" + startPositionMs + ")");
                playThenSeek(startPositionMs);
                startPolling();
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                Log.e(TAG, "SetAVTransportURI FAILED: " + defaultMsg);
                dispatchError(new RuntimeException("SetAVTransportURI failed: " + defaultMsg));
                setState(CastState.ERROR);
            }
        });
    }

    /**
     * Play 成功后按需 Seek 到起播位置。
     * <p>对齐 Chromecast LOAD.currentTime 的语义：先起播（transportState 进入 PLAYING），
     * 再定位到目标位置。DLNA 电视对"URI 转轨前直接 Seek"的容忍度不一致，先 Play 更保险。</p>
     * <p>Seek 失败不影响主链路——只是从头播而已，仍会正常轮询。</p>
     */
    private void playThenSeek(final long startPositionMs) {
        if (avTransport == null) return;
        controlPoint.execute(new Play(avTransport, "1") {
            @Override
            public void success(ActionInvocation invocation) {
                super.success(invocation);
                if (startPositionMs > 0L) {
                    Log.i(TAG, "Play success, seeking to " + startPositionMs + "ms");
                    seekTo(startPositionMs);
                }
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                dispatchError(new RuntimeException("Play failed: " + defaultMsg));
            }
        });
    }

    @Override
    public void play() {
        if (avTransport == null) return;
        controlPoint.execute(new Play(avTransport, "1") {
            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                dispatchError(new RuntimeException("Play failed: " + defaultMsg));
            }
        });
    }

    @Override
    public void pause() {
        if (avTransport == null) return;
        controlPoint.execute(new Pause(avTransport) {
            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                dispatchError(new RuntimeException("Pause failed: " + defaultMsg));
            }
        });
    }

    @Override
    public void seekTo(long positionMs) {
        if (avTransport == null) return;
        final String target = formatRelTime(positionMs);
        controlPoint.execute(new Seek(avTransport, target) {
            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                dispatchError(new RuntimeException("Seek failed: " + defaultMsg));
            }
        });
    }

    @Override
    public void stop() {
        if (avTransport == null) return;
        controlPoint.execute(new Stop(avTransport) {
            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                dispatchError(new RuntimeException("Stop failed: " + defaultMsg));
            }
        });
    }

    @Override
    public void setVolume(float volume) {
        if (renderingControl == null) return;
        long v = Math.max(0, Math.min(100, Math.round(volume * 100f)));
        controlPoint.execute(new SetVolume(renderingControl, v) {
            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                dispatchError(new RuntimeException("SetVolume failed: " + defaultMsg));
            }
        });
    }

    @Override
    public CastState getState() {
        return state;
    }

    @Override
    public long getPositionMs() {
        return positionMs;
    }

    @Override
    public long getDurationMs() {
        return durationMs;
    }

    @Override
    public void addListener(SessionListener listener) {
        if (listener != null) listeners.add(listener);
    }

    @Override
    public void removeListener(SessionListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void release() {
        if (released) return;
        released = true;
        stopPolling();
        poller.shutdownNow();
        listeners.clear();
    }

    // ---- polling loop ----

    private void startPolling() {
        if (pollingTask != null || released) return;
        // 只调度"第一次"，后续 tick 由 pollOnce 完成后自我 schedule（保证串行 + backoff）。
        pollingTask = poller.schedule(new Runnable() {
            @Override
            public void run() {
                pollOnce();
            }
        }, 0L, TimeUnit.MILLISECONDS);
    }

    private void stopPolling() {
        if (pollingTask != null) {
            pollingTask.cancel(true);
            pollingTask = null;
        }
    }

    /**
     * 计算下一 tick delay：连续失败 n 次 → 1s * 2^(n-1)，封顶 {@link #POLL_BACKOFF_CAP_MS}。
     * 无失败时保持基准 {@link #POLL_INTERVAL_MS}。
     */
    private long nextDelayMs() {
        int fails = consecutivePollFailures.get();
        if (fails <= 0) return POLL_INTERVAL_MS;
        long backoff = POLL_INTERVAL_MS * (1L << Math.min(fails - 1, 10));
        return Math.min(backoff, POLL_BACKOFF_CAP_MS);
    }

    /**
     * 一次 tick 结束后（in-flight 归零后）调用：决定是否要熔断，然后自我调度下一次 tick。
     * <p>熔断触发条件：连续失败次数 ≥ 阈值。触发后不停止轮询（我们依然想在链路恢复时继续跑），
     * 只是派发一次 ERROR + 拉长 backoff 到 30s，让 UI 有机会走 handleUnexpectedRemoteEnd。</p>
     */
    private void afterTick() {
        if (released) return;
        int fails = consecutivePollFailures.get();
        if (fails >= POLL_FAILURE_ERROR_THRESHOLD && !pollCircuitTripped) {
            pollCircuitTripped = true;
            Log.w(TAG, "poll circuit tripped after " + fails + " consecutive failures");
            dispatchError(new IllegalStateException(
                    "DLNA polling failed " + fails + " times in a row"));
        } else if (fails == 0 && pollCircuitTripped) {
            // 链路恢复：清熔断标志，下次失败重新计数。
            Log.i(TAG, "poll circuit recovered");
            pollCircuitTripped = false;
        }
        try {
            pollingTask = poller.schedule(new Runnable() {
                @Override
                public void run() {
                    pollOnce();
                }
            }, nextDelayMs(), TimeUnit.MILLISECONDS);
        } catch (Throwable t) {
            // poller 已 shutdown（release 竞速）—— 忽略。
            Log.w(TAG, "afterTick: schedule failed (released?)", t);
        }
    }

    private void pollOnce() {
        if (released) return;
        if (avTransport == null) {
            // 没有 AVTransport 服务：单次派发 error 就退，不再自我调度。
            dispatchError(new IllegalStateException("AVTransport not available"));
            return;
        }
        // 拒绝重叠：若上一 tick 的请求还没回来（理论上不该发生，因为我们改成串行 schedule 了；
        // 但外部若并发多次调 startPolling 也做兜底），直接把 in-flight 视为归零后再拨一次。
        if (!pollInflight.compareAndSet(0, 2)) {
            Log.w(TAG, "pollOnce: previous tick still in flight, skip");
            afterTick();
            return;
        }
        try {
            controlPoint.execute(new GetPositionInfo(avTransport) {
                @Override
                public void received(ActionInvocation invocation, PositionInfo info) {
                    try {
                        positionMs = info.getTrackElapsedSeconds() * 1000L;
                        long dur = info.getTrackDurationSeconds() * 1000L;
                        if (dur > 0) durationMs = dur;
                        consecutivePollFailures.set(0);
                        dispatchPosition();
                    } finally {
                        onSoapDone();
                    }
                }

                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                    try {
                        consecutivePollFailures.incrementAndGet();
                    } finally {
                        onSoapDone();
                    }
                }
            });
            controlPoint.execute(new GetTransportInfo(avTransport) {
                @Override
                public void received(ActionInvocation invocation, TransportInfo info) {
                    try {
                        setState(mapState(info.getCurrentTransportState()));
                        consecutivePollFailures.set(0);
                    } finally {
                        onSoapDone();
                    }
                }

                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                    try {
                        consecutivePollFailures.incrementAndGet();
                    } finally {
                        onSoapDone();
                    }
                }
            });
        } catch (Throwable t) {
            // execute 本身抛异常：把 in-flight 直接归零并算一次失败，交给 afterTick 决策。
            Log.e(TAG, "pollOnce: execute threw", t);
            consecutivePollFailures.incrementAndGet();
            pollInflight.set(0);
            afterTick();
        }
    }

    /** 每个 SOAP 请求完成（无论 success/failure）都调用一次。归零时触发 afterTick。 */
    private void onSoapDone() {
        if (pollInflight.decrementAndGet() == 0) {
            afterTick();
        }
    }

    private static CastState mapState(TransportState ts) {
        if (ts == null) return CastState.IDLE;
        switch (ts) {
            case PLAYING:
                return CastState.PLAYING;
            case PAUSED_PLAYBACK:
            case PAUSED_RECORDING:
                return CastState.PAUSED;
            case TRANSITIONING:
                return CastState.BUFFERING;
            case STOPPED:
            case NO_MEDIA_PRESENT:
                return CastState.STOPPED;
            default:
                return CastState.IDLE;
        }
    }

    private void setState(CastState newState) {
        if (newState == null || newState == state) return;
        state = newState;
        final CastState s = newState;
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (SessionListener l : listeners) {
                    try {
                        l.onStateChanged(s);
                    } catch (Throwable ignored) {
                    }
                }
            }
        });
    }

    private void dispatchPosition() {
        final long p = positionMs;
        final long d = durationMs;
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (SessionListener l : listeners) {
                    try {
                        l.onPositionChanged(p, d);
                    } catch (Throwable ignored) {
                    }
                }
            }
        });
    }

    private void dispatchError(final Throwable error) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (SessionListener l : listeners) {
                    try {
                        l.onError(error);
                    } catch (Throwable ignored) {
                    }
                }
            }
        });
    }

    private static String formatRelTime(long ms) {
        long total = ms / 1000L;
        long h = total / 3600L;
        long m = (total % 3600L) / 60L;
        long s = total % 60L;
        return String.format(Locale.US, "%d:%02d:%02d", h, m, s);
    }
}
