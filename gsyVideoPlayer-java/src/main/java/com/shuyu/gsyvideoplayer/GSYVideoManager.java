package com.shuyu.gsyvideoplayer;


import android.annotation.SuppressLint;
import com.shuyu.gsyvideoplayer.listener.GSYMediaPlayerListener;

import tv.danmaku.ijk.media.player.IjkLibLoader;


/**
 * 视频管理，单例
 * Created by shuyu on 2016/11/11.
 */

public class GSYVideoManager extends GSYVideoBaseManager {

    public static String TAG = "GSYVideoManager";

    @SuppressLint("StaticFieldLeak")
    private static GSYVideoManager videoManager;

    /***
     * @param libLoader 是否使用外部动态加载so
     * */
    private GSYVideoManager(IjkLibLoader libLoader) {
        init(libLoader);
    }

    /**
     * 单例管理器
     */
    public static synchronized GSYVideoManager instance() {
        if (videoManager == null) {
            videoManager = new GSYVideoManager(ijkLibLoader);
        }
        return videoManager;
    }

    /**
     * 同步创建一个临时管理器
     */
    public static synchronized GSYVideoManager tmpInstance(GSYMediaPlayerListener listener) {
        GSYVideoManager gsyVideoManager = new GSYVideoManager(ijkLibLoader);
        gsyVideoManager.buffterPoint = videoManager.buffterPoint;
        gsyVideoManager.optionModelList = videoManager.optionModelList;
        gsyVideoManager.cacheFile = videoManager.cacheFile;
        gsyVideoManager.playTag = videoManager.playTag;
        gsyVideoManager.mMapHeadData = videoManager.mMapHeadData;
        gsyVideoManager.currentVideoWidth = videoManager.currentVideoWidth;
        gsyVideoManager.currentVideoHeight = videoManager.currentVideoHeight;
        gsyVideoManager.context = videoManager.context;
        gsyVideoManager.lastState = videoManager.lastState;
        gsyVideoManager.playPosition = videoManager.playPosition;
        gsyVideoManager.timeOut = videoManager.timeOut;
        gsyVideoManager.videoType = videoManager.videoType;
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
}