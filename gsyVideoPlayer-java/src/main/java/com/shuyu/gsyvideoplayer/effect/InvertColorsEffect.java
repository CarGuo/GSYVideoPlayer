package com.shuyu.gsyvideoplayer.effect;

import android.opengl.GLSurfaceView;

import  com.shuyu.gsyvideoplayer.GSYVideoGLView.ShaderInterface;


/**
 * Inverts the video colors. This can also be known as negative Effect.
 *
 * @author sheraz.khilji
 */
public class InvertColorsEffect implements ShaderInterface {
    /**
     * Initialize Effect
     */
    public InvertColorsEffect() {
    }

    @Override
    public String getShader(GLSurfaceView mGlSurfaceView) {

        String shader = "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"
                + "varying vec2 vTextureCoord;\n"
                + "uniform samplerExternalOES sTexture;\n" + "void main() {\n"
                + "  vec4 color = texture2D(sTexture, vTextureCoord);\n"
                + "  float colorR = (1.0 - color.r) / 1.0;\n"
                + "  float colorG = (1.0 - color.g) / 1.0;\n"
                + "  float colorB = (1.0 - color.b) / 1.0;\n"
                + "  gl_FragColor = vec4(colorR, colorG, colorB, color.a);\n"
                + "}\n";

        return shader;

    }
}
