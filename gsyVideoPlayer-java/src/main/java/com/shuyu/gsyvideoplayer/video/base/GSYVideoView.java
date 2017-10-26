package com.shuyu.gsyvideoplayer.video.base;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.danikula.videocache.HttpProxyCacheServer;
import com.danikula.videocache.file.Md5FileNameGenerator;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.R;
import com.shuyu.gsyvideoplayer.listener.GSYMediaPlayerListener;
import com.shuyu.gsyvideoplayer.listener.VideoAllCallBack;
import com.shuyu.gsyvideoplayer.model.VideoOptionModel;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.utils.NetInfoModule;
import com.shuyu.gsyvideoplayer.utils.StorageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkLibLoader;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static com.shuyu.gsyvideoplayer.utils.CommonUtil.getTextSpeed;

/**
 * 视频回调与状态处理等相关层
 * Created by guoshuyu on 2017/8/2.
 */

public abstract class GSYVideoView extends GSYTextureRenderView implements GSYMediaPlayerListener {

    //正常
    public static final int CURRENT_STATE_NORMAL = 0;
    //准备中
    public static final int CURRENT_STATE_PREPAREING = 1;
    //播放中
    public static final int CURRENT_STATE_PLAYING = 2;
    //开始缓冲
    public static final int CURRENT_STATE_PLAYING_BUFFERING_START = 3;
    //暂停
    public static final int CURRENT_STATE_PAUSE = 5;
    //自动播放结束
    public static final int CURRENT_STATE_AUTO_COMPLETE = 6;
    //错误状态
    public static final int CURRENT_STATE_ERROR = 7;

    //避免切换时频繁setup
    public static final int CHANGE_DELAY_TIME = 2000;


    //当前的播放状态
    protected int mCurrentState = -1;

    //播放的tag，防止错误，因为普通的url也可能重复
    protected int mPlayPosition = -22;

    //屏幕宽度
    protected int mScreenWidth;

    //屏幕高度
    protected int mScreenHeight;

    //缓存进度
    protected int mBuffterPoint;

    //备份缓存前的播放状态
    protected int mBackUpPlayingBufferState = -1;

    //从哪个开始播放
    protected long mSeekOnStart = -1;

    //保存暂停时的时间
    protected long mPauseTime;

    //当前的播放位置
    protected long mCurrentPosition;

    //保存切换时的时间，避免频繁契合
    protected long mSaveChangeViewTIme = 0;

    //播放速度
    protected float mSpeed = 1;

    //是否播边边缓冲
    protected boolean mCache = false;

    //当前是否全屏
    protected boolean mIfCurrentIsFullscreen = false;

    //循环
    protected boolean mLooping = false;

    //是否播放过
    protected boolean mHadPlay = false;

    //是否是缓存的文件
    protected boolean mCacheFile = false;

    //是否发送了网络改变
    protected boolean mNetChanged = false;

    //是否不变调
    protected boolean mSoundTouch = false;

    //是否需要显示暂停锁定效果
    protected boolean mShowPauseCover = true;

    //音频焦点的监听
    protected AudioManager mAudioManager;

    //播放的tag，防止错误，因为普通的url也可能重复
    protected String mPlayTag = "";

    //上下文
    protected Context mContext;

    //原来的url
    protected String mOriginUrl;

    //转化后的URL
    protected String mUrl;

    //标题
    protected String mTitle;

    //网络状态
    protected String mNetSate = "NORMAL";

    //缓存路径，可不设置
    protected File mCachePath;

    //视频回调
    protected VideoAllCallBack mVideoAllCallBack;

    //http request header
    protected Map<String, String> mMapHeadData = new HashMap<>();

    //网络监听
    protected NetInfoModule mNetInfoModule;

    public GSYVideoView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public GSYVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GSYVideoView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public GSYVideoView(Context context, Boolean fullFlag) {
        super(context);
        mIfCurrentIsFullscreen = fullFlag;
        init(context);
    }

    @Override
    protected void showPauseCover() {
        if (mCurrentState == CURRENT_STATE_PAUSE && mFullPauseBitmap != null
                && !mFullPauseBitmap.isRecycled() && mShowPauseCover
                && mSurface != null && mSurface.isValid()) {
            RectF rectF = new RectF(0, 0, mTextureView.getWidth(), mTextureView.getHeight());
            Canvas canvas = mSurface.lockCanvas(new Rect(0, 0, mTextureView.getWidth(), mTextureView.getHeight()));
            if (canvas != null) {
                canvas.drawBitmap(mFullPauseBitmap, null, rectF, null);
                mSurface.unlockCanvasAndPost(canvas);
            }
        }

    }

