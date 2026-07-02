# 近期播放能力说明

本文档汇总近期 Demo 和基础能力调整，方便回归和二次开发时快速确认入口、API 和注意事项。

## 入口总览

| 能力 | Demo 入口 | 主要类 | 说明 |
| --- | --- | --- | --- |
| WebVTT 进度条预览 | `打开VIDEO` | `PreViewGSYVideoPlayer` | 通过 WebVTT 缩略图轨道展示预览图，支持独立图片和 sprite 坐标裁剪。 |
| 通用外挂字幕 | `自定义EXO支持字幕`、`通用字幕非EXO` | `GSYSubtitleController`、`GSYSubtitleView` | SRT/WebVTT 由播放器 UI 层统一解析和渲染，可跨 IJK、System、Media3 使用。 |
| 完成后保留最后一帧 | `完成保留最后一帧` | `KeepLastFrameVideo` | Demo 级实现，通过 flag 控制自然播放完成时保留当前渲染画面或回到默认封面态。 |
| 截图语义增强 | 滤镜 Demo、MediaCodec Demo 等 | `StandardGSYVideoPlayer`、`GSYRenderView` | 保留视频帧截图，同时新增包含播放器 UI 的组合截图 API。 |
| GLSurfaceView 效果和生命周期 | `滤镜` | `DetailFilterActivity`、`GSYVideoGLView*Render` | 整理 GL render 生命周期，补充滤镜、纹理、多窗口、遮罩等 Demo 场景。 |
| 多 URL 清晰度切换稳态优化 | `无缝切换` | `SmartPickVideo` | 保留双 manager 方案，强化切换过程中的位置同步、超时、失败回退和临时 manager 释放。 |
| Exo 自适应清晰度 | `EXO自适应清晰度` | `ExoAdaptiveTrackActivity`、`GSYExo2MediaPlayer` | 使用单个 HLS master playlist 或 DASH MPD，由 Media3 TrackSelector 在同一时间线内自适应或固定 video track。 |
| 播放器初始化失败安全处理 | 通用能力 | `GSYVideoBaseManager`、各 `IPlayerManager` | 内核创建或初始化失败时走错误回调和资源清理，避免直接 crash。 |
| Exo 缓存生命周期和 GIF 清理 | 通用能力 | `ExoSourceManager`、`GifCreateHelper` | 收紧 Exo cache 的打开/释放流程，GIF 生成流程结束或失败时更可靠地清理。 |
| DLNA/UPnP 投屏 | `投屏 Demo` | `CastCapability`、`JupnpDlnaProvider`、`JupnpDlnaSession`、`SampleCastControlVideo`、`CastDemoActivity` | 内核一等公民投屏能力，基于 jUPnP 3.0.3 走 DLNA `AVTransport:1` 标准协议，`SetAVTransportURI → Play → Seek` 保留中途投屏本地进度；带单机 Loopback Receiver 用于端到端自测。 |

## 近期提交覆盖

按近期提交逐条对应如下：

| 提交 | 文档覆盖 |
| --- | --- |
| `Add Exo adaptive quality demo and docs` | `README*`、`USE*`、`RECENT_FEATURES*`、`UPDATE_VERSION*`、`ARCHITECTURE.md`、`GSYVIDEO_PLAYER_PROJECT_INFO*` |
| `Harden smart quality switching` | 多 URL 清晰度切换章节、架构层级表、回归清单 |
| `Harden GL renderer lifecycle and demo` | GLSurfaceView 效果章节、架构层级表、回归清单 |
| `Fix screenshot callbacks and composed capture` | 截图能力章节、组合截图 API、架构层级表 |
| `Add keep last frame demo` | 完成后保留最后一帧章节、入口总览、回归清单 |
| `feat: add unified subtitle support` | 通用外挂字幕章节、`SUBTITLE_CN.md`、入口总览、回归清单 |
| `Add WebVTT seek preview support` | WebVTT 进度条预览章节、入口总览、回归清单 |
| `Handle player init failures gracefully` | 播放器初始化失败安全处理条目、版本说明 |
| `Improve Exo cache lifecycle and GIF cleanup` | Exo 缓存生命周期和 GIF 清理条目、版本说明 |

