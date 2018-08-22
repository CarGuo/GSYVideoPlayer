package tv.danmaku.ijk.media.exo2;

import com.google.android.exoplayer2.source.MediaSource;

import java.io.File;

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
}
