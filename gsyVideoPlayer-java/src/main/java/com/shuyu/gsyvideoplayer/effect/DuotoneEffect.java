package com.shuyu.gsyvideoplayer.effect;

import android.graphics.Color;
import android.opengl.GLSurfaceView;

import  com.shuyu.gsyvideoplayer.GSYVideoGLView.ShaderInterface;


/**
 * Representation of video using only two color tones.
 *
 * @author sheraz.khilji
 */
public class DuotoneEffect implements ShaderInterface {
    // Default values
    private int mFirstColor = Color.MAGENTA;
    private int mSecondColor = Color.YELLOW;

    /**
     * Initialize effect
     *
     * @param mFirstColor Integer, representing an ARGB color with 8 bits per channel.
     *               May be created using Color class.
     * @param mSecondColor Integer, representing an ARGB color with 8 bits per channel.
     *               May be created using Color class.
     */
    public DuotoneEffect(int mFirstColor, int mSecondColor) {
        this.mFirstColor = mFirstColor;
        this.mSecondColor = mSecondColor;
    }

    @Override
    public String getShader(GLSurfaceView mGlSurfaceView) {
        float first[] = {Color.red(mFirstColor) / 255f,
                Color.green(mFirstColor) / 255f, Color.blue(mFirstColor) / 255f};
        float second[] = {Color.red(mSecondColor) / 255f,
                Color.green(mSecondColor) / 255f,
                Color.blue(mSecondColor) / 255f};

        String firstColorString[] = new String[3];
        String secondColorString[] = new String[3];

        firstColorString[0] = "first[0] = " + first[0] + ";\n";
        firstColorString[1] = "first[1] = " + first[1] + ";\n";
        firstColorString[2] = "first[2] = " + first[2] + ";\n";

        secondColorString[0] = "second[0] = " + second[0] + ";\n";
        secondColorString[1] = "second[1] = " + second[1] + ";\n";
        secondColorString[2] = "second[2] = " + second[2] + ";\n";

        String shader = "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"
                + "uniform samplerExternalOES sTexture;\n"
                + " vec3 first;\n"
                + " vec3 second;\n"
                + "varying vec2 vTextureCoord;\n"
                + "void main() {\n"
                // Parameters that were created above
                + firstColorString[0]
                + firstColorString[1]
                + firstColorString[2]
                + secondColorString[0]
                + secondColorString[1]
                + secondColorString[2]

                + "  vec4 color = texture2D(sTexture, vTextureCoord);\n"
                + "  float energy = (color.r + color.g + color.b) * 0.3333;\n"
                + "  vec3 new_color = (1.0 - energy) * first + energy * second;\n"
                + "  gl_FragColor = vec4(new_color.rgb, color.a);\n" + "}\n";

        return shader;

    }
}
