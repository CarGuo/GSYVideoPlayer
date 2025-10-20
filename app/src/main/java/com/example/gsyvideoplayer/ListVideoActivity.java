package com.example.gsyvideoplayer;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.transition.Explode;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gsyvideoplayer.adapter.ListNormalAdapter;
import com.example.gsyvideoplayer.databinding.ActivityListVideo2Binding;
import com.example.gsyvideoplayer.databinding.ActivityListVideoBinding;
import com.shuyu.gsyvideoplayer.GSYVideoManager;

public class ListVideoActivity extends AppCompatActivity {


    ListNormalAdapter listNormalAdapter;

    private boolean isPause;
    private ActivityListVideoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 设置一个exit transition
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setEnterTransition(new Explode());
            getWindow().setExitTransition(new Explode());
        }
        super.onCreate(savedInstanceState);


        binding = ActivityListVideoBinding.inflate(getLayoutInflater());

        View rootView = binding.getRoot();
        setContentView(rootView);


        listNormalAdapter = new ListNormalAdapter(this);
        binding.videoList.setAdapter(listNormalAdapter);

        binding.videoList.setOnScrollListener(new AbsListView.OnScrollListener() {
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
                        if(GSYVideoManager.isFullState(ListVideoActivity.this)) {
                            return;
                        }
                        //如果滑出去了上面和下面就是否，和今日头条一样
                        GSYVideoManager.releaseAllVideos();
                        listNormalAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
//为了支持重力旋转
        onBackPressAdapter();

        if (GSYVideoManager.backFromWindowFull(this)) {
            return;
        }
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        GSYVideoManager.onPause();
        isPause = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        GSYVideoManager.onResume();
        isPause = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GSYVideoManager.releaseAllVideos();
        if (listNormalAdapter != null) {
            listNormalAdapter.onDestroy();
        }
    }

    /********************************为了支持重力旋转********************************/
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (listNormalAdapter != null && listNormalAdapter.getListNeedAutoLand() && !isPause) {
            listNormalAdapter.onConfigurationChanged(this, newConfig);
        }
    }

    private void onBackPressAdapter() {
        //为了支持重力旋转
        if (listNormalAdapter != null && listNormalAdapter.getListNeedAutoLand()) {
            listNormalAdapter.onBackPressed();
        }
    }
}
