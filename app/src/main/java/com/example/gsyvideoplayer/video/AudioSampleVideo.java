package com.example.gsyvideoplayer.video;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.gsyvideoplayer.R;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;

public class AudioSampleVideo extends StandardGSYVideoPlayer {

    ImageView mCoverImage;

    String mCoverOriginUrl;

    int mDefaultRes;


    public AudioSampleVideo(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public AudioSampleVideo(Context context) {
        super(context);
    }

    public AudioSampleVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        mCoverImage = (ImageView) findViewById(R.id.thumbImage);

        if (mThumbImageViewLayout != null &&
                (mCurrentState == -1 || mCurrentState == CURRENT_STATE_NORMAL || mCurrentState == CURRENT_STATE_ERROR)) {
            mThumbImageViewLayout.setVisibility(VISIBLE);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.audio_layout_cover;
    }

    public void loadCoverImage(String url, int res) {
        mCoverOriginUrl = url;
        mDefaultRes = res;
        Glide.with(getContext().getApplicationContext())
                .setDefaultRequestOptions(
                        new RequestOptions()
                                .frame(1000000)
                                .centerCrop()
                                .error(res)
                                .placeholder(res))
                .load(url)
                .into(mCoverImage);
    }

    @Override
    public GSYBaseVideoPlayer startWindowFullscreen(Context context, boolean actionBar, boolean statusBar) {
        GSYBaseVideoPlayer gsyBaseVideoPlayer = super.startWindowFullscreen(context, actionBar, statusBar);
        AudioSampleVideo audioSampleVideo = (AudioSampleVideo) gsyBaseVideoPlayer;
        audioSampleVideo.loadCoverImage(mCoverOriginUrl, mDefaultRes);
        return gsyBaseVideoPlayer;
    }


    @Override
    public GSYBaseVideoPlayer showSmallVideo(Point size, boolean actionBar, boolean statusBar) {
        //下面这里替换成你自己的强制转化
        AudioSampleVideo audioSampleVideo = (AudioSampleVideo) super.showSmallVideo(size, actionBar, statusBar);
        audioSampleVideo.mStartButton.setVisibility(GONE);
        audioSampleVideo.mStartButton = null;
        return audioSampleVideo;
    }

    @Override
    protected void cloneParams(GSYBaseVideoPlayer from, GSYBaseVideoPlayer to) {
        super.cloneParams(from, to);
        AudioSampleVideo sf = (AudioSampleVideo) from;
        AudioSampleVideo st = (AudioSampleVideo) to;
        st.mShowFullAnimation = sf.mShowFullAnimation;
    }


    /**
     * 退出window层播放全屏效果
     */
    @SuppressWarnings("ResourceType")
    @Override
    protected void clearFullscreenLayout() {
        if (!mFullAnimEnd) {
            return;
        }
        mIfCurrentIsFullscreen = false;
        int delay = 0;
        if (mOrientationUtils != null) {
            delay = mOrientationUtils.backToProtVideo();
            mOrientationUtils.setEnable(false);
            if (mOrientationUtils != null) {
                mOrientationUtils.releaseListener();
                mOrientationUtils = null;
            }
        }

        if (!mShowFullAnimation) {
            delay = 0;
        }

        final ViewGroup vp = (CommonUtil.scanForActivity(getContext())).findViewById(Window.ID_ANDROID_CONTENT);
        final View oldF = vp.findViewById(getFullId());
        if (oldF != null) {
            AudioSampleVideo gsyVideoPlayer = (AudioSampleVideo) oldF;
            gsyVideoPlayer.mIfCurrentIsFullscreen = false;
        }

        if (delay == 0) {
            backToNormal();
        } else {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    backToNormal();
                }
            }, delay);
        }

    }

    @Override
    protected void addTextureView() {

    }

    @Override
    protected void changeUiToNormal() {
        super.changeUiToNormal();
        setViewShowState(mThumbImageViewLayout, VISIBLE);
    }

    @Override
    protected void changeUiToPreparingShow() {
        super.changeUiToPreparingShow();
        setViewShowState(mThumbImageViewLayout, VISIBLE);
    }

    @Override
    protected void changeUiToPlayingShow() {
        super.changeUiToPlayingShow();
        setViewShowState(mThumbImageViewLayout, VISIBLE);
    }

    @Override
    protected void changeUiToPauseShow() {
        super.changeUiToPauseShow();
        setViewShowState(mThumbImageViewLayout, VISIBLE);
    }

    @Override
    protected void changeUiToPlayingBufferingShow() {
        super.changeUiToPlayingBufferingShow();
        setViewShowState(mThumbImageViewLayout, VISIBLE);
    }

    @Override
    protected void changeUiToCompleteShow() {
        super.changeUiToCompleteShow();
        setViewShowState(mThumbImageViewLayout, VISIBLE);
    }

    @Override
    protected void changeUiToError() {
        super.changeUiToError();
        setViewShowState(mThumbImageViewLayout, VISIBLE);
    }

    @Override
    protected void changeUiToPrepareingClear() {
        super.changeUiToPrepareingClear();
        setViewShowState(mThumbImageViewLayout, VISIBLE);
    }

    @Override
    protected void changeUiToPlayingClear() {
        super.changeUiToPlayingClear();
        setViewShowState(mThumbImageViewLayout, VISIBLE);
    }

    @Override
    protected void changeUiToPauseClear() {
        super.changeUiToPauseClear();
        setViewShowState(mThumbImageViewLayout, VISIBLE);
    }

    @Override
    protected void changeUiToPlayingBufferingClear() {
        super.changeUiToPlayingBufferingClear();
        setViewShowState(mThumbImageViewLayout, VISIBLE);
    }

    @Override
    protected void changeUiToClear() {
        super.changeUiToClear();
        setViewShowState(mThumbImageViewLayout, VISIBLE);
    }

    @Override
    protected void changeUiToCompleteClear() {
        super.changeUiToCompleteClear();
        setViewShowState(mThumbImageViewLayout, VISIBLE);
    }


}
