## GSYVideoPlayer FAQ

**[Click to see the Chinese version](QUESTION.md)**

#### ijkplayer mode, you may need a complete list of options: [ff_ffplay_options.h](https://github.com/Bilibili/ijkplayer/blob/cced91e3ae3730f5c63f3605b00d25eafcf5b97b/ijkmedia/ijkplayer/ff_ffplay_options.h)

> Online analysis: https://gpac.github.io/mp4box.js/test/filereader.html

**Third-party summary of ijkplayer issues https://juejin.im/entry/5bc7e7d6e51d450e4f392088**



#### 0. If the dependency is unsuccessful, and it is a jitpack dependency, remember to add the jitpack dependency in the build.gradle file under the project. (jitpack is deprecated)
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}

> If you can't find the dependency and there are missing packages, you can see: https://github.com/CarGuo/GSYVideoPlayer/issues/4144
```
#### 1. Failed to import the project?

There is a dependencies.gradle in the outermost part of the project, and all project dependencies are in it. Then refer to the build.gradle in the root directory of the project. There is apply from: 'dependencies.gradle' at the top, so that gsyVideoPlayer can find the corresponding dependencies. For gradle convenience, you can refer to [Collection of difficult and miscellaneous diseases of Android squatting (and Gradle) II](http://www.jianshu.com/p/86e4b336c17d)

Secondly, because so has five platforms, the remote dependency library is relatively large. When relying, if conditions permit, you can turn on the vpn and use the L2TP protocol, and the dependency download will be faster.

#### 2. ClassNotFoundException and obfuscation

Make sure that your unpacking Application configuration is normal. If obfuscation is enabled, make sure that obfuscation has been added.

And sometimes what you need is to clear it.

```
-keep class tv.danmaku.ijk.** { *; }
-dontwarn tv.danmaku.ijk.**
-keep class com.shuyu.gsyvideoplayer.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.**

