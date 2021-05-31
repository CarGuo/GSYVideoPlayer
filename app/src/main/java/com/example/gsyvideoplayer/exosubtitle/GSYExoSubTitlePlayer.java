package com.example.gsyvideoplayer.exosubtitle;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.MimeTypes;

import java.util.List;

import tv.danmaku.ijk.media.exo2.IjkExo2MediaPlayer;
import tv.danmaku.ijk.media.exo2.demo.EventLogger;

import static com.google.android.exoplayer2.util.Assertions.checkNotNull;

public class GSYExoSubTitlePlayer extends IjkExo2MediaPlayer {

    private String mSubTitile;
    private TextOutput mTextOutput;

    public GSYExoSubTitlePlayer(Context context) {
        super(context);
    }


    @Override
    public void onCues(List<Cue> cues) {
        super.onCues(cues);
        /// 这里
    }

    @Override
    public void onMetadata(Metadata metadata) {
        super.onMetadata(metadata);
        /// 这里
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
                        mInternalPlayer = new SimpleExoPlayer.Builder(mAppContext, mRendererFactory)
                                .setLooper(Looper.getMainLooper())
                                .setTrackSelector(mTrackSelector)
                                .setLoadControl(mLoadControl).build();
                        mInternalPlayer.addListener(GSYExoSubTitlePlayer.this);
                        mInternalPlayer.addAnalyticsListener(GSYExoSubTitlePlayer.this);
                        if (mTextOutput != null) {
                            mInternalPlayer.addTextOutput(mTextOutput);
                        }
                        mInternalPlayer.addListener(mEventLogger);
                        if (mSpeedPlaybackParameters != null) {
                            mInternalPlayer.setPlaybackParameters(mSpeedPlaybackParameters);
                        }
                        if (mSurface != null)
                            mInternalPlayer.setVideoSurface(mSurface);

                        if (mSubTitile != null) {
                            MediaSource textMediaSource = getTextSource(Uri.parse(mSubTitile));
                            mMediaSource = new MergingMediaSource(mMediaSource, textMediaSource);
                        }
                        mInternalPlayer.setMediaSource(mMediaSource);
                        mInternalPlayer.prepare();
                        mInternalPlayer.setPlayWhenReady(false);
                    }
                }
        );
    }

    public MediaSource getTextSource(Uri subTitle) {
        //todo C.SELECTION_FLAG_AUTOSELECT language MimeTypes
        Format textFormat = new Format.Builder()
                /// 其他的比如 text/x-ssa ，text/vtt，application/ttml+xml 等等
                .setSampleMimeType(MimeTypes.APPLICATION_SUBRIP)
                .setSelectionFlags(C.SELECTION_FLAG_FORCED)
                /// 如果出现字幕不显示，可以通过修改这个语音去对应，
                //  这个问题在内部的 selectTextTrack 时，TextTrackScore 通过 getFormatLanguageScore 方法判断语言获取匹配不上
                //  就会不出现字幕
                .setLanguage("en")
                .build();

        MediaItem.Subtitle subtitle = new MediaItem.Subtitle(
                subTitle, checkNotNull(textFormat.sampleMimeType), textFormat.language, textFormat.selectionFlags);

        DefaultHttpDataSource.Factory  factory = new DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(50000)
                .setReadTimeoutMs(50000)
                .setTransferListener( new DefaultBandwidthMeter.Builder(mAppContext).build());

        MediaSource textMediaSource = new SingleSampleMediaSource.Factory(new DefaultDataSourceFactory(mAppContext, null,
                factory))
                .createMediaSource(subtitle, C.TIME_UNSET);
        return textMediaSource;

    }


    public String getSubTitile() {
        return mSubTitile;
    }

    public void setSubTitile(String subTitile) {
        this.mSubTitile = subTitile;
    }

    public TextOutput getTextOutput() {
        return mTextOutput;
    }

    public void setTextOutput(TextOutput textOutput) {
        this.mTextOutput = textOutput;
    }

    public void addTextOutputPlaying(TextOutput textOutput) {
        if (mInternalPlayer != null) {
            mInternalPlayer.addTextOutput(textOutput);
        }
    }

    public void removeTextOutput(TextOutput textOutput) {
        if (mInternalPlayer != null) {
            mInternalPlayer.removeTextOutput(textOutput);
        }
    }

}
