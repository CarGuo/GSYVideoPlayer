
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
* **调整不同清晰度的支持。**
* **支持IJKPlayer和EXOPlayer切换。**
* **进度条小窗口预览（测试）。**
* **Https支持。**
* **连续播放一个列表的视频。**
* **支持全屏与非全屏两套布局切换**
* **弹幕支持**

[![](https://jitpack.io/v/CarGuo/GSYVideoPlayer.svg)](https://jitpack.io/#CarGuo/GSYVideoPlayer)
[ ![Download](https://api.bintray.com/packages/carguo/GSYVideoPlayer/gsyVideoPlayer/images/download.svg) ](https://bintray.com/carguo/GSYVideoPlayer/gsyVideoPlayer/_latestVersion)
[![Build Status](https://travis-ci.org/CarGuo/GSYVideoPlayer.svg?branch=master)](https://travis-ci.org/CarGuo/GSYVideoPlayer)

## 使用依赖(支持jcenter和jitpack)

### Jcenter 引入方法
```
<dependency>
  <groupId>com.shuyu</groupId>
  <artifactId>GSYVideoPlayer</artifactId>
  <version>1.6.0</version>
  <type>pom</type>
</dependency>
```
#### 直接在module下的build.gradle添加
```
compile 'com.shuyu:GSYVideoPlayer:1.6.1'

```

--------------------------------------------------------------------------------

### JitPack引入方法
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
        compile 'com.github.CarGuo:GSYVideoPlayer:v1.6.1'
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

<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/01.jpg" width="218px" height="120px"/>
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/02.jpg" width="120px" height="218px"/>
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/03.jpg" width="120px" height="218px"/>
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/04.jpg" width="120px" height="218px"/>
　

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

### 1.6.1(2017-03-23)
* setSpeed接口修改为支持播放中设置
* 内存优化
* update ijk to 0.7.8.1
* 增加超时接口 GSYVideoManager
```
/**
 * 是否需要在buffer缓冲时，增加外部超时判断，目前对于刚开始超时还没效果
 *
 * 超时后会走onError接口，播放器通过onPlayError回调出
 *
 * 错误码为 ： BUFFER_TIME_OUT_ERROR = -192
 *
 * 由于onError之后执行GSYVideoPlayer的OnError，如果不想触发错误，
 * 可以重载onError，在super之前拦截处理。
 *
 * public void onError(int what, int extra){
 *     do you want before super and return;
 *     super.onError(what, extra)
 * }
 *
 * @param timeOut          超时时间，毫秒 默认8000
 * @param needTimeOutOther 是否需要延时设置，默认关闭
 */
public void setTimeOut(int timeOut, boolean needTimeOutOther) {
    this.timeOut = timeOut;
    this.needTimeOutOther = needTimeOutOther;
}

```


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