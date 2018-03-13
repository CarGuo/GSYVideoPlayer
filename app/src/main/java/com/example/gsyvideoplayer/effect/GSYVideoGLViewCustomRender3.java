package com.example.gsyvideoplayer.effect;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.example.gsyvideoplayer.R;
import com.shuyu.gsyvideoplayer.render.glrender.GSYVideoGLViewSimpleRender;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * 图片穿孔透视播放
 */
@SuppressLint("ViewConstructor")
public class GSYVideoGLViewCustomRender3 extends GSYVideoGLViewSimpleRender {

    private int mTexturesBitmap[] = new int[1];

    private BitmapEffect mBitmapEffect = new BitmapEffect();

    public GSYVideoGLViewCustomRender3() {
        super();
    }

    @Override
    protected void bindDrawFrameTexture() {
        super.bindDrawFrameTexture();

        //绑定注入bitmap
        int mFilterInputTextureUniform2 = GLES20.glGetUniformLocation(getProgram(), "sTexture2");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexturesBitmap[0]);
        GLES20.glUniform1i(mFilterInputTextureUniform2, mTexturesBitmap[0]);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        super.onSurfaceCreated(glUnused, config);

        Bitmap bitmap = BitmapFactory.decodeResource(mSurfaceView.getResources(), R.drawable.video_brightness_6_white_36dp);
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
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
    }

    @Override
    protected String getFragmentShader() {
        return mBitmapEffect.getShader(mSurfaceView);
    }


}


