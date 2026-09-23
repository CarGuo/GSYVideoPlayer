# LUT 电影级调色首张卡片实施计划

## 1. 背景

现有 27 个滤镜都是把调色逻辑硬编码在 fragment shader 里（单点参数：亮度、对比、
饱和、棕褐等），换风格必须改 shader、发版，且难以表达“一整套曲线 + 色相 + 饱和”
的组合调色。

业界标准做法是 LUT（Look-Up Table）查找表：把每个像素 RGB 当作索引去查一张
预先在 Photoshop / DaVinci 里调好的映射表，得到目标颜色。一张表即一种电影风格
（青橙 teal-orange、赛博朋克、胶片柯达 / 富士）。运营只需热替换 LUT 文件即可
换风格，不改 shader、不发版。

本项目 GL 同源 GPUImage，ES 2.0 无 3D 纹理，因此采用 GPUImage 经典
`LookupFilter`：把 64³ 立方图按 8×8 展开成一张 512×512 的 2D 纹理采样。

## 2. 目标

1. 在单 pass 渲染链路上支持“效果自带 GL 纹理资产”（LUT），含 GL 线程安全的
   初始化 / 每帧绑定 / 释放生命周期。
2. 实现 `LookupEffect`：GPUImage 标准 512×512（8×8，每格 64×64，64³）查找。
3. 提供 identity（原样，用于自检）与 2 种强风格 LUT，demo 可热切换。

## 3. 非目标

- 不做 .cube / .3dl 文本 LUT 解析（只做 2D 展开 PNG）。
- 不做 HDR / 高精度 LUT（仍 RGBA8，linear 过滤插值）。
- 不做 LUT 强度混合滑杆 UI（shader 已内置 `uIntensity` 与原图 mix，构造可传强度，
  但 demo 暂不做滑杆）。

## 4. 技术方案

### 4.1 接口扩展（GSYVideoGLView）

现有 `ShaderInterface` 仅返回 shader 字符串，无法承载纹理。新增子接口：

```java
public interface TextureShaderInterface extends ShaderInterface {
    void onSurfaceReady(GLSurfaceView view);     // GL 线程：加载 Bitmap 上传纹理
    void onBindTextures(GLSurfaceView view, int program); // 每帧：绑定 + 设 sampler
    void onSurfaceRelease(GLSurfaceView view);   // GL 线程：删纹理
}
```

### 4.2 渲染器挂钩（GSYVideoGLViewSimpleRender）

- `onSurfaceCreated`：建 program / OES 纹理后，若当前效果是
  `TextureShaderInterface`，调 `onSurfaceReady`。
- `bindDrawFrameTexture`：OES 主纹理绑 `GL_TEXTURE0`；若效果为 texture 接口，
  调 `onBindTextures(mSurfaceView, mProgram)`，内部把 LUT 绑 `GL_TEXTURE1` 并把
  sampler 置 1。
- 换程序（`initDrawFrame` 中 program 重建）与 `setEffect`：旧 texture 效果的
  释放延迟到 GL 线程执行，避免在 UI 线程删 GL 对象；新效果首次绘制前 ready。
- `releaseAll`：在 GL 线程释放当前 texture 效果。

向后兼容：不实现该接口的普通 shader 效果路径完全不变。

### 4.3 LUT 布局规范（GPUImage 兼容）

512×512，8×8 格，每格 64×64，覆盖 64³：

- tile 序号 `t = round(b * 63)`，tile 行 `t / 8`、列 `t % 8`；
- tile 内 x 由 r 映射、y 由 g 映射；PNG 左上角为 b=0 的 tile，tile 内左列 r=0、
  顶行 g=0，向右 r 递增、向下 g 递增（GL 不翻转时 v=0 对应 PNG 顶行，无需翻转 Y）；
- 采样时对相邻两个 blue tile 做插值（GPUImage 标准 quad1 / quad2 混合，
  fract(blueColor) 为权重），tile 内坐标加半纹元并用 `(0.125 - 1/512)` 缩放
  防止采样溢到邻格；配合 LINEAR 过滤，在 64 级之间平滑，无明显色带。

shader 输入为 `samplerExternalOES`（单 pass 直接处理 OES），LUT 为
`sampler2D`。

### 4.4 资产生成

