## The following is a description of each version, which can be used as a simple wiki~, and the effect can be referred to the DEMO.

**[Click to see the Chinese version](UPDATE_VERSION.md)**

### v11.3.0 (2025-12-05)
- link #3019
- fix #4211

### v11.2.0 (2025-11-25)
- fix #4169
- fix #4174
- fix #4171
- add new function with clearVideoSurface [IjkExo2MediaPlayer]
- fix #4199
- fix #4204


### v11.1.0 (2025-08-04)

- update media3 1.8.0

### v11.0.0 (2025-07-10)

- Update and migrate underlying dependencies
- fix #4140


### v10.2.0 (2025-06-03)
- update media 1.7.1
- update aliyun player

###  v10.1.0 (2025-04-01)
- update media 1.6.0
- fix #4078
- link 4075 fix glsurface adapter video rotate info
- Add exo to switch tracks
- miniSdk 21, compileSdk 35


###  v10.0.0 (2024-11-01)
- update media3 1.4.1
- update FFMpeg 4.1.6
- update openssl-1.1.1w
- support 16k page size
- fix #3999 & #3649
- fix #4014
- fix #4019
- fix #4023
- fix #4021
- update AGP

###  v9.0.0-release-jitpack (2024-07-29)
* ex_so support 16k page size
* update media3 1.4.0
* fix #4014
* fix #3999 & #3649 system mediaPlayer setSpeed cause play
* fix #3972 ListGSYVideoPlayer carousel scene focus not removed, causing player status error


###  v8.6.0-release-jitpack (2024-03-07)
* update media 1.3.0
* update exo source intercept listener
* fix #3900


