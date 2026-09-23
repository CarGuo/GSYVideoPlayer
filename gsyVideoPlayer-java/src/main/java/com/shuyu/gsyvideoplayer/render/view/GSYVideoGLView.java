package com.shuyu.gsyvideoplayer.render.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;

import com.shuyu.gsyvideoplayer.listener.GSYVideoShotSaveListener;
import com.shuyu.gsyvideoplayer.render.GSYRenderView;
import com.shuyu.gsyvideoplayer.render.view.listener.GSYVideoGLRenderErrorListener;
import com.shuyu.gsyvideoplayer.listener.GSYVideoShotListener;
import com.shuyu.gsyvideoplayer.render.glrender.GSYVideoGLViewBaseRender;
import com.shuyu.gsyvideoplayer.render.glrender.GSYVideoGLViewSimpleRender;
import com.shuyu.gsyvideoplayer.render.view.listener.GLSurfaceListener;
import com.shuyu.gsyvideoplayer.render.view.listener.IGSYSurfaceListener;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.utils.FileUtils;
import com.shuyu.gsyvideoplayer.utils.MeasureHelper;
import com.shuyu.gsyvideoplayer.render.effect.NoEffect;

import java.io.File;


/**
 * 在videffects的基础上调整的
 * <p>
 * 原 @author sheraz.khilji
 */
@SuppressLint("ViewConstructor")
public class GSYVideoGLView extends GLSurfaceView implements GLSurfaceListener, IGSYRenderView, MeasureHelper.MeasureFormVideoParamsListener {

    private static final String TAG = GSYVideoGLView.class.getName();
    /**
     * 利用布局计算大小
     */
    public static final int MODE_LAYOUT_SIZE = 0;
    /**
     * 利用Render计算大小
     */
    public static final int MODE_RENDER_SIZE = 1;

    private GSYVideoGLViewBaseRender mRenderer;

    private Context mContext;

    private ShaderInterface mEffect = new NoEffect();

    private MeasureHelper.MeasureFormVideoParamsListener mVideoParamsListener;

    private MeasureHelper measureHelper;

    private GLSurfaceListener mOnGSYSurfaceListener;

    private IGSYSurfaceListener mIGSYSurfaceListener;

    private float[] mMVPMatrix;

    private int mMode = MODE_LAYOUT_SIZE;

    private boolean mRendererInitialized;

    private Surface mCurrentSurface;

    public interface ShaderInterface {
        String getShader(GLSurfaceView mGlSurfaceView);
    }

    /**
     * 需要自带 GL 纹理资产（如 LUT 查找表）的单 pass 效果。
     * <p>
     * 所有回调均在 GL 线程触发：{@link #onSurfaceReady} 负责加载并上传纹理，
     * {@link #onBindTextures} 每帧把资产纹理绑到主纹理（OES 占用 GL_TEXTURE0）
     * 之外的单元并设置 sampler，{@link #onSurfaceRelease} 删除纹理。
     */
    public interface TextureShaderInterface extends ShaderInterface {

        void onSurfaceReady(GLSurfaceView glSurfaceView);

        void onBindTextures(GLSurfaceView glSurfaceView, int program);

        void onSurfaceRelease(GLSurfaceView glSurfaceView);
    }

    public GSYVideoGLView(Context context) {
        super(context);
        init(context);
    }

