package com.shuyu.gsyvideoplayer;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.shuyu.gsyvideoplayer.listener.GSYMediaPlayerListener;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import static com.shuyu.gsyvideoplayer.utils.CommonUtil.hideNavKey;


/**
 * 视频管理，单例
 * Created by shuyu on 2016/11/11.
 */

public class GSYVideoManager extends GSYVideoBaseManager {

    public static final int SMALL_ID = R.id.small_id;

    public static final int FULLSCREEN_ID = R.id.full_id;

    public static String TAG = "GSYVideoManager";

    @SuppressLint("StaticFieldLeak")
    private static GSYVideoManager videoManager;


    private GSYVideoManager() {
        init();
    }

    /**
     * 单例管理器
     */
    public static synchronized GSYVideoManager instance() {
        if (videoManager == null) {
            videoManager = new GSYVideoManager();
        }
        return videoManager;
    }

    /**
     * 同步创建一个临时管理器
     */
    public static synchronized GSYVideoManager tmpInstance(GSYMediaPlayerListener listener) {
        GSYVideoManager gsyVideoManager = new GSYVideoManager();
        gsyVideoManager.bufferPoint = videoManager.bufferPoint;
        gsyVideoManager.optionModelList = videoManager.optionModelList;
        gsyVideoManager.playTag = videoManager.playTag;
        gsyVideoManager.currentVideoWidth = videoManager.currentVideoWidth;
        gsyVideoManager.currentVideoHeight = videoManager.currentVideoHeight;
        gsyVideoManager.context = videoManager.context;
        gsyVideoManager.lastState = videoManager.lastState;
        gsyVideoManager.playPosition = videoManager.playPosition;
        gsyVideoManager.timeOut = videoManager.timeOut;
        gsyVideoManager.needMute = videoManager.needMute;
        gsyVideoManager.needTimeOutOther = videoManager.needTimeOutOther;
        gsyVideoManager.setListener(listener);
        return gsyVideoManager;
    }

    /**
     * 替换管理器
     */
    public static synchronized void changeManager(GSYVideoManager gsyVideoManager) {
        videoManager = gsyVideoManager;
    }

    /**
     * 退出全屏，主要用于返回键
     *
     * @return 返回是否全屏
     */
    @SuppressWarnings("ResourceType")
    public static boolean backFromWindowFull(Context context) {
        boolean backFrom = false;
        ViewGroup vp = (ViewGroup) (CommonUtil.scanForActivity(context)).findViewById(Window.ID_ANDROID_CONTENT);
        View oldF = vp.findViewById(FULLSCREEN_ID);
        if (oldF != null) {
            backFrom = true;
            hideNavKey(context);
            if (GSYVideoManager.instance().lastListener() != null) {
                GSYVideoManager.instance().lastListener().onBackFullscreen();
            }
        }
        return backFrom;
    }

    /**
     * 页面销毁了记得调用是否所有的video
     */
    public static void releaseAllVideos() {
        if (GSYVideoManager.instance().listener() != null) {
            GSYVideoManager.instance().listener().onCompletion();
        }
        GSYVideoManager.instance().releaseMediaPlayer();
    }


    /**
     * 暂停播放
     */
    public static void onPause() {
        if (GSYVideoManager.instance().listener() != null) {
            GSYVideoManager.instance().listener().onVideoPause();
        }
    }

    /**
     * 恢复播放
     */
    public static void onResume() {
        if (GSYVideoManager.instance().listener() != null) {
            GSYVideoManager.instance().listener().onVideoResume();
        }
    }


    /**
     * 恢复暂停状态
     *
     * @param seek 是否产生seek动作,直播设置为false
     */
    public static void onResume(boolean seek) {
        if (GSYVideoManager.instance().listener() != null) {
            GSYVideoManager.instance().listener().onVideoResume(seek);
        }
    }

    /**
     * 当前是否全屏状态
     *
     * @return 当前是否全屏状态， true代表是。
     */
    @SuppressWarnings("ResourceType")
    public static boolean isFullState(Activity activity) {
        ViewGroup vp = (ViewGroup) (CommonUtil.scanForActivity(activity)).findViewById(Window.ID_ANDROID_CONTENT);
        final View full = vp.findViewById(FULLSCREEN_ID);
        GSYVideoPlayer gsyVideoPlayer = null;
        if (full != null) {
            gsyVideoPlayer = (GSYVideoPlayer) full;
        }
        return gsyVideoPlayer != null;
    }

}