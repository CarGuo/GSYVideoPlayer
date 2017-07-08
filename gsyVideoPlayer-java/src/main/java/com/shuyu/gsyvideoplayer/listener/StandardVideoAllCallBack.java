package com.shuyu.gsyvideoplayer.listener;

public interface StandardVideoAllCallBack extends VideoAllCallBack {

    //点击了空白区域开始播放，objects[0]是title，object[1]是当前所处播放器（全屏或非全屏）
    void onClickStartThumb(String url, Object... objects);

    //点击了播放中的空白区域，objects[0]是title，object[1]是当前所处播放器（全屏或非全屏）
    void onClickBlank(String url, Object... objects);

    //点击了全屏播放中的空白区域，objects[0]是title，object[1]是当前所处播放器（全屏或非全屏）
    void onClickBlankFullscreen(String url, Object... objects);

}
