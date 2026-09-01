---
name: gsy-kernel-switch
description: 在 IJK / System / ExoPlayer2(Media3) / Aliyun 四个媒体内核之间自由切换，以及自定义 Manager 的方法。
when_to_use: 需要支持 DASH/SmoothStreaming、需要 Media3 track selection、想减 so 体积只留系统解码、要用 Aliyun 内核、或者要多实例并行独立 Manager。
inputs:
  - name: kernel
    description: ijk / system / exo / ali / custom
    required: true
outputs:
  - PlayerFactory 与（可选）CacheFactory 已切换到目标 Manager
references:
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/player/PlayerFactory.java
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/player/IjkPlayerManager.java
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/player/SystemPlayerManager.java
  - file: gsyVideoPlayer-base/src/main/java/com/shuyu/gsyvideoplayer/player/IPlayerManager.java
  - file: gsyVideoPlayer-base/src/main/java/com/shuyu/gsyvideoplayer/player/BasePlayerManager.java
  - file: gsyVideoPlayer-exo_player2
  - file: gsyVideoPlayer-aliplay/src/main/java/com/shuyu/aliplay/AliPlayerManager.java
  - file: app/src/main/java/com/example/gsyvideoplayer/exo/GSYExoPlayerManager.java
  - file: app/src/main/java/com/example/gsyvideoplayer/video/manager/CustomManager.java
---

# gsy-kernel-switch

## 内核矩阵

| Kernel | Class | 来源模块 | 特点 |
|---|---|---|---|
| **IJK**（默认） | [IjkPlayerManager](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/player/IjkPlayerManager.java) | `gsyVideoPlayer-java` + `armv*/ex_so` | ffmpeg 全格式；so ~15 MB/ABI；软/硬解双通道 |
| **System** | [SystemPlayerManager](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/player/SystemPlayerManager.java) | `gsyVideoPlayer-java` | 无 so；靠 `android.media.MediaPlayer`；直播/复杂容器兼容差 |
| **Exo/Media3** | `GSYExoPlayerManager`（Demo 内）／`GSYExo2MediaPlayer` | `gsyVideoPlayer-exo_player2` | DASH / HLS / SmoothStreaming / TrackSelection |
| **Aliyun** | [AliPlayerManager](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-aliplay/src/main/java/com/shuyu/aliplay/AliPlayerManager.java) | `gsyVideoPlayer-aliplay` | 阿里云 vid/authInfo；HLS 加密 |

## 切换 API

**全局切换**：`PlayerFactory.setPlayManager(Class<? extends IPlayerManager>)`（内部 `getPlayManager` 用 `sPlayerManager.newInstance()` 反射构造，故被切入的类**必须**有 `public` 无参构造）。见 [PlayerFactory#L15-L27](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/player/PlayerFactory.java#L15-L27) 与本仓库 R8 保留规则 [proguard-rules.pro](file:///D:/workspace/project/GSYVideoPlayer/app/proguard-rules.pro)。

```java
// Exo
PlayerFactory.setPlayManager(Exo2PlayerManager.class);
CacheFactory.setCacheManager(ExoPlayerCacheManager.class);

// System
PlayerFactory.setPlayManager(SystemPlayerManager.class);

// Aliyun
PlayerFactory.setPlayManager(AliPlayerManager.class);
```

必须在 `player.setUp(...)` **之前**调用；否则本次实例仍用旧内核。

## `IPlayerManager` 抽象

统一接口见 [IPlayerManager](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-base/src/main/java/com/shuyu/gsyvideoplayer/player/IPlayerManager.java)。核心方法（子类必须实现）：

| 方法 | 说明 |
|---|---|
| `initVideoPlayer(Context, Message, List<VideoOptionModel>, ICacheManager)` | 初始化内核；`Message.obj` 是 `GSYModel` |
| `showDisplay(Message)` | 绑定 Surface / SurfaceHolder |
| `setSpeed(float, boolean soundTouch)` | 速率 + 变速不变调 |
| `setNeedMute(boolean)` | 静音 |
| `setVolume(float, float)` | 双通道音量 |
| `getMediaPlayer()` | 底层 player 实例 |
| `isSurfaceSupportLockCanvas()` | 是否支持 `lockCanvas`（TextureView 补帧） |
| `start / pause / stop / seekTo / release / getCurrentPosition / getDuration / getVideoSarNum / getVideoSarDen / getVideoWidth / getVideoHeight` | 基础控制 |

推荐继承 [BasePlayerManager](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-base/src/main/java/com/shuyu/gsyvideoplayer/player/BasePlayerManager.java) 以少写模板。

## `IPlayerInitSuccessListener` hook

[IPlayerInitSuccessListener](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-base/src/main/java/com/shuyu/gsyvideoplayer/player/IPlayerInitSuccessListener.java) 允许在 `initVideoPlayer` 完成后对底层 Player 拿手（比如设置 Exo 的 TrackSelector）：

```java
IjkPlayerManager.setIjkLibLoader(...);
// 或
GSYExoVideoManager.setPlayerInitSuccessListener(new IPlayerInitSuccessListener() {
    @Override public void onPlayerInitSuccess(IMediaPlayer mp, GSYModel model) { ... }
});
```

## `CacheFactory` 同步切换

不同内核有各自缓存方案；切内核记得配套：

| Player Manager | 对应 CacheManager |
|---|---|
| `IjkPlayerManager` / `SystemPlayerManager` | [ProxyCacheManager](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cache/ProxyCacheManager.java)（默认） |
| Exo/Media3 | `ExoPlayerCacheManager`（`gsyvideoplayer-exo_player2` 内） |
| Aliyun | 由 Aliyun SDK 自身管理，一般 `CacheFactory` 保持 `ProxyCacheManager` 或自定义空实现 |

## 多实例并行

用 [CustomManager](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/video/manager/CustomManager.java) 模式：拷贝 `GSYVideoManager` 单例套壳，在自定义 `GSYVideoView` 里覆盖 `getGSYVideoManager()`；不同页面互不影响，参考 [MultiSampleVideo](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/video/MultiSampleVideo.java) + [ListMultiVideoActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/ListMultiVideoActivity.java)。

## Demo 对照

- Exo 切源 / 无缝切换：[DetailExoListPlayer](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/exo/DetailExoListPlayer.java)、[ExoAdaptiveTrackActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/exo/ExoAdaptiveTrackActivity.java)
- MediaCodec 智能回退：[SmartMediaCodecFallbackActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/SmartMediaCodecFallbackActivity.java) + [doc/SMART_MEDIACODEC_FALLBACK_RESEARCH.md](file:///D:/workspace/project/GSYVideoPlayer/doc/SMART_MEDIACODEC_FALLBACK_RESEARCH.md)

## 常见坑

- `PlayerFactory.setPlayManager` 传入的类**必须**有 `public` 无参构造，且被 ProGuard 保留。
- Exo 切源建议用 `Exo2PlayerManager` + `GSYExo2MediaPlayer`；直接换 Manager 但用 Ijk 的 URL 会走缓存代理导致 DASH 解析失败——记得 `setOverrideExtension("mpd")` 或关闭缓存。
- 系统内核 `SystemPlayerManager` 不支持 `soundTouch`、多数直播格式；生产环境仅作 fallback。
