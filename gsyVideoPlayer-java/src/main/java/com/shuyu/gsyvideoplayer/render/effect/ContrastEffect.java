package com.shuyu.gsyvideoplayer.render.effect;

import android.opengl.GLSurfaceView;

import  com.shuyu.gsyvideoplayer.render.view.GSYVideoGLView.ShaderInterface;

/**
 * Adjusts the contrast of the video.
 *
 * @author sheraz.khilji
 */
public class ContrastEffect implements ShaderInterface {
    private float contrast;

    /**
     * Initialize Effect
     *
     * @param contrast Range should be between 0.1- 2.0 with 1.0 being normal.
     */
    public ContrastEffect(float contrast) {
        if (contrast < 0.1f)
            contrast = 0.1f;
        if (contrast > 2.0f)
            contrast = 2.0f;

        this.contrast = contrast;
    }

    @Override
    public String getShader(GLSurfaceView mGlSurfaceView) {

        String shader = "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"
                + "uniform samplerExternalOES sTexture;\n"
                + " float contrast;\n" + "varying vec2 vTextureCoord;\n"
                + "void main() {\n" + "  contrast =" + contrast + ";\n"
                + "  vec4 color = texture2D(sTexture, vTextureCoord);\n"
                + "  color -= 0.5;\n" + "  color *= contrast;\n"
                + "  color += 0.5;\n" + "  gl_FragColor = color;\n" + "}\n";
        return shader;

    }

}
