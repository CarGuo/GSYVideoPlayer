package com.example.gsyvideoplayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.gsyvideoplayer.utils.JumpUtils;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.utils.Debuger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.open_btn)
    Button openBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Debuger.enable();
        ButterKnife.bind(this);
    }

    @OnClick({R.id.open_btn, R.id.list_btn, R.id.list_btn_2, R.id.list_detail, R.id.clear_cache, R.id.recycler_2})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.open_btn:
                //直接一个页面播放的
                JumpUtils.goToVideoPlayer(this, openBtn);
                break;
            case R.id.list_btn:
                //普通列表播放，只支持全屏，但是不支持屏幕重力旋转，滑动后不持有
                JumpUtils.goToVideoPlayer(this);
                break;
            case R.id.list_btn_2:
                //支持全屏重力旋转的列表播放，滑动后不会被销毁
                JumpUtils.goToVideoPlayer2(this);
                break;
            case R.id.recycler_2:
                JumpUtils.goToVideoRecyclerPlayer2(this);
                break;
            case R.id.list_detail:
                //支持全屏重力旋转的列表播放，滑动后不会被销毁
                JumpUtils.goToDetailPlayer(this);
                break;
            case R.id.clear_cache:
                GSYVideoManager.clearAllDefaultCache(MainActivity.this);
                //String url = "http://baobab.wdjcdn.com/14564977406580.mp4";
                //GSYVideoManager.clearDefaultCache(MainActivity.this, url);
                break;
        }
    }
}
