---
name: gsy-render-and-effects
description: 三种渲染载体（TextureView / SurfaceView / GLSurfaceView）、显示比例（16:9/4:3/full/custom）、GL 滤镜与自定义 Shader。
when_to_use: 想放竖屏视频、需要 GL 滤镜、要做全屏裁减/拉伸、SurfaceView 与动画冲突、想抓截图或 gif。
inputs:
  - name: render_type
    description: TEXTURE / SURFACE / GLSURFACE
    required: true
  - name: show_type
    description: 显示比例枚举
    required: false
outputs:
  - 渲染载体 + 显示比例 + 滤镜均按需生效。
references:
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/utils/GSYVideoType.java
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/render/GSYRenderView.java
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/render/view/GSYVideoGLView.java
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/render/effect
  - file: app/src/main/java/com/example/gsyvideoplayer/DetailFilterActivity.java
---

# gsy-render-and-effects

## 渲染类型 `GSYVideoType.setRenderType(int)`

| 常量 | 值 | 说明 |
|---|---|---|
| `TEXTURE` | 0 | 默认，与动画/圆角/缩放兼容最好 |
| `SURFACE` | 1 | 性能最好，但和 View 动画冲突 |
| `GLSURFACE` | 2 | 必需，若要用 `setEffectFilter` |

见 [GSYVideoType#L39-L49](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/utils/GSYVideoType.java#L39-L49)。设置时机：`setUp` 之前，全局生效。

## 显示比例 `GSYVideoType.setShowType(int)`

| 常量 | 值 | 说明 |
|---|---|---|
| `SCREEN_TYPE_DEFAULT` | 0 | 按视频原比例居中 |
| `SCREEN_TYPE_16_9` | 1 | 强制 16:9 |
| `SCREEN_TYPE_4_3` | 2 | 强制 4:3 |
| `SCREEN_TYPE_18_9` | 6 | 全面屏 |
| `SCREEN_TYPE_FULL` | 4 | 全屏裁减；`surface_container` 建议 `FrameLayout` |
| `SCREEN_MATCH_FULL` | -4 | 全屏拉伸；`surface_container` 建议 `FrameLayout` |
| `SCREEN_TYPE_CUSTOM` | -5 | 需先 `setScreenScaleRatio(float)` |

见 [GSYVideoType#L10-L29](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/utils/GSYVideoType.java#L10-L29) 与 [GSYVideoType#L165-L167](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/utils/GSYVideoType.java#L165-L167)。

## 硬解码开关

| 静态方法 | 说明 |
|---|---|
| `enableMediaCodec()` / `disableMediaCodec()` | IJK 硬解码总开关 [#L71-L79](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/utils/GSYVideoType.java#L71-L79) |
| `enableMediaCodecTexture()` / `disableMediaCodecTexture()` | 硬解 + Texture 直渲染优化 [#L100-L108](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/utils/GSYVideoType.java#L100-L108) |
| `enableSmartMediaCodec()` / `disableSmartMediaCodec()` | 硬解失败自动软解回退 [#L86-L94](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/utils/GSYVideoType.java#L86-L94) |
| `isMediaCodec()` / `isMediaCodecTexture()` / `isSmartMediaCodec()` | 查询 |

设置时机：**`setUp` 之前**，且**部分机型**需要在 `Application.onCreate` 就配好。

## GL 滤镜

需要满足两个条件：
1. `GSYVideoType.setRenderType(GSYVideoType.GLSURFACE)`
2. `builder.setEffectFilter(new XxxEffect())` 或 `player.setEffectFilter(...)`

SDK 内置滤镜（在 `com.shuyu.gsyvideoplayer.render.effect.*`）：`AutoFixEffect`、`BarrelBlurEffect`、`BlackAndWhiteEffect`、`BrightnessEffect`、`ContrastEffect`、`CrossProcessEffect`、`DocumentaryEffect`、`DuotoneEffect`、`FillLightEffect`、`GammaEffect`、`GaussianBlurEffect`、`GrainEffect`、`GreyScaleEffect`、`HueEffect`、`InvertColorsEffect`、`LamoishEffect`、`NoEffect`、`OverlayEffect`、`PosterizeEffect`、`SampleBlurEffect`、`SaturationEffect`、`SepiaEffect`、`SharpnessEffect`、`TemperatureEffect`、`TintEffect`、`VignetteEffect`。目录：[render/effect](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/render/effect)。

自定义 Shader：实现 [`GSYVideoGLView.ShaderInterface`](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/render/view/GSYVideoGLView.java)。Demo 里 4 个复杂样例：[GSYVideoGLViewCustomRender](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/effect/GSYVideoGLViewCustomRender.java) / 2 / 3 / 4，以及像素化 [PixelationEffect](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/effect/PixelationEffect.java)、BitmapOverlay [BitmapEffect](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/effect/BitmapEffect.java) / [BitmapIconEffect](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/effect/BitmapIconEffect.java)。

## 抓帧 / gif / 截图

在 [GSYRenderView](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/render/GSYRenderView.java)：

| 方法 | 回调 | 说明 |
|---|---|---|
| `taskShotPic(GSYVideoShotListener)` | 返 Bitmap | 单帧截图 |
| `saveFrame(File, GSYVideoShotSaveListener)` | 保存到磁盘 | 落盘截图 |
| `taskGifPic(...)` + `GifCreateHelper` | 生成 gif | 见 [GifCreateHelper](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/utils/GifCreateHelper.java) |

## Demo 对照

- 滤镜集合：[DetailFilterActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/DetailFilterActivity.java) + [DetailFilterComposeActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/DetailFilterComposeActivity.kt)
- 透明视频：[SampleTransparentVideo](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/video/SampleTransparentVideo.java) + [DetailTransparentActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/DetailTransparentActivity.java)
- 自定义 Render：[CustomRenderVideoPlayer](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/video/CustomRenderVideoPlayer.java)

## 常见坑

- `SurfaceView` 在 `RecyclerView` 里滑动会撕裂 / 覆盖 → 列表用 `TEXTURE`。
- `GLSURFACE` 在低端机会漏帧，静默切 `TEXTURE` 更稳。
- 竖屏视频强制 `SCREEN_TYPE_FULL` 会大幅裁减；建议 `SCREEN_TYPE_DEFAULT` + `setAutoFullWithSize(true)`。
