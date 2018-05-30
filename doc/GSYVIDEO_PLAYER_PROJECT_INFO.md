## GSYVideoPlayer项目说明（Beta1）

#### 项目经过多版本调整之后，目前大致结构分为如下 ：

* **Player 播放内核层**：IjkMediaPlayer、ExoPlayr2、MediaPlayer（IPlayerManager）。
* **Cache 缓存层**：ProxyCacheManager、ExoPlayerCacheManager（ICacheManager）。
* **Manager 内核管理层**：GSYVideoManager（GSYVideoBaseManager <- GSYVideoViewBridge）。
* **Video  播放器控件层**：GSYTextureRenderVIew 到 GSYVideoPlayer 五层。
* **Render 渲染控件层**：TextureView、SurfaceView、GLSurfaceView（GSYRenderView <- IGSYRenderView）。

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


### 4、整套的自定义demo

[演示整套自定义的Demo](https://github.com/CarGuo/GSYVideoPlayer/tree/master/app/src/main/java/com/example/gsyvideoplayer/exo)


#### 5、还无法解决(｀・ω・´)，那就提个issue吧！