###  v8.5.0-release-jitpack (2023-11-20)
* update media 1.2.0 & compileSdk 34
* update exoplayer to androidx media
* fix #3874
* fix issues 3855 (#3856)
* add #3843


###  v8.4.0-release-jitpack (2023-07-17)
* update exoplayer to androidx media
* fix #3824 with ExoPlaybackException
* update aliyun sdk version
* fix AGP8
* support exoplayer file sink
* fix #3798


### v8.3.5-release-jitpack (2023-03-15)
* update exo 2.18.4
* fix #3773
* fix #3756
* fix #3683

### v8.3.4-release-jitpack (2022-09-01)
* update exo 2.18.1
* fix #3673
* fix #3677
* fix #3676
*
### v8.3.3-release-jitpack (2022-07-14)
* ijk adds support for assets playback
* fix #3625
* fix #3615


### v8.3.2-release-jitpack (2022-06-24)
* update exo 2.18.0
* fix #3608 compatible with secondary screen

### v8.3.1-release-jitpack (2022-06-23)
* fix #3609
* fix #3600

### v8.3.0-release-jitpack (2022-06-06)
* fix #3559 ProxyCacheManager
* remove dependencies: transitionsEverywhere
* fix #3568 HttpProxyCache & HttpUrlSource
* support AliPlayer
* surfaceView supports screenshots
* fix #3580
* feat(Media DataSource): Video playback data source supports data input stream.(Currently only IJK kernel is supported) (#3582)


### v8.2.0-release-jitpack (2022-04-15)
* fix #3542 break change progress time int 2 long
* fix #3546
* fix #3531 clearCache custom path
* update exoplayer sdk 2.17.1

### v8.1.9-release-jitpack(2022-02-14)
* fix #3496
* exo support rtsp

### v8.1.6-jitpack(2021-09-13)

* Add support for horizontal full screen and vertical screen changes, the screen does not rotate, [SimpleActivity](./app/src/main/java/com/example/gsyvideoplayer/simple/SimpleActivity.java) [SimpleDetailActivityMode2](./app/src/main/java/com/example/gsyvideoplayer/simple/SimpleDetailActivityMode2.java)
* Fix the problem that the timeout is set and it becomes invalid after retrying
* Add for some dataBinding scenarios, which will occur when the context detach activity is recycled.
* exo player 2.14.2
* fix DataSource error

### v8.1.4-jitpack(2021-06-16)

* update #3294 fix StorageUtils
* update fix #3275
* update exo '2.14.0'
* update fix #3241

### 8.1.3

* update exoplayer 2.14.0
* Fix known issues
* #3241
* #3275
* ExoSoucrcManager adds `setDatabaseProvider` method
* Modify ExoMediaSourceInterceptListener

```
       ExoSourceManager.setExoMediaSourceInterceptListener(new ExoMediaSourceInterceptListener() {
            @Override
            public MediaSource getMediaSource(String dataSource, boolean preview, boolean cacheEnable, boolean isLooping, File cacheDir) {
                //If it returns null, the default will be used
                return null;
            }

            /**
             * Through a custom HttpDataSource, you can set a self-signed certificate or ignore the certificate
             * The GSYExoHttpDataSourceFactory in the demo uses an ignored certificate
             * */
            @Override
            public DataSource.Factory getHttpDataSourceFactory(String userAgent, @Nullable TransferListener listener, int connectTimeoutMillis, int readTimeoutMillis,
                                                               Map<String, String> mapHeadData, boolean allowCrossProtocolRedirects) {
                //If it returns null, the default will be used
                GSYExoHttpDataSourceFactory factory = new GSYExoHttpDataSourceFactory(userAgent, listener,
                        connectTimeoutMillis,
                        readTimeoutMillis, allowCrossProtocolRedirects);
                factory.setDefaultRequestProperties(mapHeadData);
                return factory;
            }
        });
```


## 8.1.2 (2021-03-29)

* update support exo_player2 = '2.13.2'
* ijk uri.getScheme #3194
* fix error cache server when HostnameVerifier & TrustManager null


## 8.1.1 (2021-03-15)

* #3174 fix HostnameVerifier for google play

## 8.1.0 (2021-02-02)

* fix #3126 crash arm64 with Android11
* update support #3128 pass MotionEvent
* update target 30


## 8.0.0 (2020-12-01)

* fix #3040 CommonUtil gets network information null pointer exception
* update ijk to FFMPEG 4.0

## 7.1.8 (2020-10-26)

* update support exoplayer 2.12.1
* fix #3016, [#3009](https://github.com/CarGuo/GSYVideoPlayer/issues/3009)


## 7.1.6 (2020-09-08)

* fix #2922 deprecated SkipSSLChain, support api custom dataSource
* Because ignoring certificates will cause some Google Play review issues, it is changed to custom support
* If you need to use SkipSSLChain, you can refer to the exosource in the demo
* In addition, you can also customize the required HttpDataSource logic through getHttpDataSourceFactory

```
ExoSourceManager.setExoMediaSourceInterceptListener(new ExoMediaSourceInterceptListener() {
    @Override
    public MediaSource getMediaSource(String dataSource, boolean preview, boolean cacheEnable, boolean isLooping, File cacheDir) {
        //If it returns null, the default will be used
        return null;
    }

    /**
     * Through a custom HttpDataSource, you can set a self-signed certificate or ignore the certificate
     * The GSYExoHttpDataSourceFactory in the demo uses an ignored certificate
     * */
    @Override
    public HttpDataSource.BaseFactory getHttpDataSourceFactory(String userAgent, @Nullable TransferListener listener, int connectTimeoutMillis, int readTimeoutMillis, boolean allowCrossProtocolRedirects) {
        //If it returns null, the default will be used
        return new GSYExoHttpDataSourceFactory(userAgent, listener,
                connectTimeoutMillis,
                readTimeoutMillis, allowCrossProtocolRedirects);
    }
});
```


## 7.1.5 (2020-07-30)

* fix #2625 add WeakReference<Activity>
* fix auto full issue
* fix #2813, #2753, #2766


## 7.1.4 (2020-05-14)

* fix #2719 support Exo User-Agent
* fix #2559
* update ex_so lib
* fix proxy cache support Android Q
* proxy cache skip ssl error
* add ProxyCacheManager support DEFAULT_MAX_SIZE
* add ProxyCacheManager FileNameGenerator support custom cache file name
* add touchLongPress Api


## 7.1.3 (2020-03-19)

* update exoplayer 2.11.3
* fix #2588 setOverrideExtension method full screen invalid problem
* fix #2570 add OrientationOption to increase rotation sensitivity adjustment
* add isShowDragProgressTextOnSeekBar to determine whether to display the drag progress at the beginning of the seekbar when dragging the progress bar
* exo kernel adds support for external subtitles
* fix #2456 and update from [1869#issuecomment-569615314](https://github.com/CarGuo/GSYVideoPlayer/issues/1869#issuecomment-569615314)
* fix #2489 optimize network monitoring
* fix #2480 fix screen rotation problem

### 7.1.2(2019-12-02)

* fix #2436 add exo's http timeout
```
ExoSourceManager

 public static void setHttpReadTimeout(int httpReadTimeout)

 public static void setHttpConnectTimeout(int httpConnectTimeout)

```
* Optimize video size display calculation
* Add exo to support raw file playback

```
String url =  RawResourceDataSource.buildRawResourceUri(R.raw.test).toString();
```
* Add a demo of simulating downloading and sharing cache DetailDownloadPlayer
* Adapt to the full screen of android 10
* fix #2382, #2411, #2343, #2379, #2350, #2328
* Add support for setting custom display ratio

```
GSYVideoType.setScreenScaleRatio
```
* Add an example of external subtitles [Support for custom external subtitles in exo2 mode](https://github.com/CarGuo/GSYVideoPlayer/tree/master/app/src/main/java/com/example/gsyvideoplayer/exosubtitle)




### 7.1.1(2019-10-12)

* fix #2244, #2252 (resolveFullVideoShow not executed), #2279, #2280
* fix #2303 (remove TimerTask), #2306 (some models do not display when returning to the background)
* Add setNeedAutoAdaptation
```
    /**
     * Whether to adapt to the problem that the title display is covered due to the space occupied by the notch screen or the perforated screen in vertical and horizontal screens
     *
     * @param needAutoAdaptation default false
     */
    public void setNeedAutoAdaptation(boolean needAutoAdaptation)
```


### 7.1.0(2019-09-01)

* update ExoPlayer to 2.10.4
* Add immersive support
* Add IPlayerInitSuccessListener player initialization success callback
```
GSYVideoManager
    .instance()
    .setPlayerInitSuccessListener(new IPlayerInitSuccessListener() {
        ///Player initialization success callback, which can be used for custom settings before playback
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
* Add hard decoding without garbled screen [RecyclerView3Activity](https://github.com/CarGuo/GSYVideoPlayer/blob/master/app/src/main/java/com/example/gsyvideoplayer/RecyclerView3Activity.java)



### 7.0.2(2019-07-01)
* update ExoPlayer to 2.10.0
* Add allowCrossProtocolRedirects

```
Map<String, String> header = new HashMap<>();
        header.put("allowCrossProtocolRedirects", "true");

 xxx.setMapHeadData(header)
```

* Adjust the internal method of onVideoResume
* Modify the default brightness layout and layout compatibility issues
* Upgrade some dependencies
* exo player setSeekParameter

```
 //Set the adjacent frame of seek.
if(detailPlayer.getGSYVideoManager().getPlayer() instanceof Exo2PlayerManager) {
    ((Exo2PlayerManager) detailPlayer.getGSYVideoManager().getPlayer()).setSeekParameter(SeekParameters.NEXT_SYNC);
    Debuger.printfError("***** setSeekParameter **** ");
}
```

### 7.0.1(2019-04-07)
* Upgrade ExoPlayer to 2.9.6
* ExoPlayer adds SSL certificate ignore support
```
ExoSourceManager.setSkipSSLChain(true);
```
* Fix the problem of pressing the back button during the full-screen animation #1938
* Fix the problem of the pop-up window disappearing in full screen #1927
* Fix the audio focus problem during the full-screen switching process #1912
* Fix the button null judgment problem #1919
* Fix the release problem of the surface when switching to full screen


### 7.0.0-beta1(2019-03-03)
* orientation add pause
```
 orientationUtils.setIsPause(true);
```
* update exoPlayer to 2.9.5.
* exoPlayer and mediaPlayer support network speed display.
* Fix some problems.
* Support library switch to androidx


### 6.0.3(2019-01-15)

* update exoPlayer to 2.9.3
* update gradle 3.3.0
* update build sdk 28
* update support sdk 27.1.1
* Fix some problems with the exoplayer kernel.



### 6.0.2(2018-12-21)

* update exoPlayer to 2.9.1
* Deprecated setupLazy
* fix exoPlayer looper
* add `overrideExtension` to exoPlayer


### 6.0.1 (2018-10-14)
* Officially release version 6.0, adjust the player and cache loading mode.

```
PlayerFactory.setPlayManager(Exo2PlayerManager.class);//EXO mode
PlayerFactory.setPlayManager(SystemPlayerManager.class);//System mode
PlayerFactory.setPlayManager(IjkPlayerManager.class);//ijk mode

CacheFactory.setCacheManager(ExoPlayerCacheManager.class);//exo cache mode, supports m3u8, only supports exo
CacheFactory.setCacheManager(ProxyCacheManager.class);//Proxy cache mode, supports all modes, does not support m3u8, etc.
```
* Fix the problem that the ProxyCacheManager header setting is invalid.
* Remove useless resources.
* Fix a memory leak problem in a certain scenario.

### 6.0.0-beta (2018-08-22)
* Upgrade ExoPlayer to 2.8.4.
* Fix the problem that the header information does not exist when the proxy is cached.
* Adjust the code structure, remove the kernel switching in GSYVideoType, and load it directly through PlayerFactory.
* Adjust the code structure, ExoPlayer can be depended on separately, and loaded through PlayerFactory, which is more convenient for custom PlayerManager.

```
//PlayerFactory.setPlayManager(new Exo2PlayerManager());//EXO mode
//PlayerFactory.setPlayManager(new SystemPlayerManager());//System mode
//PlayerFactory.setPlayManager(new IjkPlayerManager());//ijk mode
```

* Adjust the code structure, CacheFactory is more convenient to customize, the default is ProxyCacheManager.

```
//CacheFactory.setCacheManager(new ExoPlayerCacheManager());//exo cache mode, supports m3u8, only supports exo
//CacheFactory.setCacheManager(new ProxyCacheManager());//Proxy cache mode, supports all modes, does not support m3u8, etc.
```

* Add ExoMediaSourceInterceptListener to facilitate the use of custom MediaSource in Exo mode.

```
ExoSourceManager.setExoMediaSourceInterceptListener(new ExoMediaSourceInterceptListener() {
           /**
            * @param dataSource link
            * @param preview Whether to bring the header, the default is true if there is a header
            * @param cacheEnable Whether to cache
            * @param isLooping Whether to loop
            * @param cacheDir Custom cache directory
            * @return When the return is not empty, use the returned custom mediaSource
            */
            @Override
            public MediaSource getMediaSource(String dataSource, boolean preview, boolean cacheEnable, boolean isLooping, File cacheDir) {
                return null;
            }
});
```



### 5.0.2 (2018-08-01)
* Fix the problem of following the screen rotation.
* Modify the handling of Audio conflicts, subclasses can override the method to handle it separately.
* fix #1300
* The default pause image is changed to false



### 5.0.1(2018-07-01)
* Update ListGSYVideoPlayer
* ijkPlayer's ex_so adds avi support
* update ExoPlayer to 2.8.2
* Fix problems in ExoPlayer mode

### 5.0.0-beta(2018-05-24)
* Adjust the cache mechanism and separate the cache manager.
* update ExoPlayer to 2.8.0, fully optimize for ExoPlayer
* Optimize ExoPlayer's problems
* Add ExoPlayer loop playback support
* Add ExoPlayer custom DEMO to demonstrate seamless switching of ExoPlayer
* ijk's `logLevel` and `ijkLibLoader` are set directly through the IJKPlayerManager static method
* Further adjust the framework structure and internal coupling


### 4.1.3(2018-05-11)
* Optimize single and double clicks
* update support and build sdk to 27
* Add whether to automatically select vertical full screen or horizontal full screen according to the video size. Note that the default rotation is invalid at this time.
```
/**
  * Whether to automatically select vertical full screen or horizontal full screen according to the video size. Note that the default rotation is invalid at this time.
  * @param autoFullWithSize default false
  */
 public void setAutoFullWithSize(boolean autoFullWithSize)
```


### 4.1.2(2018-04-14)
* Fix known issues.
* Add ijkplayer's raw playback support.
```
String url = "android.resource://" + getPackageName() + "/" + R.raw.test;
GSYVideoManager.instance().enableRawPlay(getApplicationContext());
```
* danmku branch provides a network barrage demo

### 4.1.1 (2018-04-01)
* 1. update support lib to 26.0.2
* 2. Fix the problem that the rendering layer returns the wrong size when taking a screenshot under certain conditions.
* 3. Some detailed optimization processing.
* 4. Add Manager's isFullState method
```
 /*
  * Whether it is currently in full screen state
  *
  * @return Whether it is currently in full screen state, true means yes.
  */
 public static boolean isFullState(Activity activity)
```

### 4.1.0 (2018-02-26)
* 1. update to ijk 0.8.8
* 2. Remove the log library dependency of the cache module
* 3. Remove useless dependencies of the exo module
* 4. Add resume playback method parameters
```
 XXXXManager related
/**
  * Resume paused state
  *
  * @param seek Whether to generate a seek action, live broadcast is set to false
  */
 public static void onResume(String key, boolean seek)

 Video related
 /**
  * Resume paused state
  *
  * @param seek Whether to generate a seek action
  */
 @Override
 public void onVideoResume(boolean seek)

```

### 4.0.0-beat1 (2018-02-06)
* 1. Add simple opening ad support.
`GSYSampleADVideoPlayer and DetailADPlayer`
* 2. Optimize ListGSYVideoPlayer and add `playNext()` interface.
* 3. Optimize the code structure and adjust some API interfaces (slightly adjust, occasionally incompatible with the old version, refer to the source code and demo to modify the method name).
* 4. Add GSYVideoHelper video helper class to save more resources.
* 5. Add GSYSampleCallBack to save inheritance and optimize the callback of GSYVideoProgressListener.
* 6. Add GSYVideoViewBridge and override the `getGSYVideoManager()` method to implement your own Manager.
* 7. Support custom rendering layer, `CustomRenderVideoPlayer` in the demo demonstrates how to set a custom rendering layer.
* 8. `ListMultiVideoActivity` and `MultiSampleVideo` demonstrate how to play multiple videos at the same time.
* 9. `DetailADPlayer2` and `ListADVideoActivity` demonstrate ad and intermediate insertion ad support.
* 10. Add audio focus method.
```
/**
  * Long time loss of audio focus, pause the player
  *
  * @param releaseWhenLossAudio default true, only pause when false
  */
 public void setReleaseWhenLossAudio(boolean releaseWhenLossAudio)

```


### 3.0.0 (2018-01-14)

1. Add PlayerManager, update to ExoPlayer2, and optimize support for ExoPlayer2.

2. Add system player AndroidMediaPlayer support

3. Add setUpLazy method for the list to optimize possible sliding lag in the list
```
    /**
     * The real setup is performed when the play is clicked
     */
    public boolean setUpLazy(String url, boolean cacheWithPlay, File cachePath, Map<String, String> mapHeadData, String title)

```
4. Optimize GL rendering and handle crash when switching rendering effects.



5. DEMO adds SamllVideoHelper to implement small window logic and update the demo



6. Optimize the pop-up box for touch volume, brightness, and progress, and optimize the degree of customization
```
    /**
     * layoutId of the touch progress dialog
     * Can be returned by overriding after inheritance
     * If there is a custom implementation logic, you can override the showProgressDialog method
     */
    protected int getProgressDialogLayoutId()
    /**
     * progress bar id of the touch progress dialog
     * Can be returned by overriding after inheritance, if not, it can be empty
     * If there is a custom implementation logic, you can override the showProgressDialog method
     */
    protected int getProgressDialogProgressId()

    /**
     * Current time text of the touch progress dialog
     * Can be returned by overriding after inheritance, if not, it can be empty
     * If there is a custom implementation logic, you can override the showProgressDialog method
     */
    protected int getProgressDialogCurrentDurationTextId()

    /**
     * Total time text of the touch progress dialog
     * Can be returned by overriding after inheritance, if not, it can be empty
     * If there is a custom implementation logic, you can override the showProgressDialog method
     */
    protected int getProgressDialogAllDurationTextId()

    /**
     * image id of the touch progress dialog
     * Can be returned by overriding after inheritance, if not, it can be empty
     * If there is a custom implementation logic, you can override the showProgressDialog method
     */
    protected int getProgressDialogImageId()

    /**
     * layoutId of the volume dialog
     * Can be returned by overriding after inheritance
     * If there is a custom implementation logic, you can override the showVolumeDialog method
     */
    protected int getVolumeLayoutId()
    /**
     * percentage progress bar id of the volume dialog
     * Can be returned by overriding after inheritance, if not, it can be empty
     * If there is a custom implementation logic, you can override the showVolumeDialog method
     */
    protected int getVolumeProgressId()

    /**
     * layoutId of the brightness dialog
     * Can be returned by overriding after inheritance
     * If there is a custom implementation logic, you can override the showBrightnessDialog method
     */
    protected int getBrightnessLayoutId()

    /**
     * percentage text id of the brightness dialog
     * Can be returned by overriding after inheritance, if not, it can be empty
     * If there is a custom implementation logic, you can override the showBrightnessDialog method
     */
    protected int getBrightnessTextId()

```


### 2.1.3 (2017-12-24)
* update demo gradle to 4.1
* Add support and demo for CollapsingToolbarLayout
* Small window playback in multiple windows (including desktop) (WindowActivity).
* Add playback progress callback
```
/**
 * Progress callback
 */
public void setGSYVideoProgressListener(GSYVideoProgressListener videoProgressListener)
```

### 2.1.2(2017-12-08)
* Add handling for calling OnVideoPause before Prepared
* The background video is blurred and filled, and the foreground video is played normally
```
Commented in DetailFilterActivity
//Gaussian stretch video to fill the background, replace black, and play at normal ratio in the foreground
```


### 2.1.1(2017-10-29)
* videoCache mode supports adding headers
* Add seamless video switching DEMO SmartPickVideo
* Adjust some code paths and optimize the code
* log input level interface
```
GSYVideoManager.instance().setLogLevel(IjkMediaPlayer.IJK_LOG_SILENT);
```

### 2.1.0(2017-10-10)
* Added the function of composing video frames into gif (in DEMO DetailControlActivity).
* update ijkplayer 0.84

### 2.0.9(2017-10-02)
* Add animation effects for top-level effect rendering.
* Add screenshot function.
* Add custom render support.
* Add watermark, multiple playback, etc.


### 2.0.8 (2017-09-17)
* Add GSYBaseActivityDetail abstract class to facilitate detail mode integration.
* Add some optimizations internally.
* Add simple filter function support.
```
1. Global settings
GSYVideoType.setRenderType(GSYVideoType.GLSURFACE);
2. Set filter
player.setEffectFilter(new BarrelBlurEffect());
```


### 2.0.7(2017-09-13)

* Optimized and added automatic reconnection after network disconnection, you need to add "ijkhttphook:http://ssss" before http
* update ijk to 0.8.3
* Added precise positioning of seekto in the demo to solve the problem of some videos playing from the beginning after seek

### 2.0.6(2017-08-31)
* Adjusted the display problem of the back button.
* Fixed the problem that the buffer may not disappear in full screen.
* Optimized the double-click problem.

### 2.0.5(2017-08-26)
* Add double-click to pause and start.
* Add support for SurfaceView: GSYVideoType.setRenderType(GSYVideoType.SUFRACE).
* Optimized touch problems, memory problems, and dismisstime problems.

### 2.0.4(2017-08-08)
* Added empty playback UI support.
* Adjusted GSYVideoOptionBuilder.
* Fixed known issues.
* Added an interface to adjust the playback speed during playback.
```
public void setSpeedPlaying(float speed, boolean soundTouch)
```

### 2.0.3(2017-08-06)
* update ijk to 0.8.2
* fix rtsp playback problem
* fix small window playback problem
* Adjusted some code logic and structure.

### 2.0.2(2017-07-16)
* Perfectly realize that there is no black screen and no sudden change in playback, pause, front and back switching, screen adjustment, etc., and delete the coverImageView class.
* Added pitch-shifting and constant-speed interface under 6.0
* update ijkPlayer to 0.8.1

### 2.0.1(2017-07-11)
* Optimized TextureView display
* Fixed the pause problem of SampleView


### 2.0.0(2017-07-10)
* The project structure has been adjusted and new so support has been added.

### 1.6.9(2017-07-08)

* Modify the setup parameters.
* Upgrade and modify all callback interfaces, and return the current player in the callback interface.
* Fix the error of playing local files and deleting files by mistake.
* Compatible with Appbar, thanks to [@loveRose](https://github.com/loveRose)
* The non-full-screen player can get the full-screen player object.
```
/**
 * Get the full-screen player object
 *
 * @return GSYVideoPlayer returns null if there is none.
 */
public GSYVideoPlayer getFullWindowPlayer()
```

### 1.6.8(2017-06-27)
* fix listVideoUtils title garbled problem
* fix the problem that setSpeed cannot be reset
* fix the problem that the network cannot continue to play after switching
* Add whether to follow the system settings after enabling rotation
```
/**
 * Whether to follow the system rotation, if false, it will rotate even if the system prohibits rotation
 * @param rotateWithSystem default true
 */
public void setRotateWithSystem(boolean rotateWithSystem)
```

### 1.6.7(2017-06-16)
* fix bug #265, the virtual button display problem after pressing the back button in full screen
* so compilation configuration adds protocol crypto
* Add an interface to set the disappearance time of the touch display control UI
```
StandardGSYVideoPlayer.java
/**
 * Set the disappearance time of the touch display control UI
 * @param dismissControlTime milliseconds, default 2500
 */
public void setDismissControlTime(int dismissControlTime)
```
* Adjust the ratio of touch sliding fast forward
```
/**
 * Adjust the ratio of touch sliding fast forward
 * @param seekRatio The ratio of sliding fast forward, the default is 1. The larger the value, the smaller the seek generated by sliding
 */
public void setSeekRatio(float seekRatio)
```
* Added configuration for stretching and filling
```
GSYVideoType.java
//Full-screen stretching display, when using this attribute, it is recommended to use FrameLayout for surface_container
public final static int SCREEN_MATCH_FULL = -4;
```

### 1.6.6(2017-05-24)
* update ijkplayer to 0.8.0
* update videocache to 2.7.0

### 1.6.5(2017-05-05)
* Add mirror rotation demo SampleVideo
* Modified the UI problem of loop playback
* Modified the problem of displaying progress for local files or cached files
* Fixed the problem of horizontal and vertical screens
* GSYVideoType adds SCREEN_TYPE_FULL type, which achieves full screen by cropping and enlarging the video according to the ratio
* Add setShowPauseCover interface

```
/**
 * Whether to load and display the paused cover image
 * When it is on, pause and go to the background, and then return to the foreground without a black screen, but it may cause OOM on some models
 * When it is off, pause and go to the background, and then return to the foreground with a black screen
 *
 * @param showPauseCover default true
 */
public void setShowPauseCover(boolean showPauseCover)
```

### 1.6.4(2017-04-20)
* update ijk to 0.7.9 (added soundTouch, the problem of sound changing after speed adjustment is solved)
* Fixed possible null judgment problems and synchronization problems of ListGSYVideoPlayer
* Fixed the problem that the movable small window cannot be moved after the playback ends

### 1.6.3(2017-04-15)
* Modified the problem of selecting 90 degrees when the player is in full screen
* Modified the possible stretching problem of the player
* Add a demo and library support for rotating the playback screen

### 1.6.2(2017-04-05)
* Remove useless code
* Fixed the problem of displaying a small white dot on the dynamic play button
* Added NormalGSYVideoPlayer (a player that uses normal pictures as play buttons and system loading)
* Added a method to dynamically load so
* Added setIsTouchWigetFull method, which can also prohibit the fast forward, sound, and brightness adjustment logic generated by sliding in full screen
```
/**
 * Set a custom so package loading class
 * Needs to be set before instance
 */
public static void setIjkLibLoader(IjkLibLoader libLoader)
```
```
/**
 * Whether the full-screen sliding interface can change the progress, sound, etc.
 * Default true
 */
public void setIsTouchWigetFull(boolean isTouchWigetFull)
```
### 1.6.1(2017-03-23)
* The setSpeed interface is modified to support setting during playback
* Memory optimization
* update ijk to 0.7.8.1
* Add timeout interface GSYVideoManager
```
/**
 * Whether to add an external timeout judgment when buffering, it has no effect on the timeout at the beginning
 *
 * After the timeout, the onError interface will be called, and the player will call back through onPlayError
 *
 * The error code is: BUFFER_TIME_OUT_ERROR = -192
 *
 * Since GSYVideoPlayer's OnError is executed after onError, if you don't want to trigger an error,
 * you can override onError and intercept and handle it before super.
 *
 * public void onError(int what, int extra){
 *     do you want before super and return;
 *     super.onError(what, extra)
 * }
 *
 * @param timeOut Timeout time, milliseconds, default 8000
 * @param needTimeOutOther Whether to delay the setting, default is off
 */
public void setTimeOut(int timeOut, boolean needTimeOutOther) {
    this.timeOut = timeOut;
    this.needTimeOutOther = needTimeOutOther;
}

```

### 1.6.0 (2017-02-19)
* update ijkplayer to 0.7.7.1.
* Added a barrage demo, mainly to demonstrate how to quickly integrate the barrage function.
* Fixed the problem that the loading may not disappear during playback.
* Fixed the problem of incorrect image display in full screen and exiting full screen.
* The image resources of the full-screen switch button support customization.

```
/**
 * Set the button resource for switching to full screen in the lower right corner
 * Must be set before setUp
 * If not set, the default is used
 */
public void setEnlargeImageRes(int mEnlargeImageRes)


/**
 * Set the button resource for exiting full screen in the lower right corner
 * Must be set before setUp
 * If not set, the default is used
 */
public void setShrinkImageRes(int mShrinkImageRes)

```


### 1.5.9
* update ijkplayer to 0.7.7
* update build.gradle to 2.2.3

### 1.5.8
* Fixed the problem with the lock screen touch function after the playback ends.

### 1.5.7
* change AudioManger get.

### 1.5.6
* Fixed the problem that mUrl is empty after error.
* Added the option configuration interface of GSYVideoManager.

```
/**
 * Set the option of IJK video
 */
public void setOptionModelList(List<VideoOptionModel> optionModelList)
```

### 1.5.5
* update ijk 0.7.6.
* Fast and slow playback interfaces support M and below.


### 1.5.4
* Added mute playback interface.

Under GSYVideoManager
Can refer to: ListNormalAdapter
```
/**
 * Whether to mute
 */
public void setNeedMute(boolean needMute)
```

### 1.5.3
* Fixed the problem that the default loading click will be reset during buffering.
* The virtual button will be automatically hidden after a period of time after it pops up.

## 1.5.2
* Added Error callback interface.
* Fixed the compatibility problem of Demo's PlayActivity

### 1.5.1
* The problem that the virtual button affects the progress when sliding in full screen.
* Optimized the pop-up dialog for sliding.
* Fixed some problems.

### 1.5.0
* Added support for using two sets of layouts in full screen and normal playback, and added a demo: LandLayoutVideo.
* Fixed a problem with the recyclerView in the DEMO.
* Fixed some bugs.
* Added a WebView sliding demo.

```

/**
 * If you need to use: support for using two sets of layouts in full screen and normal playback.
 * Then please remember to override the following constructor when overriding the player
 */
public XXXXXXXXXX(Context context, Boolean fullFlag) {
    super(context, fullFlag);
}

····

//This must be configured with the above constructor to take effect
@Override
public int getLayoutId() {
    if (mIfCurrentIsFullscreen) {
        return R.layout.sample_video_land;
    }
    return R.layout.sample_video;
}

```
### 1.4.9
* Added support for continuous playback list ListGSYVideoPlayer.
* Added a list playback demo DetailListPlayer.
* Reduced the size of the https version of so.


### 1.4.8
* The lock screen button adds a lock screen rotation function.
* The lock screen button adds a callback interface.
* Fixed a problem with horizontal screen.

### 1.4.7
* Fixed the problem of flashing when directly in horizontal screen.
* Modified the interface of the traffic prompt.
* Added HTTPS support.

```
/**
 * Whether to display the traffic prompt, the default is true
 */
public void setNeedShowWifiTip(boolean needShowWifiTip)
```

### 1.4.6
* Fixed the problem that the interface display is incorrect after returning to full screen on some mobile phones with virtual buttons.
* Added a test version of CustomGSYVideoPlayer to achieve the effect of previewing the progress bar by sliding (test effect).
* Fixed the problem of dividing by 0 and the invalid UI style adjustment in full screen.
* Note: CustomGSYVideoPlayer currently has better support for fully cached videos or local files, and "poor" support for pure network videos.

```
/**
 * If you need to preview the progress bar, set it to on, the default is off
 */
public void setOpenPreView(boolean localFile)
```

### 1.4.5
* Support switching between IJKPlayer and EXOPlayer, but the problem of EXOPlayer playing in the background and returning to the foreground with a black screen has no solution except seekto.

GSYVideoManager

```
/**
 * Set the playback type of the video
 * GSYVideoType IJKPLAYER = 0 or IJKEXOPLAYER = 1;
 */
public void setVideoType(Context context, int videoType)
```

### 1.4.4

* Adjust the lib, SampleVideo in the DEMO adds support for adjusting the clarity, and the DEMO borrows the URL of jjdxm_ijkplayer.
* Optimized the display between Cache cache and IJK cache.


### 1.4.3

* Added setting display ratio GSYVideoType.
* DEMO adds SampleVideo, which is used in PlayActivity to adjust the display ratio effect.
* Added interfaces to enable and disable hard decoding GSYVideoType.

GSYVideoType
```
/**
 * Set display ratio
 */
public static void setShowType(int type)

/**
 * Enable hard decoding, set before playback
 */
public static void enableMediaCodec() {
    MEDIA_CODEC_FLAG = true;
}

/**
 * Disable hard decoding, set before playback
 */
public static void disableMediaCodec()
```

### 1.4.2

* Fixed the problem of deformation of the paused screen when playing rotated video/vertical screen.
* Adjusted the sensitivity of the brightness and optimized the brightness adjustment.

### 1.4.1

* Added a full-screen lock switch, the screen click is invalid after locking.
* Added global pause and play, supporting list status.
* Fixed the problem of brightness adjustment.

StandardGSYVideoPlayer/ListVideoUtil

```
/**
 * Whether to need full-screen lock screen function
 * If used alone, please set setIfCurrentIsFullscreen to true
 */
public void setNeedLockFull(boolean needLoadFull)
```

GSYVideoManager

```
/**
 * Pause playback
 */
public static void onPause()

/**
 * Resume playback
 */
public static void onResume()
```


### 1.4.0 (3.8 and 3.9 were difficult to produce)

* Added support for lib cover to reuse cover and demo.
* Fixed the buffer progress bar;
* Added recyclerViewDemo.
* update VideoCache, remove error out put log output.
* Fixed the conflict between hiding the virtual keyboard and the actionbar in the list.



### 1.3.7
* Optimized the pop-up box.
* Optimized the problem of black screen when paused (full screen/restore full screen/exit to).
* Solved the problem of dragging the progress bar when paused.


### 1.3.6
* Differentiated the prompts for no network and no wifi.
* Updated the Demo detailPlayer to directly rotate to full screen.
* Return to the normal details effect.


### 1.3.5
* Added full-screen hiding of virtual buttons.
* Fixed the problem that the loading animation stopped during buffering.

```
/**
 * Hide virtual buttons in full screen, default is on
 */
public void setHideKey(boolean hideKey)
```

### 1.3.4
* Added an interface to clear the default cache.
* Added playback offset.
* Optimized the problem of playback time jumping caused by dragging the progress bar or caching.

GSYVideoManager

```
/**
 * Delete all default cache files
 */
public static void clearAllDefaultCache(Context context)

/**
 * Delete the default cache file corresponding to the url
 */
public static void clearDefaultCache(Context context, String url)
```

GSYVideoPlayer

```

/**
 * Clear the current cache
 */
public void clearCurrentCache()

/**
 * Where to start playing
 * At present, there is a problem of jumping in the first few seconds
 */
public void setSeekOnStart(int seekOnStart)

```

### 1.3.3

* Optimized some memory leak problems.
* Updated the demo.

### 1.3.2

* Solved the problem that the actionbar failed to hide due to compatibility with FragmentActivity.

### 1.3.1
* Updated the null judgment of lastListener.

### 1.3.0
* Support configuring cache path and adding some interfaces of ListVideoUtils.

Normal mode

```
//Default cache path method
holder.gsyVideoPlayer.setUp(url, true , "");

···

//The video cache path of a list is the same
holder.gsyVideoPlayer.setUp(url, true, new File(FileUtils.getTestPath(), ""));

···

//If the cache paths in a list are different, you need to use the following method

//To avoid being unavailable when returning from full screen, only initialize the UI that is not at the current position
if (playPosition < 0 || playPosition != position ||
        !GSYVideoManager.instance().getPlayTag().equals(ListNormalAdapter.TAG)) {
    holder.gsyVideoPlayer.initUIState();
}

//If you set that you can play by clicking the cover, if the cache list path is inconsistent, you also need to set the cover click
holder.gsyVideoPlayer.setThumbPlay(true);

holder.gsyVideoPlayer.getStartButton().setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        //Need to switch the cache path
        holder.gsyVideoPlayer.setUp(url, true, new File(FileUtils.getTestPath(), ""));
        holder.gsyVideoPlayer.startPlayLogic();
    }
});

