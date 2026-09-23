package com.shuyu.gsyvideoplayer.render.glrender;

/**
 * 金字塔（多分辨率）多 pass 效果。在 {@link GSYVideoGLViewMultiPassInterface}
 * 基础上，允许每个中间 pass 声明自己的输出分辨率（相对基础 view 尺寸的缩放系数），
 * 从而支持逐级降采样 / 上采样的金字塔编排。
 * <p>
 * 渲染器依据 {@link #getPassOutputScale(int)} 为每个 pass 选择对应尺寸的 FBO；
 * 未实现本接口的普通多 pass 效果默认按 1.0（与基础分辨率一致）处理。
 */
public interface GSYVideoGLViewPyramidInterface extends GSYVideoGLViewMultiPassInterface {

    /**
     * 指定 pass 的输出分辨率相对基础（view）分辨率的缩放系数，必须 &gt; 0。
     * 例如 0.5 表示宽高均为基础的一半。
     * <p>
     * 最后一个上屏 pass 的输出目标固定为屏幕，其返回值会被忽略。
     *
     * @param pass 从 0 开始
     */
    float getPassOutputScale(int pass);
}
