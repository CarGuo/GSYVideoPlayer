package com.example.gsyvideoplayer;

import android.app.Application;

import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

//import com.squareup.leakcanary.LeakCanary;

/**
 * Created by shuyu on 2016/11/11.
 */

public class GSYApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            //return;
        //}
        //LeakCanary.install(this);


        //GSYVideoType.enableMediaCodec();
        //GSYVideoType.enableMediaCodecTexture();

        //GSYVideoManager.instance().setVideoType(this, GSYVideoType.IJKEXOPLAYER); //EXO 1 播放内核，弃用
        //GSYVideoManager.instance().setVideoType(this, GSYVideoType.IJKEXOPLAYER2); //EXO 2 播放内核

        //GSYVideoType.setShowType(GSYVideoType.SCREEN_MATCH_FULL);
        //GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_FULL);

        //GSYVideoType.setRenderType(GSYVideoType.SUFRACE);
        //GSYVideoType.setRenderType(GSYVideoType.GLSURFACE);

        //GSYVideoManager.instance().setLogLevel(IjkMediaPlayer.IJK_LOG_SILENT);
    }
}
