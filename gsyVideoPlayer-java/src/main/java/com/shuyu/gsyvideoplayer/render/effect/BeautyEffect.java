package com.shuyu.gsyvideoplayer.render.effect;

import android.opengl.GLSurfaceView;

import com.shuyu.gsyvideoplayer.render.view.GSYVideoGLView.ShaderInterface;

import java.util.Locale;

/**
 * 美颜：保边磨皮（双边滤波单 pass 近似）+ 暖色美白。
 * <p>
 * 磨皮对 8 方向 × 2 环共 16 个邻居做双边加权：空间权重按距离固定，颜色权重按与
 * 中心像素的亮度差取高斯。平坦皮肤 / 雀斑因亮度差小被平滑；五官轮廓两侧亮度差大，
 * 权重趋近 0，边缘得以保留。之后对中间调做非线性提亮并轻微暖色偏移实现美白。
 * <p>
 * 为兼容 GLSL ES 1.00，不使用数组构造器 / 循环下标索引，8 方向采样在 Java 端
 * 完全展开。纹元尺寸复用渲染器每帧绑定的 uViewSize（cRadius / uViewSize）。
 * 输入为 samplerExternalOES。
 */
public class BeautyEffect implements ShaderInterface {

    private final float smoothLevel;

    private final float whiteLevel;

    /**
     * @param smoothLevel 磨皮强度 0~1，0 表示不磨皮
     * @param whiteLevel  美白强度 0~1，0 表示不美白
     */
    public BeautyEffect(float smoothLevel, float whiteLevel) {
        this.smoothLevel = clamp01(smoothLevel);
        this.whiteLevel = clamp01(whiteLevel);
    }

    @Override
    public String getShader(GLSurfaceView mGlSurfaceView) {
        // 磨皮半径（纹元，约 1.5~5 px）与颜色域方差（亮度差 0~1 空间内）
        float radius = 1.5f + smoothLevel * 3.5f;
        float sigmaColor = 0.06f + smoothLevel * 0.16f;
        float invTwoVar = 1.0f / (2.0f * sigmaColor * sigmaColor);

        StringBuilder accum = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            double ang = i * Math.PI / 4.0;
            float dx = (float) Math.cos(ang);
            float dy = (float) Math.sin(ang);
            String dxs = f(dx);
            String dys = f(dy);
            accum.append("    off = vec2(").append(dxs).append(", ").append(dys)
                    .append(") * texel;\n");
            accum.append("    n1 = texture2D(sTexture, vTextureCoord + off).rgb;\n");
            accum.append("    n2 = texture2D(sTexture, vTextureCoord + off * 2.0).rgb;\n");
            accum.append("    dl = luma(n1) - lc;\n");
            accum.append("    a1 = exp(-dl * dl * cInvTwoVar);\n");
            accum.append("    dl = luma(n2) - lc;\n");
            accum.append("    a2 = 0.45 * exp(-dl * dl * cInvTwoVar);\n");
            accum.append("    sum += n1 * a1 + n2 * a2;\n");
            accum.append("    wsum += a1 + a2;\n");
        }

        String whiteMix = f(whiteLevel * 0.75f);
        String warmExtra = f(whiteLevel * 0.10f);

        String smoothAccum =
                "  vec3 sum = center.rgb;\n"
                + "  float wsum = 1.0;\n"
                + "  vec3 n1;\n"
                + "  vec3 n2;\n"
                + "  vec2 off;\n"
                + "  float dl;\n"
                + "  float a1;\n"
                + "  float a2;\n"
                + accum
                + "  smoothed = sum / wsum;\n";

        return "#extension GL_OES_EGL_image_external : require\n"
                + "precision highp float;\n"
                + "uniform samplerExternalOES sTexture;\n"
                + "uniform vec2 uViewSize;\n"
                + "varying vec2 vTextureCoord;\n"
                + "const float cRadius = " + f(radius) + ";\n"
                + "const float cSmoothOn = " + (smoothLevel > 0.001f ? "1.0" : "0.0") + ";\n"
                + "const float cInvTwoVar = " + f(invTwoVar) + ";\n"
                + "const float cWhiteOn = " + (whiteLevel > 0.001f ? "1.0" : "0.0") + ";\n"
                + "const float cWhiteMix = " + whiteMix + ";\n"
                + "const float cWarmExtra = " + warmExtra + ";\n"
                + "float luma(vec3 c) {\n"
                + "  return dot(c, vec3(0.299, 0.587, 0.114));\n"
                + "}\n"
                + "void main() {\n"
                + "  vec4 center = texture2D(sTexture, vTextureCoord);\n"
                + "  vec3 smoothed = center.rgb;\n"
                + "  if (cSmoothOn > 0.5) {\n"
                + "    vec2 texel = cRadius / max(uViewSize, vec2(1.0));\n"
                + "    float lc = luma(center.rgb);\n"
                + smoothAccum
                + "  }\n"
                + "  vec3 result = smoothed;\n"
                + "  if (cWhiteOn > 0.5) {\n"
                + "    vec3 bright = 1.0 - (1.0 - smoothed) * (1.0 - smoothed);\n"
                + "    vec3 delta = bright - smoothed;\n"
                + "    result = smoothed + delta * cWhiteMix;\n"
                + "    result.r += delta.r * cWarmExtra;\n"
                + "    result.b -= delta.b * cWarmExtra;\n"
                + "  }\n"
                + "  gl_FragColor = vec4(clamp(result, 0.0, 1.0), center.a);\n"
                + "}\n";
    }

    private static float clamp01(float v) {
        if (v < 0f) {
            return 0f;
        }
        if (v > 1f) {
            return 1f;
        }
        return v;
    }

    private static String f(float v) {
        return String.format(Locale.US, "%.5f", v);
    }
}
