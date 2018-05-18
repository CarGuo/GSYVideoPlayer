package com.shuyu.gsyvideoplayer.cache;

import android.content.Context;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by guoshuyu on 2018/5/18.
 */

public class ExoCacheManager implements ICacheManager {

    private static final String TAG = "ExoCacheManager";

    private Cache mCache;

    protected Map<String, String> mMapHeadData;

    @Override
    public void doCacheLogic(Context context, IMediaPlayer mediaPlayer, String url, Map<String, String> header, File cachePath) {

    }

    @Override
    public void clearCache(Context context, String url) {

    }

    @Override
    public void release() {
        if (mCache != null) {
            try {
                mCache.release();
            } catch (Cache.CacheException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean hadCached() {
        return false;
    }

    @Override
    public void setCacheAvailableListener(ICacheAvailableListener cacheAvailableListener) {

    }

    /**
     * 本地缓存目录
     */
    protected Cache getCache(Context context) {
        if (mCache == null) {
            String path = context.getCacheDir().getAbsolutePath() + File.separator + "exo";
            boolean isLocked = SimpleCache.isCacheFolderLocked(new File(path));
            if (!isLocked) {
                mCache = new SimpleCache(new File(path), new LeastRecentlyUsedCacheEvictor(1024 * 1024 * 100));
            }
        }
        return mCache;
    }

    /**
     * 获取SourceFactory，是否带Cache
     */
    private DataSource.Factory getDataSourceFactoryCache(Context context, String dataSource, boolean cacheEnable, boolean preview) {
        if (cacheEnable) {
            Cache cache = getCache(context);
            return new CacheDataSourceFactory(cache, getDataSourceFactory(context, preview), CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
        } else {
            return getDataSourceFactory(context, preview);
        }
    }

    /**
     * 获取SourceFactory
     */
    private DataSource.Factory getDataSourceFactory(Context context, boolean preview) {
        return new DefaultDataSourceFactory(context, preview ? null : new DefaultBandwidthMeter(),
                getHttpDataSourceFactory(context, preview));
    }

    private DataSource.Factory getHttpDataSourceFactory(Context context, boolean preview) {
        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(context,
                TAG), preview ? null : new DefaultBandwidthMeter());
        if (mMapHeadData != null && mMapHeadData.size() > 0) {
            for (Map.Entry<String, String> header : mMapHeadData.entrySet()) {
                dataSourceFactory.getDefaultRequestProperties().set(header.getKey(), header.getValue());
            }
        }
        return dataSourceFactory;
    }

}
