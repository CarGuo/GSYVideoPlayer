package com.shuyu.gsyvideoplayer.render.effect;

import android.opengl.GLSurfaceView;

import  com.shuyu.gsyvideoplayer.render.view.GSYVideoGLView.ShaderInterface;

/**
 * Apply Gamma Effect on Video being played
 */
public class GammaEffect implements ShaderInterface {
    private float gammaValue;

    /**
     * Initialize Effect
     *
     * @param gammaValue Range should be between 0.0 - 2.0 with 1.0 being normal.
     */
    public GammaEffect(float gammaValue) {
        if (gammaValue < 0.0f)
            gammaValue = 0.0f;
        if (gammaValue > 2.0f)
            gammaValue = 2.0f;
        this.gammaValue = gammaValue;

    }

    @Override
    public String getShader(GLSurfaceView mGlSurfaceView) {

        String shader = "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"

                + "varying vec2 vTextureCoord;\n"
                + "uniform samplerExternalOES sTexture;\n"
                + "float gamma=" + gammaValue + ";\n"

                + "void main() {\n"

                + "vec4 textureColor = texture2D(sTexture, vTextureCoord);\n"
                + "gl_FragColor = vec4(pow(textureColor.rgb, vec3(gamma)), textureColor.w);\n"

                + "}\n";

        return shader;
    }
}