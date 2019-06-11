package com.shuyu.gsyvideoplayer.video.base;

import android.content.Context;
import android.util.AttributeSet;

import com.shuyu.gsyvideoplayer.GSYVideoManager;


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

    /*******************************下面方法为管理器和播放控件交互的方法****************************************/

    @Override
    public GSYVideoViewBridge getGSYVideoManager() {
        GSYVideoManager.instance().initContext(getContext().getApplicationContext());
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
    protected int getFullId() {
        return GSYVideoManager.FULLSCREEN_ID;
    }

    @Override
    protected int getSmallId() {
        return GSYVideoManager.SMALL_ID;
    }

}