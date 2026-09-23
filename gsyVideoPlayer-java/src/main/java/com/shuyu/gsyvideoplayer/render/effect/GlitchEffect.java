package com.shuyu.gsyvideoplayer.render.effect;

import android.opengl.GLSurfaceView;

import com.shuyu.gsyvideoplayer.render.view.GSYVideoGLView.ShaderInterface;

/**
 * 数字故障风（Glitch）：RGB 通道错位、水平撕裂条、块位移、噪点与轻微画面抖动。
 * <p>
 * 这是项目里第一个随时间变化的效果，依赖渲染器每帧自动注入的 {@code uTime}（秒）。
 * 随机量按量化时间（{@code floor(uTime*8)}）生成，保证同一帧稳定、跨帧跳变，
 * 避免逐像素随机导致画面不可读。
 */
public class GlitchEffect implements ShaderInterface {

    private final float strength;

    /**
     * @param strength 0~1，故障强度，0 退化为接近原图
     */
    public GlitchEffect(float strength) {
        if (strength < 0.0f) {
            strength = 0.0f;
        }
        if (strength > 1.0f) {
            strength = 1.0f;
        }
        this.strength = strength;
    }

    @Override
    public String getShader(GLSurfaceView mGlSurfaceView) {
        return "#extension GL_OES_EGL_image_external : require\n"
                + "precision highp float;\n"
                + "uniform samplerExternalOES sTexture;\n"
                + "uniform float uTime;\n"
                + "varying vec2 vTextureCoord;\n"
                + "float hash(vec2 p) {\n"
                + "  return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);\n"
                + "}\n"
                + "void main() {\n"
                + "  float amp = " + strength + ";\n"
                + "  float stepT = floor(uTime * 8.0);\n"
                + "  float g = hash(vec2(stepT, 1.0));\n"
                + "  float pulse = step(0.72, g);\n"
                + "  vec2 uv = vTextureCoord;\n"
                + "  uv.x += (hash(vec2(stepT, 2.0)) - 0.5) * 0.012 * amp;\n"
                + "  float shift = 0.0;\n"
                + "  for (int i = 0; i < 3; i++) {\n"
                + "    float fi = float(i);\n"
                + "    float y0 = hash(vec2(fi, stepT + 3.0));\n"
                + "    float h = 0.04 + 0.12 * hash(vec2(fi, stepT + 13.0));\n"
                + "    float s = (hash(vec2(fi, stepT + 23.0)) - 0.5)\n"
                + "              * 0.18 * amp * (0.3 + pulse);\n"
                + "    float inBand = step(y0, uv.y) * step(uv.y, y0 + h);\n"
                + "    shift += s * inBand;\n"
                + "  }\n"
                + "  uv.x += shift;\n"
                + "  vec2 blockId = floor(uv * vec2(16.0, 9.0));\n"
                + "  float bv = hash(blockId + vec2(stepT, 7.0));\n"
                + "  uv.x += (bv - 0.5) * 0.06 * amp * (0.25 + pulse)\n"
                + "          * step(0.86, bv);\n"
                + "  float split = amp * (0.003 + 0.006 * g + 0.014 * pulse);\n"
                + "  float rr = texture2D(sTexture, uv + vec2(split, 0.0)).r;\n"
                + "  vec4 mid = texture2D(sTexture, uv);\n"
                + "  float bb = texture2D(sTexture, uv - vec2(split, 0.0)).b;\n"
                + "  vec3 col = vec3(rr, mid.g, bb);\n"
                + "  vec2 npos = floor(uv * vec2(1280.0, 720.0));\n"
                + "  float n = hash(npos + vec2(stepT, 3.0));\n"
                + "  col += (n - 0.5) * 0.08 * amp;\n"
                + "  gl_FragColor = vec4(clamp(col, 0.0, 1.0), mid.a);\n"
                + "}\n";
    }
}
