# GSYVideoPlayer Compose 能力 Backlog 与推进 Plan

> 本文件归档 [v13.0.0 ~ master `46ef8db3`] 三路并行审核（破坏性 / 能力对齐 / Demo 对齐）的全部结论，并把后续工作拆成可逐轮推进、可勾选完成的子任务。
>
> **Compose 模块当前状态：未发布（Unreleased）**。所有动作均不打 tag，随 master 滚动迭代；首发将以本文件 P4-1 全部完成 + P5-1 至少一半完成 为基线再视情况评估。
>
> 校对时间：2026-05-19。基线 commit：[`46ef8db3`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer)。

---

## 0. 全局视图

| 维度 | 现状 | 目标 |
|---|---|---|
| 是否破坏既有功能 | ✅ 0 破坏 | 持续保持（每轮 `:app:assembleDebug` + `:app:assembleRelease` + 真机 monkey + crash buffer 空） |
| Wrapper 模式 vs Java | ✅ 几乎完全等价 | 维持，仅做小修补（autoPauseResume 已默认开） |
| Native 模式 vs Java | ⚠️ "最小子集 + 逃生口堵死" | P4-1 → P4-2 推进至"等价" |
| Demo 覆盖 | 8/41 ≈ 19.5%，**13 类差异化能力空白** | P5-1 必补 8 个 → P5-2 选补 8 个 → 总覆盖 ≈ 24/41 ≈ 58%（**当前进度：20/41 ≈ 49%，P5-1 8/8 已完成；P5-2 高优 4/8（D9/D10/D11/D14）已完成**） |
| 文档与发布约束 | doc/COMPOSE_USE.md 已写明"未发布" | 持续与代码同步，每轮回归此文件 |

---

## 1. 缺失/异常能力清单（按风险等级）

### 1.1 🔴 P0 ｜ 代码核心残缺（必修，且可能影响首发）

> ✅ **R2 已全部修复**（详见 § 3 进度跟踪表）；下表保留作为历史可追溯的"问题→定位→验收→实际落点"记录。

