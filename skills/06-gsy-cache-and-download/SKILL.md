---
name: gsy-cache-and-download
description: 边播边缓（AndroidVideoCache 代理）、自定义缓存目录、清理缓存、离线下载至完整文件。
when_to_use: 需要"边看边攒"离线可播；或空间敏感需要限量清理；或做真下载入库（不是缓存代理）。
inputs:
  - name: url
    description: 网络视频地址（http/https）
    required: true
  - name: cache_dir
    description: 缓存目录（默认 context.cacheDir/videoCache）
    required: false
outputs:
  - 播放同时把 mp4 缓存到本地；命中缓存后重播 0 网络。
references:
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cache/ProxyCacheManager.java
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cache/CacheFactory.java
  - file: gsyVideoPlayer-base/src/main/java/com/shuyu/gsyvideoplayer/cache/ICacheManager.java
  - file: app/src/main/java/com/example/gsyvideoplayer/DetailDownloadPlayer.java
  - file: app/src/main/java/com/example/gsyvideoplayer/DetailDownloadExoPlayer.java
---

# gsy-cache-and-download

## 概念

- **缓存**（cache）：`ProxyCacheManager` 内部起本地 HTTP 代理，拦截 `player` 的请求，把响应流一边喂给 player 一边写到本地；文件名是 URL MD5。
- **下载**（download）：与缓存共享目录，但一定要走"从头到尾"完整播完才能保证文件可复用为独立 mp4；提前 seek 会造成 range 分片，回放时代理会重新下载缺失段。

## 开启缓存

方式 1：`setUp(url, true, title)` 第二参数 `cacheWithPlay=true`。

方式 2：`GSYVideoOptionBuilder`：
- `setCacheWithPlay(true)`
- `setCachePath(File)`（可选）

## 自定义缓存目录

```java
File dir = new File(getExternalFilesDir(null), "videoCache");
builder.setCachePath(dir);
```

若使用不同 `CacheManager`（例如 Exo），传给 `CacheFactory.setCacheManager(...)` 前记得也走 `setCachePath` 或自定义 `ICacheManager` 实现。

## `ICacheManager` API

见 [ICacheManager](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-base/src/main/java/com/shuyu/gsyvideoplayer/cache/ICacheManager.java)。核心方法：

| 方法 | 说明 |
|---|---|
| `doCacheLogic(Context, IMediaPlayer, String url, Map<String,String> headers, File cachePath)` | 播放前介入，把 url 换为代理 url |
| `clearCache(Context, File cachePath, String url)` | 单条清除；`url = null` 清空整目录 |
| `hadCached()` | 当前播放是否已"命中"缓存（100% 完成） |
| `setCacheAvailableListener(CacheAvailableListener)` | 缓存进度回调 |
| `release()` | 释放代理 |

## `ProxyCacheManager` 关键静态方法

在 [ProxyCacheManager](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cache/ProxyCacheManager.java) 里：

- `clearAllCache(Context, File)` —— 清空整个缓存目录。
- 内部 `HttpProxyCacheServer` 单例 lazy 初始化，第一次 `doCacheLogic` 时才起服务；Application 就绪即可。

## `ProxyCacheUserAgentHeadersInjector`

自定义 UA / Cookie / Referer 注入到代理层：见 [ProxyCacheUserAgentHeadersInjector](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cache/ProxyCacheUserAgentHeadersInjector.java)。挂到 `HttpProxyCacheServer.Builder.headerInjector()` 上，或直接用 `Map` 传给 `setUp`。

## 离线下载

三步流程：

1. 用 `HttpProxyCacheServer` 单独跑一次预下载（不 attach 到 Player），或直接开一条播放并 `setStartAfterPrepared(false)` 静默走全长；
2. 事件 `VideoAllCallBack#onAutoComplete` 触发 → 缓存文件已完整；
3. 用 [FileUtils](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/utils/FileUtils.java) 拷贝 / 重命名到业务目录。

Demo：[DetailDownloadPlayer](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/DetailDownloadPlayer.java)（IJK）与 [DetailDownloadExoPlayer](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/DetailDownloadExoPlayer.java)（Exo）。

## 不缓存的场景

以下情况 `cacheWithPlay` 应设 `false`：
- 直播 / RTMP / RTSP（连续无尽流）；
- HLS m3u8（代理逻辑对分片不友好，建议 Exo 内核 + Exo cache）；
- DASH / SmoothStreaming；
- DRM 或私有加密。

## 常见坑

- **代理端口被占**：`HttpProxyCacheServer` 首次启动失败会 fallback 到原始 url，缓存不生效。多进程时可能出现，建议仅主进程用。
- **缓存目录跨用户**：`context.cacheDir` 是 per-user；用外部存储需要动态权限。
- **同 URL 缓存名冲突**：MD5(url) 相同即命中；如果 URL 带一次性签名，每次不同 → 缓存穿透。可以先规范化 URL 再传给 `setUp`。
- **HTTP header 变更失效**：headers 参与代理请求生成，但缓存文件名只按 URL；同一 URL 换 UA 不会重新下载。
