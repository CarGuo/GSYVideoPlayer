# CAST_TEST_PLAYBOOK — 投屏能力测试方案

> 立项日期：2026-07-01
> 分支：`feature/cast-capability`
> 关联：[CAST_FEATURE_PLAN.md](./CAST_FEATURE_PLAN.md) · [CAST_RECEIVER_DESIGN.md](./CAST_RECEIVER_DESIGN.md) · [JAVA_TEST_PLAYBOOK.md](./JAVA_TEST_PLAYBOOK.md)

---

## 0. 首要红线（对齐 [CAST_FEATURE_PLAN.md#0](./CAST_FEATURE_PLAN.md#L10-L28)）

任何测试**不允许**：
1. 修改主项目源码测试基线；
2. 让 [java_basic_regression.sh](./test_scripts/java_basic_regression.sh) 或 [java_cold_smoke.sh](./test_scripts/java_cold_smoke.sh) 通过率下降；
3. 强制引入新依赖到 `app` / `gsyVideoPlayer-java`。

**每轮 M PR 合入前**必须回归 4 项断言：

| # | 命令 | 期望 |
| --- | --- | --- |
| 1 | `./gradlew assembleDebug` | 全绿 |
| 2 | `./gradlew :gsyVideoPlayer-java:test` | 全绿 |
| 3 | `bash doc/test_scripts/java_basic_regression.sh "Detail模式"` | Cast-A~E 覆盖前保持 A/B/C/D 全绿 |
| 4 | `bash doc/test_scripts/java_cold_smoke.sh` | ≥ 38/39（M2 后为 ≥ 39/40） |

---

## 1. 测试目标（能测通的 5 个可断言事实）

| 编号 | 能力 | 通过判据（发送端 logcat + 接收端 UPnP 事件） |
| --- | --- | --- |
| Cast-A | 设备发现 | Android `onDeviceFound: <FriendlyName>` 在 5s 内出现（jUPnP RegistryListener 回调） |
| Cast-B | 连接投屏 | Android `onCastConnected`；接收端 AVTransport1 收到 `SetAVTransportURI` action |
| Cast-C | 远端起播 + 本地暂停 | 接收端 `Play` action + `TransportState=PLAYING`；Android `changeUiToPauseShow` + `CURRENT_STATE_PLAYING (remote)` |
| Cast-D | 远端 seek | 接收端 `Seek` action（Unit=REL_TIME）；Android `onCastSeek complete`；两端 position 差 ≤ 2s |
| Cast-E | 退投恢复本地 | 接收端 `Stop` action；Android `onSeekComplete` 到远端 last position + `CURRENT_STATE_PLAYING` |

---

## 2. 四层测试金字塔

对齐 [JAVA_TEST_PLAYBOOK.md](./JAVA_TEST_PLAYBOOK.md) 已有方法论。

### 2.1 Layer 1 — JVM 单元测试（占 60%）

**位置**：`gsyVideoPlayer-java/src/test/java/com/shuyu/gsyvideoplayer/cast/...`（Android JUnit4，覆盖 SPI + jUPnP DLNA 实现）+ `app/src/test/java/com/example/gsyvideoplayer/cast/...`（覆盖本地 loopback receiver 装配逻辑）。

命名风格对齐 [GSYSubtitleParserTest.java](../gsyVideoPlayer-java/src/test/java/com/shuyu/gsyvideoplayer/subtitle/GSYSubtitleParserTest.java) / [GSYVideoPreviewVttParserTest.java](../gsyVideoPlayer-java/src/test/java/com/shuyu/gsyvideoplayer/preview/GSYVideoPreviewVttParserTest.java)。

| 测试类 | 断言点 |
| --- | --- |
| `CastCapabilityTest` | 能力枚举稳定；capability 组合的 hash/equals |
| `CastDeviceParserTest` | jUPnP `RemoteDevice` / DIDL-Lite metadata → `CastDevice` 字段完整 |
| `CastMediaInfoTest` | URL / Header / Subtitle track 序列化，UTF-8 转义 |
| `CastSessionStateTest` | 状态机 `IDLE → CONNECTING → PLAYING → PAUSED → DISCONNECTED`，禁跳 `IDLE → PLAYING` |
| `CastEventDebouncerTest` | 高频 `onProgress` 节流（100ms） |
| `DlnaCastProviderTest` | `RegistryListener` 事件 → `onDeviceFound`/`onDeviceLost` 映射 |
| `DlnaCastSessionActionTest` | `SetAVTransportURI` / `Play` / `Pause` / `Stop` / `Seek` / `GetPositionInfo` 参数拼装与错误码分类 |
| （app 侧）`DevReceiverServiceLifecycleTest` | 前台 Service 的 jUPnP `UpnpService` 启停幂等，`LocalDevice` UDN 稳定 |
| （app 侧）`LoopbackAvTransportServiceTest` | AVTransport1 各 action 回调驱动本机播放器的状态机分支 |

