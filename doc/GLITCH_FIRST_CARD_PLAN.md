# Glitch 故障风 / RGB 色差 / CRT 扫描线（首个随时间动画效果）首张卡片实施计划

## 1. 背景

当前全部效果（自动修正、色阶、高斯、金字塔模糊、Bloom、LUT、美颜等）都是
**静态逐帧映射**：同一输入像素永远得到同一输出，没有随时间变化的维度。短视频常用的
差异化效果——故障风（Glitch）、RGB 通道错位、数字噪条、CRT 扫描线、画面抖动——
都依赖时间。本卡片引入 `uTime` 时间 uniform，并实现两类动画效果，为后续动态
特效（老电视、信号干扰、动态暗角等）打通基础设施。

## 2. 目标

1. 在单 pass 渲染器中支持**每帧自动注入 `uTime`（秒）**，效果类无需关心计时。
2. 新增单 pass `GlitchEffect`：RGB 通道错位 + 数字撕裂条 + 块位移 + 噪点 +
   轻微画面抖动，强度与时间相关。
3. 新增单 pass `CrtEffect`：CRT 扫描线 + 屏幕微弯曲 + RGB 像素栅格 + 色差 +
   微弱闪烁与暗角（时间相关）。
4. 接入 demo，播放中设备验证：能看到随时间变化的故障 / 扫描线，无 GL 报错，
   不影响默认渲染等其它场景。

## 3. 非目标

- 不做音频驱动的节拍同步，只按 `uTime` 做连续 / 随机动画。
- 不做多 pass 故障（单 pass 采样足够，实时性优先）。
- 不做参数滑杆 UI（构造传强，demo 给固定预设）。
- 不改动多 pass 渲染器的 uTime（本卡只做单 pass；后续动态多 pass 效果再扩展）。

## 4. 技术方案

### 4.1 渲染器自动注入 uTime

- `GSYVideoGLViewSimpleRender` 新增字段 `muTimeHandle`，在 `initProgramHandles`
  中与 `uViewSize` 一样按名查询并缓存（`glGetUniformLocation("uTime")`），
  仅在 program 创建 / 切换时查询一次。
- 以 `System.nanoTime()` 维护一个基准时间，`initPointerAndDraw()` 中当
  `muTimeHandle != -1` 时绑定 `(System.nanoTime() - startNanos) / 1e9`。
  未声明 `uTime` 的旧 shader 句柄为 -1，零开销、行为不变。
- 效果类只需在 fragment shader 声明 `uniform float uTime;` 即可使用。

> 说明：基准时间在渲染器构造时初始化，不随播放暂停重置，属于“墙上时钟”，
> 满足视觉动画需要；不追求与视频 PTS 对齐（非目标）。

### 4.2 GlitchEffect（故障风）

- 构造参数 `strength`（0~1）。
- 用 hash 伪随机（`fract(sin(dot(...)) )`）按“行 / 块 + 时间量化”产生
  随时间跳变但同一帧稳定的随机量（避免每像素闪烁到不可读）。
- 组成：
  1. **RGB 通道错位**：R/B 按时间随机量在 x 方向偏移采样，偏移随强度与
     随机脉冲放大；
  2. **水平撕裂条**：若干随机水平条带整体做 x 位移（条带位置 / 宽度按
     `floor(uTime)` 量化随机）；
  3. **块位移**：粗网格块在某些时刻整体偏移，产生数字丢块感；
  4. **噪点**：少量像素叠加亮度噪声；
  5. **轻微整体抖动**：x 方向亚像素抖动；
  6. 保持 alpha 取自原纹理。

### 4.3 CrtEffect（CRT 显像管）

- 构造参数 `strength`（0~1）。
- 用 `uViewSize` 得到纹元与宽高比：
  1. **屏幕微弯曲**：对 uv 做桶形 / 归一化偏移，边缘轻微暗化；
  2. **扫描线**：按行（`sin` / 三角）产生水平亮暗纹；
  3. **RGB 像素栅格**：按列产生细的三色栅格纹理；
  4. **色差**：R/B 在边缘轻微径向错位；
  5. **闪烁 + 暗角**：`uTime` 驱动的极轻微亮度闪烁与四周暗角；
  6. 保持 alpha 不变。

### 4.4 接入

- `DetailFilterActivity` 在默认渲染场景的滤镜序列追加“故障风”“CRT扫描线”。
- 复用现有切换逻辑，无需特殊处理。

## 5. 测试与验收

1. `:app:assembleDebug` 编译通过。
2. 模拟器播放中切到故障风 / CRT：肉眼确认随时间变化（错位 / 撕裂条跳动 /
   扫描线栅格），截图存 `app/test_evidence/glitch-card/`。
3. logcat 无 GL 编译 / 链接错误。
4. 回归：默认渲染、LUT、美颜、多 pass 场景可正常进入。

## 6. 执行记录

- 渲染器 `GSYVideoGLViewSimpleRender`：新增 `muTimeHandle`（program 创建 /
  切换时按 `uTime` 名查询一次并缓存）与基准时间 `mStartNanos`，
  `initPointerAndDraw` 中按秒绑定；未声明 `uTime` 的旧 shader 句柄为 -1，零影响。
- 新增 `GlitchEffect`（RGB 错位 + 3 条水平撕裂 + 块位移 + 噪点 + 抖动）与
  `CrtEffect`（桶形弯曲 + 扫描线 + RGB 栅格 + 色差 + 闪烁暗角）。
- `DetailFilterActivity` 滤镜序列追加“故障风”“CRT扫描线”。
- 模拟器 emulator-5554：`:app:assembleDebug` 通过，安装成功；播放中切到
  故障风，两帧对比可见错位 / 撕裂 / 块位移随时间明显增强；CRT 可见弯曲、
  扫描线与暗角。logcat 无 GL 编译 / 链接报错。证据：
  `app/test_evidence/glitch-card/40_glitch_a.png`、`41_glitch_b.png`、
  `42_crt_a.png`、`43_crt_b.png`。

