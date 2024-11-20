package com.example.gsyvideoplayer.exosubtitle;

import static androidx.media3.common.util.Assertions.checkNotNull;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;


import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.exo2.IjkExo2MediaPlayer;
import tv.danmaku.ijk.media.exo2.demo.EventLogger;

import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Metadata;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.Player;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.common.text.Cue;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.MergingMediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter;
import androidx.media3.extractor.Extractor;
import androidx.media3.extractor.ExtractorsFactory;
import androidx.media3.extractor.text.DefaultSubtitleParserFactory;
import androidx.media3.extractor.text.SubtitleExtractor;
import androidx.media3.extractor.text.SubtitleParser;

public class GSYExoSubTitlePlayer extends IjkExo2MediaPlayer {

    private String mSubTitile;
    private Player.Listener mTextOutput;

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
                            ///todo 这里设置  setSelectUndeterminedTextLanguage 无视语言选择
                            mTrackSelector = new DefaultTrackSelector(mAppContext,
                                new TrackSelectionParameters.Builder(mAppContext).
                                    setSelectUndeterminedTextLanguage(true).build());
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
                        mInternalPlayer.addListener(GSYExoSubTitlePlayer.this);
                        mInternalPlayer.addAnalyticsListener(GSYExoSubTitlePlayer.this);
                        if (mTextOutput != null) {
                            mInternalPlayer.addListener(mTextOutput);
                        }
                        mInternalPlayer.addListener(mEventLogger);
                        if (mSpeedPlaybackParameters != null) {
                            mInternalPlayer.setPlaybackParameters(mSpeedPlaybackParameters);
                        }
                        if (mSurface != null)
                            mInternalPlayer.setVideoSurface(mSurface);


                        ///其实如果你没什么特色需求，可以直接用下面这种方式
//                        List<MediaItem.SubtitleConfiguration> list = new ArrayList<>();
//                        if (mSubTitile != null) {
//                            MediaItem.SubtitleConfiguration subtitle
//                                = new MediaItem.SubtitleConfiguration.Builder(Uri.parse(mSubTitile))
//                                .setMimeType(MimeTypes.APPLICATION_SUBRIP)
//                                .setLanguage(null)
//                                .setSelectionFlags(C.SELECTION_FLAG_FORCED)
//                                .build();
//                            list.add(subtitle);
//                        }
//                        MediaItem mediaItem = new MediaItem.Builder()
//                            .setUri(mDataSource).
//                            setSubtitleConfigurations(list).build();
//                        mInternalPlayer.setMediaItem(mediaItem);

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
        ///TODO 注意，如果原来视频有内嵌字幕，例如 M3U8 embedded caption
        ///TODO 所以就算你加了外挂字幕，也需要再切换一次 track 才能看到外部字幕，具体看 GSYExoSubTitleDetailPlayer
        Format format = new Format.Builder()
            /// 其他的比如 text/x-ssa ，text/vtt，application/ttml+xml 等等
            .setSampleMimeType(MimeTypes.APPLICATION_SUBRIP)
            .setSelectionFlags(C.SELECTION_FLAG_FORCED)
            /// 如果出现字幕不显示，可以通过修改这个语音去对应，
            //  这个问题在内部的 selectTextTrack 时，TextTrackScore 通过 getFormatLanguageScore 方法判断语言获取匹配不上
            //  就会不出现字幕
            .setLanguage(null)
            .build();

        MediaItem.SubtitleConfiguration subtitle = new MediaItem.SubtitleConfiguration.Builder(subTitle)
            .setMimeType(checkNotNull(format.sampleMimeType))
            .setLanguage(format.language)
            .setSelectionFlags(format.selectionFlags).build();

        DefaultHttpDataSource.Factory factory = new DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(50000)
            .setReadTimeoutMs(50000)
            .setTransferListener(new DefaultBandwidthMeter.Builder(mAppContext).build());

        SubtitleParser.Factory subtitleParserFactory = new DefaultSubtitleParserFactory();

        ExtractorsFactory extractorsFactory =
            () ->
                new Extractor[]{
                    new SubtitleExtractor(subtitleParserFactory.create(format), format)
                };
        ProgressiveMediaSource.Factory progressiveMediaSourceFactory =
            new ProgressiveMediaSource.Factory(new DefaultDataSource.Factory(mAppContext,
                factory), extractorsFactory);

        return  progressiveMediaSourceFactory.createMediaSource(
            MediaItem.fromUri(subtitle.uri.toString()));

    }


    public String getSubTitile() {
        return mSubTitile;
    }

    public void setSubTitile(String subTitile) {
        this.mSubTitile = subTitile;
    }

    public Player.Listener getTextOutput() {
        return mTextOutput;
    }

    public void setTextOutput(Player.Listener textOutput) {
        this.mTextOutput = textOutput;
    }

    public void addTextOutputPlaying(Player.Listener textOutput) {
        if (mInternalPlayer != null) {
            mInternalPlayer.addListener(textOutput);
        }
    }

    public void removeTextOutput(Player.Listener textOutput) {
        if (mInternalPlayer != null) {
            mInternalPlayer.removeListener(textOutput);
        }
    }

}
