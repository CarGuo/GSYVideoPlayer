package com.shuyu.gsyvideoplayer.render.effect;

import android.opengl.GLES20;

import com.shuyu.gsyvideoplayer.render.glrender.GSYVideoGLViewMultiPassInterface;

/**
 * 双向（可分离）高斯模糊：水平 pass + 垂直 pass。
 * 每个 pass 使用 9-tap 归一化高斯权重，采样偏移由 uTexelSize、方向和半径决定。
 */
public class GaussianBlurMultiPassEffect implements GSYVideoGLViewMultiPassInterface {

    private static final int PASS_COUNT = 2;

    private static final String SHADER =
            "precision mediump float;\n"
            + "varying vec2 vTextureCoord;\n"
            + "uniform sampler2D sTexture;\n"
            + "uniform vec2 uTexelSize;\n"
            + "uniform vec2 uDirection;\n"
            + "uniform float uRadius;\n"
            + "void main() {\n"
            + "  vec2 stepOffset = uTexelSize * uDirection * uRadius;\n"
            + "  vec4 sum = vec4(0.0);\n"
            + "  sum += texture2D(sTexture, vTextureCoord - 4.0 * stepOffset) * 0.0162162162;\n"
            + "  sum += texture2D(sTexture, vTextureCoord - 3.0 * stepOffset) * 0.0540540541;\n"
            + "  sum += texture2D(sTexture, vTextureCoord - 2.0 * stepOffset) * 0.1216216216;\n"
            + "  sum += texture2D(sTexture, vTextureCoord - 1.0 * stepOffset) * 0.1945945946;\n"
            + "  sum += texture2D(sTexture, vTextureCoord) * 0.2270270270;\n"
            + "  sum += texture2D(sTexture, vTextureCoord + 1.0 * stepOffset) * 0.1945945946;\n"
            + "  sum += texture2D(sTexture, vTextureCoord + 2.0 * stepOffset) * 0.1216216216;\n"
            + "  sum += texture2D(sTexture, vTextureCoord + 3.0 * stepOffset) * 0.0540540541;\n"
            + "  sum += texture2D(sTexture, vTextureCoord + 4.0 * stepOffset) * 0.0162162162;\n"
            + "  gl_FragColor = sum;\n"
            + "}\n";

    private final float radius;

    public GaussianBlurMultiPassEffect(float radius) {
        this.radius = radius;
    }

    @Override
    public int getPassCount() {
        return PASS_COUNT;
    }

    @Override
    public String getPassShader(int pass) {
        return SHADER;
    }

    @Override
    public void onBindPassUniform(int program, int pass, int sourceWidth, int sourceHeight) {
        int directionHandle = GLES20.glGetUniformLocation(program, "uDirection");
        int radiusHandle = GLES20.glGetUniformLocation(program, "uRadius");
        if (pass == 0) {
            GLES20.glUniform2f(directionHandle, 1.0f, 0.0f);
        } else {
            GLES20.glUniform2f(directionHandle, 0.0f, 1.0f);
        }
        GLES20.glUniform1f(radiusHandle, radius);
    }

    @Override
    public void release() {
    }
}
