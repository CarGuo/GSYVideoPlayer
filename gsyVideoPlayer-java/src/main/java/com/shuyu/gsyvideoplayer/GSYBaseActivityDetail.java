package com.shuyu.gsyvideoplayer;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.StandardVideoAllCallBack;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;

/**
 * 详情模式播放页面基础类
 * Created by guoshuyu on 2017/9/14.
 */
public abstract class GSYBaseActivityDetail extends AppCompatActivity implements StandardVideoAllCallBack {

    protected boolean isPlay;

    protected boolean isPause;

    protected OrientationUtils orientationUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * 选择普通模式
     */
    public void initVideo() {
        //外部辅助的旋转，帮助全屏
        orientationUtils = new OrientationUtils(this, getGSYVideoPlayer());
        //初始化不打开外部的旋转
        orientationUtils.setEnable(false);
        if (getGSYVideoPlayer().getFullscreenButton() != null) {
            getGSYVideoPlayer().getFullscreenButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //直接横屏
                    orientationUtils.resolveByClick();
                    //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
                    getGSYVideoPlayer().startWindowFullscreen(GSYBaseActivityDetail.this, true, true);

                    clickForFullScreen();
                }
            });
        }
    }

    /**
     * 选择builder模式
     */
    public void initVideoBuilderMode() {
        initVideo();
        getGSYVideoOptionBuilder().
                setVideoAllCallBack(this)
                .build(getGSYVideoPlayer());
    }

    @Override
    public void onBackPressed() {
        if (orientationUtils != null) {
            orientationUtils.backToProtVideo();
        }
        if (StandardGSYVideoPlayer.backFromWindowFull(this)) {
            return;
        }
        super.onBackPressed();
    }


    @Override
    protected void onPause() {
        super.onPause();
        getGSYVideoPlayer().getCurrentPlayer().onVideoPause();
        isPause = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getGSYVideoPlayer().getCurrentPlayer().onVideoResume();
        isPause = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isPlay) {
            getGSYVideoPlayer().getCurrentPlayer().release();
        }
        if (orientationUtils != null)
            orientationUtils.releaseListener();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //如果旋转了就全屏
        if (isPlay && !isPause) {
            getGSYVideoPlayer().onConfigurationChanged(this, newConfig, orientationUtils);
        }
    }

    @Override
    public void onPrepared(String url, Object... objects) {

        if (orientationUtils == null) {
            throw new NullPointerException("initVideo() or initVideoBuilderMode() first");
        }
        //开始播放了才能旋转和全屏
        orientationUtils.setEnable(true);
        isPlay = true;
    }

    @Override
    public void onClickStartIcon(String url, Object... objects) {

    }

    @Override
    public void onClickStartError(String url, Object... objects) {

    }

    @Override
    public void onClickStop(String url, Object... objects) {

    }

    @Override
    public void onClickStopFullscreen(String url, Object... objects) {

    }

    @Override
    public void onClickResume(String url, Object... objects) {

    }

    @Override
    public void onClickResumeFullscreen(String url, Object... objects) {

    }

    @Override
    public void onClickSeekbar(String url, Object... objects) {

    }

    @Override
    public void onClickSeekbarFullscreen(String url, Object... objects) {

    }

    @Override
    public void onAutoComplete(String url, Object... objects) {

    }

    @Override
    public void onEnterFullscreen(String url, Object... objects) {

    }

    @Override
    public void onQuitFullscreen(String url, Object... objects) {
        if (orientationUtils != null) {
            orientationUtils.backToProtVideo();
        }
    }

    @Override
    public void onQuitSmallWidget(String url, Object... objects) {

    }

    @Override
    public void onEnterSmallWidget(String url, Object... objects) {

    }

    @Override
    public void onTouchScreenSeekVolume(String url, Object... objects) {

    }

    @Override
    public void onTouchScreenSeekPosition(String url, Object... objects) {

    }

    @Override
    public void onTouchScreenSeekLight(String url, Object... objects) {

    }

    @Override
    public void onPlayError(String url, Object... objects) {

    }

    @Override
    public void onClickStartThumb(String url, Object... objects) {

    }

    @Override
    public void onClickBlank(String url, Object... objects) {

    }

    @Override
    public void onClickBlankFullscreen(String url, Object... objects) {

    }

    /**
     * 播放控件
     */
    public abstract GSYBaseVideoPlayer getGSYVideoPlayer();

    /**
     * 配置播放器
     */
    public abstract GSYVideoOptionBuilder getGSYVideoOptionBuilder();

    /**
     * 点击了全屏
     */
    public abstract void clickForFullScreen();
}
