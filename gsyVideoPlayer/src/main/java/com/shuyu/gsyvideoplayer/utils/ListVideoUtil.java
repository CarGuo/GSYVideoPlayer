package com.shuyu.gsyvideoplayer.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.shuyu.gsyvideoplayer.GSYVideoPlayer;
import com.shuyu.gsyvideoplayer.R;
import com.shuyu.gsyvideoplayer.listener.StandardVideoAllCallBack;
import com.shuyu.gsyvideoplayer.video.GSYBaseVideoPlayer;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.transitionseverywhere.TransitionManager;

import java.io.File;
import java.util.Map;

import static com.shuyu.gsyvideoplayer.utils.CommonUtil.getActionBarHeight;
import static com.shuyu.gsyvideoplayer.utils.CommonUtil.getStatusBarHeight;
import static com.shuyu.gsyvideoplayer.utils.CommonUtil.hideNavKey;
import static com.shuyu.gsyvideoplayer.utils.CommonUtil.showNavKey;

/**
 * Created by shuyu on 2016/11/12.
 * 列表工具类
 * 其中记得设置进来的fullViewContainer必须是在Activity布局下的最外层布局
 */

public class ListVideoUtil {

    private String TAG = "NULL"; //播放的标志
    private StandardGSYVideoPlayer gsyVideoPlayer;
    private ViewGroup fullViewContainer;
    private ViewGroup listParent;//记录列表中item的父布局
    private ViewGroup.LayoutParams listParams;
    private OrientationUtils orientationUtils;
    private StandardVideoAllCallBack videoAllCallBack;
    private String url;
    private Context context;
    private File cachePath;

    private Object[] objects;

    private Map<String, String> mapHeadData;

    private int playPosition = -1; // 播放的位置
    private int speed = 1; // 播放速度，仅支持6.0
    private int systemUiVisibility;
    private boolean isFull; //当前是否全屏
    private boolean isSmall; //当前是否小屏
    private boolean hideStatusBar; //是否隐藏有状态bar
    private boolean hideActionBar; //是否隐藏有状态ActionBar
    private boolean isLoop;//循环
    private boolean hideKey = true;//隐藏按键
    private boolean needLockFull = true;//隐藏按键
    protected boolean needShowWifiTip = true; //是否需要显示流量提示


    private int[] listItemRect;//当前item框的屏幕位置
    private int[] listItemSize;//当前item的大小


    private boolean fullLandFrist = true; //是否全屏就马上横屏
    private boolean autoRotation = true;//是否自动旋转
    private boolean showFullAnimation = true;//是否需要全屏动画

    private Handler handler = new Handler();


