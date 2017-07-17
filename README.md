
![](https://github.com/CarGuo/GSYVideoPlayer/blob/master/home_logo.png)

## 基于[IJKPlayer](https://github.com/Bilibili/ijkplayer)，实现了多功能的视频播放器。 (请仔细阅读下方各项说明，大多数问题可在下方找到解答)。
 
* **支持基本的拖动，声音、亮度调节。**
* **支持边播边缓存，使用了[AndroidVideoCache](https://github.com/danikula/AndroidVideoCache)。**
* **支持视频本身自带rotation的旋转。**
* **增加了重力旋转与手动旋转的同步支持。**
* **支持列表播放。**
* **直接添加控件为封面。**
* **全屏和播放等的动画效果。**
* **列表的全屏效果优化，多种配置模式。**
* **列表的小窗口播放，可拖动。**
* **网络视频加载速度。**
* **锁定/解锁全屏点击功能。**
* **支持快播和慢播。**
* **调整显示比例:默认、16:9、4:3。**
* **暂停时前后台切换不黑屏**
* **调整不同清晰度的支持。**
* **支持IJKPlayer和EXOPlayer切换。**
* **进度条小窗口预览（测试）。**
* **Https支持。**
* **支持播放时旋转画面角度（0,90,180,270）。**
* **连续播放一个列表的视频。**
* **支持全屏与非全屏两套布局切换**
* **弹幕支持**
* **镜像旋转**
* **完美实现播放、暂停、前后台切换、画面调整等情况不黑屏不突变**
* **concat、rtsp、crypto、mpeg**

[![](https://jitpack.io/v/CarGuo/GSYVideoPlayer.svg)](https://jitpack.io/#CarGuo/GSYVideoPlayer)
[ ![Download](https://api.bintray.com/packages/carguo/GSYVideoPlayer/gsyVideoPlayer/images/download.svg) ](https://bintray.com/carguo/GSYVideoPlayer/gsyVideoPlayer/_latestVersion)
[![Build Status](https://travis-ci.org/CarGuo/GSYVideoPlayer.svg?branch=master)](https://travis-ci.org/CarGuo/GSYVideoPlayer)

## 使用依赖(支持jcenter或jitpack)

### 1、JCenter 引入方法（推荐）

**你可以选择下面三种的其中一种，在module下的build.gradle添加。**

#### A、直接引入
```
//完整版引入
compile 'com.shuyu:GSYVideoPlayer:2.0.2'

```

#### B、添加java和你想要的so支持：

```
compile 'com.shuyu:gsyVideoPlayer-java:2.0.2'

//根据你的需求
compile 'com.shuyu:gsyVideoPlayer-armv5:2.0.2'
compile 'com.shuyu:gsyVideoPlayer-armv7a:2.0.2'
compile 'com.shuyu:gsyVideoPlayer-arm64:2.0.2'
compile 'com.shuyu:gsyVideoPlayer-x64:2.0.2'
compile 'com.shuyu:gsyVideoPlayer-x86:2.0.2'

```

#### C、支持其他格式协议的（mpeg，rtsp, concat、crypto协议）

A、B普通版本支持263/264/265等，对于mpeg编码会有声音无画面情况。
C 引入的so支持mpeg编码和其他补充协议，但是so包相对变大。
 
```
compile 'com.shuyu:gsyVideoPlayer-java:2.0.2' 

compile 'com.shuyu:gsyVideoPlayer-ex_so:2.0.2' 

```

#### D、支持使用ijkPlayer的so

```
compile 'tv.danmaku.ijk.media:ijkplayer-armv7a:0.8.1'
compile 'tv.danmaku.ijk.media:ijkplayer-armv5:0.8.1'
compile 'tv.danmaku.ijk.media:ijkplayer-arm64:0.8.1'
compile 'tv.danmaku.ijk.media:ijkplayer-x86:0.8.1'
compile 'tv.danmaku.ijk.media:ijkplayer-x86_64:0.8.1'
```

--------------------------------------------------------------------------------

### 2、JitPack引入方法

#### First、在project下的build.gradle添加
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

#### Sencond、在module下的build.gradle添加

**你可以选择下面三种的其中一种，在module下的build.gradle添加。**

#### A、直接引入
```
//完整版引入
compile 'com.github.CarGuo.GSYVideoPlayer:GSYVideoPlayer:v2.0.2'

```

#### B、添加java和你想要的so支持：

```

compile 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-java:v2.0.2'

//根据你的需求
compile 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-x64:v2.0.2'
compile 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-x86:v2.0.2'
compile 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-arm64:v2.0.2'
compile 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-armv5:v2.0.2'
compile 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-armv7a:v2.0.2'

```

#### C、支持其他格式协议的（mpeg，rtsp, concat、crypto协议）

A、B普通版本支持263/264/265等，对于mpeg编码会有声音无画面情况。
C 方法引入的so支持mpeg编码和其他补充协议，但是so包相对变大。
 
```
compile 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-java:v2.0.2'

compile 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-ex_so:v2.0.2'

```
   
--------------------------------------------------------------------------------

* ### 下方文档以及问题集锦，你想要知道的大部分都在里面。

* ### <a href="https://github.com/CarGuo/GSYVideoPlayer/blob/master/QUESTION.md">有问题请先下面问题集锦中查阅（如依赖不成功，播放不成功等等）。</a>

* ### QQ群，有兴趣的可以进来（群里平时可能比较吵）：174815284 。

--------------------------------------------------------------------------------

## 文档Wiki

### [--- 使用说明、接口文档 - 入口](https://github.com/CarGuo/GSYVideoPlayer/wiki)

## 其他

### [--- 问题集锦 - 入口 ](https://github.com/CarGuo/GSYVideoPlayer/blob/master/QUESTION.md)
### [--- 项目支持视频格式（如果遇上黑屏，没声音）](https://github.com/CarGuo/GSYVideoPlayer/blob/master/DECODERS.md)
### [--- IJKPlayer问题 - 入口](http://www.jianshu.com/p/220b00d00deb)　
### [--- IJKPlayer编译自定义SO - 入口](http://www.jianshu.com/p/bd289e25d272)　
### [--- 简书详解 （项目的基础）- 入口](http://www.jianshu.com/p/9fe377dd9750)
### [--- 版本更新说明 - 入口](https://github.com/CarGuo/GSYVideoPlayer/blob/master/UPDATE_VERSION.md)

　
## 运行效果

* ### 1、打开一个播放
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/01.gif" width="240px" height="426px"/>

* ### 2、列表/详情模式

<div>
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/02.gif" width="240px" height="426px"/>
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/05.gif" width="240px" height="426px"/>
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/04.gif" width="240px" height="426px"/>
</div>

* ### 3、弹幕
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/09.gif" width="426px" height="240px"/>

* ### 4、进度条小窗口预览
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/07.gif" height="240px"/>


## 近期版本

### 2.0.2(2017-07-16) >>>> [片头广告功能推迟到2.0.3]
* 完美实现播放、暂停、前后台切换、画面调整等情况不黑屏不突变，删除coverImageView类。
* 增加了6.0下变调不变速接口
* update ijkPlayer to 0.8.1

### 2.0.1(2017-07-11)
* 优化了TextureView显示
* 修复SampleView的暂停问题

### 更多版本请查阅：[版本更新说明](https://github.com/CarGuo/GSYVideoPlayer/blob/master/UPDATE_VERSION.md)

-------------------

## 关于Issues

```
提问题前可先查阅上方文档和说明，请在Demo中复现问题。

问题说明：

1、说明那个Demo中哪个页面。
2、问题显现和重现步骤。
3、补充问题的视频流url，截图。
4、补充问题的机型，android版本。
```

## 混淆

```
-keep class tv.danmaku.ijk.** { *; }
-dontwarn tv.danmaku.ijk.**
-keep class com.shuyu.gsyvideoplayer.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.**
```

## 依赖大小参考
建议使用ndk过滤，详细参考 [参考第四条 ： 4、NDK的so支持](http://www.jianshu.com/p/86e4b336c17d)
![](https://ooo.0o0.ooo/2017/06/15/5941f343a39f5.png)


## License

```
请参看IJKPlayer和AndroidVideoCache相关协议。
项目最开始是从jiecao过来的，只是后来方向不同，所以慢慢的也异化了。
```
