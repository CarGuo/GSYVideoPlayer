package com.shuyu.gsyvideoplayer.builder;

import android.graphics.drawable.Drawable;
import android.view.View;

import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.listener.StandardVideoAllCallBack;
import com.shuyu.gsyvideoplayer.listener.VideoAllCallBack;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.io.File;
import java.util.Map;

/**
 * Created by guoshuyu on 2017/7/3.
 * <p>
 * 配置工具类吧。
 * <p>
 * 不是一个正常的Builder,这只是集合了所有设置配置而已.
 * 每个配置其实可以在对应的video接口中找到单独设置
 * 这只是方便使用
 */

public class GSYVideoBuilder {

    /******************************* GSYBaseVideoPlayer *****************************************/

    protected boolean mHideKey = true;//是否隐藏虚拟按键

    protected boolean mShowFullAnimation = true;//是否使用全屏动画效果

    protected boolean mNeedShowWifiTip = true; //是否需要显示流量提示

    protected boolean mRotateViewAuto = true; //是否自动旋转

    protected boolean mLockLand = false;//当前全屏是否锁定全屏

    protected boolean mLooping = false;//循环

    protected boolean mIsTouchWiget = true; //是否支持非全屏滑动触摸有效

    protected boolean mIsTouchWigetFull = true; //是否支持全屏滑动触摸有效

    protected boolean mShowPauseCover = true;//是否显示暂停图片

    protected boolean mRotateWithSystem = true; //旋转使能后是否跟随系统设置

    protected float mSeekRatio = 1; //触摸滑动进度的比例系数

    protected float mSpeed = 1;//播放速度

    protected int mShrinkImageRes = -1; //退出全屏显示的案件图片

    protected int mEnlargeImageRes = -1; //全屏显示的案件图片

    protected VideoAllCallBack mVideoAllCallBack;


    /**
     * 全屏动画
     *
     * @param showFullAnimation 是否使用全屏动画效果
     */
    public GSYVideoBuilder setShowFullAnimation(boolean showFullAnimation) {
        this.mShowFullAnimation = showFullAnimation;
        return this;
    }

    /**
     * 设置循环
     */
    public GSYVideoBuilder setLooping(boolean looping) {
        this.mLooping = looping;
        return this;
    }


    /**
     * 设置播放过程中的回调
     *
     * @param mVideoAllCallBack
     */
    public GSYVideoBuilder setVideoAllCallBack(VideoAllCallBack mVideoAllCallBack) {
        this.mVideoAllCallBack = mVideoAllCallBack;
        return this;
    }

    /**
     * 是否开启自动旋转
     */
    public GSYVideoBuilder setRotateViewAuto(boolean rotateViewAuto) {
        this.mRotateViewAuto = rotateViewAuto;
        return this;
    }

    /**
     * 一全屏就锁屏横屏，默认false竖屏，可配合setRotateViewAuto使用
     */
    public GSYVideoBuilder setLockLand(boolean lockLand) {
        this.mLockLand = lockLand;
        return this;
    }

    /**
     * 播放速度
     */
    public GSYVideoBuilder setSpeed(float speed) {
        this.mSpeed = speed;
        return this;
    }

    /**
     * 全屏隐藏虚拟按键，默认打开
     */
    public GSYVideoBuilder setHideKey(boolean hideKey) {
        this.mHideKey = hideKey;
        return this;
    }

    /**
     * 是否可以滑动界面改变进度，声音等
     * 默认true
     */
    public GSYVideoBuilder setIsTouchWiget(boolean isTouchWiget) {
        this.mIsTouchWiget = isTouchWiget;
        return this;
    }

    /**
     * 是否可以全屏滑动界面改变进度，声音等
     * 默认 true
     */
    public GSYVideoBuilder setIsTouchWigetFull(boolean isTouchWigetFull) {
        this.mIsTouchWigetFull = isTouchWigetFull;
        return this;
    }


    /**
     * 是否需要显示流量提示,默认true
     */
    public GSYVideoBuilder setNeedShowWifiTip(boolean needShowWifiTip) {
        this.mNeedShowWifiTip = needShowWifiTip;
        return this;
    }

    /**
     * 设置右下角 显示切换到全屏 的按键资源
     * 必须在setUp之前设置
     * 不设置使用默认
     */
    public GSYVideoBuilder setEnlargeImageRes(int mEnlargeImageRes) {
        this.mEnlargeImageRes = mEnlargeImageRes;
        return this;
    }

    /**
     * 设置右下角 显示退出全屏 的按键资源
     * 必须在setUp之前设置
     * 不设置使用默认
     */
    public GSYVideoBuilder setShrinkImageRes(int mShrinkImageRes) {
        this.mShrinkImageRes = mShrinkImageRes;
        return this;
    }


    /**
     * 是否需要加载显示暂停的cover图片
     * 打开状态下，暂停退到后台，再回到前台不会显示黑屏，但可以对某些机型有概率出现OOM
     * 关闭情况下，暂停退到后台，再回到前台显示黑屏
     *
     * @param showPauseCover 默认true
     */
    public GSYVideoBuilder setShowPauseCover(boolean showPauseCover) {
        this.mShowPauseCover = showPauseCover;
        return this;
    }

