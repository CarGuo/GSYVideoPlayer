package com.shuyu.gsyvideoplayer.render.glrender;

import android.opengl.GLES20;

import com.shuyu.gsyvideoplayer.utils.Debuger;

/**
 * 离屏渲染目标：一个 framebuffer + 一张 GL_TEXTURE_2D 颜色附件。
 * 仅支持 OpenGL ES 2.0，颜色格式固定为 RGBA / UNSIGNED_BYTE。
 */
public class GLFrameBuffer {

    private int frameBufferId;

    private int textureId;

    private int width;

    private int height;

    private boolean ready;

    public GLFrameBuffer() {
    }

    /**
     * 按指定尺寸创建 FBO 与颜色纹理。若已存在则先释放。
     *
     * @return true 表示 framebuffer 完整可用
     */
    public boolean setup(int targetWidth, int targetHeight) {
        if (ready && width == targetWidth && height == targetHeight) {
            return true;
        }
        release();
        if (targetWidth <= 0 || targetHeight <= 0) {
            return false;
        }

        int[] ids = new int[1];
        GLES20.glGenFramebuffers(1, ids, 0);
        frameBufferId = ids[0];

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        textureId = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                targetWidth, targetHeight, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, textureId, 0);

        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        boolean complete = status == GLES20.GL_FRAMEBUFFER_COMPLETE;
        if (!complete) {
            Debuger.printfError("GLFrameBuffer incomplete: 0x"
                    + Integer.toHexString(status));
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        if (complete) {
            width = targetWidth;
            height = targetHeight;
            ready = true;
        } else {
            release();
        }
        return complete;
    }

    /**
     * 绑定为当前渲染目标。
     */
    public void bind() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId);
        GLES20.glViewport(0, 0, width, height);
    }

    /**
     * 把颜色纹理绑定到指定纹理单元作为采样输入。
     */
    public void bindTexture(int activeTextureUnit) {
        GLES20.glActiveTexture(activeTextureUnit);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
    }

    public int getTextureId() {
        return textureId;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isReady() {
        return ready;
    }

    public void release() {
        if (frameBufferId != 0) {
            GLES20.glDeleteFramebuffers(1, new int[]{frameBufferId}, 0);
            frameBufferId = 0;
        }
        if (textureId != 0) {
            GLES20.glDeleteTextures(1, new int[]{textureId}, 0);
            textureId = 0;
        }
        ready = false;
        width = 0;
        height = 0;
    }
}
