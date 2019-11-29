package tv.danmaku.ijk.media.exo2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.Nullable;

import tv.danmaku.ijk.media.exo2.source.GSYExoHttpDataSource;
import tv.danmaku.ijk.media.exo2.source.GSYExoHttpDataSourceFactory;

import android.text.TextUtils;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheSpan;
import com.google.android.exoplayer2.upstream.cache.CacheUtil;
import com.google.android.exoplayer2.upstream.cache.ContentMetadata;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.Map;
import java.util.NavigableSet;

/**
 * Created by guoshuyu on 2018/5/18.
 */

public class ExoSourceManager {

    private static final String TAG = "ExoSourceManager";

    private static final long DEFAULT_MAX_SIZE = 512 * 1024 * 1024;

    public static final int TYPE_RTMP = 4;

    private static Cache mCache;
    /**
     * 忽律Https证书校验
     */
    private static boolean sSkipSSLChain = false;

    private static int sHttpReadTimeout = -1;

    private static int sHttpConnectTimeout = -1;


    private static boolean s = false;

    private Context mAppContext;

    private Map<String, String> mMapHeadData;

    private String mDataSource;

    private static ExoMediaSourceInterceptListener sExoMediaSourceInterceptListener;

    private boolean isCached = false;

    public static ExoSourceManager newInstance(Context context, @Nullable Map<String, String> mapHeadData) {
        return new ExoSourceManager(context, mapHeadData);
    }

    private ExoSourceManager(Context context, Map<String, String> mapHeadData) {
        mAppContext = context.getApplicationContext();
        mMapHeadData = mapHeadData;
    }

