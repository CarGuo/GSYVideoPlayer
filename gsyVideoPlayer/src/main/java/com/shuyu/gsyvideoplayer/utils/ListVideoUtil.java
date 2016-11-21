package com.shuyu.gsyvideoplayer.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.shuyu.gsyvideoplayer.GSYVideoPlayer;
import com.shuyu.gsyvideoplayer.R;
import com.shuyu.gsyvideoplayer.video.GSYBaseVideoPlayer;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import static com.shuyu.gsyvideoplayer.utils.CommonUtil.getActionBarHeight;
import static com.shuyu.gsyvideoplayer.utils.CommonUtil.getStatusBarHeight;

/**
 * Created by shuyu on 2016/11/12.
 */

public class ListVideoUtil {

    private String TAG = "NULL"; //播放的标志
    private StandardGSYVideoPlayer gsyVideoPlayer;
    private ViewGroup fullViewContainer;
    private ViewGroup listParent;//记录列表中item的父布局
    private ViewGroup.LayoutParams listParams;
    private OrientationUtils orientationUtils;
    private Context context;

    private int playPosition = -1; // 播放的位置
    private boolean isFull; //当前是否全屏
    private boolean isSmall; //当前是否小屏
    private boolean autoRotation = true;//是否自动旋转
    private boolean fullLandFrist = true; //是否全屏就马上横屏
    private boolean hideStatusBar; //是否隐藏有状态bar
    private boolean hideActionBar; //是否隐藏有状态ActionBar

    private int[] listItemRect;//当前item框的屏幕位置
    private int[] listItemSize;//当前item的大小

    private Handler handler = new Handler();

    private boolean showFullAnimation = true;

    public ListVideoUtil(Context context) {
        gsyVideoPlayer = new StandardGSYVideoPlayer(context);
        this.context = context;
        int smallVideoWidth = CommonUtil.getScreenWidth(context) / 2 - CommonUtil.dip2px(context, 20);
        int smallVideoHeight = smallVideoWidth * 3 / 4;
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
        gsyVideoPlayer.release();

        gsyVideoPlayer.setUp(url, true, "");

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
        CommonUtil.hideSupportActionBar(context, hideActionBar, hideStatusBar);
        isFull = true;
        ViewGroup viewGroup = (ViewGroup) gsyVideoPlayer.getParent();
        listParams = gsyVideoPlayer.getLayoutParams();
        if (viewGroup != null) {
            listParent = viewGroup;
            viewGroup.removeView(gsyVideoPlayer);
        }
        gsyVideoPlayer.setIfCurrentIsFullscreen(true);
        gsyVideoPlayer.getFullscreenButton().setImageResource(R.drawable.video_shrink);
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
        if (showFullAnimation && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
        CommonUtil.showSupportActionBar(context, hideActionBar, hideStatusBar);
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
                gsyVideoPlayer.getFullscreenButton().setImageResource(R.drawable.video_enlarge);
                gsyVideoPlayer.getBackButton().setVisibility(View.GONE);
            }
        }, delay);
    }


    /**
     * 动画回到正常效果
     */
    private void resolveMaterialToNormal(final GSYVideoPlayer gsyVideoPlayer) {
        if (showFullAnimation && fullViewContainer instanceof FrameLayout && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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

    public boolean isAutoRotation() {
        return autoRotation;
    }

    /**
     * 是否自动旋转
     *
     * @param autoRotation 是否要支持重力旋转
     */
    public void setAutoRotation(boolean autoRotation) {
        this.autoRotation = autoRotation;
    }

    public boolean isFullLandFrist() {
        return fullLandFrist;
    }

    /**
     * 是否全屏就马上横屏
     *
     * @param fullLandFrist 如果是，那么全屏的时候就会切换到横屏
     */
    public void setFullLandFrist(boolean fullLandFrist) {
        this.fullLandFrist = fullLandFrist;
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


    public boolean isShowFullAnimation() {
        return showFullAnimation;
    }

    /**
     * 全屏动画
     *
     * @param showFullAnimation 是否使用全屏动画效果
     */
    public void setShowFullAnimation(boolean showFullAnimation) {
        this.showFullAnimation = showFullAnimation;
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
}
