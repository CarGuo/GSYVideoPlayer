
![](https://github.com/CarGuo/GSYVideoPlayer/blob/master/home_logo.png)

## 基于[IJKPlayer](https://github.com/Bilibili/ijkplayer)（兼容系统MediaPlayer与EXOPlayer2），实现了多功能的视频播放器。 (请仔细阅读下方各项说明，大多数问题可在下方找到解答)。

状态 | 功能
-------- | ---
**支持**|**边播边缓存，使用了[AndroidVideoCache](https://github.com/danikula/AndroidVideoCache)。**
**支持**|**其他协议和编码concat、rtsp、hls、rtmp、crypto、mpeg等。**
**支持**|**DEMO演示获取视频第一帧等实现。**
**支持**|**简单滤镜（马赛克、黑白、色彩过滤、高斯、模糊、模糊等等20多种）。**
**支持**|**GL效果动画，（xyz轴旋转，放大）。**
**支持**|**视频帧截图功能，视频生成gif功能**
**支持**|**视频播放效果（水印、画面多重播放等）**
**支持**|**列表播放；列表连续播放；重力旋转与手动旋转；视频本身rotation旋转属性。**
**支持**|**全屏切换动画效果；小窗口播放，可拖动。**
**支持**|**快播和慢播；网络视频加载速度。**
**支持**|**调整显示比例:默认、16:9、4:3、填充。**
**支持**|**播放时旋转画面角度（0,90,180,270）；镜像旋转。**
**支持**|**暂停前后台切换不黑屏；调整不同清晰度的支持；无缝切换支持。**
**支持**|**Https；IJKPlayer、EXOPlayer、系统播放器切换。**
**支持**|**锁定/解锁全屏点击功能；进度条小窗口预览（测试）。**
**支持**|**全屏与非全屏两套布局切换；弹幕功能。**
**支持**|**没有任何操作控件的纯播放支持。**
**支持**|**Demo带有RecyclerView列表滑动自动播放。**
**支持**|**多窗体下（包括桌面）的小窗口播放。**
**支持**|**同时播放多个视频**
**支持**|**片头广告、跳过广告支持。**
**支持**|**中简插入广告功能。**
**支持**|**可自定义渲染层、自定义管理层、自定义播放层。**


[![](https://jitpack.io/v/CarGuo/GSYVideoPlayer.svg)](https://jitpack.io/#CarGuo/GSYVideoPlayer)
[ ![Download](https://api.bintray.com/packages/carguo/GSYVideoPlayer/gsyVideoPlayer/images/download.svg) ](https://bintray.com/carguo/GSYVideoPlayer/gsyVideoPlayer/_latestVersion)
[![Build Status](https://travis-ci.org/CarGuo/GSYVideoPlayer.svg?branch=master)](https://travis-ci.org/CarGuo/GSYVideoPlayer)

[]()
[![GitHub stars](https://img.shields.io/github/stars/CarGuo/GSYVideoPlayer.svg)](https://github.com/CarGuo/GSYVideoPlayer/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/CarGuo/GSYVideoPlayer.svg)](https://github.com/CarGuo/GSYVideoPlayer/network)
[![GitHub issues](https://img.shields.io/github/issues/CarGuo/GSYVideoPlayer.svg)](https://github.com/CarGuo/GSYVideoPlayer/issues)
[![GitHub license](https://img.shields.io/github/license/CarGuo/GSYVideoPlayer.svg)](https://github.com/CarGuo/GSYVideoPlayer/blob/master/LICENSE)

### [微信赞赏链接](https://github.com/CarGuo/GSYVideoPlayer/blob/master/thanks.jpg)

## 一、使用依赖

##### 新版本调整了代码结构，如更新后显示类路径错误，参考demo调整包路径即可。

### 1、JCenter 引入方法（推荐）

**你可以选择下面三种的其中一种，在module下的build.gradle添加。**

#### A、直接引入
```
//完整版引入
compile 'com.shuyu:GSYVideoPlayer:4.1.0'

```

#### B、添加java和你想要的so支持：

```
compile 'com.shuyu:gsyVideoPlayer-java:4.1.0'

//根据你的需求
compile 'com.shuyu:gsyVideoPlayer-armv5:4.1.0'
compile 'com.shuyu:gsyVideoPlayer-armv7a:4.1.0'
compile 'com.shuyu:gsyVideoPlayer-arm64:4.1.0'
compile 'com.shuyu:gsyVideoPlayer-x64:4.1.0'
compile 'com.shuyu:gsyVideoPlayer-x86:4.1.0'

```

#### C、支持其他格式协议的（mpeg，rtsp, concat、crypto协议）

A、B普通版本支持263/264/265等，对于mpeg编码会有声音无画面情况。
C 引入的so支持mpeg编码和其他补充协议，但是so包相对变大。
 
```
compile 'com.shuyu:gsyVideoPlayer-java:4.1.0'

compile 'com.shuyu:gsyVideoPlayer-ex_so:4.1.0'

```

### [--- 更多依赖方式请点击 - ](https://github.com/CarGuo/GSYVideoPlayer/blob/master/dependencies.md)

## 二、其他推荐

### * QQ群，有兴趣的欢迎（平时吹水吐槽多，因为人数饱和，目前开启付费入群）：174815284 。
### * [GSYGithubAPP 跨平台github客户端](https://github.com/CarGuo/GSYGithubAPP)
### * [RickText](https://github.com/CarGuo/RickText)
### * [LazyRecyclerAdapter](https://github.com/CarGuo/LazyRecyclerAdapter)

## 三、文档Wiki

### [--- 使用说明、接口文档 - 入口](https://github.com/CarGuo/GSYVideoPlayer/wiki)
### [--- 问题集锦 - 入口（大部分你遇到的问题都在这里解决） ](https://github.com/CarGuo/GSYVideoPlayer/blob/master/QUESTION.md)
### [--- 项目支持视频格式（如果遇上黑屏，没声音）](https://github.com/CarGuo/GSYVideoPlayer/blob/master/DECODERS.md)
### [--- IJKPlayer问题 - 入口](http://www.jianshu.com/p/220b00d00deb)　
### [--- IJKPlayer编译自定义SO - 入口](http://www.jianshu.com/p/bd289e25d272)　
### [--- 简书详解 （项目的基础）- 入口](http://www.jianshu.com/p/9fe377dd9750)
### [--- 版本更新说明 - 入口](https://github.com/CarGuo/GSYVideoPlayer/blob/master/UPDATE_VERSION.md)

## 四、运行效果

* ### 1、打开一个播放(旋转、镜像、填充)
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/11.gif" width="240px" height="426px"/>

* ### 2、列表/详情模式(动画、旋转、小窗体)

<div>
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/22.gif" width="240px" height="426px"/>
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/33.gif" width="240px" height="426px"/>
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/44.gif" width="240px" height="426px"/>
</div>

* ### 3、弹幕
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/55.gif" width="240px" height="426px"/>

* ### 4、滤镜和GL动画
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/09.gif"/>

* ### 6、背景铺满模糊播放

<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/99.png" width="426px" height="240px"/>

* ### 7、进度条小窗口预览
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/07.gif" height="240px"/>

## 五、近期版本

### 4.1.0 (2018-02-26)
* 1、update to ijk 0.8.8
* 2、去除cache模块的log库依赖
* 3、去除exo模块的无用依赖
* 4、增加恢复播放方法参数
```
 XXXXManager相关
/**
  * 恢复暂停状态
  *
  * @param seek 是否产生seek动作,直播设置为false
  */
 public static void onResume(String key, boolean seek)

 Video相关
 /**
  * 恢复暂停状态
  *
  * @param seek 是否产生seek动作
  */
 @Override
 public void onVideoResume(boolean seek)

```

### 4.0.0-beat1（2018-02-06）
* 1、新增简单片头广告支持。
`GSYSampleADVideoPlayer 与 DetailADPlayer`
* 2、优化了ListGSYVideoPlayer、增加`playNext()`接口。
* 3、优化代码结构，调整部分API接口（稍微调整下，偶尔有和旧版本不兼容的，参考源码和demo修改下方法名即可）。
* 4、增加GSYVideoHelper视频帮助类，更加节省资源。
* 5、增加GSYSampleCallBack节省继承，优化GSYVideoProgressListener的回调。
* 6、增加GSYVideoViewBridge、重载`getGSYVideoManager()`方法实现自己的Manager。
* 7、支持自定义渲染层，demo中`CustomRenderVideoPlayer`演示如何设置自定义渲染层。
* 8、`ListMultiVideoActivity`和`MultiSampleVideo`演示如何同时播放多个视频。
* 9、`DetailADPlayer2`和`ListADVideoActivity`演示广告与中间插入广告支持。
* 10、增加音频焦点方法。
```
/**
  * 长时间失去音频焦点，暂停播放器
  *
  * @param releaseWhenLossAudio 默认true，false的时候只会暂停
  */
 public void setReleaseWhenLossAudio(boolean releaseWhenLossAudio)

```




### 更多版本请查阅：[版本更新说明](https://github.com/CarGuo/GSYVideoPlayer/blob/master/UPDATE_VERSION.md)

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

##### 微信赞赏码

<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/thanks.jpg" height="400px" width="400px"/>


## License

```
请参看IJKPlayer和AndroidVideoCache相关协议。
项目最开始是从jiecao过来的，改着改着直接重构了。
偶尔有一变量和方法名可能还有点jiaozi的影子，但是基本是一个新项目。
```
