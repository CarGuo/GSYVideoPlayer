## 下方个版本说明，可以当做简单的wiki使用~，效果可参考DEMO。

### 7.1.2(2019-12-02)

* fix #2436 增加 exo 的 http timeout 
```
ExoSourceManager

 public static void setHttpReadTimeout(int httpReadTimeout)
 
 public static void setHttpConnectTimeout(int httpConnectTimeout)

```
* 优化视频尺寸显示计算 
* 增加 exo 支持 raw 文件播放

``` 
String url =  RawResourceDataSource.buildRawResourceUri(R.raw.test).toString();
```
* 增加模拟下载共用缓存例子 DetailDownloadPlayer
* 适配 android 10 的全屏
* fix #2382、#2411、#2343、#2379、#2350、#2328
* 增加设置自定义显示比例的支持

``` 
GSYVideoType.setScreenScaleRatio
```
* 增加外挂字幕例子 [exo2模式下支持自定增加外挂字幕](https://github.com/CarGuo/GSYVideoPlayer/tree/master/app/src/main/java/com/example/gsyvideoplayer/exosubtitle)




### 7.1.1(2019-10-12)

* fix #2244、#2252(resolveFullVideoShow 不执行情况)、#2279、#2280
* fix #2303(去除 TimerTask)、#2306（某些机型退到后台返回不显示）
* 增加 setNeedAutoAdaptation
```
    /**
     * 是否需要适配在竖屏横屏时，由于刘海屏或者打孔屏占据空间，导致标题显示被遮盖的问题
     *
     * @param needAutoAdaptation 默认false
     */
    public void setNeedAutoAdaptation(boolean needAutoAdaptation)
```


### 7.1.0(2019-09-01)

* update ExoPlayer to 2.10.4
* 添加沉浸式支持
* 增加 IPlayerInitSuccessListener 播放器初始化成果回调
```
GSYVideoManager
    .instance()
    .setPlayerInitSuccessListener(new IPlayerInitSuccessListener() {
        ///播放器初始化成果回调，可用于播放前的自定义设置
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
* 增加硬解码不花屏幕 [RecyclerView3Activity](https://github.com/CarGuo/GSYVideoPlayer/blob/master/app/src/main/java/com/example/gsyvideoplayer/RecyclerView3Activity.java)



### 7.0.2(2019-07-01)
* update ExoPlayer 到 2.10.0
* 增加 allowCrossProtocolRedirects

```
Map<String, String> header = new HashMap<>();
        header.put("allowCrossProtocolRedirects", "true");

 xxx.setMapHeadData(header)
```

* 调整 onVideoResume 内部方法
* 修改默认亮度布局和布局兼容问题
* 升级一些依赖
* exo player setSeekParameter

```
 //设置 seek 的临近帧。
if(detailPlayer.getGSYVideoManager().getPlayer() instanceof Exo2PlayerManager) {
    ((Exo2PlayerManager) detailPlayer.getGSYVideoManager().getPlayer()).setSeekParameter(SeekParameters.NEXT_SYNC);
    Debuger.printfError("***** setSeekParameter **** ");
}
```

### 7.0.1(2019-04-07)
* 升级 ExoPlayer 到 2.9.6
* ExoPlayer 增加 SSL 证书忽略支持
``` 
ExoSourceManager.setSkipSSLChain(true);
```
* 修复全屏动画过程中按下返回键问题 #1938
* 修复全屏下的弹窗消失问题 #1927
* 修复全屏切换过程过程中的音频焦点问题 #1912
* 修复按键判空问题 #1919
* 修复全屏切换surface的release问题


### 7.0.0-beta1(2019-03-03)
* orientation 增加 pause
```
 orientationUtils.setIsPause(true);
```
* update exoPlayer to 2.9.5。
* exoPlayer 和 mediaPlayer 支持网速显示。
* 修复一些问题。
* 支持库切换到 androidx


### 6.0.3(2019-01-15)

* update exoPlayer to 2.9.3
* update gradle 3.3.0 
* update build sdk 28 
* update support sdk 27.1.1
* 修復exoplayer内核的一些问题。



### 6.0.2(2018-12-21)

* update exoPlayer to 2.9.1
* Deprecated setupLazy
* fix exoPlayer looper
* add `overrideExtension` to exoPlayer


### 6.0.1 (2018-10-14)
* 正式发布6.0版本，调整player和cache加载模式。

``` 
PlayerFactory.setPlayManager(Exo2PlayerManager.class);//EXO模式
PlayerFactory.setPlayManager(SystemPlayerManager.class);//系统模式
PlayerFactory.setPlayManager(IjkPlayerManager.class);//ijk模式

CacheFactory.setCacheManager(ExoPlayerCacheManager.class);//exo缓存模式，支持m3u8，只支持exo
CacheFactory.setCacheManager(ProxyCacheManager.class);//代理缓存模式，支持所有模式，不支持m3u8等
```
* 修复 ProxyCacheManager header设置无效问题。
* 去除无用资源。
* 修复某种场景下的内存泄漏问题。

### 6.0.0-beta (2018-08-22)
* 升级 ExoPlayer 到 2.8.4。
* 修复代理缓存时头部信息不存在问题。
* 调整代码结构，移除 GSYVideoType 中的内核切换，直接通过 PlayerFactory 装载。
* 调整代码结构，ExoPlayer可单独依赖，通过 PlayerFactory 装载，更方便自定义PlayerManager。

``` 
//PlayerFactory.setPlayManager(new Exo2PlayerManager());//EXO模式
//PlayerFactory.setPlayManager(new SystemPlayerManager());//系统模式
//PlayerFactory.setPlayManager(new IjkPlayerManager());//ijk模式
```

* 调整代码结构，CacheFactory 更方便自定义，默认 ProxyCacheManager。

``` 
//CacheFactory.setCacheManager(new ExoPlayerCacheManager());//exo缓存模式，支持m3u8，只支持exo
//CacheFactory.setCacheManager(new ProxyCacheManager());//代理缓存模式，支持所有模式，不支持m3u8等
```

* 增加 ExoMediaSourceInterceptListener，方便 Exo 模式下使用自定义的 MediaSource。

``` 
ExoSourceManager.setExoMediaSourceInterceptListener(new ExoMediaSourceInterceptListener() {
           /**
            * @param dataSource  链接
            * @param preview     是否带上header，默认有header自动设置为true
            * @param cacheEnable 是否需要缓存
            * @param isLooping   是否循环
            * @param cacheDir    自定义缓存目录
            * @return 返回不为空时，使用返回的自定义mediaSource
            */
            @Override
            public MediaSource getMediaSource(String dataSource, boolean preview, boolean cacheEnable, boolean isLooping, File cacheDir) {
                return null;
            }
});
```



### 5.0.2（2018-08-01）
* Fix跟随屏幕旋转存在的问题。
* 修改对于Audio冲突时候的处理，子类可以复写方法自行另外处理.
* fix #1300
* 默认暂停图片修改为false



### 5.0.1(2018-07-01)
* Update ListGSYVideoPlayer 
* ijkPlayer的ex_so增加avi支持
* update ExoPlayer to 2.8.2
* ExoPlayer模式的问题修复

### 5.0.0-beta(2018-05-24)
* 调整cache机制，抽离cache管理器。
* update ExoPlayer to 2.8.0，全面针对ExoPlayer进行优化
* 优化ExoPlayer的问题
* 增加ExoPlayer循环播放支持
* 增加ExoPlayer自定义DEMO，演示ExoPlayer实现无缝切换
* ijk的`logLevel`、`ijkLibLoader`直接通过IJKPlayerManager静态方法设置
* 进一步调整框架结构和内部耦合度


### 4.1.3(2018-05-11)
* 优化单双击
* update support and build sdk to 27
* 增加是否根据视频尺寸，自动选择竖屏全屏或者横屏全屏，注意，这时候默认旋转无效。
```
/**
  * 是否根据视频尺寸，自动选择竖屏全屏或者横屏全屏，注意，这时候默认旋转无效
  * @param autoFullWithSize 默认false
  */
 public void setAutoFullWithSize(boolean autoFullWithSize)
```


### 4.1.2(2018-04-14)
* 修复已知问题。
* 增加ijkplayer的raw播放支持。
```
String url = "android.resource://" + getPackageName() + "/" + R.raw.test;
GSYVideoManager.instance().enableRawPlay(getApplicationContext());
```
* danmku分支提供网络弹幕demo

### 4.1.1 (2018-04-01)
* 1、update support lib to 26.0.2
* 2、修复了渲染层在某些条件下，截图时返回大小不对问题。
* 3、一些细节的优化处理。
* 4、增加Manager的isFullState方法
```
 /*
  * 当前是否全屏状态
  *
  * @return 当前是否全屏状态， true代表是。
  */
 public static boolean isFullState(Activity activity)
```

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


### 3.0.0（2018-01-14）

1、增肌PlayerManager，更新为ExoPlayer2，优化对ExoPlayer2的支持。

2、增加系统播放器AndroidMediaPlayer支持

3、增对列表增加setUpLazy方法，优化列表中可能的滑动卡顿
```
    /**
     * 在点击播放的时候才进行真正setup
     */
    public boolean setUpLazy(String url, boolean cacheWithPlay, File cachePath, Map<String, String> mapHeadData, String title)

```
4、优化GL渲染和处理切换渲染效果崩溃。



5、DEMO增加SamllVideoHelper实现小窗口逻辑，更新demo



6、优化触摸的音量、亮度、进度的弹出框，优化可自定义程度
```
    /**
     * 触摸进度dialog的layoutId
     * 继承后重写可返回自定义
     * 有自定义的实现逻辑可重载showProgressDialog方法
     */
    protected int getProgressDialogLayoutId()
    /**
     * 触摸进度dialog的进度条id
     * 继承后重写可返回自定义，如果没有可返回空
     * 有自定义的实现逻辑可重载showProgressDialog方法
     */
    protected int getProgressDialogProgressId()

    /**
     * 触摸进度dialog的当前时间文本
     * 继承后重写可返回自定义，如果没有可返回空
     * 有自定义的实现逻辑可重载showProgressDialog方法
     */
    protected int getProgressDialogCurrentDurationTextId()

    /**
     * 触摸进度dialog全部时间文本
     * 继承后重写可返回自定义，如果没有可返回空
     * 有自定义的实现逻辑可重载showProgressDialog方法
     */
    protected int getProgressDialogAllDurationTextId()

    /**
     * 触摸进度dialog的图片id
     * 继承后重写可返回自定义，如果没有可返回空
     * 有自定义的实现逻辑可重载showProgressDialog方法
     */
    protected int getProgressDialogImageId()

    /**
     * 音量dialog的layoutId
     * 继承后重写可返回自定义
     * 有自定义的实现逻辑可重载showVolumeDialog方法
     */
    protected int getVolumeLayoutId()
    /**
     * 音量dialog的百分比进度条 id
     * 继承后重写可返回自定义，如果没有可返回空
     * 有自定义的实现逻辑可重载showVolumeDialog方法
     */
    protected int getVolumeProgressId()

    /**
     * 亮度dialog的layoutId
     * 继承后重写可返回自定义
     * 有自定义的实现逻辑可重载showBrightnessDialog方法
     */
    protected int getBrightnessLayoutId()

    /**
     * 亮度dialog的百分比text id
     * 继承后重写可返回自定义，如果没有可返回空
     * 有自定义的实现逻辑可重载showBrightnessDialog方法
     */
    protected int getBrightnessTextId()

```


### 2.1.3（2017-12-24）
* update demo gradle to 4.1
* 增加对CollapsingToolbarLayout的支持与demo
* 多窗体下（包括桌面）的小窗口播放（WindowActivity）。
* 增加播放进度回调
```
/**
 * 进度回调
 */
public void setGSYVideoProgressListener(GSYVideoProgressListener videoProgressListener)
```

### 2.1.2(2017-12-08)
* 增加针对Prepared之前调用OnVideoPause的处理
* 背景视频模糊铺满，前方视频正常播放
```
DetailFilterActivity中注释的
//高斯拉伸视频铺满背景，替换黑色，前台正常比例播放
```


### 2.1.1(2017-10-29)
* videoCache模式支持增加header
* 增加无缝切换视频DEMO SmartPickVideo
* 调整部分代码路径，优化代码
* log输入等级接口
```
GSYVideoManager.instance().setLogLevel(IjkMediaPlayer.IJK_LOG_SILENT);
```

### 2.1.0(2017-10-10)
* 增加了视频帧合成gif功能（DEMO DetailControlActivity中）。
* update ijkplayer 0.84

### 2.0.9(2017-10-02)
* 增加顶层效果渲染的动画效果。
* 增加截图功能。
* 增加自定义render支持。
* 增加水印、多重播放等。


### 2.0.8（2017-09-17）
* 增加GSYBaseActivityDetail抽象类，方便detail模式集成。
* 内部增加一些优化。
* 增加简单滤镜功能支持。
```
1、全局设置
GSYVideoType.setRenderType(GSYVideoType.GLSURFACE);
2、设置滤镜
player.setEffectFilter(new BarrelBlurEffect());
```


### 2.0.7(2017-09-13）

* 优化增加了断网自动续连，需要为http前加上 "ijkhttphook:http://ssss"
* update ijk to 0.8.3
* 增加了demo中seekto精准定位，解决某些视频seek之后从头播放

### 2.0.6(2017-08-31)
* 调整了返回按键显示的问题。
* 修改了全屏可能出现缓冲不消失问题。
* 优化了双击问题。

### 2.0.5(2017-08-26)
* 增加双击暂停开始。
* 增加了SurfaceView的支持:GSYVideoType.setRenderType(GSYVideoType.SUFRACE)。
* 优化了触摸问题、内存问题、dismisstime问题。

### 2.0.4(2017-08-08)
* 增加了空播放ui支持。
* 调整了GSYVideoOptionBuilder。
* 修改了已知问题。
* 增加了播放中调整播放速度接口。
```
public void setSpeedPlaying(float speed, boolean soundTouch) 
```

### 2.0.3(2017-08-06)
* update ijk to 0.8.2
* fix rtsp 播放问题
* fix 小窗口播放问题
* 调整了部分代码逻辑与结构。

### 2.0.2(2017-07-16)
* 完美实现播放、暂停、前后台切换、画面调整等情况不黑屏不突变，删除coverImageView类。
* 增加了6.0下变调不变速接口
* update ijkPlayer to 0.8.1

### 2.0.1(2017-07-11)
* 优化了TextureView显示
* 修复SampleView的暂停问题


### 2.0.0(2017-07-10)
* 项目结构调整，增加了新的so支持。

### 1.6.9(2017-07-08)

* 修改setup的设置参数。
* 升级修改所有回调接口，回调接口中返回当前播放器。
* 修正播放本地文件错误，会错删文件问题。
* 兼容Appbar中使用，感谢[@loveRose](https://github.com/loveRose)
* 非全屏播放器可获取全屏播放器对象。
```
/**
 * 获取全屏播放器对象
 *
 * @return GSYVideoPlayer 如果没有则返回空。
 */
public GSYVideoPlayer getFullWindowPlayer()
```

### 1.6.8(2017-06-27)
* fix listVideoUtils title错乱问题
* fix setSpeed无法重置的问题 
* fix 切换网络无法继续播放问题
* 增加旋转使能后是否跟随系统设置
```
/**
 * 是否跟随系统旋转，false的话，系统禁止旋转也会跟着旋转
 * @param rotateWithSystem 默认true
 */
public void setRotateWithSystem(boolean rotateWithSystem)
```

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

### 1.6.6(2017-05-24)
* update ijkplayer to 0.8.0
* update videocache to 2.7.0

### 1.6.5(2017-05-05)
* 增加镜像旋转demo SampleVideo
* 修改了循环播放的UI问题
* 修改了本地文件或者已缓存文件，显示进度问题 
* 修复了横竖屏的问题
* GSYVideoType增加SCREEN_TYPE_FULL类型，通过按照比例裁减放大视频，达到全屏
* 增加setShowPauseCover接口

```
/**
 * 是否需要加载显示暂停的cover图片
 * 打开状态下，暂停退到后台，再回到前台不会显示黑屏，但可以对某些机型有概率出现OOM
 * 关闭情况下，暂停退到后台，再回到前台显示黑屏
 *
 * @param showPauseCover 默认true
 */
public void setShowPauseCover(boolean showPauseCover)
```

### 1.6.4(2017-04-20)
* update ijk to 0.7.9 (增加了soundTouch，调速后声音变调问题得到解决)
* 修复了可能出现的判空问题，修复了ListGSYVideoPlayer的同步问题 
* 修复了可移动小窗口播放结束无法移动的问题

### 1.6.3(2017-04-15)
* 修改了播放器全屏时的选择90度问题
* 修改了播放器可能存在的拉伸问题
* 增加旋转播放画面的demo和库支持

### 1.6.2(2017-04-05)
* 移除无用代码
* 修复了动态播放按键的显示小白点问题
* 增加了 NormalGSYVideoPlayer（使用正常图片做播放按键、系统loading的播放器）
* 增加了动态加载so的方法
* 增加了setIsTouchWigetFull方法，全屏的时候也可以禁止滑动产生的快进，声音，亮度调节逻辑
```
/**
 * 设置自定义so包加载类
 * 需要在instance之前设置
 */
public static void setIjkLibLoader(IjkLibLoader libLoader)
```
```
/**
 * 是否可以全屏滑动界面改变进度，声音等
 * 默认 true
 */
public void setIsTouchWigetFull(boolean isTouchWigetFull)
```
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

### 1.6.0 (2017-02-19)
* update ijkplayer to 0.7.7.1。
* 增加了弹幕demo，主要演示如何快速集成弹幕功能。
* 修改了播放时可能出现loading不消失问题。
* 修复了全屏和退出全屏图片显示错误问题。
* 全屏切换按键的图片资源支持自定义。

```
/**
 * 设置右下角 显示切换到全屏 的按键资源
 * 必须在setUp之前设置
 * 不设置使用默认
 */
public void setEnlargeImageRes(int mEnlargeImageRes)


/**
 * 设置右下角 显示退出全屏 的按键资源
 * 必须在setUp之前设置
 * 不设置使用默认
 */
public void setShrinkImageRes(int mShrinkImageRes)

```


### 1.5.9
* update ijkplayer to 0.7.7
* update build.gradle to 2.2.3

### 1.5.8
* 修改了锁定屏幕触摸功能再播放结束后的问题。

### 1.5.7
* change AudioManger get。

### 1.5.6
* 修复了mUrl再error后为空的问题。
* 增加了GSYVideoManager的option配置接口。

```
/**
 * 设置IJK视频的option
 */
public void setOptionModelList(List<VideoOptionModel> optionModelList)
```

### 1.5.5
* update ijk 0.7.6。
* 快播与慢播接口支持M以下。


### 1.5.4
* 增加了静音播放接口。

GSYVideoManager下
可参考：ListNormalAdapter
```
/**
 * 是否需要静音
 */
public void setNeedMute(boolean needMute)
```

### 1.5.3
* 修改了在缓冲的时候，默认loading点击会重置的问题。
* 虚拟按键在弹出的后，过一段时间自动隐藏。

## 1.5.2
* 增加了Error的回调接口。
* 修复了Demo的PlayActivity兼容问题

### 1.5.1
* 全屏滑动弹出虚拟按键会影响进度问题。
* 优化了滑动的弹出dialog。
* 修改了一些问题。

### 1.5.0
* 增加了全屏和普通播放下使用两套布局的支持，增加demo：LandLayoutVideo。
* 修改了DEMO的recyclerView的一个问题。
* 修改了一些bug。
* 增加了WebView滑动demo。

```

/**
 * 如果需要使用到：全屏和普通播放下使用两套布局的支持。
 * 那么重载播放器请记得重载下方构造方法
 */
public XXXXXXXXXX(Context context, Boolean fullFlag) {
    super(context, fullFlag);
}

····

//这个必须配置最上面的构造才能生效
@Override
public int getLayoutId() {
    if (mIfCurrentIsFullscreen) {
        return R.layout.sample_video_land;
    }
    return R.layout.sample_video;
}

```
### 1.4.9
* 增加了连续播放列表的支持 ListGSYVideoPlayer。
* 增加了列表播放的demo  DetailListPlayer。
* 减小了https版本的so的大小。


### 1.4.8
* 锁定屏幕按键增加锁定屏幕旋转功能。
* 锁定屏幕按键增加回调接口。
* 修复了横屏的一个问题。

### 1.4.7
* 修复直接横屏有闪动的问题。
* 修改了流量提示的接口。
* 增加了HTTPS支持。

```
/**
 * 是否需要显示流量提示,默认true
 */
public void setNeedShowWifiTip(boolean needShowWifiTip)
```

### 1.4.6
* 修改了某些虚拟按键手机下，全屏后返回导致界面显示不正的问题。
* 增加测试版CustomGSYVideoPlayer，实现滑动进度条预览效果（测试效果）。
* 修改了出现除以0的的问题，全屏时候调整UI样式无效的情况。
* 注：CustomGSYVideoPlayer目前对已完全缓存的视频，或者本地文件支持好一些，对纯网络视频支持“较”差。

```
/**
 * 如果是需要进度条预览的设置打开，默认关闭
 */
public void setOpenPreView(boolean localFile)
```

### 1.4.5
* 支持切换IJKPlayer和EXOPlayer,不过EXOPlayer后台播放回到前台黑的问题除了seekto无解啊。

GSYVideoManager

```
/**
 * 设置了视频的播放类型
 * GSYVideoType IJKPLAYER = 0 or IJKEXOPLAYER = 1;
 */
public void setVideoType(Context context, int videoType)
```

### 1.4.4

* 调整lib，DEMO中SampleVideo增加了调整清晰度的支持,DEMO借用了jjdxm_ijkplayer的URL。
* 优化了Cache缓存和IJK 缓存之间的显示。


### 1.4.3

* 增加了设置显示比例GSYVideoType。
* DEMO增加SampleVideo，在PlayActivity使用，调节显示比例效果。
* 增加了开启和关闭硬解码的接口GSYVideoType。

GSYVideoType
```
/**
 * 设置显示比例
 */
public static void setShowType(int type)

/**
 * 使能硬解码，播放前设置
 */
public static void enableMediaCodec() {
    MEDIA_CODEC_FLAG = true;
}

/**
 * 关闭硬解码，播放前设置
 */
public static void disableMediaCodec()
```

### 1.4.2

* 修改了暂停画面在对旋转视频/竖屏播放时变形的问题。
* 调整了亮度的灵敏度，优化了亮度调节。

### 1.4.1

* 增加了全屏锁开关，锁定后屏幕点击无效。
* 增加了全局暂停和播放，支援列表状态。
* 修正了亮度调节的问题。

StandardGSYVideoPlayer/ListVideoUtil

```
/**
 * 是否需要全屏锁定屏幕功能
 * 如果单独使用请设置setIfCurrentIsFullscreen为true
 */
public void setNeedLockFull(boolean needLoadFull)
```

GSYVideoManager

```
/**
 * 暂停播放
 */
public static void onPause()

/**
 * 恢复播放
 */
public static void onResume()
```


### 1.4.0 (3.8和3.9难产了)

* 添加了lib封面对复用封面的支持和demo。
* 修复了缓冲进度条；
* 增加了recyclerViewDemo。
* update VideoCache，去除error out put log输出。
* 修正了列表中隐藏虚拟键盘与actionbar的冲突。



### 1.3.7
* 优化了弹出框。
* 优化了暂停的时候(全屏/恢复全屏/退到)会是黑色的问题。
* 解决了暂停的时候拖动进度条问题。


### 1.3.6
* 区分了没有网络和没有wifi的提示。
* 更新了Demo detailPlayer直接旋转全屏。
* 返回正常的详情效果。


### 1.3.5
* 增加了全屏隐藏虚拟按键。
* 修复了缓冲过程中加载动画就停止了。

```
/**
 * 全屏隐藏虚拟按键，默认打开
 */
public void setHideKey(boolean hideKey)
```

### 1.3.4
* 增加了清除默认缓存接口。
* 增加了播放偏移。
* 优化了拖动进度条或者缓存导致播放时间跳动的问题。

GSYVideoManager

```
/**
 * 删除默认所有缓存文件
 */
public static void clearAllDefaultCache(Context context)

/**
 * 删除url对应默认缓存文件
 */
public static void clearDefaultCache(Context context, String url)
```

GSYVideoPlayer

```

/**
 * 清除当前缓存
 */
public void clearCurrentCache()

/**
 * 从哪里开始播放
 * 目前有时候前几秒有跳动问题
 */
public void setSeekOnStart(int seekOnStart)

```

### 1.3.3

* 优化了一些内存泄漏问题。
* 更新了demo。

### 1.3.2

* 解决了因为兼容FragmentActivity导致actionbar隐藏失败问题。

### 1.3.1
* 更新了lastListener的判空问题。

### 1.3.0
* 支持配置缓存路径，添加了ListVideoUtils的一些接口。

正常模式

```
//默认缓存路径方式
holder.gsyVideoPlayer.setUp(url, true , "");

···

//一个列表的视频缓存路径相同
holder.gsyVideoPlayer.setUp(url, true, new File(FileUtils.getTestPath(), ""));

···

//如果一个列表里的缓存路径不同，需要用下方的方式

//避免全屏返回的时候不可用了，只初始化不是当前位置的ui
if (playPosition < 0 || playPosition != position ||
        !GSYVideoManager.instance().getPlayTag().equals(ListNormalAdapter.TAG)) {
    holder.gsyVideoPlayer.initUIState();
}

//如果设置了点击封面可以播放，如果缓存列表路径不一致，还需要设置封面点击
holder.gsyVideoPlayer.setThumbPlay(true);

holder.gsyVideoPlayer.getStartButton().setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        //需要切换缓存路径的
        holder.gsyVideoPlayer.setUp(url, true, new File(FileUtils.getTestPath(), ""));
        holder.gsyVideoPlayer.startPlayLogic();
    }
});

