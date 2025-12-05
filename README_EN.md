![](./img/home_logo.png)

## Supports [IJKPlayer](https://github.com/Bilibili/ijkplayer), [Media3(EXOPlayer2)](https://github.com/androidx/media), MediaPlayer, AliPlayer, implementing a multi-functional video player. (Please read the following instructions carefully, most questions can be answered below).

## * HarmonyOS version [openharmony-tpc/GSYVideoPlayer](https://gitcode.com/openharmony-tpc/openharmony_tpc_samples/tree/master/GSYVideoPlayer)

> ## If cloning is too slow or images are not visible, you can try to synchronize from the following addresses
> - **GitCode** https://gitcode.com/ZuoYueLiang/GSYVideoPlayer
> - **Gitee**：https://gitee.com/CarGuo/GSYVideoPlayer

 Type          | Function
-------------|-------------------------------------------------------------------------------------------------------------------------------------------------
 **Cache**      | **Play while caching, using [AndroidVideoCache](https://github.com/danikula/AndroidVideoCache); Media3(ExoPlayer) uses SimpleCache.**
 **Protocols**      | **h263\4\5, Https, concat, rtsp, hls, rtmp, crypto, mpeg, etc. [ (ijk mode format support) ](https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/DECODERS.md)**
 **Filters**      | **Simple filters (mosaic, black and white, color filter, Gaussian, blur, etc. more than 20 kinds), animation, (watermark, multi-screen playback, etc.).**
 **Frame images**      | **Video first frame, video frame screenshot function, video to gif function.**
 **Playback**      | **List playback, continuous list playback, gravity rotation and manual rotation, video's own rotation attribute, fast and slow playback, network video loading speed.**
 **Screen**      | **Adjust display ratio: default, 16:9, 4:3, fill; rotate screen angle during playback (0,90,180,270); mirror rotation.**
 **Kernel**      | **IJKPlayer, Media3(EXOPlayer), MediaPlayer, AliPlayer switching, custom kernel**
 **Layout**      | **Full screen and non-full screen two sets of layout switching, pure playback support without any operation controls, barrage function, inherited custom any layout.**
 **Playback**      | **Singleton playback, multiple simultaneous playback, video list sliding automatic playback, seamless playback of list switching detail pages.**
 **Window**      | **Small window, small window playback in multiple windows (including desktop).**
 **Ads**      | **Opening ads, skip ad support, interstitial ad function.**
 **Subtitles**      | **[Media3(exo2) mode supports custom external subtitles](https://github.com/CarGuo/GSYVideoPlayer/tree/master/app/src/main/java/com/example/gsyvideoplayer/exosubtitle).**
 **Dash**    | **Media3(exo2) mode supports dash**
 **Stream**  | **Supports metadata playback**
 **Adapt 16k**  | **ex_so adapts to 16K Page Size**
 **openssl** | **Currently arm64 uses openssl 1.1.1w**
 **FFmpeg**  | **Currently arm64 uses FFmpeg 4.1.6**
 **FFmpeg**  | **Currently ex_so's arm64 supports G711a(pcm_alaw)**
 **More**      | **No black screen when pausing front and back switching; support for adjusting different definitions; seamless switching support; lock/unlock full screen click function; progress bar small window preview (test).**
 **Customization**     | **Customizable rendering layer, custom management layer, custom playback layer (control layer), custom cache layer.**

[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.carguo/gsyvideoplayer)](https://central.sonatype.com/artifact/io.github.carguo/gsyvideoplayer)
[![](https://jitpack.io/v/CarGuo/GSYVideoPlayer.svg)](https://jitpack.io/#CarGuo/GSYVideoPlayer)
[![Build Status](https://app.travis-ci.com/CarGuo/GSYVideoPlayer.svg?branch=master)](https://app.travis-ci.com/CarGuo/GSYVideoPlayer)
[![Github Actions](https://github.com/CarGuo/GSYVideoPlayer/workflows/CI/badge.svg)](https://github.com/CarGuo/GSYVideoPlayer/actions)

[]()
[![GitHub stars](https://img.shields.io/github/stars/CarGuo/GSYVideoPlayer.svg)](https://github.com/CarGuo/GSYVideoPlayer/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/CarGuo/GSYVideoPlayer.svg)](https://github.com/CarGuo/GSYVideoPlayer/network)
[![GitHub issues](https://img.shields.io/github/issues/CarGuo/GSYVideoPlayer.svg)](https://github.com/CarGuo/GSYVideoPlayer/issues)
[![GitHub license](https://img.shields.io/github/license/CarGuo/GSYVideoPlayer/blob/master/LICENSE)](https://github.com/CarGuo/GSYVideoPlayer/blob/master/LICENSE)

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

implementation 'io.github.carguo:gsyvideoplayer:11.3.0'


//Whether AliPlayer mode is needed
implementation 'io.github.carguo:gsyvideoplayer-aliplay:11.3.0'
```

#### B. Add java and the so support you want:

```groovy
 implementation 'io.github.carguo:gsyvideoplayer-java:11.3.0'

 //Whether ExoPlayer mode is needed
 implementation 'io.github.carguo:gsyvideoplayer-exo2:11.3.0'

 //Whether AliPlayer mode is needed
 implementation 'io.github.carguo:gsyvideoplayer-aliplay:11.3.0'

 //so of ijk mode according to your needs
 implementation 'io.github.carguo:gsyvideoplayer-arm64:11.3.0'
 implementation 'io.github.carguo:gsyvideoplayer-armv7a:11.3.0'
 implementation 'io.github.carguo:gsyvideoplayer-armv5:11.3.0'
 implementation 'io.github.carguo:gsyvideoplayer-x86:11.3.0'
 implementation 'io.github.carguo:gsyvideoplayer-x64:11.3.0'
```

#### C. Support other format protocols (mpeg, rtsp, concat, crypto protocols, support 16k Page Size)

A and B normal versions support 263/264/265, etc. For mpeg encoding, there will be sound but no picture.
The so introduced by C supports mpeg encoding and other supplementary protocols, but the so package is relatively larger.

```groovy
 implementation 'io.github.carguo:gsyvideoplayer-java:11.3.0'

 //Whether ExoPlayer mode is needed
 implementation 'io.github.carguo:gsyvideoplayer-exo2:11.3.0'

 //Whether AliPlayer mode is needed
 implementation 'io.github.carguo:gsyvideoplayer-aliplay:11.3.0'

 //More ijk encoding support
 implementation 'io.github.carguo:gsyvideoplayer-ex_so:11.3.0'

```

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

**You can choose one of the following three and add it to the build.gradle under the module.**

#### A. Direct Introduction

```groovy
 //Complete version introduction
 implementation 'com.shuyu:gsyvideoplayer:11.3.0'


 //Whether AliPlayer mode is needed
 implementation 'com.shuyu:gsyvideoplayer-aliplay:11.3.0'
```

#### B. Add java and the so support you want:

```groovy
 implementation 'com.shuyu:gsyvideoplayer-java:11.3.0'

 //Whether ExoPlayer mode is needed
 implementation 'com.shuyu:gsyvideoplayer-exo2:11.3.0'

 //Whether AliPlayer mode is needed
 implementation 'com.shuyu:gsyvideoplayer-aliplay:11.3.0'

 //so of ijk mode according to your needs
 implementation 'com.shuyu:gsyvideoplayer-armv5:11.3.0'
 implementation 'com.shuyu:gsyvideoplayer-armv7a:11.3.0'
 implementation 'com.shuyu:gsyvideoplayer-arm64:11.3.0'
 implementation 'com.shuyu:gsyvideoplayer-x86:11.3.0'
 implementation 'com.shuyu:gsyvideoplayer-x64:11.3.0'
```

#### C. Support other format protocols (mpeg, rtsp, concat, crypto protocols, support 16k Page Size)

A and B normal versions support 263/264/265, etc. For mpeg encoding, there will be sound but no picture.
The so introduced by C supports mpeg encoding and other supplementary protocols, but the so package is relatively larger.

```groovy
 implementation 'com.shuyu:gsyvideoplayer-java:11.3.0'

 //Whether ExoPlayer mode is needed
 implementation 'com.shuyu:gsyvideoplayer-exo2:11.3.0'


 //Whether AliPlayer mode is needed
 implementation 'com.shuyu:gsyvideoplayer-aliplay:11.3.0'

 //More ijk encoding support
 implementation 'com.shuyu:gsyvideoplayer-ex_so:11.3.0'

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

 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer:v11.3.0'


 //Whether AliPlayer mode is needed
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-aliplay:v11.3.0'
```

#### B. Add java and the so support you want:

```groovy
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-java:v11.3.0'

 //Whether ExoPlayer mode is needed
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-exo2:v11.3.0'

 //Whether AliPlayer mode is needed
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-aliplay:v11.3.0'

 //so of ijk mode according to your needs
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-arm64:v11.3.0'
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-armv7a:v11.3.0'
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-armv5:v11.3.0'
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-x86:v11.3.0'
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-x64:v11.3.0'
```

#### C. Support other format protocols (mpeg, rtsp, concat, crypto protocols, support 16k Page Size)

A and B normal versions support 263/264/265, etc. For mpeg encoding, there will be sound but no picture.
The so introduced by C supports mpeg encoding and other supplementary protocols, but the so package is relatively larger.

```groovy
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-java:v11.3.0'

 //Whether ExoPlayer mode is needed
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-exo2:v11.3.0'

 //Whether AliPlayer mode is needed
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-aliplay:v11.3.0'

 //More ijk encoding support
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-ex_so:v11.3.0'

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
 Encoding Format          | **[--- IJK so file configuration format description](https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/DECODERS.md)**
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

## V. Recent Versions

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

### v10.2.0 (2025-06-03)

- update media 1.7.1
- update aliyun player

### v10.1.0 (2025-04-01)

- update media 1.6.0
- fix #4078
- link 4075 fix glsurface adapter video rotate info
- Add exo to switch tracks
- miniSdk 21, compileSdk 35

### v10.0.0 (2024-11-01)

- update media3 1.4.1
- update FFMpeg 4.1.6
- update openssl-1.1.1w
- support 16k page size
- fix #3999 & #3649
- fix #4014
- fix #4019
- fix #4023
- fix #4021
- update AGP

### v9.0.0-release-jitpack (2024-07-29)

* ex_so support 16k page size
* update media3 1.4.0
* fix #4014
* fix #3999 & #3649 system mediaPlayer setSpeed cause play
* fix #3972 ListGSYVideoPlayer carousel scene focus not removed, causing player status error

### More versions, please check: [Version Update Instructions](https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/UPDATE_VERSION.md)

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

```
-keep class com.shuyu.gsyvideoplayer.video.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.video.**
-keep class com.shuyu.gsyvideoplayer.video.base.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.video.base.**
-keep class com.shuyu.gsyvideoplayer.utils.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.utils.**
-keep class com.shuyu.gsyvideoplayer.player.** {*;}
-dontwarn com.shuyu.gsyvideoplayer.player.**
-keep class tv.danmaku.ijk.** { *; }
-dontwarn tv.danmaku.ijk.**
-keep class androidx.media3.** {*;}
-keep interface androidx.media3.**

-keep class com.shuyu.alipay.** {*;}
-keep interface com.shuyu.alipay.**

-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, java.lang.Boolean);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
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

[![Star History Chart](https://api.star-history.com/svg?repos=CarGuo/GSYVideoPlayer&type=Date)](https://star-history.com/#CarGuo/GSYVideoPlayer&Date)

## Warm Reminder

Open source projects mainly provide communication and learning, do not provide technical support, and do not accept business cooperation, purely public interest open source

## License

```
Please refer to the IJKPlayer and AndroidVideoCache related agreements.
The project started from jiecao, and was refactored after some changes.
Occasionally, some variable and method names may still have a shadow of jiaozi, but it is basically a new project.
```
