package com.example.gsyvideoplayer;


import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Explode;
import android.view.Window;

import com.example.gsyvideoplayer.adapter.RecyclerBaseAdapter;
import com.example.gsyvideoplayer.adapter.RecyclerNormalAdapter;

import com.example.gsyvideoplayer.holder.RecyclerItemNormalHolder;
import com.example.gsyvideoplayer.model.VideoModel;
import com.shuyu.gsyvideoplayer.GSYVideoManager;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class RecyclerViewActivity extends AppCompatActivity {


    @BindView(R.id.list_item_recycler)
    RecyclerView videoList;

    LinearLayoutManager linearLayoutManager;

    RecyclerBaseAdapter recyclerBaseAdapter;

    List<VideoModel> dataList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 设置一个exit transition
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setEnterTransition(new Explode());
            getWindow().setExitTransition(new Explode());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);
        ButterKnife.bind(this);

        resolveData();

        final RecyclerNormalAdapter recyclerNormalAdapter = new RecyclerNormalAdapter(this, dataList);
        linearLayoutManager = new LinearLayoutManager(this);
        videoList.setLayoutManager(linearLayoutManager);
        videoList.setAdapter(recyclerNormalAdapter);

        videoList.addOnScrollListener(new RecyclerView.OnScrollListener() {

            int firstVisibleItem, lastVisibleItem;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                firstVisibleItem   = linearLayoutManager.findFirstVisibleItemPosition();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                //大于0说明有播放
                if (GSYVideoManager.instance().getPlayPosition() >= 0) {
                    //当前播放的位置
                    int position = GSYVideoManager.instance().getPlayPosition();
                    //对应的播放列表TAG
                    if (GSYVideoManager.instance().getPlayTag().equals(RecyclerItemNormalHolder.TAG)
                            && (position < firstVisibleItem || position > lastVisibleItem)) {

                        //如果滑出去了上面和下面就是否，和今日头条一样
                        //是否全屏
                        if(!GSYVideoManager.isFullState(RecyclerViewActivity.this)) {
                            GSYVideoManager.releaseAllVideos();
                            recyclerNormalAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });

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
        GSYVideoManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        GSYVideoManager.onResume(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GSYVideoManager.releaseAllVideos();
    }


    private void resolveData() {
        for (int i = 0; i < 19; i++) {
            VideoModel videoModel = new VideoModel();
            dataList.add(videoModel);
        }
        if (recyclerBaseAdapter != null)
            recyclerBaseAdapter.notifyDataSetChanged();
    }

}
