package com.shuyu.gsyvideoplayer.render.glrender;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.Surface;

import com.shuyu.gsyvideoplayer.render.view.GSYVideoGLView;
import com.shuyu.gsyvideoplayer.render.effect.NoEffect;
import com.shuyu.gsyvideoplayer.listener.GSYVideoShotListener;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * 在videffects的基础上调整的
 * <p>
 * 原 @author sheraz.khilji
 */
@SuppressLint("ViewConstructor")
public class GSYVideoGLViewSimpleRender extends GSYVideoGLViewBaseRender {

    private static final int FLOAT_SIZE_BYTES = 4;

    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;

    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;

    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;

    protected static final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;

    private final float[] mTriangleVerticesData = {
            // X, Y, Z, U, V
            -1.0f, -1.0f, 0.0f,
            0.0f, 0.0f, 1.0f,
            -1.0f, 0.0f, 1.0f,
            0.0f, -1.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f,
            1.0f, 1.0f,};

    private final String mVertexShader = "uniform mat4 uMVPMatrix;\n"
            + "uniform mat4 uSTMatrix;\n"
            + "attribute vec4 aPosition;\n"
            + "attribute vec4 aTextureCoord;\n"
            + "varying vec2 vTextureCoord;\n"
            + "void main() {\n"
            + "  gl_Position = uMVPMatrix * aPosition;\n"
            + "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n"
            + "}\n";

    private int mProgram;

    private int mTextureID[] = new int[2];

    private int muMVPMatrixHandle;

    private int muSTMatrixHandle;

    private int maPositionHandle;

    private int maTextureHandle;

    private int muViewSizeHandle = -1;

    private volatile boolean mUpdateSurface = false;

    private volatile boolean mTakeShotPic = false;

    private FloatBuffer mTriangleVertices;

    private SurfaceTexture mSurface;

    private Surface mPlayerSurface;

    private GSYVideoGLView.ShaderInterface mEffect = new NoEffect();

    private GSYVideoGLView.TextureShaderInterface mReadyTextureEffect;

    public GSYVideoGLViewSimpleRender() {
        mTriangleVertices = ByteBuffer
                .allocateDirect(
                        mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangleVertices.put(mTriangleVerticesData).position(0);

        Matrix.setIdentityM(mSTMatrix, 0);
        Matrix.setIdentityM(mMVPMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        if (mReleased) {
            notifyPendingShot();
            return;
        }
        synchronized (this) {
            if (mUpdateSurface) {
                try {
                    if (mSurface != null) {
                        mSurface.updateTexImage();
                        mSurface.getTransformMatrix(mSTMatrix);
                    }
                } catch (RuntimeException e) {
                    notifyRenderError("updateTexImage error: " + e.getMessage(), 0, false);
                }
                mUpdateSurface = false;
            }
        }
        if (!initDrawFrame()) {
            notifyPendingShot();
            return;
        }

        bindDrawFrameTexture();

        initPointerAndDraw();

        takeBitmap(glUnused);

    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        mCurrentViewWidth = width;
        mCurrentViewHeight = height;
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        mReleased = false;
        mReadyTextureEffect = null;

        mProgram = createProgram(getVertexShader(), getFragmentShader());
        if (mProgram == 0) {
            notifyRenderError("create GL program failed", 0, false);
            return;
        }
        if (!initProgramHandles(mProgram, false)) {
            deleteProgram();
            return;
        }

        GLES20.glGenTextures(2, mTextureID, 0);

        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID[0]);
        checkGlError("glBindTexture mTextureID");

        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        mSurface = new SurfaceTexture(mTextureID[0]);
        mSurface.setOnFrameAvailableListener(this);

        mPlayerSurface = new Surface(mSurface);

        sendSurfaceForPlayer(mPlayerSurface);
    }

    @Override
    synchronized public void onFrameAvailable(SurfaceTexture surface) {
        mUpdateSurface = true;
        if (mSurfaceView != null) {
            mSurfaceView.requestRender();
        }
    }

    @Override
    public void releaseAll() {
        releaseNonGLResources();
        if (mTextureID[0] != 0 || mTextureID[1] != 0) {
            GLES20.glDeleteTextures(mTextureID.length, mTextureID, 0);
            mTextureID[0] = 0;
            mTextureID[1] = 0;
        }
        releaseReadyTextureEffect();
        if (mProgram != 0) {
            deleteProgram();
        }
    }

    @Override
    public void releaseNonGLResources() {
        mReleased = true;
        notifyPendingShot();
        if (mPlayerSurface != null) {
            mPlayerSurface.release();
            mPlayerSurface = null;
        }
        if (mSurface != null) {
            mSurface.setOnFrameAvailableListener(null);
            mSurface.release();
            mSurface = null;
        }
    }

    /**
     * 设置滤镜效果
     *
     * @param shaderEffect
     */
    @Override
    public void setEffect(GSYVideoGLView.ShaderInterface shaderEffect) {
        if (shaderEffect != null) {
            mEffect = shaderEffect;
        }
        mChangeProgram = true;
        mChangeProgramSupportError = true;
    }

    @Override
    public GSYVideoGLView.ShaderInterface getEffect() {
        return mEffect;
    }

    protected boolean initDrawFrame() {
        if (mChangeProgram) {
            int program = createProgram(getVertexShader(), getFragmentShader());
            if (program != 0) {
                if (initProgramHandles(program, mChangeProgramSupportError)) {
                    if (mProgram != 0) {
                        GLES20.glDeleteProgram(mProgram);
                    }
                    mProgram = program;
                } else {
                    GLES20.glDeleteProgram(program);
                }
            } else {
                notifyRenderError("change GL program failed", 0, mChangeProgramSupportError);
            }
            mChangeProgram = false;
            mChangeProgramSupportError = false;
        }
        if (mProgram == 0 || mSurface == null || mTextureID[0] == 0) {
            return false;
        }
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT
                | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);
        checkGlError("glUseProgram");
        ensureTextureEffectReady();
        return true;
    }