    /**
     * @param dataSource  链接
     * @param preview     是否带上header，默认有header自动设置为true
     * @param cacheEnable 是否需要缓存
     * @param isLooping   是否循环
     * @param cacheDir    自定义缓存目录
     */
    public MediaSource getMediaSource(String dataSource, boolean preview, boolean cacheEnable, boolean isLooping, File cacheDir, @Nullable String overrideExtension) {
        MediaSource mediaSource = null;
        if (sExoMediaSourceInterceptListener != null) {
            mediaSource = sExoMediaSourceInterceptListener.getMediaSource(dataSource, preview, cacheEnable, isLooping, cacheDir);
        }
        if (mediaSource != null) {
            return mediaSource;
        }
        mDataSource = dataSource;
        Uri contentUri = Uri.parse(dataSource);
        int contentType = inferContentType(dataSource, overrideExtension);


        if ("android.resource".equals(contentUri.getScheme())) {
            DataSpec dataSpec = new DataSpec(contentUri);
            final RawResourceDataSource rawResourceDataSource = new RawResourceDataSource(mAppContext);
            try {
                rawResourceDataSource.open(dataSpec);
            } catch (RawResourceDataSource.RawResourceDataSourceException e) {
                e.printStackTrace();
            }
            DataSource.Factory factory = new DataSource.Factory() {
                @Override
                public DataSource createDataSource() {
                    return rawResourceDataSource;
                }
            };
            return new ProgressiveMediaSource.Factory(
                    factory).createMediaSource(contentUri);

        }

        switch (contentType) {
            case C.TYPE_SS:
                mediaSource = new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(getDataSourceFactoryCache(mAppContext, cacheEnable, preview, cacheDir)),
                        new DefaultDataSourceFactory(mAppContext, null,
                                getHttpDataSourceFactory(mAppContext, preview))).createMediaSource(contentUri);
                break;
            case C.TYPE_DASH:
                mediaSource = new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(getDataSourceFactoryCache(mAppContext, cacheEnable, preview, cacheDir)),
                        new DefaultDataSourceFactory(mAppContext, null,
                                getHttpDataSourceFactory(mAppContext, preview))).createMediaSource(contentUri);
                break;
            case C.TYPE_HLS:
                mediaSource = new HlsMediaSource.Factory(getDataSourceFactoryCache(mAppContext, cacheEnable, preview, cacheDir)).createMediaSource(contentUri);
                break;
            case TYPE_RTMP:
                RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory(null);
                mediaSource = new ProgressiveMediaSource.Factory(rtmpDataSourceFactory,
                        new DefaultExtractorsFactory())
                        .createMediaSource(contentUri);
                break;
            case C.TYPE_OTHER:
            default:
                mediaSource = new ProgressiveMediaSource.Factory(getDataSourceFactoryCache(mAppContext, cacheEnable,
                        preview, cacheDir), new DefaultExtractorsFactory())
                        .createMediaSource(contentUri);
                break;
        }
        if (isLooping) {
            return new LoopingMediaSource(mediaSource);
        }
        return mediaSource;
    }


    /**
     * 设置ExoPlayer 的 MediaSource 创建拦截
     */
    public static void setExoMediaSourceInterceptListener(ExoMediaSourceInterceptListener exoMediaSourceInterceptListener) {
        sExoMediaSourceInterceptListener = exoMediaSourceInterceptListener;
    }

    public static void resetExoMediaSourceInterceptListener() {
        sExoMediaSourceInterceptListener = null;
    }

    public static ExoMediaSourceInterceptListener getExoMediaSourceInterceptListener() {
        return sExoMediaSourceInterceptListener;
    }


    @SuppressLint("WrongConstant")
    @C.ContentType
    public static int inferContentType(String fileName, @Nullable String overrideExtension) {
        fileName = Util.toLowerInvariant(fileName);
        if (fileName.startsWith("rtmp:")) {
            return TYPE_RTMP;
        } else {
            return inferContentType(Uri.parse(fileName), overrideExtension);
        }
    }

    @C.ContentType
    public static int inferContentType(Uri uri, @Nullable String overrideExtension) {
        return Util.inferContentType(uri, overrideExtension);
    }

    /**
     * 本地缓存目录
     */
    public static synchronized Cache getCacheSingleInstance(Context context, File cacheDir) {
        String dirs = context.getCacheDir().getAbsolutePath();
        if (cacheDir != null) {
            dirs = cacheDir.getAbsolutePath();
        }
        if (mCache == null) {
            String path = dirs + File.separator + "exo";
            boolean isLocked = SimpleCache.isCacheFolderLocked(new File(path));
            if (!isLocked) {
                mCache = new SimpleCache(new File(path), new LeastRecentlyUsedCacheEvictor(DEFAULT_MAX_SIZE));
            }
        }
        return mCache;
    }

    public void release() {
        isCached = false;
        if (mCache != null) {
            try {
                mCache.release();
                mCache = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Cache需要release之后才能clear
     *
     * @param context
     * @param cacheDir
     * @param url
     */
    public static void clearCache(Context context, File cacheDir, String url) {
        try {
            Cache cache = getCacheSingleInstance(context, cacheDir);
            if (!TextUtils.isEmpty(url)) {
                if (cache != null) {
                    CacheUtil.remove(cache, CacheUtil.generateKey(Uri.parse(url)));
                }
            } else {
                if (cache != null) {
                    for (String key : cache.getKeys()) {
                        CacheUtil.remove(cache, key);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean cachePreView(Context context, File cacheDir, String url) {
        return resolveCacheState(getCacheSingleInstance(context, cacheDir), url);
    }

    public boolean hadCached() {
        return isCached;
    }


    public static boolean isSkipSSLChain() {
        return sSkipSSLChain;
    }

    /**
     * 设置https忽略证书
     *
     * @param skipSSLChain true时是hulve
     */
    public static void setSkipSSLChain(boolean skipSSLChain) {
        sSkipSSLChain = skipSSLChain;
    }


    public static int getHttpReadTimeout() {
        return sHttpReadTimeout;
    }

    /**
     * 如果设置小于 0 就使用默认 8000 MILLIS
     */
    public static void setHttpReadTimeout(int httpReadTimeout) {
        ExoSourceManager.sHttpReadTimeout = httpReadTimeout;
    }

    public static int getHttpConnectTimeout() {
        return sHttpConnectTimeout;
    }

    /**
     * 如果设置小于 0 就使用默认 8000 MILLIS
     */
    public static void setHttpConnectTimeout(int httpConnectTimeout) {
        ExoSourceManager.sHttpConnectTimeout = httpConnectTimeout;
    }

    /**
     * 获取SourceFactory，是否带Cache
     */
    private DataSource.Factory getDataSourceFactoryCache(Context context, boolean cacheEnable, boolean preview, File cacheDir) {
        if (cacheEnable) {
            Cache cache = getCacheSingleInstance(context, cacheDir);
            if (cache != null) {
                isCached = resolveCacheState(cache, mDataSource);
                return new CacheDataSourceFactory(cache, getDataSourceFactory(context, preview), CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
            }
        }
        return getDataSourceFactory(context, preview);
    }

    /**
     * 获取SourceFactory
     */
    private DataSource.Factory getDataSourceFactory(Context context, boolean preview) {
        return new DefaultDataSourceFactory(context, preview ? null : new DefaultBandwidthMeter(),
                getHttpDataSourceFactory(context, preview));
    }

    private DataSource.Factory getHttpDataSourceFactory(Context context, boolean preview) {
        int connectTimeout = GSYExoHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS;
        int readTimeout = GSYExoHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS;
        if (sHttpConnectTimeout > 0) {
            connectTimeout = sHttpConnectTimeout;
        }
        if (sHttpReadTimeout > 0) {
            readTimeout = sHttpReadTimeout;
        }
        boolean allowCrossProtocolRedirects = false;
        if (mMapHeadData != null && mMapHeadData.size() > 0) {
            allowCrossProtocolRedirects = "true".equals(mMapHeadData.get("allowCrossProtocolRedirects"));
        }
        if (sSkipSSLChain) {
            GSYExoHttpDataSourceFactory dataSourceFactory = new GSYExoHttpDataSourceFactory(Util.getUserAgent(context,
                    TAG), preview ? null : new DefaultBandwidthMeter.Builder(mAppContext).build(),
                    connectTimeout,
                    readTimeout, allowCrossProtocolRedirects);
            if (mMapHeadData != null && mMapHeadData.size() > 0) {
                for (Map.Entry<String, String> header : mMapHeadData.entrySet()) {
                    dataSourceFactory.getDefaultRequestProperties().set(header.getKey(), header.getValue());
                }
            }
            return dataSourceFactory;
        }
        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(context,
                TAG), preview ? null : new DefaultBandwidthMeter.Builder(mAppContext).build(),
                connectTimeout,
                readTimeout, allowCrossProtocolRedirects);
        if (mMapHeadData != null && mMapHeadData.size() > 0) {
            for (Map.Entry<String, String> header : mMapHeadData.entrySet()) {
                dataSourceFactory.getDefaultRequestProperties().set(header.getKey(), header.getValue());
            }
        }
        return dataSourceFactory;
    }

    /**
     * 根据缓存块判断是否缓存成功
     *
     * @param cache
     */
    private static boolean resolveCacheState(Cache cache, String url) {
        boolean isCache = true;
        if (!TextUtils.isEmpty(url)) {
            String key = CacheUtil.generateKey(Uri.parse(url));
            if (!TextUtils.isEmpty(key)) {
                NavigableSet<CacheSpan> cachedSpans = cache.getCachedSpans(key);
                if (cachedSpans.size() == 0) {
                    isCache = false;
                } else {
                    long contentLength = cache.getContentMetadata(key).get(ContentMetadata.KEY_CONTENT_LENGTH, C.LENGTH_UNSET);
                    long currentLength = 0;
                    for (CacheSpan cachedSpan : cachedSpans) {
                        currentLength += cache.getCachedLength(key, cachedSpan.position, cachedSpan.length);
                    }
                    isCache = currentLength >= contentLength;
                }
            } else {
                isCache = false;
            }
        }
        return isCache;
    }
}
