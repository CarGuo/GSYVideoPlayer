# GSYVideoPlayer Compose 支持

> ⚠ **当前状态：未发布（Unreleased）**
>
> `gsyVideoPlayer-compose` 模块已合入 master，但 **尚未对外发布到 Maven Central / GitHub Packages / JitPack**。下文中出现的 `13.0.0` 仅为预留坐标 —— 仅当你**自己** `./gradlew :gsyVideoPlayer-compose:publishToMavenLocal` 之后才能在你的项目里以坐标形式引用，否则只能走 `implementation project(":gsyVideoPlayer-compose")` 的源码依赖方式。
>
> 第一次正式发布的时间点请等待官方 release tag 公告。
>
> 当前能力缺口、已知问题与分轮推进路线图已归档到 [doc/COMPOSE_BACKLOG.md](./COMPOSE_BACKLOG.md)；后续每一轮代码与 demo 推进都会同步更新该文件。

新增模块：`gsyVideoPlayer-compose`，提供两种使用方式：

| 模式 | 适用场景 | 使用入口 |
|---|---|---|
| **Wrapper（AndroidView 包装）** | 已用 GSY，想快速塞进 Compose 屏；保留全屏、手势、缓存、字幕、滤镜等全部能力 | `GSYVideoPlayerView { ... }` |
| **Native（Compose 原生控件层）** | 想完全用 Compose 重绘控制 UI，但仍复用 GSY 多内核与渲染管线 | `GSYComposePlayer + GSYPlayerController` |

模块基于 [gsyVideoPlayer-java](../gsyVideoPlayer-java)，**不修改任何旧代码**。

---

## 一、引入依赖

```groovy
// 方式 A（当前唯一可用）：使用项目模块（源码依赖）
implementation project(":gsyVideoPlayer-compose")

// 方式 B（预留坐标，模块尚未对外发布）：Maven Central / GitHub Packages
// 仅当你自己 ./gradlew :gsyVideoPlayer-compose:publishToMavenLocal 后才能在本机解析
implementation "io.github.carguo:gsyvideoplayer-compose:13.0.0"
```

模块本身已 `api` 依赖 `gsyVideoPlayer-java`，无需重复引入；但如果你需要 EXO/Ali 内核，还需按照原有方式额外引入对应坐标。

> **要求**：`minSdk ≥ 23`、Kotlin 2.0.x、AGP 8.6+、JDK 17。模块内部已通过 `compose-bom 2024.06.00` 统一 Compose 依赖版本，并使用 Compose Compiler Gradle Plugin（无需再单独指定 `composeCompilerVersion`）。

---

## 二、模式一：AndroidView 包装

最快的接入方式，完全等价于在 XML 里放一个 `StandardGSYVideoPlayer`。

```kotlin
@Composable
fun MyVideoScreen(url: String) {
    GSYVideoPlayerView(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f),
        setUp = { player ->
            GSYVideoOptionBuilder()
                .setUrl(url)
                .setCacheWithPlay(true)
                .setVideoTitle("Compose Demo")
                .setIsTouchWiget(true)
                .build(player)
            player.startPlayLogic()
        },
        // 默认会自动桥接 onPause/onResume 与离屏 release
        autoPauseResume = true,
        autoReleaseOnDispose = true,
    )
}
```

特性：
- **一行 Composable 拿到 GSY 全部内置 UI**（全屏切换、手势、字幕、弹幕、滤镜……）
- 自动桥接 `Lifecycle.Event.ON_PAUSE → GSYVideoManager.onPause()`、`ON_RESUME → onResume()`
- 离开 Composition 时自动 `release()`（可关闭）

如果需要使用其它 `NormalGSYVideoPlayer` 子类（例如 `ListGSYVideoPlayer`、`SampleControlVideo`），用通用版本：

```kotlin
GSYAnyVideoPlayerView(
    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
    factory = { ctx -> SampleControlVideo(ctx) },
    setUp = { player ->
        GSYVideoOptionBuilder().setUrl(url).build(player)
        player.startPlayLogic()
    },
)
```

#### `setUpKey`：参数变化时如何让 `setUp` 重跑

