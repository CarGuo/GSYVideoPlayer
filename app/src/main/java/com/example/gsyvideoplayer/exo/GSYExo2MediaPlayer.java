package com.example.gsyvideoplayer.exo;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.media3.common.C;
import androidx.media3.common.Player;
import androidx.media3.common.Timeline;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.ConcatenatingMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;


import java.io.FileDescriptor;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.exo2.IjkExo2MediaPlayer;
import tv.danmaku.ijk.media.exo2.demo.EventLogger;

/**
 * 自定义exo player，实现不同于库的exo 无缝切换效果
 */
public class GSYExo2MediaPlayer extends IjkExo2MediaPlayer {

    private static final String TAG = "GSYExo2MediaPlayer";

    private static final long MAX_POSITION_FOR_SEEK_TO_PREVIOUS = 3000;

    private final Timeline.Window window = new Timeline.Window();

    public static final int POSITION_DISCONTINUITY = 899;

    private int playIndex = 0;

    public GSYExo2MediaPlayer(Context context) {
        super(context);
    }

    @Override
    @Deprecated
    public void setDataSource(Context context, Uri uri) {
        throw new UnsupportedOperationException("Deprecated, try setDataSource(List<String> uris, Map<String, String> headers)");
    }

    @Override
    @Deprecated
    public void setDataSource(Context context, Uri uri, Map<String, String> headers) {
        throw new UnsupportedOperationException("Deprecated, try setDataSource(List<String> uris, Map<String, String> headers)");
    }

    @Override
    @Deprecated
    public void setDataSource(String path) {
        throw new UnsupportedOperationException("Deprecated, try setDataSource(List<String> uris, Map<String, String> headers)");
    }

    @Override
    @Deprecated
    public void setDataSource(FileDescriptor fd) {
        throw new UnsupportedOperationException("Deprecated, try setDataSource(List<String> uris, Map<String, String> headers)");
    }

    @Override
    public void onPositionDiscontinuity(Player.PositionInfo oldPosition, Player.PositionInfo newPosition, @Player.DiscontinuityReason int reason) {
        super.onPositionDiscontinuity(oldPosition, newPosition, reason);
        notifyOnInfo(POSITION_DISCONTINUITY, reason);
    }

    public void setDataSource(List<String> uris, Map<String, String> headers, int index, boolean cache) {
        mHeaders = headers;
        if (uris == null) {
            return;
        }
        ConcatenatingMediaSource concatenatedSource = new ConcatenatingMediaSource();
        for (String uri : uris) {
            MediaSource mediaSource = mExoHelper.getMediaSource(uri, isPreview, cache, false, mCacheDir, getOverrideExtension());
            concatenatedSource.addMediaSource(mediaSource);
        }
        playIndex = index;
        mMediaSource = concatenatedSource;
    }


    /**
     * 上一集
     */
    public void previous() {
        if (mInternalPlayer == null) {
            return;
        }
        Timeline timeline = mInternalPlayer.getCurrentTimeline();
        if (timeline.isEmpty()) {
            return;
        }
        int windowIndex = mInternalPlayer.getCurrentMediaItemIndex();
        timeline.getWindow(windowIndex, window);
        int previousWindowIndex = mInternalPlayer.getPreviousMediaItemIndex();
        if (previousWindowIndex != C.INDEX_UNSET
            && (mInternalPlayer.getCurrentPosition() <= MAX_POSITION_FOR_SEEK_TO_PREVIOUS
            || (window.isDynamic && !window.isSeekable))) {
            mInternalPlayer.seekTo(previousWindowIndex, C.TIME_UNSET);
        } else {
            mInternalPlayer.seekTo(0);
        }
    }

    @Override
    protected void prepareAsyncInternal() {
        new Handler(Looper.getMainLooper()).post(
            new Runnable() {
                @Override
                public void run() {
                    if (mTrackSelector == null) {
                        mTrackSelector = new DefaultTrackSelector(mAppContext);
                    }
                    mEventLogger = new EventLogger(mTrackSelector);
                    boolean preferExtensionDecoders = true;
                    boolean useExtensionRenderers = true;//是否开启扩展
                    @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode = useExtensionRenderers
                        ? (preferExtensionDecoders ? DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                        : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                        : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
                    if (mRendererFactory == null) {
                        mRendererFactory = new DefaultRenderersFactory(mAppContext);
                        mRendererFactory.setExtensionRendererMode(extensionRendererMode);
                    }
                    if (mLoadControl == null) {
                        mLoadControl = new DefaultLoadControl();
                    }
                    mInternalPlayer = new ExoPlayer.Builder(mAppContext, mRendererFactory)
                        .setLooper(Looper.getMainLooper())
                        .setTrackSelector(mTrackSelector)
                        .setLoadControl(mLoadControl).build();

                    mInternalPlayer.addListener(GSYExo2MediaPlayer.this);
                    mInternalPlayer.addAnalyticsListener(GSYExo2MediaPlayer.this);
                    mInternalPlayer.addListener(mEventLogger);
                    if (mSpeedPlaybackParameters != null) {
                        mInternalPlayer.setPlaybackParameters(mSpeedPlaybackParameters);
                    }
                    if (mSurface != null)
                        mInternalPlayer.setVideoSurface(mSurface);
                    ///fix start index
                    if (playIndex > 0) {
                        mInternalPlayer.seekTo(playIndex, C.INDEX_UNSET);
                    }
                    mInternalPlayer.setMediaSource(mMediaSource, false);
                    mInternalPlayer.prepare();
                    mInternalPlayer.setPlayWhenReady(false);
                }
            }
        );
    }

    /**
     * 下一集
     */
    public void next() {
        if (mInternalPlayer == null) {
            return;
        }
        Timeline timeline = mInternalPlayer.getCurrentTimeline();
        if (timeline.isEmpty()) {
            return;
        }
        int windowIndex = mInternalPlayer.getCurrentMediaItemIndex();
        int nextWindowIndex = mInternalPlayer.getNextMediaItemIndex();
        if (nextWindowIndex != C.INDEX_UNSET) {
            mInternalPlayer.seekTo(nextWindowIndex, C.TIME_UNSET);
        } else if (timeline.getWindow(windowIndex, window).isDynamic) {
            mInternalPlayer.seekTo(windowIndex, C.TIME_UNSET);
        }
    }

    public int getCurrentWindowIndex() {
        if (mInternalPlayer == null) {
            return 0;
        }
        return mInternalPlayer.getCurrentMediaItemIndex();
    }
}
