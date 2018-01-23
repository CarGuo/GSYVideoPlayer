package com.example.gsyvideoplayer;

import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.gsyvideoplayer.adapter.ListVideoAdapter;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.utils.GSYVideoHelper;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.shuyu.gsyvideoplayer.utils.Debuger;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 列表小窗口
 */
public class ListVideo2Activity extends AppCompatActivity {

    @BindView(R.id.video_list)
    ListView videoList;

    @BindView(R.id.video_full_container)
    FrameLayout videoFullContainer;

    @BindView(R.id.activity_list_video)
    RelativeLayout activityListVideo;

    GSYVideoHelper smallVideoHelper;

    ListVideoAdapter listVideoAdapter;

    GSYVideoHelper.GSYVideoHelperBuilder gsySmallVideoHelperBuilder;

    int lastVisibleItem;

    int firstVisibleItem;

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

        //创建小窗口帮助类
        smallVideoHelper = new GSYVideoHelper(this);

        //如果不设置即使用默认的 windowViewContainer
        //smallVideoHelper.setFullViewContainer(videoFullContainer);

        //配置
        gsySmallVideoHelperBuilder = new GSYVideoHelper.GSYVideoHelperBuilder();
        gsySmallVideoHelperBuilder
                .setHideStatusBar(true)
                .setNeedLockFull(true)
                .setCacheWithPlay(true)
                .setShowFullAnimation(false)
                .setRotateViewAuto(false)
                .setLockLand(true)
                .setVideoAllCallBack(new GSYSampleCallBack(){
            @Override
            public void onPrepared(String url, Object... objects) {
                super.onPrepared(url, objects);
                Debuger.printfLog("Duration " + smallVideoHelper.getGsyVideoPlayer().getDuration() + " CurrentPosition " + smallVideoHelper.getGsyVideoPlayer().getCurrentPositionWhenPlaying());
            }

            @Override
            public void onQuitSmallWidget(String url, Object... objects) {
                super.onQuitSmallWidget(url, objects);
                //大于0说明有播放,//对应的播放列表TAG
                if (smallVideoHelper.getPlayPosition() >= 0 && smallVideoHelper.getPlayTAG().equals(ListVideoAdapter.TAG)) {
                    //当前播放的位置
                    int position = smallVideoHelper.getPlayPosition();
                    //不可视的是时候
                    if ((position < firstVisibleItem || position > lastVisibleItem)) {
                        //释放掉视频
                        smallVideoHelper.releaseVideoPlayer();
                        listVideoAdapter.notifyDataSetChanged();
                    }
                }

            }
        });

        smallVideoHelper.setGsyVideoOptionBuilder(gsySmallVideoHelperBuilder);

        listVideoAdapter = new ListVideoAdapter(this, smallVideoHelper, gsySmallVideoHelperBuilder);
        listVideoAdapter.setRootView(activityListVideo);
        videoList.setAdapter(listVideoAdapter);

        videoList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                ListVideo2Activity.this.firstVisibleItem = firstVisibleItem;
                lastVisibleItem = firstVisibleItem + visibleItemCount;
                //大于0说明有播放,//对应的播放列表TAG
                if (smallVideoHelper.getPlayPosition() >= 0 && smallVideoHelper.getPlayTAG().equals(ListVideoAdapter.TAG)) {
                    //当前播放的位置
                    int position = smallVideoHelper.getPlayPosition();
                    //不可视的是时候
                    if ((position < firstVisibleItem || position > lastVisibleItem)) {
                        //如果是小窗口就不需要处理
                        if (!smallVideoHelper.isSmall()) {
                            //小窗口
                            int size = CommonUtil.dip2px(ListVideo2Activity.this, 150);
                            smallVideoHelper.showSmallVideo(new Point(size, size), false, true);
                        }
                    } else {
                        if (smallVideoHelper.isSmall()) {
                            smallVideoHelper.smallVideoToNormal();
                        }
                    }
                }
            }

        });
    }


    @Override
    public void onBackPressed() {
        if (smallVideoHelper.backFromFull()) {
            return;
        }
        super.onBackPressed();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        smallVideoHelper.releaseVideoPlayer();
        GSYVideoPlayer.releaseAllVideos();
    }

}
