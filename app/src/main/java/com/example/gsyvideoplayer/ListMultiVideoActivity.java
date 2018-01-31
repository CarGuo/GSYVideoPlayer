package com.example.gsyvideoplayer;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ListView;

import com.example.gsyvideoplayer.adapter.ListMultiNormalAdapter;
import com.example.gsyvideoplayer.video.manager.CustomManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 多个同时播放的demo
 */
public class ListMultiVideoActivity extends AppCompatActivity {

    @BindView(R.id.video_list)
    ListView videoList;

    ListMultiNormalAdapter listMultiNormalAdapter;

    private boolean isPause;

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

        listMultiNormalAdapter = new ListMultiNormalAdapter(this);
        videoList.setAdapter(listMultiNormalAdapter);

        videoList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastVisibleItem = firstVisibleItem + visibleItemCount;
                //大于0说明有播放
                if (CustomManager.instance().size() >= 0) {
                    Map<String, CustomManager> map = CustomManager.instance();
                    List<String> removeKey = new ArrayList<>();
                    for (Map.Entry<String, CustomManager> customManagerEntry : map.entrySet()) {
                        CustomManager customManager = customManagerEntry.getValue();
                        //当前播放的位置
                        int position = customManager.getPlayPosition();
                        //对应的播放列表TAG
                        if (customManager.getPlayTag().equals(ListMultiNormalAdapter.TAG)
                                && (position < firstVisibleItem || position > lastVisibleItem)) {
                            CustomManager.releaseAllVideos(customManagerEntry.getKey());
                            removeKey.add(customManagerEntry.getKey());
                        }
                    }
                    if(removeKey.size() > 0) {
                        for (String key : removeKey) {
                            map.remove(key);
                        }
                        listMultiNormalAdapter.notifyDataSetChanged();
                    }
                }
            }

        });

    }

    @Override
    public void onBackPressed() {
        if (CustomManager.backFromWindowFull(this, listMultiNormalAdapter.getFullKey())) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CustomManager.onPauseAll();
        isPause = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        CustomManager.onResumeAll();
        isPause = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CustomManager.clearAllVideo();
    }

}
