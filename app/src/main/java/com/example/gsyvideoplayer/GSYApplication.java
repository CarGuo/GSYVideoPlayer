package com.example.gsyvideoplayer;

import android.app.Application;

import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;

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
        //GSYVideoManager.instance().setVideoType(this, GSYVideoType.IJKEXOPLAYER);
        //GSYVideoType.setShowType(GSYVideoType.SCREEN_MATCH_FULL);
        //GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_FULL);
        //GSYVideoType.setRenderType(GSYVideoType.SUFRACE);
        GSYVideoType.setRenderType(GSYVideoType.GLSURFACE);
    }
}
