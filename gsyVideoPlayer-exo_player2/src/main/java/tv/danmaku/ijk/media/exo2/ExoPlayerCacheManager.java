package tv.danmaku.ijk.media.exo2;

import android.content.Context;
import android.net.Uri;

import com.shuyu.gsyvideoplayer.cache.ICacheManager;

import java.io.File;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * ExoPlayer专用
 * Created by guoshuyu on 2018/5/21.
 */

public class ExoPlayerCacheManager implements ICacheManager {

    protected ExoSourceManager mExoSourceManager;

    @Override
    public void doCacheLogic(Context context, IMediaPlayer mediaPlayer, String url, Map<String, String> header, File cachePath) {
        if (!(mediaPlayer instanceof IjkExo2MediaPlayer)) {
            throw new UnsupportedOperationException("ExoPlayerCacheManager only support IjkExo2MediaPlayer");
        }
        IjkExo2MediaPlayer exoPlayer = ((IjkExo2MediaPlayer) mediaPlayer);
        mExoSourceManager = exoPlayer.getExoHelper();
        //通过自己的内部缓存机制
        exoPlayer.setCache(true);
        exoPlayer.setCacheDir(cachePath);
        exoPlayer.setDataSource(context, Uri.parse(url), header);
    }

    @Override
    public void clearCache(Context context, File cachePath, String url) {
        ExoSourceManager.clearCache(context, cachePath, url);
    }

    @Override
    public void release() {
        mExoSourceManager = null;
    }

    @Override
    public boolean hadCached() {
        return mExoSourceManager != null && mExoSourceManager.hadCached();
    }

    @Override
    public boolean cachePreview(Context context, File cacheDir, String url) {
        return ExoSourceManager.cachePreView(context, cacheDir, url);
    }

    @Override
    public void setCacheAvailableListener(ICacheAvailableListener cacheAvailableListener) {

    }
}
