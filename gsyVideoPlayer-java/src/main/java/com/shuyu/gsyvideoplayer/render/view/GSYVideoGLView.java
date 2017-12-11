package com.shuyu.gsyvideoplayer.render.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.Surface;

import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.listener.GSYVideoShotListener;
import com.shuyu.gsyvideoplayer.render.glrender.GSYVideoGLViewBaseRender;
import com.shuyu.gsyvideoplayer.render.glrender.GSYVideoGLViewSimpleRender;
import com.shuyu.gsyvideoplayer.utils.MeasureHelper;
import com.shuyu.gsyvideoplayer.render.effect.NoEffect;


/**
 * 在videffects的基础上调整的
 * <p>
 * 原 @author sheraz.khilji
 */
@SuppressLint("ViewConstructor")
public class GSYVideoGLView extends GLSurfaceView {

    private static final String TAG = GSYVideoGLView.class.getName();

    private GSYVideoGLViewBaseRender mRenderer;

    private Context mContext;

    private ShaderInterface mEffect = new NoEffect();

    private float[] mMVPMatrix;

    private MeasureHelper measureHelper;

    private onGSYSurfaceListener mGSYSurfaceListener;

    public interface onGSYSurfaceListener {
        void onSurfaceAvailable(Surface surface);
    }

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

    public void initRender() {
        setRenderer(mRenderer);
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

    public void setGSYSurfaceListener(onGSYSurfaceListener mGSYSurfaceListener) {
        this.mGSYSurfaceListener = mGSYSurfaceListener;
        mRenderer.setGSYSurfaceListener(this.mGSYSurfaceListener);
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

    @Override
    public void onResume() {
        super.onResume();
        if(mRenderer != null) {
            mRenderer.initRenderSize();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureHelper.prepareMeasure(widthMeasureSpec, heightMeasureSpec, (int) getRotation());
        initRenderMeasure();
    }

    protected void initRenderMeasure() {
        if (GSYVideoManager.instance().getMediaPlayer() != null) {
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

    public int getSizeH() {
        return measureHelper.getMeasuredHeight();
    }

    public int getSizeW() {
        return measureHelper.getMeasuredWidth();
    }

    public void releaseAll() {
        if (mRenderer != null) {
            mRenderer.releaseAll();
        }
    }
}
