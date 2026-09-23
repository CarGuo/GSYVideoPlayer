# Bloom（辉光）首张卡片实施计划

## 1. 背景

FBO 多 pass 管线与降采样金字塔已落地，能在多分辨率纹理间做 ping-pong。
Bloom 是最能体现该管线价值的经典效果：提取画面高亮区域 → 降采样金字塔做大范围
弥散模糊 → 与原始画面叠加，产生“亮处发光 / 光晕溢出”的观感，常用于 HDR 拟真、
梦幻氛围、UI 高光等。

当前管线每个 pass 只有一个输入纹理，而 Bloom 的最终合成 pass 必须同时采样
“原始场景”和“模糊后的亮部”，这是本卡片要解决的核心扩展点。

## 2. 目标

1. 在不破坏现有多 pass / 金字塔效果的前提下，让指定 pass 能额外采样
   “原始基础场景纹理”（base FBO）。
2. 实现可配置阈值与强度的 Bloom：bright-pass 亮部提取 + 金字塔 Kawase
   降采样 / 上采样模糊 + 最终加性合成。
3. demo 增加“Bloom 辉光”渲染场景，可在播放中实时观察。

## 3. 非目标

- 不做真正的浮点 HDR 管线 / 色调映射（仍为 RGBA8 LDR，合成后 clamp 到 1）。
- 不做镜头色散、污垢透镜（lens dirt）、十字星芒等进阶 Bloom 变体。
- 不暴露运行时滑杆调参（阈值 / 强度先写死或构造参数）。

## 4. 技术方案

### 4.1 复合接口（新增）

`GSYVideoGLViewCompositeInterface extends GSYVideoGLViewPyramidInterface`：

```java
/** 某 pass 若需额外采样“原始基础场景”，返回其 sampler 名；否则返回 null。 */
String getBaseSceneSampler(int pass);
```

- 只要 effect 实现该接口，渲染器在整帧内保留 base FBO 不被池回收（`inUse`）。
- `buildPassPrograms()` 时查询该 sampler 的 location，存入 `PassProgram`。
- `drawEffectPass()` 时把 base FBO 绑到 `GL_TEXTURE1`，sampler 置 1；
  主输入仍在 `GL_TEXTURE0`。

### 4.2 渲染器改动（GSYVideoGLViewMultiPassRender）

- `renderPasses()`：base FBO 转换后，普通流程会在第一个 effect pass 后
  `markFree(baseFbo)`；对 composite effect 跳过对 base FBO 的释放，保证最终
  合成 pass 仍可采样。
- `PassProgram` 增加 `baseSceneUniformHandle`。
- `drawEffectPass()` 增加可选的 base 纹理绑定（仅当 handle 有效且 base FBO 存在）。

向后兼容：不实现该接口的效果完全无变化。

### 4.3 Bloom 效果（新增 BloomEffect）

`levels = 3`（共 `2*levels = 6` pass）：

| pass | 作用 | 输出 scale |
|------|------|-----------|
| 0 | bright-pass 亮部提取（亮度阈值 + soft knee） | 1/2 |
| 1 | Kawase down（4-tap） | 1/4 |
| 2 | Kawase down（4-tap，到底） | 1/8 |
| 3 | Kawase up（8-tap） | 1/4 |
| 4 | Kawase up（8-tap） | 1/2 |
| 5 | 合成：`base + bloom*intensity`，clamp | 上屏 |

- bright-pass 直接输出半分辨率，借 GL_LINEAR 获得一次免费预模糊并省填充率。
- down/up 复用与金字塔迭代模糊一致的 4 / 8-tap Kawase kernel。
- 合成 pass：`sTexture` = 模糊亮部（半分辨率线性放大），
  `uOriginalTexture` = 原始全分辨率场景。
- 参数：`threshold`（默认 ~0.7）、`intensity`（默认 ~1.0）、soft knee 宽度。

## 5. 影响面

