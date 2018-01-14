package com.shuyu.gsyvideoplayer.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Handler;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.shuyu.gsyvideoplayer.listener.GSYVideoGLRenderErrorListener;
import com.shuyu.gsyvideoplayer.listener.GSYVideoShotListener;
import com.shuyu.gsyvideoplayer.listener.GSYVideoShotSaveListener;
import com.shuyu.gsyvideoplayer.render.glrender.GSYVideoGLViewBaseRender;
import com.shuyu.gsyvideoplayer.render.view.GSYSurfaceView;
import com.shuyu.gsyvideoplayer.render.view.GSYTextureView;
import com.shuyu.gsyvideoplayer.render.view.GSYVideoGLView;
import com.shuyu.gsyvideoplayer.utils.FileUtils;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;

import java.io.File;

/**
 * render绘制中间控件
 * Created by guoshuyu on 2017/8/26.
 */

public class GSYRenderView {

    private View mShowView;

    private Handler mHandler = new Handler();

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
        if (mShowView != null)
            mShowView.invalidate();
    }

    public int getWidth() {
        return (mShowView != null) ? mShowView.getWidth() : 0;
    }

    public int getHeight() {
        return (mShowView != null) ? mShowView.getHeight() : 0;
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

    /**
     * 暂停时初始化位图
     */
    public Bitmap initCoverHigh() {
        if (mShowView != null && mShowView instanceof GSYTextureView) {
            GSYTextureView textureView = (GSYTextureView) mShowView;
            Bitmap bitmap = Bitmap.createBitmap(
                    textureView.getSizeW(), textureView.getSizeH(), Bitmap.Config.ARGB_8888);
            return textureView.getBitmap(bitmap);
        }
        return null;
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
        if (gsyVideoShotListener != null) {
            if (mShowView instanceof GSYVideoGLView) {
                GSYVideoGLView gsyVideoGLView = (GSYVideoGLView) mShowView;
                gsyVideoGLView.setGSYVideoShotListener(gsyVideoShotListener, shotHigh);
                gsyVideoGLView.takeShotPic();
            } else if (mShowView instanceof GSYTextureView) {
                if (shotHigh) {
                    gsyVideoShotListener.getBitmap(initCoverHigh());
                } else {
                    gsyVideoShotListener.getBitmap(initCover());
                }
            }
        }
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
        GSYVideoShotListener gsyVideoShotListener = new GSYVideoShotListener() {
            @Override
            public void getBitmap(Bitmap bitmap) {
                if (bitmap == null) {
                    gsyVideoShotSaveListener.result(false, file);
                } else {
                    FileUtils.saveBitmap(bitmap, file);
                    gsyVideoShotSaveListener.result(true, file);
                }
            }
        };
        if (mShowView instanceof GSYVideoGLView) {
            GSYVideoGLView gsyVideoGLView = (GSYVideoGLView) mShowView;
            gsyVideoGLView.setGSYVideoShotListener(gsyVideoShotListener, high);
            gsyVideoGLView.takeShotPic();
        } else if (mShowView instanceof GSYTextureView) {
            if (high) {
                gsyVideoShotListener.getBitmap(initCoverHigh());
            } else {
                gsyVideoShotListener.getBitmap(initCover());
            }
        }
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

    public void releaseAll() {
        if (mShowView instanceof GSYVideoGLView) {
            GSYVideoGLView gsyVideoGLView = (GSYVideoGLView) mShowView;
            gsyVideoGLView.requestLayout();
            gsyVideoGLView.releaseAll();
        }
    }

    public void setGLRenderMode(int mode) {
        if (mShowView instanceof GSYVideoGLView) {
            GSYVideoGLView gsyVideoGLView = (GSYVideoGLView) mShowView;
            gsyVideoGLView.setMode(mode);
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

        addToParent(textureViewContainer, gsyTextureView);
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

        addToParent(textureViewContainer, showSurfaceView);
    }

    /**
     * 添加播放的view
     */
    public void addGLView(final Context context, final ViewGroup textureViewContainer, final int rotate,
                          final GSYVideoGLView.onGSYSurfaceListener gsySurfaceListener,
                          final GSYVideoGLView.ShaderInterface effect, final float[] transform,
                          final GSYVideoGLViewBaseRender customRender, final GSYVideoGLRenderErrorListener videoGLRenderErrorListener) {
        if (textureViewContainer.getChildCount() > 0) {
            textureViewContainer.removeAllViews();
        }
        GSYVideoGLView gsyVideoGLView = new GSYVideoGLView(context);
        if (customRender != null) {
            gsyVideoGLView.setCustomRenderer(customRender);
        }
        gsyVideoGLView.setEffect(effect);
        gsyVideoGLView.setGSYSurfaceListener(gsySurfaceListener);
        gsyVideoGLView.setRotation(rotate);
        gsyVideoGLView.initRender();
        gsyVideoGLView.setGSYVideoGLRenderErrorListener(videoGLRenderErrorListener);
        mShowView = gsyVideoGLView;

        if (transform != null && transform.length == 16) {
            gsyVideoGLView.setMVPMatrix(transform);
        }

        addToParent(textureViewContainer, gsyVideoGLView);
    }

    private void addToParent(ViewGroup textureViewContainer, View render) {
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
    protected int getTextureParams() {
        boolean typeChanged = (GSYVideoType.getShowType() != GSYVideoType.SCREEN_TYPE_DEFAULT);
        return (typeChanged) ? ViewGroup.LayoutParams.WRAP_CONTENT : ViewGroup.LayoutParams.MATCH_PARENT;
    }

}
