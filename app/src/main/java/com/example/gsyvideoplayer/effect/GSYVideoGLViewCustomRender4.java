package com.example.gsyvideoplayer.effect;

import android.annotation.SuppressLint;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.shuyu.gsyvideoplayer.render.glrender.GSYVideoGLViewSimpleRender;

import javax.microedition.khronos.opengles.GL10;


/**
 * 铺满的双重播放
 */
@SuppressLint("ViewConstructor")
public class GSYVideoGLViewCustomRender4 extends GSYVideoGLViewSimpleRender {

    public GSYVideoGLViewCustomRender4() {
        super();
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        super.onDrawFrame(glUnused);

        float[] transform = new float[16];
        Matrix.setIdentityM(transform, 0);
        Matrix.scaleM(transform, 0, (float) mCurrentViewWidth / mSurfaceView.getWidth(),
                (float) mCurrentViewHeight / mSurfaceView.getHeight(), 1);
        GLES20.glUniformMatrix4fv(getMuMVPMatrixHandle(), 1, false, transform, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glFinish();
    }

    @Override
    public void initRenderSize() {
        Matrix.scaleM(mMVPMatrix, 0, 1f, 1f, 1);
    }
}