holder.gsyVideoPlayer.getThumbImageViewLayout().setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        //需要切换缓存路径的
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

* 增加了下载速度的接口。

```
/**
 * 网络速度
 * 注意，这里如果是开启了缓存，因为读取本地代理，缓存成功后还是存在速度的
 * 再打开已经缓存的本地文件，网络速度才会回0.因为是播放本地文件了
 */
public long getNetSpeed()

/**
 * 网络速度
 * 注意，这里如果是开启了缓存，因为读取本地代理，缓存成功后还是存在速度的
 * 再打开已经缓存的本地文件，网络速度才会回0.因为是播放本地文件了
 */
public String getNetSpeedText()

```

### 1.2.8

* 升级IJKPlayer到0.7.5。
* 增加了改变播放速度(0-2左右的速度)，但只支持6.0以上。

```
/**
 * 播放速度
 */
public void setSpeed(float speed)
```

### 1.2.7
* 修改了循环播放的时候，重新播放不弹出控制UI。
* 修改了FragmentActivity的actionBar问题。


### 1.2.6

* 修正了StandardGSYVideoPlayer的接口全屏回调问题。
* 增加了循环播放的接口。

```

public void setLooping(boolean looping)

```


### 1.2.5

* 增加了新接口，支持直接横屏锁住界面。
* 关闭全屏动画，组合接口使用。

