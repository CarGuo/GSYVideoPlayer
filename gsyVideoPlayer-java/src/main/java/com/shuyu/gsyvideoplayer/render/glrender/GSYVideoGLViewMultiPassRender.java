package com.shuyu.gsyvideoplayer.render.glrender;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.view.Surface;

import com.shuyu.gsyvideoplayer.render.view.GSYVideoGLView;
import com.shuyu.gsyvideoplayer.listener.GSYVideoShotListener;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 基于 FBO 的多 pass 渲染器。
 * <p>
 * OES 外部纹理先经输入转换 pass 写入 FBO（转成普通 GL_TEXTURE_2D），
 * 随后 effect 的各 pass 在多尺寸 FBO 池取放渲染目标，支持跨分辨率的降采样
 * 金字塔；最后一个 pass 输出上屏。
 */
@SuppressLint("ViewConstructor")
public class GSYVideoGLViewMultiPassRender extends GSYVideoGLViewBaseRender {

    private static final int FLOAT_SIZE_BYTES = 4;

    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;

    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;

    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;

    protected static final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;

    private final float[] mTriangleVerticesData = {
            -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
            1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
            -1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
    };

    private static final String OES_VERTEX_SHADER =
            "uniform mat4 uSTMatrix;\n"
            + "attribute vec4 aPosition;\n"
            + "attribute vec4 aTextureCoord;\n"
            + "varying vec2 vTextureCoord;\n"
            + "void main() {\n"
            + "  gl_Position = aPosition;\n"
            + "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n"
            + "}\n";

    private static final String OES_FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n"
            + "precision mediump float;\n"
            + "varying vec2 vTextureCoord;\n"
            + "uniform samplerExternalOES sTexture;\n"
            + "void main() {\n"
            + "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n"
            + "}\n";

    private static final String PASS_VERTEX_SHADER =
            "attribute vec4 aPosition;\n"
            + "attribute vec4 aTextureCoord;\n"
            + "varying vec2 vTextureCoord;\n"
            + "void main() {\n"
            + "  gl_Position = aPosition;\n"
            + "  vTextureCoord = aTextureCoord.xy;\n"
            + "}\n";

    private final FloatBuffer mTriangleVertices;

    private int mOesTextureId;

    private SurfaceTexture mSurface;

    private Surface mPlayerSurface;

    private int mConvertProgram;

    private int mConvSTMatrixHandle;

    private int mConvPositionHandle;

    private int mConvTextureHandle;

    private int mConvTextureUniformHandle;

    private PassProgram[] mPassPrograms;

    private final List<PoolEntry> mFboPool = new ArrayList<PoolEntry>();

    private int mPoolBaseWidth;

    private int mPoolBaseHeight;

    private GSYVideoGLViewMultiPassInterface mMultiPassEffect;

    private volatile boolean mUpdateSurface;

    private volatile boolean mTakeShotPic;

    private volatile boolean mEffectChanged;

    private boolean mResourcesReady;

    public GSYVideoGLViewMultiPassRender() {
        mTriangleVertices = ByteBuffer
                .allocateDirect(mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangleVertices.put(mTriangleVerticesData).position(0);
    }

    public void setMultiPassEffect(GSYVideoGLViewMultiPassInterface effect) {
        if (mMultiPassEffect != null) {
            mMultiPassEffect.release();
        }
        mMultiPassEffect = effect;
        mEffectChanged = true;
        if (mSurfaceView != null) {
            mSurfaceView.requestRender();
        }
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

        if (!ensureResources()) {
            notifyPendingShot();
            return;
        }

        renderPasses();

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
        mResourcesReady = false;

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        mOesTextureId = textures[0];
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mOesTextureId);
        checkGlError("glBindTexture oes");
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        mConvertProgram = createProgram(OES_VERTEX_SHADER, OES_FRAGMENT_SHADER);
        if (mConvertProgram == 0) {
            notifyRenderError("create convert program failed", 0, false);
            return;
        }
        mConvPositionHandle = GLES20.glGetAttribLocation(mConvertProgram, "aPosition");
        mConvTextureHandle = GLES20.glGetAttribLocation(mConvertProgram, "aTextureCoord");
        mConvSTMatrixHandle = GLES20.glGetUniformLocation(mConvertProgram, "uSTMatrix");
        mConvTextureUniformHandle = GLES20.glGetUniformLocation(mConvertProgram, "sTexture");

        mSurface = new SurfaceTexture(mOesTextureId);
        mSurface.setOnFrameAvailableListener(this);
        mPlayerSurface = new Surface(mSurface);
        sendSurfaceForPlayer(mPlayerSurface);
    }

