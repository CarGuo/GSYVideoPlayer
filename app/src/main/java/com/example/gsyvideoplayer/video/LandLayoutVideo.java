package com.example.gsyvideoplayer.video;

import android.content.Context;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.gsyvideoplayer.R;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

/**
 * Created by shuyu on 2016/12/23.
 * CustomGSYVideoPlayer是试验中，建议使用的时候使用StandardGSYVideoPlayer
 */
public class LandLayoutVideo extends StandardGSYVideoPlayer {

    // 全屏克隆布局类型：0=普通横屏全屏 1=BOOK 左右分置 2=TABLETOP 上下分置
    private static final int FULL_LAYOUT_NORMAL = 0;
    private static final int FULL_LAYOUT_BOOK = 1;
    private static final int FULL_LAYOUT_TABLETOP = 2;

    // 克隆体在构造期（cloneParams 之前）就要决定布局，故用静态字段在进入全屏前暂存姿态。
    private static volatile int sPendingFullLayout = FULL_LAYOUT_NORMAL;

    private boolean isLinkScroll = false;
    ScaleGestureDetector scaleGestureDetector;
    float scaleFactor;

    // 当前内嵌实例所处的半折叠姿态：0 非半折 / 1 BOOK / 2 TABLETOP
    private int foldSplitMode = FULL_LAYOUT_NORMAL;

    public void setFoldSplitMode(int mode) {
        this.foldSplitMode = mode;
    }

    /**
     * 1.5.0开始加入，如果需要不同布局区分功能，需要重载
     */
    public LandLayoutVideo(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public LandLayoutVideo(Context context) {
        super(context);
    }

    public LandLayoutVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void init(Context context) {
        super.init(context);
        post(new Runnable() {
            @Override
            public void run() {
                gestureDetector = new GestureDetector(getContext().getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        touchDoubleUp(e);
                        return super.onDoubleTap(e);
                    }

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        if (!mChangePosition && !mChangeVolume && !mBrightness && mCurrentState != CURRENT_STATE_ERROR) {
                            onClickUiToggle(e);
                        }
                        return super.onSingleTapConfirmed(e);
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        super.onLongPress(e);
                    }
                });

                scaleGestureDetector = new ScaleGestureDetector(getContext().getApplicationContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {

                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        scaleFactor *= detector.getScaleFactor();
                        scaleFactor = (scaleFactor < 1 ? 1 : scaleFactor); // prevent our view from becoming too small //
                        scaleFactor = ((float) ((int) (scaleFactor * 100))) / 100; // Change precision to help with jitter when user just rests their fingers //
                        mTextureViewContainer.setScaleX(scaleFactor);
                        mTextureViewContainer.setScaleY(scaleFactor);

                        return true;
                    }
                });

            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //if (event.getPointerCount() > 1 && v.getId() == R.id.surface_container) {
        if (event.getPointerCount() > 1) {
            scaleGestureDetector.onTouchEvent(event);
            return true;
        }
        return super.onTouch(v, event);
    }

    //这个必须配置最上面的构造才能生效
    @Override
    public int getLayoutId() {
        if (mIfCurrentIsFullscreen) {
            int layout = sPendingFullLayout;
            if (layout == FULL_LAYOUT_BOOK) {
                return R.layout.sample_video_full_book;
            }
            if (layout == FULL_LAYOUT_TABLETOP) {
                return R.layout.sample_video_full_tabletop;
            }
            return R.layout.sample_video_land;
        }
        return R.layout.sample_video_normal;
    }

    @Override
    public GSYBaseVideoPlayer startWindowFullscreen(Context context, boolean actionBar, boolean statusBar) {
        sPendingFullLayout = foldSplitMode;
        return super.startWindowFullscreen(context, actionBar, statusBar);
    }

    @Override
    protected void updateStartImage() {
        if (mIfCurrentIsFullscreen) {
            if (mStartButton instanceof ImageView) {
                ImageView imageView = (ImageView) mStartButton;
                if (mCurrentState == CURRENT_STATE_PLAYING) {
                    imageView.setImageResource(com.shuyu.gsyvideoplayer.R.drawable.video_click_pause_selector);
                } else if (mCurrentState == CURRENT_STATE_ERROR) {
                    imageView.setImageResource(com.shuyu.gsyvideoplayer.R.drawable.video_click_play_selector);
                } else {
                    imageView.setImageResource(com.shuyu.gsyvideoplayer.R.drawable.video_click_play_selector);
                }
            }
        } else {
            super.updateStartImage();
        }
    }

    @Override
    public int getEnlargeImageRes() {
        return R.drawable.custom_enlarge;
    }

    @Override
    public int getShrinkImageRes() {
        return R.drawable.custom_shrink;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isLinkScroll && !isIfCurrentIsFullscreen()) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return super.onInterceptTouchEvent(ev);
    }


    @Override
    protected void resolveNormalVideoShow(View oldF, ViewGroup vp, GSYVideoPlayer gsyVideoPlayer) {
        LandLayoutVideo landLayoutVideo = (LandLayoutVideo) gsyVideoPlayer;
        landLayoutVideo.dismissProgressDialog();
        landLayoutVideo.dismissVolumeDialog();
        landLayoutVideo.dismissBrightnessDialog();
        super.resolveNormalVideoShow(oldF, vp, gsyVideoPlayer);
    }

    public void setLinkScroll(boolean linkScroll) {
        isLinkScroll = linkScroll;
    }


    /**
     * 定义结束后的显示
     */
    @Override
    protected void changeUiToCompleteClear() {
        super.changeUiToCompleteClear();
        setTextAndProgress(0, true);
        //changeUiToNormal();
    }

    @Override
    protected void changeUiToCompleteShow() {
        super.changeUiToCompleteShow();
        setTextAndProgress(0, true);
        //changeUiToNormal();
    }


    @Override
    public void onInfo(int what, int extra) {
        super.onInfo(what, extra);
        if (mRotate != 0 && GSYVideoType.getRenderType() == GSYVideoType.GLSURFACE) {
            float[] rotationMatrix = new float[16];
            Matrix.setIdentityM(rotationMatrix, 0);
            Matrix.rotateM(rotationMatrix, 0, -mRotate, 0, 0, 1);
            setMatrixGL(rotationMatrix);
        }
    }

    public void addPublicTextureView() {
        addTextureView();
    }
}
