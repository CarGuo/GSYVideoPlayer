package com.example.gsyvideoplayer;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.gsyvideoplayer.databinding.ActivityKeepLastFrameDemoBinding;
import com.example.gsyvideoplayer.utils.DemoVideoUrls;
import com.example.gsyvideoplayer.video.KeepLastFrameVideo;
import com.shuyu.gsyvideoplayer.GSYBaseActivityDetail;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;

public class KeepLastFrameDemoActivity extends GSYBaseActivityDetail<KeepLastFrameVideo> {

    private static final String DEMO_VIDEO_URL = DemoVideoUrls.MP4_BBB;

    private ActivityKeepLastFrameDemoBinding binding;

    private boolean keepLastFrameWhenComplete = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityKeepLastFrameDemoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.detailPlayer.setKeepLastFrameWhenComplete(keepLastFrameWhenComplete);
        resolveNormalVideoUI();
        initVideoBuilderMode();
        updateModeText();

        binding.keepToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keepLastFrameWhenComplete = !keepLastFrameWhenComplete;
                binding.detailPlayer.setKeepLastFrameWhenComplete(keepLastFrameWhenComplete);
                updateModeText();
            }
        });

        binding.replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.status.setText("重新开始播放");
                binding.detailPlayer.startPlayLogic();
            }
        });
    }

    @Override
    public KeepLastFrameVideo getGSYVideoPlayer() {
        return binding.detailPlayer;
    }

    @Override
    public GSYVideoOptionBuilder getGSYVideoOptionBuilder() {
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.xxx1);

        return new GSYVideoOptionBuilder()
            .setThumbImageView(imageView)
            .setUrl(DEMO_VIDEO_URL)
            .setCacheWithPlay(false)
            .setVideoTitle("最后一帧 Demo")
            .setIsTouchWiget(true)
            .setRotateViewAuto(false)
            .setLockLand(false)
            .setShowFullAnimation(false)
            .setNeedLockFull(true);
    }

    @Override
    public void clickForFullScreen() {

    }

    @Override
    public boolean getDetailOrientationRotateAuto() {
        return false;
    }

    @Override
    public void onPrepared(String url, Object... objects) {
        super.onPrepared(url, objects);
        binding.status.setText("播放中");
    }

    @Override
    public void onClickStartIcon(String url, Object... objects) {
        binding.status.setText("开始播放");
    }

    @Override
    public void onAutoComplete(String url, Object... objects) {
        boolean retained = isRetainedSurface(objects);
        if (keepLastFrameWhenComplete && retained) {
            binding.status.setText("播放完成：保留最后一帧");
        } else if (keepLastFrameWhenComplete) {
            binding.status.setText("播放完成：已开启保留，但当前渲染层没有可保留画面");
        } else {
            binding.status.setText("播放完成：默认封面态");
        }
    }

    @Override
    public void onPlayError(String url, Object... objects) {
        binding.status.setText("播放失败");
    }

    private void resolveNormalVideoUI() {
        binding.detailPlayer.getTitleTextView().setVisibility(View.GONE);
        binding.detailPlayer.getBackButton().setVisibility(View.GONE);
    }

    private void updateModeText() {
        binding.keepToggle.setText(keepLastFrameWhenComplete ? "保留最后一帧：开" : "保留最后一帧：关");
        binding.status.setText(keepLastFrameWhenComplete ? "当前模式：完成后保留最后一帧" : "当前模式：完成后显示封面");
    }

    private boolean isRetainedSurface(Object... objects) {
        if (objects != null) {
            for (Object object : objects) {
                if (object instanceof KeepLastFrameVideo) {
                    return ((KeepLastFrameVideo) object).isLastAutoCompleteRetainedSurface();
                }
            }
        }
        return binding.detailPlayer.isLastAutoCompleteRetainedSurface();
    }
}
