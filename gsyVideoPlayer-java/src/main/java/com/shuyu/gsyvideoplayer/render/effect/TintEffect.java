package com.shuyu.gsyvideoplayer.render.effect;

import android.graphics.Color;
import android.opengl.GLSurfaceView;

import  com.shuyu.gsyvideoplayer.render.view.GSYVideoGLView.ShaderInterface;


/**
 * Tints the video with specified color..
 *
 * @author sheraz.khilji
 */
public class TintEffect implements ShaderInterface {
    private int mTint = 0xFF0000FF;

    /**
     * Initialize Effect
     *
     * @param color Integer, representing an ARGB color with 8 bits per channel.
     *              May be created using Color class.
     */
    public TintEffect(int color) {
        this.mTint = color;

    }

    @Override
    public String getShader(GLSurfaceView mGlSurfaceView) {
        float color_ratio[] = {0.21f, 0.71f, 0.07f};
        String color_ratioString[] = new String[3];
        color_ratioString[0] = "color_ratio[0] = " + color_ratio[0] + ";\n";
        color_ratioString[1] = "color_ratio[1] = " + color_ratio[1] + ";\n";
        color_ratioString[2] = "color_ratio[2] = " + color_ratio[2] + ";\n";

        float tint_color[] = {Color.red(mTint) / 255f,
                Color.green(mTint) / 255f, Color.blue(mTint) / 255f};

        String tintString[] = new String[3];
        tintString[0] = "tint[0] = " + tint_color[0] + ";\n";
        tintString[1] = "tint[1] = " + tint_color[1] + ";\n";
        tintString[2] = "tint[2] = " + tint_color[2] + ";\n";

        String shader = "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"
                + "uniform samplerExternalOES sTexture;\n"
                + " vec3 tint;\n"
                + " vec3 color_ratio;\n"
                + "varying vec2 vTextureCoord;\n"
                + "void main() {\n"
                // Parameters that were created above
                + color_ratioString[0]
                + color_ratioString[1]
                + color_ratioString[2]
                + tintString[0]
                + tintString[1]
                + tintString[2]
                + "  vec4 color = texture2D(sTexture, vTextureCoord);\n"
                + "  float avg_color = dot(color_ratio, color.rgb);\n"
                + "  vec3 new_color = min(0.8 * avg_color + 0.2 * tint, 1.0);\n"
                + "  gl_FragColor = vec4(new_color.rgb, color.a);\n" + "}\n";
        return shader;

    }
}