##### GSYVideoPlayer

```
/**
 * 全屏动画
 *
 * @param showFullAnimation 是否使用全屏动画效果
 */
public void setShowFullAnimation(boolean showFullAnimation)

/**
 * 是否开启自动旋转
 */
public void setRotateViewAuto(boolean rotateViewAuto)

/**
 * 一全屏就锁屏横屏，默认false竖屏，可配合setRotateViewAuto使用
 */
public void setLockLand(boolean lockLand)
```

##### ListVideoUtil

```
/**
 * 是否自动旋转
 *
 * @param autoRotation 是否要支持重力旋转
 */
public void setAutoRotation(boolean autoRotation) {
    this.autoRotation = autoRotation;
}

/**
 * 是否全屏就马上横屏
 *
 * @param fullLandFrist 如果是，那么全屏的时候就会切换到横屏
 */
public void setFullLandFrist(boolean fullLandFrist) {
    this.fullLandFrist = fullLandFrist;
}

/**
 * 全屏动画
 *
 * @param showFullAnimation 是否使用全屏动画效果
 */
public void setShowFullAnimation(boolean showFullAnimation) {
    this.showFullAnimation = showFullAnimation;
}
```


### 1.2.4

* 兼容API修改至16,全屏动画兼容全API。
　

### 1.2.3