```

#### 3. The corresponding so cannot be found or the so is linked incorrectly.

You can configure ndk abiFilters to ensure that there are corresponding so files in the so folder used. Use Analyze Apk to check whether so should be packaged into each folder.

Refer to [#issue23](https://github.com/CarGuo/GSYVideoPlayer/issues/23)

Refer to [#issue24](https://github.com/CarGuo/GSYVideoPlayer/issues/24)

Refer to [ndk filter selection introduction](https://www.diycode.cc/topics/691)

Have you added the following code to gradle
```
android {


        ···

    defaultConfig {
        ···
        ndk {

            //APP's build.gradle sets the supported SO library architecture

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

#### 3. It stopped automatically in full screen

Did you monitor the list sliding and update the list in the monitoring.

#### 4. Normal mode does not support 3gp or mepg, mepg can use ex-so dependency.

If the recorded video cannot be played, you can try to use the system recording project: [VideoRecord](https://github.com/CarGuo/VideoRecord)
Or use the JAVACV recording project: [FFmpegRecorder](https://github.com/CrazyOrr/FFmpegRecorder), to test whether the video can be played.

Black screen related issues:
https://github.com/Bilibili/ijkplayer/issues/2541
https://github.com/Bilibili/ijkplayer/pull/1875


#### 5. m3u8\HLS format video please turn off cache

Cache does not support m3u8\HLS, to play m3u8\HLS format, you need cacheWithPlay to be false

```
setUp(String url, boolean cacheWithPlay····)

```

#### 6. Why does the video pop back when dragging, because of the key frame problem of ijk's FFMPEG.
You can try the following settings
```
VideoOptionModel videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
List<VideoOptionModel> list = new ArrayList<>();
list.add(videoOptionModel);
GSYVideoManager.instance().setOptionModelList(list);
```

#### 7. The video starts again after rotation, configure AndroidManifest.xml.
```
<activity
    android:name=".PlayActivity"
    android:configChanges="keyboard|keyboardHidden|orientation|screenSize|screenLayout|smallestScreenSize|uiMode"
    android:screenOrientation="portrait" />
```

#### 8. The loading speed of a weak network is too slow.
If the network environment is not good, you can try to turn off the cache to play, because the proxy method of playing while caching has high requirements on the network.

#### 9. How to set cookies.
When setUp, set the method with the Map<String, String> mapHeadData parameter, which is actually converted to the setOption method inside Ijk.
You can refer to ijkPlayer's [issues-1150](https://github.com/Bilibili/ijkplayer/issues/1150)

#### 10. For the function of playing multiple fragments, please refer to:
[issue64](https://github.com/CarGuo/GSYVideoPlayer/issues/64)
[issue490](https://github.com/Bilibili/ijkplayer/issues/490)
[Fragmented playback data](http://www.jianshu.com/p/ea794a357b48)

#### 11. There is picture but no sound, there is sound but no picture.
1. This situation is generally because the supported format is not packaged in so. If you need to support the format you want, you can recompile so yourself and add the additional supported format to the module configuration file. There is a compilation tutorial on the github homepage.

[2. Sometimes, TextureView needs to enable hardware acceleration](https://github.com/CarGuo/GSYVideoPlayer/issues/266) such as `android:hardwareAccelerated="true"` in the application tag

#### 12. The video sound and picture are not synchronized.

The simulator does not accept it!

1. Is the hard decoding turned off!

2. Is the config of the Activity configured!

3. What is the resolution and frame rate of the video, and does the machine support it?

Step 3 can be tried by reducing the multiple:

```
VideoOptionModel videoOptionModel =
        new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 50);
List<VideoOptionModel> list = new ArrayList<>();
list.add(videoOptionModel);
GSYVideoManager.instance().setOptionModelList(list);
```

#### 13. The url video file has a Chinese name.

For urls such as http://xxxxxxx.Chinese.mp4, if a http 400 error occurs, please convert the Chinese url to url encoding yourself;
For example, http://tool.oschina.net/encode?type=4 is converted here.


#### 14. rtsp playback failure problem

https://github.com/CarGuo/GSYVideoPlayer/issues/232

https://github.com/CarGuo/GSYVideoPlayer/issues/207


#### 15. m3u8 takes a long time to load after dragging seek

https://github.com/Bilibili/ijkplayer/issues/2874

https://github.com/CarGuo/GSYVideoPlayer/issues/252

#### 16. There is a problem playing local m3u8

```
VideoOptionModel videoOptionModel =
        new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "crypto,file,http,https,tcp,tls,udp");
VideoOptionModel videoOptionModel2 =
        new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "allowed_extensions", "ALL");
List<VideoOptionModel> list = new ArrayList<>();
list.add(videoOptionModel);
list.add(videoOptionModel2);
GSYVideoManager.instance().setOptionModelList(list);
```
#### 17. There is a problem with rtsp connection

```
VideoOptionModel videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");
List<VideoOptionModel> list = new ArrayList<>();
list.add(videoOphtionModel);
GSYVideoManager.instance().setOptionModelList(list);
```

More optimization (one setOption corresponds to VideoOptionModel in gsy)
```
//Hard decoding: 1, on, 0, off
//mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
//Soft decoding: 1, on, 0, off
//mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "videotoolbox", 0);

//rtsp settings https://ffmpeg.org/ffmpeg-protocols.html#rtsp
mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");
mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_flags", "prefer_tcp");

mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "allowed_media_types", "video"); //Configure according to media type
mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "timeout", 20000);
mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "buffer_size", 1316);
mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "infbuf", 1);  // Infinite read
mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100L);
mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 10240L);
mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1L);
//  Turn off player buffering, this must be turned off, otherwise it will get stuck after playing for a period of time, and the console will print FFP_MSG_BUFFERING_START
mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0L);
mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1L);
```
corresponding
```
List<VideoOptionModel> list = new ArrayList<>();
VideoOptionModel videoOptionMode01 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "fast", 1);//No extra optimization
list.add(videoOptionMode01);
VideoOptionModel videoOptionMode02 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 200);//10240
list.add(videoOptionMode02);
VideoOptionModel videoOptionMode03 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1);
list.add(videoOptionMode03);
//pause output until enough packets have been read after stalling
VideoOptionModel videoOptionMode04 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);//Whether to enable buffering
list.add(videoOptionMode04);
//drop frames when cpu is too slow：0-120
VideoOptionModel videoOptionMode05 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);//Frame dropping, if it is too stuck, you can try to drop frames
list.add(videoOptionMode05);
//automatically start playing on prepared
VideoOptionModel videoOptionMode06 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);
list.add(videoOptionMode06);
VideoOptionModel videoOptionMode07 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);//Default value 48
list.add(videoOptionMode07);
//max buffer size should be pre-read：Default is 15*1024*1024
VideoOptionModel videoOptionMode11 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", 0);//Maximum cache number
list.add(videoOptionMode11);
VideoOptionModel videoOptionMode12 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 2);//Default minimum frame number 2
list.add(videoOptionMode12);
VideoOptionModel videoOptionMode13 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max_cached_duration", 30);//Maximum cache duration
list.add(videoOptionMode13);
//input buffer:don't limit the input buffer size (useful with realtime streams)
VideoOptionModel videoOptionMode14 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 1);//Whether to limit the input cache number
list.add(videoOptionMode14);
VideoOptionModel videoOptionMode15 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "nobuffer");
list.add(videoOptionMode15);
VideoOptionModel videoOptionMode16 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");//tcp data transmission
list.add(videoOptionMode16);
VideoOptionModel videoOptionMode17 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzedmaxduration", 100);//Analysis stream duration: default 1024*1000
list.add(videoOptionMode17);

GSYVideoManager.instance().setOptionModelList(list);
```

#### 18. url switching 400/404 (http and https domain names are shared, etc.)

```
        VideoOptionModel videoOptionModel =
                new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
        List<VideoOptionModel> list = new ArrayList<>();
        list.add(videoOptionModel);
        VideoOptionModel videoOptionModel2 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_timeout", -1);
        list.add(videoOptionModel2);
        GSYVideoManager.instance().setOptionModelList(list);

```

#### 19. Synchronization problem between full screen and non-full screen

There are some custom operations that need to be synchronized between full screen and non-full screen switching. The specific operation is to overload the following two methods to implement your own custom operations. For details, please refer to the demo.
To get the current player externally, it is recommended to use `play.getCurPlay().xxxxxx`
```
/**
 * When in full screen, assign the corresponding processing parameter logic to the full screen player
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
 * When exiting full screen, return the corresponding processing parameter logic to the non-player
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

#### 20. Start playing at a precise time

Note that this is a global setting. After setting, you need to clear this item if you don't need it.
```
VideoOptionModel videoOptionModel =
                new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "seek-at-start", startPosition);
        List<VideoOptionModel> list = new ArrayList<>();
        list.add(videoOptionModel);
        GSYVideoManager.instance().setOptionModelList(list);
```

#### 21. AudioManager leak

https://stackoverflow.com/questions/6445052/android-context-memory-leak-listview-due-to-audiomanager

#### 22. Number of reconnections
```
VideoOptionModel videoOptionModel =
                new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER,"reconnect",5);
        List<VideoOptionModel> list = new ArrayList<>();
        list.add(videoOptionModel);
        GSYVideoManager.instance().setOptionModelList(list);
```

#### 22. mkv subtitles in ijk mode

In addition to selecting a track, you can also configure

```
VideoOptionModel videoOptionModel =
 new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "subtitle", 1);
List<VideoOptionModel> list = new ArrayList<>();
list.add(videoOptionModel);
GSYVideoManager.instance().setOptionModelList(list);
```

#### 23. Black screen problem from cover to playback

https://github.com/CarGuo/GSYVideoPlayer/issues/3941#issuecomment-1972409662

### 24. Other issues

https://github.com/CarGuo/GSYVideoPlayer/issues/2997#issuecomment-711480841


### 25. Garbled screen after seek, infinite loop buffer problem

```
invalid dts/pts combination 740157300
```

- When dts has no value, after returning, the decoding state is all reset, then the first frame of information sent should be a key frame, otherwise this frame needs to refer to other frames, resulting in a garbled screen.
- The dts timestamp is wrong, which will cause the dts to be converted to microseconds and always be less than the time passed in by seek, so the packet can never be returned, causing the seek to freeze.
- When judging whether the packet is a key frame, it ignores whether the packet is video. If the packet is audio and the result of flag & AV_PKT_FLAG_KEY is true, the packet will be returned and the seek standard will be cleared. The subsequent video read may also have non-key frames, thus causing a garbled screen.

### 26. Add ijk proxy support
```java
VideoOptionModel videoOptionModel2 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http_proxy", "http://192.168.0.116:8888 ");
List<VideoOptionModel> list = new ArrayList<>();
list.add(videoOptionModel2);
GSYVideoManager.instance().setOptionModelList(list);
```

### 27. ijk has a problem of switching audio tracks [#3790](https://github.com/CarGuo/GSYVideoPlayer/issues/3790)

```java
        VideoOptionModel videoOptionModel2 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 25);
        List<VideoOptionModel> list = new ArrayList<>();
        list.add(videoOptionModel2);
        GSYVideoManager.instance().setOptionModelList(list);
```

```java
       binding.change.setOnClickListener(new View.OnClickListener() {
            int index = 0;

            @Override
            public void onClick(View view) {
                IjkMediaPlayer player =  ((IjkMediaPlayer)((IjkPlayerManager)binding.detailPlayer.getGSYVideoManager().getPlayer() ).getMediaPlayer());
                player.selectTrack(1);
            }
        });
```

## 28.




#### More configurations

For more configurations, please refer to the links and pictures below


##### [ff_ffplay_options](https://github.com/Bilibili/ijkplayer/blob/cced91e3ae3730f5c63f3605b00d25eafcf5b97b/ijkmedia/ijkplayer/ff_ffplay_options.h)

![](https://raw.githubusercontent.com/CarGuo/GSYVideoPlayer/master/img/code/option1.jpg)
![](https://raw.githubusercontent.com/CarGuo/GSYVideoPlayer/master/img/code/option2.jpg)
