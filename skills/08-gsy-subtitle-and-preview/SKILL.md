---
name: gsy-subtitle-and-preview
description: 外挂/内嵌字幕（SRT/VTT）、Exo 内嵌字幕轨、Seek 悬浮预览缩略图（VTT thumbnails）。
when_to_use: 需要显示 srt/vtt/ass 字幕、切换字幕轨、seek 时鼠标/手指浮层展示预览帧。
inputs:
  - name: subtitle_source
    description: 字幕文件路径或 URL、MIME
    required: false
  - name: preview_source
    description: VTT 预览描述文件
    required: false
outputs:
  - player 上叠加 subtitle view；seekbar 拖动出现预览帧。
references:
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/subtitle
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/preview
  - file: app/src/main/java/com/example/gsyvideoplayer/SubtitleDetailPlayer.java
  - file: app/src/main/java/com/example/gsyvideoplayer/exosubtitle/GSYExoSubTitlePlayer.java
  - file: app/src/main/java/com/example/gsyvideoplayer/video/PreViewGSYVideoPlayer.java
---

# gsy-subtitle-and-preview

## 字幕 API

顶层控制器：[GSYSubtitleController](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/subtitle/GSYSubtitleController.java)。视图：[GSYSubtitleView](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/subtitle/GSYSubtitleView.java)。

| 方法 | 说明 |
|---|---|
| `new GSYSubtitleController(context, subtitleView)` | 绑定 UI |
| `setSources(List<GSYSubtitleSource>)` | 一次注入多轨字幕 |
| `setSource(GSYSubtitleSource)` | 只有一条 |
| `getSources()` | 只读 List |
| `selectSubtitle(String id)` | 按 id 切换字幕，"" 或 null 关闭 |
| `setStyle(GSYSubtitleStyle)` | 字体/描边/背景/颜色 |
| `setOffsetMs(long)` | 音画/字幕 offset |
| `setEnabled(boolean)` | 开关 |
| `updatePosition(long positionMs)` | 由 player 进度驱动 |
| `release()` | 释放 |

## `GSYSubtitleSource` 字段

见 [GSYSubtitleSource](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/subtitle/GSYSubtitleSource.java)：

| 字段 | 说明 |
|---|---|
| `id` | 唯一 id |
| `label` | 显示名 |
| `language` | ISO code |
| `mime` | 见 [GSYSubtitleMime](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/subtitle/GSYSubtitleMime.java)：`text/vtt`、`application/x-subrip`（srt）、`text/plain` |
| `uri` | http 或 file uri |
| `embedded` | 是否内嵌轨（Exo 场景） |

## 内置解析器

- SRT：[GSYSrtSubtitleParser](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/subtitle/GSYSrtSubtitleParser.java)
- WebVTT：[GSYWebVttSubtitleParser](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/subtitle/GSYWebVttSubtitleParser.java)
- 自定义 MIME：注册到 [GSYSubtitleParserFactory](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/subtitle/GSYSubtitleParserFactory.java)

## Exo 内嵌字幕

`gsyvideoplayer-exo_player2` 的 Manager 会把 Exo 的 subtitle track 通过 `GSYSubtitleController` 上抛。Demo：[GSYExoSubTitlePlayer](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/exosubtitle/GSYExoSubTitlePlayer.java) + [GSYExoSubTitleVideoManager](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/exosubtitle/GSYExoSubTitleVideoManager.java)。

## Seek 预览 API

顶层：[GSYVideoPreviewProvider](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/preview/GSYVideoPreviewProvider.java) 与列表实现 [GSYVideoPreviewListProvider](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/preview/GSYVideoPreviewListProvider.java)。

| 方法 | 说明 |
|---|---|
| `GSYVideoPreviewProvider#getPreviewFrame(long positionMs)` | 返 `GSYVideoPreviewFrame`，含 image url + crop 坐标 |
| `GSYVideoPreviewListProvider(List<GSYVideoPreviewFrame>)` | 直接注入帧列表（自己解析或用 VTT） |
| `GSYVideoPreviewVttParser#parse(String vttContent, String baseUrl)` | 解析 WebVTT thumbnails（雪碧图 + 时间段） |
| `GSYVideoPreviewVttParser#parseFrames(String vttContent, String baseUrl)` | 只要帧列表 |

## `GSYVideoPreviewFrame` 字段

见 [GSYVideoPreviewFrame](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/preview/GSYVideoPreviewFrame.java)：`startTimeMs`、`endTimeMs`、`imageUrl`、`cropX/Y/Width/Height`、`hasCrop()`。

## 关键调用序（VTT 预览 + SRT 字幕）

1. 加载 subtitle：`controller.setSource(new GSYSubtitleSource("cn", "中文", "zh", GSYSubtitleMime.SRT, uri, false))`;
2. 播放进度回调（`GSYVideoProgressListener`）中 `controller.updatePosition(current)`；
3. 预览：`GSYVideoPreviewVttParser.parse(vttText, base)` → 得到 Provider → 注入到 `PreViewGSYVideoPlayer#setPreviewProvider(provider)`；
4. seekbar 拖动时 SDK 会自动调 `provider.getPreviewFrame(target)` 并做 crop。

## Demo 对照

- SRT 字幕：[SubtitleDetailPlayer](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/SubtitleDetailPlayer.java)（对应 `raw/demo_subtitle.srt` / `demo_subtitle_vtt.vtt`）
- Exo 内嵌字幕：[GSYExoSubTitleDetailPlayer](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/exosubtitle/GSYExoSubTitleDetailPlayer.java)
- 预览缩略图：[PreViewGSYVideoPlayer](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/video/PreViewGSYVideoPlayer.java) + [PlayPickActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/PlayPickActivity.java)

## 常见坑

- SRT 时间轴带 BOM 或换行不规范会直接被 parser 丢弃；先用 UTF-8 无 BOM 保存。
- VTT 缩略图 URL 相对路径必须传 `baseUrl` 才能拼绝对路径。
- `updatePosition` 必须在 UI 线程或至少与 `subtitleView` 同线程；`GSYSubtitleController` 内部无线程切换。
