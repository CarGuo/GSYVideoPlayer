## GSYVideoPlayer 问题集锦

#### ijkplayer模式的，你可能会需要的option大全 : [ff_ffplay_options.h](https://github.com/Bilibili/ijkplayer/blob/cced91e3ae3730f5c63f3605b00d25eafcf5b97b/ijkmedia/ijkplayer/ff_ffplay_options.h)

**ijkplayer问题第三方总结 https://juejin.im/entry/5bc7e7d6e51d450e4f392088**

#### 0、依赖不成功的，如果是jitpach的依赖，记得在project下的build.gradle文件jitpack的依赖。（已弃用jitpack）
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

  其次，因为so有五个平台，远程依赖库比较大，依赖的时候如果有条件，可以开启vpn，用L2TP协议，依赖下载会快一些。

#### 2、ClassNotFoundException和混淆

　确保你的拆包Application配置正常，若是开启混淆，确保混淆已经添加 。

  而且有时候你需要的是clear一下。

```
-keep class tv.danmaku.ijk.** { *; }
-dontwarn tv.danmaku.ijk.**
-keep class com.shuyu.gsyvideoplayer.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.**

```

#### 3、找不到对应的so或者链接so错误。

  可以配置ndk abiFilters，确保使用到的so文件夹下都有对用的so文件，用Analyze Apk查看so是否应打包到各个文件夹。

  参考[#issue23](https://github.com/CarGuo/GSYVideoPlayer/issues/23)

  参考[#issue24](https://github.com/CarGuo/GSYVideoPlayer/issues/24)

  参考[ndk 过滤选择介绍](https://www.diycode.cc/topics/691)

  是否已经添加下方的代码到gradle
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
sourceSets {
    main {
        jniLibs.srcDirs = ['libs']
    }
}
```

#### 3、全屏的时候自动停止了

　是否监听了列表滑动了，在监听里更新了列表之类的。

#### 4、普通模式不支持3gp或者mepg，mepg可使用ex-so依赖。

如果拍摄的视频播放不了，可以尝试用使用系统录制的项目：[VideoRecord](https://github.com/CarGuo/VideoRecord)
或者使用JAVACV录制的项目：[FFmpegRecorder](https://github.com/CrazyOrr/FFmpegRecorder )，测试视频是否可以播放。

黑屏相关issues：
https://github.com/Bilibili/ijkplayer/issues/2541
https://github.com/Bilibili/ijkplayer/pull/1875


#### 5、m3u8\HLS的格式视频请关闭cache

缓存不支持m3u8\HLS，播放m3u8\HLS格式，需要cacheWithPlay为false

```
setUp(String url, boolean cacheWithPlay····)

```

#### 6、为什么拖动视屏会弹回来，因为ijk的FFMPEG对关键帧问题。
可以尝试以下设置
```
VideoOptionModel videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
List<VideoOptionModel> list = new ArrayList<>();
list.add(videoOptionModel);
GSYVideoManager.instance().setOptionModelList(list);
```

#### 7、视频旋转后重新开始，配置AndroidManifest.xml。
```
<activity
    android:name=".PlayActivity"
    android:configChanges="keyboard|keyboardHidden|orientation|screenSize|screenLayout|smallestScreenSize|uiMode"
    android:screenOrientation="portrait" />
```

#### 8、弱网络加载速度太慢。
若网络环境不好，可以尝试关闭缓存来播放，因为proxy方式的边播边缓存对于网络的要求有点高。

#### 9、如何设置cookie。
在setUp的时候，设置带有 Map<String, String> mapHeadData 参数的方法，在Ijk内部其实就是转为setOption方法。
可参考ijkPlayer的[issues-1150](https://github.com/Bilibili/ijkplayer/issues/1150)

#### 10、多个分片播放的功能，请查阅:
[issue64](https://github.com/CarGuo/GSYVideoPlayer/issues/64)
[issue490](https://github.com/Bilibili/ijkplayer/issues/490)
[分片播放资料](http://www.jianshu.com/p/ea794a357b48)

#### 11、有画面没声音，有声音没画面。
1、这种情况一般都是so里没有打包支持的格式，如果需要支持你想要的格式，可以自己重新编译so，在module配置文件加上需要额外支持的格式。github首页有编译教程。

[2、某些时候，TextureView需要开启硬件加速](https://github.com/CarGuo/GSYVideoPlayer/issues/266) 如application标签中的 `android:hardwareAccelerated="true"`

#### 12、视频声音画面不同步。

模拟器的不接！

1、是否关闭了硬解码！

2、Activity的config是否配置了！

3、视频的分辨率和帧数是多少，机器是否支持的了

第3步可以通过尝试降低倍数：

```
VideoOptionModel videoOptionModel =
        new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 50);
List<VideoOptionModel> list = new ArrayList<>();
list.add(videoOptionModel);
GSYVideoManager.instance().setOptionModelList(list);
```

#### 13、url视频文件中文名。

对于如http://xxxxxxx.中文.mp4的url，如果出现 http 400 error的情况，请自行转换中文url到url编码；
如 http://tool.oschina.net/encode?type=4 这里转化。


#### 14、rtsp播放失败问题

https://github.com/CarGuo/GSYVideoPlayer/issues/232

https://github.com/CarGuo/GSYVideoPlayer/issues/207


#### 15、m3u8拖动seek之后，加载很长时间

https://github.com/Bilibili/ijkplayer/issues/2874

https://github.com/CarGuo/GSYVideoPlayer/issues/252

#### 16、播放本地m3u8有问题

```
VideoOptionModel videoOptionModel =
        new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", crypto,file,http,https,tcp,tls,udp);
List<VideoOptionModel> list = new ArrayList<>();
list.add(videoOptionModel);
GSYVideoManager.instance().setOptionModelList(list);
```
#### 17、rtsp连接有问题

```
VideoOptionModel videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");
List<VideoOptionModel> list = new ArrayList<>();
list.add(videoOphtionModel);
GSYVideoManager.instance().setOptionModelList(list);
```

更多优化（一个setOption对应gsy中VideoOptionModel）
```
//硬解码：1、打开，0、关闭
//mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
//软解码：1、打开，0、关闭
//mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "videotoolbox", 0);

//rtsp设置 https://ffmpeg.org/ffmpeg-protocols.html#rtsp
mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");
mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_flags", "prefer_tcp");

mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "allowed_media_types", "video"); //根据媒体类型来配置
mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "timeout", 20000);
mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "buffer_size", 1316);
mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "infbuf", 1);  // 无限读
mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100L);
mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 10240L);
mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1L);
//  关闭播放器缓冲，这个必须关闭，否则会出现播放一段时间后，一直卡主，控制台打印 FFP_MSG_BUFFERING_START 
mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0L);
mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1L);
```
对应
```
VideoOptionModel videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");
        List<VideoOptionModel> list = new ArrayList<>();
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_flags", "prefer_tcp");
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "allowed_media_types", "video"); //根据媒体类型来配置
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "timeout", 20000);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "buffer_size", 1316);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "infbuf", 1);  // 无限读
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 10240);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1);
        list.add(videoOptionModel);
        //  关闭播放器缓冲，这个必须关闭，否则会出现播放一段时间后，一直卡主，控制台打印 FFP_MSG_BUFFERING_START
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);
        list.add(videoOptionModel);
