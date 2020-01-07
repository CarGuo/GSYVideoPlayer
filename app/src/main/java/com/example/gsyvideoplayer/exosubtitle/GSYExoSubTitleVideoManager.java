package com.example.gsyvideoplayer.exosubtitle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.google.android.exoplayer2.text.TextOutput;
import com.shuyu.gsyvideoplayer.GSYVideoBaseManager;
import com.shuyu.gsyvideoplayer.player.IPlayerManager;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import java.io.File;
import java.util.Map;

import static com.shuyu.gsyvideoplayer.utils.CommonUtil.hideNavKey;

public class GSYExoSubTitleVideoManager extends GSYVideoBaseManager {

    public static final int SMALL_ID = com.shuyu.gsyvideoplayer.R.id.small_id;

    public static final int FULLSCREEN_ID = com.shuyu.gsyvideoplayer.R.id.full_id;

    public static String TAG = "GSYExoVideoManager";

    @SuppressLint("StaticFieldLeak")
    private static GSYExoSubTitleVideoManager videoManager;


    private GSYExoSubTitleVideoManager() {
        init();
    }

    /**
     * 单例管理器
     */
    public static synchronized GSYExoSubTitleVideoManager instance() {
        if (videoManager == null) {
            videoManager = new GSYExoSubTitleVideoManager();
        }
        return videoManager;
    }

    @Override
    protected IPlayerManager getPlayManager() {
        playerManager = new GSYExoSubTitlePlayerManager();
        return playerManager;
    }

    public void prepare(String url, String subTitle, TextOutput textOutput, Map<String, String> mapHeadData, boolean loop, float speed, boolean cache, File cachePath, String overrideExtension) {
        Message msg = new Message();
        msg.what = HANDLER_PREPARE;
        msg.obj = new GSYExoSubTitleModel(url, subTitle, textOutput, mapHeadData, loop, speed, cache, cachePath, overrideExtension);
        sendMessage(msg);
        if (needTimeOutOther) {
            startTimeOutBuffer();
        }
    }


    /**
     * 上一集
     */
    public void previous() {
        if (playerManager == null) {
            return;
        }
        ((GSYExoSubTitleVideoManager) playerManager).previous();
    }

    /**
     * 下一集
     */
    public void next() {
        if (playerManager == null) {
            return;
        }
        ((GSYExoSubTitleVideoManager) playerManager).next();
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
            if (GSYExoSubTitleVideoManager.instance().lastListener() != null) {
                GSYExoSubTitleVideoManager.instance().lastListener().onBackFullscreen();
            }
        }
        return backFrom;
    }

    /**
     * 页面销毁了记得调用是否所有的video
     */
    public static void releaseAllVideos() {
        if (GSYExoSubTitleVideoManager.instance().listener() != null) {
            GSYExoSubTitleVideoManager.instance().listener().onCompletion();
        }
        GSYExoSubTitleVideoManager.instance().releaseMediaPlayer();
    }


    /**
     * 暂停播放
     */
    public static void onPause() {
        if (GSYExoSubTitleVideoManager.instance().listener() != null) {
            GSYExoSubTitleVideoManager.instance().listener().onVideoPause();
        }
    }

    /**
     * 恢复播放
     */
    public static void onResume() {
        if (GSYExoSubTitleVideoManager.instance().listener() != null) {
            GSYExoSubTitleVideoManager.instance().listener().onVideoResume();
        }
    }


    /**
     * 恢复暂停状态
     *
     * @param seek 是否产生seek动作,直播设置为false
     */
    public static void onResume(boolean seek) {
        if (GSYExoSubTitleVideoManager.instance().listener() != null) {
            GSYExoSubTitleVideoManager.instance().listener().onVideoResume(seek);
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
