package com.shuyu.gsyvideoplayer.cache;

import android.content.Context;

import java.io.File;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by guoshuyu on 2018/5/18.
 */

public interface ICacheManager {

    void doCacheLogic(Context context, IMediaPlayer mediaPlayer, String url, Map<String, String> header, File cachePath);

    void clearCache(Context context, String url);

    void release();

    boolean hadCached();

    void setCacheAvailableListener(ICacheAvailableListener cacheAvailableListener);

    interface ICacheAvailableListener {
        void onCacheAvailable(File cacheFile, String url, int percentsAvailable);
    }
}