    /**
     * 保证当前效果自带的纹理资产已在 GL 线程上传；切换到其它效果时先释放旧纹理。
     */
    protected void ensureTextureEffectReady() {
        GSYVideoGLView.TextureShaderInterface current =
                mEffect instanceof GSYVideoGLView.TextureShaderInterface
                        ? (GSYVideoGLView.TextureShaderInterface) mEffect : null;
        if (mReadyTextureEffect == current) {
            return;
        }
        if (mReadyTextureEffect != null) {
            mReadyTextureEffect.onSurfaceRelease(mSurfaceView);
            mReadyTextureEffect = null;
        }
        if (current != null) {
            current.onSurfaceReady(mSurfaceView);
            mReadyTextureEffect = current;
        }
    }

    /**
     * 释放当前已上传的纹理资产（GL 线程内调用）。
     */
    protected void releaseReadyTextureEffect() {
        if (mReadyTextureEffect != null) {
            mReadyTextureEffect.onSurfaceRelease(mSurfaceView);
            mReadyTextureEffect = null;
        }
    }


    protected void bindDrawFrameTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID[0]);
        if (mReadyTextureEffect != null) {
            mReadyTextureEffect.onBindTextures(mSurfaceView, mProgram);
        }
    }


    protected void takeBitmap(GL10 glUnused) {
        if (mTakeShotPic) {
            mTakeShotPic = false;
            if (mGSYVideoShotListener != null) {
                int width = mSurfaceView != null ? mSurfaceView.getWidth() : 0;
                int height = mSurfaceView != null ? mSurfaceView.getHeight() : 0;
                Bitmap bitmap = null;
                if (width > 0 && height > 0) {
                    try {
                        bitmap = createBitmapFromGLSurface(0, 0, width, height, glUnused);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                notifyShotBitmap(bitmap);
            }
        }
    }

    private void notifyPendingShot() {
        if (mTakeShotPic) {
            mTakeShotPic = false;
            notifyShotBitmap(null);
        }
    }

    private void deleteProgram() {
        if (mProgram != 0) {
            GLES20.glDeleteProgram(mProgram);
            mProgram = 0;
        }
    }


    protected void initPointerAndDraw() {
        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT,
                false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES,
                mTriangleVertices);
        checkGlError("glVertexAttribPointer maPosition");
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        checkGlError("glEnableVertexAttribArray maPositionHandle");

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(maTextureHandle, 3, GLES20.GL_FLOAT,
                false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES,
                mTriangleVertices);
        checkGlError("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(maTextureHandle);
        checkGlError("glEnableVertexAttribArray maTextureHandle");

        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix,
                0);
        GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);
        if (muViewSizeHandle != -1) {
            GLES20.glUniform2f(muViewSizeHandle, mCurrentViewWidth, mCurrentViewHeight);
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("glDrawArrays");

    }

    private boolean initProgramHandles(int program, boolean byChangedRenderError) {
        int positionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (positionHandle == -1) {
            notifyRenderError("Could not get attrib location for aPosition", 0, byChangedRenderError);
            return false;
        }
        int textureHandle = GLES20.glGetAttribLocation(program, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (textureHandle == -1) {
            notifyRenderError("Could not get attrib location for aTextureCoord", 0, byChangedRenderError);
            return false;
        }

        int mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        checkGlError("glGetUniformLocation uMVPMatrix");
        if (mvpMatrixHandle == -1) {
            notifyRenderError("Could not get uniform location for uMVPMatrix", 0, byChangedRenderError);
            return false;
        }

        int stMatrixHandle = GLES20.glGetUniformLocation(program, "uSTMatrix");
        checkGlError("glGetUniformLocation uSTMatrix");
        if (stMatrixHandle == -1) {
            notifyRenderError("Could not get uniform location for uSTMatrix", 0, byChangedRenderError);
            return false;
        }

        int viewSizeHandle = GLES20.glGetUniformLocation(program, "uViewSize");

        maPositionHandle = positionHandle;
        maTextureHandle = textureHandle;
        muMVPMatrixHandle = mvpMatrixHandle;
        muSTMatrixHandle = stMatrixHandle;
        muViewSizeHandle = viewSizeHandle;
        return true;
    }

    public int getProgram() {
        return mProgram;
    }

    public int getMuMVPMatrixHandle() {
        return muMVPMatrixHandle;
    }

    public int getMuSTMatrixHandle() {
        return muSTMatrixHandle;
    }

    public int getMaPositionHandle() {
        return maPositionHandle;
    }

    public int getMaTextureHandle() {
        return maTextureHandle;
    }

    public float[] getSTMatrix() {
        return mSTMatrix;
    }

    public int[] getTextureID() {
        return mTextureID;
    }

    protected String getVertexShader() {
        return mVertexShader;
    }

    protected String getFragmentShader() {
        return mEffect.getShader(mSurfaceView);
    }

    /**
     * 打开截图
     */
    public void takeShotPic() {
        mTakeShotPic = true;
    }

    /**
     * 截图监听
     */
    public void setGSYVideoShotListener(GSYVideoShotListener listener, boolean high) {
        super.setGSYVideoShotListener(listener, high);
    }
}
