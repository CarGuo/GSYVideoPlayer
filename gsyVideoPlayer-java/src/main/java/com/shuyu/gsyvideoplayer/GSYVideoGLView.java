package com.shuyu.gsyvideoplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;

import com.shuyu.gsyvideoplayer.utils.MeasureHelper;
import com.shuyu.gsyvideoplayer.effect.NoEffect;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * 在videffects的基础上调整的
 *
 * 原 @author sheraz.khilji
 */
@SuppressLint("ViewConstructor")
public class GSYVideoGLView extends GLSurfaceView {

    private static final String TAG = GSYVideoGLView.class.getName();

    private VideoRender mRenderer;

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
        mRenderer = new VideoRender();
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

    private static class VideoRender implements Renderer, SurfaceTexture.OnFrameAvailableListener {
        private static String TAG = VideoRender.class.getName();

        private static final int FLOAT_SIZE_BYTES = 4;
        private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
        private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
        private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
        private static final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;

        private final float[] mTriangleVerticesData = {
                // X, Y, Z, U, V
                -1.0f, -1.0f, 0, 0.f, 0.f, 1.0f, -1.0f, 0, 1.f, 0.f, -1.0f,
                1.0f, 0, 0.f, 1.f, 1.0f, 1.0f, 0, 1.f, 1.f,};

        private FloatBuffer mTriangleVertices;

        private final String mVertexShader = "uniform mat4 uMVPMatrix;\n"
                + "uniform mat4 uSTMatrix;\n"
                + "attribute vec4 aPosition;\n"
                + "attribute vec4 aTextureCoord;\n"
                + "varying vec2 vTextureCoord;\n"
                + "void main() {\n"
                + "  gl_Position = uMVPMatrix * aPosition;\n"
                + "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n"
                + "}\n";
        private float[] mMVPMatrix = new float[16];
        private float[] mSTMatrix = new float[16];

        private int mProgram;
        private int mTextureID[] = new int[2];
        private int muMVPMatrixHandle;
        private int muSTMatrixHandle;
        private int maPositionHandle;
        private int maTextureHandle;
        private boolean updateSurface = false;

        private SurfaceTexture mSurface;

        private onGSYSurfaceListener mGSYSurfaceListener;

        private ShaderInterface mEffect = new NoEffect();

        private GLSurfaceView mSurfaceView;


        public void setEffect(ShaderInterface shaderEffect) {
            if (shaderEffect != null)
                mEffect = shaderEffect;
        }

        public VideoRender() {
            mTriangleVertices = ByteBuffer
                    .allocateDirect(
                            mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            mTriangleVertices.put(mTriangleVerticesData).position(0);

            Matrix.setIdentityM(mSTMatrix, 0);
        }

        public void setGSYSurfaceListener(onGSYSurfaceListener onSurfaceListener) {
            this.mGSYSurfaceListener = onSurfaceListener;
        }

        public void setSurfaceView(GLSurfaceView surfaceView) {
            this.mSurfaceView = surfaceView;
        }

        @Override
        public void onDrawFrame(GL10 glUnused) {
            synchronized (this) {
                if (updateSurface) {
                    mSurface.updateTexImage();
                    mSurface.getTransformMatrix(mSTMatrix);
                    updateSurface = false;
                }
            }
            mProgram = createProgram(mVertexShader, mEffect.getShader(mSurfaceView));
            GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT
                    | GLES20.GL_COLOR_BUFFER_BIT);

            GLES20.glUseProgram(mProgram);
            checkGlError("glUseProgram");

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID[0]);

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

            Matrix.setIdentityM(mMVPMatrix, 0);
            GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix,
                    0);
            GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            checkGlError("glDrawArrays");
            GLES20.glFinish();

        }

        @Override
        public void onSurfaceChanged(GL10 glUnused, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
        }

        @Override
        public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {

            mProgram = createProgram(mVertexShader, mEffect.getShader(mSurfaceView));
            if (mProgram == 0) {
                return;
            }
            maPositionHandle = GLES20
                    .glGetAttribLocation(mProgram, "aPosition");
            checkGlError("glGetAttribLocation aPosition");
            if (maPositionHandle == -1) {
                throw new RuntimeException(
                        "Could not get attrib location for aPosition");
            }
            maTextureHandle = GLES20.glGetAttribLocation(mProgram,
                    "aTextureCoord");
            checkGlError("glGetAttribLocation aTextureCoord");
            if (maTextureHandle == -1) {
                throw new RuntimeException(
                        "Could not get attrib location for aTextureCoord");
            }

            muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram,
                    "uMVPMatrix");
            checkGlError("glGetUniformLocation uMVPMatrix");
            if (muMVPMatrixHandle == -1) {
                throw new RuntimeException(
                        "Could not get attrib location for uMVPMatrix");
            }

            muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram,
                    "uSTMatrix");
            checkGlError("glGetUniformLocation uSTMatrix");
            if (muSTMatrixHandle == -1) {
                throw new RuntimeException(
                        "Could not get attrib location for uSTMatrix");
            }

            // int[] textures = new int[1];
            GLES20.glGenTextures(2, mTextureID, 0);
            // GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID[0]);

            // mTextureID = textures[0];
            GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID[0]);
            checkGlError("glBindTexture mTextureID");

            // GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
            // GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            // GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
            // GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

			/*
             * Create the SurfaceTexture that will feed this textureID, and pass
			 * it to the MediaPlayer
			 */
            mSurface = new SurfaceTexture(mTextureID[0]);
            mSurface.setOnFrameAvailableListener(this);

            Surface surface = new Surface(mSurface);
            if (mGSYSurfaceListener != null) {
                mGSYSurfaceListener.onSurfaceAvailable(surface);
            }
            //surface.release();
        }

        @Override
        synchronized public void onFrameAvailable(SurfaceTexture surface) {
            updateSurface = true;
        }

        private int loadShader(int shaderType, String source) {
            int shader = GLES20.glCreateShader(shaderType);
            if (shader != 0) {
                GLES20.glShaderSource(shader, source);
                GLES20.glCompileShader(shader);
                int[] compiled = new int[1];
                GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS,
                        compiled, 0);
                if (compiled[0] == 0) {
                    Log.e(TAG, "Could not compile shader " + shaderType + ":");
                    Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                    GLES20.glDeleteShader(shader);
                    shader = 0;
                }
            }
            return shader;
        }

        private int createProgram(String vertexSource, String fragmentSource) {
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
                    Log.e(TAG, "Could not link program: ");
                    Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                    GLES20.glDeleteProgram(program);
                    program = 0;
                }
            }
            return program;
        }

        private void checkGlError(String op) {
            int error;
            while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
                Log.e(TAG, op + ": glError " + error);
                throw new RuntimeException(op + ": glError " + error);
            }
        }

    }

}
