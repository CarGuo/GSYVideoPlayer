## GSYVideoPlayer Project Description (Beta1)

**[Click to see the Chinese version](GSYVIDEO_PLAYER_PROJECT_INFO.md)**

#### After multiple versions of adjustments, the project's current structure is roughly divided as follows:

* **Player core layer**: IjkMediaPlayer, ExoPlayr2, MediaPlayer (IPlayerManager).
* **Cache layer**: ProxyCacheManager, ExoPlayerCacheManager (ICacheManager).
* **Manager core management layer**: GSYVideoManager (GSYVideoBaseManager <- GSYVideoViewBridge).
* **Video player control layer**: GSYTextureRenderVIew to GSYVideoPlayer five layers.
* **Render rendering control layer**: TextureView, SurfaceView, GLSurfaceView (GSYRenderView <- IGSYRenderView).
* **Extension capability layer**: subtitles, preview, screenshots, GL effects, and quality switching are kept in replaceable UI/Manager/Render layers whenever possible, instead of being pushed into the playback core.

**Currently, the entire video layer is the traditional controller layer, and it is also the layer that needs to be inherited for custom implementation most of the time.**

#### The structure is as follows:

![Framework diagram](https://raw.githubusercontent.com/CarGuo/GSYVideoPlayer/master/img/StructureChart2.jpg)

```

* The management layer GSVideoManager inherits GSYVideoBaseManager and controls the playback core through IPlayerManager.

* The management layer GSVideoManager implements GSYVideoViewBridge and interacts with the UI layer (mainly through the GSYVideoPlayer of the UI layer).

* The Cache layer is mainly for the implementation and management of caching. Currently, there are general proxy caching and exo's CacheDataSourceFactory.

* The UI layer GSYTextureRenderView interacts with the rendering layer through GSYRenderView, which has a built-in IGSYRenderView implementation class.

* The UI layer inherits layer by layer to implement the logic of each layer, and most of the internal methods are protect.

```

**From this, it can be seen that the project's playback core, manager, and rendering layer can all be customized and replaced.**

### Recent Features By Layer

Recent playback changes map to the existing architecture like this:

| Feature | Layer | Notes |
| --- | --- | --- |
| Unified external subtitles | Video/UI layer | `GSYSubtitleController` and `GSYSubtitleView` render SRT/WebVTT by playback position; failures do not affect video playback. |
| WebVTT seek preview | Demo + preview provider | `PreViewGSYVideoPlayer` consumes `GSYVideoPreviewProvider`; thumbnail files and sprite coordinates come from the app or server side. |
| Screenshots | Render + Video layer | Render views capture the video frame; `StandardGSYVideoPlayer` adds composed screenshot APIs that include player UI. |
| GLSurfaceView effects | Render layer | GL renderers handle filters, textures, screenshots, and release; the demo restores the previous global render type on exit. |
| Multi-URL quality switching | Video + Manager layer | `SmartPickVideo` preloads the target URL with a temporary manager, syncs seek position, commits when ready, and falls back on failure. |
| Exo adaptive quality | Exo Manager layer | HLS master / DASH MPD use one media timeline; Media3 TrackSelector handles auto track selection, and fixed quality uses TrackSelectionOverride. |
| Keep last frame | Demo Video layer | `KeepLastFrameVideo` validates the business behavior without changing the base player's default completion state. |
| Player init failure handling | Manager + Player layer | `GSYVideoBaseManager` and each `IPlayerManager` route core creation/init exceptions into error callbacks and resource cleanup. |
| Exo cache and GIF cleanup | Cache + Utils layer | `ExoSourceManager` manages the Exo cache lifecycle, while `GifCreateHelper` cleans GIF generation state and temporary resources. |
| v14 multi-pass / LUT / animated effects | Render | GL thread owns FBOs, multi-size passes, extra textures and uTime; demos select/restore GL explicitly. |
| v14 foldables | Demo / View / Compose UI | WindowManager supplies posture; demos own fullscreen clone layouts, stable Compose Layout and hinge separation. |
| v14 session isolation | Manager | Player identity and session generation constrain callbacks, queued events and watchdogs; kernel work stays on existing workers. |
| v14 FFmpeg / TLS / audio | IJK Native | Matched three-ABI tuples implement protocol/audio/TLS repairs; Java Codec2 selection uses platform capabilities. |
| v14 Compose release/buffer/ratio | Controller + View | Separate detach/dispose, merge polled buffering and expose ratio refresh. |

See [RECENT_FEATURES_EN.md](RECENT_FEATURES_EN.md) for entry points, APIs, and regression checks.

### Customization Process

#### 1. Implementation through API
The project currently mainly provides control APIs and a small number of configuration APIs:
[API address](https://github.com/CarGuo/GSYVideoPlayer/wiki/%E5%9F%BA%E7%A1%80Player-API).

#### 2. Implement custom UI by inheritance
Most of the methods and variables in the UI layer of the project are currently protect. Although this is not very good in terms of encapsulation, you can quickly implement your customization after inheritance.

For example:

* Override the `getLayoutId()` method to return your custom layout. The controls of the reused logic can be consistent as long as the control IDs are consistent. If you need to add new controls, you can override the `init(Context context)` method to refer to the source code for implementation. Note that if there are custom parameters, you need to override `cloneParams` to achieve synchronization of large and small screens. You can even override `startWindowFullscreen` and `resolveNormalVideoShow`. It's very simple to refer to the source code and demo, such as in the Demo: [SampleCoverVideo](https://github.com/CarGuo/GSYVideoPlayer/blob/master/app/src/main/java/com/example/gsyvideoplayer/video/SampleCoverVideo.java).

* As in the Demo: [EmptyControlVideo](https://github.com/CarGuo/GSYVideoPlayer/blob/master/app/src/main/java/com/example/gsyvideoplayer/video/EmptyControlVideo.java), `touchSurfaceMoveFullLogic` and `touchDoubleUp` are overridden to implement touch-related customization.

* Similarly, `showWifiDialog`, `showProgressDialog`, `showVolumeDialog`, etc. can be overridden to implement your custom pop-up windows; `onClickUiToggle`, `changeUiTo****`, `OnClick`, `OnTouch`, `touchDoubleUp` and other methods can be overridden to customize gesture behavior.

#### 3. Implementation by replacement

As shown in the figure above, the Player layer, Manger layer, and rendering layer of the playback core can all be replaced. As long as the corresponding interface is implemented and the corresponding implementation class is replaced after inheritance, the internal implementation logic of the corresponding layer can be replaced.
For example, in the Demo: [ListMultiVideoActivity](https://github.com/CarGuo/GSYVideoPlayer/blob/master/app/src/main/java/com/example/gsyvideoplayer/ListMultiVideoActivity.java), [CustomManager](https://github.com/CarGuo/GSYVideoPlayer/blob/master/app/src/main/java/com/example/gsyvideoplayer/video/manager/CustomManager.java), [MultiSampleVideo](https://github.com/CarGuo/GSYVideoPlayer/blob/master/app/src/main/java/com/example/gsyvideoplayer/video/MultiSampleVideo.java) demonstrate how to implement the effect of multiple playback cores playing at the same time through a custom Manager.



### 4. Full screen and non-full screen are implemented by refactoring the View. The main parameters are the implementation of methods such as `cloneParams`, `resolveFullVideoShow`, `startWindowFullscreen`, etc. In addition, remember to use player.getCurrentPlayer() to distinguish when getting it externally.


### 5. A complete set of custom demos

[Demo demonstrating a complete set of customizations](https://github.com/CarGuo/GSYVideoPlayer/tree/master/app/src/main/java/com/example/gsyvideoplayer/exo)



#### 6. Still can't solve it (｀・ω・´), then file an issue!