*增加了X86类型的依赖，个人可根据爱好在APP的build里面添加自己要的支持类型。

arm64和-86_64的没有加入，如果需要自己添加即可，因为编译最低需要API21

```
android {
···
defaultConfig {
    ···
    ndk {
        //设置支持的SO库架构
        abiFilters 'armeabi', 'armeabi-v7a', 'x86'
    }
}

```


### 1.2.2

*开放了取时长和总时长的接口。
*增加了VideoAllCallBack的准备视频完成的回调onPrepared。

```

 listVideoUtil.getDuration()
 listVideoUtil.getCurrentPositionWhenPlaying();

 GSYVideoPlayer.getDuration()
 GSYVideoPlayer.getCurrentPositionWhenPlaying();

```


### 1.2.1

* 调整了小窗口回调拦截错误的情况。
* 增加了SampleListener在列表小窗口点击关闭的时候更新页面。

```
//小窗口关闭被点击的时候回调处理回复页面
listVideoUtil.setVideoAllCallBack(new SampleListener(){
    @Override
    public void onQuitSmallWidget(String url, Object... objects) {
        super.onQuitSmallWidget(url, objects);
        //大于0说明有播放,//对应的播放列表TAG
        if (listVideoUtil.getPlayPosition() >= 0 && listVideoUtil.getPlayTAG().equals(ListVideoAdapter.TAG)) {
            //当前播放的位置
            int position = listVideoUtil.getPlayPosition();
            //不可视的是时候
            if ((position < firstVisibleItem || position > lastVisibleItem)) {
                //释放掉视频
                listVideoUtil.releaseVideoPlayer();
                listVideoAdapter.notifyDataSetChanged();
            }
        }
    }
});
```
　
### 1.2.0

