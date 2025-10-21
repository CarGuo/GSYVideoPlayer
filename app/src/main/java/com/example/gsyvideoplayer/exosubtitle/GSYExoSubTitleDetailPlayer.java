package com.example.gsyvideoplayer.exosubtitle;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.C;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.exoplayer.SeekParameters;
import androidx.media3.exoplayer.source.TrackGroupArray;
import androidx.media3.exoplayer.trackselection.MappingTrackSelector;
import androidx.media3.exoplayer.trackselection.TrackSelector;

import com.example.gsyvideoplayer.R;
import com.example.gsyvideoplayer.databinding.ActivityDetailExoSubtitlePlayerBinding;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.listener.GSYVideoProgressListener;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import java.util.HashMap;
import java.util.Map;

import tv.danmaku.ijk.media.exo2.IjkExo2MediaPlayer;


public class GSYExoSubTitleDetailPlayer extends AppCompatActivity {

    private boolean isPlay;
    private boolean isPause;

    private OrientationUtils orientationUtils;
    private ActivityDetailExoSubtitlePlayerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityDetailExoSubtitlePlayerBinding.inflate(getLayoutInflater());

        View rootView = binding.getRoot();
        setContentView(rootView);



        String url = getUrl();

        //增加封面
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.xxx1);

        resolveNormalVideoUI();

        //外部辅助的旋转，帮助全屏
        orientationUtils = new OrientationUtils(this, binding.detailPlayer);
        //初始化不打开外部的旋转
        orientationUtils.setEnable(false);


        Map<String, String> header = new HashMap<>();
        header.put("ee", "33");
        header.put("allowCrossProtocolRedirects", "true");
        GSYVideoOptionBuilder gsyVideoOption = new GSYVideoOptionBuilder();
        gsyVideoOption.setThumbImageView(imageView)
            .setIsTouchWiget(true)
            .setRotateViewAuto(false)
            .setLockLand(false)
            .setAutoFullWithSize(false)
            .setShowFullAnimation(false)
            .setNeedLockFull(true)
            .setUrl(url)
            .setMapHeadData(header)
            .setCacheWithPlay(false)
            .setVideoTitle("测试视频")
            .setVideoAllCallBack(new GSYSampleCallBack() {
                @Override
                public void onPrepared(String url, Object... objects) {
                    Debuger.printfError("***** onPrepared **** " + objects[0]);
                    Debuger.printfError("***** onPrepared **** " + objects[1]);
                    super.onPrepared(url, objects);
                    //开始播放了才能旋转和全屏
                    orientationUtils.setEnable(binding.detailPlayer.isRotateWithSystem());
                    isPlay = true;

                    //设置 seek 的临近帧。
                    if (binding.detailPlayer.getGSYVideoManager().getPlayer() instanceof GSYExoSubTitlePlayerManager) {
                        ((GSYExoSubTitlePlayerManager) binding.detailPlayer.getGSYVideoManager().getPlayer()).setSeekParameter(SeekParameters.NEXT_SYNC);
                        Debuger.printfError("***** setSeekParameter **** ");
                    }


                    ///TODO 注意，用这个 M3U8 的话，内部会有内嵌字幕 embedded caption
                    ///TODO 所以就算你加了外挂字幕，也需要再切换一次才能看到外部字幕
                    ///TODO 这里输出所有字幕信息
                    if (binding.detailPlayer.getGSYVideoManager().getPlayer() instanceof GSYExoSubTitlePlayerManager) {
                        IjkExo2MediaPlayer player = ((IjkExo2MediaPlayer) binding.detailPlayer.getGSYVideoManager().getPlayer().getMediaPlayer());
                        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = player.getTrackSelector().getCurrentMappedTrackInfo();
                        if (mappedTrackInfo != null) {
                            for (int i = 0; i < mappedTrackInfo.getRendererCount(); i++) {
                                TrackGroupArray rendererTrackGroups = mappedTrackInfo.getTrackGroups(i);
                                if (C.TRACK_TYPE_TEXT == mappedTrackInfo.getRendererType(i)) { //判断是否是音轨
                                    for (int j = 0; j < rendererTrackGroups.length; j++) {
                                        TrackGroup trackGroup = rendererTrackGroups.get(j);
                                        Debuger.printfError("####### Text " + trackGroup.getFormat(0).toString() + " #######");
                                    }
                                }
                            }
                        }
                    }





                }

                @Override
                public void onEnterFullscreen(String url, Object... objects) {
                    super.onEnterFullscreen(url, objects);
                    Debuger.printfError("***** onEnterFullscreen **** " + objects[0]);//title
                    Debuger.printfError("***** onEnterFullscreen **** " + objects[1]);//当前全屏player
                }

                @Override
                public void onAutoComplete(String url, Object... objects) {
                    super.onAutoComplete(url, objects);
                }

                @Override
                public void onClickStartError(String url, Object... objects) {
                    super.onClickStartError(url, objects);
                }

                @Override
                public void onQuitFullscreen(String url, Object... objects) {
                    super.onQuitFullscreen(url, objects);
                    Debuger.printfError("***** onQuitFullscreen **** " + objects[0]);//title
                    Debuger.printfError("***** onQuitFullscreen **** " + objects[1]);//当前非全屏player

                    // ------- ！！！如果不需要旋转屏幕，可以不调用！！！-------
                    // 不需要屏幕旋转，还需要设置 setNeedOrientationUtils(false)
                    if (orientationUtils != null) {
                        orientationUtils.backToProtVideo();
                    }
                }
            })
            .setLockClickListener(new LockClickListener() {
                @Override
                public void onClick(View view, boolean lock) {
                    if (orientationUtils != null) {
                        //配合下方的onConfigurationChanged
                        orientationUtils.setEnable(!lock);
                    }
                }
            })
            .setGSYVideoProgressListener(new GSYVideoProgressListener() {
                @Override
                public void onProgress(long progress, long secProgress, long currentPosition, long duration) {
                    Debuger.printfLog(" progress " + progress + " secProgress " + secProgress + " currentPosition " + currentPosition + " duration " + duration);
                }
            })
            .build(binding.detailPlayer);
        binding.detailPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //直接横屏
                // ------- ！！！如果不需要旋转屏幕，可以不调用！！！-------
                // 不需要屏幕旋转，还需要设置 setNeedOrientationUtils(false)
                orientationUtils.resolveByClick();

                //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
                binding.detailPlayer.startWindowFullscreen(GSYExoSubTitleDetailPlayer.this, true, true);
            }
        });


        ///exo 切换音轨
        binding.change.setOnClickListener(new View.OnClickListener() {
            int index = 0;

            @Override
            public void onClick(View view) {

                ///TODO 注意，DEMO 的 getUrl 如果 M3U8 的话，内部可能会有内嵌字幕 embedded caption
                ///TODO 所以就算你加了外挂字幕，也需要再切换一次才能看到外部字幕
                if (binding.detailPlayer.getGSYVideoManager().getPlayer() instanceof GSYExoSubTitlePlayerManager) {
                    IjkExo2MediaPlayer player = ((IjkExo2MediaPlayer) binding.detailPlayer.getGSYVideoManager().getPlayer().getMediaPlayer());
                    TrackSelector trackSelector = player.getTrackSelector();
                    MappingTrackSelector.MappedTrackInfo mappedTrackInfo = player.getTrackSelector().getCurrentMappedTrackInfo();

                    if (mappedTrackInfo != null) {
                        for (int i = 0; i < mappedTrackInfo.getRendererCount(); i++) {
                            TrackGroupArray rendererTrackGroups = mappedTrackInfo.getTrackGroups(i);
                            if (C.TRACK_TYPE_TEXT == mappedTrackInfo.getRendererType(i)) { //判断是否是音轨
                                if (index == 0) {
                                    index = 1;
                                } else {
                                    index = 0;
                                }
                                if (rendererTrackGroups.length <= 1) {
                                    return;
                                }
                                TrackGroup trackGroup = rendererTrackGroups.get(index);
                                TrackSelectionParameters parameters = trackSelector.getParameters().buildUpon().setForceHighestSupportedBitrate(true).setOverrideForType(new TrackSelectionOverride(trackGroup, 0)).build();
                                trackSelector.setParameters(parameters);
                            }
                        }
                    }
                }
            }
        });

        binding.detailPlayer.setSubTitle("http://img.cdn.guoshuyu.cn/subtitle2.srt");

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (orientationUtils != null) {
                    orientationUtils.backToProtVideo();
                }
                if (GSYExoSubTitleVideoManager.backFromWindowFull(GSYExoSubTitleDetailPlayer.this)) {
                    return;
                }
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        getCurPlay().onVideoPause();
        super.onPause();
        isPause = true;
    }

    @Override
    protected void onResume() {
        getCurPlay().onVideoResume(false);
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


    /**
     * orientationUtils 和  detailPlayer.onConfigurationChanged 方法是用于触发屏幕旋转的
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //如果旋转了就全屏
        if (isPlay && !isPause) {
            binding.detailPlayer.onConfigurationChanged(this, newConfig, orientationUtils, true, true);
        }
    }


    private void resolveNormalVideoUI() {
        //增加title
        binding.detailPlayer.getTitleTextView().setVisibility(View.GONE);
        binding.detailPlayer.getBackButton().setVisibility(View.GONE);
    }

    private GSYVideoPlayer getCurPlay() {
        if (binding.detailPlayer.getFullWindowPlayer() != null) {
            return binding.detailPlayer.getFullWindowPlayer();
        }
        return binding.detailPlayer;
    }


    private String getUrl() {

        ///该 MP4 没有内嵌字幕
        //return "https://pointshow.oss-cn-hangzhou.aliyuncs.com/McTk51586843620689.mp4";

        ///TODO 注意，用这个 M3U8 的话，内部会有内嵌字幕 embedded caption
        ///TODO 所以就算你加了外挂字幕，也需要再切换一次才能看到外部字幕
        return "http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear3/prog_index.m3u8";
    }
}
