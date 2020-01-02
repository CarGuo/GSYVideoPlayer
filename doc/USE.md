## 三种简单的使用方法


*注意：下面几种方式所在的Activity不要忘记配置manifest的config。*
```
<activity
    android:name=".xxxxx"
    android:configChanges="keyboard|keyboardHidden|orientation|screenSize|screenLayout|smallestScreenSize|uiMode"
    android:screenOrientation="portrait" />

```

### 一、直接播放

#### [SimplePlayer](https://github.com/CarGuo/GSYVideoPlayer/blob/master/app/src/main/java/com/example/gsyvideoplayer/simple/SimplePlayer.java)

### 二、列表中使用

#### 模式一 [SimpleListVideoActivityMode1](https://github.com/CarGuo/GSYVideoPlayer/blob/master/app/src/main/java/com/example/gsyvideoplayer/simple/SimpleListVideoActivityMode1.java)

1、adapter布局中添加播放控件
```
<com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
    android:id="@+id/detail_player"
    android:layout_width="match_parent"
    android:layout_height="@dimen/post_media_height" />

```

2、adapter中配置播放控件（也可以使用builder模式，详见下方detail模式中）
```
holder.gsyVideoPlayer.setUpLazy(url, true, null, null, "这是title");
//增加title
holder.gsyVideoPlayer.getTitleTextView().setVisibility(View.GONE);
//设置返回键
holder.gsyVideoPlayer.getBackButton().setVisibility(View.GONE);
//设置全屏按键功能
holder.gsyVideoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        holder.gsyVideoPlayer.startWindowFullscreen(context, false, true);
    }
});
//防止错位设置
holder.gsyVideoPlayer.setPlayTag(TAG);
holder.gsyVideoPlayer.setPlayPosition(position);
//是否根据视频尺寸，自动选择竖屏全屏或者横屏全屏
holder.gsyVideoPlayer.setAutoFullWithSize(true);
//音频焦点冲突时是否释放
holder.gsyVideoPlayer.setReleaseWhenLossAudio(false);
//全屏动画
holder.gsyVideoPlayer.setShowFullAnimation(true);
//小屏时不触摸滑动
holder.gsyVideoPlayer.setIsTouchWiget(false);
```

3、Activity中配置生命周期
```
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ···
        videoList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}
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
                        if(GSYVideoManager.isFullState(ListVideoActivity.this)) {
                            return;
                        }
                        //如果滑出去了上面和下面就是否，和今日头条一样
                        GSYVideoManager.releaseAllVideos();
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
        ···
    }

    @Override
    public void onBackPressed() {
        if (GSYVideoManager.backFromWindowFull(this)) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        GSYVideoManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        GSYVideoManager.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GSYVideoManager.releaseAllVideos();
    }
```


#### 模式二  [SimpleListVideoActivityMode2](https://github.com/CarGuo/GSYVideoPlayer/blob/master/app/src/main/java/com/example/gsyvideoplayer/simple/SimpleListVideoActivityMode2.java)

一、item布局中添加
```
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">

    <FrameLayout
        android:id="@+id/list_item_container"
        android:layout_width="match_parent"
        android:layout_height="@dimen/post_media_height"
        android:scaleType="centerCrop"
        android:src="@mipmap/xxx1" />


    <ImageView
        android:id="@+id/list_item_btn"
        android:layout_width="60dp"
        android:layout_height="60dp"
        android:layout_centerHorizontal="true"
        android:layout_centerVertical="true"
        android:layout_gravity="center_vertical"
        android:src="@drawable/video_click_play_selector" />

</RelativeLayout>
```