    public GSYVideoGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        setEGLContextClientVersion(2);
        mRenderer = new GSYVideoGLViewSimpleRender();
        measureHelper = new MeasureHelper(this, this);
        mRenderer.setSurfaceView(GSYVideoGLView.this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mRenderer != null) {
            mRenderer.initRenderSize();
        }
        if (mRendererInitialized) {
            requestRender();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mMode == MODE_RENDER_SIZE) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            measureHelper.prepareMeasure(widthMeasureSpec, heightMeasureSpec, (int) getRotation());
            initRenderMeasure();
        } else {
            measureHelper.prepareMeasure(widthMeasureSpec, heightMeasureSpec, (int) getRotation());
            setMeasuredDimension(measureHelper.getMeasuredWidth(), measureHelper.getMeasuredHeight());
        }
    }

    @Override
    public IGSYSurfaceListener getIGSYSurfaceListener() {
        return mIGSYSurfaceListener;
    }

    @Override
    public void setIGSYSurfaceListener(IGSYSurfaceListener surfaceListener) {
        setOnGSYSurfaceListener(this);
        mIGSYSurfaceListener = surfaceListener;
    }

    @Override
    public void onSurfaceAvailable(Surface surface) {
        mCurrentSurface = surface;
        if (mIGSYSurfaceListener != null) {
            mIGSYSurfaceListener.onSurfaceAvailable(surface);
        }
    }

    @Override
    public int getSizeH() {
        return getHeight();
    }

    @Override
    public int getSizeW() {
        return getWidth();
    }

    @Override
    public Bitmap initCover() {
        Debuger.printfLog(getClass().getSimpleName() + " not support initCover now");
        return null;
    }

    @Override
    public Bitmap initCoverHigh() {
        Debuger.printfLog(getClass().getSimpleName() + " not support initCoverHigh now");
        return null;
    }

    /**
     * 获取截图
     *
     * @param shotHigh 是否需要高清的
     */
    @Override
    public void taskShotPic(GSYVideoShotListener gsyVideoShotListener, boolean shotHigh) {
        if (gsyVideoShotListener == null) {
            return;
        }
        setGSYVideoShotListener(gsyVideoShotListener, shotHigh);
        takeShotPic();
    }

    /**
     * 保存截图
     *
     * @param high 是否需要高清的
     */
    @Override
    public void saveFrame(final File file, final boolean high, final GSYVideoShotSaveListener gsyVideoShotSaveListener) {
        GSYVideoShotListener gsyVideoShotListener = new GSYVideoShotListener() {
            @Override
            public void getBitmap(Bitmap bitmap) {
                boolean success = bitmap != null && FileUtils.saveBitmapToFile(bitmap, file);
                if (gsyVideoShotSaveListener != null) {
                    gsyVideoShotSaveListener.result(success, file);
                }
            }
        };
        setGSYVideoShotListener(gsyVideoShotListener, high);
        takeShotPic();
    }

    @Override
    public View getRenderView() {
        return this;
    }


    @Override
    public void onRenderResume() {
        requestLayout();
        onResume();
    }

    @Override
    public void onRenderPause() {
        requestLayout();
        onPause();

    }

    @Override
    public void releaseRenderAll() {
        requestLayout();
        releaseAll();

    }

    @Override
    public void setRenderMode(int mode) {
        setMode(mode);
    }


    @Override
    public void setRenderTransform(Matrix transform) {
        Debuger.printfLog(getClass().getSimpleName() + " not support setRenderTransform now");
    }

    @Override
    public void setGLRenderer(GSYVideoGLViewBaseRender renderer) {
        setCustomRenderer(renderer);
    }

    @Override
    public void setGLMVPMatrix(float[] MVPMatrix) {
        setMVPMatrix(MVPMatrix);
    }

    /**
     * 设置滤镜效果
     */
    @Override
    public void setGLEffectFilter(GSYVideoGLView.ShaderInterface effectFilter) {
        setEffect(effectFilter);
    }


    @Override
    public void setVideoParamsListener(MeasureHelper.MeasureFormVideoParamsListener listener) {
        mVideoParamsListener = listener;
    }

    @Override
    public int getCurrentVideoWidth() {
        if (mVideoParamsListener != null) {
            return mVideoParamsListener.getCurrentVideoWidth();
        }
        return 0;
    }

    @Override
    public int getCurrentVideoHeight() {
        if (mVideoParamsListener != null) {
            return mVideoParamsListener.getCurrentVideoHeight();
        }
        return 0;
    }

    @Override
    public int getVideoSarNum() {
        if (mVideoParamsListener != null) {
            return mVideoParamsListener.getVideoSarNum();
        }
        return 0;
    }

    @Override
    public int getVideoSarDen() {
        if (mVideoParamsListener != null) {
            return mVideoParamsListener.getVideoSarDen();
        }
        return 0;
    }

    protected void initRenderMeasure() {
        if (mVideoParamsListener != null && mMode == MODE_RENDER_SIZE) {
            try {
                int videoWidth = mVideoParamsListener.getCurrentVideoWidth();
                int videoHeight = mVideoParamsListener.getCurrentVideoHeight();
                if (this.mRenderer != null) {
                    this.mRenderer.setCurrentViewWidth(measureHelper.getMeasuredWidth());
                    this.mRenderer.setCurrentViewHeight(measureHelper.getMeasuredHeight());
                    this.mRenderer.setCurrentVideoWidth(videoWidth);
                    this.mRenderer.setCurrentVideoHeight(videoHeight);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void initRender() {
        setRenderer(mRenderer);
        mRendererInitialized = true;
        super.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        requestRender();
    }


    public void setGSYVideoGLRenderErrorListener(GSYVideoGLRenderErrorListener videoGLRenderErrorListener) {
        this.mRenderer.setGSYVideoGLRenderErrorListener(videoGLRenderErrorListener);
    }

    /**
     * 设置自定义的render，其他自定义设置会被取消，需要重新设置
     * 在initRender() 前设置才会生效
     *
     * @param CustomRender
     */
    public void setCustomRenderer(GSYVideoGLViewBaseRender CustomRender) {
        if (CustomRender == null) {
            return;
        }
        if (mRendererInitialized) {
            Debuger.printfLog(getClass().getSimpleName() + " not support setCustomRenderer after initRender");
            return;
        }
        this.mRenderer = CustomRender;
        mRenderer.setSurfaceView(GSYVideoGLView.this);
        mRenderer.setGSYSurfaceListener(this.mOnGSYSurfaceListener);
        mRenderer.setEffect(mEffect);
        if (mMVPMatrix != null && mMVPMatrix.length == 16) {
            mRenderer.setMVPMatrix(mMVPMatrix);
        }
        initRenderMeasure();
    }

    public void setOnGSYSurfaceListener(GLSurfaceListener mGSYSurfaceListener) {
        this.mOnGSYSurfaceListener = mGSYSurfaceListener;
        mRenderer.setGSYSurfaceListener(this.mOnGSYSurfaceListener);
    }

    public void setEffect(ShaderInterface shaderEffect) {
        if (shaderEffect != null) {
            mEffect = shaderEffect;
            runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    if (mRenderer != null) {
                        mRenderer.setEffect(mEffect);
                    }
                }
            });
        }
    }

    public void setMVPMatrix(float[] MVPMatrix) {
        if (MVPMatrix != null && MVPMatrix.length == 16) {
            mMVPMatrix = MVPMatrix.clone();
            runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    if (mRenderer != null) {
                        mRenderer.setMVPMatrix(mMVPMatrix);
                    }
                }
            });
        }
    }

    public void takeShotPic() {
        runOnGLThread(new Runnable() {
            @Override
            public void run() {
                if (mRenderer != null) {
                    mRenderer.takeShotPic();
                }
            }
        });
    }


    public void setGSYVideoShotListener(GSYVideoShotListener listener, boolean high) {
        this.mRenderer.setGSYVideoShotListener(listener, high);
    }

    public int getMode() {
        return mMode;
    }

    /**
     * @param mode MODE_LAYOUT_SIZE = 0,  MODE_RENDER_SIZE = 1
     */
    public void setMode(int mode) {
        this.mMode = mode;
    }

    public void releaseAll() {
        if (mRenderer != null) {
            final GSYVideoGLViewBaseRender renderer = mRenderer;
            renderer.markReleaseRequested();
            notifySurfaceDestroyedForPlayer();
            if (mRendererInitialized) {
                try {
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            renderer.releaseAll();
                        }
                    });
                    requestRender();
                } catch (IllegalStateException e) {
                    renderer.releaseNonGLResources();
                }
            } else {
                renderer.releaseAll();
            }
        }
    }

    public GSYVideoGLViewBaseRender getRenderer() {
        return mRenderer;
    }

    private void notifySurfaceDestroyedForPlayer() {
        Surface surface = mCurrentSurface;
        mCurrentSurface = null;
        if (surface != null && mIGSYSurfaceListener != null) {
            mIGSYSurfaceListener.onSurfaceDestroyed(surface);
        }
    }

    public ShaderInterface getEffect() {
        return mEffect;
    }

    public float[] getMVPMatrix() {
        return mMVPMatrix;
    }

    /**
     * 添加播放的view
     */
    public static GSYVideoGLView addGLView(final Context context, final ViewGroup textureViewContainer, final int rotate, final IGSYSurfaceListener gsySurfaceListener, final MeasureHelper.MeasureFormVideoParamsListener videoParamsListener, final GSYVideoGLView.ShaderInterface effect, final float[] transform, final GSYVideoGLViewBaseRender customRender, final int renderMode) {
        if (textureViewContainer.getChildCount() > 0) {
            textureViewContainer.removeAllViews();
        }
        final GSYVideoGLView gsyVideoGLView = new GSYVideoGLView(context);
        if (customRender != null) {
            gsyVideoGLView.setCustomRenderer(customRender);
        }
        gsyVideoGLView.setEffect(effect);
        gsyVideoGLView.setVideoParamsListener(videoParamsListener);
        gsyVideoGLView.setRenderMode(renderMode);
        gsyVideoGLView.setIGSYSurfaceListener(gsySurfaceListener);
        gsyVideoGLView.setRotation(rotate);
        gsyVideoGLView.initRender();
        gsyVideoGLView.setGSYVideoGLRenderErrorListener(new GSYVideoGLRenderErrorListener() {
            @Override
            public void onError(GSYVideoGLViewBaseRender render, String Error, int code, boolean byChangedRenderError) {
                if (byChangedRenderError)
                    addGLView(context, textureViewContainer, rotate, gsySurfaceListener, videoParamsListener, render.getEffect(), render.hasCustomMVPMatrix() ? render.getMVPMatrix() : transform, render, renderMode);

            }
        });
        if (transform != null && transform.length == 16) {
            gsyVideoGLView.setMVPMatrix(transform);
        } else if (rotate != 0) {
            float[] rotateMatrix = new float[16];
            android.opengl.Matrix.setIdentityM(rotateMatrix, 0);
            android.opengl.Matrix.rotateM(rotateMatrix, 0, -rotate, 0, 0, 1);
            gsyVideoGLView.setMVPMatrix(rotateMatrix);
        }
        GSYRenderView.addToParent(textureViewContainer, gsyVideoGLView);
        return gsyVideoGLView;
    }

    private void runOnGLThread(final Runnable runnable) {
        if (runnable == null) {
            return;
        }
        if (!mRendererInitialized) {
            runnable.run();
            return;
        }
        try {
            queueEvent(runnable);
            requestRender();
        } catch (IllegalStateException e) {
            runnable.run();
        }
    }


}
