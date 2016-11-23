<h4>基于IJKPlayer的播放器，重构了<a href="https://github.com/lipangit/JieCaoVideoPlayer">JieCaoVideoPlayer</a>做了代码重构与优化调整，添加了自定义功能。</h4>
======================

* <h4>支持基本的拖动，声音、亮度调节</h4>
* <h4>支持边播边缓存，使用了<a href="https://github.com/danikula/AndroidVideoCache">AndroidVideoCache</a>的代理模式实现</h4>
* <h4>支持视频本身自带rotation的旋转</h4>
* <h4>增加了重力旋转与手动旋转的同步支持</h4>
* <h4>支持列表播放</h4>
* <h4>直接添加控件为封面</h4>
* <h4>兼容一些5.0的过场效果</h4>
* <h4>列表的全屏效果优化</h4>
* <h4>列表的小窗口播放，可拖动</h4>

## [简书详解入口](http://www.jianshu.com/p/9fe377dd9750)

```
<dependency>
  <groupId>com.shuyu</groupId>
  <artifactId>gsyVideoPlayer</artifactId>
  <version>1.1.9</version>
  <type>pom</type>
</dependency>
```

```
compile 'com.shuyu:gsyVideoPlayer:1.1.9'
```

## 效果,录屏下的屏幕旋转和实际有些出入
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/01.jpg" width="218px" height="120px"/>
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/02.jpg" width="120px" height="218px"/>
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/03.jpg" width="120px" height="218px"/>
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/04.jpg" width="120px" height="218px"/>
<p></p>

* <h4>1、打开一个播放</h4>
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/01.gif" width="240px" height="426px"/>

* <h4>2、列表</h4>
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/02.gif" width="240px" height="426px"/>
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/05.gif" width="240px" height="426px"/>

* <h4>3、详情模式</h4>
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/04.gif" width="240px" height="426px"/>

### 1.1.9 修正了回调接口VideoAllCallBack的回调结果，添加了注释，可以根据需要继承后覆写。

有全屏到非全屏，有小窗口到非小窗口，结束播放错误触摸等等的接口回调，增加了Debuger，可以使能或者关闭调试输出。

### 1.1.8 增加了如果Cache文件出现播放异常，就清除缓存文件的处理（预防），StandardGSYVideoPlayer增加了一些UI配置接口

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

## 1.1.7 增加了第二种列表 ListVideoUtil可拖动小窗口支持

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

## 1.1.6 优化了第二种列表ListVideoUtil的全屏效果，和列表一的全屏效果一致，两种全屏效果增加是否打开关闭接口。

```
/**
 * 全屏动画
 *
 * @param showFullAnimation 是否使用全屏动画效果
 */
public void setShowFullAnimation(boolean showFullAnimation)
```

## 1.1.5 优化了一些UI，增加了一些有趣的动画，比如播放按键。

* 推荐这个动画效果<a href="https://github.com/codeestX/ENViews">ENViews</a>
* 增加自定义继承模板**SampleExtendsPlayer**，个人建议直接拷贝**StandardGSYVideoPlayer**修改也行。

## 1.1.4
* 优化了第一种列表的全屏动画,5.0以上展开和返回过渡顺畅，支持自动旋转的开启与关闭
* 修改了全屏下的滑动接口不正常问题，全屏下自动变为滑动的，非全屏可以设置。

```
/**
 * 是否可以滑动界面改变进度，声音等
 */
public void setIsTouchWiget(boolean isTouchWiget)

```


## 1.1.2
* 增加了TAG和position来实现第一种list列表（非ListVideoUtil模式的列表实现）的滑动错位问题

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

## 1.1.1
* 增加了ListVideoUtil全屏是否显示横屏，全屏是否自动旋转
* 增加了ListVideoUtils隐藏状态栏和title的接口



## GSYVideoPlayer 播放器控件，抽象类，继承后可以直接使用，参考 StandardGSYVideoPlayer

## 记得调用销毁
```
@Override
 protected void onDestroy() {
     super.onDestroy();
     GSYVideoPlayer.releaseAllVideos();
}
```

