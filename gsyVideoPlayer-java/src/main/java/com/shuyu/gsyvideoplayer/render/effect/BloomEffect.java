package com.shuyu.gsyvideoplayer.render.effect;

import android.opengl.GLES20;

import com.shuyu.gsyvideoplayer.render.glrender.GSYVideoGLViewCompositeInterface;

/**
 * Bloom（辉光）效果。
 * <p>
 * 流程（以 levels=3 为例，共 6 个 pass）：
 * <ol>
 *   <li>bright-pass：按亮度阈值（soft knee）提取高亮区域，直接输出半分辨率，
 *       借 GL_LINEAR 获得一次免费预模糊并降低填充率；</li>
 *   <li>逐级 Kawase down（4-tap）到 1/8；</li>
 *   <li>逐级 Kawase up（8-tap）回到半分辨率，得到大范围弥散的亮部；</li>
 *   <li>合成：原始全分辨率场景 + 模糊亮部 * 强度，clamp 到 1，上屏。</li>
 * </ol>
 * 合成 pass 通过 {@link GSYVideoGLViewCompositeInterface} 额外采样原始场景。
 * 当前为 RGBA8 LDR 管线，不含浮点 HDR 与色调映射。
 */
public class BloomEffect implements GSYVideoGLViewCompositeInterface {

    private static final String BRIGHT_SHADER =
            "precision mediump float;\n"
            + "varying vec2 vTextureCoord;\n"
            + "uniform sampler2D sTexture;\n"
            + "uniform float uThreshold;\n"
            + "uniform float uKnee;\n"
            + "void main() {\n"
            + "  vec3 c = texture2D(sTexture, vTextureCoord).rgb;\n"
            + "  float l = dot(c, vec3(0.2126, 0.7152, 0.0722));\n"
            + "  float k = max(uKnee, 0.0001);\n"
            + "  float contrib = smoothstep(uThreshold - k, uThreshold + k, l);\n"
            + "  gl_FragColor = vec4(c * contrib, 1.0);\n"
            + "}\n";

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

    private static final String COMPOSITE_SHADER =
            "precision mediump float;\n"
            + "varying vec2 vTextureCoord;\n"
            + "uniform sampler2D sTexture;\n"
            + "uniform sampler2D uOriginalTexture;\n"
            + "uniform float uIntensity;\n"
            + "void main() {\n"
            + "  vec4 base = texture2D(uOriginalTexture, vTextureCoord);\n"
            + "  vec3 bloom = texture2D(sTexture, vTextureCoord).rgb;\n"
            + "  vec3 outColor = clamp(base.rgb + bloom * uIntensity, 0.0, 1.0);\n"
            + "  gl_FragColor = vec4(outColor, base.a);\n"
            + "}\n";

    private final int levels;

    private final float threshold;

    private final float knee;

    private final float intensity;

    public BloomEffect() {
        this(3, 0.7f, 0.15f, 1.0f);
    }

    public BloomEffect(int levels, float threshold, float knee, float intensity) {
        this.levels = Math.max(1, levels);
        this.threshold = threshold;
        this.knee = knee;
        this.intensity = intensity;
    }

    @Override
    public int getPassCount() {
        return levels * 2;
    }

    @Override
    public String getPassShader(int pass) {
        if (pass == 0) {
            return BRIGHT_SHADER;
        }
        if (pass == getPassCount() - 1) {
            return COMPOSITE_SHADER;
        }
        return pass < levels ? DOWN_SHADER : UP_SHADER;
    }

    @Override
    public float getPassOutputScale(int pass) {
        if (pass == 0) {
            return 0.5f;
        }
        if (pass < levels) {
            return 1.0f / (1 << (pass + 1));
        }
        int upIndex = pass - levels;
        int exponent = levels - 1 - upIndex;
        if (exponent <= 0) {
            return 0.5f;
        }
        return 1.0f / (1 << exponent);
    }

    @Override
    public String getBaseSceneSampler(int pass) {
        return pass == getPassCount() - 1 ? "uOriginalTexture" : null;
    }

    @Override
    public void onBindPassUniform(int program, int pass, int sourceWidth, int sourceHeight) {
        if (pass == 0) {
            int thresholdHandle = GLES20.glGetUniformLocation(program, "uThreshold");
            int kneeHandle = GLES20.glGetUniformLocation(program, "uKnee");
            GLES20.glUniform1f(thresholdHandle, threshold);
            GLES20.glUniform1f(kneeHandle, knee);
        } else if (pass == getPassCount() - 1) {
            int intensityHandle = GLES20.glGetUniformLocation(program, "uIntensity");
            GLES20.glUniform1f(intensityHandle, intensity);
        }
    }

    @Override
    public void release() {
    }
}
