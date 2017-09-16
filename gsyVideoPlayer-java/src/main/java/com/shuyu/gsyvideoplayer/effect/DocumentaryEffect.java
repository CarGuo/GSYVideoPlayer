package com.shuyu.gsyvideoplayer.effect;

import android.opengl.GLSurfaceView;

import  com.shuyu.gsyvideoplayer.GSYVideoGLView.ShaderInterface;

import java.util.Date;
import java.util.Random;

/**
 * Applies black and white documentary style effect on video..
 *
 * @author sheraz.khilji
 */
public class DocumentaryEffect implements ShaderInterface {
    private int mWidth;
    private int mHeight;
    private Random mRandom;

    /**
     * Initialize Effect
     */
    public DocumentaryEffect() {

    }

    /**
     * Init all values that will be used by this shader.
     *
     * @param mGlSurfaceView which is responsible for displaying your video
     */
    private void initValues(GLSurfaceView mGlSurfaceView) {
        mWidth = mGlSurfaceView.getWidth();
        mHeight = mGlSurfaceView.getHeight();
        mRandom = new Random(new Date().getTime());
    }

    @Override
    public String getShader(GLSurfaceView mGlSurfaceView) {
        initValues(mGlSurfaceView);
        float scale[] = new float[2];
        if (mWidth > mHeight) {
            scale[0] = 1f;
            scale[1] = ((float) mHeight) / mWidth;
        } else {
            scale[0] = ((float) mWidth) / mHeight;
            scale[1] = 1f;
        }
        float max_dist = ((float) Math.sqrt(scale[0] * scale[0] + scale[1]
                * scale[1])) * 0.5f;

        float seed[] = {mRandom.nextFloat(), mRandom.nextFloat()};

        String scaleString[] = new String[2];
        String seedString[] = new String[2];

        scaleString[0] = "scale[0] = " + scale[0] + ";\n";
        scaleString[1] = "scale[1] = " + scale[1] + ";\n";

        seedString[0] = "seed[0] = " + seed[0] + ";\n";
        seedString[1] = "seed[1] = " + seed[1] + ";\n";

        String inv_max_distString = "inv_max_dist = " + 1.0f / max_dist + ";\n";
        String stepsizeString = "stepsize = " + 1.0f / 255.0f + ";\n";

        String shader = "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"
                + "uniform samplerExternalOES sTexture;\n"
                + " vec2 seed;\n"
                + " float stepsize;\n"
                + " float inv_max_dist;\n"
                + " vec2 scale;\n"
                + "varying vec2 vTextureCoord;\n"
                + "float rand(vec2 loc) {\n"
                + "  float theta1 = dot(loc, vec2(0.9898, 0.233));\n"
                + "  float theta2 = dot(loc, vec2(12.0, 78.0));\n"
                + "  float value = cos(theta1) * sin(theta2) + sin(theta1) * cos(theta2);\n"
                +
                // keep value of part1 in range: (2^-14 to 2^14).
                "  float temp = mod(197.0 * value, 1.0) + value;\n"
                + "  float part1 = mod(220.0 * temp, 1.0) + temp;\n"
                + "  float part2 = value * 0.5453;\n"
                + "  float part3 = cos(theta1 + theta2) * 0.43758;\n"
                + "  return fract(part1 + part2 + part3);\n"
                + "}\n"
                + "void main() {\n"
                // Parameters that were created above
                + scaleString[0]
                + scaleString[1]
                + seedString[0]
                + seedString[1]
                + inv_max_distString
                + stepsizeString

                // black white
                + "  vec4 color = texture2D(sTexture, vTextureCoord);\n"
                + "  float dither = rand(vTextureCoord + seed);\n"
                + "  vec3 xform = clamp(2.0 * color.rgb, 0.0, 1.0);\n"
                + "  vec3 temp = clamp(2.0 * (color.rgb + stepsize), 0.0, 1.0);\n"
                + "  vec3 new_color = clamp(xform + (temp - xform) * (dither - 0.5), 0.0, 1.0);\n"
                +
                // grayscale
                "  float gray = dot(new_color, vec3(0.299, 0.587, 0.114));\n"
                + "  new_color = vec3(gray, gray, gray);\n"
                +
                // vignette
                "  vec2 coord = vTextureCoord - vec2(0.5, 0.5);\n"
                + "  float dist = length(coord * scale);\n"
                + "  float lumen = 0.85 / (1.0 + exp((dist * inv_max_dist - 0.83) * 20.0)) + 0.15;\n"
                + "  gl_FragColor = vec4(new_color * lumen, color.a);\n"
                + "}\n";

        return shader;

    }
}
