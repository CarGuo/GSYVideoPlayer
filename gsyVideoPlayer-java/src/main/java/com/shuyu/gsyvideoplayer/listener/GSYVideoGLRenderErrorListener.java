package com.shuyu.gsyvideoplayer.listener;

/**
 * GL渲染错误
 * Created by guoshuyu on 2018/1/14.
 */

public interface GSYVideoGLRenderErrorListener {
    void onError(String Error, int code, boolean byChangedRenderError);
}
