---
name: gsy-cast-and-loopback
description: 使用内核层投屏 SPI（CastCapability + CastProvider）接入 DLNA，或做本地 loopback 回环模拟被投屏端。
when_to_use: 需要接入 DLNA/AirPlay/Chromecast 到已有播放页；做投屏发起端 UI；做集成测试的本地 loopback 接收端；或做多协议聚合发现。
inputs:
  - name: providers
    description: 一个或多个 CastProvider 实例（DLNA 默认由 jUPnP 提供）
    required: true
outputs:
  - CastCapability 单例已注册 provider，可发现设备、连接、控制播放/停止。
references:
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cast/CastCapability.java
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cast/CastProvider.java
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cast/CastSession.java
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cast/CastListener.java
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cast/CastMediaInfo.java
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cast/CastState.java
  - file: gsyVideoPlayer-cast/src/main/java/com/shuyu/gsyvideoplayer/cast/dlna
  - file: app/src/main/java/com/example/gsyvideoplayer/CastDemoActivity.java
  - file: app/src/main/java/com/example/gsyvideoplayer/cast
  - file: doc/CAST_FEATURE_PLAN.md
---

# gsy-cast-and-loopback

## 顶层架构

```
CastCapability (单例)  ← 主线程 API 入口
  ├─ registerProvider(CastProvider)
  ├─ startDiscovery(Context) / stopDiscovery()
  ├─ getAvailableDevices(): List<CastDevice>
  ├─ connect(device, ConnectCallback) : CastSession（异步）
  ├─ addListener(CastListener)
  └─ disconnect()
```

对齐 Media3 `Player` 语义：`CastCapability ≈ Player 池`、`activeSession ≈ 当前 Player`、`CastProvider ≈ 渲染后端`、`CastListener/SessionListener ≈ Player.Listener`。见 [CastCapability](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cast/CastCapability.java) 与 [CastProvider](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cast/CastProvider.java) 的头 JavaDoc。

## 核心 API

### CastCapability

| 方法 | 位置 | 说明 |
|---|---|---|
| `getInstance()` | [#L38-L47](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cast/CastCapability.java#L38-L47) | DCL 单例 |
| `registerProvider(CastProvider)` | [#L85-L90](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cast/CastCapability.java#L85-L90) | 注册；`protocol` 相同则覆盖 |
| `unregisterProvider(String)` | [#L93-L103](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cast/CastCapability.java#L93-L103) | 反注册并停止其发现 |
| `startDiscovery(Context)` | [#L106-L125](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cast/CastCapability.java#L106-L125) | 所有已注册 provider 同时开始发现 |
| `stopDiscovery()` | [#L128-L135](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cast/CastCapability.java#L128-L135) | 全部停止 |
| `getAvailableDevices()` | [#L138-L146](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cast/CastCapability.java#L138-L146) | 快照，跨 provider 聚合 |
| `connect(CastDevice, ConnectCallback)` | [#L154-L183](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cast/CastCapability.java#L154-L183) | 会自动 disconnect 旧会话 |
| `disconnect()` | [#L186-L196](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cast/CastCapability.java#L186-L196) | release 当前 session |
| `getActiveSession()` | [#L199-L201](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cast/CastCapability.java#L199-L201) | 拿当前会话（可能 null） |
| `addListener / removeListener` | [#L203-L209](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cast/CastCapability.java#L203-L209) | 主线程 listener |

### CastProvider（SPI 契约）

| 方法 | 说明 |
|---|---|
| `getProtocol(): String` | 全小写协议 id（`dlna` / `chromecast` / `airplay`） |
| `startDiscovery(Context, DiscoveryListener)` | 后台线程订阅，回调 postMain |
| `stopDiscovery()` | 释放订阅、解绑 Service |
| `connect(CastDevice, ConnectCallback): CastSession` | 建立会话；同步返回 session，异步回调结果 |

线程约定：所有 network IO 由 provider 自己后台跑，回调都 postMain。

### CastSession（媒体控制）

见 [CastSession](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cast/CastSession.java)：

| 方法 | 说明 |
|---|---|
| `load(CastMediaInfo)` | 推送要投的 URL |
| `play() / pause() / stop()` | 播控 |
| `seekTo(long ms)` | 定位 |
| `setVolume(int 0-100)` / `setMute(boolean)` | 音量 |
| `getState(): CastState` | IDLE / CONNECTING / CONNECTED / LOADING / PLAYING / PAUSED / STOPPED / ERROR，见 [CastState](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cast/CastState.java) |
| `addListener(SessionListener)` | 位置/状态回调 |
| `release()` | 归还 |

### CastMediaInfo（要投的媒体）

见 [CastMediaInfo](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cast/CastMediaInfo.java)。字段：`url`、`title`、`durationMs`、`mimeType`、`headers`、`metadata`（`Map`）。

## DLNA 参考实现

模块 `gsyvideoplayer-cast` 提供 [JupnpDlnaProvider](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-cast/src/main/java/com/shuyu/gsyvideoplayer/cast/dlna/JupnpDlnaProvider.java) 与 [JupnpDlnaSession](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-cast/src/main/java/com/shuyu/gsyvideoplayer/cast/dlna/JupnpDlnaSession.java)。业务代码调用：

```java
CastCapability cap = CastCapability.getInstance();
cap.registerProvider(new JupnpDlnaProvider());
cap.addListener(myListener);
cap.startDiscovery(context);
```

发现事件：`CastListener#onDeviceListChanged(List<CastDevice>)`；连接：`cap.connect(devices.get(0), connectCallback)`。

## 本地 Loopback（Demo 测试用）

app 模块下的 [cast/](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/cast) 提供**可发现的接收端**，方便在同一台设备上完成集成测试：

- [DevReceiverService](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/cast/DevReceiverService.java) —— foreground service，通过 jUPnP 注册"回环设备"。
- [LoopbackDeviceFactory](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/cast/LoopbackDeviceFactory.java) —— 构造 UPnP MediaRenderer。
- [LoopbackAvTransportService](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/cast/LoopbackAvTransportService.java) / [LoopbackRenderingControlService](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/cast/LoopbackRenderingControlService.java) —— AVTransport / RenderingControl 实现。
- [CastReceiverFloatingWindow](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/cast/CastReceiverFloatingWindow.java) + [CastReceiverPlayer](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/video/CastReceiverPlayer.java) —— 可视化播放。

## Demo 对照

- 一体化：[CastDemoActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/CastDemoActivity.java)
- Cast 相关控制页：[SampleCastControlVideo](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/video/SampleCastControlVideo.java)
- 设计与测试：[doc/CAST_FEATURE_PLAN.md](file:///D:/workspace/project/GSYVideoPlayer/doc/CAST_FEATURE_PLAN.md)、[doc/CAST_RECEIVER_DESIGN.md](file:///D:/workspace/project/GSYVideoPlayer/doc/CAST_RECEIVER_DESIGN.md)、[doc/CAST_TEST_PLAYBOOK.md](file:///D:/workspace/project/GSYVideoPlayer/doc/CAST_TEST_PLAYBOOK.md)

## 常见坑

- 不同 provider 上报的 `CastDevice` 需要用 `getProtocol()` 分组；直接把陌生 protocol 传给 `connect` 会拿到 `IllegalStateException`。
- Provider 网络 IO 必须在自己线程，`CastCapability` 不做 main 切换。
- Wi-Fi 多播锁没拿 → DLNA 发现不到设备：Provider 需要 `WifiManager.MulticastLock`。
- Loopback 与真实设备同网段时会同时被发现，Demo 里有过滤示例。
