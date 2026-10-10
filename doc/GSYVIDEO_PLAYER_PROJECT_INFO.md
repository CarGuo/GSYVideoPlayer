## GSYVideoPlayer项目说明（Beta1）

**[Click to see the English version](GSYVIDEO_PLAYER_PROJECT_INFO_EN.md)**

#### 项目经过多版本调整之后，目前大致结构分为如下 ：

* **Player 播放内核层**：IjkMediaPlayer、ExoPlayr2、MediaPlayer（IPlayerManager）。
* **Cache 缓存层**：ProxyCacheManager、ExoPlayerCacheManager（ICacheManager）。
* **Manager 内核管理层**：GSYVideoManager（GSYVideoBaseManager <- GSYVideoViewBridge）。
* **Video  播放器控件层**：GSYTextureRenderVIew 到 GSYVideoPlayer 五层。
* **Render 渲染控件层**：TextureView、SurfaceView、GLSurfaceView（GSYRenderView <- IGSYRenderView）。
* **扩展能力层**：字幕、预览、截图、GL 效果、清晰度切换等能力优先挂在 UI/Manager/Render 这些可替换层，避免直接污染内核层。

**目前整个video层即是传统controller层，也是大部分时候自定义实现需要继承的层**

#### 结构如下图：

![框架图](https://raw.githubusercontent.com/CarGuo/GSYVideoPlayer/master/img/StructureChart2.jpg)

```

* 管理层GSVideoManager继承GSYVideoBaseManager，通过IPlayerManager控制播放内核。

* 管理层GSVideoManager实现了GSYVideoViewBridge，和UI层交互（主要通过UI层的GSYVideoPlayer）。

* Cache层主要是对缓存的实现和管理，目前有通用的代理缓存，与exo的CacheDataSourceFactory。

* UI层GSYTextureRenderView通过GSYRenderView，内置IGSYRenderView实现类，和渲染层交互。

* UI层逐层继承实现各层逻辑，内部大部分方法为protect。

```

**从这里看出，项目的播放内核、管理器、渲染层都是可以自定义替换的。**

### 近期能力对应的层级

近期新增和调整的播放能力，可以按下面方式理解：

| 能力 | 所在层级 | 说明 |
| --- | --- | --- |
| 通用外挂字幕 | Video/UI 层 | `GSYSubtitleController` 和 `GSYSubtitleView` 根据播放器进度渲染 SRT/WebVTT，加载失败不影响主视频。 |
| WebVTT 进度条预览 | Demo + Preview Provider | `PreViewGSYVideoPlayer` 消费 `GSYVideoPreviewProvider`，缩略图和 sprite 坐标由业务侧或服务端提供。 |
| 截图 | Render + Video 层 | Render 层负责取视频帧，`StandardGSYVideoPlayer` 新增组合截图 API，用于叠加播放器 UI。 |
| GLSurfaceView 效果 | Render 层 | GL renderer 负责滤镜、纹理、截图和 release，Demo 退出时恢复原全局 render type。 |
| 多 URL 清晰度切换 | Video + Manager 层 | `SmartPickVideo` 使用临时 manager 预加载新 URL，seek 同步后提交，失败时回退原播放。 |
| Exo 自适应清晰度 | Exo Manager 层 | HLS master / DASH MPD 走单个媒体时间线，Media3 TrackSelector 自动选轨，固定清晰度使用 TrackSelectionOverride。 |
| 完成后保留最后一帧 | Demo Video 层 | `KeepLastFrameVideo` 只作为业务语义验证，不改变基础播放器默认完成态。 |
| 播放器初始化失败处理 | Manager + Player 层 | `GSYVideoBaseManager` 和各 `IPlayerManager` 将内核创建/初始化异常收敛到错误回调和资源清理。 |
| Exo cache 与 GIF 清理 | Cache + Utils 层 | `ExoSourceManager` 管理 Exo cache 生命周期，`GifCreateHelper` 负责 GIF 生成状态和临时资源清理。 |
| v14 多 pass / LUT / 动态滤镜 | Render 层 | GL 线程管理 FBO、多尺寸 pass、额外纹理与 uTime；Demo 显式启用 GL 并恢复原配置。 |
| v14 折叠屏 | Demo / View / Compose UI 层 | WindowManager 提供姿态；XML 全屏克隆布局、Compose 稳定 Layout 与铰链分隔由 Demo 管理。 |
| v14 会话隔离 | Manager 层 | 播放器实例 + 会话代次约束回调、已排队消息和超时，内核工作留在原工作线程。 |
| v14 FFmpeg / TLS / 音频 | IJK Native 层 | 三 ABI 成套库承载协议、音频时钟/倍速和 TLS 修复；Java Codec2 selector 提供平台解码器选择。 |
| v14 Compose 释放/缓冲/比例 | Controller + View 层 | detach 与 dispose 分离，合并轮询缓冲并公开显示比例刷新 API。 |

更完整的入口、API、回归清单见 [RECENT_FEATURES.md](RECENT_FEATURES.md)。

### 自定义流程

#### 1、通过API实现
项目目前内部主要提供控制API和少量配置API：
[API地址](https://github.com/CarGuo/GSYVideoPlayer/wiki/%E5%9F%BA%E7%A1%80Player-API)。

#### 2、通过继承实现自定义UI
项目目前UI层大部分方法和变量都是protect，虽然就封装性而言这并不是很好，但你可以继承后快捷实现你的自定义。

例如：

* 重写`getLayoutId()`方法，返回你的自定义布局，重用逻辑的控件只要控件Id一致即可。若需要新增控件，可重载`init(Context context)`方法参考源码实现，其中注意如有自定义参数，需要重载`cloneParams`实现大小屏同步，更甚至可以重载`startWindowFullscreen`和`resolveNormalVideoShow`，参考源码和demo，这很简单， 如Demo中： [SampleCoverVideo](https://github.com/CarGuo/GSYVideoPlayer/blob/master/app/src/main/java/com/example/gsyvideoplayer/video/SampleCoverVideo.java)。

* 如Demo中：[EmptyControlVideo](https://github.com/CarGuo/GSYVideoPlayer/blob/master/app/src/main/java/com/example/gsyvideoplayer/video/EmptyControlVideo.java)，重载 `touchSurfaceMoveFullLogic` 和 `touchDoubleUp`，实现了触摸相关的自定义。

* 同样`showWifiDialog`、`showProgressDialog` 、 `showVolumeDialog`等重写实现你的自定义弹窗；onClickUiToggle`、`changeUiTo****`、`OnClick`、`OnTouch`、`touchDoubleUp`等方法重载可自定义手势行为。

#### 3、通过替换实现

如上图所示，前面说过播放内核Player层、Manger层、渲染层都是可以替换的，只要实现了对应的接口，继承后替换对应的实现类，就可以替换对应层的内部实现逻辑。
例如Demo中： [ListMultiVideoActivity](https://github.com/CarGuo/GSYVideoPlayer/blob/master/app/src/main/java/com/example/gsyvideoplayer/ListMultiVideoActivity.java) 、[CustomManager](https://github.com/CarGuo/GSYVideoPlayer/blob/master/app/src/main/java/com/example/gsyvideoplayer/video/manager/CustomManager.java) 、[MultiSampleVideo](https://github.com/CarGuo/GSYVideoPlayer/blob/master/app/src/main/java/com/example/gsyvideoplayer/video/MultiSampleVideo.java)  就演示了如何通过自定义Manager实现，多个播放内核同时播放的效果。



### 4、全屏和非全屏是通过重构 View 来实现的，主要参数有 `cloneParams` \ `resolveFullVideoShow` 、 `startWindowFullscreen` 等方法的实现，另外外部获取记得水涌 player.getCurrentPlayer() 获取区分


### 5、整套的自定义demo

[演示整套自定义的Demo](https://github.com/CarGuo/GSYVideoPlayer/tree/master/app/src/main/java/com/example/gsyvideoplayer/exo)



#### 6、还无法解决(｀・ω・´)，那就提个issue吧！



