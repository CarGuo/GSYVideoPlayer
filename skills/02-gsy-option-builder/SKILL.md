---
name: gsy-option-builder
description: 用 GSYVideoOptionBuilder 一次性把播放器所有可调项配好；等价于对 StandardGSYVideoPlayer 挨个 setter 但更紧凑。
when_to_use: 需要开启循环 / 变速 / 边播边缓 / 触摸手势 / 自动旋转 / 封面 / 底部进度条样式 / 全屏动画 等多个开关时；或在多处播放页复用同一份配置。
inputs:
  - name: player
    description: StandardGSYVideoPlayer / GSYBaseVideoPlayer 实例
    required: true
outputs:
  - 已按 builder 状态 build 完成的 player，可直接 startPlayLogic
references:
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/GSYBaseActivityDetail.java
  - file: app/src/main/java/com/example/gsyvideoplayer/DetailPlayer.java
---

# gsy-option-builder

## API 总览

入口：[GSYVideoOptionBuilder](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java)。

链式调用，最后 `build(player)` 把状态灌到具体 `GSYBaseVideoPlayer`。

```java
new GSYVideoOptionBuilder()
    .setUrl(url)
    .setCacheWithPlay(true)
    .setVideoTitle(title)
    .setIsTouchWiget(true)
    .setRotateViewAuto(false)
    .setLockLand(false)
    .setLooping(false)
    .setSpeed(1.0f)
    .setSeekOnStart(0)
    .setVideoAllCallBack(callback)
    .build(player);
```

## 完整参数表（按 [源文件行号](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) 对齐）

