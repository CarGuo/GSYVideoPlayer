package com.shuyu.gsyvideoplayer.render.effect;

import android.opengl.GLSurfaceView;

import com.shuyu.gsyvideoplayer.render.view.GSYVideoGLView.ShaderInterface;

/**
 * Attempts to auto-fix the video based on histogram equalization.
 *
 * @author sheraz.khilji
 */
public class AutoFixEffect implements ShaderInterface {
    private float scale;

    /**
     * Initialize Effect
     *
     * @param scale Float, between 0 and 1. Zero means no adjustment, while 1
     *              indicates the maximum amount of adjustment.
     */
    public AutoFixEffect(float scale) {
        if (scale < 0.0f)
            scale = 0.0f;
        if (scale > 1.0f)
            scale = 1.0f;

        this.scale = scale;
    }

    @Override
    public String getShader(GLSurfaceView mGlSurfaceView) {

        String shader = "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"
                + "uniform samplerExternalOES tex_sampler_0;\n"
                + "uniform samplerExternalOES tex_sampler_1;\n"
                + "uniform samplerExternalOES tex_sampler_2;\n"
                + " float scale;\n" + " float shift_scale;\n"
                + " float hist_offset;\n" + " float hist_scale;\n"
                + " float density_offset;\n" + " float density_scale;\n"
                + "varying vec2 vTextureCoord;\n" + "void main() {\n"
                + "  shift_scale = "
                + (1.0f / 256f)
                + ";\n"
                + "  hist_offset = "
                + (0.5f / 766f)
                + ";\n"
                + "  hist_scale = "
                + (765f / 766f)
                + ";\n"
                + "  density_offset = "
                + (0.5f / 1024f)
                + ";\n"
                + "  density_scale = "
                + (1023f / 1024f)
                + ";\n"
                + "  scale = "
                + scale
                + ";\n"
                + "  const vec3 weights = vec3(0.33333, 0.33333, 0.33333);\n"
                + "  vec4 color = texture2D(tex_sampler_0, vTextureCoord);\n"
                + "  float energy = dot(color.rgb, weights);\n"
                + "  float mask_value = energy - 0.5;\n"
                + "  float alpha;\n"
                + "  if (mask_value > 0.0) {\n"
                + "    alpha = (pow(2.0 * mask_value, 1.5) - 1.0) * scale + 1.0;\n"
                + "  } else { \n"
                + "    alpha = (pow(2.0 * mask_value, 2.0) - 1.0) * scale + 1.0;\n"
                + "  }\n"
                + "  float index = energy * hist_scale + hist_offset;\n"
                + "  vec4 temp = texture2D(tex_sampler_1, vec2(index, 0.5));\n"
                + "  float value = temp.g + temp.r * shift_scale;\n"
                + "  index = value * density_scale + density_offset;\n"
                + "  temp = texture2D(tex_sampler_2, vec2(index, 0.5));\n"
                + "  value = temp.g + temp.r * shift_scale;\n"
                + "  float dst_energy = energy * alpha + value * (1.0 - alpha);\n"
                + "  float max_energy = energy / max(color.r, max(color.g, color.b));\n"
                + "  if (dst_energy > max_energy) {\n"
                + "    dst_energy = max_energy;\n"
                + "  }\n"
                + "  if (energy == 0.0) {\n"
                + "    gl_FragColor = color;\n"
                + "  } else {\n"
                + "    gl_FragColor = vec4(color.rgb * dst_energy / energy, color.a);\n"
                + "  }\n" + "}\n";

        return shader;

    }
}
