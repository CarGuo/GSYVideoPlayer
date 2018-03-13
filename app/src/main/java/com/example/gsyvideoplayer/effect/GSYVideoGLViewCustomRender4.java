package com.example.gsyvideoplayer.effect;

import android.annotation.SuppressLint;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.shuyu.gsyvideoplayer.render.effect.NoEffect;
import com.shuyu.gsyvideoplayer.render.glrender.GSYVideoGLViewSimpleRender;
import com.shuyu.gsyvideoplayer.utils.Debuger;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * 铺满的双重播放
 * 配合高斯模糊，可以实现，高斯拉伸视频铺满背景，替换黑色，前台正常比例播放
 */
@SuppressLint("ViewConstructor")
public class GSYVideoGLViewCustomRender4 extends GSYVideoGLViewSimpleRender {

    private int mProgram;

    public GSYVideoGLViewCustomRender4() {
        super();
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        super.onDrawFrame(glUnused);

        GLES20.glUseProgram(mProgram);

        float[] transform = new float[16];
        Matrix.setIdentityM(transform, 0);
        Matrix.scaleM(transform, 0, (float) mCurrentViewWidth / mSurfaceView.getWidth(),
                (float) mCurrentViewHeight / mSurfaceView.getHeight(), 1);

        GLES20.glUniformMatrix4fv(getMuSTMatrixHandle(), 1, false, mSTMatrix, 0);

        GLES20.glUniformMatrix4fv(getMuMVPMatrixHandle(), 1, false, transform, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glFinish();
    }


    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        super.onSurfaceCreated(glUnused, config);
        mProgram = createProgram(getVertexShader(), new NoEffect().getShader(mSurfaceView));
    }

    @Override
    public void initRenderSize() {
        Matrix.scaleM(mMVPMatrix, 0, 1f, 1f, 1);
    }
}


