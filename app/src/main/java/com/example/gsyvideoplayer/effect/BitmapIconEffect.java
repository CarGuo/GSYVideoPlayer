package com.example.gsyvideoplayer.effect;

import android.opengl.GLSurfaceView;

import com.shuyu.gsyvideoplayer.GSYVideoGLView.ShaderInterface;


/**
 * 合成两个渲染图画面
 */
public class BitmapIconEffect implements ShaderInterface {

    public BitmapIconEffect() {
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
                        + "  gl_FragColor = c1;\n"
                        + "}\n";
        return shader;

    }
}