`setUp` 默认**只在 factory 阶段调用一次**。这是有意设计：每次 `recomposition` 都执行
`builder.build(player)` 会重置 url / 重启播放，反而是 bug 而非 feature。

如果你想让 url 等参数变化时**主动**重新执行 `setUp`，传 `setUpKey`：

```kotlin
GSYVideoPlayerView(
    setUp = { player -> GSYVideoOptionBuilder().setUrl(url).build(player); player.startPlayLogic() },
    setUpKey = url,   // url 变化时再次调用 setUp，否则 update 阶段是 no-op
)
```

`setUpKey == null`（默认）：兼容老用法，`AndroidView.update` 不做任何事。

---

## 三、模式二：Compose 原生控件层

UI 完全由 Compose 自绘，画面依然由 `TextureView`（在 `AndroidView` 中）承载，内核走 `IPlayerManager`。

### 1. 一行写法（带默认控制条）

```kotlin
@Composable
fun MyNativeScreen(url: String) {
    val controller = rememberGSYPlayerController(url = url, title = "Native Demo")

    GSYComposePlayer(
        controller = controller,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f),
        showDefaultControls = true,
    )
}
```

`rememberGSYPlayerController` 会在离开 Composition 时自动 `release()`。

### 2. 完全自定义 UI

```kotlin
val controller = rememberGSYPlayerController(url = url)
val snap by controller.snapshot

Box(modifier = Modifier.fillMaxSize()) {
    GSYPlayerSurface(controller, Modifier.matchParentSize())   // 画面
    MyOwnControls(snap, controller, Modifier.matchParentSize())// 自绘控制层
}
```

### 3. 控制器 API

`GSYPlayerController`：

```kotlin
controller.setUp(builder: GSYVideoOptionBuilder)
controller.setUp(url, cacheWithPlay = false, title = "")

controller.play()
controller.togglePlayPause()
controller.pause()
controller.resume()
controller.seekTo(positionMs: Long)
controller.setSpeed(1.5f, soundTouch = true)
controller.release()

// 全屏（R2 起新增，由 GSY 内核接管，等价于 Java 版 startWindowFullscreen / backFromFull）
controller.enterFullscreen(activity, hideActionBar = true, hideStatusBar = true)
controller.exitFullscreen(activity)              // 返回 Boolean，false 表示当前并不在全屏态
val inFs: Boolean = controller.isFullscreen      // 直读底层标志位

// 用户级 VideoAllCallBack（与内部 dispatcher 链式分发，不再覆盖 events）
controller.setUserVideoAllCallBack(object : GSYSampleCallBack() {
    override fun onClickStartIcon(url: String?, vararg objects: Any?) { /* 用户埋点 */ }
})

// 逃生口：直接拿底层 StandardGSYVideoPlayer 调用尚未封装的方法（必须主线程；release 后 no-op）
controller.withHost { player ->
    player.setSubTitle("https://example.com/sub.srt")        // 字幕
    player.setMirrorRotation(true)                            // 镜像
    player.taskShotPic { bmp -> /* 截图回调 */ }              // 快照
}

val snap: State<GSYPlayerSnapshot> = controller.snapshot
// snap.value -> GSYPlayerSnapshot(state, currentPosition, duration,
//                                 bufferPercent, isPlaying, videoWidth, videoHeight)
```

`GSYPlayState` 状态机与原生 `GSYVideoView.CURRENT_STATE_*` 一一对应：

```
Idle  Preparing  Playing  Buffering  Paused  Completed  Error
```

> ⚠️ **关于 `withHost { ... }`**：这是为了在能力对齐补齐之前给业务一个**逃生口**（Escape Hatch），
> 不是推荐路径。**禁止**在 block 里调 `player.setVideoAllCallBack(...)`——会把内部
> dispatcher 顶掉，导致 `events` / `setOnXxx` / `setUserVideoAllCallBack` 全部失效。
> 需要回调请改用 [`setUserVideoAllCallBack`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/native_/GSYPlayerController.kt) 入口。

### 4. 响应式订阅（推荐：events / stateFlow）