- 新增：复合接口、BloomEffect。
- 修改：多 pass 渲染器（额外纹理绑定 + base FBO 保留）。
- demo：新增渲染场景，不影响既有 27 个单 pass 滤镜与其余场景。

## 6. 风险与对策

- base FBO 被提前回收导致合成采样到错误内容：composite effect 整帧保留 base
  FBO，标记 `inUse`，末 pass 后再统一由池重置。
- 亮部阈值过高中年画面无辉光 / 过低整片泛白：提供 soft knee 平滑截断，默认值
  在设备侧实拍校准。
- LDR 加性叠加溢出过曝：合成后 clamp 到 1，并用 intensity 控制强度。

## 7. 测试与验收

1. 构建 `:gsyVideoPlayer-java:assembleDebug` 与 `:app:assembleDebug`。
2. 设备侧播放中切到“Bloom 辉光”：
   - 高亮区域出现明显光晕 / 溢出，暗部保持不泛白；
   - 连续截图大小随画面变化，确认作用于实时帧；
   - logcat 无 FATAL、无 GL_INVALID、无 FBO incomplete、无 shader 编译 / 链接错误。
3. 回归：默认渲染清晰；金字塔迭代模糊、多 Pass 高斯仍正常（验证渲染器改动未
   影响非 composite 路径）。

## 8. 执行记录

> 状态：已完成（2026-09-23）

### 8.1 实现

1. `GSYVideoGLViewCompositeInterface`：扩展金字塔接口，新增
   `getBaseSceneSampler(pass)`，声明某 pass 额外采样原始基础场景所用的 sampler 名。
2. `GSYVideoGLViewMultiPassRender`：
   - 对 composite 效果整帧保留 base FBO（不随中间 pass 回收）；
   - `PassProgram` 新增 `baseSceneUniformHandle`，`buildPassPrograms()` 查询
     sampler location；
   - `drawEffectPass()` 主输入绑 `GL_TEXTURE0`，base 场景绑 `GL_TEXTURE1`；
   - 不实现该接口的效果路径完全不变。
3. `BloomEffect`（levels=3，共 6 pass）：
   - pass0 bright-pass：按亮度 + soft knee（`smoothstep`）提取高亮，直接输出
     半分辨率，借 GL_LINEAR 免费预模糊并降低填充率；
   - pass1/2 Kawase down（4-tap）到 1/8；pass3/4 Kawase up（8-tap）回半分辨率；
   - pass5 合成：`original + bloom * intensity`，clamp 到 1，保留原 alpha；
   - 参数 threshold=0.7、knee=0.15、intensity=1.0。
4. `DetailFilterActivity`：新增“Bloom辉光”场景（第 8 个），该场景禁用单 pass
   滤镜切换（含按钮置灰）。

### 8.2 验证（emulator-5554）

- IDE 诊断：无错误。
- 构建：`:gsyVideoPlayer-java:assembleDebug` 与 `:app:assembleDebug` 成功。
- 设备侧播放中验证：
  - 高亮区域（测试片段白色主角）呈现明显光晕 / 溢出，暗部（绿树、草地、蓝天）
    未泛白、层次保留；
  - 连续截图文件大小随画面变化，确认作用于实时播放帧；
  - logcat 无 `FATAL`、无 `GL_INVALID_*`、无 FBO incomplete、无 shader
    compile/link error、无 “FBO setup failed”。
- 回归：
  - 切回“默认渲染”画面恢复清晰，无崩溃 / GL 错误；
  - 再切“金字塔迭代模糊”模糊与播放均正常，确认渲染器改动对非 composite
    （单输入）路径无回归。
- 证据目录：`app/test_evidence/bloom-card/`
  - `01~03_playing_bloom*.png`：播放中 Bloom
  - `04_regression_default_clear.png`：默认渲染清晰
  - `05_regression_pyramid_blur.png`：金字塔迭代模糊回归