holder.gsyVideoPlayer.getThumbImageViewLayout().setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        //Need to switch the cache path
        holder.gsyVideoPlayer.setUp(url, true, new File(FileUtils.getTestPath(), ""));
        holder.gsyVideoPlayer.startPlayLogic();
    }
});
```
ListVideoUtils

```
public void setCachePath(File cachePath)

public void setObjects(Object[] objects)

public void setMapHeadData(Map<String, String> mapHeadData)

```


### 1.2.9

* Added an interface for download speed.

```
/**
 * Network speed
 * Note that if caching is enabled here, because the local proxy is read, there is still speed after the cache is successful
 * When you open the cached local file again, the network speed will return to 0. Because you are playing a local file
 */
public long getNetSpeed()

/**
 * Network speed
 * Note that if caching is enabled here, because the local proxy is read, there is still speed after the cache is successful
 * When you open the cached local file again, the network speed will return to 0. Because you are playing a local file
 */
public String getNetSpeedText()

```

### 1.2.8

* Upgrade IJKPlayer to 0.7.5.
* Added changing playback speed (speed of 0-2), but only supports 6.0 and above.

```
/**
 * Playback speed
 */
public void setSpeed(float speed)
```

### 1.2.7
* Modified that the control UI is not popped up when replaying in loop playback.
* Modified the actionBar problem of FragmentActivity.


### 1.2.6

* Fixed the full-screen callback problem of the StandardGSYVideoPlayer interface.
* Added a loop playback interface.

```

