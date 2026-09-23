# GL 渲染首张卡实施计划

> 创建时间：2026-09-22  
> 范围：OpenGL ES 2.0 普通播放路径、内置尺寸相关 Shader、Demo 中无条件 GL 同步调用  
> 完成标准：构建、静态检查、设备侧滤镜页回归均通过，并将测试证据持久化到 `app/test_evidence/`

## 1. 目标

1. 移除普通绘制路径的无条件 `glFinish()`，避免 CPU/GPU 每帧强制同步。
2. 为内置尺寸相关 Shader 提供运行时 View 尺寸 Uniform，避免宽高为 0 时生成 `Infinity` / `NaN` Shader。
3. 修复 Barrel Shader 中无意义的 `uv` 表达式，并保留输入纹理 alpha。
4. 修复 Demo 像素化 Shader 的尺寸安全和 alpha 保留问题。
5. 在滤镜页执行设备侧回归，确认页面可进入、视频可播放、特效切换不产生 GL/FATAL 错误。

## 2. 非目标

1. 本卡不引入 FBO / RenderTarget。
2. 本卡不实现双向高斯、Bloom、LUT、景深等多 pass 特效。
3. 本卡不重写 SaturationEffect 的完整调色模型。
4. 本卡不强制升级到 OpenGL ES 3.0。
5. 本卡不新增第三方依赖。

## 3. 变更计划

| 序号 | 文件 | 变更内容 |
|---|---|---|
| 1 | `GSYVideoGLViewSimpleRender.java` | 移除普通帧 `glFinish()`；记录 Surface 尺寸；设置可选 `uViewSize` Uniform |
| 2 | `SharpnessEffect.java` | 使用 `uViewSize` 计算安全 texel，固定 Shader 源 |
| 3 | `VignetteEffect.java` | 使用 `uViewSize` 计算画面比例和暗角距离 |
| 4 | `GrainEffect.java` | 使用 `uViewSize` 计算安全采样步长，避免除零 |
| 5 | `DocumentaryEffect.java` | 使用 `uViewSize` 计算画面比例和暗角距离 |
| 6 | `LamoishEffect.java` | 使用 `uViewSize` 计算画面比例、锐化步长和暗角距离 |
| 7 | `BarrelBlurEffect.java` | 删除无效 `uv`，输出 alpha 改为输入纹理 alpha |
| 8 | `PixelationEffect.java` | Demo 像素化效果改用 `uViewSize`，保留输入 alpha |
| 9 | Demo 自定义 Render | 移除普通绘制路径无条件 `glFinish()` |

## 4. 技术方案

### 4.1 运行时尺寸 Uniform

Render 侧设置可选 Uniform：

```glsl
uniform vec2 uViewSize;
```

Shader 侧使用安全下限：

```glsl
vec2 viewSize = max(uViewSize, vec2(1.0));
vec2 texel = 1.0 / viewSize;
```

Render 获取不到 Uniform 时允许 location 为 `-1`，不把它作为 Program 初始化失败条件，从而兼容不声明该 Uniform 的旧 Shader。

### 4.2 同步策略

- 普通视频帧不调用 `glFinish()`。
- `glReadPixels` 所在截图路径依赖 GL 命令顺序执行，不额外阻塞所有普通帧。
- 后续如需严格异步回读，另开 PBO / FBO 任务卡处理。

## 5. 测试与验收计划

### 5.1 本地静态验证

- `./gradlew :gsyVideoPlayer-java:assembleDebug`
- `./gradlew :app:assembleDebug`
- 必要时执行 `./gradlew :app:lintDebug`

### 5.2 设备侧回归

目标设备：`emulator-5554`。

回归路径：

1. 启动 App。
2. 进入“滤镜”页面。
3. 等待视频出帧。
4. 依次切换使用 `uViewSize` 的内置效果和 Barrel 效果。
5. 检查 logcat：
   - 无 `FATAL EXCEPTION`
   - 无 `GL_INVALID_OPERATION`
   - 无 Shader compile / link error
6. 保存 logcat、UI/Widget 证据或截图到 `app/test_evidence/gl-first-card/`。

### 5.3 完成判定

必须同时满足：

- 所有代码变更完成；
- 构建命令通过；
- 设备侧滤镜回归通过；
- 测试证据已保存；
- 计划文件记录最终结果。

## 6. 执行记录

### 6.1 阶段汇总

