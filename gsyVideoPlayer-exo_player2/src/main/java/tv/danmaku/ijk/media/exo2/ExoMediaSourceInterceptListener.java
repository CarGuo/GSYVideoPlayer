package tv.danmaku.ijk.media.exo2;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DataSink;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.upstream.cache.CacheDataSink;

import java.io.File;
import java.util.Map;

/**
 * 设置 ExoPlayer 的 MediaSource 创建拦截
 * Created by guoshuyu
 * Date: 2018-08-22
 */
public interface ExoMediaSourceInterceptListener {
    /**
     * @param dataSource  链接
     * @param preview     是否带上header，默认有header自动设置为true
     * @param cacheEnable 是否需要缓存
     * @param isLooping   是否循环
     * @param cacheDir    自定义缓存目录
     * @return 返回不为空时，使用返回的自定义mediaSource
     */
    MediaSource getMediaSource(String dataSource, boolean preview, boolean cacheEnable, boolean isLooping, File cacheDir);


    /**
     * 一般用户自定义 http 忽略 ssl 证书之类的可用于自定义
     * Demo 有对应例子
     * @return 返回不为空时，使用返回的自定义 HttpDataSource，
     */
    DataSource.Factory getHttpDataSourceFactory(
            String userAgent,
            @Nullable TransferListener listener,
            int connectTimeoutMillis,
            int readTimeoutMillis,
            Map<String, String> mapHeadData,
            boolean allowCrossProtocolRedirects);

    /**
     * 一般情况下返回 null 就可以了
     * 如果 getMediaSource 不为 null ，此方法不会被调用
     * 用于每次自定义自己的  {@link CacheDataSink}
     * @return 返回不为空时，使用返回的自定义 Cache DataSink.Factory
     */
    DataSink.Factory cacheWriteDataSinkFactory(String CachePath, String url);
}
