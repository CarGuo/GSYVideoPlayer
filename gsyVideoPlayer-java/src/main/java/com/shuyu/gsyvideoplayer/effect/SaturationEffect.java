package com.shuyu.gsyvideoplayer.effect;

import android.opengl.GLSurfaceView;

import  com.shuyu.gsyvideoplayer.GSYVideoGLView.ShaderInterface;

/**
 * Adjusts color saturation of video. There is still some issue with this
 * effect.
 *
 * @author sheraz.khilji
 */
public class SaturationEffect implements ShaderInterface {
    private float scale = 0f;

    /**
     * Initialize Effect
     *
     * @param scale Float, between -1 and 1. 0 means no change, while -1 indicates
     *              full desaturation, i.e. grayscale.
     */
    public SaturationEffect(float scale) {

        this.scale = scale;
    }

    @Override
    public String getShader(GLSurfaceView mGlSurfaceView) {
        float shift = 1.0f / 255.0f;
        float weights[] = {2f / 8f, 5f / 8f, 1f / 8f};
        float exponents[] = new float[3];

        String weightsString[] = new String[3];
        String exponentsString[] = new String[3];
        exponentsString[0] = "";
        exponentsString[1] = "";
        exponentsString[2] = "";
        String scaleString = "";

        if (scale > 0.0f) {
            exponents[0] = (0.9f * scale) + 1.0f;
            exponents[1] = (2.1f * scale) + 1.0f;
            exponents[2] = (2.7f * scale) + 1.0f;
            exponentsString[0] = "exponents[0] = " + exponents[0] + ";\n";
            exponentsString[1] = "exponents[1] = " + exponents[1] + ";\n";
            exponentsString[2] = "exponents[2] = " + exponents[2] + ";\n";
        } else
            scaleString = "scale = " + (1.0f + scale) + ";\n";

        weightsString[0] = "weights[0] = " + weights[0] + ";\n";
        weightsString[1] = "weights[1] = " + weights[1] + ";\n";
        weightsString[2] = "weights[2] = " + weights[2] + ";\n";
        String shiftString = "shift = " + shift + ";\n";

        String shader = "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"
                + "uniform samplerExternalOES sTexture;\n" + " float scale;\n"
                + " float shift;\n" + " vec3 weights;\n" + " vec3 exponents;\n"
                + "varying vec2 vTextureCoord;\n" + "void main() {\n"
                // Parameters that were created above
                + weightsString[0]
                + weightsString[1]
                + weightsString[2]
                + shiftString
                + scaleString
                + "  vec4 oldcolor = texture2D(sTexture, vTextureCoord);\n"
                + "  float kv = dot(oldcolor.rgb, weights) + shift;\n"
                + "  vec3 new_color = scale * oldcolor.rgb + (1.0 - scale) * kv;\n"
                + "  gl_FragColor= vec4(new_color, oldcolor.a);\n"
                // Parameters that were created above
                + weightsString[0]
                + weightsString[1]
                + weightsString[2]
                + exponentsString[0]
                + exponentsString[1]
                + exponentsString[2]
                + "  vec4 color = texture2D(sTexture, vTextureCoord);\n"
                + "  float de = dot(color.rgb, weights);\n"
                + "  float inv_de = 1.0 / de;\n"
                + "  vec3 verynew_color = de * pow(color.rgb * inv_de, exponents);\n"
                + "  float max_color = max(max(max(verynew_color.r, verynew_color.g), verynew_color.b), 1.0);\n"
                + "  gl_FragColor = gl_FragColor+vec4(verynew_color / max_color, color.a);\n"
                + "}\n";

        return shader;

    }

}
