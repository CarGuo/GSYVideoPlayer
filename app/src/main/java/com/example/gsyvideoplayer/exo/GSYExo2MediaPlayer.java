package com.example.gsyvideoplayer.exo;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.danikula.videocache.file.Md5FileNameGenerator;
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
import com.google.android.exoplayer2.source.LoopingMediaSource;
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

import tv.danmaku.ijk.media.exo2.demo.EventLogger;
import tv.danmaku.ijk.media.player.AbstractMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.MediaInfo;
import tv.danmaku.ijk.media.player.misc.IjkTrackInfo;

/**
 * 自定义exo player，实现不同于库的exo 无缝切换效果
 */
public class GSYExo2MediaPlayer extends AbstractMediaPlayer implements Player.EventListener, AnalyticsListener {

    private static final String TAG = "GSYExo2MediaPlayer";

    private Context mAppContext;
    private SimpleExoPlayer mInternalPlayer;
    private EventLogger mEventLogger;
    private DefaultRenderersFactory renderersFactory;
    private MediaSource mMediaSource;
    private DefaultTrackSelector mTrackSelector;
    private Surface mSurface;
    private Map<String, String> mHeaders = new HashMap<>();
    private List<String> uris = new ArrayList<>();
    private PlaybackParameters mSpeedPlaybackParameters;
    private Cache mCache;
    private int mVideoWidth;
    private int mVideoHeight;
    private int lastReportedPlaybackState;
    private boolean lastReportedPlayWhenReady;
    private boolean mIsPrepareing = true;
    private boolean mIsBuffering = false;
    private boolean isLooping = false;

    private int audioSessionId = C.AUDIO_SESSION_ID_UNSET;


    public GSYExo2MediaPlayer(Context context) {
        mAppContext = context.getApplicationContext();
        lastReportedPlaybackState = Player.STATE_IDLE;
    }


    private int getVideoRendererIndex() {
        if (mInternalPlayer != null) {
            for (int i = 0; i < mInternalPlayer.getRendererCount(); i++) {
                if (mInternalPlayer.getRendererType(i) == C.TRACK_TYPE_VIDEO) {
                    return i;
                }
            }
        }
        return 0;
    }

    @Override
    public void setDisplay(SurfaceHolder sh) {
        if (sh == null)
            setSurface(null);
        else
            setSurface(sh.getSurface());
    }