    /**
     * 调整触摸滑动快进的比例
     *
     * @param seekRatio 滑动快进的比例，默认1。数值越大，滑动的产生的seek越小
     */
    public GSYVideoBuilder setSeekRatio(float seekRatio) {
        if (seekRatio < 0) {
            return this;
        }
        this.mSeekRatio = seekRatio;
        return this;
    }

    /**
     * 是否更新系统旋转，false的话，系统禁止旋转也会跟着旋转
     *
     * @param rotateWithSystem 默认true
     */
    public GSYVideoBuilder setRotateWithSystem(boolean rotateWithSystem) {
        this.mRotateWithSystem = rotateWithSystem;
        return this;
    }


    /******************************* GSYVideoPlayer *****************************************/


    protected String mPlayTag = ""; //播放的tag，防止错误，因为普通的url也可能重复

    protected String mUrl;

    protected String mVideoTitle = null;

    protected int mPlayPosition = -22; //播放的tag，防止错误，因为普通的url也可能重复

    protected long mSeekOnStart = -1; //从哪个开始播放

    protected boolean mCacheWithPlay;

    protected File mCachePath;

    protected Map<String, String> mMapHeadData;

    /**
     * 播放tag防止错误，因为普通的url也可能重复
     *
     * @param playTag 保证不重复就好
     */
    public GSYVideoBuilder setPlayTag(String playTag) {
        this.mPlayTag = playTag;
        return this;
    }


    /**
     * 设置播放位置防止错位
     */
    public GSYVideoBuilder setPlayPosition(int playPosition) {
        this.mPlayPosition = playPosition;
        return this;
    }

    /**
     * 从哪里开始播放
     * 目前有时候前几秒有跳动问题，毫秒
     * 需要在startPlayLogic之前，即播放开始之前
     */
    public GSYVideoBuilder setSeekOnStart(long seekOnStart) {
        this.mSeekOnStart = seekOnStart;
        return this;
    }

    /**
     * 播放url
     *
     * @param url
     */
    public GSYVideoBuilder setUrl(String url) {
        this.mUrl = url;
        return this;
    }

    /**
     * 视频title
     *
     * @param videoTitle
     */
    public GSYVideoBuilder setVideoTitle(String videoTitle) {
        this.mVideoTitle = videoTitle;
        return this;
    }

    /**
     * 是否边缓存，m3u8等无效
     *
     * @param cacheWithPlay
     */
    public GSYVideoBuilder setCacheWithPlay(boolean cacheWithPlay) {
        this.mCacheWithPlay = cacheWithPlay;
        return this;
    }

    /**
     * 自定指定缓存路径，推荐不设置，使用默认路径
     *
     * @param cachePath
     */
    public GSYVideoBuilder setCachePath(File cachePath) {
        this.mCachePath = cachePath;
        return this;
    }

    /**
     * 设置请求的头信息
     *
     * @param mapHeadData
     */
    public GSYVideoBuilder setMapHeadData(Map<String, String> mapHeadData) {
        this.mMapHeadData = mapHeadData;
        return this;
    }

    /******************************* StandardGSYVideoPlayer *****************************************/


    protected View mThumbImageView; //封面

    protected StandardVideoAllCallBack mStandardVideoAllCallBack;//标准播放器的回调

    protected LockClickListener mLockClickListener;//点击锁屏的回调

    protected Drawable mBottomProgressDrawable;

    protected Drawable mBottomShowProgressDrawable;

    protected Drawable mBottomShowProgressThumbDrawable;

    protected Drawable mVolumeProgressDrawable;

    protected Drawable mDialogProgressBarDrawable;

    protected int mDialogProgressHighLightColor = -11;

    protected int mDialogProgressNormalColor = -11;

    protected int mDismissControlTime = 2500;

    protected boolean mNeedLockFull;//是否需要锁定屏幕

    protected boolean mThumbPlay;

    public GSYVideoBuilder setStandardVideoAllCallBack(StandardVideoAllCallBack standardVideoAllCallBack) {
        this.mStandardVideoAllCallBack = standardVideoAllCallBack;
        return this;
    }

    /***
     * 设置封面
     */
    public GSYVideoBuilder setThumbImageView(View view) {
        mThumbImageView = view;
        return this;
    }


    /**
     * 底部进度条-弹出的
     */
    public GSYVideoBuilder setBottomShowProgressBarDrawable(Drawable drawable, Drawable thumb) {
        mBottomShowProgressDrawable = drawable;
        mBottomShowProgressThumbDrawable = thumb;
        return this;
    }

    /**
     * 底部进度条-非弹出
     */
    public GSYVideoBuilder setBottomProgressBarDrawable(Drawable drawable) {
        mBottomProgressDrawable = drawable;
        return this;
    }

    /**
     * 声音进度条
     */
    public GSYVideoBuilder setDialogVolumeProgressBar(Drawable drawable) {
        mVolumeProgressDrawable = drawable;
        return this;
    }


