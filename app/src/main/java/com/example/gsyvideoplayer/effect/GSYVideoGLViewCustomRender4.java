package com.example.gsyvideoplayer.effect;

import android.annotation.SuppressLint;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.shuyu.gsyvideoplayer.render.effect.NoEffect;
import com.shuyu.gsyvideoplayer.render.glrender.GSYVideoGLViewSimpleRender;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * 铺满的双重播放
 * 配合高斯模糊，可以实现，高斯拉伸视频铺满背景，替换黑色，前台正常比例播放
 */
@SuppressLint("ViewConstructor")
public class GSYVideoGLViewCustomRender4 extends GSYVideoGLViewSimpleRender {

    private static final int FLOAT_SIZE_BYTES = 4;

    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;

    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;

    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;

    private final float[] mTriangleVerticesData = {
            // X, Y, Z, U, V
            -1.0f, -1.0f, 0.0f,
            0.0f, 0.0f, 1.0f,
            -1.0f, 0.0f, 1.0f,
            0.0f, -1.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f,
            1.0f, 1.0f,};

    private int mProgram;

    private int muMVPMatrixHandle;

    private int muSTMatrixHandle;

    private int maPositionHandle;

    private int maTextureHandle;

    private FloatBuffer mTriangleVertices;

    public GSYVideoGLViewCustomRender4() {
        super();
        mTriangleVertices = ByteBuffer
                .allocateDirect(mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangleVertices.put(mTriangleVerticesData).position(0);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        super.onDrawFrame(glUnused);
        if (mReleased || mProgram == 0 || mSurfaceView == null
                || mSurfaceView.getWidth() <= 0 || mSurfaceView.getHeight() <= 0) {
            return;
        }

        GLES20.glUseProgram(mProgram);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, getTextureID()[0]);

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT,
                false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES,
                mTriangleVertices);
        checkGlError("glVertexAttribPointer custom maPosition");
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        checkGlError("glEnableVertexAttribArray custom maPositionHandle");

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(maTextureHandle, 3, GLES20.GL_FLOAT,
                false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES,
                mTriangleVertices);
        checkGlError("glVertexAttribPointer custom maTextureHandle");
        GLES20.glEnableVertexAttribArray(maTextureHandle);
        checkGlError("glEnableVertexAttribArray custom maTextureHandle");

        float[] transform = new float[16];
        Matrix.setIdentityM(transform, 0);
        Matrix.scaleM(transform, 0, (float) mCurrentViewWidth / mSurfaceView.getWidth(),
                (float) mCurrentViewHeight / mSurfaceView.getHeight(), 1);

        GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);

        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, transform, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("custom glDrawArrays");
    }


    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        super.onSurfaceCreated(glUnused, config);
        mProgram = createProgram(getVertexShader(), new NoEffect().getShader(mSurfaceView));
        if (mProgram == 0) {
            notifyRenderError("create foreground program failed", 0, false);
            return;
        }
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        checkGlError("glGetAttribLocation custom aPosition");
        if (maPositionHandle == -1) {
            notifyRenderError("Could not get custom attrib location for aPosition", 0, false);
            deleteForegroundProgram();
            return;
        }
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        checkGlError("glGetAttribLocation custom aTextureCoord");
        if (maTextureHandle == -1) {
            notifyRenderError("Could not get custom attrib location for aTextureCoord", 0, false);
            deleteForegroundProgram();
            return;
        }
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        checkGlError("glGetUniformLocation custom uMVPMatrix");
        if (muMVPMatrixHandle == -1) {
            notifyRenderError("Could not get custom uniform location for uMVPMatrix", 0, false);
            deleteForegroundProgram();
            return;
        }
        muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
        checkGlError("glGetUniformLocation custom uSTMatrix");
        if (muSTMatrixHandle == -1) {
            notifyRenderError("Could not get custom uniform location for uSTMatrix", 0, false);
            deleteForegroundProgram();
        }
    }

    @Override
    public void initRenderSize() {
        Matrix.setIdentityM(mMVPMatrix, 0);
    }

    @Override
    public void releaseAll() {
        super.releaseAll();
        deleteForegroundProgram();
    }

    private void deleteForegroundProgram() {
        if (mProgram != 0) {
            GLES20.glDeleteProgram(mProgram);
            mProgram = 0;
        }
    }
}
