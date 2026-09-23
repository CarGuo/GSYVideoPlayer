package com.shuyu.gsyvideoplayer.render.effect;

import com.shuyu.gsyvideoplayer.render.glrender.GSYVideoGLViewPyramidInterface;

/**
 * 金字塔双滤波（Kawase dual-filter）迭代模糊。
 * <p>
 * 先逐级降采样（down，每级 4-tap 模糊 + 缩小一半）到底层，再逐级上采样
 * （up，每级 8-tap 模糊 + 放大一倍）回基础分辨率，最后一 pass 上屏。
 * 等效模糊范围随级数显著增大，而每个 pass 仅 4 / 8 次采样。
 */
public class IterativeBlurPyramidEffect implements GSYVideoGLViewPyramidInterface {

    private static final String DOWN_SHADER =
            "precision mediump float;\n"
            + "varying vec2 vTextureCoord;\n"
            + "uniform sampler2D sTexture;\n"
            + "uniform vec2 uTexelSize;\n"
            + "void main() {\n"
            + "  vec2 o = uTexelSize * 1.5;\n"
            + "  vec4 sum = texture2D(sTexture, vTextureCoord + vec2(-o.x, -o.y))\n"
            + "           + texture2D(sTexture, vTextureCoord + vec2( o.x, -o.y))\n"
            + "           + texture2D(sTexture, vTextureCoord + vec2(-o.x,  o.y))\n"
            + "           + texture2D(sTexture, vTextureCoord + vec2( o.x,  o.y));\n"
            + "  gl_FragColor = sum * 0.25;\n"
            + "}\n";

    private static final String UP_SHADER =
            "precision mediump float;\n"
            + "varying vec2 vTextureCoord;\n"
            + "uniform sampler2D sTexture;\n"
            + "uniform vec2 uTexelSize;\n"
            + "void main() {\n"
            + "  vec2 o = uTexelSize * 0.5;\n"
            + "  vec4 sum = texture2D(sTexture, vTextureCoord + vec2(-o.x, -o.y))\n"
            + "           + texture2D(sTexture, vTextureCoord + vec2(-o.x,  o.y))\n"
            + "           + texture2D(sTexture, vTextureCoord + vec2( o.x, -o.y))\n"
            + "           + texture2D(sTexture, vTextureCoord + vec2( o.x,  o.y))\n"
            + "           + texture2D(sTexture, vTextureCoord + vec2(-3.0*o.x, -3.0*o.y))\n"
            + "           + texture2D(sTexture, vTextureCoord + vec2(-3.0*o.x,  3.0*o.y))\n"
            + "           + texture2D(sTexture, vTextureCoord + vec2( 3.0*o.x, -3.0*o.y))\n"
            + "           + texture2D(sTexture, vTextureCoord + vec2( 3.0*o.x,  3.0*o.y));\n"
            + "  gl_FragColor = sum * 0.125;\n"
            + "}\n";

    private final int levels;

    public IterativeBlurPyramidEffect() {
        this(3);
    }

    public IterativeBlurPyramidEffect(int levels) {
        this.levels = levels < 1 ? 1 : levels;
    }

    @Override
    public int getPassCount() {
        return levels * 2;
    }

    @Override
    public String getPassShader(int pass) {
        return pass < levels ? DOWN_SHADER : UP_SHADER;
    }

    @Override
    public float getPassOutputScale(int pass) {
        if (pass < levels) {
            return 1.0f / (1 << (pass + 1));
        }
        int upIndex = pass - levels;
        int exponent = levels - 1 - upIndex;
        return exponent <= 0 ? 1.0f : 1.0f / (1 << exponent);
    }

    @Override
    public void onBindPassUniform(int program, int pass, int sourceWidth, int sourceHeight) {
    }

    @Override
    public void release() {
    }
}