> 自 v13.x 起，`GSYPlayerController` 提供与 Coroutine 完全对齐的响应式接口。
> 旧版 `setOnError / setOnComplete / setOnPrepared` 仍保留但已标记 `@Deprecated`，
> 推荐切换到 `events`（边沿事件）+ `stateFlow`（连续状态）。

```kotlin
import com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerEvent

val controller = rememberGSYPlayerController(url = url, autoPlay = true)

// 1) 一次性"边沿事件"流：onPrepared / onAutoComplete / onPlayError / onEnterFullscreen / onQuitFullscreen
LaunchedEffect(controller) {
    controller.events.collect { event ->
        when (event) {
            is GSYPlayerEvent.Prepared      -> Log.d("Demo", "已准备就绪")
            is GSYPlayerEvent.AutoComplete  -> Log.d("Demo", "播放完成")
            is GSYPlayerEvent.Error         -> Log.e("Demo", "播错 what=${event.what} extra=${event.extra}")
            GSYPlayerEvent.EnterFull        -> fullscreen = true   // 由内核接管全屏后回调
            GSYPlayerEvent.QuitFull         -> fullscreen = false
        }
    }
}

// 2) 状态读取——StateFlow 形态，便于在 ViewModel / UseCase 里 collect
LaunchedEffect(controller) {
    controller.stateFlow
        .map { it.isPlaying }
        .distinctUntilChanged()
        .collect { isPlaying -> /* ... */ }
}

// 3) Compose 直接渲染——仍可用 controller.snapshot
val snap: GSYPlayerSnapshot by controller.snapshot
```

设计取舍：
- **状态用 StateFlow / State**：当前状态值唯一、订阅者随时拿最新值，适合渲染。
- **事件用 SharedFlow**：onPrepared / onAutoComplete / onPlayError 是一次性瞬时事件，
  错过即错过；用状态字段做"事件"会逼业务方手写"事件去重"，不优雅。
- `events` 默认 `replay = 0`、`extraBufferCapacity = 16`、溢出 `DROP_OLDEST`，
  emit 不会阻塞、订阅前发生的事件不会重放。

---

## 四、生命周期与全屏

### Wrapper 模式

由 `GSYVideoPlayerView` 自动处理；如需手动控制，传 `autoPauseResume = false` 并自己用 `LifecycleEffect`。

全屏可直接调用底层 `player.startWindowFullscreen(...)`，与传统用法一致。

### Native 模式

`rememberGSYPlayerController` 会在离开 Composition 时自动 `release`。

**全屏（推荐路径，自 R2 起）**——由 GSY 内核接管，与 Java 版 `startWindowFullscreen` 走同一管线（反射克隆 host 接管渲染、自动旋转、自动隐藏系统栏）：

```kotlin
val controller = rememberGSYPlayerController(url = url, autoPlay = true)
val activity = LocalContext.current as Activity
var fullscreen by remember { mutableStateOf(false) }

LaunchedEffect(controller) {
    controller.events.collect { ev ->
        when (ev) {
            GSYPlayerEvent.EnterFull -> fullscreen = true
            GSYPlayerEvent.QuitFull  -> fullscreen = false
            else -> {}
        }
    }
}

BackHandler(enabled = fullscreen) { controller.exitFullscreen(activity) }

Button(onClick = { controller.enterFullscreen(activity) }) { Text("全屏") }
```

> 不再推荐 `Dialog(...) + 手动 requestedOrientation` 自绘全屏——两个 demo
> [DetailNativeActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/DetailNativeActivity.kt)
> 与 [ListWithFullscreenActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/ListWithFullscreenActivity.kt)
> 已切换到上述路径，可作为参考。

---

## 五、内核切换

与原版完全一致，不受 Compose 模块影响：

```kotlin
PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)
CacheFactory.setCacheManager(ExoPlayerCacheManager::class.java)
```

---

## 六、Demo

App 模块下入口 `Compose Demo`（[ComposeDemoListActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/ComposeDemoListActivity.kt)）汇总了 **24 个可运行的 Compose Activity**，外加 1 份共享测试数据：