## 文档覆盖

近期播放能力相关说明已经分散补充到以下文档：

- `README_CN.md` / `README.md`：首页能力摘要和近期能力入口。
- `doc/USE.md` / `doc/USE_EN.md`：使用层面的 Demo 入口和核心 API。
- `doc/UPDATE_VERSION.md` / `doc/UPDATE_VERSION_EN.md`：Unreleased 版本变更摘要。
- `doc/ARCHITECTURE.md`：播放能力在 UI、Manager、Render、Exo manager 等层级上的设计归属。
- `doc/GSYVIDEO_PLAYER_PROJECT_INFO.md` / `doc/GSYVIDEO_PLAYER_PROJECT_INFO_EN.md`：项目结构说明里的近期能力层级映射。
- `doc/SUBTITLE_CN.md`：通用字幕专题说明。
- `doc/KEEP_LAST_FRAME.md` / `doc/KEEP_LAST_FRAME_EN.md`：完成后保留最后一帧专题说明。
- `doc/RECENT_FEATURES.md` / `doc/RECENT_FEATURES_EN.md`：近期能力总览、API 和回归清单。

构建、依赖、SO、发布、解码器、FAQ 类文档没有强行加入本次播放能力说明，因为它们的主题不是 Demo 功能入口或播放架构。

## WebVTT 进度条预览

预览不再依赖客户端对原视频批量抽帧。业务侧可以预生成 WebVTT 缩略图轨道，cue 可以指向独立图片，也可以指向 sprite 坐标：

```text
WEBVTT

00:00:00.000 --> 00:00:05.000
thumbs.jpg#xywh=0,0,160,90
```

主要 API：

```java
player.setOpenPreView(true);
player.setPreviewVttUrl("https://example.com/thumbs.vtt");
```

库层提供 `GSYVideoPreviewVttParser`、`GSYVideoPreviewProvider`、`GSYVideoPreviewFrame`，业务层只需要按拖动进度获取对应帧。

## 通用外挂字幕

外挂字幕能力已从 Media3 专属实现调整为 UI overlay 能力。SRT/WebVTT 由 `GSYSubtitleController` 加载、解析和根据播放进度刷新，异常时只清空字幕，不影响主视频播放。

基本用法：

```java
GSYSubtitleSource source = new GSYSubtitleSource.Builder("https://example.com/subtitle.srt")
    .setMimeType(GSYSubtitleMime.APPLICATION_SUBRIP)
    .setLanguage("zh")
    .setLabel("中文")
    .setDefault(true)
    .build();

videoPlayer.setSubtitleSource(source);
videoPlayer.setSubtitleEnabled(true);
```

详细说明见 [SUBTITLE_CN.md](SUBTITLE_CN.md)。

## 完成后保留最后一帧

`KeepLastFrameVideo` 是 Demo 级实现，核心是覆写自然播放完成后的 UI 状态切换，在不主动释放 render view 的情况下保留画面。详细说明见 [KEEP_LAST_FRAME.md](KEEP_LAST_FRAME.md)。

```java
keepLastFrameVideo.setKeepLastFrameWhenComplete(true);
```

注意：这是给业务验证交互语义的 Demo，不是全局默认行为。需要根据业务播放器封面、Surface 释放策略、重播逻辑决定是否下沉到基础库。

## 截图能力

现有视频帧截图语义保持不变：

```java
player.taskShotPic(listener);
player.saveFrame(file, listener);
```

新增组合截图语义，用于需要把视频画面和播放器 UI 一起截下来的场景：

```java
player.taskShotPicWithView(listener);
player.saveFrameWithView(file, listener);
```

当前修复点：

