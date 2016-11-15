package com.shuyu.gsyvideoplayer.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.shuyu.gsyvideoplayer.R;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

/**
 * Created by shuyu on 2016/11/12.
 */

public class ListVideoUtil {

    private String TAG = "NULL"; //播放的标志
    private StandardGSYVideoPlayer gsyVideoPlayer;
    private ViewGroup fullViewContainer;
    private ViewGroup listParent;//记录列表中item的父布局
    private OrientationUtils orientationUtils;
    private Context context;
    private int playPosition = -1; // 播放的位置
    private boolean isFull; //当前是否全屏
    private boolean autoRotation;//是否自动旋转
    private boolean fullLandFrist; //是否全屏就马上横屏

    public ListVideoUtil(Context context) {
        gsyVideoPlayer = new StandardGSYVideoPlayer(context);
        this.context = context;
    }

    /**
     * 动态添加视频播放
     *
     * @param position  位置
     * @param imgView   封面
     * @param tag       TAG类型
     * @param container player的容器
     * @param playBtn   播放按键
     */
    public void addVideoPlayer(final int position, View imgView, String tag,
                               ViewGroup container, View playBtn) {
        container.removeAllViews();
        if (isCurrentViewPlaying(position, tag)) {
            if (!isFull) {
                ViewGroup viewGroup = (ViewGroup) gsyVideoPlayer.getParent();
                if (viewGroup != null)
                    viewGroup.removeAllViews();
                container.addView(gsyVideoPlayer);
                playBtn.setVisibility(View.INVISIBLE);
            }
        } else {
            playBtn.setVisibility(View.VISIBLE);
            container.removeAllViews();   //增加封面
            container.addView(imgView);
        }
    }

    /**
     * 设置列表播放中的位置和TAG,防止错位，回复播放位置
     *
     * @param playPosition 列表中的播放位置
     * @param tag          播放的是哪个列表的tag
     */
    public void setPlayPositionAndTag(int playPosition, String tag) {
        this.playPosition = playPosition;
        this.TAG = tag;
    }

    /**
     * 开始播放
     *
     * @param url 播放的URL
     */
    public void startPlay(String url) {
        gsyVideoPlayer.release();

        gsyVideoPlayer.setUp(url, true, "");

        //增加title
        gsyVideoPlayer.getTitleTextView().setVisibility(View.GONE);

        //设置返回键
        gsyVideoPlayer.getBackButton().setVisibility(View.GONE);

        //设置全屏按键功能
        gsyVideoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resolveFullBtn();
            }
        });

        gsyVideoPlayer.startPlayLogic();
    }


    public void resolveFullBtn() {
        if (fullViewContainer == null) {
            return;
        }
        if (!isFull) {
            resolveToFull();
        } else {
            resolveToNormal();
        }
    }

    /**
     * 处理全屏逻辑
     */
    private void resolveToFull() {
        isFull = true;
        ViewGroup viewGroup = (ViewGroup) gsyVideoPlayer.getParent();
        if (viewGroup != null) {
            listParent = viewGroup;
            viewGroup.removeView(gsyVideoPlayer);
        }
        fullViewContainer.setBackgroundColor(Color.BLACK);
        fullViewContainer.addView(gsyVideoPlayer);
        gsyVideoPlayer.getFullscreenButton().setImageResource(R.drawable.video_shrink);
        gsyVideoPlayer.getBackButton().setVisibility(View.VISIBLE);
        //设置旋转
        orientationUtils = new OrientationUtils((Activity) context, gsyVideoPlayer);
        orientationUtils.setEnable(isAutoRotation());
        gsyVideoPlayer.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resolveToNormal();
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (orientationUtils.getIsLand() != 1) {
                    orientationUtils.resolveByClick();
                }
            }
        }, 50);
    }

    /**
     * 处理正常逻辑
     */
    private void resolveToNormal() {
        int delay = orientationUtils.backToProtVideo();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isFull = false;
                fullViewContainer.removeAllViews();
                orientationUtils.setEnable(false);
                fullViewContainer.setBackgroundColor(Color.TRANSPARENT);
                listParent.addView(gsyVideoPlayer);
                gsyVideoPlayer.getFullscreenButton().setImageResource(R.drawable.video_enlarge);
                gsyVideoPlayer.getBackButton().setVisibility(View.GONE);
            }
        }, delay);
    }


    /**
     * 是否当前播放
     */
    private boolean isPlayView(int position, String tag) {
        return playPosition == position && TAG.equals(tag);
    }

    private boolean isCurrentViewPlaying(int position, String tag) {
        return isPlayView(position, tag);
    }

    /**
     * 处理返回正常逻辑
     */
    public boolean backFromFull() {
        boolean isFull = false;
        if (fullViewContainer.getChildCount() > 0) {
            isFull = true;
            resolveToNormal();
        }
        return isFull;
    }

    /**
     * 释放持有的视频
     */
    public void releaseVideoPlayer() {
        ViewGroup viewGroup = (ViewGroup) gsyVideoPlayer.getParent();
        if (viewGroup != null)
            viewGroup.removeAllViews();
        playPosition = -1;
        TAG = "NULL";

    }

    /**
     * 设置全屏显示的viewGroup
     *
     * @param fullViewContainer viewGroup
     */
    public void setFullViewContainer(ViewGroup fullViewContainer) {
        this.fullViewContainer = fullViewContainer;
    }

    /**
     * 是否全屏
     */
    public boolean isFull() {
        return isFull;
    }

    public boolean isAutoRotation() {
        return autoRotation;
    }

    /**
     * 是否自动旋转
     *
     * @param autoRotation 是否要支持重力旋转
     */
    public void setAutoRotation(boolean autoRotation) {
        this.autoRotation = autoRotation;
    }

    public boolean isFullLandFrist() {
        return fullLandFrist;
    }

    /**
     * 是否全屏就马上横屏
     *
     * @param fullLandFrist 如果是，那么全屏的时候就会切换到横屏
     */
    public void setFullLandFrist(boolean fullLandFrist) {
        this.fullLandFrist = fullLandFrist;
    }
}