> 表中第 25 行的 `DemoSamples.kt` 是 `data object`（与 Java/XML Demo 共用同一组测试 URL），并非可运行 Activity，仅为方便溯源附在表尾。

### 6.1 P0 / P1 — 基础与对齐 Java 老 demo（8 项）

| # | 名称 | 入口 | 说明 |
|---|---|---|---|
| 1 | Basic Wrapper（ΔD1 已升级） | [BasicWrapperActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/BasicWrapperActivity.kt) | `GSYVideoPlayerView` 最小用法 + 5 个高频 builder 选项（`setSeekRatio` / `setShowPauseCover` / `setReleaseWhenLossAudio` / `setStartAfterPrepared` / `setVideoAllCallBack`）实时演示 |
| 2 | Switch URL（ΔD5 已升级） | [SwitchUrlActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/SwitchUrlActivity.kt) | Wrapper 模式下切流；KDoc see-also → D8 无缝切换 |
| 3 | Multi-Window（ΔD6 已升级） | [MultiWindowActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/MultiWindowActivity.kt) | 多个播放器同屏共存（互斥版）；指引 → D7 真并行版 |
| 4 | List Play (Native)（ΔD4 已升级） | [ListPlayNativeActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/ListPlayNativeActivity.kt) | LazyColumn + Native Composable；离屏 `setUp` 重置 vs `pause` Switch + `setShowPauseCover` Switch + Compose 自绘占位封面 |
| 5 | Auto-Play List（ΔD7 已升级） | [AutoPlayListActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/AutoPlayListActivity.kt) | 滚动可视区自动播放/释放；KDoc 解释 surface 接管取舍 |
| 6 | List with Fullscreen | [ListWithFullscreenActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/ListWithFullscreenActivity.kt) | 列表内层级式全屏 |
| 7 | Detail Native | [DetailNativeActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/DetailNativeActivity.kt) | Native 模式详情页 |
| 8 | Full-Feature Native | [FullFeatureNativeActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/FullFeatureNativeActivity.kt) | 速率 / Seek / 错误 / 完成态 |

### 6.2 P5-1 — GSY 招牌差异化能力（8 项）

| # | 名称 | 入口 | 说明 |
|---|---|---|---|
| 9 | Native 滤镜 | [DetailFilterComposeActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/DetailFilterComposeActivity.kt) | 通过 `withHost` 注入 `setEffectFilter`，循环切换 6 种 GLSL 滤镜 |
| 10 | Native 缓存 / 下载 | [CacheDownloadComposeActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/CacheDownloadComposeActivity.kt) | `ProxyCacheManager` 代理 + `isCacheReady` 状态 + 清缓存 |
| 11 | Native 字幕 | [SubtitleComposeActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/SubtitleComposeActivity.kt) | 3 字幕源切换 + 字号 / 开关，IJK 内核 |
| 12 | Native Seamless 切换（D8） | [SwitchSeamlessComposeActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/SwitchSeamlessComposeActivity.kt) | 同一 controller 跨 list/detail 复用，不重 `setUp` 不重拉流 |
| 13 | Native 前贴片广告 | [AdInListComposeActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/AdInListComposeActivity.kt) | 单 controller AD → AutoComplete → 切正片，演示 events 边沿事件链 |
| 14 | Native Compose 自绘弹幕 | [DanmakuComposeActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/DanmakuComposeActivity.kt) | Canvas + textMeasurer 与 `snapshot.currentPosition` 同步 |
| 15 | Native EXO 多源切换 | [ExoSwitchSourceComposeActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/ExoSwitchSourceComposeActivity.kt) | `PlayerFactory.setPlayManager(Exo2PlayerManager)` + MP4/HLS + 5 档倍速 |
| 16 | Wrapper 真并行多窗口 | [MultiWindowParallelComposeActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/MultiWindowParallelComposeActivity.kt) | `MultiSampleVideo` + `CustomManager`，3 个并行播放（非互斥） |

### 6.3 P5-2 — 现代 App 高频形态（8 项）