## StandardGSYVideoPlayer 标准的播放播放器,可参考demo中的playActivity
```

设置播放url，第二个参数表示需要边播边缓存
videoPlayer.setUp(url, true, "");

//增加封面
ImageView imageView = new ImageView(this);
imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
imageView.setImageResource(R.mipmap.xxx1);
videoPlayer.setThumbImageView(imageView);

//增加title
videoPlayer.getTitleTextView().setVisibility(View.VISIBLE);
videoPlayer.getTitleTextView().setText("测试视频");

//设置返回键
videoPlayer.getBackButton().setVisibility(View.VISIBLE);

//设置旋转
orientationUtils = new OrientationUtils(this, videoPlayer);

//设置全屏按键功能
videoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        orientationUtils.resolveByClick();
    }
});

//设置返回按键功能
videoPlayer.getBackButton().setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        onBackPressed();
    }
});

//在列表中使用的接口，详情请看Demo中的ListVideoActivity

/**
 * 利用window层播放全屏效果
 *
 * @param context
 * @param actionBar 是否有actionBar，有的话需要隐藏
 * @param statusBar 是否有状态bar，有的话需要隐藏
 */
public void startWindowFullscreen(final Context context, final boolean actionBar, final boolean statusBar)


/**
 * 退出window层播放全屏效果
 */
public void clearFullscreenLayout()

还有用于onBackPressed()的
/**
  * 退出全屏
  *
  */
public static boolean backFromWindowFull(Context context)

@Override
public void onBackPressed() {
    if (StandardGSYVideoPlayer.backFromWindowFull(this)) {
        return;
    }
    super.onBackPressed();
}
```

## OrientationUtils 重力旋转的工具类
```
//设置旋转
OrientationUtils orientationUtils = new OrientationUtils(Activity, videoPlayer);
```
## ListVideoUtil 列表模式支持支持滑出屏幕继续播放和全屏的工具类
与上面的StandardGSYVideoPlayer实现列表播放和全屏播放不大一样，ListVideoUtil只有一个StandardGSYVideoPlayer，使用外部container来是先全屏
列表滑动不会影响到播放，具体可以查看demo里的ListVideoActivity和ListVideo2Activity.
```
listVideoUtil = new ListVideoUtil(Activity);
设置需要全屏显示的父类
listVideoUtil.setFullViewContainer(videoFullContainer);
.....

//对列表进行处理，传入每个item的位置，封面，TAG(如果有多个不同列表，用不同TAG区分)，视频列表item的父容器，播放按键
/**
 * 动态添加视频播放
 *
 * @param position  位置
 * @param imgView   封面
 * @param tag       TAG类型
 * @param container player的容器
 * @param playBtn   播放按键
 */
public void addVideoPlayer(final int position, View imgView, String tag,
                               ViewGroup container, View playBtn)

holder.playerBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        notifyDataSetChanged();
        //设置播放的位置和TAG
        listVideoUtil.setPlayPositionAndTag(position, TAG);
        final String url = "http://baobab.wdjcdn.com/14564977406580.mp4";
        //开始播放
        listVideoUtil.startPlay(url);
    }
});


/**
 * 是否自动旋转
 *
 * @param autoRotation 是否要支持重力旋转
 */
public void setAutoRotation(boolean autoRotation)


/**
 * 是否全屏就马上横屏
 *
 * @param fullLandFrist 如果是，那么全屏的时候就会切换到横屏
 */
public void setFullLandFrist(boolean fullLandFrist)

/**
 * 是否隐藏statusBar
 *
 * @param hideStatusBar true的话会隐藏statusBar，在退出全屏的时候会回复显示
 */
public void setHideStatusBar(boolean hideStatusBar)

/**
 * 是否隐藏actionBar
 *
 * @param hideActionBar true的话会隐藏actionbar，在退出全屏的会回复时候显示
 */
public void setHideActionBar(boolean hideActionBar)


```

### SampleExtendsPlayer 自定义继承模板，可以参考 StandardGSYVideoPlayer

