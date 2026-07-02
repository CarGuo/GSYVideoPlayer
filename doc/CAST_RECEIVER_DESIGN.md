# CAST_RECEIVER_DESIGN — 桌面投屏接收器设计

> 立项日期：2026-07-01
> 分支：`feature/cast-capability`
> 关联：[CAST_FEATURE_PLAN.md](./CAST_FEATURE_PLAN.md) · [CAST_TEST_PLAYBOOK.md](./CAST_TEST_PLAYBOOK.md)

---

## 0. 立项动因

手机真机 + 桌面接收器组合是投屏开发/回归的**最小闭环**：
- 手机端就是**真实生产代码**（无 mock），符合"测试要测到功能正常"（[JAVA_TEST_PLAYBOOK.md#L11-L15](./JAVA_TEST_PLAYBOOK.md#L11-L15)）
- 桌面端做一台"假电视"，随开随用，不用采购真实智能电视
- 桌面端可暴露 HTTP 日志端点，供自动化脚本 curl 断言

**约束**：桌面接收器**只用于本地测试**，不参与 [publish.gradle](../gradle/publish.gradle) / [maven-central-publish.gradle](../gradle/maven-central-publish.gradle) 发布链。

---

## 1. 整体架构

```
┌─────────────────────────┐              ┌──────────────────────────────┐
│  手机真机（发送端）      │              │  桌面接收器 gsy-cast-receiver │
│  Android App / Debug    │              │  纯 JVM，Java 17+             │
│                         │              │                              │
│  GSYVideoPlayer Demo    │              │  ┌────────────────────────┐  │
│  ├─ SampleCastControl…  │              │  │ jUPnP Registry         │  │
│  ├─ GSYCastManager      │  SSDP 组播   │  │ + LocalDevice(Renderer)│  │
│  └─ DLNAProvider        │◄────────────►│  │   ├─ AVTransport       │  │
│                         │              │  │   ├─ RenderingControl  │  │
│                         │  HTTP 拉流   │  │   └─ ConnectionManager │  │
│                         │◄────────────►│  └────────────────────────┘  │
│                         │              │  ┌────────────────────────┐  │
│                         │              │  │ ReceiverPlayer         │  │
│                         │              │  │ ├─ VLCJ (默认)          │  │
│                         │              │  │ └─ HeadlessPlayer (CI)  │  │
│                         │              │  └────────────────────────┘  │
│                         │              │  ┌────────────────────────┐  │
│                         │              │  │ Swing UI + LogPanel    │  │
│                         │              │  │ HTTP /log/tail 端点     │  │
│                         │              │  └────────────────────────┘  │
└─────────────────────────┘              └──────────────────────────────┘
        ▲                                             ▲
        └─────────── 同一 Wi-Fi / 网段 ────────────────┘
```

---

## 2. 技术选型

| 层 | 选型 | 版本锚点 | 理由 |
| --- | --- | --- | --- |
| 语言 | Java 17 | 与 [gradle.properties](../gradle.properties) 现有 target 保持一致，不引入 Kotlin/JVM 冲突 |
| UPnP | `org.jupnp:org.jupnp:3.0.2` | Cling 官方继任者，社区活跃，接收/发送端可共用协议实现 |
| 视频渲染 | `uk.co.caprica:vlcj:4.8.2` + 系统 libvlc | 支持 HLS/DASH/RTMP/HTTP/本地文件，无需自己写解码 |
| UI | Swing + `CallbackMediaPlayerComponent` | VLCJ 与 JavaFX 兼容有坑，Swing 是最稳的桌面方案 |
| 构建 | Gradle Application plugin | 与主项目 [build.gradle](../build.gradle) 一体化 |
| 日志 | `java.util.logging` + 内嵌 SimpleHttpServer | 无三方依赖，格式对齐 Android 端 [Debuger.java](../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/utils/Debuger.java) |

**为什么不用 Kotlin/Compose Desktop**：VLCJ 需要 native window handle，Compose Desktop 的 `SwingPanel` 嵌套 native window 在部分 macOS/Linux 版本上有闪烁 bug。Swing 是最稳的路径。

---

## 3. 模块结构

```
gsy-cast-receiver/                             # 项目根新增，与 gsyVideoPlayer-* 平级
├── build.gradle                               # Java 17 + Application plugin
├── README.md                                  # 快速启动
└── src/main/
    ├── java/com/shuyu/cast/receiver/
    │   ├── CastReceiverMain.java              # 入口
    │   ├── config/
    │   │   ├── ReceiverConfig.java            # 设备名 / UDN / 端口 / 播放器类型
    │   │   └── Args.java                      # CLI 参数解析
    │   ├── upnp/
    │   │   ├── VirtualRendererDevice.java     # 组装 UPnP 设备
    │   │   ├── AVTransportServiceImpl.java    # ★ 核心
    │   │   ├── RenderingControlServiceImpl.java
    │   │   ├── ConnectionManagerServiceImpl.java
    │   │   └── DidlLiteParser.java            # metadata 解析
    │   ├── player/
    │   │   ├── ReceiverPlayer.java            # 抽象接口
    │   │   ├── VlcjReceiverPlayer.java        # VLCJ 实现
    │   │   └── HeadlessReceiverPlayer.java    # CI 用，只记录不渲染
    │   ├── ui/
    │   │   ├── ReceiverFrame.java             # Swing 主窗口
    │   │   ├── VideoCanvas.java               # VLCJ 渲染画布
    │   │   ├── StatusBar.java                 # State / Position / Volume
    │   │   └── LogPanel.java                  # 内嵌日志面板
    │   ├── http/
    │   │   └── LogHttpServer.java             # /log/tail /state /health
    │   ├── fault/
    │   │   └── FaultInjector.java             # --reject-uri / --slow-response 等
    │   └── util/
    │       └── CastLogger.java                # 格式对齐 Android 端 Debuger
    └── resources/
        └── device-description.xml.template
```

---

## 4. 关键设计点

### 4.1 UPnP 服务组装（VirtualRendererDevice）

```java
LocalDevice device = new LocalDevice(
    new DeviceIdentity(UDN.uniqueSystemIdentifier(config.deviceName)),
    new UDADeviceType("MediaRenderer", 1),
    new DeviceDetails(
        config.deviceName,                               // 用户在手机端看到的名字
        new ManufacturerDetails("shuyu"),
        new ModelDetails("GSYReceiver", "GSY 投屏测试接收器", "v1")
    ),
    new LocalService[]{
        avTransportService,
        renderingControlService,
        connectionManagerService
    }
);
upnpService.getRegistry().addDevice(device);
```

**要点**：
- `UDN` 使用系统唯一 ID + `config.deviceName` 加盐，保证同机多实例可共存
- `MediaRenderer:1` 兼容主流 DLNA 客户端（含 GSYCastManager 的 DLNA provider）

### 4.2 AVTransport 动作 → ReceiverPlayer

```java
@UpnpAction
public void setAVTransportURI(
        @UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes id,
        @UpnpInputArgument(name = "CurrentURI") String uri,
        @UpnpInputArgument(name = "CurrentURIMetaData") String metadata) throws Exception {
    if (faultInjector.shouldRejectUri()) throw new ActionException(ErrorCode.ACTION_FAILED);
    CastLogger.i("SetAVTransportURI " + uri);                // ← 断言点
    Map<String, String> headers = DidlLiteParser.parseHeaders(metadata);
    receiverPlayer.load(uri, headers);
    setTransportState(TransportState.STOPPED);
}

@UpnpAction public void play(...) throws Exception {
    if (faultInjector.shouldDelay()) Thread.sleep(3000);
    receiverPlayer.play();
    CastLogger.i("onCastPlay");                              // ← 断言点
    setTransportState(TransportState.PLAYING);
}

@UpnpAction public void pause(...) { receiverPlayer.pause(); CastLogger.i("onCastPause"); setTransportState(TransportState.PAUSED_PLAYBACK); }
@UpnpAction public void seek(...)  { long ms = parseSeekMs(target); receiverPlayer.seek(ms); CastLogger.i("onCastSeek " + ms); }
@UpnpAction public void stop(...)  { receiverPlayer.stop();  CastLogger.i("onCastStop"); setTransportState(TransportState.STOPPED); }

@UpnpAction(out = { @UpnpOutputArgument(name = "Track", getterName = "getTrack") })
public PositionInfo getPositionInfo(...) {
    long duration = receiverPlayer.getDuration();
    long position = receiverPlayer.getPosition();
    return new PositionInfo(0, ProtocolInfo.formatDuration(duration), currentUri,
                            ProtocolInfo.formatDuration(position), ProtocolInfo.formatDuration(position));
}
```

**要点**：每一条 action 都产生**唯一格式的日志**（`onCast<Action> [payload]`），测试脚本 curl `/log/tail` 后 grep 即可断言。

### 4.3 播放器抽象

```java
public interface ReceiverPlayer {
    void load(String uri, Map<String, String> headers);
    void play(); void pause(); void stop();
    void seek(long positionMs);
    long getPosition(); long getDuration();
    void addListener(PlayerEventListener l);
    void release();
}
```

- **VlcjReceiverPlayer**（默认）：`MediaPlayerFactory.newEmbeddedMediaPlayer()`，Canvas 挂 Swing Frame
- **HeadlessReceiverPlayer**（`--headless` / CI 用）：仅记录 URI / position，用 `ScheduledExecutorService` 模拟推进 position

### 4.4 UI 布局（Swing）

```
┌─────────────────────────────────────────────────────────────┐
│ GSY Cast Receiver  [Device: GSY-Desk-01]  [IP: 192.168.1.5] │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│                                                             │
│                   [VLCJ VideoCanvas]                        │
│                                                             │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│ State: PLAYING   Pos: 00:12/03:20   Vol: 60%   URI: http... │
├─────────────────────────────────────────────────────────────┤
│ [Log] tail 200                                              │
│ 14:22:01.123 SetAVTransportURI http://.../test.mp4          │
│ 14:22:01.130 VLCJ opening media...                          │
│ 14:22:02.001 onCastPlay                                     │
│ 14:22:15.412 onCastSeek 30000                               │
│ 14:22:15.610 VLCJ time-changed 30012                        │
└─────────────────────────────────────────────────────────────┘
```

日志面板与 `LogHttpServer` 共享 ring buffer（`ArrayDeque<String>`, size=500）。

### 4.5 HTTP 端点（供测试脚本使用）

| 端点 | 方法 | 用途 |
| --- | --- | --- |
| `/health` | GET | 健康检查，返回 `OK` + 版本 |
| `/state` | GET | 返回 JSON：`{state, uri, position, duration, device_name}` |
| `/log/tail?n=200` | GET | 返回最近 N 行日志 |
| `/log/grep?q=xxx&since=ts` | GET | 供脚本按关键词轮询 |
| `/fault?mode=reject-uri&enable=1` | POST | 动态开关故障注入（无需重启） |

### 4.6 故障注入模式

| 模式 | CLI / API | 发送端预期 |
| --- | --- | --- |
| `--reject-uri` | `SetAVTransportURI` 返回 501 | Android `onCastError`，回退本地 |
| `--slow-response=3000` | 所有 action 延迟 3s | Android loading UI 出现，不 ANR |
| `--drop-after-play=10000` | 起播 10s 后关 SSDP 通告 | Android 检测断连，触发退投 |
| `--wrong-position=+30000` | `getPositionInfo` 上报位置偏移 30s | Android UI 进度不错乱 |

---

## 5. 命令行参数

```
Usage: java -jar gsy-cast-receiver.jar [options]

  --device-name <name>       设备名 (default: GSY-Desk-01)
  --port <port>              UPnP HTTP 端口 (default: 0 = 随机)
  --log-port <port>          日志 HTTP 端点端口 (default: 18080)
  --player {vlcj|headless}   播放器实现 (default: vlcj)
  --headless                 无 UI 模式，等价于 --player=headless
  --network <cidr>           限定 SSDP 通告的网段 (default: 全部)
  --reject-uri               故障：拒绝 SetAVTransportURI
  --slow-response <ms>       故障：所有 action 延迟 ms 毫秒
  --drop-after-play <ms>     故障：起播后 ms 断开
  --wrong-position <delta>   故障：位置上报偏移
  --log-level {info|debug|trace}
```

---

## 6. Gradle 集成

### 6.1 `settings.gradle`（追加）

```groovy
include ':gsy-cast-receiver'
```

**注意**：`gsy-cast-receiver` 是**纯 JVM 项目**，不能被 Android app 依赖，`app/build.gradle` 不要 include 它。

### 6.2 `gsy-cast-receiver/build.gradle`

```groovy
plugins { id 'application'; id 'java' }
java { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
application { mainClass = 'com.shuyu.cast.receiver.CastReceiverMain' }
dependencies {
    implementation 'org.jupnp:org.jupnp:3.0.2'
    implementation 'uk.co.caprica:vlcj:4.8.2'
    implementation 'org.slf4j:slf4j-simple:2.0.13'
    testImplementation 'junit:junit:4.13.2'
}
run { standardInput = System.in; args = project.hasProperty('appArgs') ? project.appArgs.split(' ') : [] }
```

### 6.3 启动命令

```powershell
# 默认模式（Swing UI + VLCJ）
./gradlew :gsy-cast-receiver:run

# 传参
./gradlew :gsy-cast-receiver:run -PappArgs="--device-name GSY-Desk-01 --port 8080"

# CI headless
./gradlew :gsy-cast-receiver:run -PappArgs="--headless"

# 打包 fat jar
./gradlew :gsy-cast-receiver:jar
java -jar gsy-cast-receiver/build/libs/gsy-cast-receiver-*.jar --device-name GSY-Desk-01
```

---

## 7. VLCJ 安装要求（人工）

VLCJ 是 JNA 绑定，需要系统安装 libvlc（VLC Player 官方版即可）。

| 平台 | 安装方式 |
| --- | --- |
| Windows | 官网下载 VLC 3.x 64bit，安装后自动可用 |
| macOS | `brew install --cask vlc` |
| Linux | `sudo apt install vlc` 或 `sudo dnf install vlc` |

首次运行若报 `NativeLibraryNotFoundException`，需在 `CastReceiverMain` 中通过 `System.setProperty("jna.library.path", "<vlc install path>")` 手动指定路径。启动脚本会自动探测常见路径。

---

## 8. 与已有能力的隔离保证

| 现有资产 | 影响 | 保证 |
| --- | --- | --- |
| Android 主项目编译链 | 无 | `gsy-cast-receiver` 是 Java `application` 插件，与 Android Gradle Plugin 完全隔离 |
| [publish.gradle](../gradle/publish.gradle) 发布 | 无 | `gsy-cast-receiver` 不 apply 任何 publish 脚本 |
| 现有 39 个 Java Demo 冷烟测 | 无 | 桌面接收器不在 Android 侧，不影响 APK |
| CI [.github/workflows/ci.yml](../.github/workflows/ci.yml) | 微增 | 追加一步 `./gradlew :gsy-cast-receiver:build`，若失败不阻断 Android 发布 |

---

## 9. 测试建议

参见 [CAST_TEST_PLAYBOOK.md](./CAST_TEST_PLAYBOOK.md)。桌面接收器自身单元测试重点：

- `DidlLiteParserTest`：不同电视厂商 metadata 兼容
- `AVTransportServiceImplTest`：action 序列 + 状态机
- `FaultInjectorTest`：4 种故障模式行为符合预期
- `LogHttpServerTest`：`/log/tail`、`/log/grep` 端点稳定

运行：
```powershell
./gradlew :gsy-cast-receiver:test
```

---

## 10. 后续演进

| 阶段 | 内容 |
| --- | --- |
| M2 完成 | 桌面接收器覆盖 DLNA 全动作，作为默认测试端 |
| M3 | 引入 `--record-session` 录制手机指令流，回放做回归 |
| M4 | 追加 Chromecast Discovery Emulator（可选，参考 `castmate` / `pychromecast` 思路） |
| 长期 | 提供 Docker 镜像 `gsy-cast-receiver:latest`，一键 `docker run` 起协议链 |

---

## 11. 变更记录

| 日期 | 版本 | 内容 |
| --- | --- | --- |
| 2026-07-01 | v0.1 | 桌面接收器初始设计，M0 落地 |
