package com.example.gsyvideoplayer;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.View;

import com.example.gsyvideoplayer.adapter.ViewPagerAdapter;
import com.example.gsyvideoplayer.databinding.ActivityViewPager2Binding;
import com.example.gsyvideoplayer.holder.RecyclerItemNormalHolder;
import com.example.gsyvideoplayer.model.VideoModel;
import com.shuyu.gsyvideoplayer.GSYVideoManager;

import java.util.ArrayList;
import java.util.List;

public class ViewPager2Activity extends AppCompatActivity {
    ActivityViewPager2Binding binding;

    List<VideoModel> dataList = new ArrayList<>();

    ViewPagerAdapter viewPagerAdapter;

    private int mCurrentPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityViewPager2Binding.inflate(getLayoutInflater());

        View rootView = binding.getRoot();
        setContentView(rootView);


        resolveData();
        viewPagerAdapter = new ViewPagerAdapter(this, dataList);
        binding.viewPager2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        binding.viewPager2.setAdapter(viewPagerAdapter);
        binding.viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                //当滑动停止时才开始播放，确保ViewHolder已经完全附加
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    //大于0说明有播放
                    int playPosition = GSYVideoManager.instance().getPlayPosition();
                    if (playPosition >= 0) {
                        //对应的播放列表TAG
                        if (GSYVideoManager.instance().getPlayTag().equals(RecyclerItemNormalHolder.TAG)
                            && (mCurrentPosition != playPosition)) {
                            GSYVideoManager.releaseAllVideos();
                            playPosition(mCurrentPosition);
                        }
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mCurrentPosition = position;
            }
        });
        binding.viewPager2.post(new Runnable() {
            @Override
            public void run() {
                playPosition(0);
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (GSYVideoManager.backFromWindowFull(ViewPager2Activity.this)) {
                    return;
                }
                finish();
            }
        });
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
        binding.viewPager2.postDelayed(new Runnable() {
            @Override
            public void run() {
                RecyclerView.ViewHolder viewHolder = ((RecyclerView) binding.
                    viewPager2.getChildAt(0)).findViewHolderForAdapterPosition(position);
                if (viewHolder != null) {
                    RecyclerItemNormalHolder recyclerItemNormalHolder = (RecyclerItemNormalHolder) viewHolder;
                    //确保播放器已经附加到窗口
                    if (recyclerItemNormalHolder.getPlayer().isAttachedToWindow()) {
                        recyclerItemNormalHolder.getPlayer().startPlayLogic();
                    } else {
                        //如果还没附加到窗口，再等待一段时间
                        binding.viewPager2.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (recyclerItemNormalHolder.getPlayer().isAttachedToWindow()) {
                                    recyclerItemNormalHolder.getPlayer().startPlayLogic();
                                }
                            }
                        }, 50);
                    }
                }
            }
        }, 100);
    }
}