    /**
     * 中间进度条
     */
    public GSYVideoBuilder setDialogProgressBar(Drawable drawable) {
        mDialogProgressBarDrawable = drawable;
        return this;
    }

    /**
     * 中间进度条字体颜色
     */
    public GSYVideoBuilder setDialogProgressColor(int highLightColor, int normalColor) {
        mDialogProgressHighLightColor = highLightColor;
        mDialogProgressNormalColor = normalColor;
        return this;
    }

    /**
     * 是否点击封面可以播放
     */
    public GSYVideoBuilder setThumbPlay(boolean thumbPlay) {
        this.mThumbPlay = thumbPlay;
        return this;
    }

    /**
     * 是否需要全屏锁定屏幕功能
     * 如果单独使用请设置setIfCurrentIsFullscreen为true
     */
    public GSYVideoBuilder setNeedLockFull(boolean needLoadFull) {
        this.mNeedLockFull = needLoadFull;
        return this;
    }

    /**
     * 锁屏点击
     */
    public GSYVideoBuilder setLockClickListener(LockClickListener lockClickListener) {
        this.mLockClickListener = lockClickListener;
        return this;
    }

    /**
     * 设置触摸显示控制ui的消失时间
     *
     * @param dismissControlTime 毫秒，默认2500
     */
    public GSYVideoBuilder setDismissControlTime(int dismissControlTime) {
        this.mDismissControlTime = dismissControlTime;
        return this;
    }

    public void build(StandardGSYVideoPlayer gsyVideoPlayer) {
        if (mStandardVideoAllCallBack != null) {
            gsyVideoPlayer.setStandardVideoAllCallBack(mStandardVideoAllCallBack);
        }

        if (mThumbImageView != null) {
            gsyVideoPlayer.setThumbImageView(mThumbImageView);
        }

        if (mBottomShowProgressDrawable != null && mBottomShowProgressThumbDrawable != null) {
            gsyVideoPlayer.setBottomShowProgressBarDrawable(mBottomShowProgressDrawable, mBottomShowProgressThumbDrawable);
        }
        if (mBottomProgressDrawable != null) {
            gsyVideoPlayer.setBottomProgressBarDrawable(mBottomProgressDrawable);
        }
        if (mVolumeProgressDrawable != null) {
            gsyVideoPlayer.setDialogVolumeProgressBar(mVolumeProgressDrawable);
        }

        if (mDialogProgressBarDrawable != null) {
            gsyVideoPlayer.setDialogProgressBar(mDialogProgressBarDrawable);
        }

        if (mDialogProgressHighLightColor > 0 && mDialogProgressNormalColor > 0) {
            gsyVideoPlayer.setDialogProgressColor(mDialogProgressHighLightColor, mDialogProgressNormalColor);
        }

        gsyVideoPlayer.setThumbPlay(mThumbPlay);

        gsyVideoPlayer.setNeedLockFull(mNeedLockFull);

        if (mLockClickListener != null) {
            gsyVideoPlayer.setLockClickListener(mLockClickListener);
        }

        gsyVideoPlayer.setDismissControlTime(mDismissControlTime);

        build((GSYVideoPlayer) gsyVideoPlayer);
    }

    public void build(GSYVideoPlayer gsyVideoPlayer) {
        build((GSYBaseVideoPlayer) gsyVideoPlayer);
        gsyVideoPlayer.setPlayTag(mPlayTag);
        gsyVideoPlayer.setPlayPosition(mPlayPosition);

        if (mSeekOnStart > 0) {
            gsyVideoPlayer.setSeekOnStart(mSeekOnStart);
        }

        gsyVideoPlayer.setUp(mUrl, mCacheWithPlay, mCachePath, mMapHeadData, mVideoTitle);
    }


    public void build(GSYBaseVideoPlayer gsyVideoPlayer) {
        gsyVideoPlayer.setShowFullAnimation(mShowFullAnimation);
        gsyVideoPlayer.setLooping(mLooping);
        if (mStandardVideoAllCallBack == null) {
            gsyVideoPlayer.setVideoAllCallBack(mVideoAllCallBack);
        }
        gsyVideoPlayer.setRotateViewAuto(mRotateViewAuto);
        gsyVideoPlayer.setLockLand(mLockLand);
        gsyVideoPlayer.setSpeed(mSpeed);
        gsyVideoPlayer.setHideKey(mHideKey);
        gsyVideoPlayer.setIsTouchWiget(mIsTouchWiget);
        gsyVideoPlayer.setIsTouchWigetFull(mIsTouchWigetFull);
        gsyVideoPlayer.setNeedShowWifiTip(mNeedShowWifiTip);
        if (mEnlargeImageRes > 0) {
            gsyVideoPlayer.setEnlargeImageRes(mEnlargeImageRes);
        }
        if (mShrinkImageRes > 0) {
            gsyVideoPlayer.setShrinkImageRes(mShrinkImageRes);
        }
        gsyVideoPlayer.setShowPauseCover(mShowPauseCover);
        gsyVideoPlayer.setSeekRatio(mSeekRatio);
        gsyVideoPlayer.setRotateWithSystem(mRotateWithSystem);
    }

}
