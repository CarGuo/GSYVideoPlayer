package com.shuyu.gsyvideoplayer.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.shuyu.gsyvideoplayer.listener.GSYVideoShotListener;
import com.shuyu.gsyvideoplayer.listener.GSYVideoShotSaveListener;
import com.shuyu.gsyvideoplayer.render.glrender.GSYVideoGLViewBaseRender;
import com.shuyu.gsyvideoplayer.render.view.GSYSurfaceView;
import com.shuyu.gsyvideoplayer.render.view.GSYTextureView;
import com.shuyu.gsyvideoplayer.render.view.GSYVideoGLView;
import com.shuyu.gsyvideoplayer.render.view.IGSYRenderView;
import com.shuyu.gsyvideoplayer.render.view.listener.IGSYSurfaceListener;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;

import java.io.File;

/**
 * render绘制中间控件
 * Created by guoshuyu on 2017/8/26.
 */

public class GSYRenderView {

    private IGSYRenderView mShowView;

    /*************************RenderView function start *************************/
    public void requestLayout() {
        if (mShowView != null) {
            mShowView.getRenderView().requestLayout();
        }
    }

    public float getRotation() {
        return mShowView.getRenderView().getRotation();
    }

    public void setRotation(float rotation) {
        if (mShowView != null)
            mShowView.getRenderView().setRotation(rotation);
    }

    public void invalidate() {
        if (mShowView != null)
            mShowView.getRenderView().invalidate();
    }

    public int getWidth() {
        return (mShowView != null) ? mShowView.getRenderView().getWidth() : 0;
    }

    public int getHeight() {
        return (mShowView != null) ? mShowView.getRenderView().getHeight() : 0;
    }

    public View getShowView() {
        return mShowView.getRenderView();
    }

    public ViewGroup.LayoutParams getLayoutParams() {
        return mShowView.getRenderView().getLayoutParams();
    }

    public void setLayoutParams(ViewGroup.LayoutParams layoutParams) {
        if (mShowView != null)
            mShowView.getRenderView().setLayoutParams(layoutParams);
    }

    /**
     * 添加播放的view
     */
    public void addView(final Context context, final ViewGroup textureViewContainer, final int rotate,
                        final IGSYSurfaceListener gsySurfaceListener,
                        final GSYVideoGLView.ShaderInterface effect, final float[] transform,
                        final GSYVideoGLViewBaseRender customRender, int mode) {
        if (GSYVideoType.getRenderType() == GSYVideoType.SUFRACE) {
            mShowView = GSYSurfaceView.addSurfaceView(context, textureViewContainer, rotate, gsySurfaceListener);
        } else if (GSYVideoType.getRenderType() == GSYVideoType.GLSURFACE) {
            mShowView = GSYVideoGLView.addGLView(context, textureViewContainer, rotate, gsySurfaceListener, effect, transform, customRender);
            setGLRenderMode(mode);
        } else {
            mShowView = GSYTextureView.addTextureView(context, textureViewContainer, rotate, gsySurfaceListener);
        }
    }

    /*************************RenderView function end *************************/

    /*************************ShowView function start *************************/

    public void setTransform(Matrix transform) {
        mShowView.setRenderTransform(transform);
    }

    /**
     * 暂停时初始化位图
     */
    public Bitmap initCover() {
        return mShowView.initCover();
    }

    /**
     * 暂停时初始化位图
     */
    public Bitmap initCoverHigh() {
        return mShowView.initCoverHigh();
    }

    /**
     * 获取截图
     */
    public void taskShotPic(GSYVideoShotListener gsyVideoShotListener) {
        this.taskShotPic(gsyVideoShotListener, false);
    }


    /**
     * 获取截图
     *
     * @param shotHigh 是否需要高清的
     */
    public void taskShotPic(GSYVideoShotListener gsyVideoShotListener, boolean shotHigh) {
        mShowView.taskShotPic(gsyVideoShotListener, shotHigh);
    }

    /**
     * 保存截图
     */
    public void saveFrame(final File file, GSYVideoShotSaveListener gsyVideoShotSaveListener) {
        saveFrame(file, false, gsyVideoShotSaveListener);
    }

    /**
     * 保存截图
     *
     * @param high 是否需要高清的
     */
    public void saveFrame(final File file, final boolean high, final GSYVideoShotSaveListener gsyVideoShotSaveListener) {
        mShowView.saveFrame(file, high, gsyVideoShotSaveListener);
    }

    public void onResume() {
        mShowView.onRenderResume();
    }

    public void onPause() {
        mShowView.onRenderPause();
    }

    public void releaseAll() {
        mShowView.releaseRenderAll();
    }

    public void setGLRenderMode(int mode) {
        mShowView.setRenderMode(mode);
    }

    /*************************ShowView function end *************************/


    /*************************common function *************************/

    public static void addToParent(ViewGroup textureViewContainer, View render) {
        int params = getTextureParams();
        if (textureViewContainer instanceof RelativeLayout) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(params, params);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            textureViewContainer.addView(render, layoutParams);
        } else if (textureViewContainer instanceof FrameLayout) {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(params, params);
            layoutParams.gravity = Gravity.CENTER;
            textureViewContainer.addView(render, layoutParams);
        }
    }

    /**
     * 获取布局参数
     *
     * @return
     */
    public static int getTextureParams() {
        boolean typeChanged = (GSYVideoType.getShowType() != GSYVideoType.SCREEN_TYPE_DEFAULT);
        return (typeChanged) ? ViewGroup.LayoutParams.WRAP_CONTENT : ViewGroup.LayoutParams.MATCH_PARENT;
    }

}
