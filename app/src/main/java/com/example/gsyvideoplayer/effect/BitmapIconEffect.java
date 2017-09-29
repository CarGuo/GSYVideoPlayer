package com.example.gsyvideoplayer.effect;

import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;

import com.shuyu.gsyvideoplayer.GSYVideoGLView.ShaderInterface;


/**
 * 水印效果
 */
public class BitmapIconEffect implements ShaderInterface {


    private Bitmap mBitmap;

    private int mWidth = -1;

    private int mHeight = -1;

    private float mAlpha = 1.0f;

    public BitmapIconEffect(Bitmap bitmap) {
        this(bitmap, bitmap.getWidth(), bitmap.getHeight());
    }

    public BitmapIconEffect(Bitmap bitmap, int width, int height) {
        this(bitmap, width, height, 1);
    }

    public BitmapIconEffect(Bitmap bitmap, int width, int height, float alpha) {
        this.mBitmap = bitmap;
        this.mWidth = width;
        this.mHeight = height;
        this.mAlpha = alpha;
    }

    @Override
    public String getShader(GLSurfaceView mGlSurfaceView) {
        String shader =
                "#extension GL_OES_EGL_image_external : require\n"
                        + "precision mediump float;\n"
                        + "varying vec2 vTextureCoord;\n"
                        + "uniform samplerExternalOES sTexture;\n"
                        + "uniform sampler2D sTexture2;\n"
                        + "void main() {\n"
                        + "  vec4 c1 = texture2D(sTexture2, vTextureCoord);\n"
                        + "  gl_FragColor = vec4(c1.rgb, c1.a *" + mAlpha + ");\n"
                        + "}\n";
        return shader;

    }

    public float getWidth() {
        return (float) mWidth;
    }

    public float getHeight() {
        return (float) mHeight;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }
}