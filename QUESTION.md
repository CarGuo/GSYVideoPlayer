## GSYVideoPlayer 问题集锦

#### 0、依赖不成功的，记得在project下的build.gradle文件jitpack的依赖。
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
#### 1、导入项目不成功？

　项目最外部有一个dependencies.gradle，所有的项目依赖都在这里面，然后参考项目根目录的build.gradle，在最顶部有apply from: 'dependencies.gradle'，这样gsyVideoPlayer就可以找到对应的依赖了。gradle方便可参考察[Android蹲坑的疑难杂症集锦（兼Gradle） 二](http://www.jianshu.com/p/86e4b336c17d)

#### 2、ClassNotFoundException和混淆

　确保你的拆包Application配置正常，若是开启混淆，确保混淆已经添加 。

```
-keep class tv.danmaku.ijk.** { *; }
-dontwarn tv.danmaku.ijk.**
-keep class com.shuyu.gsyvideoplayer.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.**

```

#### 3、找不到对应的so

  可以配置ndk abiFilters，确保使用到的so文件夹下都有对用的so文件，用Analyze Apk查看so是否应打包到各个文件夹。
  参考[#issue23](https://github.com/CarGuo/GSYVideoPlayer/issues/23)
  参考[#issue24](https://github.com/CarGuo/GSYVideoPlayer/issues/24)
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

#### 3、全屏的时候自动停止了

　是否监听了列表滑动了，在监听里更新了列表之类的。

#### 4、目前不支持3gp或者mepg，请保证视屏格式H264,AAC音频。

如果拍摄的视频播放不了，可以尝试用使用系统录制的项目：[VideoRecord](https://github.com/CarGuo/VideoRecord)
或者使用JAVACV录制的项目：[FFmpegRecorder](https://github.com/CrazyOrr/FFmpegRecorder )，测试视频是否可以播放。

#### 5、m3u8\HLS的格式视频请关闭cache

缓存不支持m3u8\HLS，播放m3u8\HLS格式，需要cacheWithPlay为false

```
setUp(String url, boolean cacheWithPlay····)

```

#### 6、为什么拖动视屏会弹回来，因为ijk的FFMPEG对关键帧问题，目前无解。

#### 7、视频旋转后重新开始，配置AndroidManifest.xml。
```
<activity
    android:name=".PlayActivity"
    android:configChanges="orientation|keyboardHidden|screenSize"
    android:screenOrientation="portrait" />
```

#### 8、弱网络加载速度太慢。
若网络环境不好，可以尝试关闭缓存来播放，因为proxy方式的边播边缓存对于网络的要求有点高。