2、Activity中创建helper和配置生命周期
```
    GSYVideoHelper smallVideoHelper;

    ListVideoAdapter listVideoAdapter;

    GSYVideoHelper.GSYVideoHelperBuilder gsySmallVideoHelperBuilder;

    int lastVisibleItem;

    int firstVisibleItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_video2);

        //创建小窗口帮助类
        smallVideoHelper = new GSYVideoHelper(this);
        //配置
        gsySmallVideoHelperBuilder = new GSYVideoHelper.GSYVideoHelperBuilder();
        gsySmallVideoHelperBuilder
                .setHideStatusBar(true)
                .setNeedLockFull(true)
                .setCacheWithPlay(true)
                .setShowFullAnimation(false)
                .setRotateViewAuto(false)
                .setLockLand(true)
                .setVideoAllCallBack(new GSYSampleCallBack(){
            @Override
            public void onQuitSmallWidget(String url, Object... objects) {
                super.onQuitSmallWidget(url, objects);
                //大于0说明有播放,//对应的播放列表TAG
                if (smallVideoHelper.getPlayPosition() >= 0 && smallVideoHelper.getPlayTAG().equals(ListVideoAdapter.TAG)) {
                    //当前播放的位置
                    int position = smallVideoHelper.getPlayPosition();
                    //不可视的是时候
                    if ((position < firstVisibleItem || position > lastVisibleItem)) {
                        //释放掉视频
                        smallVideoHelper.releaseVideoPlayer();
                        listVideoAdapter.notifyDataSetChanged();
                    }
                }

            }
        });
        
        smallVideoHelper.setGsyVideoOptionBuilder(gsySmallVideoHelperBuilder);
        
        videoList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                ListVideo2Activity.this.firstVisibleItem = firstVisibleItem;
                lastVisibleItem = firstVisibleItem + visibleItemCount;
                //大于0说明有播放,//对应的播放列表TAG
                if (smallVideoHelper.getPlayPosition() >= 0 && smallVideoHelper.getPlayTAG().equals(ListVideoAdapter.TAG)) {
                    //当前播放的位置
                    int position = smallVideoHelper.getPlayPosition();
                    //不可视的是时候
                    if ((position < firstVisibleItem || position > lastVisibleItem)) {
                        //如果是小窗口就不需要处理
                        if (!smallVideoHelper.isSmall()) {
                            //小窗口
                            int size = CommonUtil.dip2px(ListVideo2Activity.this, 150);
                            smallVideoHelper.showSmallVideo(new Point(size, size), false, true);
                        }
                    } else {
                        if (smallVideoHelper.isSmall()) {
                            smallVideoHelper.smallVideoToNormal();
                        }
                    }
                }
            }

        });
    }


    @Override
    public void onBackPressed() {
        if (smallVideoHelper.backFromFull()) {
            return;
        }
        super.onBackPressed();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        smallVideoHelper.releaseVideoPlayer();
        GSYVideoManager.releaseAllVideos();
    }
```

3、adapter中使用helper
```
···
smallVideoHelper.addVideoPlayer(position, holder.imageView, TAG, holder.videoContainer, holder.playerBtn);

holder.playerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyDataSetChanged();
                smallVideoHelper.setPlayPositionAndTag(position, TAG);
                final String url = "http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4";
                gsySmallVideoHelperBuilder.setVideoTitle("title " + position)
                        .setUrl(url);
                smallVideoHelper.startPlay();
            }
});

···
```


### 三、详情页播放

#### 模式一  [SimpleDetailActivityMode1](https://github.com/CarGuo/GSYVideoPlayer/blob/master/app/src/main/java/com/example/gsyvideoplayer/simple/SimpleDetailActivityMode1.java)

1、布局中添加播放控件
```
<com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
    android:id="@+id/detail_player"
    android:layout_width="match_parent"
    android:layout_height="@dimen/post_media_height" />

```

2、Activity继承**GSYBaseActivityDetail**
```
public class DetailControlActivity extends GSYBaseActivityDetail<StandardGSYVideoPlayer>
```

3、重载配置
```

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_player);

        detailPlayer = (StandardGSYVideoPlayer) findViewById(R.id.detail_player);
        //增加title
        detailPlayer.getTitleTextView().setVisibility(View.GONE);
        detailPlayer.getBackButton().setVisibility(View.GONE);

        initVideoBuilderMode();

    }

    @Override
    public StandardGSYVideoPlayer getGSYVideoPlayer() {
        return detailPlayer;
    }

    @Override
    public GSYVideoOptionBuilder getGSYVideoOptionBuilder() {
        //内置封面可参考SampleCoverVideo
        ImageView imageView = new ImageView(this);
        loadCover(imageView, url);
        return new GSYVideoOptionBuilder()
                .setThumbImageView(imageView)
                .setUrl(url)
                .setCacheWithPlay(true)
                .setVideoTitle(" ")
                .setIsTouchWiget(true)
                .setRotateViewAuto(false)
                .setLockLand(false)
                .setShowFullAnimation(false)
                .setNeedLockFull(true)
                .setSeekRatio(1);
    }

    @Override
    public void clickForFullScreen() {

    }


    /**
     * 是否启动旋转横屏，true表示启动
     */
    @Override
    public boolean getDetailOrientationRotateAuto() {
        return true;
    }
```

#### 模式二  [SimpleDetailActivityMode2](https://github.com/CarGuo/GSYVideoPlayer/blob/master/app/src/main/java/com/example/gsyvideoplayer/simple/SimpleDetailActivityMode2.java)

1、布局中添加播放控件
```
<com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
    android:id="@+id/detail_player"
    android:layout_width="match_parent"
    android:layout_height="@dimen/post_media_height" />

```