* 去除了一些无用的依赖库，升级IJKPlayer到0.7.4。

更容易导入，减少了无用的依赖情况，去除了import的时候需要配置gradle.properties的问题。

### 1.1.9

* 修正了回调接口VideoAllCallBack的回调结果，添加了注释，可以根据需要继承后覆写。

有全屏到非全屏，有小窗口到非小窗口，结束播放错误触摸等等的接口回调，增加了Debuger，可以使能或者关闭调试输出。

### 1.1.8

* 增加了如果Cache文件出现播放异常，就清除缓存文件的处理（预防）。
* StandardGSYVideoPlayer增加了一些UI配置接口。

```
/**
 * 底部进度条-弹出的
 */
public void setBottomShowProgressBarDrawable(Drawable drawable, Drawable thumb)


/**
 * 底部进度条-非弹出
 */
public void setBottomProgressBarDrawable(Drawable drawable)

/**
 * 声音进度条
 */
public void setDialogVolumeProgressBar(Drawable drawable)


/**
 * 中间进度条
 */
public void setDialogProgressBar(Drawable drawable)

/**
 * 中间进度条字体颜色
 */
public void setDialogProgressColor(int highLightColor, int normalColor)

```

### 1.1.7

* 增加了第二种列表 ListVideoUtil可拖动小窗口支持。

