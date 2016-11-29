package com.example.gsyvideoplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.shuyu.gsyvideoplayer.GSYVideoPlayer;
/**
 * Created by shuyu on 2016/11/18.
 */

public class SampleExtendsPlayer extends GSYVideoPlayer {

    public SampleExtendsPlayer(Context context) {
        super(context);
    }

    public SampleExtendsPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 必须继承，你可以随意布局，但是id必须一致，还有不能少了哟
     */
    @Override
    public int getLayoutId() {
        //// TODO: 2016/11/18 返回你的布局
        return R.layout.video_layout_standard;
    }

    /**
     * 必须继承，根据你的状态实现不同的逻辑效果
     */
    @Override
    protected void setStateAndUi(int state) {
        super.setStateAndUi(state);
        switch (mCurrentState) {
            case CURRENT_STATE_NORMAL://播放UI初始化
                break;
            case CURRENT_STATE_PREPAREING://播放loading
                break;
            case CURRENT_STATE_PLAYING://播放ing
                break;
            case CURRENT_STATE_PAUSE://播放暂停
                break;
            case CURRENT_STATE_ERROR://播放错误
                break;
            case CURRENT_STATE_AUTO_COMPLETE://播放完成
                break;
            case CURRENT_STATE_PLAYING_BUFFERING_START://buffering
                break;
        }
    }


    /**
     * 继承后可以实现你自定义的UI配置
     */
    @Override
    protected void init(Context context) {
        super.init(context);
        //// TODO: 2016/11/18 你自定义的UI配置
    }

    /**
     * 继承后可以实现你自定义的UI
     */
    @Override
    public boolean setUp(String url, boolean cacheWithPlay, Object... objects) {
        super.setUp(url, cacheWithPlay, objects);
        //// TODO: 2016/11/18 你自定义的设置播放属性播放时候的UI配置
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //// TODO: 2016/11/18 补充你的触摸逻辑 ，比如你在这一页自定义的哪些UI
        return super.onTouch(v, event);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        //// TODO: 2016/11/18 补充你的点击逻辑 ，比如你在这一页自定义的哪些UI
    }

    @Override
    public void showWifiDialog() {
        super.showWifiDialog();
        //// TODO: 2016/11/18 wifi状态的显示逻辑
    }


    @Override
    public void startPlayLogic() {
        //如果继承stand播放器的话要记得super
        //super.startPlayLogic();
        //// TODO: 2016/11/18 播放开始的逻辑
    }

    @Override
    protected void setProgressAndTime(int progress, int secProgress, int currentTime, int totalTime) {
        super.setProgressAndTime(progress, secProgress, currentTime, totalTime);
        //// TODO: 2016/11/18 播放进度
    }

    @Override
    protected void resetProgressAndTime() {
        super.resetProgressAndTime();
        //// TODO: 2016/11/18 播放进度重置
    }

    @Override
    protected void showProgressDialog(float deltaX, String seekTime, int seekTimePosition, String totalTime, int totalTimeDuration) {
        super.showProgressDialog(deltaX, seekTime, seekTimePosition, totalTime, totalTimeDuration);
        //// TODO: 2016/11/18 显示快进dialog
    }

    @Override
    protected void dismissProgressDialog() {
        super.dismissProgressDialog();
        //// TODO: 2016/11/18 关闭快进dialog
    }

    @Override
    protected void showVolumeDialog(float deltaY, int volumePercent) {
        super.showVolumeDialog(deltaY, volumePercent);
        //// TODO: 2016/11/18 显示声音选择
    }

    @Override
    protected void dismissVolumeDialog() {
        super.dismissVolumeDialog();
        //// TODO: 2016/11/18 关闭声音选择
    }

    @Override
    protected void showBrightnessDialog(float percent) {
        super.showBrightnessDialog(percent);
        //// TODO: 2016/11/18 显示亮度选择
    }

    @Override
    protected void dismissBrightnessDialog() {
        super.dismissVolumeDialog();
        //// TODO: 2016/11/18 关闭亮度选择
    }

    @Override
    public void onBackFullscreen() {
        //// TODO: 2016/11/18 退出全屏逻辑
    }


}
