package com.shuyu.gsyvideoplayer.render.view;


import com.shuyu.gsyvideoplayer.render.view.listener.IGSYSurfaceListener;

/**
 * Created by guoshuyu on 2018/1/29.
 */

public interface IGSYRenderView {
    IGSYSurfaceListener getIGSYSurfaceListener();

    void setIGSYSurfaceListener(IGSYSurfaceListener surfaceListener);

    int getSizeH();

    int getSizeW();
}