| 阶段 | 状态 | 结果 |
|---|---|---|
| 计划创建 | 已完成 | 2026-09-22 已写入本地计划文件 |
| 代码实施 | 已完成 | 普通路径移除 `glFinish()`，尺寸相关 Shader 接入 `uViewSize`，Barrel 与像素化效果修复 alpha |
| 构建验证 | 已完成 | `:gsyVideoPlayer-java:assembleDebug`、`:app:assembleDebug` 通过 |
| 单元测试 | 已完成 | `:gsyVideoPlayer-java:testDebugUnitTest` 通过 |
| 静态检查 | 已完成 | `:app:lintDebug` 通过 |
| 设备侧播放中回归 | 已完成 | 27 个效果均在视频播放窗口内完成切换和截图，无 App/GL 致命错误 |
| Instrumentation | 已完成 | `Rtmp16KbPageInstrumentedTest`：`OK (2 tests)` |
| 证据归档 | 已完成 | 截图、logcat 与 instrumentation 结果已持久化 |
| 计划闭环 | 已完成 | 本卡满足完成判定 |

### 6.2 最终验证命令

```powershell
.\gradlew.bat :gsyVideoPlayer-java:assembleDebug :app:assembleDebug :gsyVideoPlayer-java:testDebugUnitTest :app:lintDebug
```

结果：`BUILD SUCCESSFUL`。

```powershell
adb shell am instrument -w -e class com.example.gsyvideoplayer.Rtmp16KbPageInstrumentedTest com.example.gsyvideoplayer.test/androidx.test.runner.AndroidJUnitRunner
```

结果：

```text
com.example.gsyvideoplayer.Rtmp16KbPageInstrumentedTest:.

Time: 0.014

OK (2 tests)
```

一次使用错误包名的 instrumentation 调用产生 `ClassNotFoundException`，该结果未计入通过；修正为实际类名 `com.example.gsyvideoplayer.Rtmp16KbPageInstrumentedTest` 后重新执行并通过。

### 6.3 播放中 GL 效果回归

回归设备：`emulator-5554`。

采集方法：将 27 个效果拆成 5 组分段重播，分组数量为 6、6、6、6、3。每组从结束态重新开始播放，在播放窗口内切换效果并立即截图，避免把结束封面误判为播放中渲染。

日志检查结果：

- 未发现应用进程 `FATAL EXCEPTION`；
- 未发现 `GL_INVALID_OPERATION`、`GL_INVALID_VALUE` 等 GL 错误；
- 未发现 Shader compile / link error；
- 系统进程中的 SQLite constraint 与 AlarmManager window 警告与本次 GL 变更无关。

效果序号映射：

| 序号 | 效果 | 序号 | 效果 | 序号 | 效果 |
|---|---|---|---|---|---|
| 00 | 自动修正 | 01 | 像素化 | 02 | 黑白 |
| 03 | 对比度 | 04 | 冲印 | 05 | 纪录片 |
| 06 | 双色调 | 07 | 补光 | 08 | Gamma |
| 09 | 颗粒 | 10 | 颗粒增强 | 11 | 色相 |
| 12 | 反色 | 13 | Lomo | 14 | 色阶 |
| 15 | 桶形模糊 | 16 | 饱和度 | 17 | 棕褐 |
| 18 | 锐化 | 19 | 色温 | 20 | 染色 |
| 21 | 暗角 | 22 | 无滤镜 | 23 | Overlay |
| 24 | 采样模糊 | 25 | 高斯模糊 | 26 | 亮度 |

### 6.4 证据位置

证据目录：[final-regression-live-20260922](file:///d:/workspace/project/GSYVideoPlayer/app/test_evidence/gl-first-card/final-regression-live-20260922)

关键证据：

- `effect-00.png` 至 `effect-26.png`：27 个效果在播放中的截图；
- [final-live-logcat.txt](file:///d:/workspace/project/GSYVideoPlayer/app/test_evidence/gl-first-card/final-regression-live-20260922/final-live-logcat.txt)：设备回归完整日志；
- [instrumented-test-result.txt](file:///d:/workspace/project/GSYVideoPlayer/app/test_evidence/gl-first-card/final-regression-live-20260922/instrumented-test-result.txt)：`OK (2 tests)` 结果。

## 7. 后续卡片

1. ColorAdjust 与标准 Saturation 重构。
2. FBO 多 pass 管线。
3. 双向高斯和动态模糊背景。
4. LUT 与 Cinematic 调色。
5. Bloom、Glitch、Ripple 等高级创意特效。
