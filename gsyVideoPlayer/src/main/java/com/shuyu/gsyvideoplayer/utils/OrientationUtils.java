package com.shuyu.gsyvideoplayer.utils;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.provider.Settings;
import android.view.OrientationEventListener;

import com.shuyu.gsyvideoplayer.GSYVideoPlayer;
import com.shuyu.gsyvideoplayer.R;

/**
 * 处理屏幕旋转的的逻辑
 * Created by shuyu on 2016/11/11.
 */

public class OrientationUtils {

    private Activity activity;
    private GSYVideoPlayer gsyVideoPlayer;

    private int screenType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    private OrientationEventListener orientationEventListener;
    private boolean mClick, mClickLand, mClickPort;
    private int mIsLand;
    private boolean mEnable = true;

    public OrientationUtils(Activity activity, GSYVideoPlayer gsyVideoPlayer) {
        this.activity = activity;
        this.gsyVideoPlayer = gsyVideoPlayer;
        init();
    }

    private void init() {
        orientationEventListener = new OrientationEventListener(activity) {
            @Override
            public void onOrientationChanged(int rotation) {
                boolean autoRotateOn = (android.provider.Settings.System.getInt(activity.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
                if (!autoRotateOn) {
                    if (mIsLand == 0) {
                        return;
                    }
                }
                // 设置竖屏
                if (((rotation >= 0) && (rotation <= 30)) || (rotation >= 330)) {
                    if (mClick) {
                        if (mIsLand > 0 && !mClickLand) {
                            return;
                        } else {
                            mClickPort = true;
                            mClick = false;
                            mIsLand = 0;
                        }
                    } else {
                        if (mIsLand > 0) {
                            screenType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            gsyVideoPlayer.getmFullscreenButton().setImageResource(R.drawable.video_enlarge);
                            mIsLand = 0;
                            mClick = false;
                        }
                    }
                }
                // 设置横屏
                else if (((rotation >= 230) && (rotation <= 310))) {
                    if (mClick) {
                        if (!(mIsLand == 1) && !mClickPort) {
                            return;
                        } else {
                            mClickLand = true;
                            mClick = false;
                            mIsLand = 1;
                        }
                    } else {
                        if (!(mIsLand == 1)) {
                            screenType = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            gsyVideoPlayer.getmFullscreenButton().setImageResource(R.drawable.video_shrink);
                            mIsLand = 1;
                            mClick = false;
                        }
                    }
                }
                // 设置反向横屏
                else if (rotation > 30 && rotation < 95) {
                    if (mClick) {
                        if (!(mIsLand == 2) && !mClickPort) {
                            return;
                        } else {
                            mClickLand = true;
                            mClick = false;
                            mIsLand = 2;
                        }
                    } else if (!(mIsLand == 2)) {
                        screenType = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                        gsyVideoPlayer.getmFullscreenButton().setImageResource(R.drawable.video_shrink);
                        mIsLand = 2;
                        mClick = false;
                    }
                }
            }
        };
        orientationEventListener.enable();
    }

    public void resolveByClick() {
        mClick = true;
        if (mIsLand == 0) {
            screenType = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            gsyVideoPlayer.getmFullscreenButton().setImageResource(R.drawable.video_shrink);
            mIsLand = 1;
            mClickLand = false;
        } else {
            screenType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            gsyVideoPlayer.getmFullscreenButton().setImageResource(R.drawable.video_enlarge);
            mIsLand = 0;
            mClickPort = false;
        }

    }

    /**
     * 列表返回的样式判断。因为立即旋转会导致界面跳动的问题
     */
    public int backToProtVideo() {
        if (mIsLand > 0) {
            mClick = true;
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            mIsLand = 0;
            mClickPort = false;
            return 500;
        }
        return 0;
    }


    public boolean isEnable() {
        return mEnable;
    }

    public void setEnable(boolean enable) {
        this.mEnable = enable;
        if (mEnable) {
            orientationEventListener.enable();
        } else {
            orientationEventListener.disable();
        }
    }

    public boolean isClick() {
        return mClick;
    }

    public void setClick(boolean Click) {
        this.mClick = mClick;
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
        return screenType;
    }

    public void setScreenType(int screenType) {
        this.screenType = screenType;
    }
}