public void setLooping(boolean looping)

```


### 1.2.5

* Added a new interface to support direct horizontal screen lock interface.
* Turn off full-screen animation and use the combined interface.

##### GSYVideoPlayer

```
/**
 * Full-screen animation
 *
 * @param showFullAnimation Whether to use full-screen animation effect
 */
public void setShowFullAnimation(boolean showFullAnimation)

/**
 * Whether to enable automatic rotation
 */
public void setRotateViewAuto(boolean rotateViewAuto)

/**
 * Lock the screen horizontally as soon as it is full screen, the default is false for vertical screen, can be used with setRotateViewAuto
 */
public void setLockLand(boolean lockLand)
```

##### ListVideoUtil

```
/**
 * Whether to rotate automatically
 *
 * @param autoRotation Whether to support gravity rotation
 */
public void setAutoRotation(boolean autoRotation) {
    this.autoRotation = autoRotation;
}

/**
 * Whether to be horizontal immediately in full screen
 *
 * @param fullLandFrist If so, it will switch to horizontal screen when in full screen
 */
public void setFullLandFrist(boolean fullLandFrist) {
    this.fullLandFrist = fullLandFrist;
}

/**
 * Full-screen animation
 *
 * @param showFullAnimation Whether to use full-screen animation effect
 */
