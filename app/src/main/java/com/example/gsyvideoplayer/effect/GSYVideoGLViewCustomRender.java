package com.example.gsyvideoplayer.effect;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.shuyu.gsyvideoplayer.render.glrender.GSYVideoGLViewSimpleRender;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * 自定义渲染功能，继承原本的提供视频渲染
 * 自定义实现水印
 */
@SuppressLint("ViewConstructor")
public class GSYVideoGLViewCustomRender extends GSYVideoGLViewSimpleRender {

    private static final int FLOAT_SIZE_BYTES = 4;

    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;

    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;

    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;

    private final float[] mTriangleVerticesData = {
            // X, Y, Z, U, V
            -1.0f, -1.0f, 0.0f, 0.0f,
            0.0f, 1.0f, -1.0f, 0.0f,
            1.0f, 0.0f, -1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,};

    private float[] mMVPMatrix = new float[16];

    private float[] mSTMatrix = new float[16];

    private int muMVPMatrixHandle;

    private int muSTMatrixHandle;

    private int maPositionHandle;

    private int maTextureHandle;

    private int curProgram;

    private int mTexturesBitmap[] = new int[1];

    private FloatBuffer mTriangleVertices;

    //水印圖
    private BitmapIconEffect mBitmapEffect;

    public GSYVideoGLViewCustomRender() {
        super();
        init();
    }

    public GSYVideoGLViewCustomRender(Bitmap bitmap) {
        this(bitmap, bitmap.getWidth(), bitmap.getHeight());

    }

    public GSYVideoGLViewCustomRender(Bitmap bitmap, int width, int height) {
        this(bitmap, width, height, 1.0f);
    }

    public GSYVideoGLViewCustomRender(Bitmap bitmap, int width, int height, float alpha) {
        super();
        init();

        mBitmapEffect = new BitmapIconEffect(bitmap, width, height, alpha);
    }

    private void init() {
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
        super.onDrawFrame(glUnused);

        //curProgram = createProgram(getVertexShader(), mBitmapEffect.getShader(mSurfaceView));

        GLES20.glUseProgram(curProgram);
        checkGlError("glUseProgram");

        //绑定注入bitmap
        int mFilterInputTextureUniform2 = GLES20.glGetUniformLocation(curProgram, "sTexture2");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexturesBitmap[0]);
        GLES20.glUniform1i(mFilterInputTextureUniform2, mTexturesBitmap[0]);

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

        GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);


        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        //水印透明
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("glDrawArrays");

        GLES20.glFinish();
        GLES20.glDisable(GLES20.GL_BLEND);
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        super.onSurfaceChanged(glUnused, width, height);
        //旋转到正常角度
        Matrix.setRotateM(mMVPMatrix, 0, 180f, 0.0f, 0, 1.0f);
        //调整大小比例
        Matrix.scaleM(mMVPMatrix, 0, mBitmapEffect.getScaleW(), mBitmapEffect.getScaleH(), 1);
        //调整位置
        Matrix.translateM(mMVPMatrix, 0, mBitmapEffect.getPositionX(), mBitmapEffect.getPositionY(), 0f);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        super.onSurfaceCreated(glUnused, config);

        curProgram = createProgram(getVertexShader(), mBitmapEffect.getShader(mSurfaceView));

        if (curProgram == 0) {
            return;
        }
        maPositionHandle = GLES20
                .glGetAttribLocation(curProgram, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException(
                    "Could not get attrib location for aPosition");
        }
        maTextureHandle = GLES20.glGetAttribLocation(curProgram,
                "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureHandle == -1) {
            throw new RuntimeException(
                    "Could not get attrib location for aTextureCoord");
        }

        muMVPMatrixHandle = GLES20.glGetUniformLocation(curProgram,
                "uMVPMatrix");
        checkGlError("glGetUniformLocation uMVPMatrix");
        if (muMVPMatrixHandle == -1) {
            throw new RuntimeException(
                    "Could not get attrib location for uMVPMatrix");
        }

        muSTMatrixHandle = GLES20.glGetUniformLocation(curProgram,
                "uSTMatrix");
        checkGlError("glGetUniformLocation uSTMatrix");
        if (muSTMatrixHandle == -1) {
            throw new RuntimeException(
                    "Could not get attrib location for uSTMatrix");
        }

        //创建bitmap
        GLES20.glGenTextures(1, mTexturesBitmap, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexturesBitmap[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmapEffect.getBitmap(), 0);
    }


    @Override
    public void releaseAll() {
        super.releaseAll();
        Bitmap bitmap = mBitmapEffect.getBitmap();
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    public float[] getMVPMatrix() {
        return mMVPMatrix;
    }

    public void setCurrentMVPMatrix(float[] mMVPMatrix) {
        this.mMVPMatrix = mMVPMatrix;
    }

    public void setBitmapEffect(BitmapIconEffect bitmapEffect) {
        this.mBitmapEffect = bitmapEffect;
    }
}


