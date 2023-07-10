package com.example.gsyvideoplayer;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.exoplayer.SeekParameters;
import androidx.media3.exoplayer.offline.Download;
import androidx.media3.exoplayer.offline.DownloadManager;

import com.example.gsyvideoplayer.databinding.ActivityDetailDownloadPlayerBinding;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.cache.CacheFactory;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.listener.GSYVideoProgressListener;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.utils.FileUtils;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import tv.danmaku.ijk.media.exo2.CacheHelper;
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager;
import tv.danmaku.ijk.media.exo2.ExoPlayerCacheManager;


public class DetailDownloadExoPlayer extends AppCompatActivity {

    private boolean isPlay;
    private boolean isPause;

    //private CacheHelper cacheHelper = new CacheHelper();
    private OrientationUtils orientationUtils;

    private ActivityDetailDownloadPlayerBinding binding;

    private File cachePath = new File(FileUtils.getTestPath());

    private Map<String, String> header = new HashMap<>();


    private String url = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDetailDownloadPlayerBinding.inflate(getLayoutInflater());

        if (!(CacheFactory.getCacheManager() instanceof ExoPlayerCacheManager)) {
            Toast.makeText(this, "只支持 Exo2PlayerManager 和 ExoPlayerCacheManager 模式", Toast.LENGTH_SHORT).show();

        }
        if (!(PlayerFactory.getPlayManager() instanceof Exo2PlayerManager)) {
            Toast.makeText(this, "只支持 Exo2PlayerManager 和 ExoPlayerCacheManager 模式", Toast.LENGTH_SHORT).show();

        }


        View rootView = binding.getRoot();
        setContentView(rootView);

        //增加封面
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.xxx1);

        resolveNormalVideoUI();

        //外部辅助的旋转，帮助全屏
        orientationUtils = new OrientationUtils(this, binding.detailPlayer);
        //初始化不打开外部的旋转
        orientationUtils.setEnable(false);

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
            .setCacheWithPlay(true)
            .setVideoTitle("测试视频")
            .setCachePath(cachePath)
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
                binding.detailPlayer.startWindowFullscreen(DetailDownloadExoPlayer.this, true, true);
            }
        });


        binding.startDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownload();
            }
        });

        binding.stopDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopDownload();
            }
        });


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
        stopDownload();
        CacheHelper.release();
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


    private void startDownload() {
        if (url == null || !url.startsWith("http")) {
            Toast.makeText(this, "URL 不是 Http 开头", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!(CacheFactory.getCacheManager() instanceof ExoPlayerCacheManager)) {
            Toast.makeText(this, "只支持 Exo2PlayerManager 和 ExoPlayerCacheManager 模式", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!(PlayerFactory.getPlayManager() instanceof Exo2PlayerManager)) {
            Toast.makeText(this, "只支持 Exo2PlayerManager 和 ExoPlayerCacheManager 模式", Toast.LENGTH_SHORT).show();
            return;
        }


        ////参考实现1
//        new Thread(
//            () -> {
//                try {
//                    cacheHelper.preCacheVideo(getApplicationContext(), Uri.parse(url), cachePath,
//                        false, null, header, C.LENGTH_UNSET, new CacheWriter.ProgressListener() {
//                            @Override
//                            public void onProgress(long requestLength, long bytesCached, long newBytesCached) {
//                                Debuger.printfLog("#########", "requestLength " + requestLength + " bytesCached " + bytesCached + " newBytesCached  " + newBytesCached);
//                            }
//                        });
//                } catch (IOException e) {
//                   e.printStackTrace();
//                }
//            }
//        ).start();


        ////参考实现2
        CacheHelper.ensureDownloadManagerInitialized(getApplicationContext(), cachePath,
            false, null, header);
        try {
            CacheHelper.getDownloadManager().addListener(new DownloadManager.Listener() {
                @Override
                public void onDownloadChanged(DownloadManager downloadManager, Download download, @Nullable Exception finalException) {
                    Debuger.printfLog("#########", "download " + download.contentLength);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        CacheHelper.download(getMD5Str(url), Uri.parse(url));
    }

    private void stopDownload() {
        //cacheHelper.cancel();
        CacheHelper.pause();
    }

    public static String getMD5Str(String str) {
        byte[] digest = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            digest = md5.digest(str.getBytes("utf-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //16是表示转换为16进制数
        String md5Str = new BigInteger(1, digest).toString(16);
        return md5Str;
    }
}
