---
name: gsy-live-and-ads
description: 直播（RTMP/RTSP/HLS）、贴片广告 (PreRoll AD)、静音、切码率、无缝切源、切换过程保留最后一帧。
when_to_use: 做直播；短视频/长视频前贴片广告；PV/UV 埋点后要 A/B 分发；快节奏内容切换。
inputs:
  - name: stream_url
    description: 直播地址（rtmp://.../ rtsp://.../ .m3u8 等）
    required: false
  - name: ad_url
    description: 前贴广告地址
    required: false
outputs:
  - 直播不使用缓存；广告播放完切主片；无缝续播不闪黑。
references:
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/video/GSYADVideoPlayer.java
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/video/GSYSampleADVideoPlayer.java
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/GSYVideoADManager.java
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/GSYBaseADActivityDetail.java
  - file: app/src/main/java/com/example/gsyvideoplayer/DetailADPlayer.java
  - file: app/src/main/java/com/example/gsyvideoplayer/DetailADPlayer2.java
  - file: app/src/main/java/com/example/gsyvideoplayer/KeepLastFrameDemoActivity.java
---

# gsy-live-and-ads

## 直播接入要点

- `setUp(url, false, title)` —— `cacheWithPlay` 必须 `false`。
- `onResume` / `player.onVideoResume(false)`：**`seek=false` 避免拉流跳变**（见 [GSYVideoView#L549](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/video/base/GSYVideoView.java)）。
- 内核选择：
  - RTMP / RTSP：**必须 IJK**；System / Exo / Aliyun 各有兼容性差异。
  - HLS：Exo 更稳；Ijk 需要开启硬解 `GSYVideoType.enableMediaCodec()` 才顺。
- 关键参数：
  - `builder.setNeedShowWifiTip(false)`（后台 App 不打断）
  - `builder.setReleaseWhenLossAudio(false)`（丢音频焦点不释放）
  - `builder.setSurfaceErrorPlay(true)`

对 IJK 加 low-latency：通过 `VideoOptionModel` 注入 ffmpeg option，见 [BasePlayerManager](../../gsyVideoPlayer-base/src/main/java/com/shuyu/gsyvideoplayer/player/BasePlayerManager.java) 与 [GSYVideoManager.instance().setOptionModelList(list)](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/GSYVideoBaseManager.java)。常用 option：`analyzemaxduration=100`、`probesize=10240`、`flush_packets=1`、`fflags=nobuffer`、`packet-buffering=0`、`framedrop=1`。

## 广告 AD

- 双 Manager 双 Player：`GSYVideoADManager.instance()` 负责广告 player，`GSYVideoManager.instance()` 负责主片；两者共存不打架。
- 组件 [GSYADVideoPlayer](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/video/GSYADVideoPlayer.java) 与"广告示例"[GSYSampleADVideoPlayer](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/video/GSYSampleADVideoPlayer.java) 都继承 `StandardGSYVideoPlayer`。
- Activity 基类：[GSYBaseADActivityDetail](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/GSYBaseADActivityDetail.java)，提供 `getGSYVideoADPlayer()` / `initADVideo()` / `startAD(String adUrl, String url)` / `onADEnd`。

### `startAD(...)` 关键调用序

1. Activity 里持有主 player + AD player 两个 View；
2. `initADVideo()` 装配（内部会连锁 `OrientationUtils`）；
3. `startAD(adUrl, mainUrl)` 播广告；
4. AD complete → SDK 内部隐藏 AD player、start 主 player；
5. 用户点"跳过"→ `getGSYVideoADPlayer().onAutoCompletion()`。

## 无缝切源 / 切码率

Exo 内核用 `Exo2PlayerManager.setCurrentMediaSource(...)` 或 Demo 里的 `GSYExo2MediaPlayer.setMediaSources(...)`；Ijk 则重新 `setUp` + `startPlayLogic`。切换的过程中要求"不闪黑"，用：

- [KeepLastFrameVideo](../../app/src/main/java/com/example/gsyvideoplayer/video/KeepLastFrameVideo.java)（Demo 组件）+ [KeepLastFrameDemoActivity](../../app/src/main/java/com/example/gsyvideoplayer/KeepLastFrameDemoActivity.java)；
- 原理见 [doc/KEEP_LAST_FRAME.md](../../doc/KEEP_LAST_FRAME.md)：新 player 拿到首帧前，把旧 SurfaceTexture 内容以 Bitmap 覆盖在上层。

## 广告 / 主片全屏切换的两个陷阱

- 主片全屏 → 广告插入：`GSYVideoADManager` 无内部 `OrientationUtils`；`GSYBaseADActivityDetail` 已处理，但**自定义子类需重写 `getGSYVideoADPlayer()` 返回 AD player 才能被基类的旋转/回退纳管**。
- 广告全屏返回 → 直接销毁 AD player：`onBackPressed` 需要判断 `GSYVideoManager.backFromWindowFull(this) || GSYVideoADManager.backFromWindowFull(this)`。

## Demo 对照

- 单主片：[DetailPlayer](../../app/src/main/java/com/example/gsyvideoplayer/DetailPlayer.java)
- 前贴片 + 主片：[DetailADPlayer](../../app/src/main/java/com/example/gsyvideoplayer/DetailADPlayer.java) / [DetailADPlayer2](../../app/src/main/java/com/example/gsyvideoplayer/DetailADPlayer2.java)
- 列表条目内广告：[ListADVideoActivity](../../app/src/main/java/com/example/gsyvideoplayer/ListADVideoActivity.java) / [ListADVideoActivity2](../../app/src/main/java/com/example/gsyvideoplayer/ListADVideoActivity2.java) / [RequestListADVideoPlayer](../../app/src/main/java/com/example/gsyvideoplayer/video/RequestListADVideoPlayer.java)
- 智能硬解回退：[SmartMediaCodecFallbackActivity](../../app/src/main/java/com/example/gsyvideoplayer/SmartMediaCodecFallbackActivity.java)

## v14.0.0 RTSP 与倍速

- FORMAT `timeout` 为微秒（`20_000_000L` = 20 秒），明确配置的 `rtsp_transport` 会透传。
- PLAYER `rtsp-live-max-buffer-ms` 在 prepare 前设置，`0` 关闭，`500..60000` 毫秒用于正常 1× 直播队列恢复。恢复会丢弃旧媒体并产生跳跃/音频间隙，不是自动重连。
- 初始化重定向覆盖 OPTIONS/DESCRIBE/SETUP/首次 PLAY；不支持播放中 REDIRECT 或任意跨端口控制会话。
- `soundtouch=1` 为 0.25×–4× 软件变速；`soundtouch=0` 为设备相关平台范围。引擎切换和 EOF 按真实音频排空处理，终止错误后需要 reset 或重建。
- 详见 [QUESTION.md](../../doc/QUESTION.md) 与 [v14 发布说明](../../doc/UPDATE_VERSION.md#v1400-2026-10-10)。

## 常见坑

- 直播卡顿升级 → 检查是否开启 `cacheWithPlay`（对直播来说必须关）。
- 广告 `onAutoComplete` 后主片没自动 setUp → 检查是不是把 `setStartAfterPrepared(false)` 传给了主 builder。
- RTMP 断流回收后重连白屏 → `onVideoResume(false)` + `player.getCurrentPlayer().onVideoReset()` 再 `startPlayLogic()`。
