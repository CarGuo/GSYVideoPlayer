
![](https://github.com/CarGuo/GSYVideoPlayer/blob/master/home_logo.png)

## 基于[IJKPlayer](https://github.com/Bilibili/ijkplayer)，实现了多功能的视频播放器。

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

[![](https://jitpack.io/v/CarGuo/GSYVideoPlayer.svg)](https://jitpack.io/#CarGuo/GSYVideoPlayer)
[ ![Download](https://api.bintray.com/packages/carguo/GSYVideoPlayer/gsyVideoPlayer/images/download.svg) ](https://bintray.com/carguo/GSYVideoPlayer/gsyVideoPlayer/_latestVersion)
[![Build Status](https://travis-ci.org/CarGuo/GSYVideoPlayer.svg?branch=master)](https://travis-ci.org/CarGuo/GSYVideoPlayer)

## 使用依赖(支持jcenter和jitpack)

### 1、JCenter 引入方法

#### 直接在module下的build.gradle添加
```
compile 'com.shuyu:GSYVideoPlayer:1.6.7'

```

--------------------------------------------------------------------------------

### 2、JitPack引入方法
#### 在project下的build.gradle添加
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

#### 在module下的build.gradle添加

```
dependencies {
        compile 'com.github.CarGuo:GSYVideoPlayer:v1.6.7'
}
```

--------------------------------------------------------------------------------

* ### 下方文档以及问题集锦，你想要知道的大部分都在里面。

* ### <a href="https://github.com/CarGuo/GSYVideoPlayer/blob/master/QUESTION.md">有问题请先下面问题集锦中查阅（如依赖不成功，播放不成功等等）。</a>

* ### QQ群，有兴趣的可以进来（群里平时可能比较吵）：174815284 。

--------------------------------------------------------------------------------

## 文档Wiki

### [--- 使用说明、接口文档 - 入口](https://github.com/CarGuo/GSYVideoPlayer/wiki)

## 其他

### [--- 版本更新说明 - 入口](https://github.com/CarGuo/GSYVideoPlayer/blob/master/UPDATE_VERSION.md)
### [--- 问题集锦 - 入口 ](https://github.com/CarGuo/GSYVideoPlayer/blob/master/QUESTION.md)
### [--- IJKPlayer问题 - 入口](http://www.jianshu.com/p/220b00d00deb)　
### [--- IJKPlayer编译自定义SO - 入口](http://www.jianshu.com/p/bd289e25d272)　
### [--- 简书详解 - 入口](http://www.jianshu.com/p/9fe377dd9750)

　
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
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/09.gif" width="360px" height="240px"/>


* ### 4、进度条小窗口预览
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/07.gif" width="426px" height="240px"/>


## 近期版本

### 1.6.7(2017-06-16)
* fix bug #265，全屏按返回按键之后的虚拟按键显示问题
* so编译配置增加protocol crypto
* 增加设置触摸显示控制ui的消失时间接口 
```
StandardGSYVideoPlayer.java
/**
 * 设置触摸显示控制ui的消失时间
 * @param dismissControlTime 毫秒，默认2500
 */
public void setDismissControlTime(int dismissControlTime)
```
* 调整触摸滑动快进的比例
```
/**
 * 调整触摸滑动快进的比例
 * @param seekRatio 滑动快进的比例，默认1。数值越大，滑动的产生的seek越小
 */
public void setSeekRatio(float seekRatio) 
```
* 增加了拉伸填充的配置
```
GSYVideoType.java
//全屏拉伸显示，使用这个属性时，surface_container建议使用FrameLayout
public final static int SCREEN_MATCH_FULL = -4;
```

### 更多版本请查阅：[版本更新说明](https://github.com/CarGuo/GSYVideoPlayer/blob/master/UPDATE_VERSION.md)

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
```
