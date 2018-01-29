package com.shuyu.gsyvideoplayer.render.view.listener;

import com.shuyu.gsyvideoplayer.render.glrender.GSYVideoGLViewBaseRender;

/**
 * GL渲染错误
 * Created by guoshuyu on 2018/1/14.
 */

public interface GSYVideoGLRenderErrorListener {
    void onError(GSYVideoGLViewBaseRender render, String Error, int code, boolean byChangedRenderError);
}
