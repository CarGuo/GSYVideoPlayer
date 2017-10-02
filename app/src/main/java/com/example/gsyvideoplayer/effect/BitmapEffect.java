package com.example.gsyvideoplayer.effect;

import android.opengl.GLSurfaceView;

import com.shuyu.gsyvideoplayer.GSYVideoGLView.ShaderInterface;


/**
 * 合成两个渲染图画面
 */
public class BitmapEffect implements ShaderInterface {
    public BitmapEffect() {
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
                        + "  vec4 c2 = texture2D(sTexture, vTextureCoord);\n"
                        + "  vec4 c1 = texture2D(sTexture2, vTextureCoord);\n"
                        + "  vec4 whiteColor = vec4(1.0);\n"
                        + "  gl_FragColor = whiteColor - (whiteColor - c2) / c1;\n"
                        + "}\n";
        return shader;

    }
}