# CAST_FEATURE_PLAN — 投屏能力规划

> 立项日期：2026-07-01
> 分支：`feature/cast-capability`
> 基线：master @ `v13.1.0`（commit `5cbab191`）
> 关联文档：[CAST_RECEIVER_DESIGN.md](./CAST_RECEIVER_DESIGN.md) · [CAST_TEST_PLAYBOOK.md](./CAST_TEST_PLAYBOOK.md) · [ARCHITECTURE.md](./ARCHITECTURE.md)

---

## 0. 首要红线（不可破坏项）

本次投屏能力的 **零号约束**：**不得破坏 GSYVideoPlayer 已有任何能力**。

具体展开为：

1. **不改动播放内核**：`IJK / EXO(Media3) / System` 三大内核代码零改动，投屏能力通过 **内核 SPI（CastCapability / CastProvider / CastSession）** 挂接，UI overlay + 管理层与主播放器解耦。
2. **不动 `gsyVideoPlayer-java` 公共 API**：`GSYBaseVideoPlayer`、`StandardGSYVideoPlayer`、`GSYVideoControlView`、`GSYVideoManager` 等对外 API 只允许**新增方法**，不改签名、不改语义、不改默认行为。SPI 接口以新增形式引入。
3. **不改 Demo 默认行为**：`MainActivity` 现有 39 个入口按钮的顺序/文本/回调保持不变；投屏 Demo 作为**新按钮追加到列表末尾**。
4. **不新增独立发布 module**：投屏能力（SPI + jUPnP DLNA/UPnP 实现）**并入 `gsyVideoPlayer-java`**（或作为可选源集），`settings.gradle` 不追加新 include；主发布链 `com.shuyu:gsyvideoplayer-java` 不受影响。
5. **不改测试基线**：J 轮基线的 4 项基础能力（播放/暂停、拖动进度、全屏切换、切换内核）在 `DetailPlayer` 上 100% 通过率必须保持；`java_cold_smoke.sh` 的 38/39 (97.4%) 通过率不允许下滑。
6. **不引入必选新依赖到主 AAR**：投屏依赖 `jUPnP 3.0.3`（DLNA/UPnP）以可选方式引入；三方厂商 SDK（乐播等）保持 flavor / 独立 aar 分发。

每一次 PR 合入前，必须回归下列 4 个断言：

- `./gradlew assembleDebug` 全绿
- `./gradlew :gsyVideoPlayer-java:test` 全绿
- `bash doc/test_scripts/java_basic_regression.sh "Detail模式"` 全绿
- `bash doc/test_scripts/java_cold_smoke.sh` ≥ 38/39

---

## 1. 能力目标

在 GSYVideoPlayer 上补齐"投屏"能力，覆盖 5 个可断言的能力点：

| 编号 | 能力 | 判据 |
| --- | --- | --- |
| Cast-A | 设备发现 | 5s 内出现至少 1 台设备，`onDeviceFound` 日志出现 |
| Cast-B | 连接投屏 | `onCastConnecting` → `onCastConnected`，耗时 < 5s |
| Cast-C | 远端起播 + 本地暂停 | 远端 `PLAYING` + 本地 `changeUiToPauseShow` |
| Cast-D | 远端 seek 回传 | `onCastSeek complete`，UI 进度差 ≤ 2s |
| Cast-E | 退投恢复本地 | 断开后 `onSeekComplete` 到远端最后 position，本地恢复 `CURRENT_STATE_PLAYING` |

---

## 2. 分层架构

