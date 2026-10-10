![](./img/home_logo.png)

**[中文文档](README_CN.md)**

## Supports [IJKPlayer](https://github.com/CarGuo/ijkplayer), [Media3(EXOPlayer2)](https://github.com/androidx/media), MediaPlayer, AliPlayer, implementing a multi-functional video player. (Please read the following instructions carefully, most questions can be answered below).

## * HarmonyOS version [openharmony-tpc/GSYVideoPlayer](https://gitcode.com/openharmony-tpc/openharmony_tpc_samples/tree/master/GSYVideoPlayer)

> ## If cloning is too slow or images are not visible, you can try to synchronize from the following addresses
> - **GitCode** https://gitcode.com/ZuoYueLiang/GSYVideoPlayer
> - **Gitee**：https://gitee.com/CarGuo/GSYVideoPlayer

 Type          | Function
-------------|-------------------------------------------------------------------------------------------------------------------------------------------------
 **Cache**      | **Play while caching, using [AndroidVideoCache](https://github.com/danikula/AndroidVideoCache); Media3(ExoPlayer) uses SimpleCache.**
 **Protocols**      | **h263\4\5, Https, concat, rtsp, hls, rtmp, crypto, mpeg, etc. [ (ijk mode format support) ](https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/DECODERS_EN.md)**
 **Filters**      | **Basic filters, watermarks, multi-screen playback, multi-pass Gaussian/pyramid blur, Bloom, LUT grading, beauty, Glitch, CRT and analog-TV interference. [Details](doc/RECENT_FEATURES_EN.md).**
 **Frame images**      | **Video first frame, video frame screenshots, composed player screenshots including UI, and video to gif function.**
 **Playback**      | **List playback, continuous list playback, gravity rotation and manual rotation, video's own rotation attribute, fast and slow playback, network video loading speed.**
 **Screen**      | **Adjust display ratio: default, 16:9, 4:3, fill; rotate screen angle during playback (0,90,180,270); mirror rotation.**
 **Kernel**      | **IJKPlayer, Media3(EXOPlayer), MediaPlayer, AliPlayer switching, custom kernel**
 **Layout**      | **Full screen and non-full screen two sets of layout switching, pure playback support without any operation controls, barrage function, inherited custom any layout.**
 **Playback**      | **Singleton playback, multiple simultaneous playback, video list sliding automatic playback, seamless playback of list switching detail pages.**
 **Window**      | **Small window, small window playback in multiple windows (including desktop).**
 **Ads**      | **Opening ads, skip ad support, interstitial ad function.**
 **Subtitles**      | **Unified external subtitle overlay supports SRT/WebVTT across IJK, Media3(EXOPlayer), and MediaPlayer; Media3 embedded cues can bridge to the same UI.**
 **Dash**    | **Media3(exo2) mode supports dash; the demo supports HLS master / DASH MPD adaptive quality track switching.**
 **Stream**  | **Supports metadata playback**
 **Adapt 16k**  | **ex_so and arm64/x64 adapt to 16K Page Size**
 **openssl** | **Currently ex_so and arm64/armv7a/x64 all use OpenSSL 3.5.9**
 **FFmpeg**  | **Currently ex_so and arm64/armv7a/x64 (arm64-v8a / armeabi-v7a / x86_64) all use FFmpeg n5.1.10**
 **FFmpeg**  | **Currently ex_so and arm64/armv7a/x64 (arm64-v8a / armeabi-v7a / x86_64) all support G711a(pcm_alaw)**
 **Cast**      | **Optional `gsyvideoplayer-cast` DLNA/UPnP module built on jUPnP 3.0.3; the core keeps only the protocol-neutral `CastCapability` / `CastProvider` / `CastSession` SPI and does not pull Jetty. [Details](doc/CAST_FEATURE_PLAN.md).**
 **More**      | **No black screen when pausing front and back switching; multi-URL quality switching; Exo HLS/DASH adaptive quality; seamless switching support; keep-last-frame demo; WebVTT progress bar preview.**
 **Customization**     | **Customizable rendering layer, custom management layer, custom playback layer (control layer), custom cache layer.**
 **Foldables** | **XML / Compose FLAT, BOOK and TABLETOP split/fullscreen demos using FoldingFeature hinge information.**

[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.carguo/gsyvideoplayer)](https://central.sonatype.com/artifact/io.github.carguo/gsyvideoplayer)
[![](https://jitpack.io/v/CarGuo/GSYVideoPlayer.svg)](https://jitpack.io/#CarGuo/GSYVideoPlayer)
[![Build Status](https://app.travis-ci.com/CarGuo/GSYVideoPlayer.svg?branch=master)](https://app.travis-ci.com/CarGuo/GSYVideoPlayer)
[![Github Actions](https://github.com/CarGuo/GSYVideoPlayer/workflows/CI/badge.svg)](https://github.com/CarGuo/GSYVideoPlayer/actions)

[]()
[![GitHub stars](https://img.shields.io/github/stars/CarGuo/GSYVideoPlayer.svg)](https://github.com/CarGuo/GSYVideoPlayer/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/CarGuo/GSYVideoPlayer.svg)](https://github.com/CarGuo/GSYVideoPlayer/network)
[![GitHub issues](https://img.shields.io/github/issues/CarGuo/GSYVideoPlayer.svg)](https://github.com/CarGuo/GSYVideoPlayer/issues)
[![GitHub License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://github.com/CarGuo/GSYVideoPlayer/blob/master/LICENSE)

[]()
[![Star](https://gitcode.com/ZuoYueLiang/GSYVideoPlayer/star/badge.svg)](https://gitcode.com/ZuoYueLiang/GSYVideoPlayer/overview)
![](https://img.shields.io/github/v/release/androidx/media?label=media)

| Official Account | Juejin                                                          | Zhihu                                        | CSDN                                    | Jianshu
|---------|-------------------------------------------------------------|-------------------------------------------|-----------------------------------------|----------------------------------------------|
| GSYTech | [Click me](https://juejin.im/user/582aca2ba22b9d006b59ae68/posts) | [Click me](https://www.zhihu.com/people/carguo) | [Click me](https://blog.csdn.net/ZuoYueLiang) | [Click me](https://www.jianshu.com/u/6e613846e1ea)

![](http://img.cdn.guoshuyu.cn/WeChat-Code)

### [--------------Demo APK Download Address---------------](https://github.com/CarGuo/GSYVideoPlayer/releases)

## I. Using Dependencies

There are currently three hosting methods:

- MavenCentral: Available after version 11.0.0, all base class packages are published and hosted here.
- Github Package: Available from version 9.1.0, but before version 11.0.0, the basic dependencies of GSYIjkJava are still hosted on jitpack.
- Jitpack IO: Will continue to be released, but there is a random loss of packages on the hosting platform.


#### [--- Version Update Instructions --- ](https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/UPDATE_VERSION.md).

#### [--- Recent Playback Features --- ](https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/RECENT_FEATURES_EN.md).

### ABI and dependency selection in v14.0.0

The `gsyvideoplayer` aggregate already includes Java, Exo2 and `ex_so`. For modular IJK setup, choose `ex_so` or the required standalone ABI modules; do not include two native sets for the same ABI.

| Native artifact | ABI | Native libraries in this release |
| --- | --- | --- |
| `gsyvideoplayer-ex_so` | `arm64-v8a` / `armeabi-v7a` / `x86_64` | FFmpeg n5.1.10 + OpenSSL 3.5.9 |
| `gsyvideoplayer-arm64` | `arm64-v8a` | Identical to the matching `ex_so` tuple |
| `gsyvideoplayer-armv7a` | `armeabi-v7a` | Identical to the matching `ex_so` tuple |
| `gsyvideoplayer-x64` | `x86_64` | Identical to the matching `ex_so` tuple |
| `gsyvideoplayer-armv5` / `gsyvideoplayer-x86` | Legacy `armeabi` / `x86` | Historical libraries, not upgraded to FFmpeg 5; explicit legacy use only |

Migrated standalone modules and `ex_so` share codec/protocol configuration. Choose by ABI and package size, rather than the former standard/extended MPEG distinction. Add `compose`, `cast` and `aliplay` only as needed; `cast` requires API 26, while the core requires API 23. See [DECODERS_EN.md](doc/DECODERS_EN.md) and [BUILD_SO_EN.md](doc/BUILD_SO_EN.md).

### 1. MavenCentral Reference (Recommended)

Since jitpack keeps losing packages, it has been migrated to MavenCentral. The usage is as follows:

#### First Add

```groovy
allprojects {
    repositories {
        ///...
        mavenCentral()
        maven { url "https://maven.aliyun.com/repository/public" }
    }
}
```

**You can choose one of the following three and add it to the build.gradle under the module.**

#### A. Direct Introduction

```groovy
 //Complete version introduction

implementation 'io.github.carguo:gsyvideoplayer:14.0.0'


//Whether AliPlayer mode is needed
implementation 'io.github.carguo:gsyvideoplayer-aliplay:14.0.0'

//Whether DLNA/UPnP casting is needed (optional, minSdk 26)
implementation 'io.github.carguo:gsyvideoplayer-cast:14.0.0'
```

#### B. Add java and the so support you want:

```groovy
 implementation 'io.github.carguo:gsyvideoplayer-java:14.0.0'

 //Whether ExoPlayer mode is needed
 implementation 'io.github.carguo:gsyvideoplayer-exo2:14.0.0'

 //Optional direct RTMP module; exo2 already exposes it transitively
 implementation 'io.github.carguo:gsyvideoplayer-rtmp:14.0.0'

 //Optional DLNA/UPnP cast implementation (minSdk 26); the default player does not include Jetty
 implementation 'io.github.carguo:gsyvideoplayer-cast:14.0.0'

 //Whether AliPlayer mode is needed
 implementation 'io.github.carguo:gsyvideoplayer-aliplay:14.0.0'

 //Select required ABIs; do not also add ex_so
 implementation 'io.github.carguo:gsyvideoplayer-arm64:14.0.0'
 implementation 'io.github.carguo:gsyvideoplayer-armv7a:14.0.0'
 implementation 'io.github.carguo:gsyvideoplayer-x64:14.0.0'
```

#### C. Modular setup with all three migrated ABIs

`ex_so` bundles all three migrated ABIs with the same codec configuration as the standalone modules in B. Select B when only a subset of ABIs is needed.

```groovy
 implementation 'io.github.carguo:gsyvideoplayer-java:14.0.0'

 //Whether ExoPlayer mode is needed
 implementation 'io.github.carguo:gsyvideoplayer-exo2:14.0.0'

 //Whether AliPlayer mode is needed
 implementation 'io.github.carguo:gsyvideoplayer-aliplay:14.0.0'

 //IJK native libraries for the three migrated ABIs
 implementation 'io.github.carguo:gsyvideoplayer-ex_so:14.0.0'

```

#### D. Jetpack Compose Support (Optional)

The `gsyvideoplayer-compose` module is published with v13.1.0. It can be consumed from Maven Central / GitHub Packages like the other modules, or by depending on the source module directly with `implementation project(":gsyVideoPlayer-compose")` when working inside this repository.
>
> 🛠 **Toolchain note:** the Compose module is verified on **JDK 21 in CI** (`.github/workflows/*.yml` — `actions/setup-java` `java-version: 21`) and **JDK 17 locally** (the module pins `sourceCompatibility / targetCompatibility / jvmTarget = 17` in [gsyVideoPlayer-compose/build.gradle](gsyVideoPlayer-compose/build.gradle)). Both are fine; just make sure your local JDK is **≥ 17** so Kotlin 2.0.21 + AGP 8.9.1 can build.

The new `gsyvideoplayer-compose` module exposes Compose entries on top of the existing kernels and UI without touching any legacy code:

- **Wrapper mode**: a single Composable `GSYVideoPlayerView { ... }` embeds `StandardGSYVideoPlayer` into a Compose screen, with automatic Lifecycle bridge and `release` on dispose. An optional `setUpKey: Any?` parameter lets you trigger `setUp` again only when the data identity changes (idempotent).
- **Native mode**: `GSYComposeHostPlayer + GSYPlayerController` exposes a `GSYPlayerSnapshot` state stream **plus** an `events: SharedFlow<GSYPlayerEvent>` of one-shot edge events (`Prepared` / `AutoComplete` / `Error`), so the control UI can be drawn entirely in Compose while the rendering pipeline still uses the GSY multi-kernel core. The legacy `setOnError / setOnComplete / setOnPrepared` setters are still supported but `@Deprecated` in favour of the Flow API.

```groovy
// Maven Central:
implementation 'io.github.carguo:gsyvideoplayer-compose:14.0.0'

// Source dependency for local development:
implementation project(':gsyVideoPlayer-compose')
// compose-bom is api-exposed from the module; consumers still manage androidx.compose.* per their own project setup.
```

See [doc/COMPOSE_USE.md](doc/COMPOSE_USE.md). The sample app provides a `Compose Demo` entry with **25 runnable Compose Activities** — covering Wrapper basics, Native detail/list/multi-window/auto-play/seamless-switch, plus P5 differentiating capabilities (filter, cache/download, ad pre-roll, subtitle, custom danmaku, EXO multi-source, parallel multi-window) and P5-2 modern app patterns (vertical short video, floating window, multi-type list, web mixed layout, audio-only, URL/local file, MediaCodec hardware switch, themed custom controls), plus the v14 foldable BOOK/TABLETOP demo. `DemoSamples.kt` is a shared `data object` of test URLs, not a runnable Activity. Outstanding gaps and the rolling roadmap are tracked in [doc/COMPOSE_BACKLOG.md](doc/COMPOSE_BACKLOG.md).

### 2. Github Package Dependency Method (Recommended)

**Since Jitpack often has the problem of random loss of historical packages, a new Github Package dependency method is added. The usage is as follows**:

> However, accessing github package requires a token to access, which is more troublesome, but it is stable.

```groovy
allprojects {
    repositories {
		//...
        maven {
            url 'https://maven.pkg.github.com/CarGuo/GSYVideoPlayer'

            // You can also use your own GitHub account and token
            // For convenience, I have provided a token for an infrequently used account here
            credentials {
                // your github name
                username = 'carsmallguo'
                // your github generate new token
                password = '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'
            }
        }
        maven {
            url "https://maven.aliyun.com/repository/public"
        }
        mavenCentral()

    }
}
```

- To generate your own token, you can see: https://docs.github.com/zh/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens

> In theory, it is the avatar in the upper right corner - Settings - Developer Settings - Personal access tokens - tokens (classic) -
> Generate new token (classic) - read:packages
> Remember to choose permanent for the expiration time

> Tip: this repository's root `build.gradle` already supports reading the GitHub Packages credentials from a Gradle property or environment variable, so you don't have to hard-code your own token in the source tree:
>
> ```properties
> # ~/.gradle/gradle.properties (recommended for local builds)
> githubReadUser=<your-github-name>
> githubReadToken=<your-classic-token-with-read:packages>
> ```
>
> Or in CI:
>
> ```bash
> export GITHUB_READ_USER=<your-github-name>
> export GITHUB_READ_TOKEN=<your-classic-token-with-read:packages>
> ```
>
> The hard-coded `carsmallguo / ghp_...` pair is only kept as a fallback so first-time clones still build out of the box; it may be revoked at any time, so prefer providing your own.

**You can choose one of the following three and add it to the build.gradle under the module.**

#### A. Direct Introduction

```groovy
 //Complete version introduction
 implementation 'com.shuyu:gsyvideoplayer:14.0.0'


 //Whether AliPlayer mode is needed
 implementation 'com.shuyu:gsyvideoplayer-aliplay:14.0.0'

 //Whether DLNA/UPnP casting is needed (optional, minSdk 26)
 implementation 'com.shuyu:gsyvideoplayer-cast:14.0.0'
```

#### B. Add java and the so support you want:

```groovy
 implementation 'com.shuyu:gsyvideoplayer-java:14.0.0'

 //Whether ExoPlayer mode is needed
 implementation 'com.shuyu:gsyvideoplayer-exo2:14.0.0'

 //Optional direct RTMP module; exo2 already exposes it transitively
 implementation 'com.shuyu:gsyvideoplayer-rtmp:14.0.0'

 //Optional DLNA/UPnP cast implementation (minSdk 26); the default player does not include Jetty
 implementation 'com.shuyu:gsyvideoplayer-cast:14.0.0'

 //Whether AliPlayer mode is needed
 implementation 'com.shuyu:gsyvideoplayer-aliplay:14.0.0'

 //Select required ABIs; do not also add ex_so
 implementation 'com.shuyu:gsyvideoplayer-armv7a:14.0.0'
 implementation 'com.shuyu:gsyvideoplayer-arm64:14.0.0'
 implementation 'com.shuyu:gsyvideoplayer-x64:14.0.0'
```

#### C. Modular setup with all three migrated ABIs

`ex_so` bundles all three migrated ABIs with the same codec configuration as the standalone modules in B. Select B when only a subset of ABIs is needed.

```groovy
 implementation 'com.shuyu:gsyvideoplayer-java:14.0.0'

 //Whether ExoPlayer mode is needed
 implementation 'com.shuyu:gsyvideoplayer-exo2:14.0.0'


 //Whether AliPlayer mode is needed
 implementation 'com.shuyu:gsyvideoplayer-aliplay:14.0.0'

 //IJK native libraries for the three migrated ABIs
 implementation 'com.shuyu:gsyvideoplayer-ex_so:14.0.0'

```

#### D. Jetpack Compose Support (Optional)

```groovy
 implementation 'com.shuyu:gsyvideoplayer-compose:14.0.0'
```

### 3. Jitpack Introduction Method (will continue to be released, but not highly recommended)

Historical packages may have random packet loss, and it is not easy to supplement, see [#4144](https://github.com/CarGuo/GSYVideoPlayer/issues/4144):

#### First, add in the build.gradle under the project

```groovy
allprojects {
    repositories {
		//...
        maven { url 'https://jitpack.io' }
        maven { url "https://maven.aliyun.com/repository/public" }
        mavenCentral()
    }
}
```

**You can choose one of the following three and add it to the build.gradle under the module.**

#### A. Direct Introduction

```groovy
 //Complete version introduction

 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer:v14.0.0'


 //Whether AliPlayer mode is needed
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-aliplay:v14.0.0'

 //Whether DLNA/UPnP casting is needed (optional, minSdk 26)
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-cast:v14.0.0'
```

#### B. Add java and the so support you want:

```groovy
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-java:v14.0.0'

 //Whether ExoPlayer mode is needed
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-exo2:v14.0.0'

 //Optional DLNA/UPnP cast implementation (minSdk 26)
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-cast:v14.0.0'

 //Whether AliPlayer mode is needed
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-aliplay:v14.0.0'

 //Select required ABIs; do not also add ex_so
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-arm64:v14.0.0'
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-armv7a:v14.0.0'
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-x64:v14.0.0'
```

#### C. Modular setup with all three migrated ABIs

`ex_so` bundles all three migrated ABIs with the same codec configuration as the standalone modules in B. Select B when only a subset of ABIs is needed.

```groovy
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-java:v14.0.0'

 //Whether ExoPlayer mode is needed
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-exo2:v14.0.0'

 //Whether AliPlayer mode is needed
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-aliplay:v14.0.0'

 //IJK native libraries for the three migrated ABIs
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-ex_so:v14.0.0'

```

----------------------------------------------------------

#### Global switching support in code (for more, please refer to the documentation and demo below)

```

//EXOPlayer kernel, supports more formats
PlayerFactory.setPlayManager(Exo2PlayerManager.class);
//System kernel mode
PlayerFactory.setPlayManager(SystemPlayerManager.class);
//ijk kernel, default mode
PlayerFactory.setPlayManager(IjkPlayerManager.class);
//aliplay kernel, default mode
PlayerFactory.setPlayManager(AliPlayerManager.class);


//exo cache mode, supports m3u8, only supports exo
//Set before Exo cache is created. Default is 512 MB.
ExoSourceManager.setCacheMaxSize(1024L * 1024L * 1024L);
CacheFactory.setCacheManager(ExoPlayerCacheManager.class);
//Proxy cache mode, supports all modes, does not support m3u8, etc., default
CacheFactory.setCacheManager(ProxyCacheManager.class);



//Switch rendering mode
GSYVideoType.setShowType(GSYVideoType.SCREEN_MATCH_FULL);
//Default display ratio
GSYVideoType.SCREEN_TYPE_DEFAULT = 0;
//16:9
GSYVideoType.SCREEN_TYPE_16_9 = 1;
//4:3
GSYVideoType.SCREEN_TYPE_4_3 = 2;
//Full screen cropping display, for normal display CoverImageView it is recommended to use FrameLayout as the parent layout
GSYVideoType.SCREEN_TYPE_FULL = 4;
//Full screen stretching display, when using this attribute, it is recommended to use FrameLayout for surface_container
GSYVideoType.SCREEN_MATCH_FULL = -4;
/***
 * Custom display ratio under SCREEN_TYPE_CUSTOM
 * @param screenScaleRatio Aspect ratio, such as 16:9
 */
public static void setScreenScaleRatio(float screenScaleRatio)


//Switch drawing mode
GSYVideoType.setRenderType(GSYVideoType.SUFRACE);
GSYVideoType.setRenderType(GSYVideoType.GLSURFACE);
GSYVideoType.setRenderType(GSYVideoType.TEXTURE);


//ijk close log
IjkPlayerManager.setLogLevel(IjkMediaPlayer.IJK_LOG_SILENT);


//exoplayer custom MediaSource
ExoSourceManager.setExoMediaSourceInterceptListener(new ExoMediaSourceInterceptListener() {
    @Override
    public MediaSource getMediaSource(String dataSource, boolean preview, boolean cacheEnable, boolean isLooping, File cacheDir) {
        //Customizable MediaSource
        return null;
    }
});

```

### [--- More dependency methods, please click - ](https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/DEPENDENCIES.md)

## II. Other Recommendations

###     * My technical community: [Juejin](https://juejin.cn/user/817692379985752/posts)

###     * QQ group, welcome if interested (usually a lot of chitchat and complaints, because the number of people is saturated, it's just daily nonsense, no one solves problems): ~~

174815284~~ , New group: 992451658 .

###     * [Flutter Github Client](https://github.com/CarGuo/gsy_github_app_flutter) , [Compose Github Client](https://github.com/CarGuo/GSYGithubAppCompose) , [React Native Github Client](https://github.com/CarGuo/GSYGithubAPP) , [Weex Github Client](https://github.com/CarGuo/GSYGithubAPPWeex) , [Native Kotlin Github Client](https://github.com/CarGuo/GSYGithubAPPKotlin)

###     * [RxFFmpeg Android audio and video editing tool](https://github.com/microshow/RxFFmpeg)

###     * [oarplayer Rtmp player, based on MediaCodec and srs-librtmp, does not rely on ffmpeg](https://github.com/qingkouwei/oarplayer)

###     * HarmonyOS version [openharmony-tpc/GSYVideoPlayer](https://gitcode.com/openharmony-tpc/openharmony_tpc_samples/tree/master/GSYVideoPlayer)

## III. Documentation Wiki

 Document            | Portal
---------------|----------------------------------------------------------------------------------------------------------------------------------------------------
 **Usage Instructions**      | ***[--- Simple usage, quick start documentation](https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/USE.md)***
 **Recommended Reading**      | ***[--- Basic audio and video knowledge that mobile developers must know 1](https://juejin.cn/post/7057132141875822622), [--- Basic audio and video knowledge that mobile developers must know 2](https://mp.weixin.qq.com/s/HjSdmAsHuvixCH_EWdvk3Q)***
 **Project Analysis Description**    | ***[--- Project analysis description, including project architecture and analysis](https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/GSYVIDEO_PLAYER_PROJECT_INFO.md)***
 API Documentation Entrance        | **[--- Usage instructions, API documentation - Entrance](https://github.com/CarGuo/GSYVideoPlayer/wiki)**
 **FAQ Entrance**    | ***[--- FAQ - Entrance (most of the problems you encounter are solved here) ](https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/QUESTION.md)***
 Encoding Format          | **[--- IJK so file configuration format description](https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/DECODERS_EN.md)**
 Compile Custom SO       | **[--- IJKPlayer Compile Custom SO - Entrance](https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/BUILD_SO.md)**
 Version Update Instructions        | **[--- Version Update Instructions - Entrance](https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/UPDATE_VERSION.md)**
 compileSdk too high | --- **[#3514](https://github.com/CarGuo/GSYVideoPlayer/issues/3514)**

![Framework diagram](./img/StructureChart2.jpg)
![Framework diagram2](./img/StructureChart3.jpg)
![Framework diagram3](./img/p1.png)
![Framework diagram4](./img/p4.png)
![Framework diagram5](./img/p2.png)

> More visible: https://codewiki.google/github.com/carguo/gsyvideoplayer

## IV. Running Effect

* ### 1. Open a playback (rotation, mirror, fill)

<img src="./img/11.gif" width="240px" height="426px"/>

* ### 2. List/Detail Mode (animation, rotation, small window)

<div>
<img src="./img/22.gif" width="240px" height="426px"/>
<img src="./img/33.gif" width="240px" height="426px"/>
<img src="./img/44.gif" width="240px" height="426px"/>
</div>

* ### 3. Barrage

<img src="./img/55.gif" width="240px" height="426px"/>

* ### 4. Filters and GL animation

<img src="./img/09.gif"/>

* ### 6. Background filled with blur playback

<img src="./img/99.png" width="426px" height="240px"/>

* ### 7. Progress bar small window preview

<img src="./img/07.gif" height="240px"/>

The demo now uses a WebVTT thumbnail track for seek preview instead of extracting many frames from the original video on the client. The VTT can point to separate images or sprite coordinates:

```text
WEBVTT

00:00:00.000 --> 00:00:01.000
160p-00001.jpg#xywh=0,0,284,160
```

Library APIs include `GSYVideoPreviewVttParser`, `GSYVideoPreviewProvider`, and `GSYVideoPreviewFrame`. The app layer loads the frame image and crops the sprite area if needed. See `PreViewGSYVideoPlayer#setPreviewVttUrl(String previewVttUrl)`.

## V. Recent Versions

### v14.0.0 (2026-10-10)

- **IJK upgrade**: unify `ex_so` and arm64/armv7a/x64 on **FFmpeg n5.1.10 + OpenSSL 3.5.9**, with matched FFmpeg/player/SDL tuples and MJPEG / HEVC seek/replay repairs.
- **TLS fixes**: fix ARMv7 HTTPS certificate-loading crashes and preserve explicit CA, peer verification and proxy settings across nested HLS requests. Final ARMv7 / ARM64 each pass CA loading and 15 playback checks on the originally affected device.
- **Audio and RTSP**: support 0.25x–4x software tempo; repair audio drain, startup and EOF/error states; add initial RTSP redirects, opt-in live queue bounds and session callback/watchdog isolation. `timeout` is in microseconds.
- **GL pipeline and effects**: add FBO multi-pass rendering, downsampling pyramids, Gaussian blur, Bloom, LUT grading, edge-preserving beauty, Glitch, CRT and analog-TV interference, plus `uTime` and texture-asset lifecycle support.
- **Foldables**: add XML / Compose FLAT, BOOK and TABLETOP split demos with hinge, rotation, fullscreen and posture injection handling; increase runnable Compose demos to **25**.
- **Compose**: fix #4259 host-detach resource release and #4261 buffering updates; add the dynamic aspect-ratio API and improve IJK/EXO cache switching and buffering demos.
- **R8 and tooling**: narrow reflection/JNI keep rules; use Gradle **8.12**, AGP **8.9.1**, R8 **9.4.14**; add **13 integration skills** and session/Compose CI regressions.
- **ABI migration**: the default aggregate/`ex_so` ships `arm64-v8a` / `armeabi-v7a` / `x86_64`. Legacy `armeabi` / `x86` require explicit standalone legacy modules and are not upgraded to FFmpeg 5.

See the [full v14.0.0 changelog](doc/UPDATE_VERSION_EN.md#v1400-2026-10-10) for features, migration and verification scope, and the [release audit](doc/V14_RELEASE_REVIEW.md) for all **27 commits** and source/artifact mapping. Current x86_64 / 16 KiB-page runtime, hardware-decoded pixels and the complete acoustic gate remain unqualified.

### v13.2.1 (2026-08-19)

- Move `JupnpDlnaProvider`, `JupnpDlnaSession`, jUPnP, and Jetty into the independently published optional `gsyvideoplayer-cast` module.
- Keep the protocol-neutral Cast SPI in `gsyvideoplayer-java`; default `gsyvideoplayer` and `gsyvideoplayer-java` consumers no longer resolve jUPnP/Jetty and retain the Media3-defined `minSdk 23` floor.
- The optional cast artifact declares its real `minSdk 26` requirement and contributes its jUPnP service, network/multicast permissions, and reflection-safe R8 consumer rules.
- Fix transient `STOPPED` handling during DLNA startup and make explicit disconnect stop the receiver before restoring local playback once.

### v13.2.0 (2026-08-19)

- Add the independently publishable `gsyvideoplayer-rtmp` module with four ABI binaries rebuilt by NDK r22b for 16 KB pages; `gsyvideoplayer-exo2` exposes it transitively.
- Update AliPlayer to 7.5.0, its first release with official 16 KB page-size support.
- Add first-class DLNA/UPnP cast capability inside `gsyVideoPlayer-java`: `CastCapability` / `CastProvider` / `CastSession` / `CastListener` SPI are the stable public contract, and the default `JupnpDlnaProvider` / `JupnpDlnaSession` implementation speaks DLNA `AVTransport:1` on top of jUPnP 3.0.3.
- `CastMediaInfo` now carries an immutable `startPositionMs` field. The `SetAVTransportURI → Play → Seek` chain guarantees "casting mid-playback resumes at the same position remotely" and disconnect returns the local player to the last known remote position.
- `SampleCastControlVideo` collapses into a remote-control overlay while casting — the local surface and audio are released, and a clean resume path restores local playback on disconnect. `CastDemoActivity` provides the DLNA device picker plus a Loopback Receiver toggle.
- Ships an on-device Loopback Receiver (`DevReceiverService` in `:dlna` process + `LoopbackAvTransportService` + `CastReceiverFloatingWindow`) for end-to-end smoke tests without a real TV. Cross-process state is bridged with `sendBroadcast` + `setPackage` private intents.
- All strings live in `res/values{,-zh-rCN}/strings.xml`. Android 13+ receivers use `RECEIVER_NOT_EXPORTED` and the notification/foreground-service permission model is honoured.

### v13.1.0 (2026-06-30)

- Publish the `gsyvideoplayer-compose` artifact and document Maven Central / GitHub Packages coordinates.
- Add the Compose module with Wrapper and Native modes plus 24 runnable Compose demo activities.
- Add the smart MediaCodec fallback demo for hardware decode failure downgrade to software decode.
- Improve auto-play demo lifecycle handling and Exo cache lifecycle / max-size configuration.
- Add Java and Compose regression playbooks and align release documentation for the v13.1.0 publishing flow.

### v13.0.0 (2026-05-07)

- Add Exo HLS master / DASH MPD adaptive quality demo and recent playback feature guides.
- Add unified SRT/WebVTT external subtitle support across IJK, System, and Media3.
- Add WebVTT seek preview, keep-last-frame demo, player screenshots, and GL effect improvements.
- Improve multi-URL quality switching, player core error handling, Exo cache lifecycle, and GIF cleanup.
- Fix subtitle loader release/resume during detach, fullscreen, and small-window transitions.
- Fall back to non-cache playback when the Exo cache folder is locked, and avoid stale hadCached state.
- Fix SurfaceView screenshot bitmap cleanup and stale preview VTT async provider overwrite.

### v12.1.0 (2026-04-01)

- update media3 1.10.0
- ex_so: fix ex_so (x86_64) https #4238

### v12.0.0 (2026-03-13)

- ex_so: update ffmpeg 4.3
- ex_so: x86_64 support 16k page size
- ex_so: armv7a update ffmpeg n4.3
- ex_so: #4224 add common-page-size for 64-bit linker flags for Stack Canary
- fix #4225 ff_hevc_sao_band_filter_neon_8+100)
- #4178 exo player rtmp fix 16k
- #4226 Fix NPE in Kotlin implementations of ExoMediaSourceInterceptListener
- #4228 Fix fullscreen player requiring two clicks to resume from paused state
- Update mediaVersion to 1.9.2
- fix #4218 support export exo cache
- Update minSdk to 23 and media version to 1.9

### v11.3.0 (2025-12-05)
- link #3019
- fix #4211


### v11.2.0 (2025-11-25)
- fix #4169
- fix #4174
- fix #4171
- add new function with clearVideoSurface [IjkExo2MediaPlayer]
- fix #4199
- fix #4204

### v11.1.0 (2025-08-04)

- update media3 1.8.0

### v11.0.0 (2025-07-10)

- Update and migrate underlying dependencies
- fix #4140


### More versions, please check: [Version Update Instructions](https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/UPDATE_VERSION_EN.md)

## VI. About Issues

```
Before asking questions, please refer to the documents and instructions above, and reproduce the problem in the Demo.

Problem description:

1. Which page in which Demo.
2. Problem manifestation and reproduction steps.
3. Supplementary video stream url and screenshots of the problem.
4. Supplementary model and Android version of the problem.
```

## VII. Obfuscation

v14.0.0 keeps JNI and reflection entry points rather than entire GSY/Media3 packages. Preserve custom fullscreen/small-window constructors and no-arg player/cache manager constructors. See the [FAQ](doc/QUESTION_EN.md) and [R8 report](doc/R8_ANALYZER_REPORT.md).

```proguard
# ijk JNI: native side reflects back into Java, keep the whole package
-keep class tv.danmaku.ijk.media.player.** { *; }
-dontwarn tv.danmaku.ijk.media.player.**

# ---- Three reflection hotspots inside GSYVideoPlayer, MUST keep ----

# 1) Fullscreen / small-window: GSYBaseVideoPlayer re-instantiates itself via
#    getConstructor(Context[, Boolean]).newInstance()
-keep class * extends com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer {
    public <init>(android.content.Context);
    public <init>(android.content.Context, java.lang.Boolean);
}

# 2) PlayerFactory instantiates IPlayerManager implementations via Class.newInstance()
-keep class * implements com.shuyu.gsyvideoplayer.player.IPlayerManager {
    public <init>();
}
-keep interface com.shuyu.gsyvideoplayer.player.IPlayerManager { *; }

# 3) CacheFactory instantiates ICacheManager implementations via Class.newInstance()
-keep class * implements com.shuyu.gsyvideoplayer.cache.ICacheManager {
    public <init>();
}
-keep interface com.shuyu.gsyvideoplayer.cache.ICacheManager { *; }

# Media3 aar already ships its own consumer-rules, just silence warnings here
-dontwarn androidx.media3.**
-dontwarn com.google.android.exoplayer2.**
```

If it is an Alibaba Cloud player, you can refer to its documentation ( https://help.aliyun.com/document_detail/124711.html?spm=a2c4g.124711.0.0.7fa0125dkwUPoU
), you need to add some keep rules:

```
-keep class com.alivc.**{*;}
-keep class com.aliyun.**{*;}
-keep class com.cicada.**{*;}
-dontwarn com.alivc.**
-dontwarn com.aliyun.**
-dontwarn com.cicada.**
```

## Warm Reminder

#### [If cloning is too slow, you can try downloading from Gitee](https://gitee.com/CarGuo/GSYVideoPlayer)

```
Regarding customization and problems, please refer to the FAQ, demo, and issues first.

Learn more about basic audio and video common sense, and understand containers, audio and video encoding, ffmpeg, and the differences in mediacodec.
Try to avoid asking why others can play.

The player is highly customizable. For customization, please refer to the demo and read the source code. There are many functions now, and the demo is constantly being updated.

Some new functions and project structures are also constantly being adjusted.

Welcome to ask questions, thank you.

```

## Dependency Size Reference

It is recommended to use ndk filtering, please refer to [Reference 4: 4. NDK so support](http://www.jianshu.com/p/86e4b336c17d)
![](https://ooo.0o0.ooo/2017/06/15/5941f343a39f5.png)

## Star History Chart

![Star History Chart](https://star-history-eight.vercel.app/api/svg??repos=CarGuo/GSYVideoPlayer&type=Date)

## Warm Reminder

Open source projects mainly provide communication and learning, do not provide technical support, and do not accept business cooperation, purely public interest open source

## License

```
Please refer to the IJKPlayer and AndroidVideoCache related agreements.
The project started from jiecao, and was refactored after some changes.
Occasionally, some variable and method names may still have a shadow of jiaozi, but it is basically a new project.
```
