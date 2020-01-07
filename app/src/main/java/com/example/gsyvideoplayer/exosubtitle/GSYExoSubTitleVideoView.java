package com.example.gsyvideoplayer.exosubtitle;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.example.gsyvideoplayer.R;
import com.google.android.exoplayer2.text.CaptionStyleCompat;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.video.NormalGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import java.util.HashMap;

public class GSYExoSubTitleVideoView extends NormalGSYVideoPlayer {

    private SubtitleView mSubtitleView;
    private String mSubTitle;

    public GSYExoSubTitleVideoView(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public GSYExoSubTitleVideoView(Context context) {
        super(context);
    }

    public GSYExoSubTitleVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        mSubtitleView = findViewById(R.id.sub_title_view);


        mSubtitleView.setStyle(new CaptionStyleCompat(Color.BLACK, Color.TRANSPARENT, Color.TRANSPARENT, CaptionStyleCompat.EDGE_TYPE_NONE, CaptionStyleCompat.EDGE_TYPE_NONE, null));
        mSubtitleView.setFixedTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
    }

    @Override
    public int getLayoutId() {
        return R.layout.video_layout_subtitle;
    }


    @Override
    protected void startPrepare() {
        if (getGSYVideoManager().listener() != null) {
            getGSYVideoManager().listener().onCompletion();
        }
        if (mVideoAllCallBack != null) {
            Debuger.printfLog("onStartPrepared");
            mVideoAllCallBack.onStartPrepared(mOriginUrl, mTitle, this);
        }
        getGSYVideoManager().setListener(this);
        getGSYVideoManager().setPlayTag(mPlayTag);
        getGSYVideoManager().setPlayPosition(mPlayPosition);
        mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        try {
            if (mContext instanceof Activity) {
                ((Activity) mContext).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mBackUpPlayingBufferState = -1;

        getGSYVideoManager().prepare(mUrl, mSubTitle, mSubtitleView, (mMapHeadData == null) ? new HashMap<String, String>() : mMapHeadData, mLooping, mSpeed, mCache, mCachePath, mOverrideExtension);
        setStateAndUi(CURRENT_STATE_PREPAREING);
    }


    public String getSubTitle() {
        return mSubTitle;
    }

    public void setSubTitle(String subTitle) {
        this.mSubTitle = subTitle;
    }


    /**********以下重载 GSYVideoPlayer 的 全屏 SubtitleView 相关实现***********/


    @Override
    public GSYBaseVideoPlayer startWindowFullscreen(Context context, boolean actionBar, boolean statusBar) {
        GSYBaseVideoPlayer gsyBaseVideoPlayer = super.startWindowFullscreen(context, actionBar, statusBar);
        GSYExoSubTitleVideoView gsyExoSubTitleVideoView = (GSYExoSubTitleVideoView) gsyBaseVideoPlayer;
        ((GSYExoSubTitlePlayerManager) GSYExoSubTitleVideoManager.instance().getPlayer())
                .addTextOutputPlaying(gsyExoSubTitleVideoView.mSubtitleView);

        return gsyBaseVideoPlayer;
    }

    @Override
    protected void resolveNormalVideoShow(View oldF, ViewGroup vp, GSYVideoPlayer gsyVideoPlayer) {
        super.resolveNormalVideoShow(oldF, vp, gsyVideoPlayer);
        GSYExoSubTitleVideoView gsyExoSubTitleVideoView = (GSYExoSubTitleVideoView) gsyVideoPlayer;
        ((GSYExoSubTitlePlayerManager) GSYExoSubTitleVideoManager.instance().getPlayer())
                .removeTextOutput(gsyExoSubTitleVideoView.mSubtitleView);

    }


    /**********以下重载GSYVideoPlayer的GSYVideoViewBridge相关实现***********/


    @Override
    public GSYExoSubTitleVideoManager getGSYVideoManager() {
        GSYExoSubTitleVideoManager.instance().initContext(getContext().getApplicationContext());
        return GSYExoSubTitleVideoManager.instance();
    }

    @Override
    protected boolean backFromFull(Context context) {
        return GSYExoSubTitleVideoManager.backFromWindowFull(context);
    }

    @Override
    protected void releaseVideos() {
        GSYExoSubTitleVideoManager.releaseAllVideos();
    }

    @Override
    protected int getFullId() {
        return GSYExoSubTitleVideoManager.FULLSCREEN_ID;
    }

    @Override
    protected int getSmallId() {
        return GSYExoSubTitleVideoManager.SMALL_ID;

    }
}
