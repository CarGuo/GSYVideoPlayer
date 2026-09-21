package com.example.gsyvideoplayer;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Consumer;

import com.example.gsyvideoplayer.databinding.ActivityFoldDetailBinding;
import com.example.gsyvideoplayer.utils.DemoVideoUrls;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import androidx.window.layout.WindowLayoutInfo;
import androidx.window.java.layout.WindowInfoTrackerCallbackAdapter;
import androidx.window.layout.FoldingFeature;
import androidx.window.layout.WindowInfoTracker;

public class FoldDetailActivity extends AppCompatActivity {

    private ActivityFoldDetailBinding binding;

    private WindowInfoTrackerCallbackAdapter windowInfoTracker;
    private final Consumer<WindowLayoutInfo> layoutStateChangeCallback = this::onWindowLayoutInfoChange;

    private boolean isPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFoldDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        windowInfoTracker =
            new WindowInfoTrackerCallbackAdapter(WindowInfoTracker.getOrCreate(this));

        resolvePlayerUI();
        buildPlayer();
        applyFoldState(null);
    }

    private void resolvePlayerUI() {
        binding.foldPlayer.getTitleTextView().setVisibility(View.GONE);
        binding.foldPlayer.getBackButton().setVisibility(View.VISIBLE);
        binding.foldPlayer.getBackButton().setOnClickListener(v -> onBackPressed());
    }

    private void buildPlayer() {
        String url = DemoVideoUrls.SAMPLE_GSY;

        ImageView cover = new ImageView(this);
        cover.setScaleType(ImageView.ScaleType.CENTER_CROP);
        cover.setImageResource(R.mipmap.xxx1);

        GSYVideoOptionBuilder builder = new GSYVideoOptionBuilder();
        builder.setThumbImageView(cover)
            .setIsTouchWiget(true)
            .setRotateViewAuto(false)
            .setRotateWithSystem(false)
            .setLockLand(false)
            .setShowFullAnimation(false)
            .setNeedLockFull(false)
            .setUrl(url)
            .setCacheWithPlay(false)
            .setSurfaceErrorPlay(false)
            .setVideoTitle("折叠屏 XML Demo")
            .setVideoAllCallBack(new GSYSampleCallBack() {
                @Override
                public void onPrepared(String url, Object... objects) {
                    super.onPrepared(url, objects);
                    isPlay = true;
                }
            })
            .build(binding.foldPlayer);

        binding.foldPlayer.startPlayLogic();
    }

    private void onWindowLayoutInfoChange(@NonNull WindowLayoutInfo layoutInfo) {
        FoldingFeature feature = null;
        for (androidx.window.layout.DisplayFeature df : layoutInfo.getDisplayFeatures()) {
            if (df instanceof FoldingFeature) {
                feature = (FoldingFeature) df;
                break;
            }
        }
        applyFoldState(feature);
    }

    private void applyFoldState(FoldingFeature feature) {
        boolean isBook = feature != null
            && feature.getState() == FoldingFeature.State.HALF_OPENED
            && feature.getOrientation() == FoldingFeature.Orientation.VERTICAL;

        boolean isTabletop = feature != null
            && feature.getState() == FoldingFeature.State.HALF_OPENED
            && feature.getOrientation() == FoldingFeature.Orientation.HORIZONTAL;

        boolean split = isBook || isTabletop;
        LinearLayout root = binding.foldRoot;

        root.setOrientation(isBook ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);

        if (isBook) {
            binding.foldPosture.setText("左右折叠 · BOOK 竖向折痕");
        } else if (isTabletop) {
            binding.foldPosture.setText("上下折叠 · TABLETOP 横向折痕");
        } else {
            binding.foldPosture.setText("展开态");
        }

        binding.foldDivider.setVisibility(split ? View.VISIBLE : View.GONE);

        ViewGroup.LayoutParams playerParams = binding.foldPlayer.getLayoutParams();
        ViewGroup.LayoutParams dividerParams = binding.foldDivider.getLayoutParams();

        if (isBook) {
            playerParams.width = 0;
            playerParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            if (playerParams instanceof LinearLayout.LayoutParams) {
                ((LinearLayout.LayoutParams) playerParams).weight = 1f;
                ((LinearLayout.LayoutParams) playerParams).gravity = Gravity.CENTER_VERTICAL;
            }
            dividerParams.width = getHingeWidth(feature);
            dividerParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            binding.foldInfoScroll.getLayoutParams().width = 0;
            binding.foldInfoScroll.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            setScrollWeight(1f);
        } else {
            playerParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            if (isTabletop) {
                playerParams.height = 0;
                if (playerParams instanceof LinearLayout.LayoutParams) {
                    ((LinearLayout.LayoutParams) playerParams).weight = 1f;
                    ((LinearLayout.LayoutParams) playerParams).gravity = Gravity.CENTER_HORIZONTAL;
                }
                dividerParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                dividerParams.height = getHingeWidth(feature);
                binding.foldInfoScroll.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                binding.foldInfoScroll.getLayoutParams().height = 0;
                setScrollWeight(1f);
            } else {
                playerParams.height = (int) getResources().getDimension(R.dimen.post_media_height);
                if (playerParams instanceof LinearLayout.LayoutParams) {
                    ((LinearLayout.LayoutParams) playerParams).weight = 0f;
                    ((LinearLayout.LayoutParams) playerParams).gravity = Gravity.NO_GRAVITY;
                }
                binding.foldInfoScroll.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                binding.foldInfoScroll.getLayoutParams().height = 0;
                setScrollWeight(1f);
            }
        }

        binding.foldPlayer.setLayoutParams(playerParams);
        binding.foldDivider.setLayoutParams(dividerParams);
    }

    private void setScrollWeight(float weight) {
        ViewGroup.LayoutParams p = binding.foldInfoScroll.getLayoutParams();
        if (p instanceof LinearLayout.LayoutParams) {
            ((LinearLayout.LayoutParams) p).weight = weight;
        }
        binding.foldInfoScroll.setLayoutParams(p);
    }

    private int getHingeWidth(FoldingFeature feature) {
        if (feature == null) {
            return 0;
        }
        int width = feature.getBounds().width();
        int height = feature.getBounds().height();
        int thickness = Math.max(width, height) > 0 ? Math.min(width, height) : 0;
        if (thickness <= 0) {
            thickness = (int) (24 * getResources().getDisplayMetrics().density);
        }
        return thickness;
    }

    @Override
    protected void onStart() {
        super.onStart();
        windowInfoTracker.addWindowLayoutInfoListener(this, Runnable::run, layoutStateChangeCallback);
    }

    @Override
    protected void onStop() {
        super.onStop();
        windowInfoTracker.removeWindowLayoutInfoListener(layoutStateChangeCallback);
    }

    @Override
    public void onBackPressed() {
        if (GSYVideoManager.backFromWindowFull(this)) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getCurPlay().onVideoPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getCurPlay().onVideoResume(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isPlay) {
            getCurPlay().release();
        }
    }

    private GSYVideoPlayer getCurPlay() {
        if (binding.foldPlayer.getFullWindowPlayer() != null) {
            return binding.foldPlayer.getFullWindowPlayer();
        }
        return binding.foldPlayer;
    }
}
