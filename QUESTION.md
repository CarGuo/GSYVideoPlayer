## GSYVideoPlayer 问题集锦

#### 0、因为某些原因，现在使用jitpack.io，如果使用依赖的朋友，还请各位切换到jitpack的依赖，详情可见github首页，还请见谅见谅。

#### 1、导入项目不成功？

　项目最外部有一个dependencies.gradle，所有的项目依赖都在这里面，然后参考项目根目录的build.gradle，在最顶部有apply from: 'dependencies.gradle'，这样gsyVideoPlayer就可以找到对应的依赖了。gradle方便可参考察[Android蹲坑的疑难杂症集锦（兼Gradle） 二](http://www.jianshu.com/p/86e4b336c17d)

#### 2、ClassNotFoundException和混淆

　确保你的拆包Application配置正常 。

```
-keep class tv.danmaku.ijk.** { *; }
-dontwarn tv.danmaku.ijk.**
-keep class com.shuyu.gsyvideoplayer.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.**

```

#### 3、找不到对应的so

  可以配置ndkguolv，或者引用21以上的arm64，x84_64
```
android {


        ···

    defaultConfig {
        ···
        ndk {

            //APP的build.gradle设置支持的SO库架构

            abiFilters 'armeabi', 'armeabi-v7a', 'x86'
        }

    }
}
```
```
compile 'tv.danmaku.ijk.media:ijkplayer-arm64:0.7.5'
compile 'tv.danmaku.ijk.media:ijkplayer-x86_64:0.7.5'

```

#### 3、全屏的时候自动停止了

　是否监听了列表滑动了，在监听里更新了列表之类的。


#### 4、播放一个列表的视频，可以在在listener的oncomplete里面，结束后延时一会再播放。



#### 5、目前不支持3gp或者mepg，请保证视屏格式H264,AAC音频。

如果拍摄的视频播放不了，可以尝试用使用系统录制的项目：[VideoRecord](https://github.com/CarGuo/VideoRecord)
或者使用JAVACV录制的项目：[FFmpegRecorder](https://github.com/CrazyOrr/FFmpegRecorder )，测试视频是否可以播放。

#### 6、m3u8格式视频请关闭cache

缓存不支持m3u8，播放m3u8格式格式需要cacheWithPlay为false

```
setUp(String url, boolean cacheWithPlay····)

```

#### 6、如何在列表暂停，参看主页版本说明 1.4.1



#### 7、如何直接横屏，参看主页版本说明 1.2.5



#### 8、如何从XXX开始播放，参考主页版本说明 1.3.4



#### 9、为什么拖动视屏会弹回来，因为ijk的FFMPEG对关键帧问题，目前无解。



#### 10、列表全屏返回问题，参看demo的list接口。


### 11、播放TAG

TAG是用于判断多个列表下不同播放列表的
position也不要忘记哟

```
gsyVideoPlayer.setPlayTag(TAG);
gsyVideoPlayer.setPlayPosition(position);
```

### 12、虚拟按键

```
/**
 * 全屏隐藏虚拟按键，默认打开
 */
public void setHideKey(boolean hideKey)
```


### 13、缓存清除与配置 1.3.4、1.3.0


### 14、使能和关闭界面的声音、亮度、进度触摸功能

```
/**
 * 是否可以滑动界面改变进度，声音等
 */
public void setIsTouchWiget(boolean isTouchWiget)
```

### 15、设置title和播放参数

多种setup效果，这是其中一类。

```
/**
 * 设置播放URL
 *
 * @param url           播放url
 * @param cacheWithPlay 是否边播边缓存
 * @param cachePath     缓存路径，如果是M3U8或者HLS，请设置为false
 * @param mapHeadData   头部信息
 * @param objects       object[0]目前为title
 * @return
 */
@Override
public boolean setUp(String url, boolean cacheWithPlay, File cachePath, Map<String, String> mapHeadData, Object... objects) {

```