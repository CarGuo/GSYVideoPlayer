# FBO 降采样金字塔 + 迭代模糊实施计划

> 创建时间：2026-09-22
> 范围：OpenGL ES 2.0，在既有多 pass 管线上支持 **pass 间分辨率变化**，构建降采样金字塔，并落地双滤波（Kawase 式 down/up）迭代模糊
> 前置：[FBO_MULTIPASS_FIRST_CARD_PLAN.md](FBO_MULTIPASS_FIRST_CARD_PLAN.md)
> 完成标准：构建、静态检查、设备侧播放中验证通过，证据持久化到 `app/test_evidence/`

## 1. 背景与目标

上一卡的多 pass 管线只用两个**等尺寸** FBO ping-pong，所有中间 pass 都按 view
1:1 分辨率执行。它无法表达逐级缩小 / 放大的金字塔效果，大半径模糊只能靠加大
shader 采样半径，开销高且边缘易出问题。

本卡目标：

1. 让每个中间 pass 可**声明自己的输出分辨率（相对基础尺寸的缩放系数）**。
2. 渲染器维护一个**多尺寸 FBO 池**，按 pass 需求取放、跨分辨率编排，尺寸变化时重建。
3. 利用金字塔在低分辨率上做模糊，用很少的 tap 得到大范围、平滑的模糊（迭代模糊）。
4. 落地 **双滤波（Kawase dual-filter）**：逐级 down（模糊+缩小）再逐级 up（模糊+放大）。
5. 保持向后兼容：现有「多Pass高斯」场景与 `GSYVideoGLViewMultiPassInterface` 用法不变。

## 2. 非目标

1. 不实现 Bloom、动态模糊背景、景深（后续卡复用金字塔）。
2. 不引入第三方依赖，不升级 OpenGL ES 版本。
3. 不做非线性 / 各向异性缩放；金字塔统一宽高按 2 的幂次减半。
4. 不在播放过程中热切换渲染器（沿用“渲染器播放前设置”的约束）。
5. 不处理金字塔底层以外的 mipmap（每级 FBO 本身不生成 mip chain，过滤用 LINEAR）。

## 3. 渲染管线设计（以 3 级为例）

```
解码帧 OES
   │ 转换 pass（OES→2D）
   ▼
FBO 1.0  (基础分辨率)
   │ down0：4-tap，uDirection 无，scale=0.5
   ▼
FBO 0.5
   │ down1：scale=0.25
   ▼
FBO 0.25
   │ down2：scale=0.125
   ▼
FBO 0.125
   │ up0：8-tap，scale=0.25
   ▼
FBO 0.25
   │ up1：scale=0.5
   ▼
FBO 0.5
   │ up2：scale=1.0 → 直接上屏
   ▼
默认 framebuffer（屏幕）
```

- 总 pass 数 = `2 * level`（L 个 down + L 个 up），最后一个 up 直接上屏。
- down 逐级缩小并模糊，up 逐级放大并模糊，等效模糊半径随级数显著增大，但每
  pass 仅 4 / 8 tap。

### 3.1 FBO 池复用

同一帧内任意时刻只有“上一 pass 输出（当前输入）”和“当前 pass 输出”两个目标
在逻辑上存活。渲染器维护 FBO 池：

- `acquire(w,h)`：优先取一个**空闲且尺寸匹配**的 FBO，否则新建并 `setup`。
- pass 绘制命令入队后，即可把其输入 FBO 标记为空闲（后续复用发生在更靠后的 GL
  命令，顺序天然正确，无需 `glFinish`）。
- 下降段建出的 0.5 / 0.25 等 FBO，在上升段被同尺寸 pass 直接复用。
- 峰值 FBO 数量 ≈ 金字塔层数 + 1，而非 pass 数。
- 基础分辨率变化（surface 尺寸变化 / 重建）时，整体释放池并重建，避免孤儿 FBO。

## 4. 文件变更清单

| 序号 | 文件 | 类型 | 内容 |
|---|---|---|---|
| 1 | `render/glrender/GSYVideoGLViewPyramidInterface.java` | 新增 | 扩展多 pass 接口：`getPassOutputScale(pass)` 声明每 pass 输出缩放 |
| 2 | `render/glrender/GSYVideoGLViewMultiPassRender.java` | 修改 | 固定双 FBO → 多尺寸 FBO 池；按金字塔接口计算每 pass 目标分辨率并跨分辨率编排 |
| 3 | `render/effect/IterativeBlurPyramidEffect.java` | 新增 | Kawase 双滤波：L down（4-tap）+ L up（8-tap），层数可配 |
| 4 | `app/.../DetailFilterActivity.java` | 修改 | 新增“金字塔迭代模糊”渲染场景，禁用该场景的单 pass 滤镜切换 |

## 5. 技术要点

### 5.1 金字塔接口

```java
public interface GSYVideoGLViewPyramidInterface extends GSYVideoGLViewMultiPassInterface {
    /** 当前 pass 输出相对基础（view）分辨率的缩放系数，&gt;0；最终上屏 pass 会忽略其目标尺寸 */
    float getPassOutputScale(int pass);
}
```

渲染器对普通 `GSYVideoGLViewMultiPassInterface` 默认按 scale=1.0 处理，从而
兼容现有高斯效果（此时池行为等价于原来的双 FBO）。

### 5.2 目标分辨率

- 基础：`mCurrentViewWidth / mCurrentViewHeight`。
- 第 i 个 pass：`max(1, round(base * scale_i))`。
- viewport 由 `GLFrameBuffer.bind()` 按自身尺寸设置；上屏 pass 恢复基础 viewport。

### 5.3 Kawase 双滤波 shader

- down：以**输入** texelSize 的 1.5 倍偏移取 4 角 tap 平均：
  `uv ± 1.5*texel` 的 4 个对角采样，`*0.25`。
