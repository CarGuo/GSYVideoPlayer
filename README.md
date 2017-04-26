
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

[![](https://jitpack.io/v/CarGuo/GSYVideoPlayer.svg)](https://jitpack.io/#CarGuo/GSYVideoPlayer)
[ ![Download](https://api.bintray.com/packages/carguo/GSYVideoPlayer/gsyVideoPlayer/images/download.svg) ](https://bintray.com/carguo/GSYVideoPlayer/gsyVideoPlayer/_latestVersion)
[![Build Status](https://travis-ci.org/CarGuo/GSYVideoPlayer.svg?branch=master)](https://travis-ci.org/CarGuo/GSYVideoPlayer)

## 使用依赖(支持jcenter和jitpack)

### 1、JCenter 引入方法

#### 直接在module下的build.gradle添加
```
compile 'com.shuyu:GSYVideoPlayer:1.6.4'

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
        compile 'com.github.CarGuo:GSYVideoPlayer:v1.6.4'
}
```

--------------------------------------------------------------------------------

* ### 下方文档以及问题集锦，你想要知道的大部分都在里面。

* ### ！！有问题请先下面问题集锦中查阅（如依赖不成功，播放不成功等等）！！

* ### QQ群，有兴趣的可以进来，无底线欢迎：174815284 。

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
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/02.gif" width="240px" height="426px"/>
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/05.gif" width="240px" height="426px"/>
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/04.gif" width="240px" height="426px"/>

* ### 3、弹幕
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/09.gif" width="360px" height="240px"/>


* ### 4、进度条小窗口预览
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/07.gif" width="426px" height="240px"/>

## 近期版本

### 1.6.5(未发布)
* 修改了循环播放的UI问题
* 修改了本地文件或者已缓存文件，显示进度问题 


### 1.6.4(2017-04-20)
* update ijk to 0.7.9 (增加了soundTouch，调速后声音变调问题得到解决)
* 修复了可能出现的判空问题，修复了ListGSYVideoPlayer的同步问题 
* 修复了可移动小窗口播放结束无法移动的问题

### 更多版本请查阅：[版本更新说明](https://github.com/CarGuo/GSYVideoPlayer/blob/master/UPDATE_VERSION.md)

## 混淆

```
-keep class tv.danmaku.ijk.** { *; }
-dontwarn tv.danmaku.ijk.**
-keep class com.shuyu.gsyvideoplayer.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.**
```

## License

```
请参看IJKPlayer和AndroidVideoCache相关协议。
```