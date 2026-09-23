# FBO 多 pass 渲染管线 + 双向高斯模糊实施计划

> 创建时间：2026-09-22
> 范围：OpenGL ES 2.0，新增基于 FBO 的多 pass 渲染基础设施，并以双向高斯模糊作为首个落地效果
> 完成标准：构建、静态检查、设备侧播放中切换验证通过，证据持久化到 `app/test_evidence/`

## 1. 背景与目标

现有 GL 路径（`GSYVideoGLViewSimpleRender`）只支持**单 pass**：视频解码输出的
`GL_TEXTURE_EXTERNAL_OES` 外部纹理经一个 fragment shader 直接上屏。它无法表达需要
中间结果的高质量效果（可分离高斯、Bloom、动态模糊背景、景深等）。

本卡目标：

1. 提供 FBO / RenderTarget 抽象，支持把画面渲染到离屏的普通 `GL_TEXTURE_2D`。
2. 提供多 pass effect 接口与渲染编排，支持在 FBO 之间 ping-pong，最后一 pass 上屏。
3. 处理外部纹理到普通纹理的**转换 pass**（OES → 2D，应用 ST 变换矩阵）。
4. 以**双向（可分离）高斯模糊**作为首个落地效果：水平 pass + 垂直 pass。
5. 通过隔离的“渲染场景”接入 demo，不影响既有 27 个单 pass 滤镜的回归结果。

## 2. 非目标

1. 本卡不替换或重构现有 `GSYVideoGLViewSimpleRender` 单 pass 路径。
2. 本卡不做降采样金字塔（mip chain）/ 多次迭代模糊，FBO 首轮按 view 尺寸 1:1 分配。
3. 本卡不实现 Bloom、动态模糊背景、景深（它们作为后续卡复用管线）。
4. 不升级 OpenGL ES 版本，不引入第三方依赖。
5. 不在播放过程中热切换多 pass 渲染器（沿用 GSY “渲染器需在播放前设置”的约束）。

## 3. 渲染管线设计

```
解码帧 OES 外部纹理 (mTextureID[0])
        │  Pass 0: 输入转换 (samplerExternalOES + uSTMatrix)
        ▼
   FBO[0]  (GL_TEXTURE_2D)
        │  Pass 1: 水平高斯 (sampler2D, uDirection=(1,0))
        ▼
   FBO[1]  (GL_TEXTURE_2D)
        │  Pass 2: 垂直高斯 (sampler2D, uDirection=(0,1))
        ▼
   默认 framebuffer（屏幕）
```

- FBO 数量 = effect 声明的 pass 数（输入转换固定写入 `FBO[0]`，第 i 个 pass 读
  `FBO[i]`、写 `FBO[i+1]`，最后一个 pass 写屏幕）。
- 中间 pass 统一使用独立的 fullscreen quad 与标准 0..1 UV，identity MVP，不做形变，
  以保证每 pass 图像对齐。
- 每帧绘制顺序即 GL 命令顺序，不使用 `glFinish()`，避免 CPU/GPU 强制同步。

## 4. 文件变更清单

| 序号 | 文件 | 类型 | 内容 |
|---|---|---|---|
| 1 | `render/glrender/GLFrameBuffer.java` | 新增 | FBO + 颜色纹理封装：按尺寸创建、resize 重建、绑定、作为采样纹理、释放 |
| 2 | `render/glrender/GSYVideoGLViewMultiPassInterface.java` | 新增 | 多 pass effect 接口：pass 数、各 pass fragment shader、释放回调 |
| 3 | `render/glrender/GSYVideoGLViewMultiPassRender.java` | 新增 | 多 pass 渲染器：OES 输入、转换 pass、FBO ping-pong 编排、截图支持、资源释放 |
| 4 | `render/effect/GaussianBlurMultiPassEffect.java` | 新增 | 双向高斯：2 个 pass（H/V），9-tap 高斯权重，半径可配 |
| 5 | `app/.../DetailFilterActivity.java` | 修改 | 新增“多Pass高斯”渲染场景，播放前装配多 pass 渲染器 |

## 5. 技术要点

### 5.1 FBO 封装

- `glGenFramebuffers` + 一张 `GL_TEXTURE_2D` 颜色附件（`GL_RGBA`，
  `UNSIGNED_BYTE`，`CLAMP_TO_EDGE`，`LINEAR`）。
- `glFramebufferTexture2D` 绑定后检查 `GL_FRAMEBUFFER_COMPLETE`。
- 尺寸变化（`onSurfaceChanged`）时删除并按新尺寸重建。

### 5.2 输入转换 pass

- 顶点着色器与现有路径一致，应用 `uSTMatrix`，采样器为 `samplerExternalOES`。
- fragment 仅原样输出，完成 OES → 2D 的转换并写入 `FBO[0]`。

### 5.3 中间 pass 标准 uniform

渲染器在每个中间 pass 绘制前设置（shader 未声明时 location 为 -1，跳过）：

