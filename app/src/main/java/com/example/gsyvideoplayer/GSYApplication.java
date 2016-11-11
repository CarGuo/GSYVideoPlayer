package com.example.gsyvideoplayer;

import android.app.Application;

import com.shuyu.gsyvideoplayer.GSYVideoManager;

/**
 * 初始化视频播放
 * Created by shuyu on 2016/11/11.
 */

public class GSYApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        GSYVideoManager.initVideo(this);
    }
}
