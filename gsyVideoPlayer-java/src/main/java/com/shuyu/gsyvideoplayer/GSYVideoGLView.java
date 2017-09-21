package com.shuyu.gsyvideoplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.Surface;

import com.shuyu.gsyvideoplayer.listener.GSYVideoShotListener;
import com.shuyu.gsyvideoplayer.utils.MeasureHelper;
import com.shuyu.gsyvideoplayer.effect.NoEffect;

import java.io.File;


/**
 * 在videffects的基础上调整的
 *
 * 原 @author sheraz.khilji
 */
@SuppressLint("ViewConstructor")
public class GSYVideoGLView extends GLSurfaceView {

    private static final String TAG = GSYVideoGLView.class.getName();

    private GSYVideoGLViewSimpleRender mRenderer;

    private Context mContext;

    private ShaderInterface mEffect = new NoEffect();

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
        setRenderer(mRenderer);
        mRenderer.setSurfaceView(GSYVideoGLView.this);
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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (GSYVideoManager.instance().getMediaPlayer() != null) {
            try {
                int videoWidth = GSYVideoManager.instance().getCurrentVideoWidth();
                int videoHeight = GSYVideoManager.instance().getCurrentVideoHeight();

                int videoSarNum = GSYVideoManager.instance().getMediaPlayer().getVideoSarNum();
                int videoSarDen = GSYVideoManager.instance().getMediaPlayer().getVideoSarDen();

                if (videoWidth > 0 && videoHeight > 0) {
                    measureHelper.setVideoSampleAspectRatio(videoSarNum, videoSarDen);
                    measureHelper.setVideoSize(videoWidth, videoHeight);
                }
                measureHelper.setVideoRotation((int) getRotation());
                measureHelper.doMeasure(widthMeasureSpec, heightMeasureSpec);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        setMeasuredDimension(measureHelper.getMeasuredWidth(), measureHelper.getMeasuredHeight());
    }

    public int getSizeH() {
        return measureHelper.getMeasuredHeight();
    }

    public int getSizeW() {
        return measureHelper.getMeasuredWidth();
    }

}
