package com.shuyu.gsyvideoplayer;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.danikula.videocache.HttpProxyCacheServer;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;

import java.io.File;

import tv.danmaku.ijk.media.player.IjkLibLoader;

import static com.shuyu.gsyvideoplayer.utils.CommonUtil.hideNavKey;

/**
 * 视频管理，单例
 * Created by shuyu on 2018/01/26.
 */

public class GSYVideoADManager extends GSYVideoBaseManager {

    public static final int SMALL_ID =95598;

    public static final int FULLSCREEN_ID = 95597;

    public static String TAG = "GSYVideoADManager";

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
     * 获取缓存代理服务
     */
    protected static HttpProxyCacheServer getProxy(Context context) {
        HttpProxyCacheServer proxy = GSYVideoADManager.instance().proxy;
        return proxy == null ? (GSYVideoADManager.instance().proxy =
                GSYVideoADManager.instance().newProxy(context)) : proxy;
    }




    /**
     * 获取缓存代理服务,带文件目录的
     */
    public static HttpProxyCacheServer getProxy(Context context, File file) {

        //如果为空，返回默认的
        if (file == null) {
            return getProxy(context);
        }

        //如果已经有缓存文件路径，那么判断缓存文件路径是否一致
        if (GSYVideoADManager.instance().cacheFile != null
                && !GSYVideoADManager.instance().cacheFile.getAbsolutePath().equals(file.getAbsolutePath())) {
            //不一致先关了旧的
            HttpProxyCacheServer proxy = GSYVideoADManager.instance().proxy;

            if (proxy != null) {
                proxy.shutdown();
            }
            //开启新的
            return (GSYVideoADManager.instance().proxy =
                    GSYVideoADManager.instance().newProxy(context, file));
        } else {
            //还没有缓存文件的或者一致的，返回原来
            HttpProxyCacheServer proxy = GSYVideoADManager.instance().proxy;

            return proxy == null ? (GSYVideoADManager.instance().proxy =
                    GSYVideoADManager.instance().newProxy(context, file)) : proxy;
        }
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
            if (GSYVideoADManager.instance().lastListener() != null) {
                GSYVideoADManager.instance().lastListener().onBackFullscreen();
            }
        }
        return backFrom;
    }

    /**
     * 页面销毁了记得调用是否所有的video
     */
    public static void releaseAllVideos() {
        if (GSYVideoADManager.instance().listener() != null) {
            GSYVideoADManager.instance().listener().onCompletion();
        }
        GSYVideoADManager.instance().releaseMediaPlayer();
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