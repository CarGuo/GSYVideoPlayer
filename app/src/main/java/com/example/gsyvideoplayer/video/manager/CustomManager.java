package com.example.gsyvideoplayer.video.manager;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.shuyu.gsyvideoplayer.GSYVideoBaseManager;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;

import java.util.HashMap;
import java.util.Map;

import tv.danmaku.ijk.media.player.IjkLibLoader;

import static com.shuyu.gsyvideoplayer.utils.CommonUtil.hideNavKey;
import static com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer.FULLSCREEN_ID;

/**
 * 多个播放的管理器
 * Created by guoshuyu on 2018/1/31.
 */

public class CustomManager extends GSYVideoBaseManager {

    public static String TAG = "GSYVideoManager";

    private static Map<String, CustomManager> sMap = new HashMap<>();

    /***
     * @param libLoader 是否使用外部动态加载so
     * */
    public CustomManager(IjkLibLoader libLoader) {
        init(libLoader);
    }


    /**
     * 退出全屏，主要用于返回键
     *
     * @return 返回是否全屏
     */
    @SuppressWarnings("ResourceType")
    public static boolean backFromWindowFull(Context context, String key) {
        boolean backFrom = false;
        ViewGroup vp = (ViewGroup) (CommonUtil.scanForActivity(context)).findViewById(Window.ID_ANDROID_CONTENT);
        View oldF = vp.findViewById(FULLSCREEN_ID);
        if (oldF != null) {
            backFrom = true;
            hideNavKey(context);
            if (getCustomManager(key).lastListener() != null) {
                getCustomManager(key).lastListener().onBackFullscreen();
            }
        }
        return backFrom;
    }

    /**
     * 页面销毁了记得调用是否所有的video
     */
    public static void releaseAllVideos(String key) {
        if (getCustomManager(key).listener() != null) {
            getCustomManager(key).listener().onCompletion();
        }
        getCustomManager(key).releaseMediaPlayer();
    }


    /**
     * 暂停播放
     */
    public void onPause(String key) {
        if (getCustomManager(key).listener() != null) {
            getCustomManager(key).listener().onVideoPause();
        }
    }

    /**
     * 恢复播放
     */
    public void onResume(String key) {
        if (getCustomManager(key).listener() != null) {
            getCustomManager(key).listener().onVideoResume();
        }
    }


    /**
     * 单例管理器
     */
    public static synchronized Map<String, CustomManager> instance() {
        return sMap;
    }

    /**
     * 单例管理器
     */
    public static synchronized CustomManager getCustomManager(String key) {
        if (TextUtils.isEmpty(key)) {
            throw new IllegalStateException("key not be empty");
        }
        CustomManager customManager = sMap.get(key);
        if (customManager == null) {
            customManager = new CustomManager(null);
            sMap.put(key, customManager);
        }
        return customManager;
    }

    public static void onPauseAll() {
        if (sMap.size() > 0) {
            for (Map.Entry<String, CustomManager> header : sMap.entrySet()) {
                header.getValue().onPause(header.getKey());
            }
        }
    }

    public static void onResumeAll() {
        if (sMap.size() > 0) {
            for (Map.Entry<String, CustomManager> header : sMap.entrySet()) {
                header.getValue().onResume(header.getKey());
            }
        }
    }

    public static void clearAllVideo() {
        if (sMap.size() > 0) {
            for (Map.Entry<String, CustomManager> header : sMap.entrySet()) {
                CustomManager.releaseAllVideos(header.getKey());
            }
        }
        sMap.clear();
    }

    public static void removeManager(String key) {
        sMap.remove(key);
    }
}
