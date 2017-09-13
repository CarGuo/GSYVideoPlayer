package com.example.gsyvideoplayer;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.gsyvideoplayer.listener.SampleListener;
import com.example.gsyvideoplayer.video.LandLayoutVideo;
import com.example.gsyvideoplayer.view.CustomInputDialog;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InputUrlDetailActivity extends AppCompatActivity {

    @BindView(R.id.post_detail_nested_scroll)
    NestedScrollView postDetailNestedScroll;

    //推荐使用StandardGSYVideoPlayer，功能一致
    //CustomGSYVideoPlayer部分功能处于试验阶段
    @BindView(R.id.detail_player)
    LandLayoutVideo detailPlayer;

    @BindView(R.id.activity_detail_player)
    RelativeLayout activityDetailPlayer;

    @BindView(R.id.inputUrl)
    Button inputUrl;


    private boolean isPlay;
    private boolean isPause;
    private boolean cache;
    private String url;

    private OrientationUtils orientationUtils;

    private GSYVideoOptionBuilder gsyVideoOptionBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_url_detail);
        ButterKnife.bind(this);

        url = "http://baobab.wdjcdn.com/14564977406580.mp4";

        //增加封面
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.xxx1);

        resolveNormalVideoUI();

        //外部辅助的旋转，帮助全屏
        orientationUtils = new OrientationUtils(this, detailPlayer);
        //初始化不打开外部的旋转
        orientationUtils.setEnable(false);

        gsyVideoOptionBuilder = new GSYVideoOptionBuilder()
                .setThumbImageView(imageView)
                .setIsTouchWiget(true)
                .setRotateViewAuto(false)
                .setLockLand(false)
                .setShowFullAnimation(false)
                .setNeedLockFull(true)
                .setSeekRatio(1)
                .setUrl(url)
                .setCacheWithPlay(cache)
                .setVideoTitle("测试视频")
                .setStandardVideoAllCallBack(new SampleListener() {
                    @Override
                    public void onPrepared(String url, Object... objects) {
                        super.onPrepared(url, objects);
                        //开始播放了才能旋转和全屏
                        orientationUtils.setEnable(true);
                        isPlay = true;
                    }

                    @Override
                    public void onQuitFullscreen(String url, Object... objects) {
                        super.onQuitFullscreen(url, objects);
                        if (orientationUtils != null) {
                            orientationUtils.backToProtVideo();
                        }
                    }
                });
        gsyVideoOptionBuilder.build(detailPlayer);

        detailPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //直接横屏
                orientationUtils.resolveByClick();

                //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
                detailPlayer.startWindowFullscreen(InputUrlDetailActivity.this, true, true);
            }
        });

        detailPlayer.setLockClickListener(new LockClickListener() {
            @Override
            public void onClick(View view, boolean lock) {
                if (orientationUtils != null) {
                    //配合下方的onConfigurationChanged
                    orientationUtils.setEnable(!lock);
                }
            }
        });

        inputUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog();
            }
        });

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
        getCurPlay().onVideoPause();
        super.onPause();
        isPause = true;
    }

    @Override
    protected void onResume() {
        getCurPlay().onVideoResume();
        super.onResume();
        isPause = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isPlay) {
            getCurPlay().release();
        }
        //GSYPreViewManager.instance().releaseMediaPlayer();
        if (orientationUtils != null)
            orientationUtils.releaseListener();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //如果旋转了就全屏
        if (isPlay && !isPause) {
            detailPlayer.onConfigurationChanged(this, newConfig, orientationUtils);
        }
    }

    private GSYVideoPlayer getCurPlay() {
        if (detailPlayer.getFullWindowPlayer() != null) {
            return  detailPlayer.getFullWindowPlayer();
        }
        return detailPlayer;
    }

    private void playVideo() {
        detailPlayer.release();
        gsyVideoOptionBuilder.setUrl(url)
                .setCacheWithPlay(cache)
                .setVideoTitle("测试视频")
                .build(detailPlayer);
        gsyVideoOptionBuilder.build(detailPlayer);
        detailPlayer.postDelayed(new Runnable() {
            @Override
            public void run() {
                detailPlayer.startPlayLogic();
            }
        }, 1000);
    }

    private void resolveNormalVideoUI() {
        //增加title
        detailPlayer.getTitleTextView().setVisibility(View.GONE);
        detailPlayer.getBackButton().setVisibility(View.GONE);
    }


    private void showInputDialog() {
        final CustomInputDialog customInputDialog = new CustomInputDialog(this);
        customInputDialog.setInput(url);
        customInputDialog.setCache(cache);
        customInputDialog.setTitle("请输入URL");
        customInputDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new CustomInputDialog.OnClickListener() {
            @Override
            public void onInputChanged(String input, boolean cache) {
                url = input;
                InputUrlDetailActivity.this.cache = cache;
            }

            @Override
            public void onClick(DialogInterface dialog, int which) {
                url = customInputDialog.getEditMessage().getText().toString();
                playVideo();
            }
        });
        customInputDialog.setCancelable(true);
        customInputDialog.show();
    }


}
