## Three Simple Ways to Use

*Note: Do not forget to configure the manifest's `configChanges` for the Activity where the following methods are used.*
```xml
<activity
    android:name=".xxxxx"
    android:configChanges="keyboard|keyboardHidden|orientation|screenSize|screenLayout|smallestScreenSize|uiMode"
    android:screenOrientation="portrait" />
```

### 1. Direct Playback

#### [SimplePlayer](https://github.com/CarGuo/GSYVideoPlayer/blob/master/app/src/main/java/com/example/gsyvideoplayer/simple/SimplePlayer.java)

### 2. Usage in a List

#### Mode 1: [SimpleListVideoActivityMode1](https://github.com/CarGuo/GSYVideoPlayer/blob/master/app/src/main/java/com/example/gsyvideoplayer/simple/SimpleListVideoActivityMode1.java)

1.  Add the player control to the adapter's layout:
```xml
<com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
    android:id="@+id/detail_player"
    android:layout_width="match_parent"
    android:layout_height="@dimen/post_media_height" />
```

2.  Configure the player control in the adapter (you can also use the builder pattern, see the detail mode below):
```java
holder.gsyVideoPlayer.setUpLazy(url, true, null, null, "This is the title");
// Add title
holder.gsyVideoPlayer.getTitleTextView().setVisibility(View.GONE);
// Set back button
holder.gsyVideoPlayer.getBackButton().setVisibility(View.GONE);
// Set fullscreen button functionality
holder.gsyVideoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        holder.gsyVideoPlayer.startWindowFullscreen(context, false, true);
    }
});
// Set to prevent position mismatch
holder.gsyVideoPlayer.setPlayTag(TAG);
holder.gsyVideoPlayer.setPlayPosition(position);
// Whether to automatically select portrait or landscape fullscreen based on video size
holder.gsyVideoPlayer.setAutoFullWithSize(true);
// Whether to release when audio focus is lost
holder.gsyVideoPlayer.setReleaseWhenLossAudio(false);
// Fullscreen animation
holder.gsyVideoPlayer.setShowFullAnimation(true);
// Disable touch sliding in small screen mode
holder.gsyVideoPlayer.setIsTouchWiget(false);
```

3.  Configure the lifecycle in the Activity:
```java
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ···
        videoList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastVisibleItem = firstVisibleItem + visibleItemCount;
                // Greater than 0 means there is a playback
                if (GSYVideoManager.instance().getPlayPosition() >= 0) {
                    // Current playback position
                    int position = GSYVideoManager.instance().getPlayPosition();
                    // Corresponding playback list TAG
                    if (GSYVideoManager.instance().getPlayTag().equals(ListNormalAdapter.TAG)
                            && (position < firstVisibleItem || position > lastVisibleItem)) {
                        if(GSYVideoManager.isFullState(ListVideoActivity.this)) {
                            return;
                        }
                        // If scrolled out of view, release the player, similar to Toutiao
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

#### Mode 2: [SimpleListVideoActivityMode2](https://github.com/CarGuo/GSYVideoPlayer/blob/master/app/src/main/java/com/example/gsyvideoplayer/simple/SimpleListVideoActivityMode2.java)

1.  Add to the item layout:
```xml
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

2.  Create a helper and configure the lifecycle in the Activity:
```java
    GSYVideoHelper smallVideoHelper;
    ListVideoAdapter listVideoAdapter;
    GSYVideoHelper.GSYVideoHelperBuilder gsySmallVideoHelperBuilder;
    int lastVisibleItem;
    int firstVisibleItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_video2);

        // Create small window helper
        smallVideoHelper = new GSYVideoHelper(this);
        // Configuration
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
                // Greater than 0 means there is a playback, // Corresponding playback list TAG
                if (smallVideoHelper.getPlayPosition() >= 0 && smallVideoHelper.getPlayTAG().equals(ListVideoAdapter.TAG)) {
                    // Current playback position
                    int position = smallVideoHelper.getPlayPosition();
                    // When not visible
                    if ((position < firstVisibleItem || position > lastVisibleItem)) {
                        // Release the video
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
                // Greater than 0 means there is a playback, // Corresponding playback list TAG
                if (smallVideoHelper.getPlayPosition() >= 0 && smallVideoHelper.getPlayTAG().equals(ListVideoAdapter.TAG)) {
                    // Current playback position
                    int position = smallVideoHelper.getPlayPosition();
                    // When not visible
                    if ((position < firstVisibleItem || position > lastVisibleItem)) {
                        // If it's a small window, no need to handle
                        if (!smallVideoHelper.isSmall()) {
                            // Small window
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

3.  Use the helper in the adapter:
```java
···
smallVideoHelper.addVideoPlayer(position, holder.imageView, TAG, holder.videoContainer, holder.playerBtn);

holder.playerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyDataSetChanged();
                smallVideoHelper.setPlayPositionAndTag(position, TAG);
                final String url = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
                gsySmallVideoHelperBuilder.setVideoTitle("title " + position)
                        .setUrl(url);
                smallVideoHelper.startPlay();
            }
});
···
```

### 3. Playback on a Detail Page

#### Mode 1: [SimpleDetailActivityMode1](https://github.com/CarGuo/GSYVideoPlayer/blob/master/app/src/main/java/com/example/gsyvideoplayer/simple/SimpleDetailActivityMode1.java)

1.  Add the player control to the layout:
```xml
<com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
    android:id="@+id/detail_player"
    android:layout_width="match_parent"
    android:layout_height="@dimen/post_media_height" />