| # | 名称 | 入口 | 说明 |
|---|---|---|---|
| 17 | 竖屏短视频 (VerticalPager) | [VerticalShortVideoComposeActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/VerticalShortVideoComposeActivity.kt) | `VerticalPager` + 单 controller 跨页 `setUp`，循环播放 |
| 18 | 悬浮窗（画中画） | [FloatingWindowComposeActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/FloatingWindowComposeActivity.kt) | `SYSTEM_ALERT_WINDOW` 拉起 `FloatPlayerView`，跨 Activity 常驻 |
| 19 | 多类型列表 | [MoreTypeComposeActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/MoreTypeComposeActivity.kt) | LazyColumn 多 type cell（Normal/Ad/Cover/Unknown） |
| 20 | 图文混排（视频 + WebView） | [WebDetailComposeActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/WebDetailComposeActivity.kt) | 上方 Compose 播放器 + 下方 `AndroidView` WebView 双栈共存 |
| 21 | 纯音频播放 | [AudioOnlyComposeActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/AudioOnlyComposeActivity.kt) | raw 资源 + `enableRawPlay`；Compose 端 controller 仍要挂一个 `GSYPlayerSurface`（host 必须有 Surface 载体，1dp 隐身节点即可） |
| 22 | 自定义 URL / 本地文件 | [LocalFileComposeActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/LocalFileComposeActivity.kt) | URL 输入 + cache 切换 + `raw://` / `http://` 多种源类型 |
| 23 | MediaCodec 硬解切换 | [MediaCodecComposeActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/MediaCodecComposeActivity.kt) | `GSYVideoType.enableMediaCodec()` / `disableMediaCodec()` 实时切换 |
| 24 | 自定义主题 Controls | [CustomControlsThemeComposeActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/CustomControlsThemeComposeActivity.kt) | Compose 自绘控件取代 `GSYDefaultControls`：渐变浮层 + Slider seek + 多主题切换 |

### 6.4 共享测试数据

| # | 名称 | 入口 | 说明 |
|---|---|---|---|
| 25 | Demo 数据 | [DemoSamples.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/DemoSamples.kt) | 复用与 Java/XML Demo 同一组测试 URL（`data object`，非可运行 Activity） |

---

## 七、发布

`gsyVideoPlayer-compose/build.gradle` 已 `apply` 了：

- `gradle/lib.gradle` —— 标准 `com.android.library` + `singleVariant('release') { withSourcesJar(); withJavadocJar() }`
- `gradle/publish.gradle` —— 发布到 GitHub Packages（`com.shuyu:gsyvideoplayer-compose`）
- `gradle/maven-central-publish.gradle` —— 发布到 Maven Central（`io.github.carguo:gsyvideoplayer-compose`）

### 1) Maven Central（推荐）

仓库根目录打 `vX.Y.Z` tag，触发 [.github/workflows/publish-maven-central.yml](../.github/workflows/publish-maven-central.yml)，会一并发布所有库模块（含 compose）：

```
io.github.carguo:gsyvideoplayer-compose:<PROJ_VERSION>
```

也可手动执行：

```bash
./gradlew :gsyVideoPlayer-compose:publishMavenCentralPublicationToSonatypeRepository \
          closeAndReleaseSonatypeStagingRepository \
          -PPUBLISH_TARGET=mavenCentral
```

### 2) GitHub Packages

打 tag（任意名）触发 [.github/workflows/release.yml](../.github/workflows/release.yml)：

```
com.shuyu:gsyvideoplayer-compose:<PROJ_VERSION>
```

手动执行：

```bash
./gradlew :gsyVideoPlayer-compose:publishReleasePublicationToGsyvideoplayerRepository \
          -PPUBLISH_TARGET=github
```

### 3) JitPack

JitPack 默认会跑 `./gradlew install` / `publishToMavenLocal`（详见根目录 [jitpack.yml](../jitpack.yml)），compose 模块会随聚合发布产出，使用方式：

```groovy
implementation 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-compose:vX.Y.Z'
```

> 三个渠道都直接复用现有的 GitHub Action 与 jitpack.yml，**无需额外为 compose 模块新增任务**。本地烟雾测试推荐：
> ```
> ./gradlew :gsyVideoPlayer-compose:assembleRelease publishToMavenLocal
> ```

