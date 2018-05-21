package com.example.gsyvideoplayer.exo;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
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
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.exo2.IjkExo2MediaPlayer;
import tv.danmaku.ijk.media.exo2.demo.EventLogger;
import tv.danmaku.ijk.media.player.AbstractMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.MediaInfo;
import tv.danmaku.ijk.media.player.misc.IjkTrackInfo;

/**
 * 自定义exo player，实现不同于库的exo 无缝切换效果
 */
public class GSYExo2MediaPlayer extends IjkExo2MediaPlayer {

    private static final String TAG = "GSYExo2MediaPlayer";

    private static final long MAX_POSITION_FOR_SEEK_TO_PREVIOUS = 3000;

    private final Timeline.Window window = new Timeline.Window();

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

    public void setDataSource(List<String> uris, Map<String, String> headers, boolean cache) {
        mHeaders = headers;
        if (uris == null) {
            return;
        }
        ConcatenatingMediaSource concatenatedSource = new ConcatenatingMediaSource();
        for (String uri : uris) {
            MediaSource mediaSource = mExoHelper.getMediaSource(uri, isPreview, cache, false, mCacheDir);
            concatenatedSource.addMediaSource(mediaSource);
        }
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
        int windowIndex = mInternalPlayer.getCurrentWindowIndex();
        timeline.getWindow(windowIndex, window);
        int previousWindowIndex = mInternalPlayer.getPreviousWindowIndex();
        if (previousWindowIndex != C.INDEX_UNSET
                && (mInternalPlayer.getCurrentPosition() <= MAX_POSITION_FOR_SEEK_TO_PREVIOUS
                || (window.isDynamic && !window.isSeekable))) {
            mInternalPlayer.seekTo(previousWindowIndex, C.TIME_UNSET);
        } else {
            mInternalPlayer.seekTo(0);
        }
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
        int windowIndex = mInternalPlayer.getCurrentWindowIndex();
        int nextWindowIndex = mInternalPlayer.getNextWindowIndex();
        if (nextWindowIndex != C.INDEX_UNSET) {
            mInternalPlayer.seekTo(nextWindowIndex, C.TIME_UNSET);
        } else if (timeline.getWindow(windowIndex, window, false).isDynamic) {
            mInternalPlayer.seekTo(windowIndex, C.TIME_UNSET);
        }
    }

}
