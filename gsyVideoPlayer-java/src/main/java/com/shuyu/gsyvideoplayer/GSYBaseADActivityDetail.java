package com.shuyu.gsyvideoplayer;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.listener.VideoAllCallBack;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.GSYADVideoPlayer;
import com.shuyu.gsyvideoplayer.video.NormalGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

/**
 * 详情AD模式播放页面基础类
 * Created by guoshuyu on 2017/9/14.
 */
public abstract class GSYBaseADActivityDetail<T extends GSYBaseVideoPlayer, R extends GSYADVideoPlayer> extends GSYBaseActivityDetail<T> {

    protected OrientationUtils mADOrientationUtils;

    protected boolean isAdPlayed;

    @Override
    public void initVideo() {
        super.initVideo();
        //外部辅助的旋转，帮助全屏
        mADOrientationUtils = new OrientationUtils(this, getGSYADVideoPlayer());
        //初始化不打开外部的旋转
        mADOrientationUtils.setEnable(false);
        if (getGSYADVideoPlayer().getFullscreenButton() != null) {
            getGSYADVideoPlayer().getFullscreenButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //直接横屏
                    showADFull();
                    clickForFullScreen();
                }
            });
        }
    }

    /**
     * 选择builder模式
     */
    @Override
    public void initVideoBuilderMode() {
        super.initVideoBuilderMode();
        getGSYADVideoOptionBuilder()
                .setVideoAllCallBack(new GSYSampleCallBack() {

                    @Override
                    public void onStartPrepared(String url, Object... objects) {
                        super.onStartPrepared(url, objects);
                        isAdPlayed = true;
                        //开始播放了才能旋转和全屏
                        mADOrientationUtils.setEnable(getDetailOrientationRotateAuto());
                    }

                    @Override
                    public void onAutoComplete(String url, Object... objects) {
                        getGSYADVideoPlayer().release();
                        getGSYADVideoPlayer().onVideoReset();
                        getGSYADVideoPlayer().setVisibility(View.GONE);
                        //todo 如果在全屏下的处理
                        //todo 中间弹出逻辑处理
                        //todo 开始缓冲的时候问题
                        //todo 是否增加一个开始缓冲的回调
                        getGSYVideoPlayer().getCurrentPlayer().startAfterPrepared();
                        if (getGSYADVideoPlayer().getCurrentPlayer().isIfCurrentIsFullscreen()) {
                            getGSYADVideoPlayer().removeFullWindowViewOnly();
                            if (!getGSYVideoPlayer().getCurrentPlayer().isIfCurrentIsFullscreen()) {
                                showFull();
                                getGSYVideoPlayer().setSaveBeforeFullSystemUiVisibility(getGSYADVideoPlayer().getSaveBeforeFullSystemUiVisibility());
                            }
                        }
                    }

                    @Override
                    public void onQuitFullscreen(String url, Object... objects) {
                        if (mADOrientationUtils != null) {
                            mADOrientationUtils.backToProtVideo();
                        }
                    }

                })
                .build(getGSYADVideoPlayer());
    }


    @Override
    public void showFull() {
        if (orientationUtils.getIsLand() != 1) {
            //直接横屏
            orientationUtils.resolveByClick();
        }
        getGSYVideoPlayer().startWindowFullscreen(this, hideActionBarWhenFull(), hideStatusBarWhenFull());
    }

    @Override
    public void onBackPressed() {
        if (mADOrientationUtils != null) {
            mADOrientationUtils.backToProtVideo();
        }
        if (GSYVideoADManager.backFromWindowFull(this)) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        GSYVideoADManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        GSYVideoADManager.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GSYVideoADManager.releaseAllVideos();
        if (mADOrientationUtils != null)
            mADOrientationUtils.releaseListener();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //如果旋转了就全屏
        boolean backUpIsPlay = isPlay;
        if (isAdPlayed && !isPause) {
            if (getGSYADVideoPlayer().getCurrentPlayer().isInPlayingState()) {
                isPlay = false;
                getGSYADVideoPlayer().getCurrentPlayer().onConfigurationChanged(this, newConfig, mADOrientationUtils);
            }
        }
        super.onConfigurationChanged(newConfig);
        isPlay = backUpIsPlay;
    }

    @Override
    public void onStartPrepared(String url, Object... objects) {
        super.onStartPrepared(url, objects);
        getGSYADVideoPlayer().setVisibility(View.VISIBLE);
    }

    @Override
    public void onPrepared(String url, Object... objects) {
        super.onPrepared(url, objects);
        getGSYADVideoPlayer().startPlayLogic();
    }

    @Override
    public void onEnterFullscreen(String url, Object... objects) {
        super.onEnterFullscreen(url, objects);
        //隐藏调全屏对象的返回按键
        GSYVideoPlayer gsyVideoPlayer = (GSYVideoPlayer) objects[1];
        gsyVideoPlayer.getBackButton().setVisibility(View.GONE);
    }

    @Override
    public void clickForFullScreen() {

    }

    public void showADFull() {
        mADOrientationUtils.resolveByClick();
        getGSYADVideoPlayer().startWindowFullscreen(GSYBaseADActivityDetail.this, hideActionBarWhenFull(), hideStatusBarWhenFull());
    }

    public abstract R getGSYADVideoPlayer();

    /**
     * 配置AD播放器
     */
    public abstract GSYVideoOptionBuilder getGSYADVideoOptionBuilder();
}