---

## 八、模块结构速览

```
gsyVideoPlayer-compose/
└── src/main/java/com/shuyu/gsyvideoplayer/compose/
    ├── common/
    │   └── LifecycleBridge.kt                # 通用 Lifecycle ↔ Compose 桥
    ├── wrapper/
    │   ├── GSYVideoPlayerView.kt             # 模式一：包装 StandardGSYVideoPlayer
    │   └── GSYAnyVideoPlayerView.kt          # 模式一通用版：泛型 NormalGSYVideoPlayer
    └── native_/
        ├── GSYComposeHostPlayer.java         # 隐藏 UI 的 host player（仅留 surface_container）
        ├── GSYPlayerController.kt            # 控制器 + State 同步
        ├── GSYPlayerState.kt                 # State / Snapshot 数据类
        ├── GSYPlayerSurface.kt               # Surface + rememberGSYPlayerController
        └── GSYDefaultControls.kt             # 顶层 GSYComposePlayer + 默认控制条
```

---

## 九、能力矩阵：何时选 Wrapper / 何时选 Native

下表帮助 0~1 接入业务在两种模式之间快速决策。**列**是常见诉求，**行**是模式：

| 诉求 | Wrapper（`GSYVideoPlayerView` / `GSYAnyVideoPlayerView`） | Native（`GSYComposePlayer` / `GSYPlayerController` + `GSYPlayerSurface`） |
|---|---|---|
| 几乎零成本接入、与 Java demo 一一对应 | ✅ 推荐 | ⚠️ 需要自己写 UI / 状态订阅 |
| 复用所有 `GSYVideoOptionBuilder` 链式选项（~30+ 项，含字幕、回调、缓存、滤镜） | ✅ 直接 `.build(player)` | ⚠️ 仅常用项有 controller 直挂入口（其余仍可通过 `controller.setUp(builder)` 完整透传，或 `controller.withHost { player -> ... }` 直访 host API） |
| 内置全屏（`startWindowFullscreen`）、SwitchUtil 无缝切换 | ✅ 完整 | ❌ 需要自己组合（见 D8 SwitchSeamless） |
| 列表多 item 各自独立播放（每行一个 player） | ✅ 但每个都是完整 View 树，性能略重 | ✅ 推荐：单 controller + LazyColumn 复用 surface |
| 多窗口"真并行"（同屏多路同时出声） | ⚠️ 受 GSYVideoManager 互斥 | ✅ D7 多 CustomManager 实例 |
| Compose 原生订阅 events / state（流式驱动 UI） | ❌ 内部仍是命令式 callback | ✅ 推荐：`controller.events`（SharedFlow） + `controller.snapshot`（Compose `State<GSYPlayerSnapshot>`，`val s by controller.snapshot`） / `controller.stateFlow`（StateFlow） |
| 完全自定义控件层（手势、按钮、字幕、画中画…） | ⚠️ 需要继承 `StandardGSYVideoPlayer` 重写 | ✅ 推荐：`GSYPlayerSurface` + 自绘 |
| 需要"播完接力下一段"且**零黑屏** | ✅ Wrapper 路径走 SwitchUtil | ❌ 当前 Native setUp 会丢 surface 接管，会闪一下（P0-1 落地后改善，见 D7 注释） |
| 需要在不打断播放的前提下，跨 Activity / Fragment 复用同一个播放实例 | ⚠️ 需要手写 `release` 兜底 | ✅ controller `rememberSaveable` 化是后续 backlog |

**简化决策：**
- **新业务、需要 Compose 原生 UI 流式驱动** → Native
- **存量业务想最快从 Java/XML demo 平移过来** → Wrapper
- **拿不准** → 先 Wrapper 跑通，再按需局部替换为 Native（两条链路可共用同一份 Builder 配置）

---

## 十、Cookbook：从 Java/XML 迁移 Compose

下面 5 个常见场景给出"Java demo → Compose demo"的最小改造模板，仓库中均有对应 Activity 可直接对照。

### 1) 最小播放（基本播放）

