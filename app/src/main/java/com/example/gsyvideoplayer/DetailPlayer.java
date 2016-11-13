package com.example.gsyvideoplayer;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.shuyu.gsyvideoplayer.GSYVideoPlayer;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.gsyvideoplayer.utils.CommonUtil.setViewHeight;

public class DetailPlayer extends AppCompatActivity {

    @BindView(R.id.post_detail_nested_scroll)
    NestedScrollView postDetailNestedScroll;
    @BindView(R.id.detail_player)
    StandardGSYVideoPlayer detailPlayer;
    @BindView(R.id.activity_detail_player)
    RelativeLayout activityDetailPlayer;

    private boolean isFull;

    private OrientationUtils orientationUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_player);
        ButterKnife.bind(this);

        String url = "http://baobab.wdjcdn.com/14564977406580.mp4";
        detailPlayer.setUp(url, true, "");

        //增加封面
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.xxx1);
        detailPlayer.setThumbImageView(imageView);

        resolveNormalVideoUI();

        orientationUtils = new OrientationUtils(this, detailPlayer);
        orientationUtils.setEnable(false);

        detailPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toDo();
            }
        });

        detailPlayer.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toNormal();
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (isFull) {
            detailPlayer.getBackButton().performClick();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GSYVideoPlayer.releaseAllVideos();
    }

    private void toFull() {
        isFull = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TransitionManager.beginDelayedTransition(activityDetailPlayer);
        }
        setViewHeight(detailPlayer, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        resolveFullVideoUI();
        orientationUtils.setEnable(true);
    }

    private void toNormal() {
        isFull = false;
        orientationUtils.setEnable(false);
        int delay = orientationUtils.backToProtVideo();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    TransitionManager.beginDelayedTransition(activityDetailPlayer);
                }
                setViewHeight(detailPlayer, ViewGroup.LayoutParams.MATCH_PARENT,
                        (int) getResources().getDimension(R.dimen.post_media_height));
            }
        }, delay);
    }

    private void toDo() {
        if (isFull) {
            toNormal();
        } else {
            toFull();
        }
    }

    private void resolveNormalVideoUI() {
        //增加title
        detailPlayer.getTitleTextView().setVisibility(View.GONE);
        detailPlayer.getTitleTextView().setText("测试视频");
        detailPlayer.getBackButton().setVisibility(View.GONE);
    }

    private void resolveFullVideoUI() {
        detailPlayer.getTitleTextView().setVisibility(View.VISIBLE);
        detailPlayer.getTitleTextView().setText("测试视频");
        detailPlayer.getBackButton().setVisibility(View.VISIBLE);
    }

}
