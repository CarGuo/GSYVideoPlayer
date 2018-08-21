package com.example.gsyvideoplayer;

import android.app.Application;
import android.support.multidex.MultiDexApplication;

import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.cache.CacheFactory;
import com.shuyu.gsyvideoplayer.cache.ProxyCacheManager;
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.player.SystemPlayerManager;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;

import tv.danmaku.ijk.media.exo2.Exo2PlayerManager;
import tv.danmaku.ijk.media.exo2.ExoPlayerCacheManager;

//import com.squareup.leakcanary.LeakCanary;

/**
 * Created by shuyu on 2016/11/11.
 */

public class GSYApplication extends MultiDexApplication {

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

        //PlayerFactory.setPlayManager(new Exo2PlayerManager());//EXO模式
        //PlayerFactory.setPlayManager(new SystemPlayerManager());//系统模式
        //PlayerFactory.setPlayManager(new IjkPlayerManager());//ijk模式

        //CacheFactory.setCacheManager(new ExoPlayerCacheManager());//exo缓存模式，支持m3u8，只支持exo
        //CacheFactory.setCacheManager(new ProxyCacheManager());//代理缓存模式，支持所有模式，不支持m3u8等

        //GSYVideoType.setShowType(GSYVideoType.SCREEN_MATCH_FULL);
        //GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_FULL);

        //GSYVideoType.setRenderType(GSYVideoType.SUFRACE);
        //GSYVideoType.setRenderType(GSYVideoType.GLSURFACE);

        //IjkPlayerManager.setLogLevel(IjkMediaPlayer.IJK_LOG_SILENT);

        //GSYVideoType.setShowType(GSYVideoType.SCREEN_MATCH_FULL);
    }
}
