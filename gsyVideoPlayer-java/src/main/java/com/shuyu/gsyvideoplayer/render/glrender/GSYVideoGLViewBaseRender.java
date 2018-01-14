package com.shuyu.gsyvideoplayer.render.glrender;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLException;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.view.Surface;

import com.shuyu.gsyvideoplayer.render.view.GSYVideoGLView;
import com.shuyu.gsyvideoplayer.listener.GSYVideoShotListener;
import com.shuyu.gsyvideoplayer.utils.Debuger;

import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;


/**
 *
 */
@SuppressLint("ViewConstructor")
public abstract class GSYVideoGLViewBaseRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    //是否需要高清截图
    protected boolean mHighShot = false;

    protected GSYVideoGLView.onGSYSurfaceListener mGSYSurfaceListener;

    protected GLSurfaceView mSurfaceView;

    protected float[] mMVPMatrix = new float[16];

    protected float[] mSTMatrix = new float[16];

    protected int mCurrentViewWidth = 0;

    protected int mCurrentViewHeight = 0;

    protected int mCurrentVideoWidth = 0;

    protected int mCurrentVideoHeight = 0;


    public abstract void releaseAll();

    public void setSurfaceView(GLSurfaceView surfaceView) {
        this.mSurfaceView = surfaceView;
    }

    public void sendSurfaceForPlayer(Surface surface) {
        if (mGSYSurfaceListener != null) {
            mGSYSurfaceListener.onSurfaceAvailable(surface);
        }
    }

    protected int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS,
                    compiled, 0);
            if (compiled[0] == 0) {
                Debuger.printfError("Could not compile shader " + shaderType + ":");
                Debuger.printfError(GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    protected int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS,
                    linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Debuger.printfError("Could not link program: ");
                Debuger.printfError(GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    protected void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Debuger.printfError(op + ": glError " + error);
            //throw new RuntimeException(op + ": glError " + error);
        }
    }

    /**
     * 创建bitmap截图
     */
    protected Bitmap createBitmapFromGLSurface(int x, int y, int w, int h, GL10 gl) {
        int bitmapBuffer[] = new int[w * h];
        int bitmapSource[] = new int[w * h];
        IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);
        try {
            gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.
                            GL_UNSIGNED_BYTE,
                    intBuffer);
            int offset1, offset2;
            for (int i = 0; i < h; i++) {
                offset1 = i * w;
                offset2 = (h - i - 1) * w;
                for (int j = 0; j < w; j++) {
                    int texturePixel = bitmapBuffer[offset1 + j];
                    int blue = (texturePixel >> 16) & 0xff;
                    int red = (texturePixel << 16) & 0x00ff0000;
                    int pixel = (texturePixel & 0xff00ff00) | red | blue;
                    bitmapSource[offset2 + j] = pixel;
                }
            }
        } catch (GLException e) {
            return null;
        }
        if (mHighShot) {
            return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
        } else {
            return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.RGB_565);
        }
    }


    public void setGSYSurfaceListener(GSYVideoGLView.onGSYSurfaceListener onSurfaceListener) {
        this.mGSYSurfaceListener = onSurfaceListener;
    }

    public float[] getMVPMatrix() {
        return mMVPMatrix;
    }

    /**
     * 形变动画
     */
    public void setMVPMatrix(float[] MVPMatrix) {
        this.mMVPMatrix = MVPMatrix;
    }

    /**
     * 打开截图
     */
    public void takeShotPic() {
    }

    /**
     * 截图监听
     */
    public void setGSYVideoShotListener(GSYVideoShotListener listener, boolean high) {
    }

    /**
     * 设置滤镜效果
     *
     * @param shaderEffect
     */
    public void setEffect(GSYVideoGLView.ShaderInterface shaderEffect) {
    }

    public int getCurrentViewWidth() {
        return mCurrentViewWidth;
    }

    public void setCurrentViewWidth(int currentViewWidth) {
        this.mCurrentViewWidth = currentViewWidth;
    }

    public int getCurrentViewHeight() {
        return mCurrentViewHeight;
    }

    public void setCurrentViewHeight(int currentViewHeight) {
        this.mCurrentViewHeight = currentViewHeight;
    }

    public int getCurrentVideoWidth() {
        return mCurrentVideoWidth;
    }

    public void setCurrentVideoWidth(int currentVideoWidth) {
        this.mCurrentVideoWidth = currentVideoWidth;
    }

    public int getCurrentVideoHeight() {
        return mCurrentVideoHeight;
    }

    public void setCurrentVideoHeight(int currentVideoHeight) {
        this.mCurrentVideoHeight = currentVideoHeight;
    }

    public void initRenderSize() {
        if (mCurrentViewWidth != 0 && mCurrentViewHeight != 0) {
            Matrix.scaleM(mMVPMatrix, 0, (float) mCurrentViewWidth / mSurfaceView.getWidth(),
                    (float) mCurrentViewHeight / mSurfaceView.getHeight(), 1);
        }
    }
}


