package com.shuyu.gsyvideoplayer.render.effect;

import android.opengl.GLSurfaceView;

import com.shuyu.gsyvideoplayer.render.view.GSYVideoGLView.ShaderInterface;

/**
 * CRT 显像管效果：屏幕微弯曲、水平扫描线、RGB 像素栅格、边缘色差、轻微闪烁与暗角。
 * <p>
 * 依赖渲染器每帧自动注入的 {@code uTime}（秒）与 {@code uViewSize}。
 */
public class CrtEffect implements ShaderInterface {

    private final float strength;

    /**
     * @param strength 0~1，CRT 质感强度
     */
    public CrtEffect(float strength) {
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
                + "void main() {\n"
                + "  float amp = " + strength + ";\n"
                + "  vec2 aspect = vec2(max(uViewSize.x / max(uViewSize.y, 1.0), 1.0), 1.0);\n"
                + "  vec2 cc = (vTextureCoord - 0.5) * aspect;\n"
                + "  float r2 = dot(cc, cc);\n"
                + "  vec2 bend = cc * (1.0 + 0.16 * amp * r2);\n"
                + "  vec2 uv = bend / aspect + 0.5;\n"
                + "  if (uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0) {\n"
                + "    gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);\n"
                + "    return;\n"
                + "  }\n"
                + "  vec2 px = vec2(1.0 / max(uViewSize.x, 1.0),\n"
                + "                1.0 / max(uViewSize.y, 1.0));\n"
                + "  float ca = 0.0022 * amp * r2;\n"
                + "  float rr = texture2D(sTexture, uv + vec2(ca, 0.0)).r;\n"
                + "  vec4 mid = texture2D(sTexture, uv);\n"
                + "  float bb = texture2D(sTexture, uv - vec2(ca, 0.0)).b;\n"
                + "  vec3 col = vec3(rr, mid.g, bb);\n"
                + "  float line = 0.5 + 0.5 * sin(uv.y * uViewSize.y * 3.14159);\n"
                + "  col *= 1.0 - 0.22 * amp * line;\n"
                + "  float colx = uv.x * uViewSize.x;\n"
                + "  float gateR = 0.5 + 0.5 * sin(colx * 6.28318 / 3.0);\n"
                + "  float gateG = 0.5 + 0.5 * sin((colx + 1.0) * 6.28318 / 3.0);\n"
                + "  float gateB = 0.5 + 0.5 * sin((colx + 2.0) * 6.28318 / 3.0);\n"
                + "  col *= 1.0 - amp * 0.10 * vec3(gateR, gateG, gateB);\n"
                + "  float flicker = 1.0 + 0.02 * amp * sin(uTime * 31.0);\n"
                + "  float vig = 1.0 - 0.5 * amp * r2;\n"
                + "  col *= flicker * vig;\n"
                + "  gl_FragColor = vec4(clamp(col, 0.0, 1.0), mid.a);\n"
                + "}\n";
    }
}
