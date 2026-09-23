package com.shuyu.gsyvideoplayer.render.effect;

import android.opengl.GLSurfaceView;

import com.shuyu.gsyvideoplayer.render.view.GSYVideoGLView.ShaderInterface;

/**
 * Applies lomo-camera style effect to video.
 *
 * @author sheraz.khilji
 */
public class VignetteEffect implements ShaderInterface {
    private float mScale = 0f;
    private final float mShade = 0.85f;

    /**
     * Initialize Effect
     *
     * @param scale Float, between 0 and 1. 0 means no change.
     */
    public VignetteEffect(float scale) {
        if (scale < 0.0f) {
            scale = 0.0f;
        }
        if (scale > 1.0f) {
            scale = 1.0f;
        }
        this.mScale = scale;
    }

    @Override
    public String getShader(GLSurfaceView mGlSurfaceView) {
        return "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"
                + "uniform samplerExternalOES sTexture;\n"
                + "uniform vec2 uViewSize;\n"
                + "varying vec2 vTextureCoord;\n"
                + "void main() {\n"
                + "  vec2 viewSize = max(uViewSize, vec2(1.0));\n"
                + "  vec2 scale = viewSize.x > viewSize.y\n"
                + "    ? vec2(1.0, viewSize.y / viewSize.x)\n"
                + "    : vec2(viewSize.x / viewSize.y, 1.0);\n"
                + "  float inv_max_dist = 1.0 / (length(scale) * 0.5);\n"
                + "  float shade = " + mShade + ";\n"
                + "  float range = " + (1.30f - (float) Math.sqrt(mScale) * 0.7f) + ";\n"
                + "  const float slope = 20.0;\n"
                + "  vec2 coord = vTextureCoord - vec2(0.5, 0.5);\n"
                + "  float dist = length(coord * scale);\n"
                + "  float lumen = shade / (1.0 + exp((dist * inv_max_dist - range) * slope)) + (1.0 - shade);\n"
                + "  vec4 color = texture2D(sTexture, vTextureCoord);\n"
                + "  gl_FragColor = vec4(color.rgb * lumen, color.a);\n"
                + "}\n";
    }
}