public void setShowFullAnimation(boolean showFullAnimation) {
    this.showFullAnimation = showFullAnimation;
}
```


### 1.2.4

* Compatible API modified to 16, full-screen animation compatible with all APIs.
　

### 1.2.3

* Added dependencies of X86 type, individuals can add the support type they want in the build of the APP according to their hobbies.

arm64 and -86_64 are not added, if you need to add them yourself, because the minimum compilation requires API21

```
android {
···
defaultConfig {
    ···
    ndk {
        //Set the supported SO library architecture
        abiFilters 'armeabi', 'armeabi-v7a', 'x86'
    }
}

```


### 1.2.2

* Opened the interfaces for getting duration and total duration.
* Added the onPrepared callback for the completion of preparing the video in VideoAllCallBack.

```

 listVideoUtil.getDuration()
 listVideoUtil.getCurrentPositionWhenPlaying();

 GSYVideoPlayer.getDuration()
 GSYVideoPlayer.getCurrentPositionWhenPlaying();

```


### 1.2.1

* Adjusted the situation where the small window callback intercepts errors.
* Added SampleListener to update the page when the close button is clicked in the list small window.

```
//The callback handles the reply page when the small window close is clicked
listVideoUtil.setVideoAllCallBack(new SampleListener(){
    @Override
    public void onQuitSmallWidget(String url, Object... objects) {
        super.onQuitSmallWidget(url, objects);
        //Greater than 0 means there is playback, //The corresponding playback list TAG
        if (listVideoUtil.getPlayPosition() >= 0 && listVideoUtil.getPlayTAG().equals(ListVideoAdapter.TAG)) {
            //The current playback position
            int position = listVideoUtil.getPlayPosition();
            //When it is not visible
            if ((position < firstVisibleItem || position > lastVisibleItem)) {
                //Release the video
                listVideoUtil.releaseVideoPlayer();
                listVideoAdapter.notifyDataSetChanged();
            }
        }
    }
});
```
　
### 1.2.0

* Removed some useless dependent libraries and upgraded IJKPlayer to 0.7.4.

Easier to import, reducing useless dependencies, and removing the problem of needing to configure gradle.properties when importing.

### 1.1.9

* Fixed the callback result of the callback interface VideoAllCallBack, added comments, which can be overridden after inheritance as needed.

There are interface callbacks from full screen to non-full screen, from small window to non-full screen, end playback error touch, etc., added Debuger, which can enable or disable debugging output.

### 1.1.8

* Added handling to clear the cache file if a playback exception occurs in the Cache file (prevention).
* StandardGSYVideoPlayer adds some UI configuration interfaces.

```
/**
 * Bottom progress bar - pop-up
 */
public void setBottomShowProgressBarDrawable(Drawable drawable, Drawable thumb)


/**
 * Bottom progress bar - non-pop-up
 */
public void setBottomProgressBarDrawable(Drawable drawable)

/**
 * Sound progress bar
 */
public void setDialogVolumeProgressBar(Drawable drawable)


/**
 * Middle progress bar
 */
public void setDialogProgressBar(Drawable drawable)

/**
 * Middle progress bar font color
 */
public void setDialogProgressColor(int highLightColor, int normalColor)

```

### 1.1.7

* Added a second type of list ListVideoUtil that supports draggable small windows.

```
@Override
public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    int lastVisibleItem = firstVisibleItem + visibleItemCount;
    //Greater than 0 means there is playback, //The corresponding playback list TAG
    if (listVideoUtil.getPlayPosition() >= 0 && listVideoUtil.getPlayTAG().equals(ListVideoAdapter.TAG)) {
        //The current playback position
        int position = listVideoUtil.getPlayPosition();
        //When it is not visible
        if ((position < firstVisibleItem || position > lastVisibleItem)) {
            //If it is a small window, no need to handle it
            if (!listVideoUtil.isSmall()) {
                //Small window
                int size = CommonUtil.dip2px(ListVideo2Activity.this, 150);
                listVideoUtil.showSmallVideo(new Point(size, size), false, true);
            }
        } else {
            if (listVideoUtil.isSmall()) {
                listVideoUtil.smallVideoToNormal();
            }
        }
    }
}
```

### 1.1.6
* Optimized the full-screen effect of the second type of list ListVideoUtil, which is consistent with the full-screen effect of the first list. The two full-screen effects add an interface to open and close.

```
/**
 * Full-screen animation
 *
 * @param showFullAnimation Whether to use full-screen animation effect
 */
