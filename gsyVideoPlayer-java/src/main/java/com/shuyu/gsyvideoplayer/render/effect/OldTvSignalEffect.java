package com.shuyu.gsyvideoplayer.render.effect;

import android.opengl.GLSurfaceView;

import com.shuyu.gsyvideoplayer.render.view.GSYVideoGLView.ShaderInterface;

/**
 * 老电视模拟信号干扰（Bad Reception）：密集滚动雪花、垂直滚动干扰带、水平失步漂移、
 * 多径重影（鬼影）、间歇“丢台”整屏全雪花，叠加降饱和暖黄旧电视调色、弱扫描线与暗角。
 * <p>
 * 与现有动态效果的区分：
 * <ul>
 *     <li>{@link GlitchEffect} 是<b>数字</b>故障：量化硬跳变、RGB 错位、数字丢块；</li>
 *     <li>{@link CrtEffect} 是显像管<b>硬件物理</b>：屏幕弯曲、三色像素栅格；</li>
 *     <li>本效果是<b>模拟天线接收不良</b>：连续翻滚雪花、信号强度起伏、滚动带与丢台。</li>
 * </ul>
 * 依赖渲染器每帧自动注入的 {@code uTime}（秒）与 {@code uViewSize}。
 */
public class OldTvSignalEffect implements ShaderInterface {

    private final float strength;

    /**
     * @param strength 0~1，干扰强度，0 退化为接近原图
     */
    public OldTvSignalEffect(float strength) {
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
                + "uniform vec2 uViewSize;\n"
                + "uniform float uTime;\n"
                + "varying vec2 vTextureCoord;\n"
                + "float hash(vec2 p) {\n"
                + "  return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);\n"
                + "}\n"
                + "void main() {\n"
                + "  float amp = " + strength + ";\n"
                + "  float frameT = floor(uTime * 24.0);\n"
                + "  float secT = floor(uTime);\n"
                + "  float cyclePos = mod(uTime, 7.0);\n"
                + "  float dropFade = smoothstep(0.0, 0.12, cyclePos)\n"
                + "                   * (1.0 - smoothstep(0.48, 0.6, cyclePos));\n"
                + "  float signal = mix(1.0, 0.06, dropFade);\n"
                + "  float wobble = 0.85 + 0.15 * sin(uTime * 1.3);\n"
                + "  float drift = (sin(uTime * 2.1) * 0.008\n"
                + "                + (hash(vec2(secT, 5.0)) - 0.5) * 0.012)\n"
                + "                * amp * (0.4 + (1.0 - signal));\n"
                + "  vec2 uv = vTextureCoord;\n"
                + "  uv.x += drift;\n"
                + "  float ghostOff = 0.018 * amp;\n"
                + "  vec4 mainc = texture2D(sTexture, uv);\n"
                + "  vec3 ghost1 = texture2D(sTexture, uv + vec2(ghostOff, 0.0)).rgb;\n"
                + "  vec3 ghost2 = texture2D(sTexture, uv - vec2(ghostOff * 0.6, 0.0)).rgb;\n"
                + "  vec3 base = mainc.rgb + (ghost1 + ghost2) * 0.12 * amp;\n"
                + "  float luma = dot(base, vec3(0.299, 0.587, 0.114));\n"
                + "  vec3 graded = mix(vec3(luma), base, 1.0 - 0.28 * amp);\n"
                + "  graded = (graded - 0.5) * (1.0 + 0.18 * amp) + 0.5;\n"
                + "  graded += vec3(0.03, 0.012, -0.02) * amp;\n"
                + "  float band = sin((uv.y - uTime * 0.12) * 6.28318 * 1.5);\n"
                + "  graded *= 1.0 + 0.10 * amp * band;\n"
                + "  float bd = abs(fract(uv.y - uTime * 0.25) - 0.5);\n"
                + "  float bar = 1.0 - smoothstep(0.005, 0.02, bd);\n"
                + "  graded += bar * 0.25 * amp;\n"
                + "  vec2 npos = floor(vTextureCoord * max(uViewSize, vec2(1.0)));\n"
                + "  float n = hash(npos + vec2(frameT, frameT * 1.7));\n"
                + "  vec3 snow = vec3(n);\n"
                + "  float noiseGain = amp * (0.18 * wobble + (1.0 - signal) * 0.95);\n"
                + "  vec3 col = mix(graded, snow, clamp(noiseGain, 0.0, 1.0));\n"
                + "  float line = 0.5 + 0.5 * sin(uv.y * max(uViewSize.y, 1.0) * 3.14159);\n"
                + "  col *= 1.0 - 0.10 * amp * line;\n"
                + "  vec2 cc = vTextureCoord - 0.5;\n"
                + "  col *= 1.0 - 0.35 * amp * dot(cc, cc);\n"
                + "  gl_FragColor = vec4(clamp(col, 0.0, 1.0), mainc.a);\n"
                + "}\n";
    }
}
