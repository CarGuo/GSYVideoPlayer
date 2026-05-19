package com.shuyu.gsyvideoplayer.compose.native_;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.shuyu.gsyvideoplayer.R;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

/**
 * Compose 原生模式底层承载 Player：
 * 复用 StandardGSYVideoPlayer 的内核与渲染管线，但隐藏所有自带 UI，
 * 由 Compose 端自行绘制控制条与浮层。
 *
 * <p><b>类与构造器为 public 是反射克隆全屏所必需</b>——
 * {@link com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer#startWindowFullscreen}
 * 通过 {@link Class#getConstructor(Class[])} 反射构造同类全屏副本，
 * 该 API 仅返回 public 构造器；非 public 时会抛 NoSuchMethodException。
 * 业务上仍只通过 {@link GSYPlayerController} / {@link GSYPlayerSurface} 使用，
 * 避免直接持有此类引用。
 */
public class GSYComposeHostPlayer extends StandardGSYVideoPlayer {

    /**
     * Compose 端 Controller 注入的 hook，转发底层 [com.shuyu.gsyvideoplayer.listener.GSYMediaPlayerListener]
     * 中 VideoAllCallBack 不覆盖的 buffer / seek 边沿事件。
     *
     * <p>设计要点：
     * <ul>
     *   <li>仅 Controller 应在 attachHost / detachHost 中赋值，业务方禁止直写；</li>
     *   <li>克隆全屏体由 GSY 内核 {@code cloneParams} 复制字段，但反射只复制 protected
     *       字段；本类的两个 hook 字段是 public 但只可被同包 Controller 通过 attachHost 安装；
     *       全屏克隆体由 Controller 重新走 attachHost 路径接管 hook，不依赖 cloneParams 的字段拷贝。</li>
     * </ul>
     */
    public interface BufferingHook { void onBufferingUpdate(int percent); }
    public interface SeekCompleteHook { void onSeekComplete(); }

    private volatile BufferingHook bufferingHook;
    private volatile SeekCompleteHook seekCompleteHook;

    public void setBufferingHook(BufferingHook hook) {
        this.bufferingHook = hook;
    }

    public void setSeekCompleteHook(SeekCompleteHook hook) {
        this.seekCompleteHook = hook;
    }

    public GSYComposeHostPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
        hideSelfWidgets();
    }

    public GSYComposeHostPlayer(Context context) {
        super(context);
        hideSelfWidgets();
    }

    public GSYComposeHostPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        hideSelfWidgets();
    }

    @Override
    public void onBufferingUpdate(int percent) {
        super.onBufferingUpdate(percent);
        BufferingHook h = bufferingHook;
        if (h != null) {
            try { h.onBufferingUpdate(percent); } catch (Throwable ignored) {}
        }
    }

    @Override
    public void onSeekComplete() {
        super.onSeekComplete();
        SeekCompleteHook h = seekCompleteHook;
        if (h != null) {
            try { h.onSeekComplete(); } catch (Throwable ignored) {}
        }
    }

    @Override
    public int getLayoutId() {
        // 复用默认布局，仅保留 surface_container 可见
        return R.layout.video_layout_normal;
    }

    private void hideSelfWidgets() {
        hideIfPresent(R.id.start);
        hideIfPresent(R.id.layout_bottom);
        hideIfPresent(R.id.layout_top);
        hideIfPresent(R.id.bottom_progressbar);
        hideIfPresent(R.id.loading);
        hideIfPresent(R.id.thumb);
        hideIfPresent(R.id.lock_screen);
        hideIfPresent(R.id.small_close);
        hideIfPresent(R.id.back_tiny);
    }

    private void hideIfPresent(int id) {
        try {
            View v = findViewById(id);
            if (v != null) {
                v.setVisibility(GONE);
            }
        } catch (Throwable ignored) {
            // 不同版本/扩展子类可能缺失某个 id，这里安全降级，不影响其它控件
        }
    }

    @Override
    protected void changeUiToNormal() { /* no-op，UI 由 Compose 接管 */ }

    @Override
    protected void changeUiToPreparingShow() { /* no-op */ }

    @Override
    protected void changeUiToPlayingBufferingShow() { /* no-op */ }

    @Override
    protected void changeUiToPlayingShow() { /* no-op */ }

    @Override
    protected void changeUiToPauseShow() { /* no-op */ }

    @Override
    protected void changeUiToError() { /* no-op */ }

    @Override
    protected void changeUiToCompleteShow() { /* no-op */ }

    @Override
    protected void changeUiToPlayingClear() { /* no-op */ }

    @Override
    protected void changeUiToPauseClear() { /* no-op */ }

    @Override
    protected void changeUiToCompleteClear() { /* no-op */ }

    @Override
    protected void changeUiToPrepareingClear() { /* no-op */ }

    @Override
    protected void touchSurfaceMoveFullLogic(float absDeltaX, float absDeltaY) {
        super.touchSurfaceMoveFullLogic(absDeltaX, absDeltaY);
        // 默认禁用所有手势，Compose 端可自定义
        mChangePosition = false;
        mChangeVolume = false;
        mBrightness = false;
    }

    @Override
    protected void touchDoubleUp(MotionEvent e) { /* 不响应双击 */ }
}
