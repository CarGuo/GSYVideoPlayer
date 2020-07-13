package com.shuyu.gsyvideoplayer.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.provider.Settings;
import android.view.OrientationEventListener;
import android.view.Surface;

import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;

import java.lang.ref.WeakReference;

/**
 * 处理屏幕旋转的的逻辑
 * Created by shuyu on 2016/11/11.
 */

public class OrientationUtils {

    private static final int LAND_TYPE_NULL = 0;
    private static final int LAND_TYPE_NORMAL = 1;
    private static final int LAND_TYPE_REVERSE = 2;

    private WeakReference<Activity> mActivity;
    private GSYBaseVideoPlayer mVideoPlayer;
    private OrientationEventListener mOrientationEventListener;
    private OrientationOption mOrientationOption;

    private int mScreenType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    private int mIsLand = LAND_TYPE_NULL;

    private boolean mClick = false;
    private boolean mClickLand = false;
    private boolean mClickPort;
    private boolean mEnable = true;
    //是否跟随系统
    private boolean mRotateWithSystem = true;

    private boolean mIsPause = false;

    private boolean mIsOnlyRotateLand = false;

    /**
     * @param activity
     * @param gsyVideoPlayer
     */
    public OrientationUtils(Activity activity, GSYBaseVideoPlayer gsyVideoPlayer) {
        this(activity, gsyVideoPlayer, null);
    }

    public OrientationUtils(Activity activity, GSYBaseVideoPlayer gsyVideoPlayer, OrientationOption orientationOption) {
        this.mActivity = new WeakReference(activity);
        this.mVideoPlayer = gsyVideoPlayer;
        if (orientationOption == null) {
            this.mOrientationOption = new OrientationOption();
        } else {
            this.mOrientationOption = orientationOption;
        }
        initGravity(activity);
        init();
    }

    protected void init() {
        final Activity activity = mActivity.get();
        if(activity == null) {
            return;
        }
        final Context context = activity.getApplicationContext();
        mOrientationEventListener = new OrientationEventListener(context) {
            @SuppressLint("SourceLockedOrientationActivity")
            @Override
            public void onOrientationChanged(int rotation) {
                boolean autoRotateOn = (Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
                if (!autoRotateOn && mRotateWithSystem) {
                    if (!mIsOnlyRotateLand || getIsLand() == LAND_TYPE_NULL) {
                        return;
                    }
                }
                if (mVideoPlayer != null && mVideoPlayer.isVerticalFullByVideoSize()) {
                    return;
                }
                if (mIsPause) {
                    return;
                }
                // 设置竖屏
                if (((rotation >= 0) && (rotation <= mOrientationOption.getNormalPortraitAngleStart()))
                        || (rotation >= mOrientationOption.getNormalPortraitAngleEnd())) {
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
                            if (!mIsOnlyRotateLand) {
                                mScreenType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                                if (mVideoPlayer.getFullscreenButton() != null) {
                                    if (mVideoPlayer.isIfCurrentIsFullscreen()) {
                                        mVideoPlayer.getFullscreenButton().setImageResource(mVideoPlayer.getShrinkImageRes());
                                    } else {
                                        mVideoPlayer.getFullscreenButton().setImageResource(mVideoPlayer.getEnlargeImageRes());
                                    }
                                }
                                mIsLand = LAND_TYPE_NULL;
                            }
                            mClick = false;
                        }
                    }
                }
                // 设置横屏
                else if (((rotation >= mOrientationOption.getNormalLandAngleStart())
                        && (rotation <= mOrientationOption.getNormalLandAngleEnd()))) {
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
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            if (mVideoPlayer.getFullscreenButton() != null) {
                                mVideoPlayer.getFullscreenButton().setImageResource(mVideoPlayer.getShrinkImageRes());
                            }
                            mIsLand = 1;
                            mClick = false;
                        }
                    }
                }
                // 设置反向横屏
                else if (rotation > mOrientationOption.getReverseLandAngleStart()
                        && rotation < mOrientationOption.getReverseLandAngleEnd()) {
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
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
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


    private void initGravity(Activity activity) {
        if (mIsLand == LAND_TYPE_NULL) {
            int defaultRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            if (defaultRotation == Surface.ROTATION_0) {
                // 竖向为正方向。 如：手机、小米平板
                mIsLand = LAND_TYPE_NULL;
                mScreenType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            } else if (defaultRotation == Surface.ROTATION_270) {
                // 横向为正方向。 如：三星、sony平板
                mIsLand = LAND_TYPE_REVERSE;
                mScreenType = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            } else {
                // 未知方向
                mIsLand = LAND_TYPE_NORMAL;
                mScreenType = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            }
        }
    }

    private void setRequestedOrientation(int requestedOrientation) {
        final Activity activity = mActivity.get();
        if(activity == null) {
            return;
        }
        try {
            activity.setRequestedOrientation(requestedOrientation);
        } catch (IllegalStateException exception) {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {
                Debuger.printfError("OrientationUtils", exception);
            } else {
                exception.printStackTrace();
            }
        }
    }

    /**
     * 点击切换的逻辑，比如竖屏的时候点击了就是切换到横屏不会受屏幕的影响
     */
    @SuppressLint("SourceLockedOrientationActivity")
    public void resolveByClick() {
        if (mIsLand == LAND_TYPE_NULL && mVideoPlayer != null && mVideoPlayer.isVerticalFullByVideoSize()) {
            return;
        }
        mClick = true;

        final Activity activity = mActivity.get();
        if(activity == null) {
            return;
        }
        if (mIsLand == LAND_TYPE_NULL) {
            int request = activity.getRequestedOrientation();
            if (request == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                mScreenType = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            } else {
                mScreenType = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            }
            setRequestedOrientation(mScreenType);
            if (mVideoPlayer.getFullscreenButton() != null) {
                mVideoPlayer.getFullscreenButton().setImageResource(mVideoPlayer.getShrinkImageRes());
            }
            mIsLand = LAND_TYPE_NORMAL;
            mClickLand = false;
        } else {
            mScreenType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
    @SuppressLint("SourceLockedOrientationActivity")
    public int backToProtVideo() {
        if (mIsLand > LAND_TYPE_NULL) {
            mClick = true;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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

    public boolean isOnlyRotateLand() {
        return mIsOnlyRotateLand;
    }

    /**
     * 旋转时仅处理横屏
     */
    public void setOnlyRotateLand(boolean onlyRotateLand) {
        this.mIsOnlyRotateLand = onlyRotateLand;
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

    public OrientationOption getOrientationOption() {
        return mOrientationOption;
    }

    public void setOrientationOption(OrientationOption orientationOption) {
        this.mOrientationOption = orientationOption;
    }
}