    @Override
    protected void releasePauseCover() {
        try {
            if (mCurrentState != CURRENT_STATE_PAUSE && mFullPauseBitmap != null
                    && !mFullPauseBitmap.isRecycled() && mShowPauseCover) {
                mFullPauseBitmap.recycle();
                mFullPauseBitmap = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected void updatePauseCover() {
        if ((mFullPauseBitmap == null || mFullPauseBitmap.isRecycled()) && mShowPauseCover) {
            try {
                initCover();
            } catch (Exception e) {
                e.printStackTrace();
                mFullPauseBitmap = null;
            }
        }
    }

    protected Context getActivityContext() {
        return CommonUtil.getActivityContext(getContext());
    }

    protected void init(Context context) {

        if (getActivityContext() != null) {
            this.mContext = getActivityContext();
        } else {
            this.mContext = context;
        }

        initInflate(mContext);

        mTextureViewContainer = (ViewGroup) findViewById(R.id.surface_container);
        if (isInEditMode())
            return;
        mScreenWidth = getActivityContext().getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getActivityContext().getResources().getDisplayMetrics().heightPixels;
        mAudioManager = (AudioManager) getActivityContext().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

    }

    protected void initInflate(Context context) {
        try {
            View.inflate(context, getLayoutId(), this);
        } catch (InflateException e) {
            if (e.toString().contains("GSYImageCover")) {
                Debuger.printfError("********************\n" +
                        "*****   注意   *****" +
                        "********************\n" +
                        "*该版本需要清除布局文件中的GSYImageCover\n" +
                        "****  Attention  ***\n" +
                        "*Please remove GSYImageCover from Layout in this Version\n" +
                        "********************\n");
                e.printStackTrace();
                throw new InflateException("该版本需要清除布局文件中的GSYImageCover，please remove GSYImageCover from your layout");
            } else {
                e.printStackTrace();
            }
        }
    }

    /**
     * 开始播放逻辑
     */
    protected void startButtonLogic() {
        if (mVideoAllCallBack != null && mCurrentState == CURRENT_STATE_NORMAL) {
            Debuger.printfLog("onClickStartIcon");
            mVideoAllCallBack.onClickStartIcon(mOriginUrl, mTitle, this);
        } else if (mVideoAllCallBack != null) {
            Debuger.printfLog("onClickStartError");
            mVideoAllCallBack.onClickStartError(mOriginUrl, mTitle, this);
        }
        prepareVideo();
    }

    /**
     * 开始状态视频播放
     */
    protected void prepareVideo() {
        startPrepare();
        addTextureView();
    }

    protected void startPrepare() {
        if (GSYVideoManager.instance().listener() != null) {
            GSYVideoManager.instance().listener().onCompletion();
        }
        GSYVideoManager.instance().setListener(this);
        GSYVideoManager.instance().setPlayTag(mPlayTag);
        GSYVideoManager.instance().setPlayPosition(mPlayPosition);
        mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        ((Activity) getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mBackUpPlayingBufferState = -1;
        GSYVideoManager.instance().prepare(mUrl, mMapHeadData, mLooping, mSpeed);
        setStateAndUi(CURRENT_STATE_PREPAREING);
    }

    /**
     * 监听是否有外部其他多媒体开始播放
     */
    protected AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    post(new Runnable() {
                        @Override
                        public void run() {
                            releaseAllVideos();
                        }
                    });
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    try {
                        if (GSYVideoManager.instance().getMediaPlayer().isPlaying()) {
                            GSYVideoManager.instance().getMediaPlayer().pause();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    break;
            }
        }
    };


    /**
     * 设置播放URL
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param title         title
     * @return
     */
    public boolean setUp(String url, boolean cacheWithPlay, String title) {
        return setUp(url, cacheWithPlay, ((File) null), title);
    }


    /**
     * 设置播放URL
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param cachePath     缓存路径，如果是M3U8或者HLS，请设置为false
     * @param mapHeadData   头部信息
     * @param title         title
     * @return
     */
    public boolean setUp(String url, boolean cacheWithPlay, File cachePath, Map<String, String> mapHeadData, String title) {
        if (setUp(url, cacheWithPlay, cachePath, title)) {
            this.mMapHeadData.clear();
            if (mapHeadData != null)
                this.mMapHeadData.putAll(mapHeadData);
            return true;
        }
        return false;
    }

    /**
     * 设置播放URL
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param cachePath     缓存路径，如果是M3U8或者HLS，请设置为false
     * @param title         title
     * @return
     */
    public boolean setUp(String url, boolean cacheWithPlay, File cachePath, String title) {
        mCache = cacheWithPlay;
        mCachePath = cachePath;
        mOriginUrl = url;
        if (isCurrentMediaListener() &&
                (System.currentTimeMillis() - mSaveChangeViewTIme) < CHANGE_DELAY_TIME)
            return false;
        mCurrentState = CURRENT_STATE_NORMAL;
        if (cacheWithPlay && url.startsWith("http") && !url.contains("127.0.0.1") && !url.contains(".m3u8")) {
            HttpProxyCacheServer proxy = GSYVideoManager.getProxy(getActivityContext().getApplicationContext(), cachePath);
            //此处转换了url，然后再赋值给mUrl。
            url = proxy.getProxyUrl(url);
            mCacheFile = (!url.startsWith("http"));
            //注册上缓冲监听
            if (!mCacheFile && GSYVideoManager.instance() != null) {
                proxy.registerCacheListener(GSYVideoManager.instance(), mOriginUrl);
            }
        } else if (!cacheWithPlay && (!url.startsWith("http") && !url.startsWith("rtmp")
                && !url.startsWith("rtsp") && !url.contains(".m3u8"))) {
            mCacheFile = true;
        }
        this.mUrl = url;
        this.mTitle = title;
        setStateAndUi(CURRENT_STATE_NORMAL);
        return true;
    }


    /**
     * 重置
     */
    public void onVideoReset() {
        setStateAndUi(CURRENT_STATE_NORMAL);
    }

    /**
     * 暂停状态
     */
    @Override
    public void onVideoPause() {
        try {
            if (GSYVideoManager.instance().getMediaPlayer() != null &&
                    GSYVideoManager.instance().getMediaPlayer().isPlaying()) {
                setStateAndUi(CURRENT_STATE_PAUSE);
                mPauseTime = System.currentTimeMillis();
                mCurrentPosition = GSYVideoManager.instance().getMediaPlayer().getCurrentPosition();
                if (GSYVideoManager.instance().getMediaPlayer() != null)
                    GSYVideoManager.instance().getMediaPlayer().pause();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 恢复暂停状态
     */
    @Override
    public void onVideoResume() {
        mPauseTime = 0;
        if (mCurrentState == CURRENT_STATE_PAUSE) {
            try {
                if (mCurrentPosition > 0 && GSYVideoManager.instance().getMediaPlayer() != null) {
                    setStateAndUi(CURRENT_STATE_PLAYING);
                    GSYVideoManager.instance().getMediaPlayer().seekTo(mCurrentPosition);
                    GSYVideoManager.instance().getMediaPlayer().start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 处理因切换网络而导致的问题
     */
    protected void netWorkErrorLogic() {
        final long currentPosition = getCurrentPositionWhenPlaying();
        Debuger.printfError("******* Net State Changed. renew player to connect *******" + currentPosition);
        GSYVideoManager.instance().releaseMediaPlayer();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                setSeekOnStart(currentPosition);
                startPlayLogic();
            }
        }, 500);
    }


    /**
     * 播放错误的时候，删除缓存文件
     */
    protected void deleteCacheFileWhenError() {
        clearCurrentCache();
        Debuger.printfError("Link Or mCache Error, Please Try Again" + mUrl);
        mUrl = mOriginUrl;
    }

    @Override
    public void onPrepared() {
        if (mCurrentState != CURRENT_STATE_PREPAREING) return;

        try {
            if (GSYVideoManager.instance().getMediaPlayer() != null) {
                GSYVideoManager.instance().getMediaPlayer().start();
            }

            setStateAndUi(CURRENT_STATE_PLAYING);

            if (mVideoAllCallBack != null && isCurrentMediaListener()) {
                Debuger.printfLog("onPrepared");
                mVideoAllCallBack.onPrepared(mOriginUrl, mTitle, this);
            }

            if (GSYVideoManager.instance().getMediaPlayer() != null && mSeekOnStart > 0) {
                GSYVideoManager.instance().getMediaPlayer().seekTo(mSeekOnStart);
                mSeekOnStart = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        createNetWorkState();

        listenerNetWorkState();

        mHadPlay = true;

        if (mTextureView != null) {
            mTextureView.onResume();
        }
    }

    @Override
    public void onAutoCompletion() {
        setStateAndUi(CURRENT_STATE_AUTO_COMPLETE);

        mSaveChangeViewTIme = 0;

        if (mTextureViewContainer.getChildCount() > 0) {
            mTextureViewContainer.removeAllViews();
        }

        if (!mIfCurrentIsFullscreen)
            GSYVideoManager.instance().setLastListener(null);
        mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
        ((Activity) getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        releaseNetWorkState();

        if (mVideoAllCallBack != null && isCurrentMediaListener()) {
            Debuger.printfLog("onAutoComplete");
            mVideoAllCallBack.onAutoComplete(mOriginUrl, mTitle, this);
        }
    }

    @Override
    public void onCompletion() {
        //make me normal first
        setStateAndUi(CURRENT_STATE_NORMAL);

        mSaveChangeViewTIme = 0;

        if (mTextureViewContainer.getChildCount() > 0) {
            mTextureViewContainer.removeAllViews();
        }

        if (!mIfCurrentIsFullscreen) {
            GSYVideoManager.instance().setListener(null);
            GSYVideoManager.instance().setLastListener(null);
        }
        GSYVideoManager.instance().setCurrentVideoHeight(0);
        GSYVideoManager.instance().setCurrentVideoWidth(0);

        mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
        ((Activity) getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        releaseNetWorkState();

    }

    @Override
    public void onSeekComplete() {

    }

    @Override
    public void onError(int what, int extra) {

        if (mNetChanged) {
            mNetChanged = false;
            netWorkErrorLogic();
            if (mVideoAllCallBack != null) {
                mVideoAllCallBack.onPlayError(mOriginUrl, mTitle, this);
            }
            return;
        }

        if (what != 38 && what != -38) {
            setStateAndUi(CURRENT_STATE_ERROR);
            deleteCacheFileWhenError();
            if (mVideoAllCallBack != null) {
                mVideoAllCallBack.onPlayError(mOriginUrl, mTitle, this);
            }
        }
    }

    @Override
    public void onInfo(int what, int extra) {
        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
            mBackUpPlayingBufferState = mCurrentState;
            //避免在onPrepared之前就进入了buffering，导致一只loading
            if (mHadPlay && mCurrentState != CURRENT_STATE_PREPAREING && mCurrentState > 0)
                setStateAndUi(CURRENT_STATE_PLAYING_BUFFERING_START);

        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
            if (mBackUpPlayingBufferState != -1) {

                if (mHadPlay && mCurrentState != CURRENT_STATE_PREPAREING && mCurrentState > 0)
                    setStateAndUi(mBackUpPlayingBufferState);

                mBackUpPlayingBufferState = -1;
            }
        } else if (what == IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED) {
            mRotate = extra;
            if (mTextureView != null)
                mTextureView.setRotation(mRotate);
        }
    }

    @Override
    public void onVideoSizeChanged() {
        int mVideoWidth = GSYVideoManager.instance().getCurrentVideoWidth();
        int mVideoHeight = GSYVideoManager.instance().getCurrentVideoHeight();
        if (mVideoWidth != 0 && mVideoHeight != 0 && mTextureView != null) {
            mTextureView.requestLayout();
        }
    }

    /**
     * 清除当前缓存
     */
    public void clearCurrentCache() {
        //只有都为true时，才是缓存文件
        if (mCacheFile && mCache) {
            //是否为缓存文件
            Debuger.printfError(" mCacheFile Local Error " + mUrl);
            //可能是因为缓存文件除了问题
            CommonUtil.deleteFile(mUrl.replace("file://", ""));
            mUrl = mOriginUrl;
        } else if (mUrl.contains("127.0.0.1")) {
            //是否为缓存了未完成的文件
            Md5FileNameGenerator md5FileNameGenerator = new Md5FileNameGenerator();
            String name = md5FileNameGenerator.generate(mOriginUrl);
            if (mCachePath != null) {
                String path = mCachePath.getAbsolutePath() + File.separator + name + ".download";
                CommonUtil.deleteFile(path);
            } else {
                String path = StorageUtils.getIndividualCacheDirectory
                        (getActivityContext().getApplicationContext()).getAbsolutePath()
                        + File.separator + name + ".download";
                CommonUtil.deleteFile(path);
            }
        }

    }

    /**
     * 获取当前播放进度
     */
    public int getCurrentPositionWhenPlaying() {
        int position = 0;
        if (mCurrentState == CURRENT_STATE_PLAYING || mCurrentState == CURRENT_STATE_PAUSE) {
            try {
                position = (int) GSYVideoManager.instance().getMediaPlayer().getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                return position;
            }
        }
        return position;
    }

    /**
     * 获取当前总时长
     */
    public int getDuration() {
        int duration = 0;
        try {
            duration = (int) GSYVideoManager.instance().getMediaPlayer().getDuration();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return duration;
        }
        return duration;
    }

    /**
     * 页面销毁了记得调用是否所有的video
     */
    public static void releaseAllVideos() {
        if (GSYVideoManager.instance().listener() != null) {
            GSYVideoManager.instance().listener().onCompletion();
        }
        GSYVideoManager.instance().releaseMediaPlayer();
    }

    /**
     * 释放吧
     */
    public void release() {
        mSaveChangeViewTIme = 0;
        if (isCurrentMediaListener() &&
                (System.currentTimeMillis() - mSaveChangeViewTIme) > CHANGE_DELAY_TIME) {
            releaseAllVideos();
        }
    }


    protected boolean isCurrentMediaListener() {
        return GSYVideoManager.instance().listener() != null
                && GSYVideoManager.instance().listener() == this;
    }

    /**
     * 创建网络监听
     */
    protected void createNetWorkState() {
        if (mNetInfoModule == null) {
            mNetInfoModule = new NetInfoModule(getActivityContext().getApplicationContext(), new NetInfoModule.NetChangeListener() {
                @Override
                public void changed(String state) {
                    if (!mNetSate.equals(state)) {
                        Debuger.printfError("******* change network state ******* " + state);
                        mNetChanged = true;
                    }
                    mNetSate = state;
                }
            });
            mNetSate = mNetInfoModule.getCurrentConnectionType();
        }
    }

    /**
     * 监听网络状态
     */
    protected void listenerNetWorkState() {
        if (mNetInfoModule != null) {
            mNetInfoModule.onHostResume();
        }
    }

    /**
     * 取消网络监听
     */
    protected void unListenerNetWorkState() {
        if (mNetInfoModule != null) {
            mNetInfoModule.onHostPause();
        }
    }

    /**
     * 释放网络监听
     */
    protected void releaseNetWorkState() {
        if (mNetInfoModule != null) {
            mNetInfoModule.onHostPause();
            mNetInfoModule = null;
        }
    }

    /************************* 需要继承处理部分 *************************/

    /**
     * 设置播放显示状态
     *
     * @param state
     */
    protected abstract void setStateAndUi(int state);


    /**
     * 当前UI
     */
    public abstract int getLayoutId();

    /**
     * 开始播放
     */
    public abstract void startPlayLogic();

    /************************* 公开接口 *************************/

    /**
     * 设置自定义so包加载类，必须在setUp之前调用
     * 不然setUp时会第一次实例化GSYVideoManager
     */
    public void setIjkLibLoader(IjkLibLoader libLoader) {
        GSYVideoManager.setIjkLibLoader(libLoader);
    }

    /**
     * 获取当前播放状态
     */
    public int getCurrentState() {
        return mCurrentState;
    }

    /**
     * 播放tag防止错误，因为普通的url也可能重复
     */
    public String getPlayTag() {
        return mPlayTag;
    }

    /**
     * 播放tag防止错误，因为普通的url也可能重复
     *
     * @param playTag 保证不重复就好
     */
    public void setPlayTag(String playTag) {
        this.mPlayTag = playTag;
    }


    public int getPlayPosition() {
        return mPlayPosition;
    }

    /**
     * 设置播放位置防止错位
     */
    public void setPlayPosition(int playPosition) {
        this.mPlayPosition = playPosition;
    }

    /**
     * 网络速度
     * 注意，这里如果是开启了缓存，因为读取本地代理，缓存成功后还是存在速度的
     * 再打开已经缓存的本地文件，网络速度才会回0.因为是播放本地文件了
     */
    public long getNetSpeed() {
        if (GSYVideoManager.instance().getMediaPlayer() != null
                && (GSYVideoManager.instance().getMediaPlayer() instanceof IjkMediaPlayer)) {
            try {
                return ((IjkMediaPlayer) GSYVideoManager.instance().getMediaPlayer()).getTcpSpeed();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return -1;
        } else {
            return -1;
        }

    }

    /**
     * 网络速度
     * 注意，这里如果是开启了缓存，因为读取本地代理，缓存成功后还是存在速度的
     * 再打开已经缓存的本地文件，网络速度才会回0.因为是播放本地文件了
     */
    public String getNetSpeedText() {
        long speed = getNetSpeed();
        return getTextSpeed(speed);
    }

    public long getSeekOnStart() {
        return mSeekOnStart;
    }

    /**
     * 从哪里开始播放
     * 目前有时候前几秒有跳动问题，毫秒
     * 需要在startPlayLogic之前，即播放开始之前
     */
    public void setSeekOnStart(long seekOnStart) {
        this.mSeekOnStart = seekOnStart;
    }

    /**
     * 缓冲进度/缓存进度
     */
    public int getBuffterPoint() {
        return mBuffterPoint;
    }

    /**
     * 是否全屏
     */
    public boolean isIfCurrentIsFullscreen() {
        return mIfCurrentIsFullscreen;
    }

    public void setIfCurrentIsFullscreen(boolean ifCurrentIsFullscreen) {
        this.mIfCurrentIsFullscreen = ifCurrentIsFullscreen;
    }

    public boolean isLooping() {
        return mLooping;
    }

    /**
     * 设置循环
     */
    public void setLooping(boolean looping) {
        this.mLooping = looping;
    }


    /**
     * 设置播放过程中的回调
     *
     * @param mVideoAllCallBack
     */
    public void setVideoAllCallBack(VideoAllCallBack mVideoAllCallBack) {
        this.mVideoAllCallBack = mVideoAllCallBack;
    }

    public float getSpeed() {
        return mSpeed;
    }

    /**
     * 播放速度
     */
    public void setSpeed(float speed) {
        setSpeed(speed, false);
    }

    /**
     * 播放速度
     *
     * @param speed      速度
     * @param soundTouch 是否对6.0下开启变速不变调
     */
    public void setSpeed(float speed, boolean soundTouch) {
        this.mSpeed = speed;
        this.mSoundTouch = soundTouch;
        if (GSYVideoManager.instance().getMediaPlayer() != null
                && GSYVideoManager.instance().getMediaPlayer() instanceof IjkMediaPlayer) {
            if (speed > 0) {
                try {
                    ((IjkMediaPlayer) GSYVideoManager.instance().getMediaPlayer()).setSpeed(speed);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (mSoundTouch) {
                    VideoOptionModel videoOptionModel =
                            new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", 1);
                    List<VideoOptionModel> list = GSYVideoManager.instance().getOptionModelList();
                    if (list != null) {
                        list.add(videoOptionModel);
                    } else {
                        list = new ArrayList<>();
                        list.add(videoOptionModel);
                    }
                    GSYVideoManager.instance().setOptionModelList(list);
                }
            }
        }
    }

    /**
     * 播放中生效的播放数据
     *
     * @param speed
     * @param soundTouch
     */
    public void setSpeedPlaying(float speed, boolean soundTouch) {
        setSpeed(speed, soundTouch);
        if (GSYVideoManager.instance().getMediaPlayer() != null) {
            IjkMediaPlayer ijkMediaPlayer = (IjkMediaPlayer) GSYVideoManager.instance().getMediaPlayer();
            try {
                ijkMediaPlayer.setSpeed(speed);
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", (soundTouch) ? 1 : 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isShowPauseCover() {
        return mShowPauseCover;
    }

    /**
     * 是否需要加载显示暂停的cover图片
     * 打开状态下，暂停退到后台，再回到前台不会显示黑屏，但可以对某些机型有概率出现OOM
     * 关闭情况下，暂停退到后台，再回到前台显示黑屏
     *
     * @param showPauseCover 默认true
     */
    public void setShowPauseCover(boolean showPauseCover) {
        this.mShowPauseCover = showPauseCover;
    }

    /**
     * seekto what you want
     */
    public void seekTo(long position) {
        try {
            if (GSYVideoManager.instance().getMediaPlayer() != null && position > 0) {
                GSYVideoManager.instance().getMediaPlayer().seekTo(position);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
