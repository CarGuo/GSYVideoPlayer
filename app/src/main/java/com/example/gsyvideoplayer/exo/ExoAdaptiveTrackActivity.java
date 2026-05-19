package com.example.gsyvideoplayer.exo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;

import com.example.gsyvideoplayer.R;
import com.example.gsyvideoplayer.databinding.ActivityExoAdaptiveTrackBinding;
import com.example.gsyvideoplayer.utils.DemoVideoUrls;
import com.shuyu.gsyvideoplayer.GSYBaseActivityDetail;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.model.GSYVideoModel;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Exo adaptive track switching demo.
 * It uses one HLS master playlist or one DASH MPD instead of multiple standalone URLs.
 */
public class ExoAdaptiveTrackActivity extends GSYBaseActivityDetail<GSYExo2PlayerView> {

    private static final String HLS_MASTER_URL = DemoVideoUrls.HLS_MUX;
    private static final String DASH_MPD_URL = DemoVideoUrls.DASH_ENVIVIO;

    private ActivityExoAdaptiveTrackBinding binding;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private int renderType = GSYVideoType.getRenderType();
    private boolean useDashSource;
    private boolean isDestroyed;
    private boolean fixedTrackOverride;

    private final Runnable refreshTrackRunnable = new Runnable() {
        @Override
        public void run() {
            refreshTrackInfo();
            if (!isDestroyed) {
                mainHandler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExoAdaptiveTrackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        renderType = GSYVideoType.getRenderType();
        GSYVideoType.setRenderType(GSYVideoType.SURFACE);

        initVideo();
        setupPlayer(false, false);
        initControls();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (orientationUtils != null) {
                    orientationUtils.backToProtVideo();
                }
                if (GSYExoVideoManager.backFromWindowFull(ExoAdaptiveTrackActivity.this)) {
                    return;
                }
                finish();
            }
        });
    }

    private void initControls() {
        binding.hlsSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupPlayer(false, true);
            }
        });
        binding.dashSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupPlayer(true, true);
            }
        });
        binding.quality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQualityDialog();
            }
        });
        binding.detailPlayer.setLockClickListener(new LockClickListener() {
            @Override
            public void onClick(View view, boolean lock) {
                if (orientationUtils != null) {
                    orientationUtils.setEnable(!lock);
                }
            }
        });
    }

    private void setupPlayer(boolean dashSource, boolean autoStart) {
        useDashSource = dashSource;
        fixedTrackOverride = false;
        mainHandler.removeCallbacks(refreshTrackRunnable);
        GSYExoVideoManager.releaseAllVideos();
        isPlay = false;

        List<GSYVideoModel> urls = new ArrayList<>();
        urls.add(new GSYVideoModel(getCurrentSourceUrl(), getCurrentSourceTitle()));
        binding.detailPlayer.setUp(urls, 0);
        binding.detailPlayer.setOverrideExtension(useDashSource ? "mpd" : "m3u8");
        binding.detailPlayer.setExoCache(false);

        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.xxx1);
        binding.detailPlayer.setThumbImageView(imageView);
        binding.detailPlayer.getTitleTextView().setVisibility(View.VISIBLE);
        binding.detailPlayer.getBackButton().setVisibility(View.VISIBLE);
        binding.detailPlayer.setIsTouchWiget(true);
        binding.detailPlayer.setRotateViewAuto(false);
        binding.detailPlayer.setAutoFullWithSize(true);
        binding.detailPlayer.setLockLand(false);
        binding.detailPlayer.setShowFullAnimation(false);
        binding.detailPlayer.setNeedLockFull(true);
        binding.detailPlayer.setVideoAllCallBack(this);

        refreshSourceButtons();
        refreshTrackInfo();
        mainHandler.postDelayed(refreshTrackRunnable, 1000);
        if (autoStart) {
            binding.detailPlayer.startPlayLogic();
        }
    }

    private String getCurrentSourceUrl() {
        return useDashSource ? DASH_MPD_URL : HLS_MASTER_URL;
    }

    private String getCurrentSourceTitle() {
        return useDashSource ? "DASH MPD 自适应码率" : "HLS Master 自适应码率";
    }

    private void refreshSourceButtons() {
        binding.hlsSource.setEnabled(useDashSource);
        binding.dashSource.setEnabled(!useDashSource);
    }

    private void showQualityDialog() {
        final List<GSYExo2MediaPlayer.VideoTrackInfo> tracks = getSupportedVideoTracks();
        if (tracks.isEmpty()) {
            Toast.makeText(this, "视频轨道还没准备好", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> items = new ArrayList<>();
        items.add("自动（TrackSelector 自适应）");
        for (GSYExo2MediaPlayer.VideoTrackInfo track : tracks) {
            items.add(track.getLabel());
        }

        new AlertDialog.Builder(this)
            .setTitle("选择清晰度")
            .setItems(items.toArray(new String[0]), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    boolean success;
                    if (which == 0) {
                        success = GSYExoVideoManager.instance().clearVideoTrackOverride();
                        fixedTrackOverride = false;
                        binding.quality.setText("清晰度：自动");
                    } else {
                        GSYExo2MediaPlayer.VideoTrackInfo track = tracks.get(which - 1);
                        success = GSYExoVideoManager.instance()
                            .setVideoTrackOverride(track.groupIndex, track.trackIndex);
                        fixedTrackOverride = success;
                        binding.quality.setText("清晰度：" + track.getLabel().replace("  *", ""));
                    }
                    if (!success) {
                        Toast.makeText(ExoAdaptiveTrackActivity.this, "清晰度切换失败", Toast.LENGTH_SHORT).show();
                    }
                    mainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            refreshTrackInfo();
                        }
                    }, 500);
                }
            })
            .show();
    }

    private List<GSYExo2MediaPlayer.VideoTrackInfo> getSupportedVideoTracks() {
        List<GSYExo2MediaPlayer.VideoTrackInfo> tracks =
            GSYExoVideoManager.instance().getVideoTrackInfoList();
        List<GSYExo2MediaPlayer.VideoTrackInfo> supported = new ArrayList<>();
        for (GSYExo2MediaPlayer.VideoTrackInfo track : tracks) {
            if (track.supported) {
                supported.add(track);
            }
        }
        return supported;
    }

    private void refreshTrackInfo() {
        List<GSYExo2MediaPlayer.VideoTrackInfo> tracks =
            GSYExoVideoManager.instance().getVideoTrackInfoList();
        StringBuilder builder = new StringBuilder();
        builder.append(useDashSource ? "当前源：DASH MPD" : "当前源：HLS Master");
        builder.append('\n').append(getCurrentSourceUrl());
        if (tracks.isEmpty()) {
            builder.append("\n\n视频轨道：等待播放器解析中");
            binding.trackInfo.setText(builder.toString());
            return;
        }
        builder.append("\n\n视频轨道：");
        String selected = null;
        for (GSYExo2MediaPlayer.VideoTrackInfo track : tracks) {
            builder.append('\n')
                .append(track.supported ? "" : "[不支持] ")
                .append(track.getLabel());
            if (track.adaptiveSupported) {
                builder.append("  adaptive");
            }
            if (track.selected) {
                selected = track.getLabel().replace("  *", "");
            }
        }
        binding.trackInfo.setText(builder.toString());
        if (fixedTrackOverride && selected != null) {
            binding.quality.setText("清晰度：" + selected);
        } else if (!fixedTrackOverride) {
            binding.quality.setText("清晰度：自动");
        }
    }

    @Override
    public GSYExo2PlayerView getGSYVideoPlayer() {
        return binding.detailPlayer;
    }

    @Override
    public GSYVideoOptionBuilder getGSYVideoOptionBuilder() {
        return null;
    }

    @Override
    public void clickForFullScreen() {
    }

    @Override
    public boolean getDetailOrientationRotateAuto() {
        return true;
    }

    @Override
    public void onPrepared(String url, Object... objects) {
        super.onPrepared(url, objects);
        refreshTrackInfo();
    }

    @Override
    public void onPlayError(String url, Object... objects) {
        super.onPlayError(url, objects);
        binding.trackInfo.setText("播放失败：" + url);
    }

    @Override
    public void onEnterFullscreen(String url, Object... objects) {
        super.onEnterFullscreen(url, objects);
        GSYVideoPlayer gsyVideoPlayer = (GSYVideoPlayer) objects[1];
        gsyVideoPlayer.getBackButton().setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        isDestroyed = true;
        mainHandler.removeCallbacks(refreshTrackRunnable);
        super.onDestroy();
        GSYVideoType.setRenderType(renderType);
    }
}
