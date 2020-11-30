package com.example.gsyvideoplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.View;

import com.example.gsyvideoplayer.adapter.ViewPagerAdapter;
import com.example.gsyvideoplayer.holder.RecyclerItemNormalHolder;
import com.example.gsyvideoplayer.model.VideoModel;
import com.shuyu.gsyvideoplayer.GSYVideoManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ViewPager2Activity extends AppCompatActivity {

    @BindView(R.id.view_pager2)
    ViewPager2 viewPager2;

    List<VideoModel> dataList = new ArrayList<>();

    ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager2);
        ButterKnife.bind(this);
        resolveData();
        viewPagerAdapter = new ViewPagerAdapter(this, dataList);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        viewPager2.setAdapter(viewPagerAdapter);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                //大于0说明有播放
                int playPosition = GSYVideoManager.instance().getPlayPosition();
                if (playPosition >= 0) {
                    //对应的播放列表TAG
                    if (GSYVideoManager.instance().getPlayTag().equals(RecyclerItemNormalHolder.TAG)
                            && (position != playPosition)) {
                        playPosition(position);
                    }
                }
            }
        });
        viewPager2.post(new Runnable() {
            @Override
            public void run() {
                playPosition(0);
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
        if (viewPagerAdapter != null)
            viewPagerAdapter.notifyDataSetChanged();
    }

    private void playPosition(int position) {
        RecyclerView.ViewHolder viewHolder = ((RecyclerView) viewPager2.getChildAt(0)).findViewHolderForAdapterPosition(position);
        if (viewHolder != null) {
            RecyclerItemNormalHolder recyclerItemNormalHolder = (RecyclerItemNormalHolder) viewHolder;
            recyclerItemNormalHolder.getPlayer().startPlayLogic();
        }
    }
}



