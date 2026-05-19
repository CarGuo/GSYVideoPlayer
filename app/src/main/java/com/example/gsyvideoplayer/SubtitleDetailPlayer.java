package com.example.gsyvideoplayer;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gsyvideoplayer.databinding.ActivityDetailSubtitlePlayerBinding;
import com.example.gsyvideoplayer.utils.DemoVideoUrls;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.subtitle.GSYSubtitleSource;
import com.shuyu.gsyvideoplayer.subtitle.GSYSubtitleStyle;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubtitleDetailPlayer extends AppCompatActivity {

    private boolean isPlay;
    private boolean isPause;
    private boolean subtitleEnabled = true;
    private boolean largeSubtitle;
    private OrientationUtils orientationUtils;
    private ActivityDetailSubtitlePlayerBinding binding;
    private List<GSYSubtitleSource> subtitleSources;
    private int subtitleSourceIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PlayerFactory.setPlayManager(IjkPlayerManager.class);

        binding = ActivityDetailSubtitlePlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.xxx1);

        binding.detailPlayer.getTitleTextView().setVisibility(View.GONE);
        binding.detailPlayer.getBackButton().setVisibility(View.GONE);

        orientationUtils = new OrientationUtils(this, binding.detailPlayer);
        orientationUtils.setEnable(false);

        Map<String, String> header = new HashMap<>();
        header.put("allowCrossProtocolRedirects", "true");

        new GSYVideoOptionBuilder()
            .setThumbImageView(imageView)
            .setIsTouchWiget(true)
            .setRotateViewAuto(false)
            .setLockLand(false)
            .setShowFullAnimation(false)
            .setNeedLockFull(true)
            .setUrl(getUrl())
            .setMapHeadData(header)
            .setCacheWithPlay(false)
            .setVideoTitle("通用字幕 IJK Demo")
            .setVideoAllCallBack(new GSYSampleCallBack() {
                @Override
                public void onPrepared(String url, Object... objects) {
                    super.onPrepared(url, objects);
                    Debuger.printfError("***** subtitle ijk onPrepared **** " + objects[0]);
                    orientationUtils.setEnable(binding.detailPlayer.isRotateWithSystem());
                    isPlay = true;
                }

                @Override
                public void onEnterFullscreen(String url, Object... objects) {
                    super.onEnterFullscreen(url, objects);
                    Debuger.printfError("***** subtitle ijk onEnterFullscreen **** " + objects[0]);
                }

                @Override
                public void onQuitFullscreen(String url, Object... objects) {
                    super.onQuitFullscreen(url, objects);
                    Debuger.printfError("***** subtitle ijk onQuitFullscreen **** " + objects[0]);
                    if (orientationUtils != null) {
                        orientationUtils.backToProtVideo();
                    }
                }
            })
            .setLockClickListener(new LockClickListener() {
                @Override
                public void onClick(View view, boolean lock) {
                    if (orientationUtils != null) {
                        orientationUtils.setEnable(!lock);
                    }
                }
            })
            .build(binding.detailPlayer);

        subtitleSources = createSubtitleSources();
        binding.detailPlayer.setSubtitleSources(subtitleSources);
        applySubtitleStyle(16);

        binding.detailPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orientationUtils.resolveByClick();
                binding.detailPlayer.startWindowFullscreen(SubtitleDetailPlayer.this, true, true);
            }
        });

        binding.subtitleToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subtitleEnabled = !subtitleEnabled;
                getCurPlay().setSubtitleEnabled(subtitleEnabled);
                binding.subtitleToggle.setText(subtitleEnabled ? "GSY SUBTITLE OFF" : "GSY SUBTITLE ON");
            }
        });

        binding.subtitleSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                largeSubtitle = !largeSubtitle;
                int size = largeSubtitle ? 22 : 16;
                getCurPlay().setSubtitleStyle(createSubtitleStyle(size));
                binding.subtitleSize.setText(largeSubtitle ? "SUBTITLE SIZE 16" : "SUBTITLE SIZE 22");
            }
        });

        binding.subtitleSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (subtitleSources == null || subtitleSources.isEmpty()) {
                    return;
                }
                subtitleSourceIndex = (subtitleSourceIndex + 1) % subtitleSources.size();
                GSYSubtitleSource source = subtitleSources.get(subtitleSourceIndex);
                if (getCurPlay().selectSubtitle(source.getId())) {
                    binding.subtitleSource.setText("SUBTITLE " + source.getLabel());
                }
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
        if (orientationUtils != null) {
            orientationUtils.releaseListener();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (isPlay && !isPause) {
            binding.detailPlayer.onConfigurationChanged(this, newConfig, orientationUtils, true, true);
        }
    }

    private void applySubtitleStyle(int sizeSp) {
        binding.detailPlayer.setSubtitleStyle(createSubtitleStyle(sizeSp));
    }

    private GSYSubtitleStyle createSubtitleStyle(int sizeSp) {
        return new GSYSubtitleStyle.Builder()
            .setTextColor(Color.WHITE)
            .setTextSizeSp(sizeSp)
            .setShadow(Color.BLACK, 3, 1, 1)
            .setBottomMarginDp(56)
            .build();
    }

    private GSYVideoPlayer getCurPlay() {
        if (binding.detailPlayer.getFullWindowPlayer() != null) {
            return binding.detailPlayer.getFullWindowPlayer();
        }
        return binding.detailPlayer;
    }

    private String getUrl() {
        return DemoVideoUrls.MP4_BBB;
    }

    private List<GSYSubtitleSource> createSubtitleSources() {
        List<GSYSubtitleSource> sources = new ArrayList<>();
        sources.add(new GSYSubtitleSource.Builder(getLocalSrtSubtitleUri())
            .setId("local-srt")
            .setLabel("SRT LOCAL")
            .setLanguage("zh")
            .setDefault(true)
            .build());
        sources.add(new GSYSubtitleSource.Builder(getLocalVttSubtitleUri())
            .setId("local-vtt")
            .setLabel("VTT LOCAL")
            .setLanguage("en")
            .build());
        sources.add(new GSYSubtitleSource.Builder(getNetworkSrtSubtitleUrl())
            .setId("network-srt")
            .setLabel("SRT NETWORK")
            .setLanguage("zh")
            .build());
        return sources;
    }

    private String getLocalSrtSubtitleUri() {
        return "android.resource://" + getPackageName() + "/" + R.raw.demo_subtitle;
    }

    private String getLocalVttSubtitleUri() {
        return "android.resource://" + getPackageName() + "/" + R.raw.demo_subtitle_vtt;
    }

    private String getNetworkSrtSubtitleUrl() {
        return DemoVideoUrls.SUBTITLE_SRT;
    }
}
