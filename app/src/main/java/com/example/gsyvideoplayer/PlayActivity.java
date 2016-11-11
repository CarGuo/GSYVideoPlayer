package com.example.gsyvideoplayer;

import android.annotation.TargetApi;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.Transition;
import android.view.View;


import com.example.gsyvideoplayer.listener.OnTransitionListener;
import com.shuyu.gsyvideoplayer.GSYVideoPlayer;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlayActivity extends AppCompatActivity {

    public final static String IMG_TRANSITION = "IMG_TRANSITION";
    public final static String TRANSITION = "TRANSITION";

    @BindView(R.id.video_player)
    StandardGSYVideoPlayer videoPlayer;

    OrientationUtils orientationUtils;

    private boolean isTransition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        ButterKnife.bind(this);
        isTransition = getIntent().getBooleanExtra(TRANSITION, false);
        init();
    }

    private void init() {
        String url = "http://baobab.wdjcdn.com/14564977406580.mp4";
        videoPlayer.setUp(url, true, "");

        //Uri uri = Uri.parse(cover);
        //standardPlayer.thumbImageView.setImageURI(uri);
        //standardPlayer.titleTextView.setVisibility(View.GONE);

        videoPlayer.backButton.setVisibility(View.VISIBLE);
        videoPlayer.setIsTouchWiget(true);


        orientationUtils = new OrientationUtils(this, videoPlayer);

        videoPlayer.fullscreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orientationUtils.resolveByClick();
            }
        });

        videoPlayer.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        initTransition();
    }


    @Override
    protected void onPause() {
        super.onPause();
        videoPlayer.onVideoPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        if (orientationUtils.getScreenType() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            videoPlayer.fullscreenButton.performClick();
            return;
        }
        StandardGSYVideoPlayer.setJcBuriedPointStandard(null);
        GSYVideoPlayer.releaseAllVideos();
        if (isTransition && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.onBackPressed();
        } else {
            finish();
            overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
        }
    }


    private void initTransition() {
        if (isTransition && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
            ViewCompat.setTransitionName(videoPlayer, IMG_TRANSITION);
            addTransitionListener();
            startPostponedEnterTransition();
        } else {
            videoPlayer.startPlayLocic();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean addTransitionListener() {
        final Transition transition = getWindow().getSharedElementEnterTransition();
        if (transition != null) {
            transition.addListener(new OnTransitionListener() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    videoPlayer.startPlayLocic();
                    // Make sure we remove ourselves as a listener
                    transition.removeListener(this);
                }
            });
            return true;
        }
        return false;
    }

}
