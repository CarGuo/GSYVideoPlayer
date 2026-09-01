---
name: gsy-fullscreen-orientation
description: 处理进入/退出全屏、屏幕旋转、竖屏视频自适应、系统旋转设置联动、反向横屏。
when_to_use: 出现"全屏没进/没退"、"横屏方向反了"、"旋转跟随系统坏了"、"竖屏视频被横过来"、"抖动无限循环"这类问题；或首次在详情页接入全屏。
inputs:
  - name: activity
    description: 承载 player 的 Activity
    required: true
  - name: player
    description: GSYBaseVideoPlayer 或其子类
    required: true
outputs:
  - OrientationUtils 已绑定；点击"全屏"按钮可以正确进入 window fullscreen 副本；返回键与旋转都能正确复位。
references:
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/utils/OrientationUtils.java
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/utils/OrientationOption.java
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/GSYBaseActivityDetail.java
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/video/base/GSYBaseVideoPlayer.java
---

# gsy-fullscreen-orientation

## 概念

- **视觉全屏**：`player.startWindowFullscreen(context, hideActionBar, hideStatusBar)`，会用反射克隆一个同类 player 塞进 `android.R.id.content` 顶部，返回原实例。
- **屏幕方向**：`OrientationUtils` 监听 `OrientationEventListener` + `Settings.System.ACCELEROMETER_ROTATION`，负责旋转 Activity 方向。
- **两者可解耦**：只想旋转不切副本 → `setNeedOrientationUtils(false)` + 自建 `OrientationUtils`；只想切副本不旋转 → 不构造 `OrientationUtils`，`player` 保持默认 `setRotateViewAuto(false)`。

## 核心 API

| 类 / 方法 | 位置 | 说明 |
|---|---|---|
| `OrientationUtils(Activity, GSYBaseVideoPlayer)` | [OrientationUtils#L50-L52](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/utils/OrientationUtils.java#L50-L52) | 主构造 |
| `OrientationUtils(Activity, player, OrientationOption)` | [OrientationUtils#L54-L64](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/utils/OrientationUtils.java#L54-L64) | 支持自定义旋转角度阈值 |
| `setEnable(boolean)` | `OrientationUtils` | 使能/失能自动旋转 |
| `setRotateWithSystem(boolean)` | `OrientationUtils` | 是否跟随系统"旋转开关" |
| `setIsOnlyRotateLand(boolean)` | `OrientationUtils` | true 时仅在横屏方向变化时响应 |
| `resolveByClick()` | `OrientationUtils` | 用户点全屏按钮时手动触发一次旋转 |
| `backToProtVideo()` | `OrientationUtils` | 返回键触发时把方向复位竖屏 |
| `setIsPause(boolean)` | `OrientationUtils` | 生命周期 pause/resume 桥接 |
| `releaseListener()` | `OrientationUtils` | `onDestroy` 释放监听 |
| `startWindowFullscreen(Context, boolean, boolean)` | [GSYBaseVideoPlayer](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/video/base/GSYBaseVideoPlayer.java) | 进入 window 全屏，返回克隆体 |
| `GSYVideoManager.backFromWindowFull(Context)` | [GSYVideoManager#L82-L94](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/GSYVideoManager.java#L82-L94) | 返回键统一处理 |
| `GSYVideoManager.isFullState(Activity)` | [GSYVideoManager#L143-L151](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/GSYVideoManager.java#L143-L151) | 当前是否全屏 |

## `OrientationOption` 参数

| 字段 | 默认 | 作用 |
|---|---|---|
| `normalPortraitAngleStart / End` | 5 / 355 | 判定竖屏区间 |
| `normalLandAngleStart / End` | 85 / 95 | 判定正向横屏区间 |
| `reverseLandAngleStart / End` | 265 / 275 | 判定反向横屏区间 |

区间越大越"灵敏"，太大会抖动。

## 关键调用序（详情页最佳实践）

1. `onCreate`：`orientationUtils = new OrientationUtils(this, player)`; `orientationUtils.setEnable(false)`（进页面不自动旋转）
2. 全屏按钮点击：`orientationUtils.resolveByClick()` → `player.startWindowFullscreen(this, true, true)`
3. `onBackPressed`：`orientationUtils.backToProtVideo()` → `GSYVideoManager.backFromWindowFull(this)`
4. `onPause`：`orientationUtils.setIsPause(true)`
5. `onResume`：`orientationUtils.setIsPause(false)`
6. `onDestroy`：`orientationUtils.releaseListener()`

`GSYBaseActivityDetail` 已把 1/2/3/4/5/6 全套代码写好，直接继承最省事。

## Demo 对照

- 最标准的全屏 + 旋转：[DetailPlayer](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/DetailPlayer.java)
- 只旋转不切副本：[LandLayoutVideo](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/video/LandLayoutVideo.java) + [DetailNormalActivityPlayer](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/DetailNormalActivityPlayer.java)
- 反射克隆入口：[GSYBaseVideoPlayer](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/video/base/GSYBaseVideoPlayer.java)（`startWindowFullscreen` → `getConstructor(Context.class, Boolean.class)`）

## 常见坑

- **黑屏一瞬**：`startWindowFullscreen` 用反射构造新 player，若你的子类只暴露 `(Context)` 构造且未加 `(Context, Boolean)`，R8 混淆后必炸。ProGuard 里必须保留：`-keep class * extends GSYBaseVideoPlayer { public <init>(android.content.Context); public <init>(android.content.Context, java.lang.Boolean); }`
- **旋转停不下来**：忘了 `orientationUtils.setIsPause(true)` 或 `releaseListener()`。
- **系统关了自动旋转但还想手动横屏**：`setRotateWithSystem(false)`。
- **竖屏视频被强制横屏**：`setAutoFullWithSize(true)` 让 SDK 自适应。
