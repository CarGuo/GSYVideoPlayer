package com.example.gsyvideoplayer.exo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.danikula.videocache.HttpProxyCacheServer;
import com.shuyu.gsyvideoplayer.GSYVideoBaseManager;
import com.shuyu.gsyvideoplayer.player.IPlayerManager;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import java.io.File;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.player.IjkLibLoader;

import static com.shuyu.gsyvideoplayer.utils.CommonUtil.hideNavKey;

/**
 * Created by guoshuyu on 2018/5/16.
 * 自定义管理器，连接自定义exo view 和 exo player，实现无缝切换效果
 */
public class GSYExoVideoManager extends GSYVideoBaseManager {
    public static final int SMALL_ID = com.shuyu.gsyvideoplayer.R.id.small_id;

    public static final int FULLSCREEN_ID = com.shuyu.gsyvideoplayer.R.id.full_id;

    public static String TAG = "GSYExoVideoManager";

    @SuppressLint("StaticFieldLeak")
    private static GSYExoVideoManager videoManager;

    /***
     * @param libLoader 是否使用外部动态加载so
     * */
    private GSYExoVideoManager(IjkLibLoader libLoader) {
        init(libLoader);
    }

    /**
     * 单例管理器
     */
    public static synchronized GSYExoVideoManager instance() {
        if (videoManager == null) {
            videoManager = new GSYExoVideoManager(ijkLibLoader);
        }
        return videoManager;
    }

    @Override
    public HttpProxyCacheServer newProxy(Context context, File file) {
        throw new UnsupportedOperationException("no support");
    }

    @Override
    public void setProxy(HttpProxyCacheServer proxy) {
        throw new UnsupportedOperationException("no support");
    }

    @Override
    public HttpProxyCacheServer newProxy(Context context) {
        throw new UnsupportedOperationException("no support");
    }

    @Override
    protected IPlayerManager getPlayManager(int videoType) {
        return new GSYExoPlayerManager();
    }

    public void prepare(List<String> urls, Map<String, String> mapHeadData, boolean loop, float speed) {
        if (urls.size() == 0) return;
        Message msg = new Message();
        msg.what = HANDLER_PREPARE;
        mMapHeadData = mapHeadData;
        msg.obj = new GSYExoModel(urls, mapHeadData, loop, speed);
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
        ((GSYExoPlayerManager)playerManager).previous();
    }

    /**
     * 下一集
     */
    public void next() {
        if (playerManager == null) {
            return;
        }
        ((GSYExoPlayerManager)playerManager).next();
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
            if (GSYExoVideoManager.instance().lastListener() != null) {
                GSYExoVideoManager.instance().lastListener().onBackFullscreen();
            }
        }
        return backFrom;
    }

    /**
     * 页面销毁了记得调用是否所有的video
     */
    public static void releaseAllVideos() {
        if (GSYExoVideoManager.instance().listener() != null) {
            GSYExoVideoManager.instance().listener().onCompletion();
        }
        GSYExoVideoManager.instance().releaseMediaPlayer();
    }


    /**
     * 暂停播放
     */
    public static void onPause() {
        if (GSYExoVideoManager.instance().listener() != null) {
            GSYExoVideoManager.instance().listener().onVideoPause();
        }
    }

    /**
     * 恢复播放
     */
    public static void onResume() {
        if (GSYExoVideoManager.instance().listener() != null) {
            GSYExoVideoManager.instance().listener().onVideoResume();
        }
    }


    /**
     * 恢复暂停状态
     *
     * @param seek 是否产生seek动作,直播设置为false
     */
    public static void onResume(boolean seek) {
        if (GSYExoVideoManager.instance().listener() != null) {
            GSYExoVideoManager.instance().listener().onVideoResume(seek);
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
