package com.shuyu.gsyvideoplayer.render.view.listener;

import com.shuyu.gsyvideoplayer.render.glrender.GSYVideoGLViewBaseRender;

/**
 * GL渲染错误
 * Created by guoshuyu on 2018/1/14.
 */
public interface GSYVideoGLRenderErrorListener {
    /**
     *
     * @param render
     * @param Error 错误文本
     * @param code 错误代码
     * @param byChangedRenderError 错误是因为切换effect导致的
     */
    void onError(GSYVideoGLViewBaseRender render, String Error, int code, boolean byChangedRenderError);
}