    @Override
    public synchronized void onFrameAvailable(SurfaceTexture surface) {
        mUpdateSurface = true;
        if (mSurfaceView != null) {
            mSurfaceView.requestRender();
        }
    }

    @Override
    public void releaseAll() {
        releaseNonGLResources();
        deletePassPrograms();
        if (mConvertProgram != 0) {
            GLES20.glDeleteProgram(mConvertProgram);
            mConvertProgram = 0;
        }
        releaseFboPool();
        if (mOesTextureId != 0) {
            GLES20.glDeleteTextures(1, new int[]{mOesTextureId}, 0);
            mOesTextureId = 0;
        }
        if (mMultiPassEffect != null) {
            mMultiPassEffect.release();
            mMultiPassEffect = null;
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

    @Override
    public void setEffect(GSYVideoGLView.ShaderInterface shaderEffect) {
    }

    @Override
    public GSYVideoGLView.ShaderInterface getEffect() {
        return null;
    }

    private boolean ensureResources() {
        if (mEffectChanged) {
            buildPassPrograms();
            mEffectChanged = false;
        }
        if (mMultiPassEffect == null || mConvertProgram == 0 || mSurface == null
                || mOesTextureId == 0 || mPassPrograms == null) {
            return false;
        }
        int width = mCurrentViewWidth;
        int height = mCurrentViewHeight;
        if (width <= 0 || height <= 0) {
            return false;
        }
        if (width != mPoolBaseWidth || height != mPoolBaseHeight) {
            releaseFboPool();
            mPoolBaseWidth = width;
            mPoolBaseHeight = height;
        }
        return true;
    }

    private void renderPasses() {
        for (PoolEntry entry : mFboPool) {
            entry.inUse = false;
        }

        GLFrameBuffer baseFbo = acquireFbo(mPoolBaseWidth, mPoolBaseHeight);
        baseFbo.bind();
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        drawConvertPass();

        GLFrameBuffer lastOutput = baseFbo;
        boolean composite = mMultiPassEffect instanceof GSYVideoGLViewCompositeInterface;
        int passCount = mMultiPassEffect.getPassCount();
        for (int i = 0; i < passCount; i++) {
            boolean finalPass = i == passCount - 1;
            GLFrameBuffer output = null;
            if (finalPass) {
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
                GLES20.glViewport(0, 0, mCurrentViewWidth, mCurrentViewHeight);
            } else {
                float scale = resolveOutputScale(i);
                int targetW = Math.max(1, Math.round(mPoolBaseWidth * scale));
                int targetH = Math.max(1, Math.round(mPoolBaseHeight * scale));
                output = acquireFbo(targetW, targetH);
                output.bind();
            }
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            drawEffectPass(mPassPrograms[i], lastOutput, i, baseFbo);
            if (!(composite && lastOutput == baseFbo)) {
                markFree(lastOutput);
            }
            if (!finalPass) {
                lastOutput = output;
            }
        }
    }

    private float resolveOutputScale(int pass) {
        if (mMultiPassEffect instanceof GSYVideoGLViewPyramidInterface) {
            float scale = ((GSYVideoGLViewPyramidInterface) mMultiPassEffect)
                    .getPassOutputScale(pass);
            if (scale > 0f) {
                return scale;
            }
        }
        return 1.0f;
    }

    private GLFrameBuffer acquireFbo(int width, int height) {
        PoolEntry free = null;
        for (PoolEntry entry : mFboPool) {
            if (!entry.inUse && entry.fbo.isReady()
                    && entry.fbo.getWidth() == width && entry.fbo.getHeight() == height) {
                free = entry;
                break;
            }
        }
        if (free == null) {
            free = new PoolEntry(new GLFrameBuffer());
            if (!free.fbo.setup(width, height)) {
                notifyRenderError("FBO setup failed: " + width + "x" + height, 0, false);
                return free.fbo;
            }
            mFboPool.add(free);
        }
        free.inUse = true;
        return free.fbo;
    }

    private void markFree(GLFrameBuffer fbo) {
        for (PoolEntry entry : mFboPool) {
            if (entry.fbo == fbo) {
                entry.inUse = false;
                return;
            }
        }
    }

    private void releaseFboPool() {
        for (PoolEntry entry : mFboPool) {
            entry.fbo.release();
        }
        mFboPool.clear();
        mPoolBaseWidth = 0;
        mPoolBaseHeight = 0;
    }

    private void drawConvertPass() {
        GLES20.glUseProgram(mConvertProgram);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mOesTextureId);
        if (mConvTextureUniformHandle != -1) {
            GLES20.glUniform1i(mConvTextureUniformHandle, 0);
        }
        GLES20.glUniformMatrix4fv(mConvSTMatrixHandle, 1, false, mSTMatrix, 0);

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(mConvPositionHandle, 3, GLES20.GL_FLOAT,
                false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        GLES20.glEnableVertexAttribArray(mConvPositionHandle);

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(mConvTextureHandle, 2, GLES20.GL_FLOAT,
                false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        GLES20.glEnableVertexAttribArray(mConvTextureHandle);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("convert draw");
    }

    private void drawEffectPass(PassProgram pass, GLFrameBuffer input, int passIndex,
                                GLFrameBuffer baseFbo) {
        GLES20.glUseProgram(pass.program);

        input.bindTexture(GLES20.GL_TEXTURE0);
        if (pass.textureUniformHandle != -1) {
            GLES20.glUniform1i(pass.textureUniformHandle, 0);
        }
        if (pass.baseSceneUniformHandle != -1 && baseFbo != null) {
            baseFbo.bindTexture(GLES20.GL_TEXTURE1);
            GLES20.glUniform1i(pass.baseSceneUniformHandle, 1);
        }
        if (pass.texelSizeHandle != -1) {
            float tw = input.getWidth() > 0 ? 1.0f / input.getWidth() : 0f;
            float th = input.getHeight() > 0 ? 1.0f / input.getHeight() : 0f;
            GLES20.glUniform2f(pass.texelSizeHandle, tw, th);
        }
        mMultiPassEffect.onBindPassUniform(pass.program, passIndex,
                input.getWidth(), input.getHeight());

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(pass.positionHandle, 3, GLES20.GL_FLOAT,
                false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        GLES20.glEnableVertexAttribArray(pass.positionHandle);

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(pass.textureCoordHandle, 2, GLES20.GL_FLOAT,
                false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        GLES20.glEnableVertexAttribArray(pass.textureCoordHandle);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("pass " + passIndex + " draw");
    }

    private void buildPassPrograms() {
        deletePassPrograms();
        if (mMultiPassEffect == null) {
            return;
        }
        int count = mMultiPassEffect.getPassCount();
        if (count <= 0) {
            return;
        }
        mPassPrograms = new PassProgram[count];
        for (int i = 0; i < count; i++) {
            int program = createProgram(PASS_VERTEX_SHADER, mMultiPassEffect.getPassShader(i));
            if (program == 0) {
                notifyRenderError("create pass program failed: " + i, 0, false);
                deletePassPrograms();
                return;
            }
            PassProgram pass = new PassProgram();
            pass.program = program;
            pass.positionHandle = GLES20.glGetAttribLocation(program, "aPosition");
            pass.textureCoordHandle = GLES20.glGetAttribLocation(program, "aTextureCoord");
            pass.textureUniformHandle = GLES20.glGetUniformLocation(program, "sTexture");
            pass.texelSizeHandle = GLES20.glGetUniformLocation(program, "uTexelSize");
            if (mMultiPassEffect instanceof GSYVideoGLViewCompositeInterface) {
                String baseSampler = ((GSYVideoGLViewCompositeInterface) mMultiPassEffect)
                        .getBaseSceneSampler(i);
                if (baseSampler != null) {
                    pass.baseSceneUniformHandle = GLES20.glGetUniformLocation(
                            program, baseSampler);
                }
            }
            mPassPrograms[i] = pass;
        }
    }

    private void deletePassPrograms() {
        if (mPassPrograms != null) {
            for (PassProgram pass : mPassPrograms) {
                if (pass != null && pass.program != 0) {
                    GLES20.glDeleteProgram(pass.program);
                }
            }
            mPassPrograms = null;
        }
    }

    private void takeBitmap(GL10 glUnused) {
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

    public void takeShotPic() {
        mTakeShotPic = true;
    }

    @Override
    public void setGSYVideoShotListener(GSYVideoShotListener listener, boolean high) {
        super.setGSYVideoShotListener(listener, high);
    }

    private static class PassProgram {
        int program;
        int positionHandle;
        int textureCoordHandle;
        int textureUniformHandle;
        int texelSizeHandle;
        int baseSceneUniformHandle = -1;
    }

    private static class PoolEntry {
        final GLFrameBuffer fbo;

        boolean inUse;

        PoolEntry(GLFrameBuffer fbo) {
            this.fbo = fbo;
        }
    }
}
