package com.example.gsyvideoplayer.video;

import android.content.Context;
import android.util.AttributeSet;

import com.example.gsyvideoplayer.video.manager.CastReceiverManager;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoViewBridge;

/**
 * Cast Demo 侧的接收端播放器（悬浮小窗内容）。
 *
 * <p>关键设计：覆写 {@link #getGSYVideoManager()} 返回独立的
 * {@link CastReceiverManager} 单例。这样 sender 用主 {@code GSYVideoManager}，
 * receiver 用 {@code CastReceiverManager}，两个 IJK 实例互不影响 —— 从根本上消除了
 * M7-c 遗留的"同进程 GSYVideoManager 全局单例冲突"问题（rule 6/8：根治非兜底）。
 *
 * <p>沿用项目已有范式：
 * <ul>
 *   <li>{@link com.example.gsyvideoplayer.video.manager.CustomManager} —— 多播放器独立
 *       Manager 的做法</li>
 *   <li>{@link com.shuyu.gsyvideoplayer.video.GSYADVideoPlayer} —— 覆写 getGSYVideoManager
 *       返回自定义 Manager 的做法</li>
 * </ul>
 */
public class CastReceiverPlayer extends StandardGSYVideoPlayer {

    public CastReceiverPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public CastReceiverPlayer(Context context) {
        super(context);
    }

    public CastReceiverPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public GSYVideoViewBridge getGSYVideoManager() {
        CastReceiverManager.instance().initContext(getContext().getApplicationContext());
        return CastReceiverManager.instance();
    }
}
