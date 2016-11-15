<h4>基于IJKPlayer的播放器，直接参考了<a href="https://github.com/lipangit/JieCaoVideoPlayer">JieCaoVideoPlayer</a>进行了一些自己想要的调整</h4>
======================
```
<dependency>
  <groupId>com.shuyu</groupId>
  <artifactId>gsyVideoPlayer</artifactId>
  <version>1.1.1</version>
  <type>pom</type>
</dependency>
```
```

compile 'com.shuyu:gsyVideoPlayer:1.1.1'

```

* <h4>支持基本的拖动，声音、亮度调节</h4>
* <h4>支持边播边缓存，使用了<a href="https://github.com/danikula/AndroidVideoCache">AndroidVideoCache</a>的代理模式实现</h4>
* <h4>支持视频本身自带rotation的旋转</h4>
* <h4>增加了重力旋转与手动旋转的同步支持</h4>
* <h4>支持列表播放</h4>
* <h4>直接添加控件为封面</h4>
* <h4>兼容一些5.0的过场效果</h4>

## 效果,录屏下的屏幕旋转和实际有些出入
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/01.jpg" width="218px" height="120px"/>
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/02.jpg" width="120px" height="218px"/>
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/03.jpg" width="120px" height="218px"/>
<p></p>
* <h4>1、打开一个播放</h4>
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/01.gif" width="240px" height="426px"/>
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/02.gif" width="240px" height="426px"/>
* <h4>2、列表</h4>
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/03.gif" width="240px" height="426px"/>
* <h4>3、详情模式</h4>
<img src="https://github.com/CarGuo/GSYVideoPlayer/blob/master/04.gif" width="240px" height="426px"/>

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
### 混淆
```
-keep class tv.danmaku.ijk.** { *; }
-dontwarn tv.danmaku.ijk.**
```

* <h4>//TODO 优化全屏的动画和全屏的效果</h4>
* <h4>//TODO 优化接入的效果</h4>