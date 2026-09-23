package com.shuyu.gsyvideoplayer.render.glrender;

/**
 * 需要在指定 pass 额外采样“原始基础场景纹理”的复合多 pass 效果。
 * <p>
 * 普通多 pass / 金字塔效果的每个 pass 只能拿到上一个 pass 的输出；而像 Bloom
 * 这类效果，其最终合成 pass 必须同时拿到“原始画面”和“处理结果”。实现本接口后：
 * <ul>
 *   <li>渲染器在整帧内保留输入转换得到的 base FBO，不会在中间 pass 后回收；</li>
 *   <li>对 {@link #getBaseSceneSampler(int)} 返回非空 sampler 名的 pass，渲染器会
 *       把 base FBO 绑到一个额外纹理单元（GL_TEXTURE1）并设置该 sampler；</li>
 *   <li>该 pass 的主输入（上一个 pass 输出）仍绑定在 GL_TEXTURE0。</li>
 * </ul>
 */
public interface GSYVideoGLViewCompositeInterface extends GSYVideoGLViewPyramidInterface {

    /**
     * 指定 pass 是否需要额外采样原始基础场景。
     *
     * @param pass 从 0 开始
     * @return 该 pass shader 中用于采样原始场景的 sampler uniform 名；
     *         若该 pass 不需要，返回 null
     */
    String getBaseSceneSampler(int pass);
}
