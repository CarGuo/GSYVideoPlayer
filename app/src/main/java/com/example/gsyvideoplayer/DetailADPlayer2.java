package com.example.gsyvideoplayer;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.shuyu.gsyvideoplayer.GSYBaseActivityDetail;
import com.shuyu.gsyvideoplayer.GSYVideoADManager;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.NormalGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.GSYADVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;


public class DetailADPlayer2 extends GSYBaseActivityDetail<NormalGSYVideoPlayer> {

    private OrientationUtils adOrientationUtils;

    private NormalGSYVideoPlayer detailPlayer;

    private GSYADVideoPlayer adPlayer;

    private String urlAd = "http://video.7k.cn/app_video/20171202/6c8cf3ea/v.m3u8.mp4";

    private String urlAd2 = "http://video.7k.cn/app_video/20171202/6c8cf3ea/v.m3u8.mp4";

    private String url = "http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4";

    private boolean isAdPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_ad_player2);

        detailPlayer = (NormalGSYVideoPlayer) findViewById(R.id.detail_player);
        adPlayer = (GSYADVideoPlayer) findViewById(R.id.ad_player);

        //普通模式
        resolveNormalVideoUI();

        initVideoBuilderMode();

        detailPlayer.setLockClickListener(new LockClickListener() {
            @Override
            public void onClick(View view, boolean lock) {
                if (orientationUtils != null) {
                    //配合下方的onConfigurationChanged
                    orientationUtils.setEnable(!lock);
                }
            }
        });
        detailPlayer.setStartAfterPrepared(false);
        detailPlayer.setReleaseWhenLossAudio(false);

        final GSYVideoOptionBuilder adBuilder = getGSYVideoOptionBuilder();
        adBuilder.setUrl(urlAd)
                .setVideoAllCallBack(new GSYSampleCallBack() {

                    @Override
                    public void onStartPrepared(String url, Object... objects) {
                        super.onStartPrepared(url, objects);
                        isAdPlayer = true;
                        //开始播放了才能旋转和全屏
                        adOrientationUtils.setEnable(getDetailOrientationRotateAuto());
                    }

                    @Override
                    public void onPrepared(String url, Object... objects) {
                        super.onPrepared(url, objects);
                    }

                    @Override
                    public void onAutoComplete(String url, Object... objects) {
                        adPlayer.release();
                        adPlayer.onVideoReset();
                        adPlayer.setVisibility(View.GONE);
                        //todo 如果在全屏下的处理
                        //todo 中间弹出逻辑处理
                        //todo 开始缓冲的时候问题
                        //todo 是否增加一个开始缓冲的回调
                        getGSYVideoPlayer().getCurrentPlayer().startAfterPrepared();
                        if (adPlayer.getCurrentPlayer().isIfCurrentIsFullscreen()) {
                            adPlayer.removeFullWindowViewOnly();
                            if (!getGSYVideoPlayer().getCurrentPlayer().isIfCurrentIsFullscreen()) {
                                showFull();
                                getGSYVideoPlayer().setSaveBeforeFullSystemUiVisibility(adPlayer.getSaveBeforeFullSystemUiVisibility());
                            }
                        }
                    }

                    @Override
                    public void onQuitFullscreen(String url, Object... objects) {
                        if (adOrientationUtils != null) {
                            adOrientationUtils.backToProtVideo();
                        }
                    }

                });
        adBuilder.build(adPlayer);

    }

    @Override
    public void initVideo() {
        super.initVideo();
        //外部辅助的旋转，帮助全屏
        adOrientationUtils = new OrientationUtils(this, adPlayer);
        //初始化不打开外部的旋转
        adOrientationUtils.setEnable(false);
        if (adPlayer.getFullscreenButton() != null) {
            adPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //直接横屏
                    adOrientationUtils.resolveByClick();
                    //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
                    adPlayer.startWindowFullscreen(DetailADPlayer2.this, hideActionBarWhenFull(), hideStatusBarWhenFull());
                }
            });
        }
    }

    @Override
    public void showFull() {
        if (orientationUtils.getIsLand() != 1) {
            //直接横屏
            orientationUtils.resolveByClick();
        }
        getGSYVideoPlayer().startWindowFullscreen(DetailADPlayer2.this);

    }

    @Override
    public void onBackPressed() {
        if (adOrientationUtils != null) {
            adOrientationUtils.backToProtVideo();
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
        if (adOrientationUtils != null)
            adOrientationUtils.releaseListener();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //如果旋转了就全屏
        boolean backUpIsPlay = isPlay;
        if (isAdPlayer && !isPause) {
            if (adPlayer.getCurrentPlayer().isInPlayingState()) {
                isPlay = false;
                adPlayer.onConfigurationChanged(this, newConfig, adOrientationUtils);
            }
        }
        super.onConfigurationChanged(newConfig);
        isPlay = backUpIsPlay;
    }

    @Override
    public NormalGSYVideoPlayer getGSYVideoPlayer() {
        return detailPlayer;
    }

    @Override
    public GSYVideoOptionBuilder getGSYVideoOptionBuilder() {
        //不需要builder的
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.xxx1);
        return new GSYVideoOptionBuilder()
                .setThumbImageView(imageView)
                .setUrl(url)
                .setCacheWithPlay(true)
                .setVideoTitle(" ")
                .setFullHideActionBar(true)
                .setFullHideStatusBar(true)
                .setIsTouchWiget(true)
                .setRotateViewAuto(false)
                .setLockLand(false)
                .setShowFullAnimation(false)//打开动画
                .setNeedLockFull(true)
                .setSeekRatio(1);
    }

    @Override
    public void onStartPrepared(String url, Object... objects) {
        super.onStartPrepared(url, objects);
        adPlayer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPrepared(String url, Object... objects) {
        super.onPrepared(url, objects);
        adPlayer.startPlayLogic();
    }

    @Override
    public void clickForFullScreen() {

    }

    /**
     * 是否启动旋转横屏，true表示启动
     *
     * @return true
     */
    @Override
    public boolean getDetailOrientationRotateAuto() {
        return true;
    }

    @Override
    public void onEnterFullscreen(String url, Object... objects) {
        super.onEnterFullscreen(url, objects);
        //隐藏调全屏对象的返回按键
        GSYVideoPlayer gsyVideoPlayer = (GSYVideoPlayer) objects[1];
        gsyVideoPlayer.getBackButton().setVisibility(View.GONE);
    }


    private void resolveNormalVideoUI() {
        //增加title
        detailPlayer.getTitleTextView().setVisibility(View.VISIBLE);
        detailPlayer.getBackButton().setVisibility(View.VISIBLE);
    }
}