- SurfaceView 通过 PixelCopy 截图时所有失败路径都会回调 `null` 或 `success=false`。
- TextureView、SurfaceView、GLSurfaceView 保存文件时会根据真实写入结果回调。
- 组合截图会先拿视频帧，再叠加播放器 UI，适合需要包含字幕 overlay、控制栏等视图层的场景。

## GLSurfaceView 效果

GL 相关整理集中在 `DetailFilterActivity` 和 `GSYVideoGLView*Render`：

- Demo 内统一切换到 `GSYVideoType.GLSURFACE`，退出时恢复进入前的 render type。
- `GSYVideoGLViewBaseRender` / `GSYVideoGLViewSimpleRender` 增强 release 和截图回调，降低 GL 生命周期异常导致的卡住风险。
- `滤镜` Demo 可以切换普通滤镜、纹理水印、多窗口播放、图片穿孔、背景高斯等场景。

## 多 URL 清晰度切换

`SmartPickVideo` 仍然适用于多个独立 URL 的清晰度切换或下一集切换。这个方案不是 HLS/DASH 标准 ABR，核心是使用临时 manager 预加载新 URL，准备好后同步到当前播放位置并提交切换。

当前优化点：

- 切换时记录最新播放位置，避免直接回到 0。
- 增加 seek 误差阈值、重试和超时保护。
- 切换失败会回退原播放，不让临时 manager 长期占用资源。

如果服务端可以提供 HLS master playlist 或 DASH MPD，优先使用下面的 Exo 自适应清晰度 Demo。

## Exo 自适应清晰度

`EXO自适应清晰度` Demo 使用一个 HLS master URL 或一个 DASH MPD URL。Exo/Media3 会把同一个 track group 里的多码率 video track 用于 adaptive playback。

Demo 内置测试源：

- HLS master：`https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8`
- DASH MPD：`https://dash.akamaized.net/envivio/EnvivioDash3/manifest.mpd`

新增能力：

```java
GSYExoVideoManager.instance().getVideoTrackInfoList();
GSYExoVideoManager.instance().clearVideoTrackOverride();
GSYExoVideoManager.instance().setVideoTrackOverride(groupIndex, trackIndex);
```

说明：

- 默认选择“自动”，由 TrackSelector 根据带宽、buffer、设备能力选择 track。
- 选择固定清晰度时，会使用 `TrackSelectionOverride` 固定到某个 video track。
- 清除 override 后恢复自适应。

## 播放器初始化失败安全处理

内核初始化相关改动集中在 `GSYVideoBaseManager` 和各内核 `IPlayerManager`。当 IJK、System、Exo、AliPlayer 创建或初始化失败时，流程会尽量走 `onError` 和释放清理，而不是把异常直接抛到业务层导致 crash。

这个能力没有单独 Demo 入口，属于全局稳定性兜底。建议通过非法 URL、缺失解码能力或故意构造异常初始化场景回归，确认播放器进入错误态且应用不崩。

## Exo 缓存生命周期和 GIF 清理

`ExoSourceManager` 收紧了 Exo cache 的生命周期处理，降低 cache 打开、复用和释放过程中的资源残留风险。`GifCreateHelper` 在 GIF 生成结束、失败或取消后会更可靠地推进状态和清理临时资源。

这个能力同样没有单独入口。回归时建议覆盖 Exo cache 播放、退出页面、重新进入播放，以及滤镜 Demo 里的 GIF 生成/失败路径。

## 回归建议

每次修改这些能力后，至少执行：