**Java**：`DetailPlayer` 里 `findViewById(R.id.detail_player)` + `GSYVideoOptionBuilder().setUrl(...).build(player)`。

**Compose**：

```kotlin
GSYVideoPlayerView(
    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
    setUp = { player ->
        GSYVideoOptionBuilder()
            .setUrl(url)
            .setVideoTitle(title)
            .setIsTouchWiget(true)
            .build(player)
    },
)
```

对照：[BasicWrapperActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/BasicWrapperActivity.kt)（同时演示 setVideoAllCallBack / setSeekRatio / setShowPauseCover / setReleaseWhenLossAudio 等 5 个高频 builder 选项）

### 2) 全屏（含横竖屏 + 锁屏）

**Java**：`DetailPlayer` 里 `setRotateViewAuto(true).setLockLand(true).setNeedLockFull(true)` + `onBackPressed` 兜底。

**Compose（Wrapper）**：保持上方 builder 不变，把这三项打开即可，全屏由内部 `OrientationUtils` 接管；返回键拦截在 `BackHandler { ... }` 内调用 `player.onBackFullscreen()`。

**Compose（Native）**：自绘控件层，监听 `controller.snapshot.value.state.isPlaying` + 自定义 fullscreenComposable 切换，参考 [ListWithFullscreenActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/ListWithFullscreenActivity.kt)。

### 3) 列表内播放

**Java**：`ListNormalAdapter` 里手动管理"出屏 release / 入屏 setUp"。

**Compose**：单实例 `controller` + `LazyColumn` + `snapshotFlow { firstVisible to lastVisible }` 自动暂停。

```kotlin
val controller = rememberGSYPlayerController()
LaunchedEffect(listState, playingIndex) {
    snapshotFlow { listState.firstVisibleItemIndex to (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1) }
        .distinctUntilChanged()
        .collect { (first, last) ->
            if (playingIndex !in first..last) controller.pause()
        }
}
```

对照：[ListPlayNativeActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/ListPlayNativeActivity.kt)（含离屏 `pause` vs `setUp 重置` 两种语义切换 + 占位封面）

### 4) 字幕 / 回调 / 自定义封面

**Java**：`builder.setSubTitle(srt).setVideoAllCallBack(new GSYSampleCallBack(){ ... })`。

**Compose（Wrapper）**：直接复用同一份 builder。

```kotlin
.setVideoAllCallBack(object : GSYSampleCallBack() {
    override fun onPrepared(url: String?, vararg objects: Any?) {
        super.onPrepared(url, *objects)
        // ... 上报埋点 / 更新 UI state
    }
})
```

**Compose（Native）**：把 callback 替换为 `controller.events`（hot SharedFlow） / `controller.stateFlow`（StateFlow） / `controller.snapshot`（Compose `State<GSYPlayerSnapshot>`），用 `LaunchedEffect { controller.events.collect { ... } }` 即可订阅，**完全不依赖 callback 接口**。

### 5) 切源（同 player 切流 / 接力）

| 场景 | 路径 | 说明 |
|---|---|---|
| 暴力切源（允许一次黑屏 + 重新 prepare） | `controller.setUp(newUrl, ...)` | [SwitchUrlActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/SwitchUrlActivity.kt) |
| 无缝切源（同 controller 跨 Composable 复用，零黑屏） | 同一 `GSYPlayerController` + `GSYPlayerSurface` 在 list/detail 间换位 attach（host 内部 `setSurfaceToPlay` 接管，等价 Java 的 `SwitchUtil.savePlayState/clonePlayState`） | [SwitchSeamlessComposeActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/SwitchSeamlessComposeActivity.kt) |
| 列表自动接力下一段 | `controller.events.collect { if (it is AutoComplete) controller.setUp(next) }` | [AutoPlayListActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/AutoPlayListActivity.kt) （顶部 KDoc 注释了 surface 接管取舍） |

> **通用原则**：Java 里"链式 builder + callback"的写法，**Wrapper 路径几乎等价复用**；
> Native 路径把 callback 换成 `events` / `stateFlow` / `snapshot` 订阅，把 fullscreen 等粘性 View 行为改成"一个 Composable 状态切换"。

