package com.example.gsyvideoplayer;


import static androidx.media3.common.PlaybackException.*;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.C;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.common.Tracks;
import androidx.media3.exoplayer.SeekParameters;
import androidx.media3.exoplayer.source.TrackGroupArray;
import androidx.media3.exoplayer.trackselection.MappingTrackSelector;
import androidx.media3.exoplayer.trackselection.TrackSelector;
import androidx.media3.extractor.mp4.Track;

import com.example.gsyvideoplayer.databinding.ActivityDetailPlayerBinding;
import com.example.gsyvideoplayer.utils.DemoVideoUrls;
import com.google.common.collect.ImmutableList;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.listener.GSYVideoProgressListener;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.model.VideoOptionModel;
import com.shuyu.gsyvideoplayer.player.IPlayerManager;
import com.shuyu.gsyvideoplayer.player.SystemPlayerManager;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.exo2.Exo2PlayerManager;
import tv.danmaku.ijk.media.exo2.IjkExo2MediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;


public class DetailPlayer extends AppCompatActivity {


    private boolean isPlay;
    private boolean isPause;
    private boolean inPipMode = false;

    private OrientationUtils orientationUtils;

    private ActivityDetailPlayerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDetailPlayerBinding.inflate(getLayoutInflater());

        View rootView = binding.getRoot();
        setContentView(rootView);


        String url = getUrl();

        //binding.detailPlayer.setUp(url, false, null, "测试视频");
        //binding.detailPlayer.setLooping(true);
        //binding.detailPlayer.setShowPauseCover(false);

        //如果视频帧数太高导致卡画面不同步
        //VideoOptionModel videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 30);
        //如果视频seek之后从头播放
//        VideoOptionModel videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
//        List<VideoOptionModel> list = new ArrayList<>();
//        list.add(videoOptionModel);
//        GSYVideoManager.instance().setOptionModelList(list);

        //GSYVideoManager.instance().setTimeOut(4000, true);


        /***************rtsp 配置****************/
        /*VideoOptionModel videoOptionModel =
                new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
        List<VideoOptionModel> list = new ArrayList<>();
        list.add(videoOptionModel);
        VideoOptionModel videoOptionModel2 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_timeout", -1);
        VideoOptionModel videoOptionModel3 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");
        VideoOptionModel videoOptionModel4 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_flags", "prefer_tcp");
        VideoOptionModel videoOptionMode04 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);//是否开启缓冲
        VideoOptionModel videoOptionMode14 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 1);//是否限制输入缓存数
        VideoOptionModel videoOptionMode15 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "nobuffer");
        VideoOptionModel videoOptionMode17 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzedmaxduration", 100);//分析码流时长:默认1024*1000
        list.add(videoOptionModel2);
        list.add(videoOptionModel3);
        list.add(videoOptionModel4);
        list.add(videoOptionMode04);
        list.add(videoOptionMode14);
        list.add(videoOptionMode15);
        list.add(videoOptionMode17);
        GSYVideoManager.instance().setOptionModelList(list);*/



        ///  ijkplayer subtitle
//        VideoOptionModel videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "subtitle", 1);
//        List<VideoOptionModel> list = new ArrayList<>();
//        list.add(videoOptionModel);
//        GSYVideoManager.instance().setOptionModelList(list);
//        GSYVideoManager.instance().setPlayerInitSuccessListener(new IPlayerInitSuccessListener() {
//            @Override
//            public void onPlayerInitSuccess(IMediaPlayer player, GSYModel model) {
//                if (player instanceof IjkMediaPlayer) {
//                    IjkMediaPlayer ijkMediaPlayer = ((IjkMediaPlayer)player);
//                    ijkMediaPlayer.setOnTimedTextListener(new IMediaPlayer.OnTimedTextListener() {
//                        @Override
//                        public void onTimedText(IMediaPlayer mp, IjkTimedText text) {
//                            if (text != null) {
//                                ///  render subtitle text
//                                subtitleTextView.setText(text.getText());
//                            }
//                        }
//                    });
//                }
//            }
//        });


        /***************rtsp 配置****************/


        VideoOptionModel videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
        List<VideoOptionModel> list = new ArrayList<>();
        list.add(videoOptionModel);
        GSYVideoManager.instance().setOptionModelList(list);