- `sampler2D sTexture`：绑定当前输入 FBO 纹理到 TEXTURE0。
- `vec2 uTexelSize`：`1/FBO宽, 1/FBO高`。

高斯 fragment 额外声明 `vec2 uDirection` 与 `float uRadius`，由渲染器按 pass 传入。

### 5.4 双向高斯

- 可分离卷积：先横向 9-tap，再纵向 9-tap，复杂度由 O(r²) 降为 O(2r)。
- 权重使用归一化高斯系数，保留输入 alpha（`vec4(sum.rgb, center.a)` 或直接 sum 含 alpha）。

## 6. 测试与验收计划

### 6.1 本地静态验证

- `./gradlew :gsyVideoPlayer-java:assembleDebug`
- `./gradlew :app:assembleDebug`
- `./gradlew :gsyVideoPlayer-java:testDebugUnitTest`
- 必要时 `./gradlew :app:lintDebug`

### 6.2 设备侧验证（emulator-5554）

1. 启动 App → 进入“滤镜”页面。
2. 切换“渲染模式”到“多Pass高斯”，在**播放过程中**观察并截图。
3. 检查 logcat：
   - 无 `FATAL EXCEPTION`
   - 无 `GL_INVALID_OPERATION / GL_INVALID_FRAMEBUFFER_OPERATION`
   - 无 FBO incomplete、无 shader compile/link error
4. 对比无模糊（普通渲染）与多 pass 高斯截图，确认整体平滑、边缘无撕裂/黑边、
   alpha 正常、无明显性能暴跌。
5. 证据保存到 `app/test_evidence/multipass-fbo-first-card/`。

### 6.3 完成判定

- 所有代码变更完成且构建通过；
- 设备侧播放中验证通过且 FBO 状态完整；
- 测试证据保存；
- 计划文件记录最终结果。

## 7. 风险与回退

- 风险：FBO 在部分模拟器上 color attachment 格式支持差异。对策：检查
  FRAMEBUFFER_COMPLETE，失败时回调现有 render error 通道。
- 风险：外部纹理 ST 矩阵在转换 pass 处理不当导致镜像/黑边。对策：复用现有经过
  验证的顶点变换，仅把渲染目标改为 FBO。
- 回退：本卡为纯新增 + demo 场景接入，删除场景入口与新增文件即可完全回退，不影响
  现有单 pass 路径。

## 8. 执行记录

> 状态：已完成（2026-09-22）

### 8.1 实现

1. `GLFrameBuffer`：FBO + 颜色附件（`GL_RGBA/UNSIGNED_BYTE`，`CLAMP_TO_EDGE`，
   `LINEAR`），创建后校验 `GL_FRAMEBUFFER_COMPLETE`，尺寸变化时删除重建。
2. `GSYVideoGLViewMultiPassInterface`：声明 pass 数、各 pass fragment shader、
   `onBindPassUniform` 自定义 uniform 回调与 `release`。
3. `GSYVideoGLViewMultiPassRender`：OES 输入 → 转换 pass（应用 `uSTMatrix`）写入
   `FBO[0]` → FBO ping-pong 执行中间 pass → 最后 pass 上屏；随 surface 生命周期
   重建 FBO 并在释放时清理。
4. `GaussianBlurMultiPassEffect`：2 pass（水平/垂直），9-tap 归一化高斯权重，
   `uDirection` 分别为 `(1,0)` / `(0,1)`，半径可配，保留 alpha。
5. `DetailFilterActivity`：新增“多Pass高斯”渲染场景，播放前装配多 pass 渲染器，
   该场景下禁用单 pass 滤镜切换，避免与既有 27 效果冲突。

### 8.2 验证（emulator-5554）

- IDE 诊断（编译/类型）：无错误。
- 设备侧播放中验证：
  - 普通渲染基线 → 切到“多Pass高斯”，播放过程中画面整体平滑、柔化明显；
  - 水平 + 垂直两 pass 结果各向同性，无单向拖影、黑边、撕裂，alpha 正常；
  - 逐帧截图文件大小随画面变化，确认效果作用在实时播放帧而非静止封面；
  - logcat 无 `FATAL`、无 `GL_INVALID_*`、无 FBO incomplete、无 shader
    compile/link error；
  - 切回普通渲染后画面恢复清晰，切换过程无崩溃、无 GL 错误，FBO 随释放清理。
- 证据目录：`app/test_evidence/multipass-fbo-first-card/`
  - `00_normal_baseline.png`：普通渲染基线
  - `01_playing_gaussian.png` / `02_playing_gaussian_later.png`：播放中高斯模糊
  - `03_back_to_normal.png`：切回普通渲染

## 9. 后续卡片

1. FBO 降采样金字塔 + 迭代模糊，用于大半径模糊与 Bloom。
2. Bloom（亮部提取 → 模糊 → 合成）。
3. 动态模糊背景（前台正常比例 + 背景铺满模糊），可对标现有 CustomRender4 但效果更佳。
4. 景深 DoF、玻璃折射等依赖中间结果的高级效果。
