package com.shuyu.gsyvideoplayer.builder;

import android.graphics.drawable.Drawable;
import android.view.View;

import com.shuyu.gsyvideoplayer.listener.GSYVideoProgressListener;
import com.shuyu.gsyvideoplayer.render.view.GSYVideoGLView;
import com.shuyu.gsyvideoplayer.render.effect.NoEffect;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
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

public class GSYVideoOptionBuilder {

    //退出全屏显示的案件图片
    protected int mShrinkImageRes = -1;

    //全屏显示的案件图片
    protected int mEnlargeImageRes = -1;

    //播放的tag，防止错误，因为普通的url也可能重复
    protected int mPlayPosition = -22;

    //触摸快进dialog的进度高量颜色
    protected int mDialogProgressHighLightColor = -11;

    //触摸快进dialog的进度普通颜色
    protected int mDialogProgressNormalColor = -11;

    //触摸隐藏等待时间
    protected int mDismissControlTime = 2500;

    //从哪个开始播放
    protected long mSeekOnStart = -1;

    //触摸滑动进度的比例系数
    protected float mSeekRatio = 1;

    //播放速度
    protected float mSpeed = 1;

    //是否隐藏虚拟按键
    protected boolean mHideKey = true;

    //是否使用全屏动画效果
    protected boolean mShowFullAnimation = true;

    //是否根据视频尺寸，自动选择竖屏全屏或者横屏全屏，注意，这时候默认旋转无效
    protected boolean mAutoFullWithSize = false;

    //是否需要显示流量提示
    protected boolean mNeedShowWifiTip = true;

    //是否自动旋转
    protected boolean mRotateViewAuto = true;

    //当前全屏是否锁定全屏
    protected boolean mLockLand = false;

    //循环
    protected boolean mLooping = false;

    //是否支持非全屏滑动触摸有效
    protected boolean mIsTouchWiget = true;

    //是否支持全屏滑动触摸有效
    protected boolean mIsTouchWigetFull = true;

    //是否显示暂停图片
    protected boolean mShowPauseCover = true;

    //旋转使能后是否跟随系统设置
    protected boolean mRotateWithSystem = true;

    //边播放边缓存
    protected boolean mCacheWithPlay;

    //是否需要锁定屏幕
    protected boolean mNeedLockFull;

    //点击封面播放
    protected boolean mThumbPlay;

    //是否需要变速不变调
    protected boolean mSounchTouch;

    //是否需要lazy的setup
    protected boolean mSetUpLazy = false;

    //Prepared之后是否自动开始播放
    protected boolean mStartAfterPrepared = true;

    //是否播放器当失去音频焦点
    protected boolean mReleaseWhenLossAudio = true;

    //是否需要在利用window实现全屏幕的时候隐藏actionbar
    protected boolean mActionBar = false;

    //是否需要在利用window实现全屏幕的时候隐藏statusbar
    protected boolean mStatusBar = false;

    //拖动进度条时，是否在 seekbar 开始部位显示拖动进度
    protected boolean isShowDragProgressTextOnSeekBar = false;

    //播放的tag，防止错误，因为普通的url也可能重复
    protected String mPlayTag = "";

    //播放url
    protected String mUrl;

    //视频title
    protected String mVideoTitle = null;

    // 是否需要覆盖拓展类型
    protected String mOverrideExtension;

    private boolean mIsOnlyRotateLand = false;

    //是否自定义的缓冲文件路径
    protected File mCachePath;

    //http请求头
    protected Map<String, String> mMapHeadData;

    //视频状体回调
    protected VideoAllCallBack mVideoAllCallBack;

    //点击锁屏的回调
    protected LockClickListener mLockClickListener;

    //封面
    protected View mThumbImageView;

    //底部进度条样式
    protected Drawable mBottomProgressDrawable;

    //底部进度条样式
    protected Drawable mBottomShowProgressDrawable;

    //底部进度条小圆点样式
    protected Drawable mBottomShowProgressThumbDrawable;

    //音量进度条样式
    protected Drawable mVolumeProgressDrawable;

    //滑动dialog进度条样式
    protected Drawable mDialogProgressBarDrawable;

    //滤镜
    protected GSYVideoGLView.ShaderInterface mEffectFilter = new NoEffect();

    //进度回调
    protected GSYVideoProgressListener mGSYVideoProgressListener;


    /**
     * 是否根据视频尺寸，自动选择竖屏全屏或者横屏全屏，注意，这时候默认旋转无效
     *
     * @param autoFullWithSize 默认false
     */
    public GSYVideoOptionBuilder setAutoFullWithSize(boolean autoFullWithSize) {
        this.mAutoFullWithSize = autoFullWithSize;
        return this;
    }