对齐 [ARCHITECTURE.md](./ARCHITECTURE.md#L4-L5) 中的原则 **"播放内核只负责媒体解码和时间线，跨内核能力尽量放在播放器 UI 或 GSY 管理层"**。

```
┌─────────────────────────────────────────────────────────────┐
│ Demo 层 (app/)                                              │
│   ├─ MainActivity 新增按钮："投屏DEMO"（追加，不改现有）      │
│   ├─ CastDemoActivity                                       │
│   └─ SampleCastControlVideo extends StandardGSYVideoPlayer  │
├─────────────────────────────────────────────────────────────┤
│ UI overlay 层                                               │
│   ├─ CastButton (video_layout_standard.xml 追加，非必现)     │
│   └─ CastDeviceListDialog                                   │
├─────────────────────────────────────────────────────────────┤
│ 内核 SPI 层（并入 gsyVideoPlayer-java）                     │
│   ├─ CastCapability     ── 能力声明（是否支持 DLNA/其他协议）│
│   ├─ CastProvider       ── 发现 / 建连 / 会话工厂            │
│   ├─ CastSession        ── 单次投屏会话：load/play/pause/    │
│   │                        stop/seek/status/事件回调         │
│   ├─ 缺省实现：jUPnP 3.0.3 DLNA/UPnP                         │
│   │    ├─ DlnaCastProvider  ── Registry + Discovery          │
│   │    └─ DlnaCastSession   ── AVTransport1 action 封装      │
│   └─ 三方厂商实现（Chromecast / 乐播 等）走同一 SPI 挂接      │
├─────────────────────────────────────────────────────────────┤
│ 本地 loopback 接收端（Demo 层，用于单机自投自收验收）        │
│   DevReceiverService(:dlna) 前台 Service 内                 │
│     ├─ jUPnP LocalDevice（MediaRenderer:1）                 │
│     └─ LoopbackAvTransportService                           │
│   通过 jUPnP UDA-SSDP 在本机注册 & 被同 App 内的 sender 发现 │
└─────────────────────────────────────────────────────────────┘
```

**关键设计**：SPI（CastCapability/CastProvider/CastSession）与 `GSYVideoManager` 解耦，通过事件回调对接 Demo；本地播放器不感知远端存在，只按 Demo 层调度进入"暂停 + 覆盖 CastOverlay"状态。本地验收（同机自投自收）走 `DevReceiverService(:dlna)` 里托管的 jUPnP `LocalDevice` + `LoopbackAvTransportService`，与线上真投屏走**完全相同的 DLNA/UPnP 协议**，仅设备位置不同。

---

## 3. 模块划分

### 3.1 无新增独立发布 module

投屏能力（SPI + jUPnP DLNA/UPnP 缺省实现）**并入 `gsyVideoPlayer-java`**，不再走"新增独立 module"路线；`settings.gradle` 无 include 追加。三方厂商 SDK（乐播等闭源）仍走 flavor / 独立 aar 分发。

### 3.2 `gsyVideoPlayer-java` 内 cast 目录建议结构

```
gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cast/
├── CastCapability.java        （SPI：能力声明）
├── CastProvider.java          （SPI：发现 / 建连 / 会话工厂）
├── CastSession.java           （SPI：单次会话生命周期与命令面）
├── CastState.java             （枚举：IDLE/CONNECTING/PLAYING/PAUSED/...）
├── CastListener.java          （事件回调）
├── model/
│   ├── CastDevice.java
│   └── CastMediaInfo.java
└── dlna/                      （缺省实现：jUPnP 3.0.3）
    ├── DlnaCastProvider.java
    └── DlnaCastSession.java   （封装 AVTransport1 action）
```

> 注：jUPnP 依赖由该 flavor / 可选源集引入，不进入基础 AAR classpath，不影响下游只用本地播放能力的 App。

### 3.3 App 层：本地 loopback 接收端

用于同机自投自收验收（同时也是最直白的 DLNA renderer 参考实现）：

```
app/src/main/java/com/example/gsyvideoplayer/cast/
└── DevReceiverService.java     （前台 Service, foregroundServiceType=mediaPlayback）
    ├─ jUPnP UpnpService（Registry + StreamServer）
    ├─ LocalDevice: urn:schemas-upnp-org:device:MediaRenderer:1
    └─ LoopbackAvTransportService（AVTransport1 服务实现，回调驱动本地 IJK/EXO 起播）
```

---

## 4. 里程碑与推进节奏

| M | 内容 | 交付物 | 回归动作 |
| --- | --- | --- | --- |
| **M0**（本轮） | 分支 + 3 份规划文档 + ARCHITECTURE 追加 | 本 PR | 无源码改动，仅 doc/ 增加 |
| **M1** | 在 `gsyVideoPlayer-java` 内新增 cast 目录 + SPI 空骨架（CastCapability / CastProvider / CastSession），`app` 不新增依赖 | 空实现可编译 | 跑 basic_regression + cold_smoke，确认无回归 |
| **M2** | jUPnP 3.0.3 DLNA/UPnP 缺省实现：设备发现（SSDP + Registry Listener）+ AVTransport1 起播 / 暂停 / Seek / Stop；App 层 `DevReceiverService(:dlna)` 前台 Service 起 jUPnP `LocalDevice` + `LoopbackAvTransportService`，用于单机自投自收 | Demo 中新增"投屏DEMO"入口，与本机 loopback receiver 起播通链 | Cast-A ~ Cast-E 全绿 + 原有能力零回归 |
| **M3** | 位置同步、退投恢复、异常回退（`onCastError`） | `SampleCastControlVideo` + `CastOverlay` 上线 | 端到端回归 |
| **M4** | 扩展：Chromecast provider、字幕投屏、Header 透传、乐播 SDK 挂载点 | 各 provider 各自实现 SPI，独立可选 | 各 provider 独立回归 |

**每个 M 单独提交 PR**，PR 合入前必跑第 0 节 4 项断言。

---

## 5. 对已有能力的影响面盘点

| 已有能力 | 影响 | 缓解 |
| --- | --- | --- |
| [MainActivity.java](../app/src/main/java/com/example/gsyvideoplayer/MainActivity.java) 39 个入口 | 追加 1 个按钮 | 追加到 layout 末尾，不改 id/顺序；`java_cold_smoke.sh` 更新为 40/40 |
| [StandardGSYVideoPlayer.java](../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/video/StandardGSYVideoPlayer.java) | 零改动 | 投屏按钮/UI 走 Demo 子类 `SampleCastControlVideo`，不修改父类 |
| [video_layout_standard.xml](../gsyVideoPlayer-java/src/main/res/layout/video_layout_standard.xml) | 零改动 | 投屏按钮放 Demo 层 layout：`sample_cast_video.xml` |
| [PlayerFactory](../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/player/PlayerFactory.java) 三内核切换 | 零改动 | 投屏通过 SPI 与内核解耦 |
| [ARCHITECTURE.md](./ARCHITECTURE.md) "近期播放能力补充"表 | 追加 1 行 | 见 §6 |
| [gradle/dependencies.gradle](../gradle/dependencies.gradle) | 追加 `jUPnP 3.0.3` 可选依赖 | 只在启用 cast 源集时生效，不进入基础 AAR classpath |
| [publish.gradle](../gradle/publish.gradle) 发布链 | 零改动 | 投屏并入 `gsyVideoPlayer-java`，不新增发布 artifact |

---

## 6. ARCHITECTURE.md 追加行（预告，在 M0 末尾提交）

将在 [ARCHITECTURE.md](./ARCHITECTURE.md) "近期播放能力补充"表格末尾追加：

| 能力 | 所在层级 | 设计说明 |
| --- | --- | --- |
| 投屏（DLNA / Chromecast） | UI overlay + 内核 SPI（CastCapability / CastProvider / CastSession） | 内核只负责本地时间线；投屏走 jUPnP 3.0.3 DLNA/UPnP 缺省实现（AVTransport1），第三方厂商实现（Chromecast/乐播）通过同一 SPI 挂接；投屏中本地暂停并保留 UI，退投时按远端最后 position 恢复本地播放。 |

---

## 7. 风险清单

| 风险 | 影响 | 应对 |
| --- | --- | --- |
| jUPnP SSDP 组播权限被路由器屏蔽 | 发现不到设备 | 提供"手动输入 IP"回退入口；`WifiManager.MulticastLock` 全程保持 |
| 本地文件投屏需 HTTP 化 | 安全 / 端口冲突 | jUPnP 内建 StreamServer 承担 URI 服务，随机端口，退投立即关闭 |
| 电视端 Header/DRM 兼容差 | 部分源无法投 | 白名单降级：会话回调 `onCastError` → 回退本地播放 |
| jUPnP 依赖体积增量 | AAR 变大 | 只在启用 cast 源集时打入；下游不用投屏时零增量 |
| 三方 SDK license | 主库污染 | Chromecast / 乐播作为 flavor / 独立 aar 分发，实现 SPI 挂接 |
| Compose 端集成 | 生命周期泄漏 | 复用 [LifecycleBridge.kt](../gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/common/LifecycleBridge.kt) 现有机制 |

---

## 8. 下一步

1. 落地本文件 + `CAST_RECEIVER_DESIGN.md` + `CAST_TEST_PLAYBOOK.md`（M0）
2. 在 [ARCHITECTURE.md](./ARCHITECTURE.md) 追加投屏一行
3. 打 M0 PR，通过第 0 节 4 项回归后进入 M1

---

## 9. 变更记录

| 日期 | 版本 | 内容 |
| --- | --- | --- |
| 2026-07-01 | v0.1 | 立项，M0 规划文档落地 |
| 2026-07-01 ~ 2026-07-02 | v0.2 ~ v0.13 | **历史迭代（已废弃架构）**：早期分支曾探索"独立发布 module + 自研 HTTP/JSON 私协议 + 桌面 JVM 接收器 + 单机自研 mDNS 广播 + adb loopback 兜底"路线，历时 12 版迭代，其间涉及的独立 module、私协议控制服务器、私自定义 mDNS 广告/解析组件、单机 loopback 通知栏命名等**均已在 v0.14 整体下线**，仅保留能力目标（Cast-A~E）与红线约束不变。若需追溯历史决策，见 git 历史 v0.2~v0.13 commit 段。 |
| 2026-07-02 | v0.14 | **架构重定向到 jUPnP 3.0.3 DLNA/UPnP + 内核 SPI**：（1）取消独立发布 module，投屏能力（`CastCapability` / `CastProvider` / `CastSession` 三件 SPI + `dlna/` 缺省实现）并入 `gsyVideoPlayer-java`；（2）线上真投屏与本地验收**统一走 DLNA/UPnP 标准协议**——`DlnaCastProvider` 通过 jUPnP `Registry` + `RegistryListener` 做 SSDP 发现，`DlnaCastSession` 封装 `AVTransport:1` action（`SetAVTransportURI` / `Play` / `Pause` / `Stop` / `Seek` / `GetPositionInfo`）；（3）单机自投自收验收工具重构为 `DevReceiverService(:dlna)`：前台 Service (`foregroundServiceType=mediaPlayback`) 内起 jUPnP `UpnpService`，注册 `LocalDevice: urn:schemas-upnp-org:device:MediaRenderer:1` + `LoopbackAvTransportService`（AVTransport1 Service 实现，action 回调驱动本机播放器），设备名按 jUPnP UDN 规范生成；（4）废弃全部自研 HTTP/JSON 端点、自研 mDNS 广告/解析组件、adb reverse 兜底路径；（5）第三方厂商（Chromecast / 乐播）实现同一 SPI，作为可选 flavor / 独立 aar 挂接。 |

---

## 10. 历史章节归档说明

原文档 §10（GSY-Cast v1 自研 HTTP/JSON 协议规格）、§11（M4 现状盘点）、§12（M5-a JavaFX Backend）、§13（M5-b 竞态修复 + UI 反向桥）、§14（M6 UX 补齐三件套：Disconnect / Polling / Lifecycle Gate）**均描述的是 v0.2~v0.13 期间的废弃架构**（自研私协议控制端点、桌面 JVM 接收器、单机自研 mDNS 广告与解析组件、adb loopback 兜底、HeadlessPlayerBackend / JavaFxPlayerBackend 等），与当前 v0.14 落地的 **jUPnP 3.0.3 DLNA/UPnP + 内核 SPI (CastCapability/CastProvider/CastSession) + DevReceiverService(:dlna)** 架构不再吻合，故在本次整理时整体下线以避免误导后续实现者。

如需追溯历史决策（例如为何一度放弃 UPnP 走自研协议、又为何回归 DLNA），请查阅 git 历史中 v0.2~v0.13 对应的 commit。当前落地方向以本文件 §0~§9 + [CAST_TEST_PLAYBOOK.md](./CAST_TEST_PLAYBOOK.md) 为准。
