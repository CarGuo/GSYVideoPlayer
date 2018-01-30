package com.shuyu.gsyvideoplayer.video.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.shuyu.gsyvideoplayer.render.GSYRenderView;
import com.shuyu.gsyvideoplayer.render.view.GSYVideoGLView;
import com.shuyu.gsyvideoplayer.render.effect.NoEffect;
import com.shuyu.gsyvideoplayer.render.glrender.GSYVideoGLViewBaseRender;
import com.shuyu.gsyvideoplayer.render.view.listener.IGSYSurfaceListener;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;

/**
 * 绘制View
 * Created by guoshuyu on 2017/8/2.
 */

public abstract class GSYTextureRenderView extends FrameLayout implements IGSYSurfaceListener {

    //native绘制
    protected Surface mSurface;

    //渲染控件
    protected GSYRenderView mTextureView;

    //渲染控件父类
    protected ViewGroup mTextureViewContainer;

    //满屏填充暂停为徒
    protected Bitmap mFullPauseBitmap;

    //GL的滤镜
    protected GSYVideoGLView.ShaderInterface mEffectFilter = new NoEffect();

    //GL的自定义渲染
    protected GSYVideoGLViewBaseRender mRenderer;

    //GL的角度
    protected float[] mMatrixGL = null;

    //画面选择角度
    protected int mRotate;

    //GL的布局模式
    protected int mMode = GSYVideoGLView.MODE_LAYOUT_SIZE;

    public GSYTextureRenderView(@NonNull Context context) {
        super(context);
    }

    public GSYTextureRenderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GSYTextureRenderView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /******************** start render  listener****************************/


    @Override
    public void onSurfaceAvailable(Surface surface) {
        pauseLogic(surface, (mTextureView != null && mTextureView.getShowView() instanceof TextureView));
    }

    @Override
    public void onSurfaceSizeChanged(Surface surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceDestroyed(Surface surface) {
        //清空释放
        setDisplay(null);
        //同一消息队列中去release
        releaseSurface(surface);
        return true;
    }

    @Override
    public void onSurfaceUpdated(Surface surface) {
        //如果播放的是暂停全屏了
        releasePauseCover();
    }


    /******************** end render listener****************************/

    /**
     * 暂停逻辑
     */
    protected void pauseLogic(Surface surface, boolean pauseLogic) {
        mSurface = surface;
        if (pauseLogic)
            //显示暂停切换显示的图片
            showPauseCover();
        setDisplay(mSurface);
    }

    /**
     * 添加播放的view
     * 继承后重载addTextureView，继承GSYRenderView后实现自己的IGSYRenderView类，既可以使用自己自定义的显示层
     */
    protected void addTextureView() {
        mTextureView = new GSYRenderView();
        mTextureView.addView(getContext(), mTextureViewContainer, mRotate, this, mEffectFilter, mMatrixGL, mRenderer, mMode);
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
        if (mTextureView != null) {
            int params = getTextureParams();
            ViewGroup.LayoutParams layoutParams = mTextureView.getLayoutParams();
            layoutParams.width = params;
            layoutParams.height = params;
            mTextureView.setLayoutParams(layoutParams);
        }
    }

    /**
     * 暂停时初始化位图
     */
    protected void initCover() {
        if (mTextureView != null) {
            mFullPauseBitmap = mTextureView.initCover();
        }
    }

    /**
     * 小窗口渲染
     **/
    protected void setSmallVideoTextureView(OnTouchListener onTouchListener) {
        mTextureViewContainer.setOnTouchListener(onTouchListener);
        mTextureViewContainer.setOnClickListener(null);
        setSmallVideoTextureView();
    }

    public GSYVideoGLView.ShaderInterface getEffectFilter() {
        return mEffectFilter;
    }

    /**
     * 获取渲染的代理层
     */
    public GSYRenderView getRenderProxy() {
        return mTextureView;
    }

    /**
     * 设置滤镜效果
     */
    public void setEffectFilter(GSYVideoGLView.ShaderInterface effectFilter) {
        this.mEffectFilter = effectFilter;
        if (mTextureView != null) {
            mTextureView.setEffectFilter(effectFilter);
        }
    }

    /**
     * GL模式下的画面matrix效果
     *
     * @param matrixGL 16位长度
     */
    public void setMatrixGL(float[] matrixGL) {
        this.mMatrixGL = matrixGL;
        if (mTextureView != null) {
            mTextureView.setMatrixGL(mMatrixGL);
        }
    }

    /**
     * 自定义GL的渲染render
     */
    public void setCustomGLRenderer(GSYVideoGLViewBaseRender renderer) {
        this.mRenderer = renderer;
        if (mTextureView != null) {
            mTextureView.setGLRenderer(renderer);
        }
    }

    /**
     * GL布局的绘制模式，利用布局计算大小还是使用render计算大小
     *
     * @param mode MODE_LAYOUT_SIZE = 0,  MODE_RENDER_SIZE = 1
     */
    public void setGLRenderMode(int mode) {
        mMode = mode;
        if (mTextureView != null) {
            mTextureView.setGLRenderMode(mode);
        }
    }


    //暂停时使用绘制画面显示暂停、避免黑屏
    protected abstract void showPauseCover();

    //清除暂停画面
    protected abstract void releasePauseCover();

    //小屏幕绘制层
    protected abstract void setSmallVideoTextureView();

    //设置播放
    protected abstract void setDisplay(Surface surface);

    //释放
    protected abstract void releaseSurface(Surface surface);
}