```

#### 18、url切换400/404（http与https域名共用等）

```
        VideoOptionModel videoOptionModel =
                new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
        List<VideoOptionModel> list = new ArrayList<>();
        list.add(videoOptionModel);
        VideoOptionModel videoOptionModel2 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_timeout", -1);
        list.add(videoOptionModel2);
        GSYVideoManager.instance().setOptionModelList(list);

```

#### 19、全屏与非全屏的同步问题

有一些自定义操作，需要全屏与非全屏切换之间做同步处理，具体操作是重载下面两个方法，实现自己的自定义操作，详细可参考demo。
外部获取当前播放器，推荐使用`play.getCurPlay().xxxxxx`
```
/**
 * 全屏时将对应处理参数逻辑赋给全屏播放器
 *
 * @param context
 * @param actionBar
 * @param statusBar
 * @return
 */
@Override
public GSYBaseVideoPlayer startWindowFullscreen(Context context, boolean actionBar, boolean statusBar) {
    SmartPickVideo sampleVideo = (SmartPickVideo) super.startWindowFullscreen(context, actionBar, statusBar);
    sampleVideo.mSourcePosition = mSourcePosition;
    sampleVideo.mType = mType;
    sampleVideo.mUrlList = mUrlList;
    sampleVideo.mTypeText = mTypeText;
    sampleVideo.mSwitchSize.setText(mTypeText);
    return sampleVideo;
}

/**
 * 推出全屏时将对应处理参数逻辑返回给非播放器
 *
 * @param oldF
 * @param vp
 * @param gsyVideoPlayer
 */
@Override
protected void resolveNormalVideoShow(View oldF, ViewGroup vp, GSYVideoPlayer gsyVideoPlayer) {
    super.resolveNormalVideoShow(oldF, vp, gsyVideoPlayer);
    if (gsyVideoPlayer != null) {
        SmartPickVideo sampleVideo = (SmartPickVideo) gsyVideoPlayer;
        mSourcePosition = sampleVideo.mSourcePosition;
        mType = sampleVideo.mType;
        mTypeText = sampleVideo.mTypeText;
        mSwitchSize.setText(mTypeText);
        setUp(mUrlList, mCache, mCachePath, mTitle);
    }
}
```

#### 20、精准的某个时间开始播放

注意，这个是全局地设置，设置之后如果不需要需要清除这个item。
```
VideoOptionModel videoOptionModel =
                new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "seek-at-start", startPosition);
        List<VideoOptionModel> list = new ArrayList<>();
        list.add(videoOptionModel);
        GSYVideoManager.instance().setOptionModelList(list);
```

#### 21、AudioManager泄漏

https://stackoverflow.com/questions/6445052/android-context-memory-leak-listview-due-to-audiomanager

#### 22、重连次数
```
VideoOptionModel videoOptionModel =
                new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER,"reconnect",5);
        List<VideoOptionModel> list = new ArrayList<>();
        list.add(videoOptionModel);
        GSYVideoManager.instance().setOptionModelList(list);
```

#### 22、ijk 模式的 mkv 字幕

出了选择 track 之外，还可以配置

```
VideoOptionModel videoOptionModel =
 new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "subtitle", 1);
List<VideoOptionModel> list = new ArrayList<>();
list.add(videoOptionModel);
GSYVideoManager.instance().setOptionModelList(list);
```


#### 更多配置

更多配置可通过下方链接和图片参考配置


##### [ff_ffplay_options](https://github.com/Bilibili/ijkplayer/blob/cced91e3ae3730f5c63f3605b00d25eafcf5b97b/ijkmedia/ijkplayer/ff_ffplay_options.h)

![](https://raw.githubusercontent.com/CarGuo/GSYVideoPlayer/master/img/code/option1.jpg)
![](https://raw.githubusercontent.com/CarGuo/GSYVideoPlayer/master/img/code/option2.jpg)
