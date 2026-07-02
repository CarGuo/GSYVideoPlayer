package com.example.gsyvideoplayer.cast;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.gsyvideoplayer.R;
import com.example.gsyvideoplayer.utils.floatUtil.FloatWindow;
import com.example.gsyvideoplayer.utils.floatUtil.IFloatWindow;
import com.example.gsyvideoplayer.utils.floatUtil.MoveType;
import com.example.gsyvideoplayer.utils.floatUtil.Screen;
import com.example.gsyvideoplayer.video.CastReceiverPlayer;
import com.example.gsyvideoplayer.video.manager.CastReceiverManager;

/**
 * Cast Demo 侧的接收端悬浮小窗。
 *
 * <p>沿用项目内已有范式（不自造轮子）：
 * <ul>
 *   <li>{@link FloatWindow} builder —— 见
 *       {@link com.example.gsyvideoplayer.WindowActivity} 与
 *       {@link com.example.gsyvideoplayer.view.FloatPlayerView} 的用法</li>
 *   <li>{@link CastReceiverPlayer} 覆写 getGSYVideoManager 返
 *       {@link CastReceiverManager}，与 sender 主 GSYVideoManager 分离，
 *       根治同进程 IJK 单例竞争</li>
 * </ul>
 *
 * <p>控制通道（{@link DevReceiverService} 内的 jUPnP AVTransport）在收到 SetURI/Play/Pause/Seek/Stop
 * 之后，只需回到主线程调用本类的静态方法即可，不再拉起独立 Activity。
 */
public final class CastReceiverFloatingWindow {

    private static final String TAG = "gsy_cast_receiver_floating";

    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private CastReceiverFloatingWindow() {
    }

    @MainThread
    public static void showAndPlay(@NonNull Context appContext,
                                   @NonNull String uri,
                                   @Nullable String title) {
        ensureBuilt(appContext);
        IFloatWindow win = FloatWindow.get(TAG);
        if (win == null) return;
        CastReceiverPlayer player = findPlayer(win);
        if (player == null) return;
        player.setUp(uri, false, title == null ? "" : title);
        win.show();
        player.startPlayLogic();
    }

    @MainThread
    public static void play() {
        runOnPlayer(new PlayerAction() {
            @Override
            public void run(@NonNull CastReceiverPlayer p) {
                p.onVideoResume(false);
            }
        });
    }

    @MainThread
    public static void pause() {
        runOnPlayer(new PlayerAction() {
            @Override
            public void run(@NonNull CastReceiverPlayer p) {
                p.onVideoPause();
            }
        });
    }

    @MainThread
    public static void seek(final long positionMs) {
        runOnPlayer(new PlayerAction() {
            @Override
            public void run(@NonNull CastReceiverPlayer p) {
                p.seekTo(positionMs);
            }
        });
    }

    @MainThread
    public static void stop() {
        IFloatWindow win = FloatWindow.get(TAG);
        if (win == null) return;
        CastReceiverPlayer player = findPlayer(win);
        if (player != null) {
            player.release();
        }
        CastReceiverManager.releaseAll();
        win.hide();
    }

    @MainThread
    public static void destroy() {
        IFloatWindow win = FloatWindow.get(TAG);
        if (win == null) return;
        CastReceiverPlayer player = findPlayer(win);
        if (player != null) {
            player.release();
        }
        CastReceiverManager.releaseAll();
        FloatWindow.destroy(TAG);
    }

    /**
     * 允许非主线程直接投递命令，内部自动切到主线程。
     */
    public static void postShowAndPlay(@NonNull final Context appContext,
                                       @NonNull final String uri,
                                       @Nullable final String title) {
        MAIN.post(new Runnable() {
            @Override
            public void run() {
                showAndPlay(appContext, uri, title);
            }
        });
    }

    public static void postPlay() {
        MAIN.post(new Runnable() {
            @Override
            public void run() {
                play();
            }
        });
    }

    public static void postPause() {
        MAIN.post(new Runnable() {
            @Override
            public void run() {
                pause();
            }
        });
    }

    public static void postSeek(final long positionMs) {
        MAIN.post(new Runnable() {
            @Override
            public void run() {
                seek(positionMs);
            }
        });
    }

    public static void postStop() {
        MAIN.post(new Runnable() {
            @Override
            public void run() {
                stop();
            }
        });
    }