2、onCreate中配置播放器、添加旋转外部旋转支持、增加监听。（也可以会直接通过player设置，不用builder）
```
//外部辅助的旋转，帮助全屏
orientationUtils = new OrientationUtils(this, detailPlayer);
//初始化不打开外部的旋转
orientationUtils.setEnable(false);

GSYVideoOptionBuilder gsyVideoOption = new GSYVideoOptionBuilder();
gsyVideoOption.setThumbImageView(imageView)
        .setIsTouchWiget(true)
        .setRotateViewAuto(false)
        .setLockLand(false)
        .setAutoFullWithSize(true)
        .setShowFullAnimation(false)
        .setNeedLockFull(true)
        .setUrl(url)
        .setCacheWithPlay(false)
        .setVideoTitle("测试视频")
        .setVideoAllCallBack(new GSYSampleCallBack() {
            @Override
            public void onPrepared(String url, Object... objects) {
                super.onPrepared(url, objects);
                //开始播放了才能旋转和全屏
                orientationUtils.setEnable(true);
                isPlay = true;
            }

            @Override
            public void onQuitFullscreen(String url, Object... objects) {
                super.onQuitFullscreen(url, objects);
                Debuger.printfError("***** onQuitFullscreen **** " + objects[0]);//title
                Debuger.printfError("***** onQuitFullscreen **** " + objects[1]);//当前非全屏player
                if (orientationUtils != null) {
                    orientationUtils.backToProtVideo();
                }
            }
        }).setLockClickListener(new LockClickListener() {
            @Override
            public void onClick(View view, boolean lock) {
                if (orientationUtils != null) {
                    //配合下方的onConfigurationChanged
                    orientationUtils.setEnable(!lock);
                }
            }
        }).build(detailPlayer);

detailPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        //直接横屏
        orientationUtils.resolveByClick();

        //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
        detailPlayer.startWindowFullscreen(SimpleDetailActivityMode2.this, true, true);
    }
});
```

3、配置生命周期

```
@Override
public void onBackPressed() {
    if (orientationUtils != null) {
        orientationUtils.backToProtVideo();
    }
    if (GSYVideoManager.backFromWindowFull(this)) {
        return;
    }
    super.onBackPressed();
}


@Override
protected void onPause() {
    detailPlayer.getCurrentPlayer().onVideoPause();
    super.onPause();
    isPause = true;
}

@Override
protected void onResume() {
    detailPlayer.getCurrentPlayer().onVideoResume(false);
    super.onResume();
    isPause = false;
}

@Override
protected void onDestroy() {
    super.onDestroy();
    if (isPlay) {
        detailPlayer.getCurrentPlayer().release();
    }
    if (orientationUtils != null)
        orientationUtils.releaseListener();
}



@Override
public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    //如果旋转了就全屏
    if (isPlay && !isPause) {
        detailPlayer.onConfigurationChanged(this, newConfig, orientationUtils, true, true);
    }
}
```


### 更多使用

（以下设置全局生效哦）

#### 切换内核
``` 
//PlayerFactory.setPlayManager(new Exo2PlayerManager());//EXO模式
//PlayerFactory.setPlayManager(new SystemPlayerManager());//系统模式
//PlayerFactory.setPlayManager(new IjkPlayerManager());//ijk模式
```

#### 调整代码结构，CacheFactory 更方便自定义，默认 ProxyCacheManager。

``` 
//CacheFactory.setCacheManager(new ExoPlayerCacheManager());//exo缓存模式，支持m3u8，只支持exo
//CacheFactory.setCacheManager(new ProxyCacheManager());//代理缓存模式，支持所有模式，不支持m3u8等
```

#### 增加 ExoMediaSourceInterceptListener，方便 Exo 模式下使用自定义的 MediaSource。

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



#### 切换比例

```
 
//默认显示比例
GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT);
    
//16:9 
GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_16_9);

//全屏裁减显示，为了显示正常 CoverImageView 建议使用FrameLayout作为父布局
GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_FULL);

//全屏拉伸显示，使用这个属性时，surface_container建议使用FrameLayout
GSYVideoType.setShowType(GSYVideoType.SCREEN_MATCH_FULL); 

 //4:3
GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_4_3); 

```

#### 切换渲染
```
//默认TextureView
GSYVideoType.setRenderType(GSYVideoType.TEXTURE);

//SurfaceView，动画切换等时候效果比较差
GSYVideoType.setRenderType(GSYVideoType.SUFRACE);

//GLSurfaceView、支持滤镜
GSYVideoType.setRenderType(GSYVideoType.GLSURFACE);

```

### 高级自定义

[--- 项目解析说明、包含项目架构和解析](https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/GSYVIDEO_PLAYER_PROJECT_INFO.md)***