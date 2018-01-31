package com.shuyu.gsyvideoplayer.video.base;

import android.content.Context;
import android.util.AttributeSet;

import com.danikula.videocache.HttpProxyCacheServer;
import com.shuyu.gsyvideoplayer.GSYVideoManager;

import java.io.File;

import tv.danmaku.ijk.media.player.IjkLibLoader;

/**
 * 兼容的空View，目前用于 GSYVideoManager的设置
 * Created by shuyu on 2016/11/11.
 */

public abstract class GSYVideoPlayer extends GSYBaseVideoPlayer {

    public GSYVideoPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public GSYVideoPlayer(Context context) {
        super(context);
    }

    public GSYVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GSYVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 设置自定义so包加载类，必须在setUp之前调用
     * 不然setUp时会第一次实例化GSYVideoManager
     */
    public void setIjkLibLoader(IjkLibLoader libLoader) {
        GSYVideoManager.setIjkLibLoader(libLoader);
    }

    @Override
    protected GSYVideoViewBridge getGSYVideoManager() {
        return GSYVideoManager.instance();
    }

    @Override
    protected boolean backFromFull(Context context) {
        return GSYVideoManager.backFromWindowFull(context);
    }

    @Override
    protected void releaseVideos() {
        GSYVideoManager.releaseAllVideos();
    }

    @Override
    protected HttpProxyCacheServer getProxy(Context context, File file) {
        return GSYVideoManager.getProxy(context, file);
    }
}