package com.example.gsyvideoplayer;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.gsyvideoplayer.adapter.ListNormalAdapter;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListVideoActivity extends AppCompatActivity {

    @BindView(R.id.video_list)
    ListView videoList;
    @BindView(R.id.activity_list_video)
    RelativeLayout activityListVideo;

    ListNormalAdapter listNormalAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 设置一个exit transition
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setEnterTransition(new Explode());
            getWindow().setExitTransition(new Explode());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_video);
        ButterKnife.bind(this);

        listNormalAdapter = new ListNormalAdapter(this);
        videoList.setAdapter(listNormalAdapter);

        videoList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastVisibleItem = firstVisibleItem + visibleItemCount;
                //大于0说明有播放
                if (GSYVideoManager.instance().getPlayPosition() >= 0) {
                    //当前播放的位置
                    int position = GSYVideoManager.instance().getPlayPosition();
                    //对应的播放列表TAG
                    if (GSYVideoManager.instance().getPlayTag().equals(ListNormalAdapter.TAG)
                            && (position < firstVisibleItem || position > lastVisibleItem)) {
                        //如果滑出去了上面和下面就是否，和今日头条一样
                        GSYVideoPlayer.releaseAllVideos();
                        listNormalAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (StandardGSYVideoPlayer.backFromWindowFull(this)) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        GSYVideoManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        GSYVideoManager.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GSYVideoPlayer.releaseAllVideos();
    }
}