    @Override
    public void setSurface(Surface surface) {
        mSurface = surface;
        if (mInternalPlayer != null) {
            if (surface != null && !surface.isValid()) {
                mSurface = null;
            }
            mInternalPlayer.setVideoSurface(surface);
        }
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
    public void setDataSource(String path) {
        throw new UnsupportedOperationException("Deprecated, try setDataSource(List<String> uris, Map<String, String> headers)");
    }

    @Override
    public void setDataSource(FileDescriptor fd) {
        throw new UnsupportedOperationException("Deprecated, try setDataSource(List<String> uris, Map<String, String> headers)");
    }

    public void setDataSource(List<String> uris, Map<String, String> headers, boolean cache) {
        mHeaders = headers;
        this.uris.clear();
        if (uris != null) {
            this.uris.addAll(uris);
        } else {
            return;
        }
        ConcatenatingMediaSource concatenatedSource = new ConcatenatingMediaSource();
        for (String uri : uris) {
            MediaSource mediaSource = getMediaSource(Uri.parse(uri).toString(), false,  cache);
            concatenatedSource.addMediaSource(mediaSource);
        }
        mMediaSource = concatenatedSource;
    }

    @Override
    public String getDataSource() {
        return null;
    }

    public MediaSource getMediaSource() {
        return mMediaSource;
    }

    public List<String> getDataSourceList() {
        return uris;
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        if (mInternalPlayer != null)
            throw new IllegalStateException("can't prepare a prepared player");
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(new DefaultBandwidthMeter());
        mTrackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        mEventLogger = new EventLogger(mTrackSelector);

        boolean preferExtensionDecoders = true;
        boolean useExtensionRenderers = true;//是否开启扩展
        @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode = useExtensionRenderers
                ? (preferExtensionDecoders ? DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;

        renderersFactory = new DefaultRenderersFactory(mAppContext, extensionRendererMode);
        DefaultLoadControl loadControl = new DefaultLoadControl();
        mInternalPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, mTrackSelector, loadControl, null);
        mInternalPlayer.addListener(this);
        mInternalPlayer.addAnalyticsListener(this);
        mInternalPlayer.addListener(mEventLogger);
        if (mSpeedPlaybackParameters != null) {
            mInternalPlayer.setPlaybackParameters(mSpeedPlaybackParameters);
        }
        if (mSurface != null)
            mInternalPlayer.setVideoSurface(mSurface);

        mInternalPlayer.prepare(mMediaSource);
        mInternalPlayer.setPlayWhenReady(false);
    }

    @Override
    public void start() throws IllegalStateException {
        if (mInternalPlayer == null)
            return;
        mInternalPlayer.setPlayWhenReady(true);
    }

    @Override
    public void stop() throws IllegalStateException {
        if (mInternalPlayer == null)
            return;
        mInternalPlayer.release();
    }

    @Override
    public void pause() throws IllegalStateException {
        if (mInternalPlayer == null)
            return;
        mInternalPlayer.setPlayWhenReady(false);
    }

    @Override
    public void setWakeMode(Context context, int mode) {
        // FIXME: implement
    }

    @Override
    public void setScreenOnWhilePlaying(boolean screenOn) {
        // TODO: do nothing
    }

    @Override
    public IjkTrackInfo[] getTrackInfo() {
        // TODO: implement
        return null;
    }

    @Override
    public int getVideoWidth() {
        return mVideoWidth;
    }

    @Override
    public int getVideoHeight() {
        return mVideoHeight;
    }

    @Override
    public boolean isPlaying() {
        if (mInternalPlayer == null)
            return false;
        int state = mInternalPlayer.getPlaybackState();
        switch (state) {
            case Player.STATE_BUFFERING:
            case Player.STATE_READY:
                return mInternalPlayer.getPlayWhenReady();
            case Player.STATE_IDLE:
            case Player.STATE_ENDED:
            default:
                return false;
        }
    }

    @Override
    public void seekTo(long msec) throws IllegalStateException {
        if (mInternalPlayer == null)
            return;
        mInternalPlayer.seekTo(msec);
    }

    @Override
    public long getCurrentPosition() {
        if (mInternalPlayer == null)
            return 0;
        return mInternalPlayer.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        if (mInternalPlayer == null)
            return 0;
        return mInternalPlayer.getDuration();
    }

    @Override
    public int getVideoSarNum() {
        return 1;
    }

    @Override
    public int getVideoSarDen() {
        return 1;
    }

    @Override
    public void reset() {
        if (mInternalPlayer != null) {
            mInternalPlayer.release();
            mInternalPlayer = null;
        }

        mSurface = null;
        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    @Override
    public void setLooping(boolean looping) {
        isLooping = looping;
    }

    @Override
    public boolean isLooping() {
        return isLooping;
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        if (mInternalPlayer != null)
            mInternalPlayer.setVolume((leftVolume + rightVolume) / 2);
    }

    @Override
    public int getAudioSessionId() {
        return audioSessionId;
    }

    @Override
    public MediaInfo getMediaInfo() {
        // TODO: no support
        return null;
    }

    @Override
    public void setLogEnabled(boolean enable) {
        // do nothing
    }

    @Override
    public boolean isPlayable() {
        return true;
    }

    @Override
    public void setAudioStreamType(int streamtype) {
        // do nothing
    }

    @Override
    public void setKeepInBackground(boolean keepInBackground) {
        // do nothing
    }

    @Override
    public void release() {
        if (mInternalPlayer != null) {
            reset();
            mEventLogger = null;
        }
        if (mCache != null) {
            try {
                mCache.release();
            } catch (Cache.CacheException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopPlayback() {
        mInternalPlayer.stop();
    }

    /**
     * 倍速播放
     *
     * @param speed 倍速播放，默认为1
     * @param pitch 音量缩放，默认为1，修改会导致声音变调
     */
    public void setSpeed(@Size(min = 0) float speed, @Size(min = 0) float pitch) {
        PlaybackParameters playbackParameters = new PlaybackParameters(speed, pitch);
        mSpeedPlaybackParameters = playbackParameters;
        if (mInternalPlayer != null) {
            mInternalPlayer.setPlaybackParameters(playbackParameters);
        }
    }

    public float getSpeed() {
        return mInternalPlayer.getPlaybackParameters().speed;
    }

    public int getBufferedPercentage() {
        if (mInternalPlayer == null)
            return 0;

        return mInternalPlayer.getBufferedPercentage();
    }

    public static final int TYPE_RTMP = 4;

    /**
     * Makes a best guess to infer the type from a file name.
     *
     * @param fileName Name of the file. It can include the path of the file.
     * @return The content type.
     */
    @C.ContentType
    private int inferContentType(String fileName) {
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

    private MediaSource getMediaSource(String dataSource, boolean preview, boolean cacheEnable) {
        Uri contentUri = Uri.parse(dataSource);
        int contentType = inferContentType(dataSource);
        MediaSource mediaSource;
        switch (contentType) {
            case C.TYPE_SS:
                mediaSource = new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(getDataSourceFactoryCache(dataSource, cacheEnable, preview)),
                        new DefaultDataSourceFactory(mAppContext, null,
                                getHttpDataSourceFactory(preview))).createMediaSource(contentUri);
                break;
            case C.TYPE_DASH:
                mediaSource = new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(getDataSourceFactoryCache(dataSource, cacheEnable, preview)),
                        new DefaultDataSourceFactory(mAppContext, null,
                                getHttpDataSourceFactory(preview))).createMediaSource(contentUri);
                break;
            case C.TYPE_HLS:
                mediaSource = new HlsMediaSource.Factory(getDataSourceFactoryCache(dataSource, cacheEnable, preview)).createMediaSource(contentUri);
                break;
            case TYPE_RTMP:
                RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory(null);
                mediaSource = new ExtractorMediaSource.Factory(rtmpDataSourceFactory)
                        .setExtractorsFactory(new DefaultExtractorsFactory())
                        .createMediaSource(contentUri);
                break;
            case C.TYPE_OTHER:
            default:
                mediaSource = new ExtractorMediaSource.Factory(getDataSourceFactoryCache(dataSource, cacheEnable, preview))
                        .setExtractorsFactory(new DefaultExtractorsFactory())
                        .createMediaSource(contentUri);
                break;
        }
        /*if (isLooping()) {
            return new LoopingMediaSource(mediaSource);
        }*/
        return mediaSource;
    }

    /**
     * 获取SourceFactory，是否带Cache
     */
    private DataSource.Factory getDataSourceFactoryCache(String dataSource, boolean cacheEnable, boolean preview) {
        if (cacheEnable) {
            Cache cache = getCache();
            return new CacheDataSourceFactory(cache, getDataSourceFactory(preview), CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
        } else {
            return getDataSourceFactory(preview);
        }
    }

    /**
     * 本地缓存目录
     */
    private Cache getCache() {
        if (mCache == null) {
            String path = mAppContext.getCacheDir().getAbsolutePath() + File.separator + "exo";
            boolean isLocked = SimpleCache.isCacheFolderLocked(new File(path));
            if (!isLocked) {
                mCache = new SimpleCache(new File(path), new LeastRecentlyUsedCacheEvictor(1024 * 1024 * 100));
            }
        }
        return mCache;
    }

    /**
     * 获取SourceFactory
     */
    private DataSource.Factory getDataSourceFactory(boolean preview) {
        return new DefaultDataSourceFactory(mAppContext, preview ? null : new DefaultBandwidthMeter(),
                getHttpDataSourceFactory(preview));
    }

    private DataSource.Factory getHttpDataSourceFactory(boolean preview) {
        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(mAppContext,
                TAG), preview ? null : new DefaultBandwidthMeter());
        if (mHeaders != null && mHeaders.size() > 0) {
            for (Map.Entry<String, String> header : mHeaders.entrySet()) {
                dataSourceFactory.getDefaultRequestProperties().set(header.getKey(), header.getValue());
            }
        }
        return dataSourceFactory;
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        //重新播放状态顺序为：STATE_IDLE -》STATE_BUFFERING -》STATE_READY
        //缓冲时顺序为：STATE_BUFFERING -》STATE_READY
        //Log.e(TAG, "onPlayerStateChanged: playWhenReady = " + playWhenReady + ", playbackState = " + playbackState);
        if (lastReportedPlayWhenReady != playWhenReady || lastReportedPlaybackState != playbackState) {
            if (mIsBuffering) {
                switch (playbackState) {
                    case Player.STATE_ENDED:
                    case Player.STATE_READY:
                        notifyOnInfo(IMediaPlayer.MEDIA_INFO_BUFFERING_END, mInternalPlayer.getBufferedPercentage());
                        mIsBuffering = false;
                        break;
                }
            }

            if (mIsPrepareing) {
                switch (playbackState) {
                    case Player.STATE_READY:
                        notifyOnPrepared();
                        mIsPrepareing = false;
                        break;
                }
            }

            switch (playbackState) {
                case Player.STATE_BUFFERING:
                    notifyOnInfo(IMediaPlayer.MEDIA_INFO_BUFFERING_START, mInternalPlayer.getBufferedPercentage());
                    mIsBuffering = true;
                    break;
                case Player.STATE_READY:
                    break;
                case Player.STATE_ENDED:
                    notifyOnCompletion();
                    break;
                default:
                    break;
            }
        }
        lastReportedPlayWhenReady = playWhenReady;
        lastReportedPlaybackState = playbackState;
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        notifyOnError(IMediaPlayer.MEDIA_ERROR_UNKNOWN, IMediaPlayer.MEDIA_ERROR_UNKNOWN);
    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }

    /////////////////////////////////////AudioRendererEventListener/////////////////////////////////////////////


    @Override
    public void onPlayerStateChanged(EventTime eventTime, boolean playWhenReady, int playbackState) {

    }

    @Override
    public void onTimelineChanged(EventTime eventTime, int reason) {

    }

    @Override
    public void onPositionDiscontinuity(EventTime eventTime, int reason) {

    }

    @Override
    public void onSeekStarted(EventTime eventTime) {

    }

    @Override
    public void onSeekProcessed(EventTime eventTime) {

    }

    @Override
    public void onPlaybackParametersChanged(EventTime eventTime, PlaybackParameters playbackParameters) {

    }

    @Override
    public void onRepeatModeChanged(EventTime eventTime, int repeatMode) {

    }

    @Override
    public void onShuffleModeChanged(EventTime eventTime, boolean shuffleModeEnabled) {

    }

    @Override
    public void onLoadingChanged(EventTime eventTime, boolean isLoading) {

    }

    @Override
    public void onPlayerError(EventTime eventTime, ExoPlaybackException error) {

    }

    @Override
    public void onTracksChanged(EventTime eventTime, TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadStarted(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData) {

    }

    @Override
    public void onLoadCompleted(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData) {

    }

    @Override
    public void onLoadCanceled(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData) {

    }

    @Override
    public void onLoadError(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData, IOException error, boolean wasCanceled) {

    }

    @Override
    public void onDownstreamFormatChanged(EventTime eventTime, MediaSourceEventListener.MediaLoadData mediaLoadData) {

    }

    @Override
    public void onUpstreamDiscarded(EventTime eventTime, MediaSourceEventListener.MediaLoadData mediaLoadData) {

    }

    @Override
    public void onMediaPeriodCreated(EventTime eventTime) {

    }

    @Override
    public void onMediaPeriodReleased(EventTime eventTime) {

    }

    @Override
    public void onReadingStarted(EventTime eventTime) {

    }

    @Override
    public void onBandwidthEstimate(EventTime eventTime, int totalLoadTimeMs, long totalBytesLoaded, long bitrateEstimate) {

    }

    @Override
    public void onViewportSizeChange(EventTime eventTime, int width, int height) {

    }

    @Override
    public void onNetworkTypeChanged(EventTime eventTime, @Nullable NetworkInfo networkInfo) {

    }

    @Override
    public void onMetadata(EventTime eventTime, Metadata metadata) {

    }

    @Override
    public void onDecoderEnabled(EventTime eventTime, int trackType, DecoderCounters decoderCounters) {

    }

    @Override
    public void onDecoderInitialized(EventTime eventTime, int trackType, String decoderName, long initializationDurationMs) {

    }

    @Override
    public void onDecoderInputFormatChanged(EventTime eventTime, int trackType, Format format) {

    }

    @Override
    public void onDecoderDisabled(EventTime eventTime, int trackType, DecoderCounters decoderCounters) {
        audioSessionId = C.AUDIO_SESSION_ID_UNSET;
    }

    @Override
    public void onAudioSessionId(EventTime eventTime, int audioSessionId) {
        this.audioSessionId = audioSessionId;
    }

    @Override
    public void onAudioUnderrun(EventTime eventTime, int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {

    }

    @Override
    public void onDroppedVideoFrames(EventTime eventTime, int droppedFrames, long elapsedMs) {

    }

    @Override
    public void onVideoSizeChanged(EventTime eventTime, int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        mVideoWidth = width;
        mVideoHeight = height;
        notifyOnVideoSizeChanged(width, height, 1, 1);
        if (unappliedRotationDegrees > 0)
            notifyOnInfo(IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED, unappliedRotationDegrees);
    }

    @Override
    public void onRenderedFirstFrame(EventTime eventTime, Surface surface) {

    }

    @Override
    public void onDrmKeysLoaded(EventTime eventTime) {

    }

    @Override
    public void onDrmSessionManagerError(EventTime eventTime, Exception error) {

    }

    @Override
    public void onDrmKeysRestored(EventTime eventTime) {

    }

    @Override
    public void onDrmKeysRemoved(EventTime eventTime) {

    }



    /*************自定义***************/


    private final Timeline.Window window = new Timeline.Window();
    private static final long MAX_POSITION_FOR_SEEK_TO_PREVIOUS = 3000;

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
