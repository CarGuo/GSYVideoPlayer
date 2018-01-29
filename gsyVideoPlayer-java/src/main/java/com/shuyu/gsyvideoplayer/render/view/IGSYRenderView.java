package com.shuyu.gsyvideoplayer.render.view;


import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.view.TextureView;
import android.view.View;

import com.shuyu.gsyvideoplayer.listener.GSYVideoShotListener;
import com.shuyu.gsyvideoplayer.listener.GSYVideoShotSaveListener;
import com.shuyu.gsyvideoplayer.render.view.listener.IGSYSurfaceListener;

import java.io.File;

/**
 * Created by guoshuyu on 2018/1/29.
 */

public interface IGSYRenderView {

    IGSYSurfaceListener getIGSYSurfaceListener();

    void setIGSYSurfaceListener(IGSYSurfaceListener surfaceListener);

    int getSizeH();

    int getSizeW();

    void taskShotPic(GSYVideoShotListener gsyVideoShotListener, boolean shotHigh);

    void saveFrame(final File file, final boolean high, final GSYVideoShotSaveListener gsyVideoShotSaveListener);

    View getRenderView();

    Bitmap initCover();

    Bitmap initCoverHigh();

    void onRenderResume();

    void onRenderPause();

    void releaseRenderAll();

    void setRenderMode(int mode);

    void setRenderTransform(Matrix transform);

}