- up：以 0.5 倍**输入** texelSize 为单位，取内 4 点（±1）与外 4 点（±3）共
  8 tap 平均 `*0.125`。
- 两 pass 均使用 `sampler2D sTexture`、`vec2 uTexelSize`，保留 alpha；
  `CLAMP_TO_EDGE` 保证边缘不渗色。

### 5.4 资源生命周期

- surface 创建：池清空、program 重建标志置位。
- 每帧：池中 FBO 按各自尺寸 `setup`（内部判断尺寸不变则复用，变化则重建）。
- 基础尺寸变化：整池 release 后清空，下一帧按新尺寸重新 acquire。
- `releaseAll`：释放池中所有 FBO、program，并回调 effect.release。

## 6. 测试与验收计划

### 6.1 本地静态验证

- `./gradlew :gsyVideoPlayer-java:assembleDebug`
- `./gradlew :app:assembleDebug`
- `./gradlew :gsyVideoPlayer-java:testDebugUnitTest`
- IDE 诊断无错误。

### 6.2 设备侧验证（emulator-5554）

1. 启动 App → 进入“滤镜”页面。
2. 切到“金字塔迭代模糊”，在**播放过程中**观察并连续截图。
3. logcat 检查：
   - 无 `FATAL EXCEPTION`；
   - 无 `GL_INVALID_OPERATION / GL_INVALID_FRAMEBUFFER_OPERATION`；
   - 无 FBO incomplete、无 shader compile/link error。
4. 图像判定：整体大范围平滑模糊，强于/不同于单层高斯；无单向拖影、黑边、
   撕裂、边缘渗色，alpha 正常；无明显性能暴跌。
5. 回归：再切回“多Pass高斯”与“默认渲染”，确认旧路径正常、无崩溃/GL 错误。
6. 证据保存到 `app/test_evidence/pyramid-iterative-blur-card/`。

### 6.3 完成判定

- 代码完成、构建与静态检查通过；
- 设备侧播放中验证通过，金字塔各级 FBO 完整、复用正常；
- 旧场景回归通过；
- 证据保存且计划文件记录最终结果。

## 7. 风险与回退

- 风险：取整后某级尺寸为奇数/非 2 幂，UV 对不齐产生轻微抖动。对策：
  down/up 使用基于输入 texelSize 的偏移，LINEAR 过滤，输出尺寸统一
  `max(1, round())`。
- 风险：FBO 复用过早导致读写冲突。对策：复用只发生在“绘制已入队”之后的后续
  pass，GL 命令顺序保证先采样后写回；不使用跨帧/异步持有。
- 风险：基础尺寸变化遗留孤儿 FBO。对策：检测 base 变化即整池释放重建。
- 回退：纯新增 + demo 场景接入；删除场景入口与新增文件、还原渲染器即可回退。

## 8. 执行记录

> 状态：已完成（2026-09-22）

### 8.1 实现

1. `GSYVideoGLViewPyramidInterface`：扩展多 pass 接口，新增
   `getPassOutputScale(pass)` 声明每 pass 输出相对基础分辨率的缩放。
2. `GSYVideoGLViewMultiPassRender`：固定双 FBO 改为**多尺寸 FBO 池**：
   - `acquireFbo(w,h)` 优先复用空闲且尺寸匹配的 FBO，否则新建；
   - 每个非末 pass 按金字塔缩放计算目标分辨率，跨分辨率编排，末 pass 上屏；
   - 基础尺寸变化即整池释放重建，避免孤儿 FBO；
   - 普通多 pass 接口默认 scale=1.0，行为等价于原双 FBO，向后兼容。
3. `IterativeBlurPyramidEffect`：Kawase 双滤波，3 级（共 6 pass）：
   - down 每级 4 角 tap（输入 texelSize × 1.5）平均，逐级缩小一半并模糊；
   - up 每级 8 tap（内 4 ±1、外 4 ±3）平均，逐级放大一倍并模糊，末 pass 上屏；
   - 等效模糊范围随级数显著增大，单 pass 仅 4 / 8 次采样。
4. `DetailFilterActivity`：新增“金字塔迭代模糊”场景（第 7 个），该场景禁用
   单 pass 滤镜切换。

### 8.2 验证（emulator-5554）

- IDE 诊断：无错误。
- 构建：`:gsyVideoPlayer-java:assembleDebug` 与 `:app:assembleDebug` 成功。
- 设备侧播放中验证：
  - 切到“金字塔迭代模糊”后重播，00:02 等时刻画面呈现大范围、弥散式平滑模糊，
    无单向拖影、黑边、撕裂、边缘渗色，alpha 正常；
  - 连续截图文件大小随画面变化，确认作用于实时播放帧；
  - logcat 无 `FATAL`、无 `GL_INVALID_*`、无 FBO incomplete、无 shader
    compile/link error、无 “FBO setup failed”。
- 回归：
  - 切回“默认渲染”画面恢复清晰，无崩溃 / GL 错误；
  - 再切到旧“多Pass高斯”，池化重构下模糊效果与播放均正常，确认非金字塔
    接口（scale=1.0）路径无回归。
- 证据目录：`app/test_evidence/pyramid-iterative-blur-card/`
  - `01~03_playing_pyramid_blur*.png`：播放中金字塔迭代模糊
  - `04_regression_default_clear.png`：默认渲染清晰
  - `05_regression_multipass_gaussian.png`：旧多 Pass 高斯回归

## 9. 后续卡片

1. Bloom（金字塔低分辨率层做亮部提取 + 模糊，回升合成）。
2. 动态模糊背景（前台正常比例 + 金字塔模糊背景铺满）。
3. 景深 DoF（按深度/亮度在不同金字塔层取不同模糊量）。
