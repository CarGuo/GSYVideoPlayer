---
name: gsy-list-playback
description: RecyclerView / ViewPager2 / ViewPager 列表滑动自动播放，滚动切换、复用绑定、进入详情页时"漂移"到全屏。
when_to_use: 短视频信息流、朋友圈式内嵌播放、抖音/快手类 ViewPager2 竖播、列表进入详情页的无缝续播。
inputs:
  - name: adapter
    description: RecyclerView.Adapter / FragmentStateAdapter
    required: true
outputs:
  - 列表条目具备"进入可视区自动播放，离开释放"的能力，且切换详情页可续播。
references:
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/utils/ListVideoUtil.java
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/utils/GSYVideoHelper.java
  - file: app/src/main/java/com/example/gsyvideoplayer/utils/ScrollCalculatorHelper.java
  - file: app/src/main/java/com/example/gsyvideoplayer/AutoPlayRecyclerViewActivity.java
  - file: app/src/main/java/com/example/gsyvideoplayer/ViewPager2Activity.java
  - file: app/src/main/java/com/example/gsyvideoplayer/RecyclerViewActivity.java
---

# gsy-list-playback

## 三条实现路线

| 路线 | 特点 | 适用 |
|---|---|---|
| **A. 每 item 内嵌 `StandardGSYVideoPlayer`** | 每条 item 有独立 player 实例，滑到即播 | 大多数场景（含无限缩略页面） |
| **B. `ListVideoUtil` 单例复用** | 全列表共享一个 player 实例，动态 `attachToParent` | 内存敏感、条目多；不能同屏两个视频 |
| **C. `GSYVideoHelper` 悬浮小窗** | 用于详情页返回列表时"漂浮"续播 | 抖音式详情返回 |

## 路线 A：每 item 一个 player

关键点：**在 `onBindViewHolder` 里给每个 player `setPlayPosition(position)` + `setPlayTag(String)`**，防止滚动复用导致 URL 命中错人。

`GSYVideoOptionBuilder`：
- `setPlayTag(String)` [#L347](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java#L347)
- `setPlayPosition(int)` [#L356](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java#L356)

滚动出屏自动释放：`RecyclerView.OnScrollListener` → 当前 firstVisible / lastVisible 之外的 holder 调 `GSYVideoManager.releaseAllVideos()`。

见 [RecyclerViewActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/RecyclerViewActivity.java)。

## 路线 B：`ListVideoUtil`（跨 item 复用一个 player）

工具类：[ListVideoUtil](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/utils/ListVideoUtil.java)。

| API | 说明 |
|---|---|
| `ListVideoUtil(Context)` | 单例创建（在 Activity/Fragment 里持一份） |
| `addVideoPlayer(int position, ImageView thumb, String tag, ViewGroup container, View playBtn)` | 把 player 移到 `container`；`tag` 与 `position` 用于识别是否是新目标 |
| `setPlayPositionAndTag(int, String)` | 手动更新目标 |
| `startPlay(String url)` | 开始播放 |
| `isCurrentViewPlaying(int pos, String tag)` | 判定当前播放是否在这个 item |
| `releaseVideoPlayer()` | 释放，通常在离开页面时调 |

**自动可视区触发**用 [ScrollCalculatorHelper](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/utils/ScrollCalculatorHelper.java) 的 `onScrollReleaseAllVideos(...) / onScrollPlayVideo(...)`。见 [AutoPlayRecyclerViewActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/AutoPlayRecyclerViewActivity.java)。

## 路线 C：`GSYVideoHelper` 小窗漂移

工具类：[GSYVideoHelper](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/utils/GSYVideoHelper.java)。核心思路：**列表 item / 详情页共用同一个 player 实例**，在离开列表时把 player parent 换到浮层容器，进入详情再换回去。

| API | 说明 |
|---|---|
| `GSYVideoHelper(Context, GSYBaseVideoPlayer)` | 携一份 player |
| `setSmall(int width, int height, int gravity, int marginX, int marginY)` | 小窗尺寸 |
| `showSmallVideo(...)` / `smallVideoToNormal()` | 小窗 ↔ 正常 |
| `releaseVideoPlayer()` | 释放 |

Demo：[SmallVideoHelper](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/utils/SmallVideoHelper.java)（app 层包装） + [ListVideoActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/ListVideoActivity.java)。

## ViewPager2 竖播

- 用 `FragmentStateAdapter`，每个 Fragment 内嵌 `StandardGSYVideoPlayer` 或 `NormalGSYVideoPlayer`；
- `ViewPager2.registerOnPageChangeCallback` 里 `onPageSelected` → `previousPlayer.onVideoPause(); currentPlayer.startPlayLogic()`；
- 记得 `setUserInputEnabled` 竖向。

Demo：[ViewPager2Activity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/ViewPager2Activity.java)（RecyclerView 变体） + Compose 版 [VerticalShortVideoComposeActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/VerticalShortVideoComposeActivity.kt)。

## 常见坑

- **相同 URL 不重播**：忘了 `setPlayTag/setPlayPosition`，`setUp` 内部去重返回 false。
- **切换过快出黑帧**：`setUp` 后立即 `startPlayLogic` 前先隐藏 `thumb`；或用 `KeepLastFrameVideo` 保留上一帧（详见 [doc/KEEP_LAST_FRAME.md](file:///D:/workspace/project/GSYVideoPlayer/doc/KEEP_LAST_FRAME.md)）。
- **同屏多个 item 各自播放**：单例 `GSYVideoManager` 只能有一个 listener；同屏并行需要参考 [CustomManager](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/video/manager/CustomManager.java)。