| Setter | 类型 | 默认 | 用途 |
|---|---|---|---|
| `setUrl(String)` [#L376](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | String | null | 播放地址 |
| `setVideoTitle(String)` [#L386](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | String | null | 顶部标题 |
| `setMapHeadData(Map)` [#L437](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | `Map<String,String>` | null | HTTP header |
| `setOverrideExtension(String)` [#L561](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | String | null | 强制 Exo 用哪种解封装器（`m3u8`/`mpd`/`ism`） |
| `setCacheWithPlay(boolean)` [#L396](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | boolean | false | 边播边缓 |
| `setCachePath(File)` [#L427](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | File | null | 缓存目录 |
| `setLooping(boolean)` [#L202](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | boolean | false | 循环 |
| `setSpeed(float)` [#L237](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | float | 1.0 | 播放速率，0.25~3.0 常用 |
| `setSoundTouch(boolean)` [#L246](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | boolean | false | 变速不变调 |
| `setSeekOnStart(long)` [#L366](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | long ms | -1 | 起播 seek 位置 |
| `setSeekRatio(float)` [#L324](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | float | 1.0 | 触摸横滑与进度的比例 |
| `setStartAfterPrepared(boolean)` [#L406](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | boolean | true | 首帧就绪后自动播；`false` 用于"手动 start" |
| `setReleaseWhenLossAudio(boolean)` [#L417](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | boolean | true | 丢失音频焦点是否 release |
| `setIsTouchWiget(boolean)` [#L263](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | boolean | true | 非全屏是否响应触摸手势 |
| `setIsTouchWigetFull(boolean)` [#L272](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | boolean | true | 全屏是否响应触摸手势 |
| `setDismissControlTime(int ms)` [#L543](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | int | 2500 | 控制条自动隐藏时间 |
| `setNeedShowWifiTip(boolean)` [#L281](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | boolean | true | 移动网络提示 |
| `setRotateViewAuto(boolean)` [#L221](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | boolean | true | 自动旋转全屏；关闭后配合 `OrientationUtils` |
| `setRotateWithSystem(boolean)` [#L337](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | boolean | true | 是否跟随系统旋转设置 |
| `setLockLand(boolean)` [#L229](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | boolean | false | 全屏是否锁横屏 |
| `setAutoFullWithSize(boolean)` [#L184](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | boolean | false | 竖屏视频自动竖屏全屏 |
| `setOnlyRotateLand(boolean)` [#L567](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | boolean | false | 只允许横屏方向变化 |
| `setShowFullAnimation(boolean)` [#L194](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | boolean | true | 全屏过渡动画 |
| `setHideKey(boolean)` [#L254](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | boolean | true | 全屏隐藏虚拟按键 |
| `setFullHideActionBar(boolean)` [#L587](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | boolean | false | 全屏隐藏 ActionBar |
| `setFullHideStatusBar(boolean)` [#L592](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | boolean | false | 全屏隐藏 StatusBar |
| `setNeedLockFull(boolean)` [#L525](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | boolean | false | 全屏显示屏幕锁按钮 |
| `setLockClickListener(LockClickListener)` [#L533](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | 接口 | null | 屏幕锁点击回调 |
| `setEnlargeImageRes(int)` [#L291](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | drawableRes | -1 | 全屏按钮图标 |
| `setShrinkImageRes(int)` [#L301](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | drawableRes | -1 | 退出全屏按钮图标 |
| `setThumbImageView(View)` [#L464](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | View | null | 封面容器（`ImageView` 或子布局） |
| `setThumbPlay(boolean)` [#L516](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | boolean | false | 点击封面即播 |
| `setShowPauseCover(boolean)` [#L314](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | boolean | true | 暂停时显示最后一帧 |
| `setBottomProgressBarDrawable(Drawable)` [#L482](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | Drawable | null | 底部长条进度条颜色 |
| `setBottomShowProgressBarDrawable(Drawable, Drawable)` [#L473](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | 2×Drawable | null | 交互进度条 & thumb |
| `setDialogProgressBar(Drawable)` [#L499](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | Drawable | null | 触摸拖动进度条样式 |
| `setDialogVolumeProgressBar(Drawable)` [#L490](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | Drawable | null | 触摸音量进度条 |
| `setDialogProgressColor(int, int)` [#L507](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | int color | -11 | 触摸进度条高亮/普通颜色 |
| `setShowDragProgressTextOnSeekBar(boolean)` [#L572](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | boolean | false | 拖动时 seekbar 上气泡 |
| `setEffectFilter(GSYVideoGLView.ShaderInterface)` [#L551](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | Shader | `NoEffect` | GL 滤镜（渲染必须 `GLSURFACE`） |
| `setPlayTag(String)` [#L347](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | String | "" | 列表播放去重用 |
| `setPlayPosition(int)` [#L356](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | int | -22 | 列表位置索引 |
| `setSetUpLazy(boolean)` [#L582](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | boolean | false | 延迟 setUp，`build` 后手动 `startPlayLogic` |
| `setVideoAllCallBack(VideoAllCallBack)` [#L213](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | 接口 | null | 全事件回调（prepared/complete/error…） |
| `setGSYVideoProgressListener(GSYVideoProgressListener)` [#L455](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | 接口 | null | 高频进度回调 |
| `setGSYStateUiListener(GSYStateUiListener)` [#L600](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | 接口 | null | UI 状态变化回调 |
| `setSurfaceErrorPlay(boolean)` [#L446](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | boolean | true | Surface 异常时点击继续 |
| `setNeedOrientationUtils(boolean)` [#L611](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/builder/GSYVideoOptionBuilder.java) | boolean | true | 全屏是否内部构造 `OrientationUtils` |

## 关键调用序

1. 构造 `GSYVideoOptionBuilder` → 链式 setter；
2. `builder.build(player)` 会自动 setUp（除非 `setSetUpLazy(true)`）；
3. 需要在页面里同时接收回调 → 让 Activity `implements VideoAllCallBack`，直接 `setVideoAllCallBack(this)`。

## Demo 对照

- 详情页 + Builder 全套：[DetailPlayer](../../app/src/main/java/com/example/gsyvideoplayer/DetailPlayer.java)
- 结合 `GSYBaseActivityDetail` 的最短接入：[DetailControlActivity](../../app/src/main/java/com/example/gsyvideoplayer/DetailControlActivity.java) 与 [GSYBaseActivityDetail#L56-L61](../../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/GSYBaseActivityDetail.java)

## 常见坑

- `build(player)` 内部会调 `setUp`；若 `setSetUpLazy(true)` 需要自己再调 `startPlayLogic()`。
- Effect 滤镜需要先 `GSYVideoType.setRenderType(GLSURFACE)`，否则 `setEffectFilter` 无效。
- `setPlayTag` 与 `setPlayPosition` 是列表页避免"上下滑滚 setUp 命中同 URL 不重播"的关键。