```bash
./gradlew :gsyVideoPlayer-java:testDebugUnitTest :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

真机建议检查：

- `打开VIDEO`：拖动进度条，确认 WebVTT 预览图能出现。
- `自定义EXO支持字幕`、`通用字幕非EXO`：本地 SRT、本地 VTT、网络 SRT 都能切换，失败不影响播放。
- `完成保留最后一帧`：打开/关闭 flag，确认完成后分别停留最后一帧和回到封面。
- `滤镜`：切换滤镜和 GL 场景，确认播放、截图、GIF 不崩。
- `无缝切换`：切换多个 URL，确认不回 0，失败可回退。
- `EXO自适应清晰度`：HLS 和 DASH 都能播放，轨道列表能显示，自动/固定清晰度能切换。
- 播放失败和内核初始化异常：确认进入错误回调，不直接 crash。
- Exo cache 和 GIF：确认退出、重进、失败路径都能清理资源。
- `投屏 Demo`：可选择 DLNA 设备，投屏后本地塌陷成遥控 overlay；断开后本地在近似远端进度处继续；启用 `Loopback Receiver` 可无真电视自测。

## DLNA/UPnP 投屏

投屏能力以内核 SPI 形式并入 `gsyVideoPlayer-java`，不新增独立发布 module。默认实现走 jUPnP 3.0.3 的 DLNA `AVTransport:1` 协议。SPI 三件套：

- `CastCapability`：入口门面，通过 `GSYVideoBaseManager.getCastCapability()` 获取。
- `CastProvider`：负责设备发现，暴露 `startDiscovery(CastListener)` / `stopDiscovery()`。
- `CastSession`：一次投屏会话生命周期，`setMediaItem(CastMediaInfo)` → `play/pause/stop/seekTo` → `disconnect()`。

投屏中途起播示例：

```java
CastCapability cast = GSYVideoManager.instance().getCastCapability();
cast.getProvider().startDiscovery(new CastListener() {
    @Override public void onDeviceFound(CastDevice device) { /* 展示到列表 */ }
});

// 用户选中设备后：
long localPositionMs = videoPlayer.getCurrentPositionWhenPlaying();
CastMediaInfo media = new CastMediaInfo(url, title, "video/mp4", /*durationMs*/ 0L, localPositionMs);
CastSession session = cast.connect(selectedDevice);
session.setMediaItem(media);  // SPI 内部会做 SetAVTransportURI → Play → Seek(localPositionMs)
```

Demo：`MainActivity` 底部有独立"投屏 Demo"入口 `CastDemoActivity`，可打开 DLNA 设备选择列表或开关 Loopback Receiver。演示播放器 `SampleCastControlVideo` 在投屏成功后自动塌陷成远端遥控 overlay，本地 surface / audio 释放；断开后按最近远端进度恢复本地播放。

单机自测 Loopback Receiver：
- `DevReceiverService` 跑在独立 `:dlna` 进程，注册 jUPnP `LocalDevice`（`urn:schemas-upnp-org:device:MediaRenderer:1`）+ `LoopbackAvTransportService` + `LoopbackRenderingControlService`。
- 接收端 `CastReceiverFloatingWindow` 以 SYSTEM_ALERT_WINDOW 悬浮窗呈现远端播放画面（`CastReceiverPlayer` 内嵌 IJK 内核）。
- `getPositionInfo` / `getTransportInfo` 回填真实进度状态，sender 端 1Hz 轮询看到的进度就是远端真实播放位置。
- Service ↔ Activity 通过 `setPackage` 私有广播（`ACTION_STATE_READY` / `ACTION_STATE_STOPPED` / `ACTION_STATE_ERROR`）同步状态；Android 13+ 已适配 `RECEIVER_NOT_EXPORTED`。

依赖开关：投屏依赖在 [gradle/dependencies.gradle](../gradle/dependencies.gradle) 的 `deps.jupnp` 定义（`org.jupnp:org.jupnp:3.0.3` + `org.jupnp:org.jupnp.support:3.0.3`），仅在 `gsyVideoPlayer-java` 启用 cast 源集时打入；下游不用投屏时 AAR 零增量（详见 [DEPENDENCIES.md](DEPENDENCIES.md)）。

更多能力目标与测试判据见 [CAST_FEATURE_PLAN.md](CAST_FEATURE_PLAN.md) 与 [CAST_TEST_PLAYBOOK.md](CAST_TEST_PLAYBOOK.md)。