| ID | 问题 | 现状定位 | 验收标准 | R2 落点 |
|---|---|---|---|---|
| P0-1 ✅ | `GSYPlayerController.host` 是 `internal`，Native 模式所有"Standard 已有但 Controller 未暴露"的能力（字幕 / 滤镜 / 镜像 / 列表小窗 / setSeekOnStart / 快照截图 / GIF）无法通过逃生口访问 | [GSYPlayerController.kt#L23-L24](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/native_/GSYPlayerController.kt#L23-L24) | 提供 `controller.withHost { player -> ... }` 闭包（限定主线程 + 自动 null-check + released 期间 no-op）；新增单测/Demo 覆盖一次"调 setSubtitleSources" 路径 | ✅ Controller 新增 `withHost<R>(block)`：主线程检查（非主线程抛 IllegalStateException）+ released no-op 返回 null；KDoc 显式禁止在 block 里调 `setVideoAllCallBack`；`host: internal var` 保留不破坏二进制 |
| P0-2 ✅ | `installInternalCallback` 直接 `player.setVideoAllCallBack(...)`，覆盖用户 callback；用户用 `withHost` 调 `setVideoAllCallBack` 时也会反向覆盖内部 callback，事件流断裂 | [GSYPlayerController.kt#L165-L187](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/native_/GSYPlayerController.kt#L165-L187) | 改为"内部 callback 链式分发 + 保留用户 setUserVideoAllCallBack 注入点"；单测：用户 callback 与 events 同时触发时双方都收到 | ✅ 重构为稳定 dispatcher 单例（`internalDispatcher`，22 项全覆盖）+ `@Volatile userCallback` 字段；新增 `setUserVideoAllCallBack(callback)` 公开入口；克隆全屏时 dispatcher 引用本身被 Java 侧 `setVideoAllCallBack` 复制，事件流跨克隆体不断 |
| P0-3 ✅ | Native 模式没有全屏切换 API：`GSYPlayerController` 缺 `enterFullscreen / exitFullscreen`，`GSYPlayerEvent` 缺 `EnterFull / QuitFull` | [GSYPlayerEvent.kt](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/native_/GSYPlayerEvent.kt) [GSYPlayerController.kt](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/native_/GSYPlayerController.kt) | Controller 新增 `enterFullscreen(activity, hideActionBar=true, hideStatusBar=true)` / `exitFullscreen(activity)`；`GSYPlayerEvent` 加 `EnterFull` / `QuitFull`；Demo `FullFeatureNativeActivity` 接入按钮真机回归 ≤ 1.5s 切回 | ✅ Controller 新增 `enterFullscreen(activity, hideActionBar, hideStatusBar)` / `exitFullscreen(activity)` / `isFullscreen`；`GSYPlayerEvent` 加 `EnterFull` / `QuitFull` data object；ΔD2/ΔD8 接入；额外修复 [GSYComposeHostPlayer.java](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/native_/GSYComposeHostPlayer.java) class+构造器 → public（GSY 反射克隆要求 public Constructor，否则 NoSuchMethodException）；emulator 回归：双 host 树确认（原 host + `app:id/full_id` 克隆体），BACK 退出 OK，二轮 round trip 0 crash |

### 1.2 🟡 P1 ｜ 代码次重要残缺

| ID | 问题 | 现状定位 | 验收标准 |
|---|---|---|---|
| P1-1 | Native 模式手势完全禁用且无 Compose 替代品：vol/brightness/seek 三 flag 强制 false，未提供 `Modifier.gsyGestureControl(controller)`；锁屏退化为单纯布尔标志 | [GSYComposeHostPlayer.java#L99-L105](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/native_/GSYComposeHostPlayer.java#L99-L105) | 新文件 `GSYGestureModifier.kt` 提供 `Modifier.gsyGestureControl(controller, enableSeek/Volume/Brightness, onCenterToast)`；锁屏激活时手势屏蔽生效；`FullFeatureNativeActivity` Demo 集成 |
| P1-2 | `rememberGSYPlayerController` 只 release，不自动 pause/resume；后台切换不暂停，与 Wrapper 行为不一致 | [GSYPlayerController.kt#L1-L60](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/native_/GSYPlayerController.kt#L1-L60) [GSYPlayerSurface.kt](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/native_/GSYPlayerSurface.kt) | `rememberGSYPlayerController(autoPauseResume = true)` 默认开；ON_PAUSE → `GSYVideoManager.onPause()`；ON_RESUME → `GSYVideoManager.onResume()`；HOME → 切回真机回归无 crash |
| P1-3 | `GSYPlayerEvent` 仅 3 类，`VideoAllCallBack` 22 项中 19 项未映射：缺 `Buffering(start/end/percent)` / `SeekComplete` / `EnterSmall` / `QuitSmall` / `ClickStart/Resume/Stop/Seekbar/Blank` / `TouchScreenSeek*` 等 | [GSYPlayerEvent.kt](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/native_/GSYPlayerEvent.kt) | sealed class 扩 19 项；`installInternalCallback` 全数 `tryEmit`；保持兼容（旧的 Prepared/AutoComplete/Error 不动） |
| P1-4 | Native Snapshot 只读字段缺失：`videoSar(Num/Den)` / `netSpeed(Long/text)` / `isCacheReady` 全部没暴露 | [GSYPlayerSnapshot.kt](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/native_/GSYPlayerSnapshot.kt) | 扩字段 + `syncFromHost` 同步；Demo 加可视化 |
| P1-5 | builder-only 字段没有 controller 直 setter：headers / cachePath / setSeekOnStart / setLooping / setStartAfterPrepared / setOverrideExtension / setShowPauseCover / setReleaseWhenLossAudio | [GSYPlayerController.kt](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/native_/GSYPlayerController.kt) | Controller 加直 setter，内部委托 host；保留 `setUp(builder)` 的兼容路径 |
| P1-6 | 缓冲粒度差异：500ms 轮询会丢真实 `onBufferingUpdate(percent)` 抖动 | 同 P1-3 / [GSYPlayerController.kt#L34-L42](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/native_/GSYPlayerController.kt#L34-L42) | `events.BufferingProgress(percent)` 实时发；Snapshot 保留低频字段 |

### 1.3 🟢 P2 ｜ 一致性 / 卫生（小问题，但影响首发口碑）

> ✅ **R1 已全部修复**（详见 § 3 进度跟踪表）；下表保留作为历史可追溯的"问题→定位→验收→实际落点"记录。

| ID | 问题 | 现状定位 | 验收标准 | R1 落点 |
|---|---|---|---|---|
| P2-1 | "9 个 Compose Demo" 措辞错误，实际只有 8 个（`DemoSamples.kt` 是 data object，不是 Activity） | [README.md#L156-L158](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/README.md#L156-L158) [README_CN.md#L159-L161](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/README_CN.md#L159-L161) [doc/COMPOSE_USE.md](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/doc/COMPOSE_USE.md) [ComposeDemoListActivity.kt](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/ComposeDemoListActivity.kt) | 三处文档统一改为"8 个示例"，并标明每个示例对应的 Java demo 来源 | ✅ 三处全改为 "8 个可运行 Compose Activity"，并显式说明 `DemoSamples.kt` 是 `data object` |
| P2-2 | 9 个 Compose Activity 全部 `exported="true"`，与老 Activity 默认 `false` 不一致 | [AndroidManifest.xml#L250-L294](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/AndroidManifest.xml#L250-L294) | 改 `exported="false"`；`adb am start -n` 调试需求改写到 README 用 monkey 替代说明 | ✅ 9 个 Activity 全改 `exported="false"`；emulator 验证：内部跳转 ✅ + 外部 `am start` 被严格拒绝（`SecurityException: not exported from uid 10211`） |
| P2-3 | `consumer-rules.pro` 仅 7 行注释空规则；将来如有反射 / `@Composable` 类被业务做反射查找会被 R8 剥离 | [consumer-rules.pro](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-compose/consumer-rules.pro) | 加最小兜底：`-dontwarn com.shuyu.gsyvideoplayer.compose.**` + 注释说明保留底线 | ✅ 加最小集 keep（HostPlayer / Controller / Snapshot / Surface + Event sealed 全部子类 + Wrapper Kt facade + LifecycleBridgeKt），`assembleRelease` R8 通过 |
| P2-4 | `mediaVersion` 1.10.0→1.10.1 升级与 Compose 任务捆绑，未独立 commit | [gradle/dependencies.gradle#L5](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gradle/dependencies.gradle#L5) | 文档备注（影响极小，不强制回滚）；后续依赖升级单独 commit | ✅ 在 [gsyVideoPlayer-compose/build.gradle](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-compose/build.gradle) 顶部加注释块，说明本模块不直接引用 media3、版本统一由 root `gradle/dependencies.gradle` 管理；后续仅需在根 `mediaVersion` 改一处即可 |
| P2-5 | README 未明确"JDK 17"硬要求，新接入者用 JDK 8 会困惑 | [app/build.gradle#L30-L37](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/build.gradle#L30-L37) | README 顶部"环境要求"加一行 JDK ≥ 17 | ✅ 在 README.md / README_CN.md 的 Compose 章节"未发布"提示块下追加 🛠 工具链说明：CI=JDK 21 / 本地 JDK 17（模块 jvmTarget=17，要求 ≥ 17） |
| P2-6 | PAT fallback 仍是有效 token 明文 | [build.gradle#L36-L42](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/build.gradle#L36-L42) | 提交 fallback 改空字符串（同时由仓主在 GitHub 撤旧 token + 新发；文档同步说明） | ✅ 状态核实：[release.yml](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/.github/workflows/release.yml) 已使用 `secrets.GITHUB_TOKEN`（CI 内置），未硬编码 PAT、无 fallback；`build.gradle` 取 token 路径在前轮 P3-5 已改为 properties/env 优先；本项无需再改文件 |

### 1.4 🟢 P3 ｜ Demo 覆盖度（按 P5-1 / P5-2 / P5-3 三档分轮补）

#### P5-1 ｜ 必补的 8 个 demo（覆盖 GSY 招牌差异化能力）

| ID | 待补 Demo | 对位 Java demo | 关键演示能力 |
|---|---|---|---|
| D1 | `DetailFilterComposeActivity` | [DetailFilterActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/DetailFilterActivity.java) | GLSurface 滤镜 / 镜像 / 自定义 Render |
| D2 | `CacheDownloadComposeActivity` | [DetailDownloadPlayer](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/DetailDownloadPlayer.java) + [DetailDownloadExoPlayer](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/DetailDownloadExoPlayer.java) + change_cache/clear_cache 按钮 | 缓存进度 + 清缓存 + Proxy↔EXO 切换 |
| D3 | `AdInListComposeActivity` | [ListADVideoActivity2](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/ListADVideoActivity2.java) + [DetailADPlayer](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/DetailADPlayer.java) | 列表内夹广告 + 前贴片 |
| D4 | `SubtitleComposeActivity` | [SubtitleDetailPlayer](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/SubtitleDetailPlayer.java) + [GSYExoSubTitleDetailPlayer](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/GSYExoSubTitleDetailPlayer.java) | 通用字幕 + EXO 外挂字幕选轨 |
| D5 | `DanmakuComposeActivity` | [DanmkuVideoActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/DanmkuVideoActivity.java) | 弹幕叠加 |
| D6 | `ExoSwitchSourceComposeActivity` | [DetailExoListPlayer](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/DetailExoListPlayer.java) + [ExoAdaptiveTrackActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/ExoAdaptiveTrackActivity.java) | 自定义 EXO MediaSource + HLS/DASH 自适应 |
| D7 | `MultiWindowParallelComposeActivity` | [ListMultiVideoActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/ListMultiVideoActivity.java)（CustomManager 真并行） | 升级现有 MultiWindow 由"互斥激活"为"真·多 Manager 并行" |
| D8 | `SwitchSeamlessComposeActivity` | [SwitchListVideoActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/SwitchListVideoActivity.java) + `SwitchUtil` | 列表 → 详情共享 surface 不重拉流 |

#### P5-2 ｜ 选补的 8 个 demo（现代 App 高频形态）

| ID | 待补 Demo | 对位 Java demo |
|---|---|---|
| D9 | `VerticalShortVideoComposeActivity` ✅ | [ViewPager2Activity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/ViewPager2Activity.java) |
| D10 | `FloatingWindowComposeActivity` ✅ | [WindowActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/WindowActivity.java) |
| D11 | `MoreTypeComposeActivity` ✅ | [DetailMoreTypeActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/DetailMoreTypeActivity.java) |
| D12 | `AudioOnlyComposeActivity` | [AudioDetailPlayer](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/AudioDetailPlayer.java) |
| D13 | `LocalFileComposeActivity` | [InputUrlDetailActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/InputUrlDetailActivity.java) 本地分支 |
| D14 | `WebDetailComposeActivity` ✅ | [WebDetailActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/WebDetailActivity.java) |
| D15 | `MediaCodecComposeActivity` | [RecyclerView3Activity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/RecyclerView3Activity.java) |
| D16 | `CustomControlsThemeComposeActivity` | 参考 [LandLayoutVideo](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/video/LandLayoutVideo.java) / [SampleControlVideo](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/video/SampleControlVideo.java) |

#### P5-3 ｜ 长尾选补（演示价值高、对 Compose 端"出 demo 卖点"有意义）

| ID | 待补 Demo | 对位 Java demo |
|---|---|---|
| D17 | `TransparentVideoComposeActivity` | [DetailTransparentActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/DetailTransparentActivity.java) |
| D18 | `KeepLastFrameComposeActivity` | [KeepLastFrameDemoActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/KeepLastFrameDemoActivity.java) |
| D19 | `SmartPickComposeActivity` | [PlayPickActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/PlayPickActivity.java) |
| D20 | `TVFocusComposeActivity` | [PlayTVActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/PlayTVActivity.java) |
| D21 | `SharedElementTransitionComposeActivity` | [PlayActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/PlayActivity.java) |
| D22 | `FragmentInteropComposeActivity` | [FragmentVideoActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/FragmentVideoActivity.java) |

#### 现有 8 个 Compose demo "演示深度浅" 的回归升级（P5-Δ）

| ID | 已有 Demo | 浅在哪里 | 升级目标 |
|---|---|---|---|
| ΔD1 | [BasicWrapperActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/BasicWrapperActivity.kt) | builder 仅设了 5 项，缺 ~30+ 项配置示范 | 加 `setVideoAllCallBack` + `setSeekRatio` + `setShowPauseCover` 等 |
| ΔD2 ✅ | [DetailNativeActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/DetailNativeActivity.kt) | 全屏只是 `requestedOrientation`，没用底层全屏管线 | ✅ R2 已切换为 `controller.enterFullscreen / exitFullscreen` + `events.EnterFull/QuitFull` 同步本地标志位；BackHandler(enabled=fullscreen) 拦 BACK |
| ΔD3 | [FullFeatureNativeActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/FullFeatureNativeActivity.kt) | 缺音量/亮度/进度手势条 + 中央 toast | P1-1 完成后接入 `Modifier.gsyGestureControl` |
| ΔD4 | [ListPlayNativeActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/ListPlayNativeActivity.kt) | 仅"滚出屏暂停"，没有"释放占位/封面恢复" | 演示 `setThumbImageView` + 离屏 release |
| ΔD5 | [SwitchUrlActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/SwitchUrlActivity.kt) | 名为"切换 URL"，实为 setUp 重置；没演示真 `SwitchUtil` | D8 出来后此 demo 文档加"see also"指向 |
| ΔD6 | [MultiWindowActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/MultiWindowActivity.kt) | 单例 GSYVideoManager 互斥，没用 CustomManager | D7 出来后此 demo 改名为"互斥版"，新增"并行版"对照 |
| ΔD7 | [AutoPlayListActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/AutoPlayListActivity.kt) | 用 `events.AutoComplete` setUp 下一段，丢 surface 接管 | 先以注释说明取舍，等 P0-1 后可演示更深路径 |
| ΔD8 ✅ | [ListWithFullscreenActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/ListWithFullscreenActivity.kt) | 用 `BackHandler+Insets` 模拟，没用底层全屏管线 | ✅ R2 已删除 `WindowInsetsControllerCompat` / 手动 `requestedOrientation` / `FullscreenLayer` 自绘，改为 `controller.enterFullscreen` + `events.EnterFull/QuitFull`，全屏渲染由内核克隆体接管 |

---

## 2. 推进 Plan（分轮，每轮一个 PR / 一组 commit）

> 推荐顺序：**P3 轻量** → **P4-1 P0** → **P4-2 P1** → **P5-1 必补 demo** → **P5-2 选补 demo** → **P5-Δ 老 demo 升级 + 文档收口** → 评估首发。
>
> 每轮收尾必跑：
> 1. `./gradlew :gsyVideoPlayer-compose:assembleRelease`
> 2. `./gradlew :app:assembleDebug`
> 3. `./gradlew :gsyVideoPlayer-compose:publishToMavenLocal`（默认 + `-PPUBLISH_TARGET=mavenCentral`）
> 4. emulator-5554 真机回归（受影响 demo + crash buffer 空验证）
> 5. doc/COMPOSE_USE.md + README.md/README_CN.md 同步
> 6. **不发 tag**，commit + push master

### 轮次 R1 — P3 轻量修复（预计 0.5 天） ✅ 已完成

- [x] P2-1 文档"9 → 8 demo" 三处统一（README.md / README_CN.md / doc/COMPOSE_USE.md）
- [x] P2-2 9 个 compose Activity Manifest `exported="false"`
- [x] P2-3 `consumer-rules.pro` 加最小兜底
- [x] P2-4 `mediaVersion` 备注（在 compose 模块 build.gradle 顶部加注释块）
- [x] P2-5 README/README_CN 在 Compose 章节标注 JDK 17 / CI JDK 21 工具链差异
- [x] P2-6 PAT fallback 状态核实：release.yml 已用 secrets.GITHUB_TOKEN，无需改文件
- [x] 真机回归：emulator-5554 装机成功；Monkey 200 事件 0 crash；same-uid 内部多层跳转 ✅；shell 跨 uid `am start` 被严格拒绝（`SecurityException: not exported from uid 10211`）
- [x] 构建回归：`./gradlew :app:assembleDebug :gsyVideoPlayer-compose:assembleRelease :gsyVideoPlayer-compose:publishToMavenLocal -x lint` —— **BUILD SUCCESSFUL in 17s**
- [x] commit `compose: R1 housekeeping (P2-1~6 docs/manifest/consumer-rules)` 并 push（不发 tag）

### 轮次 R2 — P4-1 代码 P0（预计 1.5 天） ✅ 已完成

- [x] P0-1 `controller.withHost { player -> ... }` 公开闭包；保留 `host: internal` 不破坏二进制
- [x] P0-2 `installInternalCallback` 重构为内部分发器：`internalCallback` + `userCallback` 链式
- [x] P0-3 Controller `enterFullscreen / exitFullscreen` + `GSYPlayerEvent.EnterFull / QuitFull`
- [x] ΔD2 / ΔD8：[DetailNativeActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/DetailNativeActivity.kt) + [ListWithFullscreenActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/ListWithFullscreenActivity.kt) 切换到新全屏 API 并真机回归
- [x] doc/COMPOSE_USE.md 加"逃生口 withHost { ... } 用法"章节
- [x] 额外修复：[GSYComposeHostPlayer](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/native_/GSYComposeHostPlayer.java) class+构造器 → public（GSY 反射克隆 `getConstructor(Class[])` 仅返回 public 构造器；非 public 时 emulator 报 NoSuchMethodException 全屏失败）
- [x] 真机回归：emulator-5554 装机；Monkey 200 事件 0 crash；ΔD2 全屏进/退（双 host 树确认 `app:id/full_id` 克隆体出现/移除）；ΔD2 二轮 round trip dispatcher 复用稳定；ΔD8 全屏进/退（首次 setUp + 等 host attach 后的第二次点击进入全屏，符合 Compose 异步重组语义）；6 个其他 demo same-uid 启动；cross-uid `am start` 仍被严格拒绝；AndroidRuntime FATAL 与 crash buffer 全空
- [x] 构建回归：`./gradlew :app:assembleDebug :gsyVideoPlayer-compose:assembleRelease :gsyVideoPlayer-compose:publishToMavenLocal -x lint` —— **BUILD SUCCESSFUL in 29s**
- [x] commit `compose: R2 P0 fixes (withHost + callback fanout + fullscreen)` 并 push（不发 tag）

### 轮次 R3 — P4-2 代码 P1（预计 1.5 天）

- [x] P1-1 `Modifier.gsyGestureControl(controller, ...)` + 锁屏联动 ✅ 新文件 [GSYGestureModifier.kt](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/native_/GSYGestureModifier.kt)：横向 drag = seek（onDragEnd commit），左半屏纵向 = 亮度，右半屏纵向 = 音量；snapshot.isLocked 时所有手势短路；ΔD3 接入并加中央 toast
- [x] P1-2 `rememberGSYPlayerController(autoPauseResume = true)` 默认开 ✅ ON_PAUSE → `GSYVideoManager.onPause()`，ON_RESUME → `GSYVideoManager.onResume()`
- [x] P1-3 `GSYPlayerEvent` 扩 19 项 + `installInternalCallback` 全数 emit ✅ sealed class 5→24 项；dispatcher 22 项 callback 各加 `_events.tryEmit`
- [x] P1-4 Snapshot 扩 SAR / netSpeed / isCacheReady ✅ 5 字段（videoSarNum/Den / netSpeed/Long/Text / isCacheReady） + syncFromHost 同步 + runCatching 兜底
- [x] P1-5 Controller 直 setter ✅ 8 个 setter（headers / cachePath / setSeekOnStart / setLooping / setStartAfterPrepared / setOverrideExtension / setShowPauseCover / setReleaseWhenLossAudio）+ 缓存 reapply 机制（attach 新 host / 全屏克隆体后自动 reapply）+ 主线程门
- [x] P1-6 `events.BufferingProgress(percent)` + `SeekComplete` 实时 ✅ [GSYComposeHostPlayer.java](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/native_/GSYComposeHostPlayer.java) 加 `BufferingHook` / `SeekCompleteHook`；Controller 在 attachHost / setUp 安装、detachHost / release 清理
- [x] ΔD3 [FullFeatureNativeActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/FullFeatureNativeActivity.kt) 接入 gestureModifier ✅ 三向手势 + 中央 toast
- [x] R3 真机回归 ✅ ΔD3 进入 + 横/纵 swipe + Activity 存活；ΔD2 全屏 round trip；Monkey 50 events `--pct-touch 60 --pct-motion 30` 0 FATAL；状态显示 `Playing | 01:02 / 1:32:27`

### 轮次 R4 — P5-1 必补 demo（每个独立 commit / 8 commits 或一次合并）

- [x] D1 DetailFilterCompose ✅ [DetailFilterComposeActivity.kt](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/DetailFilterComposeActivity.kt) — `controller.withHost { player.setEffectFilter(...) }` 注入 6 种 GLSL 滤镜（NoEffect / Gamma / 黑白 / 反色 / Sepia / 高斯模糊），`GSYVideoType.setRenderType(GLSURFACE)` 在 onCreate 设置、onDestroy 还原
- [x] D2 CacheDownloadCompose ✅ [CacheDownloadComposeActivity.kt](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/CacheDownloadComposeActivity.kt) — `ProxyCacheManager.instance().newProxy(ctx).getProxyUrl(url)` 套缓存代理 + `setCacheWithPlay(true)`；展示 `snapshot.isCacheReady` / `bufferPercent` / `netSpeedText`；清缓存按钮调 `GSYVideoManager.instance().clearAllDefaultCache(ctx)`
- [x] D3 AdInListCompose ✅ [AdInListComposeActivity.kt](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/AdInListComposeActivity.kt) — 单 controller 通过 `events.AutoComplete` 边沿事件链 setUp 切正片，简化 Java 双 player 模式；emulator 实证 `阶段：广告播放中 → 正片播放中`
- [x] D4 SubtitleCompose ✅ [SubtitleComposeActivity.kt](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/SubtitleComposeActivity.kt) — `PlayerFactory.setPlayManager(IjkPlayerManager.class)` 切回 IJK；3 字幕源（SRT 本地 / VTT 本地 / SRT 网络）；`controller.withHost` 注入 `setSubtitleSources / setSubtitleStyle / setSubtitleEnabled / selectSubtitle`；字号 16↔22 sp 切换
- [x] D5 DanmakuCompose ✅ [DanmakuComposeActivity.kt](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/DanmakuComposeActivity.kt) — Compose Canvas + `rememberTextMeasurer` 自绘 12 条 3 轨弹幕，与 `controller.snapshot.currentPosition` 联动；不依赖 master.flame.danmaku；emulator 实证 `Playing 48s/5547s` 真起播
- [x] D6 ExoSwitchSourceCompose ✅ [ExoSwitchSourceComposeActivity.kt](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/ExoSwitchSourceComposeActivity.kt) — `PlayerFactory.setPlayManager(Exo2PlayerManager.class)` 切 EXO 内核；4 源（MP4/HLS/GSY 默认/IMG_0382）+ 5 档倍速（0.75/1.0/1.25/1.5/2.0）；emulator logcat `ExoPlayerImpl: Init [AndroidXMedia3/1.10.1]` 实证；切 HLS 后新 instance Init + onPrepared
- [x] D7 MultiWindowParallelCompose（CustomManager）✅ [MultiWindowParallelComposeActivity.kt](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/MultiWindowParallelComposeActivity.kt) — Wrapper 模式 × 3 包 [MultiSampleVideo](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/video/MultiSampleVideo.java)，每个 view 独立 `setPlayTag + setPlayPosition` → 路由到独立 `CustomManager.getCustomManager(getKey())`；区别于现有 P1 [MultiWindowActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/MultiWindowActivity.kt)（互斥版）；`onDispose` 释放 3 个 manager
- [x] D8 SwitchSeamlessCompose（共享 surface）✅ [SwitchSeamlessComposeActivity.kt](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/SwitchSeamlessComposeActivity.kt) — Compose 端 seamless 精髓：同一 controller 跨 list 缩略区与 detail 大屏区复用（`GSYPlayerSurface` attach/detach 切换位置），不重 setUp、不释放、进度连续
- [x] [ComposeDemoListActivity.kt](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/ComposeDemoListActivity.kt) 8 → 12 → 16 项；8 个新 Activity 在 [AndroidManifest.xml](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/AndroidManifest.xml) 注册 `exported="false"`
- [x] R4 真机回归 ✅ emulator-5554 装机；上半场 4 个 demo am start + BACK + Monkey 50 events 0 FATAL；下半场 4 个 demo 通过 UI Automator 真点击进入：D6 logcat `ExoPlayerImpl: Init` + `onPrepared` + 切 HLS Release/Init / D5 长视频 Playing 48s/5547s / D3 阶段切换 ad→feature 实证 / D7 onPrepared + CURRENT_STATE_PLAYING；Monkey 100 events 0 FATAL；logcat AndroidRuntime FATAL/crash buffer 全空

### 轮次 R5 — P5-2 选补 demo（视进度执行）

- [x] D9 VerticalShortVideoCompose ✅ [VerticalShortVideoComposeActivity.kt](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/VerticalShortVideoComposeActivity.kt) — Compose Foundation `VerticalPager + rememberPagerState`，单 controller 通过 `snapshotFlow { pagerState.currentPage }.distinctUntilChanged().collect` 跨页 setUp，仅在当前页 attach `GSYPlayerSurface`；setLooping(true) + setCacheWithPlay(true)；emulator 实证 page 1 `Playing 1/5` + page 2 切页后 logcat `GSYVideoPlayer: onPrepared` 链路通
- [x] D10 FloatingWindowCompose ✅ [FloatingWindowComposeActivity.kt](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/FloatingWindowComposeActivity.kt) — `rememberLauncherForActivityResult(StartActivityForResult())` 申请 `SYSTEM_ALERT_WINDOW` + `Util.hasPermission` 检测；复用 [FloatPlayerView](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/view/FloatPlayerView.java)（Wrapper 路线）证明 Compose Activity 同样能驱动全局画中画；onDestroy → `FloatWindow.destroy()`；emulator 实证 `权限状态：✅ 已授予 / 悬浮窗状态：▶ 已显示`
- [x] D11 MoreTypeCompose ✅ [MoreTypeComposeActivity.kt](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/MoreTypeComposeActivity.kt) — `enum CellType { Normal, Ad, Cover, Unknown }` 4 类 cell；6 条 entry LazyColumn；单 controller 仅 attach activeIndex；emulator 实证点 Normal cell 后 `当前激活：#1 样片 #1 · 普通视频 | Playing 1115/5547619 ms` + logcat `videoWidth: 480 videoHeight: 384`
- [x] D14 WebDetailCompose ✅ [WebDetailComposeActivity.kt](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/WebDetailComposeActivity.kt) — Column 上方 16:9 `GSYPlayerSurface + GSYDefaultControls`（Compose 区），下方 `AndroidView { WebView }` `Modifier.weight(1f)` 弹性占据剩余空间；emulator 实证 `当前：Playing | 1904/10027 ms` + logcat `libwebviewchromium_plat_support.so` 加载（WebView 渲染）+ `CURRENT_STATE_PLAYING`
- [x] [ComposeDemoListActivity.kt](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/ComposeDemoListActivity.kt) 16 → 20 项；4 个新 Activity 在 [AndroidManifest.xml](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/AndroidManifest.xml) 注册 `exported="false"` + configChanges 全集
- [x] R5 上半场真机回归 ✅ emulator-5554 装机；4 个新 demo 通过 UI Automator 真点击进入 + 起播实证；Monkey 100 events `--pct-touch 60 --pct-motion 30` 0 FATAL；logcat AndroidRuntime FATAL/crash buffer 全空
- [ ] D12 / D13 / D15 / D16 待 R5 下半场或后续轮次推进

### 轮次 R6 — P5-Δ 老 demo 升级 + 文档收口 + 首发评估

- [ ] ΔD1 / ΔD4 / ΔD5 / ΔD6 / ΔD7 升级
- [ ] doc/COMPOSE_USE.md 加"能力矩阵 + 何时选 Wrapper / 何时选 Native"对照表（即本文件 § 1）
- [ ] doc/COMPOSE_USE.md 加"从 Java 迁移 Compose 的 cookbook"
- [ ] 评估首发：若 P0/P1 全过，至少 P5-1 8 个 demo 真机回归通过、crash buffer 空，可考虑去掉"未发布"标识、打 v13.x.0 tag

### 轮次 R7（可选）— P5-3 长尾 demo + AliPlayer / PiP / HTTP DNS Compose 端"新增能力示范"

- [ ] D17 ~ D22
- [ ] PiP（Compose 端首发示范，Java 端也没有，作为差异化卖点）

---

## 3. 进度跟踪

| 轮次 | 状态 | 起止 commit | 备注 |
|---|---|---|---|
| R1 | ✅ 已完成 | `24360bff` (归档 plan 落盘) → `276420b7` (R1 修复) | P2 六项全过；构建 + 模拟器双回归通过；不发 tag |
| R2 | ✅ 已完成 | `276420b7` (R1 修复) → `6f5f846c` (R2 修复) | P0-1/2/3 三项全过；额外修复 GSYComposeHostPlayer public class+构造器（反射克隆门票）；构建 + 模拟器双回归通过；不发 tag |
| R3 | ✅ 已完成 | `6f5f846c` (R2) → `fe66e7fd` (R3 P1 reactive parity) | P1 五项全过；events SharedFlow + stateFlow + setUserVideoAllCallBack + 8 直 setter + BufferingProgress/SeekComplete + ΔD3 手势；emulator Monkey 50 events 0 FATAL |
| R4 | ✅ 已完成（8/8 demo） | `fe66e7fd` (R3) → `47ce1877` (R4 上半场 D1/D2/D4/D8) → 本轮 (R4 续 D3/D5/D6/D7) | P5-1 全部 8 项已落（滤镜 / 缓存下载 / 前贴片广告 / 字幕 / 自绘弹幕 / EXO 多源 / Wrapper 真并行 / Seamless 切换）；ComposeDemoListActivity 8 → 16 项；emulator 真 UI 点击实证：D6 EXO 内核切换 logcat 实证 + D5 长视频 Playing 48s/5547s + D3 ad→feature 阶段切换 + D7 真起播 0 crash；Monkey 100 events 0 FATAL |
| R5 | ◐ 半完成（4/8） | `47ce1877` (R4 上半场) → `d05d0a8c` (R4 续 D3/D5/D6/D7) → 本轮 (R5 上半场 D9/D10/D11/D14) | P5-2 高优 4 项已落（VerticalPager 短视频 / 悬浮窗 + 系统权限申请 / 多类型 cell 列表 / WebView 图文混排）；ComposeDemoListActivity 16 → 20 项；emulator 真 UI 点击实证：D9 page1 `Playing 1/5` + 切页 onPrepared / D10 `悬浮窗状态：▶ 已显示` / D11 `Playing 1115/5547619 ms` + videoWidth 480x384 / D14 `Playing 1904/10027 ms` + libwebviewchromium 加载；Monkey 100 events 0 FATAL；D12/D13/D15/D16 待续轮 |
| R6 | ☐ pending | — | 首发评估前置 |
| R7 | ☐ pending | — | 选做 |

> 每轮完成后，将本表格状态 ☐ 改为 ✅ 并附 commit hash；同时把 § 1 / § 2 中已完成项的 `[ ]` 改为 `[x]`。

---

## 4. 备忘 / 不收口的决策

- **Compose 模块在 P5-1 完成前，仍维持"未发布"状态**：不打 tag，不在 Maven Central 上放 13.0.0。
- **PR 粒度建议**：每轮一个独立 PR（或一组合并 commit），便于回滚；commit message 用 `compose: R<N> ...` 前缀串起跟踪。
- **真机回归不可省**：每轮至少跑受影响 demo + 1 个非 Compose Activity（保证不破坏老功能），并核对 crash buffer 与 ANR。
- **Code Reference 维护**：当 Native 模式新增公开 API（`withHost` / `enterFullscreen` / `events` 扩项 / 直 setter）时，每条都应在 doc/COMPOSE_USE.md 给出可点击的 file 链接，便于贡献者跟读。
