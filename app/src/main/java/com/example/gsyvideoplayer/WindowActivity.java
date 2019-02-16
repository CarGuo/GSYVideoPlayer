package com.example.gsyvideoplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.Button;


import com.example.gsyvideoplayer.utils.floatUtil.FloatWindow;
import com.example.gsyvideoplayer.utils.floatUtil.MoveType;
import com.example.gsyvideoplayer.utils.floatUtil.Screen;
import com.example.gsyvideoplayer.utils.floatUtil.Util;
import com.example.gsyvideoplayer.view.FloatPlayerView;
import com.shuyu.gsyvideoplayer.GSYVideoManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 多窗体下的悬浮窗页面
 */
public class WindowActivity extends AppCompatActivity {

    @BindView(R.id.start_window)
    Button startWindow;
    @BindView(R.id.jump_other)
    Button jumpOther;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_window);
        ButterKnife.bind(this);
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Util.hasPermission(this)) {
                requestAlertWindowPermission();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GSYVideoManager.instance().releaseMediaPlayer();
        /**
         * 这里在返回主页的时候销毁了，因为不想和DEMO中其他页面冲突
         */
        FloatWindow.destroy();
    }

    @RequiresApi(api = 23)
    private void requestAlertWindowPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, 1);
    }


    @RequiresApi(api = 23)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Build.VERSION.SDK_INT >= 23){
            //todo 用23以上编译即可出现canDrawOverlays
            if (Util.hasPermission(this)) {

            } else {
                this.finish();
            }
        }
    }

    @OnClick({R.id.start_window, R.id.jump_other})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.start_window:
                if (FloatWindow.get() != null) {
                    return;
                }
                FloatPlayerView floatPlayerView = new FloatPlayerView(getApplicationContext());
                FloatWindow
                        .with(getApplicationContext())
                        .setView(floatPlayerView)
                        .setWidth(Screen.width, 0.4f)
                        .setHeight(Screen.width, 0.4f)
                        .setX(Screen.width, 0.8f)
                        .setY(Screen.height, 0.3f)
                        .setMoveType(MoveType.slide)
                        .setFilter(false)
                        .setMoveStyle(500, new BounceInterpolator())
                        .build();
                FloatWindow.get().show();
                break;
            case R.id.jump_other:
                startActivity(new Intent(this, EmptyActivity.class));
                break;
        }
    }
}
