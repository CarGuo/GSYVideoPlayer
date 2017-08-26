package com.shuyu.gsyvideoplayer.video.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.shuyu.gsyvideoplayer.GSYRenderView;
import com.shuyu.gsyvideoplayer.GSYSurfaceView;
import com.shuyu.gsyvideoplayer.GSYTextureView;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;

/**
 * 绘制View
 * Created by guoshuyu on 2017/8/2.
 */

public abstract class GSYTextureRenderView extends FrameLayout implements TextureView.SurfaceTextureListener, SurfaceHolder.Callback2 {

    //native绘制
    protected Surface mSurface;

    //渲染控件
    protected GSYRenderView mTextureView;

    //渲染控件父类
    protected ViewGroup mTextureViewContainer;

    //满屏填充暂停为徒
    protected Bitmap mFullPauseBitmap;

    //画面选择角度
    protected int mRotate;

    public GSYTextureRenderView(@NonNull Context context) {
        super(context);
    }

    public GSYTextureRenderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GSYTextureRenderView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /******************** TextureView  ****************************/

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Surface newSurface = new Surface(surface);
        pauseLogic(newSurface, true);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        //清空释放
        GSYVideoManager.instance().setDisplay(null);
        surface.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        //如果播放的是暂停全屏了
        releasePauseCover();
    }

    /******************** SurfaceView ****************************/
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        pauseLogic(holder.getSurface(), false);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //清空释放
        GSYVideoManager.instance().setDisplay(null);
        holder.getSurface().release();
    }

    @Override
    public void surfaceRedrawNeeded(SurfaceHolder holder) {
    }


    /**
     * 暂停逻辑
     */
    protected void pauseLogic(Surface surface, boolean pauseLogic) {
        mSurface = surface;
        if (pauseLogic)
            //显示暂停切换显示的图片
            showPauseCover();
        GSYVideoManager.instance().setDisplay(mSurface);
    }

    /**
     * 添加播放的view
     */
    protected void addTextureView() {

        mTextureView = new GSYRenderView();

        if (GSYVideoType.getRenderType() == GSYVideoType.SUFRACE) {
            mTextureView.addSurfaceView(getContext(), mTextureViewContainer, mRotate, this);
            return;
        }
        mTextureView.addTextureView(getContext(), mTextureViewContainer, mRotate, this);

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

    /**
     * 调整TextureView去适应比例变化
     */
    protected void changeTextureViewShowType() {
        int params = getTextureParams();
        ViewGroup.LayoutParams layoutParams = mTextureView.getLayoutParams();
        layoutParams.width = params;
        layoutParams.height = params;
        mTextureView.setLayoutParams(layoutParams);
    }

    /**
     * 暂停时初始化位图
     */
    protected void initCover() {
        mFullPauseBitmap = mTextureView.initCover();
    }

    /**
     * 小窗口渲染
     **/
    protected void setSmallVideoTextureView(OnTouchListener onTouchListener) {
        mTextureViewContainer.setOnTouchListener(onTouchListener);
        mTextureViewContainer.setOnClickListener(null);
        setSmallVideoTextureView();

    }

    //暂停时使用绘制画面显示暂停、避免黑屏
    protected abstract void showPauseCover();

    //清除暂停画面
    protected abstract void releasePauseCover();

    //小屏幕绘制层
    protected abstract void setSmallVideoTextureView();

}
