---
name: gsy-compose-integration
description: 在 Jetpack Compose 中通过 AndroidView 桥接 GSYVideoPlayer，覆盖列表、竖直短视频、生命周期管控。
when_to_use: 项目已经或正在迁移到 Compose；需要在可组合项里嵌入播放器；需要复用列表滑动/自动播放逻辑但 UI 用 Compose。
inputs:
  - name: url
    description: 视频 URL
    required: true
outputs:
  - Composable 内嵌 GSYVideoPlayer，生命周期与 Composable 一致。
references:
  - file: gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose
  - file: app/src/main/java/com/example/gsyvideoplayer/compose
---

# gsy-compose-integration

## 模块

`gsyVideoPlayer-compose` 是 Compose 桥接层，两种模式并存：

| 模式 | 入口 | 说明 |
|---|---|---|
| **Wrapper**（推荐上手） | [GSYVideoPlayerView](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/wrapper/GSYVideoPlayerView.kt) / [GSYAnyVideoPlayerView](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/wrapper/GSYAnyVideoPlayerView.kt) | `AndroidView` 直接承载 `StandardGSYVideoPlayer` / 任意 `NormalGSYVideoPlayer` 子类，保留全部内置能力 |
| **Native**（纯 Compose 控件） | [rememberGSYPlayerController](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/native_/GSYPlayerSurface.kt) + [GSYPlayerSurface](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/native_/GSYPlayerSurface.kt) + [GSYDefaultControls](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/native_/GSYDefaultControls.kt) | 只承载画面，控制层完全用 Compose 自绘 |
| **通用工具** | [LifecycleBridge](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/common/LifecycleBridge.kt) | `LifecycleEffect { event -> ... }` 用于订阅宿主 Lifecycle |

## Wrapper API：`GSYVideoPlayerView`