    /**
     * 全屏动画
     *
     * @param showFullAnimation 是否使用全屏动画效果
     */
    public GSYVideoOptionBuilder setShowFullAnimation(boolean showFullAnimation) {
        this.mShowFullAnimation = showFullAnimation;
        return this;
    }

    /**
     * 设置循环
     */
    public GSYVideoOptionBuilder setLooping(boolean looping) {
        this.mLooping = looping;
        return this;
    }


    /**
     * 设置播放过程中的回调
     *
     * @param mVideoAllCallBack
     */
    public GSYVideoOptionBuilder setVideoAllCallBack(VideoAllCallBack mVideoAllCallBack) {
        this.mVideoAllCallBack = mVideoAllCallBack;
        return this;
    }

    /**
     * 是否开启自动旋转
     */
    public GSYVideoOptionBuilder setRotateViewAuto(boolean rotateViewAuto) {
        this.mRotateViewAuto = rotateViewAuto;
        return this;
    }

    /**
     * 一全屏就锁屏横屏，默认false竖屏，可配合setRotateViewAuto使用
     */
    public GSYVideoOptionBuilder setLockLand(boolean lockLand) {
        this.mLockLand = lockLand;
        return this;
    }

    /**
     * 播放速度
     */
    public GSYVideoOptionBuilder setSpeed(float speed) {
        this.mSpeed = speed;
        return this;
    }


    /**
     * 变声不变调
     */
    public GSYVideoOptionBuilder setSoundTouch(boolean soundTouch) {
        this.mSounchTouch = soundTouch;
        return this;
    }

    /**
     * 全屏隐藏虚拟按键，默认打开
     */
    public GSYVideoOptionBuilder setHideKey(boolean hideKey) {
        this.mHideKey = hideKey;
        return this;
    }

    /**
     * 是否可以滑动界面改变进度，声音等
     * 默认true
     */
    public GSYVideoOptionBuilder setIsTouchWiget(boolean isTouchWiget) {
        this.mIsTouchWiget = isTouchWiget;
        return this;
    }

    /**
     * 是否可以全屏滑动界面改变进度，声音等
     * 默认 true
     */
    public GSYVideoOptionBuilder setIsTouchWigetFull(boolean isTouchWigetFull) {
        this.mIsTouchWigetFull = isTouchWigetFull;
        return this;
    }


    /**
     * 是否需要显示流量提示,默认true
     */
    public GSYVideoOptionBuilder setNeedShowWifiTip(boolean needShowWifiTip) {
        this.mNeedShowWifiTip = needShowWifiTip;
        return this;
    }

    /**
     * 设置右下角 显示切换到全屏 的按键资源
     * 必须在setUp之前设置
     * 不设置使用默认
     */
    public GSYVideoOptionBuilder setEnlargeImageRes(int mEnlargeImageRes) {
        this.mEnlargeImageRes = mEnlargeImageRes;
        return this;
    }

