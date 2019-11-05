package com.shuyu.gsyvideoplayer.utils;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.provider.Settings;
import android.view.OrientationEventListener;

import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;

/**
 * 处理屏幕旋转的的逻辑
 * Created by shuyu on 2016/11/11.
 */

public class OrientationUtils {

    private static final int LAND_TYPE_NULL = 0;
    private static final int LAND_TYPE_NORMAL = 1;
    private static final int LAND_TYPE_REVERSE = 2;

    private Activity mActivity;
    private GSYBaseVideoPlayer mVideoPlayer;
    private OrientationEventListener mOrientationEventListener;

    private int mScreenType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    private int mIsLand = LAND_TYPE_NULL;

    private boolean mClick = false;
    private boolean mClickLand = false;
    private boolean mClickPort;
    private boolean mEnable = true;
    //是否跟随系统
    private boolean mRotateWithSystem = true;

    private boolean mIsPause = false;

    /**
     * @param activity
     * @param gsyVideoPlayer
     */
    public OrientationUtils(Activity activity, GSYBaseVideoPlayer gsyVideoPlayer) {
        this.mActivity = activity;
        this.mVideoPlayer = gsyVideoPlayer;
        init();
    }

    private void init() {
        mOrientationEventListener = new OrientationEventListener(mActivity.getApplicationContext()) {
            @Override
            public void onOrientationChanged(int rotation) {
                boolean autoRotateOn = (Settings.System.getInt(mActivity.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
                if (!autoRotateOn && mRotateWithSystem) {
                    return;
                }
                if (mVideoPlayer != null && mVideoPlayer.isVerticalFullByVideoSize()) {
                    return;
                }
                if (mIsPause) {
                    return;
                }
                // 设置竖屏
                if (((rotation >= 0) && (rotation <= 30)) || (rotation >= 330)) {
                    if (mClick) {
                        if (mIsLand > LAND_TYPE_NULL && !mClickLand) {
                            return;
                        } else {
                            mClickPort = true;
                            mClick = false;
                            mIsLand = LAND_TYPE_NULL;
                        }
                    } else {
                        if (mIsLand > LAND_TYPE_NULL) {
                            mScreenType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            if (mVideoPlayer.getFullscreenButton() != null) {
                                if (mVideoPlayer.isIfCurrentIsFullscreen()) {
                                    mVideoPlayer.getFullscreenButton().setImageResource(mVideoPlayer.getShrinkImageRes());
                                } else {
                                    mVideoPlayer.getFullscreenButton().setImageResource(mVideoPlayer.getEnlargeImageRes());
                                }
                            }
                            mIsLand = LAND_TYPE_NULL;
                            mClick = false;
                        }
                    }
                }
                // 设置横屏
                else if (((rotation >= 230) && (rotation <= 310))) {
                    if (mClick) {
                        if (!(mIsLand == LAND_TYPE_NORMAL) && !mClickPort) {
                            return;
                        } else {
                            mClickLand = true;
                            mClick = false;
                            mIsLand = LAND_TYPE_NORMAL;
                        }
                    } else {
                        if (!(mIsLand == LAND_TYPE_NORMAL)) {
                            mScreenType = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            if (mVideoPlayer.getFullscreenButton() != null) {
                                mVideoPlayer.getFullscreenButton().setImageResource(mVideoPlayer.getShrinkImageRes());
                            }
                            mIsLand = 1;
                            mClick = false;
                        }
                    }
                }
                // 设置反向横屏
                else if (rotation > 30 && rotation < 95) {
                    if (mClick) {
                        if (!(mIsLand == LAND_TYPE_REVERSE) && !mClickPort) {
                            return;
                        } else {
                            mClickLand = true;
                            mClick = false;
                            mIsLand = LAND_TYPE_REVERSE;
                        }
                    } else if (!(mIsLand == LAND_TYPE_REVERSE)) {
                        mScreenType = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                        if (mVideoPlayer.getFullscreenButton() != null) {
                            mVideoPlayer.getFullscreenButton().setImageResource(mVideoPlayer.getShrinkImageRes());
                        }
                        mIsLand = LAND_TYPE_REVERSE;
                        mClick = false;
                    }
                }
            }
        };
        mOrientationEventListener.enable();
    }

    /**
     * 点击切换的逻辑，比如竖屏的时候点击了就是切换到横屏不会受屏幕的影响
     */
    public void resolveByClick() {
        if (mIsLand == LAND_TYPE_NULL && mVideoPlayer != null && mVideoPlayer.isVerticalFullByVideoSize()) {
            return;
        }
        mClick = true;
        if (mIsLand == LAND_TYPE_NULL) {
            int request = mActivity.getRequestedOrientation();
            if(request == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                mScreenType = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            } else {
                mScreenType = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            }
            mActivity.setRequestedOrientation(mScreenType);
            if (mVideoPlayer.getFullscreenButton() != null) {
                mVideoPlayer.getFullscreenButton().setImageResource(mVideoPlayer.getShrinkImageRes());
            }
            mIsLand = LAND_TYPE_NORMAL;
            mClickLand = false;
        } else {
            mScreenType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            if (mVideoPlayer.getFullscreenButton() != null) {
                if (mVideoPlayer.isIfCurrentIsFullscreen()) {
                    mVideoPlayer.getFullscreenButton().setImageResource(mVideoPlayer.getShrinkImageRes());
                } else {
                    mVideoPlayer.getFullscreenButton().setImageResource(mVideoPlayer.getEnlargeImageRes());
                }
            }
            mIsLand = LAND_TYPE_NULL;
            mClickPort = false;
        }

    }

    /**
     * 列表返回的样式判断。因为立即旋转会导致界面跳动的问题
     */
    public int backToProtVideo() {
        if (mIsLand > LAND_TYPE_NULL) {
            mClick = true;
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            if (mVideoPlayer != null && mVideoPlayer.getFullscreenButton() != null)
                mVideoPlayer.getFullscreenButton().setImageResource(mVideoPlayer.getEnlargeImageRes());
            mIsLand = LAND_TYPE_NULL;
            mClickPort = false;
            return 500;
        }
        return LAND_TYPE_NULL;
    }


    public boolean isEnable() {
        return mEnable;
    }

    public void setEnable(boolean enable) {
        this.mEnable = enable;
        if (mEnable) {
            mOrientationEventListener.enable();
        } else {
            mOrientationEventListener.disable();
        }
    }

    public void releaseListener() {
        if (mOrientationEventListener != null) {
            mOrientationEventListener.disable();
        }
    }

    public boolean isClick() {
        return mClick;
    }

    public void setClick(boolean click) {
        this.mClick = click;
    }

    public boolean isClickLand() {
        return mClickLand;
    }

    public void setClickLand(boolean ClickLand) {
        this.mClickLand = ClickLand;
    }

    public int getIsLand() {
        return mIsLand;
    }

    public void setIsLand(int IsLand) {
        this.mIsLand = IsLand;
    }


    public boolean isClickPort() {
        return mClickPort;
    }

    public void setClickPort(boolean ClickPort) {
        this.mClickPort = ClickPort;
    }

    public int getScreenType() {
        return mScreenType;
    }

    public void setScreenType(int mScreenType) {
        this.mScreenType = mScreenType;
    }


    public boolean isRotateWithSystem() {
        return mRotateWithSystem;
    }

    /**
     * 是否更新系统旋转，false的话，系统禁止旋转也会跟着旋转
     *
     * @param rotateWithSystem 默认true
     */
    public void setRotateWithSystem(boolean rotateWithSystem) {
        this.mRotateWithSystem = rotateWithSystem;
    }

    public boolean isPause() {
        return mIsPause;
    }

    public void setIsPause(boolean isPause) {
        this.mIsPause = isPause;
    }
}