```

2.  Make the Activity inherit **GSYBaseActivityDetail**:
```java
public class DetailControlActivity extends GSYBaseActivityDetail<StandardGSYVideoPlayer>
```

3.  Override configuration:
```java
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_player);

        detailPlayer = (StandardGSYVideoPlayer) findViewById(R.id.detail_player);
        // Add title
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
        // For a built-in cover, refer to SampleCoverVideo
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
     * Whether to enable auto-rotation to landscape, true means enabled
     */
    @Override
    public boolean getDetailOrientationRotateAuto() {
        return true;
    }
```

#### Mode 2: [SimpleDetailActivityMode2](https://github.com/CarGuo/GSYVideoPlayer/blob/master/app/src/main/java/com/example/gsyvideoplayer/simple/SimpleDetailActivityMode2.java)

1.  Add the player control to the layout:
```xml
<com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
    android:id="@+id/detail_player"
    android:layout_width="match_parent"
    android:layout_height="@dimen/post_media_height" />
```

2.  In `onCreate`, configure the player, add external rotation support, and add listeners (you can also set it directly through the player without using the builder):
```java
// External helper for rotation, helps with fullscreen
orientationUtils = new OrientationUtils(this, detailPlayer);
// Do not enable external rotation initially
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
        .setVideoTitle("Test Video")
        .setVideoAllCallBack(new GSYSampleCallBack() {
            @Override
            public void onPrepared(String url, Object... objects) {
                super.onPrepared(url, objects);
                // Can only rotate and go fullscreen after playback starts
                orientationUtils.setEnable(true);
                isPlay = true;
            }

            @Override
            public void onQuitFullscreen(String url, Object... objects) {
                super.onQuitFullscreen(url, objects);
                Debuger.printfError("***** onQuitFullscreen **** " + objects[0]);//title
                Debuger.printfError("***** onQuitFullscreen **** " + objects[1]);//current non-fullscreen player
                if (orientationUtils != null) {
                    orientationUtils.backToProtVideo();
                }
            }
        }).setLockClickListener(new LockClickListener() {
            @Override
            public void onClick(View view, boolean lock) {
                if (orientationUtils != null) {
                    // Cooperate with onConfigurationChanged below
                    orientationUtils.setEnable(!lock);
                }
            }
        }).build(detailPlayer);

detailPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        // Directly go to landscape
        orientationUtils.resolveByClick();

        // The first true is whether to hide the action bar, the second true is whether to hide the status bar
        detailPlayer.startWindowFullscreen(SimpleDetailActivityMode2.this, true, true);
    }
});
```

3.  Configure the lifecycle:
```java
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
    // If rotated, go fullscreen
    if (isPlay && !isPause) {
        detailPlayer.onConfigurationChanged(this, newConfig, orientationUtils, true, true);
    }
}
```

### More Usage

(The following settings are global)

#### Switching Player Core
```java
PlayerFactory.setPlayManager(Exo2PlayerManager.class); // EXO mode
PlayerFactory.setPlayManager(SystemPlayerManager.class); // System mode
PlayerFactory.setPlayManager(IjkPlayerManager.class); // ijk mode
PlayerFactory.setPlayManager(AliPlayerManager.class); // aliplay core mode
```

#### Code structure adjustment, `CacheFactory` is more convenient for customization, defaults to `ProxyCacheManager`.
```java
//CacheFactory.setCacheManager(new ExoPlayerCacheManager()); // exo cache mode, supports m3u8, only for exo
//CacheFactory.setCacheManager(new ProxyCacheManager()); // proxy cache mode, supports all modes, does not support m3u8, etc.
```

#### Added `ExoMediaSourceInterceptListener` to facilitate the use of custom `MediaSource` in Exo mode.
```java
ExoSourceManager.setExoMediaSourceInterceptListener(new ExoMediaSourceInterceptListener() {
           /**
            * @param dataSource  URL
            * @param preview     Whether to include headers, defaults to true if headers are present
            * @param cacheEnable Whether caching is needed
            * @param isLooping   Whether to loop
            * @param cacheDir    Custom cache directory
            * @return If not null, the returned custom mediaSource will be used
            */
            @Override
            public MediaSource getMediaSource(String dataSource, boolean preview, boolean cacheEnable, boolean isLooping, File cacheDir) {
                return null;
            }
});
```

#### Switching Aspect Ratio
```java
// Default aspect ratio
GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT);

// 16:9
GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_16_9);

// Fullscreen crop, for normal display, it is recommended to use FrameLayout as the parent layout for CoverImageView
GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_FULL);

// Fullscreen stretch, when using this attribute, it is recommended to use FrameLayout for surface_container
GSYVideoType.setShowType(GSYVideoType.SCREEN_MATCH_FULL);

// 4:3
GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_4_3);
```

#### Switching Renderer
```java
// Default TextureView
GSYVideoType.setRenderType(GSYVideoType.TEXTURE);

// SurfaceView, the effect is poor during animation switching, etc.
GSYVideoType.setRenderType(GSYVideoType.SUFRACE);

// GLSurfaceView, supports filters
GSYVideoType.setRenderType(GSYVideoType.GLSURFACE);
```

### Advanced Customization

[--- Project Analysis, including project architecture and analysis](https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/GSYVIDEO_PLAYER_PROJECT_INFO.md)***
