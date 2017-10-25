package com.shuyu.gsyvideoplayer.render.effect;

import android.opengl.GLSurfaceView;

import  com.shuyu.gsyvideoplayer.render.view.GSYVideoGLView.ShaderInterface;


/**
 * Applies back-light filling to the video.
 *
 * @author sheraz.khilji
 */
public class FillLightEffect implements ShaderInterface {
    private float strength = 0f;

    /**
     * Initialize Effect
     *
     * @param strength Float, between 0 and 1. 0 means no change.
     */
    public FillLightEffect(float strength) {
        if (strength < 0.0f)
            strength = 0f;
        if (strength > 1.0f)
            strength = 1f;

        this.strength = strength;
    }

    @Override
    public String getShader(GLSurfaceView mGlSurfaceView) {
        float fade_gamma = 0.3f;
        float amt = 1.0f - strength;
        float mult = 1.0f / (amt * 0.7f + 0.3f);
        float faded = fade_gamma + (1.0f - fade_gamma) * mult;
        float igamma = 1.0f / faded;

        String multString = "mult = " + mult + ";\n";
        String igammaString = "igamma = " + igamma + ";\n";

        String shader = "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"
                + "uniform samplerExternalOES sTexture;\n"
                + " float mult;\n"
                + " float igamma;\n"
                + "varying vec2 vTextureCoord;\n"
                + "void main()\n"
                + "{\n"
                // Parameters that were created above
                + multString
                + igammaString

                + "  const vec3 color_weights = vec3(0.25, 0.5, 0.25);\n"
                + "  vec4 color = texture2D(sTexture, vTextureCoord);\n"
                + "  float lightmask = dot(color.rgb, color_weights);\n"
                + "  float backmask = (1.0 - lightmask);\n"
                + "  vec3 ones = vec3(1.0, 1.0, 1.0);\n"
                + "  vec3 diff = pow(mult * color.rgb, igamma * ones) - color.rgb;\n"
                + "  diff = min(diff, 1.0);\n"
                + "  vec3 new_color = min(color.rgb + diff * backmask, 1.0);\n"
                + "  gl_FragColor = vec4(new_color, color.a);\n" + "}\n";

        return shader;

    }

}
