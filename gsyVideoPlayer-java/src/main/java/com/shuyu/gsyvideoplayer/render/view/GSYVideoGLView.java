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

import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.listener.GSYVideoShotSaveListener;
import com.shuyu.gsyvideoplayer.render.GSYRenderView;
import com.shuyu.gsyvideoplayer.render.view.listener.GSYVideoGLRenderErrorListener;
import com.shuyu.gsyvideoplayer.listener.GSYVideoShotListener;
import com.shuyu.gsyvideoplayer.render.glrender.GSYVideoGLViewBaseRender;
import com.shuyu.gsyvideoplayer.render.glrender.GSYVideoGLViewSimpleRender;
import com.shuyu.gsyvideoplayer.render.view.listener.GLSurfaceListener;
import com.shuyu.gsyvideoplayer.render.view.listener.IGSYSurfaceListener;
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
public class GSYVideoGLView extends GLSurfaceView implements GLSurfaceListener, IGSYRenderView {

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

    private MeasureHelper measureHelper;

    private GLSurfaceListener mOnGSYSurfaceListener;

    private IGSYSurfaceListener mIGSYSurfaceListener;

    private float[] mMVPMatrix;

    private int mMode = MODE_LAYOUT_SIZE;

    public interface ShaderInterface {
        String getShader(GLSurfaceView mGlSurfaceView);
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
        measureHelper = new MeasureHelper(this);
        mRenderer.setSurfaceView(GSYVideoGLView.this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mRenderer != null) {
            mRenderer.initRenderSize();
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
        if (mIGSYSurfaceListener != null) {
            mIGSYSurfaceListener.onSurfaceAvailable(surface);
        }
    }

    @Override
    public int getSizeH() {
        return measureHelper.getMeasuredHeight();
    }

    @Override
    public int getSizeW() {
        return measureHelper.getMeasuredWidth();
    }


    protected void initRenderMeasure() {
        if (GSYVideoManager.instance().getMediaPlayer() != null && mMode == MODE_RENDER_SIZE) {
            try {
                int videoWidth = GSYVideoManager.instance().getCurrentVideoWidth();
                int videoHeight = GSYVideoManager.instance().getCurrentVideoHeight();
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
        this.mRenderer = CustomRender;
        mRenderer.setSurfaceView(GSYVideoGLView.this);
        initRenderMeasure();
    }

    public void setOnGSYSurfaceListener(GLSurfaceListener mGSYSurfaceListener) {
        this.mOnGSYSurfaceListener = mGSYSurfaceListener;
        mRenderer.setGSYSurfaceListener(this.mOnGSYSurfaceListener);
    }

    public void setEffect(ShaderInterface shaderEffect) {
        if (shaderEffect != null) {
            mEffect = shaderEffect;
            mRenderer.setEffect(mEffect);
        }
    }

    public void setMVPMatrix(float[] MVPMatrix) {
        if (MVPMatrix != null) {
            mMVPMatrix = MVPMatrix;
            mRenderer.setMVPMatrix(MVPMatrix);
        }
    }

    public void takeShotPic() {
        mRenderer.takeShotPic();
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
            mRenderer.releaseAll();
        }
    }


    public GSYVideoGLViewBaseRender getRenderer() {
        return mRenderer;
    }

    public ShaderInterface getEffect() {
        return mEffect;
    }

    public float[] getMVPMatrix() {
        return mMVPMatrix;
    }


    @Override
    public Bitmap initCover() {
        return null;
    }

    @Override
    public Bitmap initCoverHigh() {
        return null;
    }


    /**
     * 获取截图
     *
     * @param shotHigh 是否需要高清的
     */
    public void taskShotPic(GSYVideoShotListener gsyVideoShotListener, boolean shotHigh) {
        if (gsyVideoShotListener != null) {
            setGSYVideoShotListener(gsyVideoShotListener, shotHigh);
            takeShotPic();

        }
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

    }

    /**
     * 添加播放的view
     */
    public static GSYVideoGLView addGLView(final Context context, final ViewGroup textureViewContainer, final int rotate,
                                           final IGSYSurfaceListener gsySurfaceListener,
                                           final GSYVideoGLView.ShaderInterface effect, final float[] transform,
                                           final GSYVideoGLViewBaseRender customRender) {
        if (textureViewContainer.getChildCount() > 0) {
            textureViewContainer.removeAllViews();
        }
        final GSYVideoGLView gsyVideoGLView = new GSYVideoGLView(context);
        if (customRender != null) {
            gsyVideoGLView.setCustomRenderer(customRender);
        }
        gsyVideoGLView.setEffect(effect);
        gsyVideoGLView.setIGSYSurfaceListener(gsySurfaceListener);
        gsyVideoGLView.setRotation(rotate);
        gsyVideoGLView.initRender();
        gsyVideoGLView.setGSYVideoGLRenderErrorListener(new GSYVideoGLRenderErrorListener() {
            @Override
            public void onError(GSYVideoGLViewBaseRender render, String Error, int code, boolean byChangedRenderError) {
                if (byChangedRenderError)
                    addGLView(context,
                            textureViewContainer,
                            rotate,
                            gsySurfaceListener,
                            render.getEffect(),
                            render.getMVPMatrix(),
                            render);

            }
        });
        if (transform != null && transform.length == 16) {
            gsyVideoGLView.setMVPMatrix(transform);
        }
        GSYRenderView.addToParent(textureViewContainer, gsyVideoGLView);
        return gsyVideoGLView;
    }


}
