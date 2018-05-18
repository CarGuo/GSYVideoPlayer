package com.example.gsyvideoplayer;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.gsyvideoplayer.utils.JumpUtils;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.utils.Debuger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.PermissionUtils;
import permissions.dispatcher.RuntimePermissions;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.open_btn)
    Button openBtn;

    @BindView(R.id.open_btn_empty)
    Button openBtn2;

    final String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Debuger.enable();
        ButterKnife.bind(this);
        boolean hadPermission = PermissionUtils.hasSelfPermissions(this, permissions);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hadPermission) {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            requestPermissions(permissions, 1110);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean sdPermissionResult = PermissionUtils.verifyPermissions(grantResults);
        if (!sdPermissionResult) {
            Toast.makeText(this, "没获取到sd卡权限，无法播放本地视频哦", Toast.LENGTH_LONG).show();
        }
    }

    @OnClick({R.id.open_btn, R.id.list_btn, R.id.list_btn_2, R.id.list_detail, R.id.clear_cache, R.id.recycler, R.id.recycler_2, R.id.list_detail_list, R.id.web_detail, R.id.danmaku_video, R.id.fragment_video,
            R.id.more_type, R.id.input_type, R.id.open_btn_empty, R.id.open_control, R.id.open_filter, R.id.open_btn_pick, R.id.open_btn_auto, R.id.open_scroll, R.id.open_window, R.id.open_btn_ad,
            R.id.open_btn_multi, R.id.open_btn_ad2, R.id.open_list_ad, R.id.open_custom_exo})
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
            case R.id.recycler:
                //recycler的demo
                JumpUtils.goToVideoRecyclerPlayer(this);
                break;
            case R.id.recycler_2:
                //recycler的demo
                JumpUtils.goToVideoRecyclerPlayer2(this);
                break;
            case R.id.list_detail:
                //支持旋转全屏的详情模式
                JumpUtils.goToDetailPlayer(this);
                break;
            case R.id.list_detail_list:
                //播放一个连续列表
                JumpUtils.goToDetailListPlayer(this);
                break;
            case R.id.web_detail:
                //正常播放，带preview
                JumpUtils.gotoWebDetail(this);
                break;
            case R.id.danmaku_video:
                //播放一个弹幕视频
                JumpUtils.gotoDanmaku(this);
                break;
            case R.id.fragment_video:
                //播放一个弹幕视频
                JumpUtils.gotoFragment(this);
                break;
            case R.id.more_type:
                //跳到多类型详情播放器，比如切换分辨率，旋转等
                JumpUtils.gotoMoreType(this);
                break;
            case R.id.input_type:
                JumpUtils.gotoInput(this);
                break;
            case R.id.open_btn_empty:
                JumpUtils.goToPlayEmptyControlActivity(this, openBtn2);
                break;
            case R.id.open_control:
                JumpUtils.gotoControl(this);
                break;
            case R.id.open_filter:
                JumpUtils.gotoFilter(this);
                break;
            case R.id.open_btn_pick:
                //无缝切换
                JumpUtils.goToVideoPickPlayer(this, openBtn);
                break;
            case R.id.open_btn_auto:
                //列表自动播放
                JumpUtils.goToAutoVideoPlayer(this);
                break;
            case R.id.open_scroll:
                //列表自动播放
                JumpUtils.goToScrollDetailPlayer(this);
                break;
            case R.id.open_window:
                //多窗体下的悬浮窗
                JumpUtils.goToScrollWindow(this);
                break;
            case R.id.open_btn_ad:
                //广告
                JumpUtils.goToVideoADPlayer(this);
                break;
            case R.id.open_btn_multi:
                //多个同时播放
                JumpUtils.goToMultiVideoPlayer(this);
                break;
            case R.id.open_btn_ad2:
                //多个同时播放
                JumpUtils.goToVideoADPlayer2(this);
                break;
            case R.id.open_list_ad:
                //多个同时播放
                JumpUtils.goToADListVideoPlayer(this);
                break;
            case R.id.open_custom_exo:
                //多个同时播放
                JumpUtils.goToDetailExoListPlayer(this);
                break;
            case R.id.clear_cache:
                //清理缓存
                GSYVideoManager.instance().clearAllDefaultCache(MainActivity.this);
                //String url = "https://res.exexm.com/cw_145225549855002";
                //GSYVideoManager.clearDefaultCache(MainActivity.this, url);
                break;
        }
    }
}
