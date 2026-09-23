package com.shuyu.gsyvideoplayer.render.effect;

import android.opengl.GLSurfaceView;

import com.shuyu.gsyvideoplayer.render.view.GSYVideoGLView.ShaderInterface;

import java.util.Date;
import java.util.Random;

/**
 * Applies black and white documentary style effect on video..
 *
 * @author sheraz.khilji
 */
public class DocumentaryEffect implements ShaderInterface {
    private Random mRandom;

    /**
     * Initialize Effect
     */
    public DocumentaryEffect() {
    }

    @Override
    public String getShader(GLSurfaceView mGlSurfaceView) {
        if (mRandom == null) {
            mRandom = new Random(new Date().getTime());
        }
        float[] seed = {mRandom.nextFloat(), mRandom.nextFloat()};

        return "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"
                + "uniform samplerExternalOES sTexture;\n"
                + "uniform vec2 uViewSize;\n"
                + "varying vec2 vTextureCoord;\n"
                + "float rand(vec2 loc) {\n"
                + "  float theta1 = dot(loc, vec2(0.9898, 0.233));\n"
                + "  float theta2 = dot(loc, vec2(12.0, 78.0));\n"
                + "  float value = cos(theta1) * sin(theta2) + sin(theta1) * cos(theta2);\n"
                + "  float temp = mod(197.0 * value, 1.0) + value;\n"
                + "  float part1 = mod(220.0 * temp, 1.0) + temp;\n"
                + "  float part2 = value * 0.5453;\n"
                + "  float part3 = cos(theta1 + theta2) * 0.43758;\n"
                + "  return fract(part1 + part2 + part3);\n"
                + "}\n"
                + "void main() {\n"
                + "  vec2 seed = vec2(" + seed[0] + ", " + seed[1] + ");\n"
                + "  float stepsize = 1.0 / 255.0;\n"
                + "  vec2 viewSize = max(uViewSize, vec2(1.0));\n"
                + "  vec2 scale = viewSize.x > viewSize.y\n"
                + "    ? vec2(1.0, viewSize.y / viewSize.x)\n"
                + "    : vec2(viewSize.x / viewSize.y, 1.0);\n"
                + "  float inv_max_dist = 1.0 / (length(scale) * 0.5);\n"
                + "  vec4 color = texture2D(sTexture, vTextureCoord);\n"
                + "  float dither = rand(vTextureCoord + seed);\n"
                + "  vec3 xform = clamp(2.0 * color.rgb, 0.0, 1.0);\n"
                + "  vec3 temp = clamp(2.0 * (color.rgb + stepsize), 0.0, 1.0);\n"
                + "  vec3 new_color = clamp(xform + (temp - xform) * (dither - 0.5), 0.0, 1.0);\n"
                + "  float gray = dot(new_color, vec3(0.299, 0.587, 0.114));\n"
                + "  new_color = vec3(gray, gray, gray);\n"
                + "  vec2 coord = vTextureCoord - vec2(0.5, 0.5);\n"
                + "  float dist = length(coord * scale);\n"
                + "  float lumen = 0.85 / (1.0 + exp((dist * inv_max_dist - 0.83) * 20.0)) + 0.15;\n"
                + "  gl_FragColor = vec4(new_color * lumen, color.a);\n"
                + "}\n";
    }
}
