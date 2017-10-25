package com.shuyu.gsyvideoplayer.render.effect;

import android.opengl.GLSurfaceView;

import  com.shuyu.gsyvideoplayer.render.view.GSYVideoGLView.ShaderInterface;

/**
 * Sharpens the video.
 *
 * @author sheraz.khilji
 */
public class SharpnessEffect implements ShaderInterface {
    private int mWidth;
    private int mHeight;
    private float scale = 0f;

    /**
     * Initialize Effect
     *
     * @param scale Float, between 0 and 1. 0 means no change.
     */
    public SharpnessEffect(float scale) {
        if (scale < 0.0f)
            scale = 0.0f;
        if (scale > 1.0f)
            scale = 1.0f;

        this.scale = scale;
    }

    /**
     * Init all values that will be used by this shader.
     *
     * @param mGlSurfaceView which is responsible for displaying your video
     */
    private void initValues(GLSurfaceView mGlSurfaceView) {
        mWidth = mGlSurfaceView.getWidth();
        mHeight = mGlSurfaceView.getHeight();
    }

    @Override
    public String getShader(GLSurfaceView mGlSurfaceView) {
        initValues(mGlSurfaceView);

        String stepsizeXString = "stepsizeX = " + 1.0f / mWidth + ";\n";
        String stepsizeYString = "stepsizeY = " + 1.0f / mHeight + ";\n";
        String scaleString = "scale = " + scale + ";\n";

        String shader = "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"
                + "uniform samplerExternalOES sTexture;\n"
                + " float scale;\n"
                + " float stepsizeX;\n"
                + " float stepsizeY;\n"
                + "varying vec2 vTextureCoord;\n"
                + "void main() {\n"
                // Parameters that were created above
                + stepsizeXString
                + stepsizeYString
                + scaleString
                + "  vec3 nbr_color = vec3(0.0, 0.0, 0.0);\n"
                + "  vec2 coord;\n"
                + "  vec4 color = texture2D(sTexture, vTextureCoord);\n"
                + "  coord.x = vTextureCoord.x - 0.5 * stepsizeX;\n"
                + "  coord.y = vTextureCoord.y - stepsizeY;\n"
                + "  nbr_color += texture2D(sTexture, coord).rgb - color.rgb;\n"
                + "  coord.x = vTextureCoord.x - stepsizeX;\n"
                + "  coord.y = vTextureCoord.y + 0.5 * stepsizeY;\n"
                + "  nbr_color += texture2D(sTexture, coord).rgb - color.rgb;\n"
                + "  coord.x = vTextureCoord.x + stepsizeX;\n"
                + "  coord.y = vTextureCoord.y - 0.5 * stepsizeY;\n"
                + "  nbr_color += texture2D(sTexture, coord).rgb - color.rgb;\n"
                + "  coord.x = vTextureCoord.x + stepsizeX;\n"
                + "  coord.y = vTextureCoord.y + 0.5 * stepsizeY;\n"
                + "  nbr_color += texture2D(sTexture, coord).rgb - color.rgb;\n"
                + "  gl_FragColor = vec4(color.rgb - 2.0 * scale * nbr_color, color.a);\n"
                + "}\n";

        return shader;

    }

}
