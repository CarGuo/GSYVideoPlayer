package com.example.gsyvideoplayer.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.shuyu.gsyvideoplayer.listener.GSYVideoShotListener;
import com.shuyu.gsyvideoplayer.listener.GSYVideoShotSaveListener;
import com.shuyu.gsyvideoplayer.render.GSYRenderView;
import com.shuyu.gsyvideoplayer.render.glrender.GSYVideoGLViewBaseRender;
import com.shuyu.gsyvideoplayer.render.view.GSYVideoGLView;
import com.shuyu.gsyvideoplayer.render.view.IGSYRenderView;
import com.shuyu.gsyvideoplayer.render.view.listener.IGSYSurfaceListener;
import com.shuyu.gsyvideoplayer.utils.MeasureHelper;

import java.io.File;

/**
 * 自定义渲染层
 * Created by guoshuyu on 2018/1/30.
 */

public class CustomTextureSurface extends SurfaceView implements IGSYRenderView, SurfaceHolder.Callback2 {

    private IGSYSurfaceListener mIGSYSurfaceListener;

    private MeasureHelper measureHelper;

    public CustomTextureSurface(Context context) {
        super(context);
        init();
    }

    public CustomTextureSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomTextureSurface(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        measureHelper = new MeasureHelper(this);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureHelper.prepareMeasure(widthMeasureSpec, heightMeasureSpec, (int) getRotation());
        setMeasuredDimension(measureHelper.getMeasuredWidth(), measureHelper.getMeasuredHeight());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mIGSYSurfaceListener != null) {
            mIGSYSurfaceListener.onSurfaceAvailable(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mIGSYSurfaceListener != null) {
            mIGSYSurfaceListener.onSurfaceSizeChanged(holder.getSurface(), width, height);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //清空释放
        if (mIGSYSurfaceListener != null) {
            mIGSYSurfaceListener.onSurfaceDestroyed(holder.getSurface());
        }
    }

    @Override
    public void surfaceRedrawNeeded(SurfaceHolder holder) {

    }

    @Override
    public IGSYSurfaceListener getIGSYSurfaceListener() {
        return mIGSYSurfaceListener;
    }

    @Override
    public void setIGSYSurfaceListener(IGSYSurfaceListener surfaceListener) {
        getHolder().addCallback(this);
        this.mIGSYSurfaceListener = surfaceListener;
    }


    @Override
    public int getSizeH() {
        return measureHelper.getMeasuredHeight();
    }

    @Override
    public int getSizeW() {
        return measureHelper.getMeasuredWidth();
    }

    @Override
    public void taskShotPic(GSYVideoShotListener gsyVideoShotListener, boolean shotHigh) {

    }

    @Override
    public void saveFrame(File file, boolean high, GSYVideoShotSaveListener gsyVideoShotSaveListener) {

    }

    @Override
    public View getRenderView() {
        return this;
    }

    @Override
    public Bitmap initCover() {
        return null;
    }

    @Override
    public Bitmap initCoverHigh() {
        return null;
    }

    @Override
    public void onRenderResume() {

    }

    @Override
    public void onRenderPause() {

    }

    @Override
    public void releaseRenderAll() {

    }

    @Override
    public void setRenderMode(int mode) {

    }

    @Override
    public void setRenderTransform(Matrix transform) {

    }

    @Override
    public void setGLRenderer(GSYVideoGLViewBaseRender renderer) {

    }

    @Override
    public void setGLMVPMatrix(float[] MVPMatrix) {

    }

    @Override
    public void setGLEffectFilter(GSYVideoGLView.ShaderInterface effectFilter) {

    }

    /**
     * 添加播放的view
     */
    public static CustomTextureSurface addSurfaceView(Context context, ViewGroup textureViewContainer, int rotate,
                                                      final IGSYSurfaceListener gsySurfaceListener) {
        if (textureViewContainer.getChildCount() > 0) {
            textureViewContainer.removeAllViews();
        }
        CustomTextureSurface showSurfaceView = new CustomTextureSurface(context);
        showSurfaceView.setIGSYSurfaceListener(gsySurfaceListener);
        showSurfaceView.setRotation(rotate);
        GSYRenderView.addToParent(textureViewContainer, showSurfaceView);
        return showSurfaceView;
    }
}
