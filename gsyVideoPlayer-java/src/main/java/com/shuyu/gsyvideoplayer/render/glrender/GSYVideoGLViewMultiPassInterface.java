package com.shuyu.gsyvideoplayer.render.glrender;

/**
 * 多 pass 滤镜效果。渲染器负责 FBO 编排，实现方负责声明每个中间 pass 的
 * fragment shader（均采样 sampler2D 普通纹理），以及在 pass 绘制前设置自定义 uniform。
 * <p>
 * 流程：OES 输入转换 pass 写入 FBO[0]；随后 getPassCount() 个 pass 依次读取
 * FBO[i] 写入 FBO[i+1]，最后一个 pass 的输出上屏。
 */
public interface GSYVideoGLViewMultiPassInterface {

    /**
     * 中间处理 pass 数量（不含 OES 输入转换 pass）。
     */
    int getPassCount();

    /**
     * 指定 pass 的 fragment shader 源码。采样器为 sampler2D，输入坐标 vTextureCoord。
     *
     * @param pass 从 0 开始
     */
    String getPassShader(int pass);

    /**
     * 在指定 pass 的 draw 之前回调，实现方可在此设置自己的 uniform
     * （此时 program 已 use，输入纹理已绑定到 TEXTURE0）。
     *
     * @param program       当前 pass 使用的 program
     * @param pass          当前 pass 序号
     * @param sourceWidth   输入 FBO 宽
     * @param sourceHeight  输入 FBO 高
     */
    void onBindPassUniform(int program, int pass, int sourceWidth, int sourceHeight);

    /**
     * 释放实现方持有的资源（可选）。
     */
    void release();
}
