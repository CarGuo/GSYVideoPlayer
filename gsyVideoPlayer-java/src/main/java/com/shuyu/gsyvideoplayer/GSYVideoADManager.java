package com.shuyu.gsyvideoplayer;


import android.annotation.SuppressLint;
import tv.danmaku.ijk.media.player.IjkLibLoader;

/**
 * 视频管理，单例
 * Created by shuyu on 2018/01/26.
 */

public class GSYVideoADManager extends GSYVideoBaseManager {

    public static String TAG = "GSYVideoManager";

    @SuppressLint("StaticFieldLeak")
    private static GSYVideoADManager videoManager;

    //单例模式实在不好给instance()加参数，还是直接设为静态变量吧
    //自定义so包加载类
    private static IjkLibLoader ijkLibLoader;

    /***
     * @param libLoader 是否使用外部动态加载so
     * */
    private GSYVideoADManager(IjkLibLoader libLoader) {
        ijkLibLoader = libLoader;
        init(libLoader);
    }


    /**
     * 单例管理器
     */
    public static synchronized GSYVideoADManager instance() {
        if (videoManager == null) {
            videoManager = new GSYVideoADManager(ijkLibLoader);
        }
        return videoManager;
    }


    /**
     * 暂停播放
     */
    public static void onPause() {
        if (GSYVideoADManager.instance().listener() != null) {
            GSYVideoADManager.instance().listener().onVideoPause();
        }
    }

    /**
     * 恢复播放
     */
    public static void onResume() {
        if (GSYVideoADManager.instance().listener() != null) {
            GSYVideoADManager.instance().listener().onVideoResume();
        }
    }
}