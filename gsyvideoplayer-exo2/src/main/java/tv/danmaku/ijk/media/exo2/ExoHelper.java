package tv.danmaku.ijk.media.exo2;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
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

/**
 * Created by guoshuyu on 2018/5/18.
 */

public class ExoHelper {

    private static final String TAG = "IjkExo2MediaPlayer";

    public static final int TYPE_RTMP = 4;

    protected Context mAppContext;

    protected Cache mCache;

    protected Map<String, String> mMapHeadData;

    public ExoHelper(Context context, Map<String, String> mapHeadData) {
        mAppContext = context.getApplicationContext();
        mMapHeadData = mapHeadData;
    }


    public MediaSource getMediaSource(String dataSource, boolean preview, boolean cacheEnable, boolean isLooping) {
        Uri contentUri = Uri.parse(dataSource);
        int contentType = inferContentType(dataSource);
        MediaSource mediaSource;
        switch (contentType) {
            case C.TYPE_SS:
                mediaSource = new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(getDataSourceFactoryCache(mAppContext, dataSource, cacheEnable, preview)),
                        new DefaultDataSourceFactory(mAppContext, null,
                                getHttpDataSourceFactory(mAppContext, preview))).createMediaSource(contentUri);
                break;
            case C.TYPE_DASH:
                mediaSource = new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(getDataSourceFactoryCache(mAppContext, dataSource, cacheEnable, preview)),
                        new DefaultDataSourceFactory(mAppContext, null,
                                getHttpDataSourceFactory(mAppContext, preview))).createMediaSource(contentUri);
                break;
            case C.TYPE_HLS:
                mediaSource = new HlsMediaSource.Factory(getDataSourceFactoryCache(mAppContext, dataSource, cacheEnable, preview)).createMediaSource(contentUri);
                break;
            case TYPE_RTMP:
                RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory(null);
                mediaSource = new ExtractorMediaSource.Factory(rtmpDataSourceFactory)
                        .setExtractorsFactory(new DefaultExtractorsFactory())
                        .createMediaSource(contentUri);
                break;
            case C.TYPE_OTHER:
            default:
                mediaSource = new ExtractorMediaSource.Factory(getDataSourceFactoryCache(mAppContext, dataSource, cacheEnable, preview))
                        .setExtractorsFactory(new DefaultExtractorsFactory())
                        .createMediaSource(contentUri);
                break;
        }
        if (isLooping) {
            return new LoopingMediaSource(mediaSource);
        }
        return mediaSource;
    }

    @C.ContentType
    public static int inferContentType(String fileName) {
        fileName = Util.toLowerInvariant(fileName);
        if (fileName.endsWith(".mpd")) {
            return C.TYPE_DASH;
        } else if (fileName.endsWith(".m3u8")) {
            return C.TYPE_HLS;
        } else if (fileName.endsWith(".ism") || fileName.endsWith(".isml")
                || fileName.endsWith(".ism/manifest") || fileName.endsWith(".isml/manifest")) {
            return C.TYPE_SS;
        } else if (fileName.startsWith("rtmp:")) {
            return TYPE_RTMP;
        } else {
            return C.TYPE_OTHER;
        }
    }


    /**
     * 本地缓存目录
     */
    public Cache getCache(Context context) {
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
