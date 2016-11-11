package com.shuyu.gsyvideoplayer.listener;

public interface StandardVideoAllCallBack extends VideoAllCallBack {

    void onClickStartThumb(String url, Object... objects);

    void onClickBlank(String url, Object... objects);

    void onClickBlankFullscreen(String url, Object... objects);

}