用本机 Python（numpy + pillow）离线生成到 `app/src/main/assets/lut/`：

- `identity.png`：恒等映射，作用后画面应完全不变（用于自检布局 / 翻转是否正确）；
- `teal_orange.png`：经典电影 teal-orange（暗部偏青、亮部偏暖橙，带轻微对比
  S 曲线）；
- `cyberpunk.png`：高饱和洋红 / 青、冷调、强对比。

生成即对 64×64×64 网格逐点做色彩变换后 bake 进表，运行时零额外算力。

## 5. 影响面

- 修改：`GSYVideoGLView`（新增内部接口）、`GSYVideoGLViewSimpleRender`（挂钩）。
- 新增：`LookupEffect`、assets/lut/*.png。
- demo：滤镜页把 LUT 作为可选渲染 / 滤镜入口；不影响既有 27 滤镜与多 pass 场景。

## 6. 风险与对策

- LUT y 轴翻转 / tile 顺序错导致颜色错乱：先用 identity.png 实拍，作用后必须与
  原图一致，否则修正翻转后再测风格表。
- UI 线程删 GL 纹理：释放统一延迟到 GL 线程（换 program / releaseAll）。
- LUT 加载失败（资产缺失 / Bitmap 为空）：回调渲染错误并回退 NoEffect，不崩溃。
- 64 级色带：blue 双 tile 插值 + GL_LINEAR。

## 7. 测试与验收

1. 构建 `:gsyVideoPlayer-java:assembleDebug` 与 `:app:assembleDebug`。
2. identity LUT：播放中作用后画面与无滤镜视觉一致（无变色 / 无翻转）。
3. teal_orange / cyberpunk：播放中呈现对应电影调色，暗部不糊、肤色风格化合理；
   连续截图大小随画面变化，确认作用于实时帧。
4. logcat 无 FATAL、无 GL_INVALID、无 shader 编译 / 链接错误、无纹理加载错误。
5. 回归：默认渲染与既有滤镜、多 pass / 金字塔 / Bloom 不受影响。

## 8. 执行记录

- **接口 / 渲染器**：在 `GSYVideoGLView` 新增 `TextureShaderInterface`
  （onSurfaceReady / onBindTextures / onSurfaceRelease）；在
  `GSYVideoGLViewSimpleRender` 的四处挂钩完成——`initDrawFrame` 内
  `ensureTextureEffectReady()`（program 重建后、首绘前在 GL 线程上传纹理）、
  `bindDrawFrameTexture` 内调 `onBindTextures`、`releaseAll` 与
  `onSurfaceCreated` 重置时释放旧纹理。非纹理效果路径零改动。
- **坐标修正（关键）**：实现初期误记“GL v=0 对应 PNG 底行”，据此做了 Y 翻转。
  复核 GPUImage 权威实现后确认：`GLUtils.texImage2D` 不翻转时 v=0 对应 PNG
  顶行。已把生成脚本（tile 内不再翻转）与 shader（改用 GPUImage 标准
  quad1/quad2、无 Y flip、`(0.125-1/512)` 防溢色）一并修正为业界标准布局，
  保证用户后续可直接替换网上下载的标准 LUT PNG。
- **资产**：离线脚本生成 `assets/lut/identity.png`、`teal_orange.png`、
  `cyberpunk.png`；identity 对网格自检最大误差 0.0019（8-bit 量化内，正常）。
- **暂停态排查（关键认知）**：GL 视图为 `RENDERMODE_WHEN_DIRTY`，视频暂停 /
  播完后无新帧，此时切换效果只更新了普通 View 覆盖的文字标签，GL 画面不重绘，
  导致截图视频区像素不变——属预期，并非 shader 失效。故最终全部在**播放中**
  采帧验证。
- **设备验证（emulator-5554，播放中采帧）**：
  - identity：画面与无滤镜一致，无变色 / 无翻转；
  - teal_orange：进度 00:02 帧，草地偏青蓝、主体偏暖橙，风格明确；
  - cyberpunk：进度 00:01 帧，强品红 / 紫粉、高对比；
  - 热替换即时生效；logcat 无 FATAL、无 shader 编译 / 链接错误、无纹理加载错误。
- 构建：`:app:assembleDebug` 成功，`:app:installDebug` 安装通过。

**状态：已完成。**
