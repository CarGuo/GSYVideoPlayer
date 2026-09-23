package com.shuyu.gsyvideoplayer.render.effect;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.GLSurfaceView;

import com.shuyu.gsyvideoplayer.render.view.GSYVideoGLView;
import com.shuyu.gsyvideoplayer.utils.Debuger;

import java.io.InputStream;

/**
 * 基于 LUT 查找表的电影级调色（GPUImage LookupFilter 同款做法）。
 * <p>
 * 加载一张 512x512、8x8 展开（每格 64x64，覆盖 64^3）的 LUT 图，把视频像素
 * RGB 映射到表中。更换 LUT 文件即可热替换整体调色风格，无需改 shader、无需发版。
 * ES 2.0 无 3D 纹理，故用 2D 展开图；shader 对 blue 维度相邻两个格子做插值，
 * 配合 GL_LINEAR 在 64 级之间平滑。
 * <p>
 * LUT 采用业界标准排布：PNG 左上角为 blue=0 的格子；格子内左列 r=0、顶行 g=0，
 * 向右 r 递增、向下 g 递增；从左到右、从上到下 blue 递增。可直接替换网上下载的
 * 标准 LUT 文件（命名为 .png 放入 assets）。
 * <p>
 * 输入视频为 samplerExternalOES（TEXTURE0），LUT 为 sampler2D（TEXTURE1）。
 */
public class LookupEffect implements GSYVideoGLView.TextureShaderInterface {

    private static final String SHADER =
            "#extension GL_OES_EGL_image_external : require\n"
            + "precision highp float;\n"
            + "varying vec2 vTextureCoord;\n"
            + "uniform samplerExternalOES sTexture;\n"
            + "uniform sampler2D uLookupTexture;\n"
            + "uniform float uIntensity;\n"
            + "void main() {\n"
            + "  vec4 textureColor = texture2D(sTexture, vTextureCoord);\n"
            + "  float blueColor = textureColor.b * 63.0;\n"
            + "  vec2 quad1;\n"
            + "  quad1.y = floor(floor(blueColor) / 8.0);\n"
            + "  quad1.x = floor(blueColor) - quad1.y * 8.0;\n"
            + "  vec2 quad2;\n"
            + "  quad2.y = floor(ceil(blueColor) / 8.0);\n"
            + "  quad2.x = ceil(blueColor) - quad2.y * 8.0;\n"
            + "  vec2 texPos1;\n"
            + "  texPos1.x = (quad1.x * 0.125) + 0.5 / 512.0"
            + "               + ((0.125 - 1.0 / 512.0) * textureColor.r);\n"
            + "  texPos1.y = (quad1.y * 0.125) + 0.5 / 512.0"
            + "               + ((0.125 - 1.0 / 512.0) * textureColor.g);\n"
            + "  vec2 texPos2;\n"
            + "  texPos2.x = (quad2.x * 0.125) + 0.5 / 512.0"
            + "               + ((0.125 - 1.0 / 512.0) * textureColor.r);\n"
            + "  texPos2.y = (quad2.y * 0.125) + 0.5 / 512.0"
            + "               + ((0.125 - 1.0 / 512.0) * textureColor.g);\n"
            + "  vec4 newColor1 = texture2D(uLookupTexture, texPos1);\n"
            + "  vec4 newColor2 = texture2D(uLookupTexture, texPos2);\n"
            + "  vec4 newColor = mix(newColor1, newColor2, fract(blueColor));\n"
            + "  gl_FragColor = mix(textureColor,"
            + "                    vec4(newColor.rgb, textureColor.a), uIntensity);\n"
            + "}\n";

    private final String assetPath;

    private final float intensity;

    private int mLutTextureId;

    public LookupEffect(String assetPath) {
        this(assetPath, 1.0f);
    }

    public LookupEffect(String assetPath, float intensity) {
        this.assetPath = assetPath;
        this.intensity = intensity;
    }

    @Override
    public String getShader(GLSurfaceView glSurfaceView) {
        return SHADER;
    }

    @Override
    public void onSurfaceReady(GLSurfaceView glSurfaceView) {
        Bitmap bitmap = loadBitmap(glSurfaceView);
        if (bitmap == null) {
            Debuger.printfError("LookupEffect LUT load failed: " + assetPath);
            return;
        }
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        mLutTextureId = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mLutTextureId);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        bitmap.recycle();
    }

    @Override
    public void onBindTextures(GLSurfaceView glSurfaceView, int program) {
        int intensityHandle = GLES20.glGetUniformLocation(program, "uIntensity");
        if (intensityHandle != -1) {
            GLES20.glUniform1f(intensityHandle, intensity);
        }
        if (mLutTextureId == 0) {
            return;
        }
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mLutTextureId);
        int lookupHandle = GLES20.glGetUniformLocation(program, "uLookupTexture");
        if (lookupHandle != -1) {
            GLES20.glUniform1i(lookupHandle, 1);
        }
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    }

    @Override
    public void onSurfaceRelease(GLSurfaceView glSurfaceView) {
        if (mLutTextureId != 0) {
            GLES20.glDeleteTextures(1, new int[]{mLutTextureId}, 0);
            mLutTextureId = 0;
        }
    }

    private Bitmap loadBitmap(GLSurfaceView glSurfaceView) {
        AssetManager assets = glSurfaceView.getContext().getAssets();
        InputStream inputStream = null;
        try {
            inputStream = assets.open(assetPath);
            return BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            Debuger.printfError("LookupEffect open LUT error: " + e.getMessage());
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