```
@Override
public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    int lastVisibleItem = firstVisibleItem + visibleItemCount;
    //大于0说明有播放,//对应的播放列表TAG
    if (listVideoUtil.getPlayPosition() >= 0 && listVideoUtil.getPlayTAG().equals(ListVideoAdapter.TAG)) {
        //当前播放的位置
        int position = listVideoUtil.getPlayPosition();
        //不可视的是时候
        if ((position < firstVisibleItem || position > lastVisibleItem)) {
            //如果是小窗口就不需要处理
            if (!listVideoUtil.isSmall()) {
                //小窗口
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
* 优化了第二种列表ListVideoUtil的全屏效果，和列表一的全屏效果一致，两种全屏效果增加是否打开关闭接口。

```
/**
 * 全屏动画
 *
 * @param showFullAnimation 是否使用全屏动画效果
 */
public void setShowFullAnimation(boolean showFullAnimation)
```

### 1.1.5

* 优化了一些UI，增加了一些有趣的动画，比如播放按键。
* 推荐这个动画效果[ENViews](https://github.com/codeestX/ENViews)。
* 增加自定义继承模板**SampleExtendsPlayer**，个人建议直接拷贝**StandardGSYVideoPlayer**修改也行。

### 1.1.4
* 优化了第一种列表的全屏动画,5.0以上展开和返回过渡顺畅，支持自动旋转的开启与关闭。
* 修改了全屏下的滑动接口不正常问题，全屏下自动变为滑动的，非全屏可以设置。

```
/**
 * 是否可以滑动界面改变进度，声音等
 */
public void setIsTouchWiget(boolean isTouchWiget)

```


### 1.1.2
* 增加了TAG和position来实现第一种list列表（非ListVideoUtil模式的列表实现）的滑动错位问题。

```

videoList.setOnScrollListener(new AbsListView.OnScrollListener() {
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int lastVisibleItem = firstVisibleItem + visibleItemCount;
        //大于0说明有播放
        if (GSYVideoManager.instance().getPlayPosition() >= 0) {
            //当前播放的位置
            int position = GSYVideoManager.instance().getPlayPosition();
            //对应的播放列表TAG
            if (GSYVideoManager.instance().getPlayTag().equals(ListNormalAdapter.TAG)
                    && (position < firstVisibleItem || position > lastVisibleItem)) {
                //如果滑出去了上面和下面就是否，和今日头条一样
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
* 增加了ListVideoUtil全屏是否显示横屏，全屏是否自动旋转。
* 增加了ListVideoUtils隐藏状态栏和title的接口。


