package com.shuyu.gsyvideoplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.shuyu.gsyvideoplayer.utils.GSYVideoType;

/**
 * render绘制中间控件
 * Created by guoshuyu on 2017/8/26.
 */

public class GSYRenderView {

    private View mShowView;

    public void setTransform(Matrix transform) {
        if (mShowView instanceof TextureView) {
            ((TextureView) mShowView).setTransform(transform);
        }
    }

    public void requestLayout() {
        if (mShowView != null)
            mShowView.requestLayout();
    }

    public float getRotation() {
        return mShowView.getRotation();
    }

    public void setRotation(float rotation) {
        if (mShowView != null)
            mShowView.setRotation(rotation);
    }

    public void invalidate() {
        mShowView.invalidate();
    }

    public int getWidth() {
        return mShowView.getWidth();
    }

    public int getHeight() {
        return mShowView.getHeight();
    }

    public View getShowView() {
        return mShowView;
    }

    /**
     * 暂停时初始化位图
     */
    public Bitmap initCover() {
        if (mShowView != null && mShowView instanceof GSYTextureView) {
            GSYTextureView textureView = (GSYTextureView) mShowView;
            Bitmap bitmap = Bitmap.createBitmap(
                    textureView.getSizeW(), textureView.getSizeH(), Bitmap.Config.RGB_565);
            return textureView.getBitmap(bitmap);
        }
        return null;
    }

    public ViewGroup.LayoutParams getLayoutParams() {
        return mShowView.getLayoutParams();
    }

    public void setLayoutParams(ViewGroup.LayoutParams layoutParams) {
        if (mShowView != null)
            mShowView.setLayoutParams(layoutParams);
    }

    public void onResume() {
        if (mShowView instanceof GSYVideoGLView) {
            GSYVideoGLView gsyVideoGLView = (GSYVideoGLView) mShowView;
            gsyVideoGLView.requestLayout();
            gsyVideoGLView.onResume();
        }
    }

    public void onPause() {
        if (mShowView instanceof GSYVideoGLView) {
            GSYVideoGLView gsyVideoGLView = (GSYVideoGLView) mShowView;
            gsyVideoGLView.requestLayout();
            gsyVideoGLView.onPause();
        }
    }

    /**
     * 添加播放的view
     */
    public void addTextureView(Context context, ViewGroup textureViewContainer, int rotate, TextureView.SurfaceTextureListener listener) {

        if (textureViewContainer.getChildCount() > 0) {
            textureViewContainer.removeAllViews();
        }
        GSYTextureView gsyTextureView = new GSYTextureView(context);
        gsyTextureView.setSurfaceTextureListener(listener);
        gsyTextureView.setRotation(rotate);

        mShowView = gsyTextureView;

        int params = getTextureParams();

        if (textureViewContainer instanceof RelativeLayout) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(params, params);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            textureViewContainer.addView(gsyTextureView, layoutParams);
        } else if (textureViewContainer instanceof FrameLayout) {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(params, params);
            layoutParams.gravity = Gravity.CENTER;
            textureViewContainer.addView(gsyTextureView, layoutParams);
        }
    }

    /**
     * 添加播放的view
     */
    public void addSurfaceView(Context context, ViewGroup textureViewContainer, int rotate, SurfaceHolder.Callback2 callback2) {
        if (textureViewContainer.getChildCount() > 0) {
            textureViewContainer.removeAllViews();
        }
        GSYSurfaceView showSurfaceView = new GSYSurfaceView(context);
        showSurfaceView.getHolder().addCallback(callback2);
        showSurfaceView.setRotation(rotate);

        mShowView = showSurfaceView;

        int params = getTextureParams();

        if (textureViewContainer instanceof RelativeLayout) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(params, params);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            textureViewContainer.addView(showSurfaceView, layoutParams);
        } else if (textureViewContainer instanceof FrameLayout) {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(params, params);
            layoutParams.gravity = Gravity.CENTER;
            textureViewContainer.addView(showSurfaceView, layoutParams);
        }
    }

    /**
     * 添加播放的view
     */
    public void addGLView(Context context, ViewGroup textureViewContainer, int rotate, GSYVideoGLView.onGSYSurfaceListener gsySurfaceListener, GSYVideoGLView.ShaderInterface effect) {
        if (textureViewContainer.getChildCount() > 0) {
            textureViewContainer.removeAllViews();
        }
        GSYVideoGLView gsyVideoGLView = new GSYVideoGLView(context);
        gsyVideoGLView.setEffect(effect);
        gsyVideoGLView.setGSYSurfaceListener(gsySurfaceListener);
        gsyVideoGLView.setRotation(rotate);

        mShowView = gsyVideoGLView;

        int params = getTextureParams();

        if (textureViewContainer instanceof RelativeLayout) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(params, params);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            textureViewContainer.addView(gsyVideoGLView, layoutParams);
        } else if (textureViewContainer instanceof FrameLayout) {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(params, params);
            layoutParams.gravity = Gravity.CENTER;
            textureViewContainer.addView(gsyVideoGLView, layoutParams);
        }
    }

    /**
     * 获取布局参数
     *
     * @return
     */
    protected int getTextureParams() {
        boolean typeChanged = (GSYVideoType.getShowType() != GSYVideoType.SCREEN_TYPE_DEFAULT);
        return (typeChanged) ? ViewGroup.LayoutParams.WRAP_CONTENT : ViewGroup.LayoutParams.MATCH_PARENT;
    }

}