        /// ijk rtmp
       /*VideoOptionModel videoOptionModel =
                new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "crypto,file,http,https,tcp,tls,udp,rtmp,rtsp");
        List<VideoOptionModel> list = new ArrayList<>();
        list.add(videoOptionModel);
        GSYVideoManager.instance().setOptionModelList(list);*/

        //增加封面
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.xxx1);

        //binding.detailPlayer.setThumbImageView(imageView);

        resolveNormalVideoUI();

        //外部辅助的旋转，帮助全屏
        orientationUtils = new OrientationUtils(this, binding.detailPlayer);
        //初始化不打开外部的旋转
        orientationUtils.setEnable(false);

        /**仅仅横屏旋转，不变直*/
        //orientationUtils.setOnlyRotateLand(true);

        //ProxyCacheManager.DEFAULT_MAX_SIZE = 1024 * 1024 * 1024 * 1024;
        //ProxyCacheManager.DEFAULT_MAX_COUNT = 8;

        Map<String, String> header = new HashMap<>();
        header.put("ee", "33");
        header.put("allowCrossProtocolRedirects", "true");
        header.put("User-Agent", "GSY");
        GSYVideoOptionBuilder gsyVideoOption = new GSYVideoOptionBuilder();
        gsyVideoOption.setThumbImageView(imageView).setIsTouchWiget(true).setRotateViewAuto(false)
            //仅仅横屏旋转，不变直
            //.setOnlyRotateLand(true)
            .setRotateWithSystem(false)
            .setLockLand(true)
            .setAutoFullWithSize(true)
            .setShowFullAnimation(false)
            .setNeedLockFull(true)
            //.setSeekOnStart(3000)
            .setUrl(url)
            .setMapHeadData(header)
            .setCacheWithPlay(false)
            .setSurfaceErrorPlay(false)
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
                    if (binding.detailPlayer.getGSYVideoManager().getPlayer() instanceof Exo2PlayerManager) {
                        ((Exo2PlayerManager) binding.detailPlayer.getGSYVideoManager().getPlayer()).setSeekParameter(SeekParameters.NEXT_SYNC);
                        Debuger.printfError("***** setSeekParameter **** ");
                    }


                    if (binding.detailPlayer.getGSYVideoManager().getPlayer() instanceof Exo2PlayerManager) {
                        IjkExo2MediaPlayer player = ((IjkExo2MediaPlayer) binding.detailPlayer.getGSYVideoManager().getPlayer().getMediaPlayer());
                        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = player.getTrackSelector().getCurrentMappedTrackInfo();
                        boolean hadVideo = false;
                        if (mappedTrackInfo != null) {
                            for (int i = 0; i < mappedTrackInfo.getRendererCount(); i++) {
                                TrackGroupArray rendererTrackGroups = mappedTrackInfo.getTrackGroups(i);
                                if (C.TRACK_TYPE_AUDIO == mappedTrackInfo.getRendererType(i)) { //判断是否是音轨
                                    for (int j = 0; j < rendererTrackGroups.length; j++) {
                                        TrackGroup trackGroup = rendererTrackGroups.get(j);
                                        for (int k = 0; k < trackGroup.length; k++) {
                                            Debuger.printfError("####### Audio " + trackGroup.getFormat(k).toString() + " #######");
                                        }
                                    }
                                } else if (C.TRACK_TYPE_VIDEO == mappedTrackInfo.getRendererType(i)) {
                                    for (int j = 0; j < rendererTrackGroups.length; j++) {
                                        TrackGroup trackGroup = rendererTrackGroups.get(j);
                                        for (int k = 0; k < trackGroup.length; k++) {
                                            Debuger.printfError("####### Video " + trackGroup.getFormat(k).toString() + " #######");
                                        }
                                    }
                                    hadVideo = true;
                                } else {
                                    for (int j = 0; j < rendererTrackGroups.length; j++) {
                                        TrackGroup trackGroup = rendererTrackGroups.get(j);
                                        Debuger.printfError("####### Other " + trackGroup.getFormat(0).toString() + " #######");
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
//                    IPlayerManager playerManager = binding.detailPlayer.getGSYVideoManager().getPlayer();
//                    if (playerManager instanceof SystemPlayerManager) {
//                        playerManager.release();
//                    }
                }

                @Override
                public void onComplete(String url, Object... objects) {
                    super.onComplete(url, objects);
//                    IPlayerManager playerManager = binding.detailPlayer.getGSYVideoManager().getPlayer();
//                    if (playerManager instanceof SystemPlayerManager) {
//                        playerManager.release();
//                    }
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

                @Override
                public void onPlayError(String url, Object... objects) {
                    super.onPlayError(url, objects);
                    if (objects[2] != null && binding.detailPlayer.getGSYVideoManager().getPlayer() instanceof Exo2PlayerManager) {
                        Debuger.printfError("#######################");
                        int code = ((int) objects[2]);
                        String errorStatus = "****";
                        switch (code) {
                            case ERROR_CODE_UNSPECIFIED:
                                errorStatus = "ERROR_CODE_UNSPECIFIED";
                                break;
                            case ERROR_CODE_REMOTE_ERROR:
                                errorStatus = "ERROR_CODE_REMOTE_ERROR";
                                break;
                            case ERROR_CODE_BEHIND_LIVE_WINDOW:
                                errorStatus = "ERROR_CODE_BEHIND_LIVE_WINDOW";
                                break;
                            case ERROR_CODE_TIMEOUT:
                                errorStatus = "ERROR_CODE_TIMEOUT";
                                break;
                            case ERROR_CODE_FAILED_RUNTIME_CHECK:
                                errorStatus = "ERROR_CODE_FAILED_RUNTIME_CHECK";
                                break;
                            case ERROR_CODE_IO_UNSPECIFIED:
                                errorStatus = "ERROR_CODE_IO_UNSPECIFIED";
                                break;
                            case ERROR_CODE_IO_NETWORK_CONNECTION_FAILED:
                                errorStatus = "ERROR_CODE_IO_NETWORK_CONNECTION_FAILED";
                                break;
                            case ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT:
                                errorStatus = "ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT";
                                break;
                            case ERROR_CODE_IO_INVALID_HTTP_CONTENT_TYPE:
                                errorStatus = "ERROR_CODE_IO_INVALID_HTTP_CONTENT_TYPE";
                                break;
                            case ERROR_CODE_IO_BAD_HTTP_STATUS:
                                errorStatus = "ERROR_CODE_IO_BAD_HTTP_STATUS";
                                break;
                            case ERROR_CODE_IO_FILE_NOT_FOUND:
                                errorStatus = "ERROR_CODE_IO_FILE_NOT_FOUND";
                                break;
                            case ERROR_CODE_IO_NO_PERMISSION:
                                errorStatus = "ERROR_CODE_IO_NO_PERMISSION";
                                break;
                            case ERROR_CODE_IO_CLEARTEXT_NOT_PERMITTED:
                                errorStatus = "ERROR_CODE_IO_CLEARTEXT_NOT_PERMITTED";
                                break;
                            case ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE:
                                errorStatus = "ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE";
                                break;
                            case ERROR_CODE_PARSING_CONTAINER_MALFORMED:
                                errorStatus = "ERROR_CODE_PARSING_CONTAINER_MALFORMED";
                                break;
                            case ERROR_CODE_PARSING_MANIFEST_MALFORMED:
                                errorStatus = "ERROR_CODE_PARSING_MANIFEST_MALFORMED";
                                break;
                            case ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED:
                                errorStatus = "ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED";
                                break;
                            case ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED:
                                errorStatus = "ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED";
                                break;
                            case ERROR_CODE_DECODER_INIT_FAILED:
                                errorStatus = "ERROR_CODE_DECODER_INIT_FAILED";
                                break;
                            case ERROR_CODE_DECODER_QUERY_FAILED:
                                errorStatus = "ERROR_CODE_DECODER_QUERY_FAILED";
                                break;
                            case ERROR_CODE_DECODING_FAILED:
                                errorStatus = "ERROR_CODE_DECODING_FAILED";
                                break;
                            case ERROR_CODE_DECODING_FORMAT_EXCEEDS_CAPABILITIES:
                                errorStatus = "ERROR_CODE_DECODING_FORMAT_EXCEEDS_CAPABILITIES";
                                break;
                            case ERROR_CODE_DECODING_FORMAT_UNSUPPORTED:
                                errorStatus = "ERROR_CODE_DECODING_FORMAT_UNSUPPORTED";
                                break;
                            case ERROR_CODE_AUDIO_TRACK_INIT_FAILED:
                                errorStatus = "ERROR_CODE_AUDIO_TRACK_INIT_FAILED";
                                break;
                            case ERROR_CODE_AUDIO_TRACK_WRITE_FAILED:
                                errorStatus = "ERROR_CODE_AUDIO_TRACK_WRITE_FAILED";
                                break;
                            case ERROR_CODE_DRM_UNSPECIFIED:
                                errorStatus = "ERROR_CODE_DRM_UNSPECIFIED";
                                break;
                            case ERROR_CODE_DRM_SCHEME_UNSUPPORTED:
                                errorStatus = "ERROR_CODE_DRM_SCHEME_UNSUPPORTED";
                                break;
                            case ERROR_CODE_DRM_PROVISIONING_FAILED:
                                errorStatus = "ERROR_CODE_DRM_PROVISIONING_FAILED";
                                break;
                            case ERROR_CODE_DRM_CONTENT_ERROR:
                                errorStatus = "ERROR_CODE_DRM_CONTENT_ERROR";
                                break;
                            case ERROR_CODE_DRM_LICENSE_ACQUISITION_FAILED:
                                errorStatus = "ERROR_CODE_DRM_LICENSE_ACQUISITION_FAILED";
                                break;
                            case ERROR_CODE_DRM_DISALLOWED_OPERATION:
                                errorStatus = "ERROR_CODE_DRM_DISALLOWED_OPERATION";
                                break;
                            case ERROR_CODE_DRM_SYSTEM_ERROR:
                                errorStatus = "ERROR_CODE_DRM_SYSTEM_ERROR";
                                break;
                            case ERROR_CODE_DRM_DEVICE_REVOKED:
                                errorStatus = "ERROR_CODE_DRM_DEVICE_REVOKED";
                                break;
                            case ERROR_CODE_DRM_LICENSE_EXPIRED:
                                errorStatus = "ERROR_CODE_DRM_LICENSE_EXPIRED";
                                break;
                            case CUSTOM_ERROR_CODE_BASE:
                                errorStatus = "CUSTOM_ERROR_CODE_BASE";
                                break;
                        }
                        Debuger.printfError(errorStatus);
                        Debuger.printfError("#######################");
                    }
                }
            }).setLockClickListener(new LockClickListener() {
                @Override
                public void onClick(View view, boolean lock) {
                    if (orientationUtils != null) {
                        //配合下方的onConfigurationChanged
                        orientationUtils.setEnable(!lock);
                    }
                }
            }).setGSYVideoProgressListener(new GSYVideoProgressListener() {
                @Override
                public void onProgress(long progress, long secProgress, long currentPosition, long duration) {
                    Debuger.printfLog(" progress " + progress + " secProgress " + secProgress + " currentPosition " + currentPosition + " duration " + duration);
                }
            }).build(binding.detailPlayer);

        binding.detailPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //直接横屏
                // ------- ！！！如果不需要旋转屏幕，可以不调用！！！-------
                // 不需要屏幕旋转，还需要设置 setNeedOrientationUtils(false)
                orientationUtils.resolveByClick();

                //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
                binding.detailPlayer.startWindowFullscreen(DetailPlayer.this, true, true);
            }
        });


        binding.openBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                fileSearch();
            }
        });

        binding.pip.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    inPipMode = true;
                    DetailPlayer.this.enterPictureInPictureMode();
                }
            }
        });

        ///exo 切换音轨
        binding.change.setOnClickListener(new View.OnClickListener() {
            int index = 0;

            @Override
            public void onClick(View view) {
                if (binding.detailPlayer.getGSYVideoManager().getPlayer() instanceof Exo2PlayerManager) {
                    IjkExo2MediaPlayer player = ((IjkExo2MediaPlayer) binding.detailPlayer.getGSYVideoManager().getPlayer().getMediaPlayer());
                    TrackSelector trackSelector = player.getTrackSelector();
                    MappingTrackSelector.MappedTrackInfo mappedTrackInfo = player.getTrackSelector().getCurrentMappedTrackInfo();

                    if (mappedTrackInfo != null) {
                        for (int i = 0; i < mappedTrackInfo.getRendererCount(); i++) {
                            TrackGroupArray rendererTrackGroups = mappedTrackInfo.getTrackGroups(i);
                            if (C.TRACK_TYPE_AUDIO == mappedTrackInfo.getRendererType(i)) { //判断是否是音轨
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
                } else {
                    Toast.makeText(DetailPlayer.this, "当前不是 Exo 内核或者未播放", Toast.LENGTH_LONG).show();
                }
            }
        });
        binding.showTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.detailPlayer.getGSYVideoManager().getPlayer() instanceof Exo2PlayerManager) {
                    if (binding.detailPlayer.isInPlayingState()) {
                        IjkExo2MediaPlayer player = ((IjkExo2MediaPlayer) binding.detailPlayer.getGSYVideoManager().getPlayer().getMediaPlayer());
                        List<String> list = new ArrayList<>();
                        Tracks track = player.getCurrentTracks();
                        for (int i = 0; i < track.getGroups().size(); i++) {
                            Tracks.Group group = track.getGroups().get(i);
                            if (C.TRACK_TYPE_AUDIO == group.getType() || C.TRACK_TYPE_VIDEO == group.getType()) {
                                for (int j = 0; j < group.getMediaTrackGroup().length; j++) {
                                    list.add("- " + group.getMediaTrackGroup().getFormat(j) + "\n");
                                }
                            }
                            showOption(list.toArray(new String[0]));
                        }
                    }
                } else {
                    Toast.makeText(DetailPlayer.this, "当前不是 Exo 内核或者未播放", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        inPipMode = isInPictureInPictureMode;
    }

    @Override
    public void onBackPressed() {

        // ------- ！！！如果不需要旋转屏幕，可以不调用！！！-------
        // 不需要屏幕旋转，还需要设置 setNeedOrientationUtils(false)
        if (orientationUtils != null) {
            orientationUtils.backToProtVideo();
        }

        if (GSYVideoManager.backFromWindowFull(this)) {
            return;
        }
        super.onBackPressed();
    }


    @Override
    protected void onPause() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInPictureInPictureMode()) {
            // Continue playback
        } else {
            getCurPlay().onVideoPause();
            isPause = true;
        }
        super.onPause();
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
        if (orientationUtils != null) orientationUtils.releaseListener();
    }


    /**
     * orientationUtils 和  binding.detailPlayer.onConfigurationChanged 方法是用于触发屏幕旋转的
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //如果旋转了就全屏
        if (isPlay && !isPause && !inPipMode) {
            binding.detailPlayer.onConfigurationChanged(this, newConfig, orientationUtils, true, true);
        }
    }

    private void showOption(final String[] list) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("显示可切换轨道");
        builder.setItems(list, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
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

        //String url = "android.resource://" + getPackageName() + "/" + R.raw.test;
        //注意，用ijk模式播放raw视频，这个必须打开
        GSYVideoManager.instance().enableRawPlay(getApplicationContext());

        ///exo 播放 raw
        //String url = "rawresource://" + getPackageName() + "/" + R.raw.test;

        ///exo raw 支持
        ///String url =  "assets:///test1.mp4";


        //断网自动重新链接，url前接上ijkhttphook:
        //String url = "ijkhttphook:https://res.exexm.com/cw_145225549855002";

        // For curated demo URLs see com.example.gsyvideoplayer.utils.DemoVideoUrls
        String url = DemoVideoUrls.HLS_BIPBOP_GEAR3;
        return url;
    }

    private static final int READ_REQUEST_CODE = 42;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null || resultCode != Activity.RESULT_OK) return;
        if (requestCode == READ_REQUEST_CODE) {
            getPathForSearch(data.getData());
        }
    }


    private void getPathForSearch(Uri uri) {
        if (uri == null) {
            Toast.makeText(this, "未获取到视频地址", Toast.LENGTH_SHORT).show();
            return;
        }
        Debuger.printfLog("Open document uri: " + uri);
        binding.detailPlayer.setUp(uri.toString(), false, "File");
        binding.detailPlayer.startPlayLogic();
    }

    protected void fileSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }
}
