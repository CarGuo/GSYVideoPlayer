package com.example.gsyvideoplayer.simple;

import android.graphics.Point;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.gsyvideoplayer.R;
import com.example.gsyvideoplayer.simple.adapter.SimpleListVideoMode2Adapter;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.shuyu.gsyvideoplayer.utils.GSYVideoHelper;

/**
 * 简单列表实现模式2
 */
public class SimpleListVideoActivityMode2 extends AppCompatActivity {


    ListView videoList;

    RelativeLayout activityListVideo;

    GSYVideoHelper smallVideoHelper;

    SimpleListVideoMode2Adapter listVideoAdapter;

    GSYVideoHelper.GSYVideoHelperBuilder gsySmallVideoHelperBuilder;

    int lastVisibleItem;

    int firstVisibleItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_video2);

        videoList = (ListView)findViewById(R.id.video_list) ;
        activityListVideo = (RelativeLayout)findViewById(R.id.activity_list_video) ;

        //创建小窗口帮助类
        smallVideoHelper = new GSYVideoHelper(this);

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
                    public void onQuitSmallWidget(String url, Object... objects) {
                        super.onQuitSmallWidget(url, objects);
                        //大于0说明有播放,//对应的播放列表TAG
                        if (smallVideoHelper.getPlayPosition() >= 0 && smallVideoHelper.getPlayTAG().equals(SimpleListVideoMode2Adapter.TAG)) {
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

        listVideoAdapter = new SimpleListVideoMode2Adapter(this, smallVideoHelper, gsySmallVideoHelperBuilder);
        listVideoAdapter.setRootView(activityListVideo);
        videoList.setAdapter(listVideoAdapter);

        videoList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                SimpleListVideoActivityMode2.this.firstVisibleItem = firstVisibleItem;
                lastVisibleItem = firstVisibleItem + visibleItemCount;
                //大于0说明有播放,//对应的播放列表TAG
                if (smallVideoHelper.getPlayPosition() >= 0 && smallVideoHelper.getPlayTAG().equals(SimpleListVideoMode2Adapter.TAG)) {
                    //当前播放的位置
                    int position = smallVideoHelper.getPlayPosition();
                    //不可视的是时候
                    if ((position < firstVisibleItem || position > lastVisibleItem)) {
                        //如果是小窗口就不需要处理
                        if (!smallVideoHelper.isSmall()) {
                            //小窗口
                            int size = CommonUtil.dip2px(SimpleListVideoActivityMode2.this, 150);
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
        GSYVideoManager.releaseAllVideos();
    }

}
