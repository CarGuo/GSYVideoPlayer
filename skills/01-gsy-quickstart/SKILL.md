---
name: gsy-quickstart
description: 从零把 GSYVideoPlayer 塞进一个 Activity 或 Fragment，跑通「setUp → startPlayLogic → release」最小闭环。
when_to_use: 用户第一次在项目里集成 GSYVideoPlayer；只要能播一个 mp4 就行；或需要确认基础生命周期是否接对。
inputs:
  - name: play_url
    description: 视频地址（http/https/file/asset/rtmp/rtsp）
    required: true
  - name: container
    description: 承载 View 的容器（一般是 FrameLayout）
    required: true
outputs:
  - StandardGSYVideoPlayer 实例已 setUp 并可控（play/pause/release）
references:
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/video/StandardGSYVideoPlayer.java
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/video/base/GSYVideoView.java
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/GSYVideoManager.java
  - file: app/src/main/java/com/example/gsyvideoplayer/PlayActivity.java
---

# gsy-quickstart

## 依赖

- 主库：`com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-java:<version>`
- ijk so（至少选一个 ABI 或用 `x86` 组合）：`gsyVideoPlayer-armv7a` / `gsyVideoPlayer-armv64` / `gsyVideoPlayer-armv5` / `gsyVideoPlayer-ex_so`
- 权限：`INTERNET`、`WAKE_LOCK`、`ACCESS_NETWORK_STATE`（AndroidManifest）

明细见 [doc/DEPENDENCIES.md](file:///D:/workspace/project/GSYVideoPlayer/doc/DEPENDENCIES.md)。

## 核心 API

| 类 / 方法 | 位置 | 作用 |
|---|---|---|
| `StandardGSYVideoPlayer(Context)` | [StandardGSYVideoPlayer](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/video/StandardGSYVideoPlayer.java) | 主用 UI 组件，默认布局在 `R.layout.video_layout_standard` |
| `setUp(url, cacheWithPlay, title)` | [GSYVideoView#L438-L451](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/video/base/GSYVideoView.java#L438-L451) | 最短形式的地址绑定 |
| `setUp(url, cacheWithPlay, cachePath, title)` | [GSYVideoView#L477-L488](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/video/base/GSYVideoView.java#L477-L488) | 支持自定义缓存目录 |
| `setUp(url, cacheWithPlay, cachePath, headers, title)` | [GSYVideoView#L453-L475](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/video/base/GSYVideoView.java#L453-L475) | 附带 HTTP header |
| `startPlayLogic()` | `StandardGSYVideoPlayer`（继承自 `GSYVideoControlView`） | 触发 prepare→start 全流程 |
| `getCurrentPlayer()` | `GSYBaseVideoPlayer` | 获取"当前活跃 player"（可能是全屏克隆体） |
| `onVideoPause()` / `onVideoResume()` / `onVideoResume(boolean seek)` | [GSYVideoView#L518-L577](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/video/base/GSYVideoView.java#L518-L577) | 对应 Activity `onPause / onResume`；直播场景传 `seek=false` |
| `release()` | [GSYVideoView#L818](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/video/base/GSYVideoView.java#L818) | 释放当前 Player 与 Surface |
| `GSYVideoManager.releaseAllVideos()` | [GSYVideoManager#L99-L104](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/GSYVideoManager.java#L99-L104) | 全局收尾（切页面时兜底） |
| `GSYVideoManager.onPause() / onResume()` | [GSYVideoManager#L110-L135](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/GSYVideoManager.java#L110-L135) | 全局暂停/恢复所有 GSY player |

## 参数表：`setUp(...)`

| 参数 | 类型 | 说明 |
|---|---|---|
| `url` | `String` | 支持 http/https/file/asset/rtmp/rtsp/自定义 scheme；`ProxyCacheManager` 会自动拦截可缓存的普通 http |
| `cacheWithPlay` | `boolean` | true → 边播边缓；对直播 / m3u8 / DRM 建议 false |
| `cachePath` | `File?` | 自定义缓存目录，默认 `context.cacheDir/videoCache` |
| `mapHeadData` | `Map<String,String>?` | HTTP 请求头（Referer、Cookie、Auth 等） |
| `title` | `String` | 顶部标题栏文案，会显示在 `R.id.title` |

返回值：`boolean`，false 表示 URL 为空或与当前 tag 相同（防重复 setup）。

## 最小调用序（Activity 生命周期）

1. `onCreate` → `player.setUp(...)` → 可选 `player.setThumb...`、`setPlayTag()` → `player.startPlayLogic()`
2. `onPause` → `player.getCurrentPlayer().onVideoPause()`
3. `onResume` → `player.getCurrentPlayer().onVideoResume()`（直播传 `false`）
4. `onBackPressed` → 若在全屏：`GSYVideoManager.backFromWindowFull(this)` 返回 true 就吞掉事件
5. `onDestroy` → `player.getCurrentPlayer().release()` + `GSYVideoManager.releaseAllVideos()`

## Demo 对照

- 最短一屏：[PlayActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/PlayActivity.java)
- Fragment：[VideoFragment](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/fragment/VideoFragment.java) + [FragmentVideoActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/FragmentVideoActivity.java)
- 空控件模式（自绘 UI）：[PlayEmptyControlActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/PlayEmptyControlActivity.java) + [EmptyControlVideo](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/video/EmptyControlVideo.java)

## 常见坑

- 忘记 `release()` → dex 泄漏 + surface 崩溃：一定在 `onDestroy` 兜底。
- 不覆盖 `getCurrentPlayer()` 直接调 `player.onVideoPause()`：全屏切回后指令发到"影子"实例，导致状态错乱。
- `setUp` 传空 title 走 `null`，会隐藏顶部栏（`View.GONE`），不会崩。