public void setShowFullAnimation(boolean showFullAnimation)
```

### 1.1.5

* Optimized some UI and added some interesting animations, such as the play button.
* Recommend this animation effect [ENViews](https://github.com/codeestX/ENViews).
* Added a custom inheritance template **SampleExtendsPlayer**, I personally suggest copying **StandardGSYVideoPlayer** and modifying it.

### 1.1.4
* Optimized the full-screen animation of the first type of list, the expansion and return transition is smooth on 5.0 and above, and supports the opening and closing of automatic rotation.
* Fixed the problem that the sliding interface is abnormal in full screen, it automatically becomes sliding in full screen, and can be set in non-full screen.

```
/**
 * Whether the sliding interface can change the progress, sound, etc.
 */
public void setIsTouchWiget(boolean isTouchWiget)

```


### 1.1.2
* Added TAG and position to solve the sliding dislocation problem of the first type of list (list implementation in non-ListVideoUtil mode).

```

videoList.setOnScrollListener(new AbsListView.OnScrollListener() {
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int lastVisibleItem = firstVisibleItem + visibleItemCount;
        //Greater than 0 means there is playback
        if (GSYVideoManager.instance().getPlayPosition() >= 0) {
            //The current playback position
            int position = GSYVideoManager.instance().getPlayPosition();
            //The corresponding playback list TAG
            if (GSYVideoManager.instance().getPlayTag().equals(ListNormalAdapter.TAG)
                    && (position < firstVisibleItem || position > lastVisibleItem)) {
                //If it slides out, the top and bottom are no, just like Toutiao
                GSYVideoPlayer.releaseAllVideos();
                listNormalAdapter.notifyDataSetChanged();
            }
        }
    }
});

····

holder.gsyVideoPlayer.setPlayTag(TAG);
holder.gsyVideoPlayer.setPlayPosition(position);

```

### 1.1.1
* Added whether the ListVideoUtil is displayed in horizontal screen in full screen, and whether the full screen is automatically rotated.
* Added an interface to hide the status bar and title of ListVideoUtils.