    private static void ensureBuilt(@NonNull Context appContext) {
        if (FloatWindow.get(TAG) != null) return;
        View content = LayoutInflater.from(appContext)
                .inflate(R.layout.layout_cast_receiver_floating, null);
        FloatWindow.with(appContext)
                .setView(content)
                .setWidth(Screen.width, 0.35f)
                .setHeight(Screen.width, 0.2f)
                .setX(Screen.width, 0.63f)
                .setY(Screen.height, 0.05f)
                .setMoveType(MoveType.slide)
                .setFilter(false)
                .setTag(TAG)
                .build();
    }

    @Nullable
    private static CastReceiverPlayer findPlayer(@NonNull IFloatWindow win) {
        View root = win.getView();
        if (root == null) return null;
        return root.findViewById(R.id.cast_receiver_player);
    }

    private static void runOnPlayer(@NonNull PlayerAction action) {
        IFloatWindow win = FloatWindow.get(TAG);
        if (win == null) return;
        CastReceiverPlayer player = findPlayer(win);
        if (player == null) return;
        action.run(player);
    }

    // ---- 只读快照：供 :dlna 进程内 LoopbackAvTransportService 回填 GetPositionInfo/GetTransportInfo ----

    /**
     * 当前播放位置（毫秒）。悬浮窗未建立或播放器未 attach 时返回 0。
     * <p>SOAP 线程直接调用；GSY 内核内 getCurrentPositionWhenPlaying 本身对多线程 read 安全
     * （读的是 IJK player 内部字段），无需额外锁。</p>
     */
    public static long getCurrentPositionMs() {
        IFloatWindow win = FloatWindow.get(TAG);
        if (win == null) return 0L;
        CastReceiverPlayer player = findPlayer(win);
        if (player == null) return 0L;
        try {
            return Math.max(0L, player.getCurrentPositionWhenPlaying());
        } catch (Throwable t) {
            return 0L;
        }
    }

    /**
     * 当前媒体总时长（毫秒）。未 prepare 时返回 0。
     */
    public static long getDurationMs() {
        IFloatWindow win = FloatWindow.get(TAG);
        if (win == null) return 0L;
        CastReceiverPlayer player = findPlayer(win);
        if (player == null) return 0L;
        try {
            return Math.max(0L, player.getDuration());
        } catch (Throwable t) {
            return 0L;
        }
    }

    /**
     * 内核状态映射到常量：0=STOPPED / 1=PLAYING / 2=PAUSED / 3=BUFFERING / 4=ERROR。
     * <p>接口用 int 是为了避免 :dlna 进程和主进程都依赖 org.jupnp.support.model.TransportState
     * 造成的类初始化耦合；LoopbackAvTransportService 内部再翻译到 jUPnP 常量。</p>
     */
    public static final int TRANSPORT_STATE_STOPPED   = 0;
    public static final int TRANSPORT_STATE_PLAYING   = 1;
    public static final int TRANSPORT_STATE_PAUSED    = 2;
    public static final int TRANSPORT_STATE_BUFFERING = 3;
    public static final int TRANSPORT_STATE_ERROR     = 4;

    public static int getTransportState() {
        IFloatWindow win = FloatWindow.get(TAG);
        if (win == null) return TRANSPORT_STATE_STOPPED;
        CastReceiverPlayer player = findPlayer(win);
        if (player == null) return TRANSPORT_STATE_STOPPED;
        try {
            int s = player.getCurrentState();
            switch (s) {
                case com.shuyu.gsyvideoplayer.video.base.GSYVideoView.CURRENT_STATE_PLAYING:
                    return TRANSPORT_STATE_PLAYING;
                case com.shuyu.gsyvideoplayer.video.base.GSYVideoView.CURRENT_STATE_PAUSE:
                    return TRANSPORT_STATE_PAUSED;
                case com.shuyu.gsyvideoplayer.video.base.GSYVideoView.CURRENT_STATE_PREPAREING:
                case com.shuyu.gsyvideoplayer.video.base.GSYVideoView.CURRENT_STATE_PLAYING_BUFFERING_START:
                    return TRANSPORT_STATE_BUFFERING;
                case com.shuyu.gsyvideoplayer.video.base.GSYVideoView.CURRENT_STATE_ERROR:
                    return TRANSPORT_STATE_ERROR;
                case com.shuyu.gsyvideoplayer.video.base.GSYVideoView.CURRENT_STATE_NORMAL:
                case com.shuyu.gsyvideoplayer.video.base.GSYVideoView.CURRENT_STATE_AUTO_COMPLETE:
                default:
                    return TRANSPORT_STATE_STOPPED;
            }
        } catch (Throwable t) {
            return TRANSPORT_STATE_STOPPED;
        }
    }

    private interface PlayerAction {
        void run(@NonNull CastReceiverPlayer player);
    }
}
