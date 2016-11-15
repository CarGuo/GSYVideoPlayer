package com.example.gsyvideoplayer;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.gsyvideoplayer.adapter.ListVideoAdapter;
import com.shuyu.gsyvideoplayer.GSYVideoPlayer;
import com.shuyu.gsyvideoplayer.utils.ListVideoUtil;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListVideo2Activity extends AppCompatActivity {

    @BindView(R.id.video_list)
    ListView videoList;
    @BindView(R.id.video_full_container)
    FrameLayout videoFullContainer;
    @BindView(R.id.activity_list_video)
    RelativeLayout activityListVideo;

    ListVideoUtil listVideoUtil;
    ListVideoAdapter listVideoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 设置一个exit transition
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setEnterTransition(new Explode());
            getWindow().setExitTransition(new Explode());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_video2);
        ButterKnife.bind(this);

        listVideoUtil = new ListVideoUtil(this);
        listVideoUtil.setFullViewContainer(videoFullContainer);
        listVideoUtil.setHideStatusBar(true);
        listVideoAdapter = new ListVideoAdapter(this, listVideoUtil);
        listVideoAdapter.setRootView(activityListVideo);
        videoList.setAdapter(listVideoAdapter);

    }


    @Override
    public void onBackPressed() {
        if (listVideoUtil.backFromFull()) {
            return;
        }
        super.onBackPressed();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        listVideoUtil.releaseVideoPlayer();
        GSYVideoPlayer.releaseAllVideos();
    }

}
