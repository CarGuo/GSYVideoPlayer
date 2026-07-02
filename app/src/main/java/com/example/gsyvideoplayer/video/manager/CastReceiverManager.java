package com.example.gsyvideoplayer.video.manager;

import com.shuyu.gsyvideoplayer.GSYVideoBaseManager;
import com.shuyu.gsyvideoplayer.player.IPlayerManager;
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager;

/**
 * Cast Demo 专用的独立 GSYVideoBaseManager 单例。
 *
 * <p>贴项目内已有的 {@link CustomManager} 范式：把 receiver 播放器完全跟 sender 端
 * 使用的 {@link com.shuyu.gsyvideoplayer.GSYVideoManager} 全局单例隔离开。
 * 好处：
 * <ul>
 *   <li>sender / receiver 各自独立 IJK 实例，无 M7-c 遗留的 GSYVideoManager 全局单例竞争</li>
 *   <li>Demo Manager 仅存在于 {@code :app} 模块，不影响内核库；真实业务方接入投屏时可以
 *       用自己的 receiver Manager（真电视 / 机顶盒）替换本 Demo Manager</li>
 * </ul>
 */
public final class CastReceiverManager extends GSYVideoBaseManager {

    public static final String PLAYER_TAG = "GSYCastReceiverManager";

    private static volatile CastReceiverManager sInstance;

    private CastReceiverManager() {
        init();
    }

    public static CastReceiverManager instance() {
        if (sInstance == null) {
            synchronized (CastReceiverManager.class) {
                if (sInstance == null) {
                    sInstance = new CastReceiverManager();
                }
            }
        }
        return sInstance;
    }

    @Override
    protected IPlayerManager getPlayManager() {
        return new IjkPlayerManager();
    }

    public static void releaseAll() {
        if (sInstance == null) return;
        if (sInstance.listener() != null) {
            sInstance.listener().onCompletion();
        }
        sInstance.releaseMediaPlayer();
    }
}