**运行**：

```powershell
./gradlew :gsyVideoPlayer-java:test
./gradlew :app:testDebugUnitTest
```

目标：**PR 阻断，通过率 100%**。

### 2.2 Layer 2 — 协议集成测试（App 内 loopback receiver）

**思路**：在同一台设备内，`DevReceiverService(:dlna)` 前台 Service 内起 jUPnP `UpnpService` + `LocalDevice`(MediaRenderer:1) + `LoopbackAvTransportService`，跑真实 SSDP/UPnP 协议链（loopback 地址 + jUPnP 自带 StreamServer），发送端 `DlnaCastProvider` 通过 `RegistryListener` 发现该本机 renderer 并推流。

- 位置：`app/src/androidTest/java/com/example/gsyvideoplayer/cast/...`
- 工具：`androidx.test` + `AndroidJUnit4`

关键用例：

| 用例 | 步骤 | 断言 |
| --- | --- | --- |
| `discovery_within_5s` | 启动 `DevReceiverService(:dlna)` 后建 `DlnaCastProvider`，5s 内发现本机 renderer | `deviceList.size ≥ 1`（`RegistryListener.remoteDeviceAdded` 触发） |
| `load_and_play` | 建立 `CastSession` → `SetAVTransportURI`(HLS demo) → `Play` | 3s 内 `GetTransportInfo.CurrentTransportState=PLAYING` |
| `seek_accuracy` | `Seek Unit=REL_TIME Target=00:00:30` | `GetPositionInfo.RelTime` ∈ [28s, 32s] |
| `disconnect_cleanup` | 关闭 Service 后 SSDP `byebye` 已发送，jUPnP `UpnpService.shutdown()` 完成 | `Registry` 空 + 端口无泄漏（`netstat` 检测） |
| `reject_uri_fallback` | `LoopbackAvTransportService` 显式拒 URI（模拟）→ Android 触发 `onCastError` | 本地回退，UI 不崩溃 |

**运行**：

```powershell
# 单机自投自收：拉起 CastDemoActivity 后开启内置 loopback receiver 开关
./gradlew :app:connectedAndroidTest
```

### 2.3 Layer 3 — UI 自动化（沿用 J 轮 adb + logcat 断言）

**新增脚本**：[doc/test_scripts/java_cast_regression.sh](./test_scripts/java_cast_regression.sh)（M2 落地），完全模仿 [java_basic_regression.sh](./test_scripts/java_basic_regression.sh) 的 A/B/C/D 四段式，追加 Cast-A ~ Cast-E。

