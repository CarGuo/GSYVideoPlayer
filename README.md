
![](https://github.com/CarGuo/GSYVideoPlayer/blob/master/img/home_logo.png)

## 基于[IJKPlayer](https://github.com/Bilibili/ijkplayer)（兼容系统MediaPlayer与EXOPlayer2），实现了多功能的视频播放器。 (请仔细阅读下方各项说明，大多数问题可在下方找到解答)。

> ## [如果克隆太慢，可尝试从码云地址下载](https://gitee.com/CarGuo/GSYVideoPlayer)

类型 | 功能
-------- | ---
**缓存**|**边播边缓存，使用了[AndroidVideoCache](https://github.com/danikula/AndroidVideoCache)；ExoPlayer使用SimpleCache。**
**协议**|**h263\4\5、Https、concat、rtsp、hls、rtmp、crypto、mpeg等等。[（ijk模式格式支持）](https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/DECODERS.md)**
**滤镜**|**简单滤镜（马赛克、黑白、色彩过滤、高斯、模糊、模糊等等20多种）、动画、（水印、画面多重播放等）。**
**帧图**|**视频第一帧、视频帧截图功能，视频生成gif功能。**
**播放**|**列表播放、列表连续播放、重力旋转与手动旋转、视频本身rotation旋转属性、快播和慢播、网络视频加载速度。**
**画面**|**调整显示比例:默认、16:9、4:3、填充；播放时旋转画面角度（0,90,180,270）；镜像旋转。**
**内核**|**IJKPlayer、EXOPlayer、MediaPlayer切换、自定义内核**
**布局**|**全屏与非全屏两套布局切换、没有任何操作控件的纯播放支持、弹幕功能、继承自定义任何布局。**
**播放**|**单例播放、多个同时播放、视频列表滑动自动播放、列表切换详情页面无缝播放。**
**窗口**|**小窗口、多窗体下（包括桌面）的小窗口播放。**
**广告**|**片头广告、跳过广告支持、中间插入广告功能。**
**字幕**|**[exo2模式下支持自定增加外挂字幕](https://github.com/CarGuo/GSYVideoPlayer/tree/master/app/src/main/java/com/example/gsyvideoplayer/exosubtitle)。**
**更多**|**暂停前后台切换不黑屏；调整不同清晰度的支持；无缝切换支持；锁定/解锁全屏点击功能；进度条小窗口预览（测试）。**
**自定义**|**可自定义渲染层、自定义管理层、自定义播放层（控制层）、自定义缓存层。**

[ ![Download](https://api.bintray.com/packages/carguo/GSYVideoPlayer/gsyVideoPlayer-java/images/download.svg) ](https://bintray.com/carguo/GSYVideoPlayer/gsyVideoPlayer-java/_latestVersion)
[![](https://jitpack.io/v/CarGuo/GSYVideoPlayer.svg)](https://jitpack.io/#CarGuo/GSYVideoPlayer)
[ ![Download](https://api.bintray.com/packages/carguo/GSYVideoPlayer/gsyVideoPlayer/images/download.svg) ](https://bintray.com/carguo/GSYVideoPlayer/gsyVideoPlayer/_latestVersion)
[![Build Status](https://travis-ci.org/CarGuo/GSYVideoPlayer.svg?branch=master)](https://travis-ci.org/CarGuo/GSYVideoPlayer)

[]()
[![GitHub stars](https://img.shields.io/github/stars/CarGuo/GSYVideoPlayer.svg)](https://github.com/CarGuo/GSYVideoPlayer/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/CarGuo/GSYVideoPlayer.svg)](https://github.com/CarGuo/GSYVideoPlayer/network)
[![GitHub issues](https://img.shields.io/github/issues/CarGuo/GSYVideoPlayer.svg)](https://github.com/CarGuo/GSYVideoPlayer/issues)
[![GitHub license](https://img.shields.io/github/license/CarGuo/GSYVideoPlayer.svg)](https://github.com/CarGuo/GSYVideoPlayer/blob/master/LICENSE)

### [-----------------微信赞赏链接-----------------](https://github.com/CarGuo/GSYVideoPlayer/blob/master/img/thanks.jpg)

### [--------------Demo APK 下载地址---------------](https://github.com/CarGuo/GSYVideoPlayer/releases)

![](http://img.cdn.guoshuyu.cn/WeChat-Code)

## 一、使用依赖

#### 7.0版本使用了anndroidx，support版本请看6.x.x，请查看：[--- 版本更新说明 - 入口](https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/UPDATE_VERSION.md)。

### 1、JCenter 引入方法（推荐）

**你可以选择下面三种的其中一种，在module下的build.gradle添加。**

#### A、直接引入
```
//完整版引入
implementation 'com.shuyu:GSYVideoPlayer:7.1.2'

```

#### B、添加java和你想要的so支持：

```
implementation 'com.shuyu:gsyVideoPlayer-java:7.1.2'

//是否需要ExoPlayer模式
implementation 'com.shuyu:GSYVideoPlayer-exo2:7.1.2'

//根据你的需求ijk模式的so
implementation 'com.shuyu:gsyVideoPlayer-armv5:7.1.2'
implementation 'com.shuyu:gsyVideoPlayer-armv7a:7.1.2'
implementation 'com.shuyu:gsyVideoPlayer-arm64:7.1.2'
implementation 'com.shuyu:gsyVideoPlayer-x64:7.1.2'
implementation 'com.shuyu:gsyVideoPlayer-x86:7.1.2'

```

#### C、支持其他格式协议的（mpeg，rtsp, concat、crypto协议）

A、B普通版本支持263/264/265等，对于mpeg编码会有声音无画面情况。
C 引入的so支持mpeg编码和其他补充协议，但是so包相对变大。
 
```
implementation 'com.shuyu:gsyVideoPlayer-java:7.1.2'

//是否需要ExoPlayer模式
implementation 'com.shuyu:GSYVideoPlayer-exo2:7.1.2'

//更多ijk的编码支持
implementation 'com.shuyu:gsyVideoPlayer-ex_so:7.1.2'

```

#### 代码中的全局切换支持（更多请参看下方文档和demo）

```

//EXOPlayer内核，支持格式更多
PlayerFactory.setPlayManager(Exo2PlayerManager.class);
//系统内核模式
PlayerFactory.setPlayManager(SystemPlayerManager.class);
//ijk内核，默认模式
PlayerFactory.setPlayManager(IjkPlayerManager.class);


//exo缓存模式，支持m3u8，只支持exo
CacheFactory.setCacheManager(ExoPlayerCacheManager.class);
//代理缓存模式，支持所有模式，不支持m3u8等，默认
CacheFactory.setCacheManager(ProxyCacheManager.class);



//切换渲染模式
GSYVideoType.setShowType(GSYVideoType.SCREEN_MATCH_FULL);
//默认显示比例
GSYVideoType.SCREEN_TYPE_DEFAULT = 0;
//16:9
GSYVideoType.SCREEN_TYPE_16_9 = 1;
//4:3
GSYVideoType.SCREEN_TYPE_4_3 = 2;
//全屏裁减显示，为了显示正常 CoverImageView 建议使用FrameLayout作为父布局
GSYVideoType.SCREEN_TYPE_FULL = 4;
//全屏拉伸显示，使用这个属性时，surface_container建议使用FrameLayout
GSYVideoType.SCREEN_MATCH_FULL = -4;
/***
 * SCREEN_TYPE_CUSTOM 下自定义显示比例
 * @param screenScaleRatio  高宽比，如 16：9
 */
public static void setScreenScaleRatio(float screenScaleRatio)


//切换绘制模式
GSYVideoType.setRenderType(GSYVideoType.SUFRACE);
GSYVideoType.setRenderType(GSYVideoType.GLSURFACE);
GSYVideoType.setRenderType(GSYVideoType.TEXTURE);


//ijk关闭log
IjkPlayerManager.setLogLevel(IjkMediaPlayer.IJK_LOG_SILENT);


//exoplayer自定义MediaSource
ExoSourceManager.setExoMediaSourceInterceptListener(new ExoMediaSourceInterceptListener() {
    @Override
    public MediaSource getMediaSource(String dataSource, boolean preview, boolean cacheEnable, boolean isLooping, File cacheDir) {
        //可自定义MediaSource
        return null;
    }
});

```

### [--- 更多依赖方式请点击 - ](https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/DEPENDENCIES.md)

## 二、其他推荐

### * QQ群，有兴趣的欢迎（平时吹水吐槽多，因为人数饱和，目前开启付费入群）：174815284 。
### * [Flutter Github客户端](https://github.com/CarGuo/gsy_github_app_flutter) 、[React Native Github客户端](https://github.com/CarGuo/GSYGithubAPP) 、 [Weex Github客户端](https://github.com/CarGuo/GSYGithubAPPWeex) 、 [原生 Kotlin Github客户端](https://github.com/CarGuo/GSYGithubAPPKotlin)
### * [RxFFmpeg Android 的音视频编辑工具](https://github.com/microshow/RxFFmpeg)
### * [oarplayer Rtmp播放器,基于MediaCodec与srs-librtmp,不依赖ffmpeg](https://github.com/qingkouwei/oarplayer)
### * [RickText](https://github.com/CarGuo/RickText)
### * [LazyRecyclerAdapter](https://github.com/CarGuo/LazyRecyclerAdapter)


## 三、文档Wiki

文档 | 传送门
-------- | ---
**使用说明**|***[--- 简单使用，快速上手文档](https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/USE.md)***
**建议阅读**|***[--- 移动开发者必知的音视频基础知识](https://mp.weixin.qq.com/s/HjSdmAsHuvixCH_EWdvk3Q)***
**项目解析说明**|***[--- 项目解析说明、包含项目架构和解析](https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/GSYVIDEO_PLAYER_PROJECT_INFO.md)***
接口文档入口|**[--- 使用说明、接口文档 - 入口](https://github.com/CarGuo/GSYVideoPlayer/wiki)**
**问题集锦入口**|***[--- 问题集锦 - 入口（大部分你遇到的问题都在这里解决） ](https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/QUESTION.md)***
编码格式|**[--- IJK so文件配置格式说明](https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/DECODERS.md)**
编译自定义SO|**[--- IJKPlayer编译自定义SO - 入口](http://www.jianshu.com/p/bd289e25d272)**
版本更新说明|**[--- 版本更新说明 - 入口](https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/UPDATE_VERSION.md)**


![框架图](https://raw.githubusercontent.com/CarGuo/GSYVideoPlayer/master/img/StructureChart2.jpg)

## 四、运行效果

* ### 1、打开一个播放(旋转、镜像、填充)
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/img/11.gif" width="240px" height="426px"/>

* ### 2、列表/详情模式(动画、旋转、小窗体)

<div>
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/img/22.gif" width="240px" height="426px"/>
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/img/33.gif" width="240px" height="426px"/>
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/img/44.gif" width="240px" height="426px"/>
</div>

* ### 3、弹幕
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/img/55.gif" width="240px" height="426px"/>

* ### 4、滤镜和GL动画
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/img/09.gif"/>

* ### 6、背景铺满模糊播放

<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/img/99.png" width="426px" height="240px"/>

* ### 7、进度条小窗口预览
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/img/07.gif" height="240px"/>

## 五、近期版本


### 7.1.2(2019-12-02)

* fix #2436 增加 exo 的 http timeout 
```
ExoSourceManager

 public static void setHttpReadTimeout(int httpReadTimeout)
 
 public static void setHttpConnectTimeout(int httpConnectTimeout)

```
* 优化视频尺寸显示计算 
* 增加 exo 支持 raw 文件播放

``` 
String url =  RawResourceDataSource.buildRawResourceUri(R.raw.test).toString();
```
* 增加模拟下载共用缓存例子 DetailDownloadPlayer
* 适配 android 10 的全屏
* fix #2382、#2411、#2343、#2379、#2350、#2328
* 增加设置自定义显示比例的支持

``` 
GSYVideoType.setScreenScaleRatio
```
* 增加外挂字幕例子 [exo2模式下支持自定增加外挂字幕](https://github.com/CarGuo/GSYVideoPlayer/tree/master/app/src/main/java/com/example/gsyvideoplayer/exosubtitle)


### 7.1.1(2019-10-12)

* fix #2244、#2252(resolveFullVideoShow 不执行情况)、#2279、#2280
* fix #2303(去除 TimerTask)、#2306（某些机型退到后台返回不显示）
* 增加 setNeedAutoAdaptation
```
    /**
     * 是否需要适配在竖屏横屏时，由于刘海屏或者打孔屏占据空间，导致标题显示被遮盖的问题
     *
     * @param needAutoAdaptation 默认false
     */
    public void setNeedAutoAdaptation(boolean needAutoAdaptation)
```

### 7.1.0(2019-09-01)

* update ExoPlayer to 2.10.4
* 添加沉浸式支持
* 增加 IPlayerInitSuccessListener 播放器初始化成果回调
```
GSYVideoManager
    .instance()
    .setPlayerInitSuccessListener(new IPlayerInitSuccessListener() {
        ///播放器初始化成果回调，可用于播放前的自定义设置
        @Override
        public void onPlayerInitSuccess(IMediaPlayer player, GSYModel model) {
            if (player instanceof IjkExo2MediaPlayer) {
                ((IjkExo2MediaPlayer) player).setTrackSelector(new DefaultTrackSelector());
                ((IjkExo2MediaPlayer) player).setLoadControl(new DefaultLoadControl());
            }
        }
    });
```
* fix #2142
* 增加硬解码不花屏幕 [RecyclerView3Activity](https://github.com/CarGuo/GSYVideoPlayer/blob/master/app/src/main/java/com/example/gsyvideoplayer/RecyclerView3Activity.java)


### 非 androidx 版本为 6.0.3 以下版本。更多兼容版本请查阅版本更新。

### 更多版本请查阅：[版本更新说明](https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/UPDATE_VERSION.md)

## 六、关于Issues

```
提问题前可先查阅上方文档和说明，请在Demo中复现问题。

问题说明：

1、说明那个Demo中哪个页面。
2、问题显现和重现步骤。
3、补充问题的视频流url，截图。
4、补充问题的机型，android版本。
```

## 七、混淆

```
-keep class com.shuyu.gsyvideoplayer.video.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.video.**
-keep class com.shuyu.gsyvideoplayer.video.base.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.video.base.**
-keep class com.shuyu.gsyvideoplayer.utils.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.utils.**
-keep class tv.danmaku.ijk.** { *; }
-dontwarn tv.danmaku.ijk.**

-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
```

## 温馨提示

#### [如果克隆太慢，可尝试从码云地址下载](https://gitee.com/CarGuo/GSYVideoPlayer)

```
关于自定义和出现问题的请先看问题集锦、demo、issue。

多了解一些音视频的基础常识，对容器，音视频编码，ffmpeg先做一些了解，以及mediacodec等的不同。
尽量少出现为什么别的能播的问题哟。

播放器的可自定义还是挺高的，定制请参考demo，多看源码。现在的功能有些多，demo也在不断的更新。

一些新功能和项目结构也在不断的调整。

欢迎提出问题，谢谢。

```

## 依赖大小参考
建议使用ndk过滤，详细参考 [参考第四条 ： 4、NDK的so支持](http://www.jianshu.com/p/86e4b336c17d)
![](https://ooo.0o0.ooo/2017/06/15/5941f343a39f5.png)


## 非常感谢您的支持

#### 撸码不易，如果对你有所帮助，欢迎您的赞赏

![](https://github.com/CarGuo/GSYVideoPlayer/blob/master/img/thanks.jpg)


## License

```
请参看IJKPlayer和AndroidVideoCache相关协议。
项目最开始是从jiecao过来的，改着改着直接重构了。
偶尔有一变量和方法名可能还有点jiaozi的影子，但是基本是一个新项目。
```
