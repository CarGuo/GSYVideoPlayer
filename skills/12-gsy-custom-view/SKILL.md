---
name: gsy-custom-view
description: 自定义播放器 UI（继承 StandardGSYVideoPlayer）与自定义控制层：换 seekbar、加弹幕层、加清晰度切换、加投屏按钮等。
when_to_use: SDK 默认 UI 不符合需求；需要在标准控制层上加自定义控件；需要多状态视图（loading/error/paywall）。
inputs:
  - name: layout_id
    description: 自定义 XML 布局资源 id
    required: true
outputs:
  - 一个继承 GSYBaseVideoPlayer 子类的自定义播放器组件。
references:
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/video/StandardGSYVideoPlayer.java
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/video/NormalGSYVideoPlayer.java
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/video/base/GSYBaseVideoPlayer.java
  - file: app/src/main/java/com/example/gsyvideoplayer/video/SampleCoverVideo.java
  - file: app/src/main/java/com/example/gsyvideoplayer/video/SampleControlVideo.java
  - file: app/src/main/java/com/example/gsyvideoplayer/video/LandLayoutVideo.java
---

# gsy-custom-view

## 继承层次

```
GSYTextureRenderView（渲染管理）
  └── GSYVideoView（状态机 / 生命周期）
        └── GSYVideoControlView（默认控制层：进度条/按钮）
              └── GSYVideoPlayer（触摸手势）
                    └── GSYBaseVideoPlayer（全屏/小屏切换）
                          ├── StandardGSYVideoPlayer（默认 UI）
                          └── NormalGSYVideoPlayer（无返回/无标题的简版）
```

自定义组件通常继承 [StandardGSYVideoPlayer](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/video/StandardGSYVideoPlayer.java) 或 [NormalGSYVideoPlayer](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/video/NormalGSYVideoPlayer.java)。

## 必须实现的三件事

### 1) 自定义布局 & id

覆写：
```java
@Override public int getLayoutId() { return R.layout.my_video_player; }
```
布局里必须包含（id 不能改）：
- `@id/surface_container` —— 放渲染 View（FrameLayout）
- 需要用到的默认控件（如 `@id/start`、`@id/progress`、`@id/current`、`@id/total`、`@id/bottom_progressbar`、`@id/thumb`、`@id/small_close`、`@id/back`、`@id/fullscreen`、`@id/loading`）—— 缺哪个哪个功能失效，编译不报错。

### 2) 覆写全屏 & 小屏组件类

关键：`getConstructor(Context, Boolean)` 是 SDK 内部反射拷贝出的全屏实例。**必须提供两个构造器**：

```java
public MyVideoPlayer(Context c) { super(c); }
public MyVideoPlayer(Context c, Boolean fullFlag) { super(c, fullFlag); }
public MyVideoPlayer(Context c, AttributeSet a) { super(c, a); }
```

R8 侧已在 [proguard-rules.pro](file:///D:/workspace/project/GSYVideoPlayer/app/proguard-rules.pro) 保留：
```
-keep class * extends com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer {
    public <init>(android.content.Context);
    public <init>(android.content.Context, java.lang.Boolean);
}
```

### 3) 覆写 `showSmallVideo() / hideSmallVideo()`（如需要小窗）以及 `startWindowFullscreen()`（如要全屏样式差异）

## 常用重写点

| 方法 | 作用 |
|---|---|
| `changeUiToNormal()` / `changeUiToPreparingShow()` / `changeUiToPlayingShow()` / `changeUiToPauseShow()` / `changeUiToCompleteShow()` / `changeUiToError()` / `changeUiToPlayingBufferingShow()` | 不同状态下的 UI 切换钩子 |
| `onClickUiToggle()` | 单击 UI 显隐 |
| `setStateAndUi(int state)` | 由 SDK 内部调，可 super 后加自己的逻辑 |
| `updateStartImage()` | 播放/暂停按钮 icon 切换 |
| `resolveTypeUI()` | 首帧比例决定后的自定义处理 |
| `dismissBrightnessDialog / dismissVolumeDialog / dismissProgressDialog` | 手势弹窗 UI |
| `hideAllWidget / setViewShowState` | 显隐所有控件 |

## 手势自定义

`GSYVideoControlView` 里通过如下开关切换：
- `setIsTouchWiget(boolean)` —— 手势总开关（默认 true）
- `setIsTouchWigetFull(boolean)` —— 只在全屏开
- `setNeedLockFull(boolean)` —— 全屏锁按钮
- `setSeekRatio(float)` —— 手势 seek 灵敏度
- `setThreshold(int)` —— 触发手势阈值

手势弹窗替换：覆写 `showBrightnessDialog / showVolumeDialog / showProgressDialog` 用自己的 Dialog。

## 标准封面 / 清晰度 / 弹幕

- 封面：Demo 组件 [SampleCoverVideo](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/video/SampleCoverVideo.java) 演示用 Glide 加载封面。
- 清晰度切换：Demo 组件 [SwitchVideo](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/video/SwitchVideo.java) + [DetailListPlayer](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/DetailListPlayer.java)（多列源，`setUp(List<GSYVideoModel>, ...)`）。
- 弹幕层：Demo 组件 [DanmakuVideoPlayer](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/video/DanmakuVideoPlayer.java) —— 在布局叠一层 `DanmakuView`，在 `onProgress` 里 `dispatch(long time)`。
- 控制条自定义：Demo 组件 [SampleControlVideo](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/video/SampleControlVideo.java)（毛玻璃底栏 + 播放按钮）。
- 横屏专用布局：Demo 组件 [LandLayoutVideo](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/video/LandLayoutVideo.java)（`getFullId()` 返回不同 layout）。

## 多状态视图（loading / error / paywall）

在 `changeUiToError()` 与 `changeUiToPreparingShow()` 里 `setVisibility` 自定义 View；也可以在 `onVideoAllCallBack.onPlayError` 中显式 `showErrorLayer()`。

## 常见坑

- 忘写 `Boolean` 构造器 → 全屏切换 `IllegalArgumentException` 或直接 crash（反射失败）。
- 自定义 `layout` 缺 id → 默认功能沉默失效，问题不易定位。建议基于 `layout_standard_video_player.xml` 复制修改。
- 全屏组件和非全屏组件类型不同 → 用 `getCurrentPlayer()` 获取"当前活跃 player"，别缓存 `this`。
- 释放时机：`onDestroy` 里既要 `super.onBackPressed` 判断，也要 `GSYVideoManager.releaseAllVideos()`。