**关键坑位（对齐 [JAVA_TEST_PLAYBOOK.md#6](./JAVA_TEST_PLAYBOOK.md#L226-L246) 5 大坑）**：

1. **入口进入路径**：投屏 demo Activity 也 `exported=false`，必须 `am start MainActivity` → 滚动 → 点击 "投屏DEMO"
2. **控件 ID 前缀**：`com.example.gsyvideoplayer:id/cast_btn`（**不是** `com.shuyu.gsyvideoplayer:id/...`）
3. **controls 2s 自动隐藏**：dump 前先 `tap (540,545)` 唤起
4. **textAllCaps**：按钮 dump 是 "投屏DEMO"（大写英文段）
5. **Dialog 层级**：`CastDeviceListDialog` 需 `uiautomator dump --compressed`

**脚本骨架**：

```bash
#!/bin/bash
# 前置：App 内 DevReceiverService(:dlna) 已启用（悬浮按钮打开）；单机 loopback，无需 PC
adb shell am start -n com.example.gsyvideoplayer/.MainActivity
scroll_and_tap "投屏DEMO"
tap 540 545                          # 唤起 controls
tap_by_id "cast_btn"

# Cast-A: 设备发现（本机 MediaRenderer 通过 jUPnP RegistryListener 5s 内出现）
wait_logcat "remoteDeviceAdded.*MediaRenderer" 5

tap_by_text "$(get_first_renderer_label)"
wait_logcat "DlnaCastSession.*CONNECTED" 5      # Cast-B（AVTransport1 SetAVTransportURI 已发出）

# Cast-C: 手机端本地暂停 + receiver receipt 起播
assert_logcat "changeUiToPauseShow"
assert_logcat "LoopbackAvTransportService.*SetAVTransportURI"
assert_logcat "LoopbackAvTransportService.*Play"

# Cast-D: seek
swipe_progress 12 80
wait_logcat "LoopbackAvTransportService.*Seek.*REL_TIME" 3
wait_logcat "onCastSeek complete" 3

# Cast-E: 退投恢复
tap_by_id "cast_disconnect"
wait_logcat "LoopbackAvTransportService.*Stop" 3
wait_logcat "onSeekComplete" 3
assert_logcat "CURRENT_STATE_PLAYING"
```

### 2.4 Layer 4 — 端到端手工回归矩阵（真机 + 真设备）

真接收端（真电视）+ 网络异常等长尾场景必须手工验证：

| 维度 | 组合 | 覆盖点 |
| --- | --- | --- |
| **内核 × 投屏** | IJK / EXO / System × 投屏 = 3 组 | URL / Header 传递一致 |
| **源类型** | HTTP / HLS / DASH / 本地文件 = 4 | 本地文件触发 `LocalHttpServer`，退投端口关闭 |
| **接收端** | App 内 loopback (`DevReceiverService(:dlna)`) + 真 DLNA 电视 + 第三方 SPI (Chromecast/乐播 flavor) = ≥3 | 三家协议路径起播 |
| **弱网/断网** | Wi-Fi 掉线 / SSDP 多播被路由屏蔽 / 接收端断电 | 触发 `onCastError`，UI 回退 |
| **生命周期** | 后台切前台 / 锁屏 / 旋转 | 投屏态不丢，位置同步不错乱 |
| **Compose 端** | [ComposeDemoListActivity](../app/src/main/java/com/example/gsyvideoplayer/compose/ComposeDemoListActivity.kt) 新增入口 | 与 [GSYComposeHostPlayer.java](../gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/native_/GSYComposeHostPlayer.java) 生命周期联动无泄漏 |

---

## 3. 手机 单机 loopback 端到端最小闭环

### 3.1 前置准备（一次性）

1. **接收端**：App 内前台 Service `DevReceiverService(:dlna)` 拉起 jUPnP `UpnpService` + `LocalDevice(MediaRenderer:1)` + `LoopbackAvTransportService`。同机 loopback + jUPnP 自带 StreamServer，**不再需要桌面 receiver、不需要跨 PC 同网段、不需要防火墙特殊放行**。
2. **权限**：`FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_MEDIA_PLAYBACK` + `ACCESS_WIFI_STATE` + `CHANGE_WIFI_MULTICAST_STATE` + `POST_NOTIFICATIONS`（Android 13+）
3. **多播锁**：省电模式下部分设备（Xiaomi/华为）会丢 SSDP 组播，`DevReceiverService` 启动时 `WifiManager.MulticastLock.acquire()`
4. **手机安装 Demo**：
   ```powershell
   ./gradlew :app:installDebug
   ```

### 3.2 一次跑通流程（Cast-A ~ Cast-E）

| 步骤 | 手机操作 | Receiver（App 内 loopback）预期 | logcat / UPnP action 断言 |
| --- | --- | --- | --- |
| 1 | 打开 App → 点 "投屏DEMO" | — | 进入 `CastDemoActivity` |
| 2 | 点右下角 "启用回环接收器" 悬浮按钮 | `DevReceiverService(:dlna)` 前台通知栏出现 | `UpnpService started` + `LocalDevice announced` |
| 3 | 点播放器右上 "投屏" 按钮 | 设备列表出现本机 `MediaRenderer` FriendlyName | `RegistryListener.remoteDeviceAdded` |
| 4 | 选中本机 renderer | `LoopbackAvTransportService.SetAVTransportURI` + `Play` 被调用；PIP receipt Activity 起来真解码渲染 | `DlnaCastSession → CastState.CONNECTED → PLAYING`；`TransportState=PLAYING` |
| 5 | 手机本地进度条拖到 50% | `Seek Unit=REL_TIME Target=xx:xx:xx`；receipt Activity 跳转 | `onCastSeek complete`，差 ≤ 2s |
| 6 | 点击暂停 | `Pause` action；receipt 画面静止 | `TransportState=PAUSED_PLAYBACK` |
| 7 | 点击"断开投屏" | `Stop` action；receipt Activity 退出 | `onSeekComplete` 回本地 last position + 本地 `CURRENT_STATE_PLAYING` |

### 3.3 一键联调命令

```powershell
./gradlew :app:installDebug
adb shell am start -n com.example.gsyvideoplayer/.CastDemoActivity
adb logcat -s DlnaCastSession NsdDiscoveryCtrl CastControlVideo DevReceiverService
```

---

## 4. 冷烟测挂载

参照 [java_cold_smoke.sh](./test_scripts/java_cold_smoke.sh) 的 38/39 基线，M2 后追加：

```
投屏DEMO → CastDemoActivity
```

冷烟仅断言"进入 Activity 且无 FATAL/ANR"，**即使无接收端也能跑通**（Cast SDK 允许在无设备时 idle 等待）。

**新基线**：≥ 39/40（97.5%），不允许下滑。

---

## 5. CI 接入策略

| 层 | 是否上 CI | 位置 |
| --- | --- | --- |
| Layer 1 单元测试（Android/gsyVideoPlayer-java + app） | ✅ 必上 | [.github/workflows/ci.yml](../.github/workflows/ci.yml) 的 `./gradlew test` 阶段 |
| Layer 2 协议集成（本机 loopback） | ⚠️ 可选 | 需 emulator runner + SSDP 多播（jUPnP StreamServer + `MulticastLock`），先本地跑；后续用 CI Linux runner + 有 Wi-Fi 模拟器 |
| Layer 3 UI 自动化 | ❌ 暂缓 | 与 J 轮结论一致，先本地跑 |
| Layer 4 手工回归 | — | 发版前 checklist |

---

## 6. "最少测通"路径（如果只有 1 天）

如果时间紧张，走这个最短闭环，覆盖 Cast-A/B/C/E 4 个断言（**单机 loopback，无需 PC**）：

1. `./gradlew :app:installDebug`，`adb shell am start -n com.example.gsyvideoplayer/.CastDemoActivity`
2. 点右下角 "启用回环接收器" 悬浮按钮 → 通知栏出现前台 Service
3. 点播放器右上 "投屏" → 设备列表出现本机 renderer FriendlyName → 选中
4. PIP receipt Activity 起来真解码；拉进度条 → receipt 内视频跳转
5. 点"断开投屏" → 手机本地续播

无需自动化脚本，肉眼可断言 4/5 能力点。

---

## 7. 日志埋点约定（发送端）

对齐 [Debuger.java](../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/utils/Debuger.java) 现有习惯，在 `CastSession` / `DlnaCastSession` 生命周期回调 + `DlnaCastProvider` 发现回调关键节点打日志：

```java
// CastListener 回调（gsyVideoPlayer-java/cast/CastListener.java）
Debuger.printfLog("onDeviceFound: " + device.getFriendlyName());
Debuger.printfLog("onCastConnecting: " + device.getUdn());
Debuger.printfLog("onCastConnected");
Debuger.printfLog("onCastStateChanged: " + castState);   // IDLE/CONNECTING/CONNECTED/PLAYING/PAUSED
Debuger.printfLog("onCastProgress: " + positionMs + "/" + durationMs);
Debuger.printfLog("onCastError " + code + " " + msg);
Debuger.printfLog("onCastDisconnected");

// DLNA action 落点（DlnaCastSession 内部）
Debuger.printfLog("AVTransport1.SetAVTransportURI " + uri);
Debuger.printfLog("AVTransport1.Play");
Debuger.printfLog("AVTransport1.Pause");
Debuger.printfLog("AVTransport1.Seek REL_TIME " + hhmmss);
Debuger.printfLog("AVTransport1.Stop");
```

**Tag 统一使用** `GSYCast` / `DlnaCastSession`，脚本用 `adb logcat -s GSYCast DlnaCastSession` 抓取，避免与主库日志混淆。

---

## 8. 文档沉淀节奏

| 阶段 | 文档 |
| --- | --- |
| M0 | 本文件 + [CAST_FEATURE_PLAN.md](./CAST_FEATURE_PLAN.md) + [CAST_RECEIVER_DESIGN.md](./CAST_RECEIVER_DESIGN.md) + [ARCHITECTURE.md](./ARCHITECTURE.md) 追加一行 |
| M1 | 更新本文件 §5 CI 章节，附上编译截图 |
| M2 | 补充 5 项能力实证日志（对齐 [JAVA_TEST_PLAYBOOK.md#3](./JAVA_TEST_PLAYBOOK.md#L58-L148) 格式） |
| M3 | 补充 5 个 Activity 覆盖矩阵（对齐 [JAVA_TEST_PLAYBOOK.md#4](./JAVA_TEST_PLAYBOOK.md#L151-L168)） |
| M4 | 补充 3 种协议 provider × 3 类接收端矩阵 |

---

## 9. 变更记录

| 日期 | 版本 | 内容 |
| --- | --- | --- |
| 2026-07-01 | v0.1 | 测试方案初稿，M0 落地 |
| 2026-07-01 ~ 2026-07-02 | v0.2 ~ v0.13 | 历史迭代（已废弃架构：自研 HTTP/JSON 协议 + 桌面 receiver + `adb reverse` loopback 兜底 + 三条硬编码 mock 设备发现），整段记录因架构重定向已下线，详见 §10 |
| 2026-07-02 | v0.14 | 架构重定向：全面切到 jUPnP 3.0.3 DLNA/UPnP + 内核 SPI（CastCapability / CastProvider / CastSession）；loopback 走 `DevReceiverService(:dlna)` + jUPnP `LocalDevice` + `LoopbackAvTransportService`；桌面 receiver 模块已下线；测试判据以标准 UPnP AVTransport1 action 为准 |

---

## 10. 历史章节归档说明（2026-07-02 v0.14 起）

原 §10 M4 真机端到端联调实测 及其后续所有小节（§10.1 ~ §10.14，涵盖 M4 / M5-a / M5-b / M6 / M7-a-1 ~ M7-a-4 各期真机 adb 冒烟记录、协议契约常量 anchor、跨子网 mDNS 硬证据、Windows 多网卡 setsockopt 修复等）**已因架构重定向到 jUPnP 3.0.3 DLNA/UPnP + 内核 SPI 而整体下线**。

原因：这些记录围绕以下已废弃组件展开，其判据、日志匹配串、协议契约与当前架构不再兼容：

- 自研 HTTP/JSON 6-endpoint 协议（`POST /cast/{load,play,pause,stop,seek}` + `GET /cast/status`）→ 由标准 UPnP AVTransport1 action 取代（`SetAVTransportURI` / `Play` / `Pause` / `Stop` / `Seek` / `GetPositionInfo`）
- `_gsycast._tcp.local.` 私有 mDNS 服务 + TXT 契约 `proto=gsy-cast / ver / port / backend / spec` → 由 UPnP SSDP + `urn:schemas-upnp-org:device:MediaRenderer:1` 取代
- 桌面 JVM receiver（`gsy-cast-receiver` 独立模块 + `--headless` / `--backend fx` / JavaFX MediaPlayer）→ 由 App 内 `DevReceiverService(:dlna)` + jUPnP `LocalDevice` + `LoopbackAvTransportService` 单机 loopback 取代
- 私协议侧发送端组件（发送侧 mDNS parser、NsdManager 集成、`submitDiscoveredCandidates` mock 融合、跨 /24 `adb reverse` 兜底） → 由 jUPnP `RegistryListener` + `DlnaCastProvider` 一体化取代
- Loopback 命名约定（含设备 model 后缀的私有 friendly name 格式）→ 由 UPnP 标准 FriendlyName / UDN 取代

**若需查阅历史记录**：请在 git 历史中检出 v0.13 及之前的版本（tag/commit 参见分支 `feature/cast-capability` 早期提交），本文件不再内嵌旧判据表、旧脚本、旧协议契约。所有新增测试用例、判据表、日志匹配串**必须以 §1~§8 为准**。
