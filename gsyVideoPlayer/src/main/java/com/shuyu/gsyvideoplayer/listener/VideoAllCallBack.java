package com.shuyu.gsyvideoplayer.listener;

/**
 * Created by Nathen
 * On 2016/04/04 22:13
 */
public interface VideoAllCallBack {

    //加载成功
    void onPrepared(String url, Object... objects);

    //点击了开始按键播放
    void onClickStartIcon(String url, Object... objects);

    //点击了错误状态下的开始按键
    void onClickStartError(String url, Object... objects);

    //点击了播放状态下的开始按键--->停止
    void onClickStop(String url, Object... objects);

    //点击了全屏播放状态下的开始按键--->停止
    void onClickStopFullscreen(String url, Object... objects);

    //点击了暂停状态下的开始按键--->播放
    void onClickResume(String url, Object... objects);

    //点击了全屏暂停状态下的开始按键--->播放
    void onClickResumeFullscreen(String url, Object... objects);

    //点击了空白弹出seekbar
    void onClickSeekbar(String url, Object... objects);

    //点击了全屏的seekbar
    void onClickSeekbarFullscreen(String url, Object... objects);

    //播放完了
    void onAutoComplete(String url, Object... objects);

    //进去全屏
    void onEnterFullscreen(String url, Object... objects);

    //退出全屏
    void onQuitFullscreen(String url, Object... objects);

    //进入小窗口
    void onQuitSmallWidget(String url, Object... objects);

    //退出小窗口
    void onEnterSmallWidget(String url, Object... objects);

    //触摸调整声音
    void onTouchScreenSeekVolume(String url, Object... objects);

    //触摸调整进度
    void onTouchScreenSeekPosition(String url, Object... objects);

    //触摸调整亮度
    void onTouchScreenSeekLight(String url, Object... objects);

    //播放错误
    void onPlayError(String url, Object... objects);

}
