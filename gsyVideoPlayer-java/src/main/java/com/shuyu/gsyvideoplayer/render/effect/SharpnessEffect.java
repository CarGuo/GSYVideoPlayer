package com.shuyu.gsyvideoplayer.render.effect;

import android.opengl.GLSurfaceView;

import com.shuyu.gsyvideoplayer.render.view.GSYVideoGLView.ShaderInterface;

/**
 * Sharpens the video.
 *
 * @author sheraz.khilji
 */
public class SharpnessEffect implements ShaderInterface {
    private float scale = 0f;

    /**
     * Initialize Effect
     *
     * @param scale Float, between 0 and 1. 0 means no change.
     */
    public SharpnessEffect(float scale) {
        if (scale < 0.0f) {
            scale = 0.0f;
        }
        if (scale > 1.0f) {
            scale = 1.0f;
        }

        this.scale = scale;
    }

    @Override
    public String getShader(GLSurfaceView mGlSurfaceView) {
        return "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"
                + "uniform samplerExternalOES sTexture;\n"
                + "uniform vec2 uViewSize;\n"
                + "varying vec2 vTextureCoord;\n"
                + "void main() {\n"
                + "  float scale = " + scale + ";\n"
                + "  vec2 texel = 1.0 / max(uViewSize, vec2(1.0));\n"
                + "  vec3 nbr_color = vec3(0.0, 0.0, 0.0);\n"
                + "  vec2 coord;\n"
                + "  vec4 color = texture2D(sTexture, vTextureCoord);\n"
                + "  coord.x = vTextureCoord.x - 0.5 * texel.x;\n"
                + "  coord.y = vTextureCoord.y - texel.y;\n"
                + "  nbr_color += texture2D(sTexture, coord).rgb - color.rgb;\n"
                + "  coord.x = vTextureCoord.x - texel.x;\n"
                + "  coord.y = vTextureCoord.y + 0.5 * texel.y;\n"
                + "  nbr_color += texture2D(sTexture, coord).rgb - color.rgb;\n"
                + "  coord.x = vTextureCoord.x + texel.x;\n"
                + "  coord.y = vTextureCoord.y - 0.5 * texel.y;\n"
                + "  nbr_color += texture2D(sTexture, coord).rgb - color.rgb;\n"
                + "  coord.x = vTextureCoord.x + texel.x;\n"
                + "  coord.y = vTextureCoord.y + 0.5 * texel.y;\n"
                + "  nbr_color += texture2D(sTexture, coord).rgb - color.rgb;\n"
                + "  gl_FragColor = vec4(color.rgb - 2.0 * scale * nbr_color, color.a);\n"
                + "}\n";
    }
}