    /**
     * 设置右下角 显示退出全屏 的按键资源
     * 必须在setUp之前设置
     * 不设置使用默认
     */
    public GSYVideoOptionBuilder setShrinkImageRes(int mShrinkImageRes) {
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
    public GSYVideoOptionBuilder setShowPauseCover(boolean showPauseCover) {
        this.mShowPauseCover = showPauseCover;
        return this;
    }

    /**
     * 调整触摸滑动快进的比例
     *
     * @param seekRatio 滑动快进的比例，默认1。数值越大，滑动的产生的seek越小
     */
    public GSYVideoOptionBuilder setSeekRatio(float seekRatio) {
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
    public GSYVideoOptionBuilder setRotateWithSystem(boolean rotateWithSystem) {
        this.mRotateWithSystem = rotateWithSystem;
        return this;
    }

    /**
     * 播放tag防止错误，因为普通的url也可能重复
     *
     * @param playTag 保证不重复就好
     */
    public GSYVideoOptionBuilder setPlayTag(String playTag) {
        this.mPlayTag = playTag;
        return this;
    }


    /**
     * 设置播放位置防止错位
     */
    public GSYVideoOptionBuilder setPlayPosition(int playPosition) {
        this.mPlayPosition = playPosition;
        return this;
    }

    /**
     * 从哪里开始播放
     * 目前有时候前几秒有跳动问题，毫秒
     * 需要在startPlayLogic之前，即播放开始之前
     */
    public GSYVideoOptionBuilder setSeekOnStart(long seekOnStart) {
        this.mSeekOnStart = seekOnStart;
        return this;
    }

    /**
     * 播放url
     *
     * @param url
     */
    public GSYVideoOptionBuilder setUrl(String url) {
        this.mUrl = url;
        return this;
    }

    /**
     * 视频title
     *
     * @param videoTitle
     */
    public GSYVideoOptionBuilder setVideoTitle(String videoTitle) {
        this.mVideoTitle = videoTitle;
        return this;
    }

    /**
     * 是否边缓存，m3u8等无效
     *
     * @param cacheWithPlay
     */
    public GSYVideoOptionBuilder setCacheWithPlay(boolean cacheWithPlay) {
        this.mCacheWithPlay = cacheWithPlay;
        return this;
    }

    /**
     * 准备成功之后立即播放
     *
     * @param startAfterPrepared 默认true，false的时候需要在prepared后调用startAfterPrepared()
     */
    public GSYVideoOptionBuilder setStartAfterPrepared(boolean startAfterPrepared) {
        this.mStartAfterPrepared = startAfterPrepared;
        return this;
    }


    /**
     * 长时间失去音频焦点，暂停播放器
     *
     * @param releaseWhenLossAudio 默认true，false的时候只会暂停
     */
    public GSYVideoOptionBuilder setReleaseWhenLossAudio(boolean releaseWhenLossAudio) {
        this.mReleaseWhenLossAudio = releaseWhenLossAudio;
        return this;
    }

    /**
     * 自定指定缓存路径，推荐不设置，使用默认路径
     *
     * @param cachePath
     */
    public GSYVideoOptionBuilder setCachePath(File cachePath) {
        this.mCachePath = cachePath;
        return this;
    }

    /**
     * 设置请求的头信息
     *
     * @param mapHeadData
     */
    public GSYVideoOptionBuilder setMapHeadData(Map<String, String> mapHeadData) {
        this.mMapHeadData = mapHeadData;
        return this;
    }


    /**
     * 进度回调
     */
    public GSYVideoOptionBuilder setGSYVideoProgressListener(GSYVideoProgressListener videoProgressListener) {
        this.mGSYVideoProgressListener = videoProgressListener;
        return this;
    }


    /***
     * 设置封面
     */
    public GSYVideoOptionBuilder setThumbImageView(View view) {
        mThumbImageView = view;
        return this;
    }


    /**
     * 底部进度条-弹出的
     */
    public GSYVideoOptionBuilder setBottomShowProgressBarDrawable(Drawable drawable, Drawable thumb) {
        mBottomShowProgressDrawable = drawable;
        mBottomShowProgressThumbDrawable = thumb;
        return this;
    }

    /**
     * 底部进度条-非弹出
     */
    public GSYVideoOptionBuilder setBottomProgressBarDrawable(Drawable drawable) {
        mBottomProgressDrawable = drawable;
        return this;
    }

    /**
     * 声音进度条
     */
    public GSYVideoOptionBuilder setDialogVolumeProgressBar(Drawable drawable) {
        mVolumeProgressDrawable = drawable;
        return this;
    }


    /**
     * 中间进度条
     */
    public GSYVideoOptionBuilder setDialogProgressBar(Drawable drawable) {
        mDialogProgressBarDrawable = drawable;
        return this;
    }

    /**
     * 中间进度条字体颜色
     */
    public GSYVideoOptionBuilder setDialogProgressColor(int highLightColor, int normalColor) {
        mDialogProgressHighLightColor = highLightColor;
        mDialogProgressNormalColor = normalColor;
        return this;
    }

    /**
     * 是否点击封面可以播放
     */
    public GSYVideoOptionBuilder setThumbPlay(boolean thumbPlay) {
        this.mThumbPlay = thumbPlay;
        return this;
    }

    /**
     * 是否需要全屏锁定屏幕功能
     * 如果单独使用请设置setIfCurrentIsFullscreen为true
     */
    public GSYVideoOptionBuilder setNeedLockFull(boolean needLoadFull) {
        this.mNeedLockFull = needLoadFull;
        return this;
    }

    /**
     * 锁屏点击
     */
    public GSYVideoOptionBuilder setLockClickListener(LockClickListener lockClickListener) {
        this.mLockClickListener = lockClickListener;
        return this;
    }

    /**
     * 设置触摸显示控制ui的消失时间
     *
     * @param dismissControlTime 毫秒，默认2500
     */
    public GSYVideoOptionBuilder setDismissControlTime(int dismissControlTime) {
        this.mDismissControlTime = dismissControlTime;
        return this;
    }

    /**
     * 设置滤镜效果
     */
    public GSYVideoOptionBuilder setEffectFilter(GSYVideoGLView.ShaderInterface effectFilter) {
        this.mEffectFilter = effectFilter;
        return this;
    }

    /**
     * 是否需要覆盖拓展类型，目前只针对exoPlayer内核模式有效
     * @param overrideExtension 比如传入 m3u8,mp4,avi 等类型
     */
    public GSYVideoOptionBuilder setOverrideExtension(String overrideExtension) {
        this.mOverrideExtension = overrideExtension;
        return this;
    }


    public GSYVideoOptionBuilder setOnlyRotateLand(boolean onlyRotateLand) {
        this.mIsOnlyRotateLand = onlyRotateLand;
        return this;
    }

    public GSYVideoOptionBuilder setShowDragProgressTextOnSeekBar(boolean isShowDragProgressTextOnSeekBar) {
        this.isShowDragProgressTextOnSeekBar = isShowDragProgressTextOnSeekBar;
        return this;
    }

    /**
     * 在播放前才真正执行setup
     * 目前弃用，请使用正常setup
     */
    @Deprecated
    public GSYVideoOptionBuilder setSetUpLazy(boolean setUpLazy) {
        this.mSetUpLazy = setUpLazy;
        return this;
    }

    public GSYVideoOptionBuilder setFullHideActionBar(boolean actionBar) {
        this.mActionBar = actionBar;
        return this;
    }

    public GSYVideoOptionBuilder setFullHideStatusBar(boolean statusBar) {
        this.mStatusBar = statusBar;
        return this;
    }

    public void build(StandardGSYVideoPlayer gsyVideoPlayer) {
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

        build((GSYBaseVideoPlayer) gsyVideoPlayer);
    }

    public void build(GSYBaseVideoPlayer gsyVideoPlayer) {
        gsyVideoPlayer.setPlayTag(mPlayTag);
        gsyVideoPlayer.setPlayPosition(mPlayPosition);

        gsyVideoPlayer.setThumbPlay(mThumbPlay);

        if (mThumbImageView != null) {
            gsyVideoPlayer.setThumbImageView(mThumbImageView);
        }

        gsyVideoPlayer.setNeedLockFull(mNeedLockFull);

        if (mLockClickListener != null) {
            gsyVideoPlayer.setLockClickListener(mLockClickListener);
        }

        gsyVideoPlayer.setDismissControlTime(mDismissControlTime);


        if (mSeekOnStart > 0) {
            gsyVideoPlayer.setSeekOnStart(mSeekOnStart);
        }

        gsyVideoPlayer.setShowFullAnimation(mShowFullAnimation);
        gsyVideoPlayer.setLooping(mLooping);
        if (mVideoAllCallBack != null) {
            gsyVideoPlayer.setVideoAllCallBack(mVideoAllCallBack);
        }
        if (mGSYVideoProgressListener != null) {
            gsyVideoPlayer.setGSYVideoProgressListener(mGSYVideoProgressListener);
        }
        gsyVideoPlayer.setOverrideExtension(mOverrideExtension);
        gsyVideoPlayer.setAutoFullWithSize(mAutoFullWithSize);
        gsyVideoPlayer.setRotateViewAuto(mRotateViewAuto);
        gsyVideoPlayer.setOnlyRotateLand(mIsOnlyRotateLand);
        gsyVideoPlayer.setLockLand(mLockLand);
        gsyVideoPlayer.setSpeed(mSpeed, mSounchTouch);
        gsyVideoPlayer.setHideKey(mHideKey);
        gsyVideoPlayer.setIsTouchWiget(mIsTouchWiget);
        gsyVideoPlayer.setIsTouchWigetFull(mIsTouchWigetFull);
        gsyVideoPlayer.setNeedShowWifiTip(mNeedShowWifiTip);
        gsyVideoPlayer.setEffectFilter(mEffectFilter);
        gsyVideoPlayer.setStartAfterPrepared(mStartAfterPrepared);
        gsyVideoPlayer.setReleaseWhenLossAudio(mReleaseWhenLossAudio);
        gsyVideoPlayer.setFullHideActionBar(mActionBar);
        gsyVideoPlayer.setShowDragProgressTextOnSeekBar(isShowDragProgressTextOnSeekBar);
        gsyVideoPlayer.setFullHideStatusBar(mStatusBar);
        if (mEnlargeImageRes > 0) {
            gsyVideoPlayer.setEnlargeImageRes(mEnlargeImageRes);
        }
        if (mShrinkImageRes > 0) {
            gsyVideoPlayer.setShrinkImageRes(mShrinkImageRes);
        }
        gsyVideoPlayer.setShowPauseCover(mShowPauseCover);
        gsyVideoPlayer.setSeekRatio(mSeekRatio);
        gsyVideoPlayer.setRotateWithSystem(mRotateWithSystem);
        if (mSetUpLazy) {
            gsyVideoPlayer.setUpLazy(mUrl, mCacheWithPlay, mCachePath, mMapHeadData, mVideoTitle);
        } else {
            gsyVideoPlayer.setUp(mUrl, mCacheWithPlay, mCachePath, mMapHeadData, mVideoTitle);
        }
    }

}
