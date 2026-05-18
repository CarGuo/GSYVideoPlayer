# GSYVideoPlayer Compose 支持

新增模块：`gsyVideoPlayer-compose`，提供两种使用方式：

| 模式 | 适用场景 | 使用入口 |
|---|---|---|
| **Wrapper（AndroidView 包装）** | 已用 GSY，想快速塞进 Compose 屏；保留全屏、手势、缓存、字幕、滤镜等全部能力 | `GSYVideoPlayerView { ... }` |
| **Native（Compose 原生控件层）** | 想完全用 Compose 重绘控制 UI，但仍复用 GSY 多内核与渲染管线 | `GSYComposePlayer + GSYPlayerController` |

模块基于 [gsyVideoPlayer-java](../gsyVideoPlayer-java)，**不修改任何旧代码**。

---

## 一、引入依赖

```groovy
// 方式 A：使用项目模块
implementation project(":gsyVideoPlayer-compose")

// 方式 B：使用 Maven Central 发布版（13.0.0+）
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

val snap: State<GSYPlayerSnapshot> = controller.snapshot
// snap.value -> GSYPlayerSnapshot(state, currentPosition, duration,
//                                 bufferPercent, isPlaying, videoWidth, videoHeight)
```

`GSYPlayState` 状态机与原生 `GSYVideoView.CURRENT_STATE_*` 一一对应：

```
Idle  Preparing  Playing  Buffering  Paused  Completed  Error
```

---

## 四、生命周期与全屏

### Wrapper 模式

由 `GSYVideoPlayerView` 自动处理；如需手动控制，传 `autoPauseResume = false` 并自己用 `LifecycleEffect`。

全屏可直接调用底层 `player.startWindowFullscreen(...)`，与传统用法一致。

### Native 模式

`rememberGSYPlayerController` 会在离开 Composition 时自动 `release`。
全屏推荐使用 `Dialog(properties = DialogProperties(usePlatformDefaultWidth = false))` 或 Compose Navigation 切路由。

---

## 五、内核切换

与原版完全一致，不受 Compose 模块影响：

```kotlin
PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)
CacheFactory.setCacheManager(ExoPlayerCacheManager::class.java)
```

---

## 六、Demo

App 模块下入口 `Compose Demo`（[ComposeDemoListActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/ComposeDemoListActivity.kt)）汇总了 9 个可运行示例：

| # | 名称 | 入口 | 说明 |
|---|---|---|---|
| 1 | Basic Wrapper | [BasicWrapperActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/BasicWrapperActivity.kt) | `GSYVideoPlayerView` 最小用法 |
| 2 | Switch URL | [SwitchUrlActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/SwitchUrlActivity.kt) | Wrapper 模式下切流 |
| 3 | Multi-Window | [MultiWindowActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/MultiWindowActivity.kt) | 多个播放器同屏共存 |
| 4 | List Play (Native) | [ListPlayNativeActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/ListPlayNativeActivity.kt) | LazyColumn + Native Composable |
| 5 | Auto-Play List | [AutoPlayListActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/AutoPlayListActivity.kt) | 滚动可视区自动播放/释放 |
| 6 | List with Fullscreen | [ListWithFullscreenActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/ListWithFullscreenActivity.kt) | 列表内层级式全屏 |
| 7 | Detail Native | [DetailNativeActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/DetailNativeActivity.kt) | Native 模式详情页 |
| 8 | Full-Feature Native | [FullFeatureNativeActivity.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/FullFeatureNativeActivity.kt) | 速率 / Seek / 错误 / 完成态 |
| 9 | Demo 数据 | [DemoSamples.kt](../app/src/main/java/com/example/gsyvideoplayer/compose/host/DemoSamples.kt) | 复用与 Java/XML Demo 同一组测试 URL |

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