```

/**
 * 必须继承，你可以随意布局，但是id必须一致，还有不能少了哟
 */
@Override
public int getLayoutId() {
    //// TODO: 2016/11/18 返回你的布局
    return R.layout.video_layout_standard;
}

/**
 * 必须继承，根据你的状态实现不同的逻辑效果
 */
@Override
protected void setStateAndUi(int state) {
    super.setStateAndUi(state);
    switch (mCurrentState) {
        case CURRENT_STATE_NORMAL://播放UI初始化
            break;
        case CURRENT_STATE_PREPAREING://播放loading
            break;
        case CURRENT_STATE_PLAYING://播放ing
            break;
        case CURRENT_STATE_PAUSE://播放暂停
            break;
        case CURRENT_STATE_ERROR://播放错误
            break;
        case CURRENT_STATE_AUTO_COMPLETE://播放完成
            break;
        case CURRENT_STATE_PLAYING_BUFFERING_START://buffering
            break;
    }
}


/**
 * 继承后可以实现你自定义的UI配置
 */
@Override
protected void init(Context context) {
    super.init(context);
    //// TODO: 2016/11/18 你自定义的UI配置
}

/**
 * 继承后可以实现你自定义的UI
 */
@Override
public boolean setUp(String url, boolean cacheWithPlay, Object... objects) {
    super.setUp(url, cacheWithPlay, objects);
    //// TODO: 2016/11/18 你自定义的设置播放属性播放时候的UI配置
    return false;
}

@Override
public boolean onTouch(View v, MotionEvent event) {
    //// TODO: 2016/11/18 补充你的触摸逻辑 ，比如你在这一页自定义的哪些UI
    return super.onTouch(v, event);
}

@Override
public void onClick(View v) {
    super.onClick(v);
    //// TODO: 2016/11/18 补充你的点击逻辑 ，比如你在这一页自定义的哪些UI
}

@Override
public void showWifiDialog() {
    super.showWifiDialog();
    //// TODO: 2016/11/18 wifi状态的显示逻辑
}


@Override
public void startPlayLogic() {
    //// TODO: 2016/11/18 播放开始的逻辑
}

@Override
protected void setProgressAndTime(int progress, int secProgress, int currentTime, int totalTime) {
    super.setProgressAndTime(progress, secProgress, currentTime, totalTime);
    //// TODO: 2016/11/18 播放进度
}

@Override
protected void resetProgressAndTime() {
    super.resetProgressAndTime();
    //// TODO: 2016/11/18 播放进度重置
}

@Override
protected void showProgressDialog(float deltaX, String seekTime, int seekTimePosition, String totalTime, int totalTimeDuration) {
    super.showProgressDialog(deltaX, seekTime, seekTimePosition, totalTime, totalTimeDuration);
    //// TODO: 2016/11/18 显示快进dialog
}

@Override
protected void dismissProgressDialog() {
    super.dismissProgressDialog();
    //// TODO: 2016/11/18 关闭快进dialog
}

@Override
protected void showVolumeDialog(float deltaY, int volumePercent) {
    super.showVolumeDialog(deltaY, volumePercent);
    //// TODO: 2016/11/18 显示声音选择
}

@Override
protected void dismissVolumeDialog() {
    super.dismissVolumeDialog();
    //// TODO: 2016/11/18 关闭声音选择
}

@Override
protected void showBrightnessDialog(float percent) {
    super.showBrightnessDialog(percent);
    //// TODO: 2016/11/18 显示亮度选择
}

@Override
protected void dismissBrightnessDialog() {
    super.dismissVolumeDialog();
    //// TODO: 2016/11/18 关闭亮度选择
}

@Override
public void onBackFullscreen() {
    //// TODO: 2016/11/18 退出全屏逻辑
}

```

</p>

### 混淆
</p>

```
-keep class tv.danmaku.ijk.** { *; }
-dontwarn tv.danmaku.ijk.**
```

//TODO 增加了一些UI个性化设置的接口，调整UI的