见 [GSYVideoPlayerView#L28-L36](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/wrapper/GSYVideoPlayerView.kt#L28-L36)：

| 参数 | 类型 | 说明 |
|---|---|---|
| `modifier` | `Modifier` | 通常 `Modifier.fillMaxWidth().aspectRatio(16f/9)` |
| `setUp` | `(StandardGSYVideoPlayer) -> Unit` | 等价 `GSYVideoOptionBuilder.build(player)` |
| `setUpKey` | `Any?` = null | 非空时其变化会重新调用 `setUp`（典型：传 url） |
| `autoReleaseOnDispose` | Boolean = true | 离屏自动 `player.release()` |
| `autoPauseResume` | Boolean = true | 自动桥 Lifecycle：`ON_PAUSE→GSYVideoManager.onPause()`、`ON_RESUME→onResume()` |
| `onPlayerCreated` | `(StandardGSYVideoPlayer) -> Unit` | 拿到 player 实例的回调 |

## Wrapper API：`GSYAnyVideoPlayerView<T : NormalGSYVideoPlayer>`

见 [GSYAnyVideoPlayerView#L26-L35](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/wrapper/GSYAnyVideoPlayerView.kt#L26-L35)。相比 `GSYVideoPlayerView` 多出：

| 参数 | 类型 | 说明 |
|---|---|---|
| `factory` | `(Context) -> T` | 传自定义子类（如 `ListGSYVideoPlayer`、`SampleControlVideo`、`DanmakuVideoPlayer`） |

## Native API：`rememberGSYPlayerController`

见 [rememberGSYPlayerController#L26-L32](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/native_/GSYPlayerSurface.kt#L26-L32)：

| 参数 | 默认 | 说明 |
|---|---|---|
| `url` | null | 非空时内部 `LaunchedEffect` 触发 `controller.setUp(...)` |
| `cacheWithPlay` | false | 边播边缓 |
| `title` | "" | 标题（透传给底层 player） |
| `autoPlay` | false | attach 完成后自动 `startPlayLogic` |
| `autoPauseResume` | true | 自动订阅 Lifecycle（同 wrapper） |

返回 [GSYPlayerController](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/native_/GSYPlayerController.kt)，可以再调 `.play() / .pause() / .seekTo(ms) / .setSpeed(...) / .setUp(url, cache, title, autoPlay) / .dispose()`；状态订阅走 [GSYPlayerState](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/native_/GSYPlayerState.kt)（`StateFlow<GSYPlayerState>`），事件走 [GSYPlayerEvent](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/native_/GSYPlayerEvent.kt)。

## 最小可运行示例

**Wrapper 模式**：

```kotlin
GSYVideoPlayerView(
    modifier = Modifier.fillMaxWidth().aspectRatio(16f/9),
    setUpKey = url,
    setUp = { player ->
        GSYVideoOptionBuilder()
            .setUrl(url)
            .setCacheWithPlay(true)
            .setVideoTitle("demo")
            .setIsTouchWiget(true)
            .setAutoFullWithSize(true)
            .build(player)
        player.startPlayLogic()
    }
)
```

**Native 模式**：

```kotlin
val controller = rememberGSYPlayerController(url = url, autoPlay = true)
Box(Modifier.fillMaxWidth().aspectRatio(16f/9)) {
    GSYPlayerSurface(controller = controller, modifier = Modifier.matchParentSize())
    GSYDefaultControls(controller = controller)
}
```

## 生命周期最佳实践

```kotlin
val lifecycle = LocalLifecycleOwner.current.lifecycle
DisposableEffect(lifecycle) {
    val obs = LifecycleEventObserver { _, e ->
        when (e) {
            Lifecycle.Event.ON_PAUSE  -> state.pause()
            Lifecycle.Event.ON_RESUME -> state.resumeIfNeeded()
            Lifecycle.Event.ON_DESTROY-> state.release()
            else -> Unit
        }
    }
    lifecycle.addObserver(obs)
    onDispose { lifecycle.removeObserver(obs) }
}
```

## Demo 对照

| 场景 | Activity |
|---|---|
| 原生桥接示例 | [DetailNativeActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/DetailNativeActivity.kt) / [BasicWrapperActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/BasicWrapperActivity.kt) / [FullFeatureNativeActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/FullFeatureNativeActivity.kt) |
| 广告 + 主片（列表内） | [AdInListComposeActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/AdInListComposeActivity.kt) |
| 列表 + 全屏 | [ListWithFullscreenActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/ListWithFullscreenActivity.kt) / [ListPlayNativeActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/ListPlayNativeActivity.kt) / [AutoPlayListActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/AutoPlayListActivity.kt) |
| 抖音式竖屏 | [VerticalShortVideoComposeActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/VerticalShortVideoComposeActivity.kt) |
| 滤镜 | [DetailFilterComposeActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/DetailFilterComposeActivity.kt) |
| 无缝切源 / Exo 切源 | [SwitchSeamlessComposeActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/SwitchSeamlessComposeActivity.kt) / [ExoSwitchSourceComposeActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/ExoSwitchSourceComposeActivity.kt) / [SwitchUrlActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/SwitchUrlActivity.kt) |
| 字幕 | [SubtitleComposeActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/SubtitleComposeActivity.kt) |
| 弹幕 | [DanmakuComposeActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/DanmakuComposeActivity.kt) |
| 缓存 / 下载 | [CacheDownloadComposeActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/CacheDownloadComposeActivity.kt) |
| 硬解 / MediaCodec | [MediaCodecComposeActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/MediaCodecComposeActivity.kt) |
| 悬浮小窗 | [FloatingWindowComposeActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/FloatingWindowComposeActivity.kt) |
| 音频独立 | [AudioOnlyComposeActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/AudioOnlyComposeActivity.kt) |
| 多类型混排 | [MoreTypeComposeActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/MoreTypeComposeActivity.kt) |
| 多窗口并行 | [MultiWindowActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/MultiWindowActivity.kt) / [MultiWindowParallelComposeActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/MultiWindowParallelComposeActivity.kt) |
| 本地文件 | [LocalFileComposeActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/LocalFileComposeActivity.kt) |
| 自定义主题 | [CustomControlsThemeComposeActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/CustomControlsThemeComposeActivity.kt) |
| WebView 详情 | [WebDetailComposeActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/WebDetailComposeActivity.kt) |

样例 hosts 全在 [compose/host/](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host)。

## 常见坑

- Compose 频繁重组 → `AndroidView` 的 `factory` 只跑一次；不要把 `setUp` 放在 `factory` 里，放到 `update` block，并用 `remember(url)` 做去重。
- `AndroidView` 内部布局要指定固定高度或 `aspectRatio`，否则 SurfaceView 会 0 高度导致黑屏。
- 竖直短视频 + `Pager`：切页时用 `pagerState.currentPage` 触发 `state.play(url) / previousState.pause()`；释放放在 `DisposableEffect(pagerState)` 里。
- 与 Compose 主题深浅色切换：`GSYVideoPlayerCompose` 是原生 View 层，配色需要单独 `setBottomProgressBarDrawable` 等 API。