    public ListVideoUtil(Context context) {
        gsyVideoPlayer = new StandardGSYVideoPlayer(context);
        this.context = context;
    }

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
                               ViewGroup container, View playBtn) {
        container.removeAllViews();
        if (isCurrentViewPlaying(position, tag)) {
            if (!isFull) {
                ViewGroup viewGroup = (ViewGroup) gsyVideoPlayer.getParent();
                if (viewGroup != null)
                    viewGroup.removeAllViews();
                container.addView(gsyVideoPlayer);
                playBtn.setVisibility(View.INVISIBLE);
            }
        } else {
            playBtn.setVisibility(View.VISIBLE);
            container.removeAllViews();   //增加封面
            container.addView(imgView);
        }
    }

    /**
     * 设置列表播放中的位置和TAG,防止错位，回复播放位置
     *
     * @param playPosition 列表中的播放位置
     * @param tag          播放的是哪个列表的tag
     */
    public void setPlayPositionAndTag(int playPosition, String tag) {
        this.playPosition = playPosition;
        this.TAG = tag;
    }

    /**
     * 开始播放
     *
     * @param url 播放的URL
     */
    public void startPlay(String url) {

        if (isSmall()) {
            smallVideoToNormal();
        }

        this.url = url;

        gsyVideoPlayer.release();

        gsyVideoPlayer.setLooping(isLoop);

        gsyVideoPlayer.setSpeed(speed);

        gsyVideoPlayer.setNeedShowWifiTip(needShowWifiTip);

        gsyVideoPlayer.setNeedLockFull(needLockFull);

        gsyVideoPlayer.setUp(url, true, cachePath, mapHeadData, objects);

        if(objects != null && objects.length > 0) {
            gsyVideoPlayer.getTitleTextView().setText((String)objects[0]);
        }

        //增加title
        gsyVideoPlayer.getTitleTextView().setVisibility(View.GONE);

        //设置返回键
        gsyVideoPlayer.getBackButton().setVisibility(View.GONE);

        //设置全屏按键功能
        gsyVideoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resolveFullBtn();
            }
        });

        gsyVideoPlayer.startPlayLogic();
    }


    public void resolveFullBtn() {
        if (fullViewContainer == null) {
            return;
        }
        if (!isFull) {
            resolveToFull();
        } else {
            resolveMaterialToNormal(gsyVideoPlayer);
        }
    }

    /**
     * 处理全屏逻辑
     */
    private void resolveToFull() {
        systemUiVisibility = ((Activity) context).getWindow().getDecorView().getSystemUiVisibility();
        CommonUtil.hideSupportActionBar(context, hideActionBar, hideStatusBar);
        if (hideKey) {
            hideNavKey(context);
        }
        isFull = true;
        ViewGroup viewGroup = (ViewGroup) gsyVideoPlayer.getParent();
        listParams = gsyVideoPlayer.getLayoutParams();
        if (viewGroup != null) {
            listParent = viewGroup;
            viewGroup.removeView(gsyVideoPlayer);
        }
        gsyVideoPlayer.setIfCurrentIsFullscreen(true);
        gsyVideoPlayer.getFullscreenButton().setImageResource(gsyVideoPlayer.getShrinkImageRes());
        gsyVideoPlayer.getBackButton().setVisibility(View.VISIBLE);
        //设置旋转
        orientationUtils = new OrientationUtils((Activity) context, gsyVideoPlayer);
        orientationUtils.setEnable(isAutoRotation());
        gsyVideoPlayer.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resolveMaterialToNormal(gsyVideoPlayer);
            }
        });
        if (showFullAnimation) {
            if (fullViewContainer instanceof FrameLayout) {
                //目前只做了frameLoayout的判断
                resolveMaterialAnimation();
            } else {
                resolveFullAdd();
            }

        } else {
            resolveFullAdd();
        }
    }

    /**
     * 添加到全屏父布局里
     */
    private void resolveFullAdd() {
        fullViewContainer.setBackgroundColor(Color.BLACK);
        fullViewContainer.addView(gsyVideoPlayer);
        resolveChangeFirstLogic(50);
    }

    /**
     * 如果是5.0的动画开始位置
     */
    private void resolveMaterialAnimation() {
        listItemRect = new int[2];
        listItemSize = new int[2];
        saveLocationStatus(context, hideStatusBar, hideActionBar);
        FrameLayout.LayoutParams lpParent = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setBackgroundColor(Color.BLACK);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(listItemSize[0], listItemSize[1]);
        lp.setMargins(listItemRect[0], listItemRect[1], 0, 0);
        frameLayout.addView(gsyVideoPlayer, lp);
        fullViewContainer.addView(frameLayout, lpParent);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //开始动画
                TransitionManager.beginDelayedTransition(fullViewContainer);
                resolveMaterialFullVideoShow(gsyVideoPlayer);
                resolveChangeFirstLogic(600);
            }
        }, 300);
    }

    /**
     * 如果是5.0的，要从原位置过度到全屏位置
     */
    private void resolveMaterialFullVideoShow(GSYBaseVideoPlayer gsyVideoPlayer) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) gsyVideoPlayer.getLayoutParams();
        lp.setMargins(0, 0, 0, 0);
        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.gravity = Gravity.CENTER;
        gsyVideoPlayer.setLayoutParams(lp);
        gsyVideoPlayer.setIfCurrentIsFullscreen(true);
    }


    /**
     * 处理正常逻辑
     */
    private void resolveToNormal() {
        int delay = orientationUtils.backToProtVideo();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isFull = false;
                fullViewContainer.removeAllViews();
                if (gsyVideoPlayer.getParent() != null) {
                    ((ViewGroup) gsyVideoPlayer.getParent()).removeView(gsyVideoPlayer);
                }
                orientationUtils.setEnable(false);
                gsyVideoPlayer.setIfCurrentIsFullscreen(false);
                fullViewContainer.setBackgroundColor(Color.TRANSPARENT);
                listParent.addView(gsyVideoPlayer, listParams);
                gsyVideoPlayer.getFullscreenButton().setImageResource(gsyVideoPlayer.getEnlargeImageRes());
                gsyVideoPlayer.getBackButton().setVisibility(View.GONE);
                gsyVideoPlayer.setIfCurrentIsFullscreen(false);
                if (videoAllCallBack != null) {
                    Debuger.printfLog("onQuitFullscreen");
                    videoAllCallBack.onQuitFullscreen(url);
                }
                if (hideKey) {
                    showNavKey(context, systemUiVisibility);
                }
                CommonUtil.showSupportActionBar(context, hideActionBar, hideStatusBar);
            }
        }, delay);
    }


    /**
     * 动画回到正常效果
     */
    private void resolveMaterialToNormal(final GSYVideoPlayer gsyVideoPlayer) {
        if (showFullAnimation && fullViewContainer instanceof FrameLayout) {
            int delay = orientationUtils.backToProtVideo();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    TransitionManager.beginDelayedTransition(fullViewContainer);
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) gsyVideoPlayer.getLayoutParams();
                    lp.setMargins(listItemRect[0], listItemRect[1], 0, 0);
                    lp.width = listItemSize[0];
                    lp.height = listItemSize[1];
                    //注意配置回来，不然动画效果会不对
                    lp.gravity = Gravity.NO_GRAVITY;
                    gsyVideoPlayer.setLayoutParams(lp);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            resolveToNormal();
                        }
                    }, 400);
                }
            }, delay);
        } else {
            resolveToNormal();
        }
    }


    /**
     * 是否全屏一开始马上自动横屏
     */
    private void resolveChangeFirstLogic(int time) {
        if (isFullLandFrist()) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (orientationUtils.getIsLand() != 1) {
                        orientationUtils.resolveByClick();
                    }
                }
            }, time);
        }
        gsyVideoPlayer.setIfCurrentIsFullscreen(true);
        if (videoAllCallBack != null) {
            Debuger.printfLog("onEnterFullscreen");
            videoAllCallBack.onEnterFullscreen(this.url);
        }
    }

    /**
     * 保存大小和状态
     */
    private void saveLocationStatus(Context context, boolean statusBar, boolean actionBar) {
        listParent.getLocationOnScreen(listItemRect);
        int statusBarH = getStatusBarHeight(context);
        int actionBerH = getActionBarHeight((Activity) context);
        if (statusBar) {
            listItemRect[1] = listItemRect[1] - statusBarH;
        }
        if (actionBar) {
            listItemRect[1] = listItemRect[1] - actionBerH;
        }
        listItemSize[0] = listParent.getWidth();
        listItemSize[1] = listParent.getHeight();
    }


    /**
     * 是否当前播放
     */
    private boolean isPlayView(int position, String tag) {
        return playPosition == position && TAG.equals(tag);
    }

    private boolean isCurrentViewPlaying(int position, String tag) {
        return isPlayView(position, tag);
    }

    /**
     * 处理返回正常逻辑
     */
    public boolean backFromFull() {
        boolean isFull = false;
        if (fullViewContainer.getChildCount() > 0) {
            isFull = true;
            resolveMaterialToNormal(gsyVideoPlayer);
        }
        return isFull;
    }

    /**
     * 释放持有的视频
     */
    public void releaseVideoPlayer() {
        ViewGroup viewGroup = (ViewGroup) gsyVideoPlayer.getParent();
        if (viewGroup != null)
            viewGroup.removeAllViews();
        playPosition = -1;
        TAG = "NULL";
        if (orientationUtils != null)
            orientationUtils.releaseListener();

    }

    /**
     * 显示小屏幕效果
     *
     * @param size      小视频的大小
     * @param actionBar 是否有actionBar
     * @param statusBar 是否有状态栏
     */
    public void showSmallVideo(Point size, final boolean actionBar, final boolean statusBar) {
        if (gsyVideoPlayer.getCurrentState() == GSYVideoPlayer.CURRENT_STATE_PLAYING) {
            gsyVideoPlayer.showSmallVideo(size, actionBar, statusBar);
            isSmall = true;
        }
    }


    /**
     * 恢复小屏幕效果
     */
    public void smallVideoToNormal() {
        isSmall = false;
        gsyVideoPlayer.hideSmallVideo();
    }


    /**
     * 设置全屏显示的viewGroup
     *
     * @param fullViewContainer viewGroup
     */
    public void setFullViewContainer(ViewGroup fullViewContainer) {
        this.fullViewContainer = fullViewContainer;
    }

    /**
     * 是否全屏
     */
    public boolean isFull() {
        return isFull;
    }

    /**
     * 是否自动旋转
     *
     * @param autoRotation 是否要支持重力旋转
     */
    public void setAutoRotation(boolean autoRotation) {
        this.autoRotation = autoRotation;
    }

    public boolean isAutoRotation() {
        return autoRotation;
    }

    /**
     * 是否全屏就马上横屏
     *
     * @param fullLandFrist 如果是，那么全屏的时候就会切换到横屏
     */
    public void setFullLandFrist(boolean fullLandFrist) {
        this.fullLandFrist = fullLandFrist;
    }

    public boolean isFullLandFrist() {
        return fullLandFrist;
    }

    /**
     * 全屏动画
     *
     * @param showFullAnimation 是否使用全屏动画效果
     */
    public void setShowFullAnimation(boolean showFullAnimation) {
        this.showFullAnimation = showFullAnimation;
    }

    public boolean isShowFullAnimation() {
        return showFullAnimation;
    }


    public boolean isHideStatusBar() {
        return hideStatusBar;
    }

    /**
     * 是否隐藏statusBar
     *
     * @param hideStatusBar true的话会隐藏statusBar，在退出全屏的时候会回复显示
     */
    public void setHideStatusBar(boolean hideStatusBar) {
        this.hideStatusBar = hideStatusBar;
    }

    public boolean isHideActionBar() {
        return hideActionBar;
    }

    /**
     * 是否隐藏actionBar
     *
     * @param hideActionBar true的话会隐藏actionbar，在退出全屏的会回复时候显示
     */
    public void setHideActionBar(boolean hideActionBar) {
        this.hideActionBar = hideActionBar;
    }

    /**
     * 视频接口回调
     *
     * @param videoAllCallBack 回调
     */
    public void setVideoAllCallBack(StandardVideoAllCallBack videoAllCallBack) {
        this.videoAllCallBack = videoAllCallBack;
        gsyVideoPlayer.setStandardVideoAllCallBack(videoAllCallBack);
    }

    public int getPlayPosition() {
        return playPosition;
    }

    public String getPlayTAG() {
        return TAG;
    }

    public boolean isSmall() {
        return isSmall;
    }


    public boolean isLoop() {
        return isLoop;
    }

    /**
     * 循环
     */
    public void setLoop(boolean loop) {
        isLoop = loop;
    }

    /**
     * 获取当前总时长
     */
    public int getDuration() {
        return gsyVideoPlayer.getDuration();
    }


    public int getSpeed() {
        return speed;
    }

    /**
     * 播放速度，仅支持6.0
     */
    public void setSpeed(int speed) {
        this.speed = speed;
    }


    public File getCachePath() {
        return cachePath;
    }

    /**
     * 缓存的路径
     */
    public void setCachePath(File cachePath) {
        this.cachePath = cachePath;
    }

    public Object[] getObjects() {
        return objects;
    }

    public void setObjects(Object[] objects) {
        this.objects = objects;
    }

    public Map<String, String> getMapHeadData() {
        return mapHeadData;
    }

    public void setMapHeadData(Map<String, String> mapHeadData) {
        this.mapHeadData = mapHeadData;
    }

    /**
     * 获取当前播放进度
     */
    public int getCurrentPositionWhenPlaying() {
        return gsyVideoPlayer.getCurrentPositionWhenPlaying();
    }

    /**
     * 获取播放器,直接拿播放器，根据需要自定义配置
     */
    public StandardGSYVideoPlayer getGsyVideoPlayer() {
        return gsyVideoPlayer;
    }

    public boolean isHideKey() {
        return hideKey;
    }

    /**
     * 隐藏虚拟按键
     */
    public void setHideKey(boolean hideKey) {
        this.hideKey = hideKey;
    }

    public boolean isNeedLockFull() {
        return needLockFull;
    }

    /**
     * 是否需要全屏锁定屏幕功能
     */
    public void setNeedLockFull(boolean needLoadFull) {
        this.needLockFull = needLoadFull;
    }

    public boolean isNeedShowWifiTip() {
        return needShowWifiTip;
    }

    /**
     * 是否需要显示流量提示,默认true
     */
    public void setNeedShowWifiTip(boolean needShowWifiTip) {
        this.needShowWifiTip = needShowWifiTip;
    }

}
