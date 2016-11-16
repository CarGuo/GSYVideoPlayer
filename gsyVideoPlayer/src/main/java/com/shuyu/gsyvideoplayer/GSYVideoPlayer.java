package com.shuyu.gsyvideoplayer;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.danikula.videocache.HttpProxyCacheServer;
import com.shuyu.gsyvideoplayer.listener.GSYMediaPlayerListener;
import com.shuyu.gsyvideoplayer.listener.VideoAllCallBack;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import tv.danmaku.ijk.media.player.IMediaPlayer;

import static com.shuyu.gsyvideoplayer.utils.CommonUtil.hideSupportActionBar;
import static com.shuyu.gsyvideoplayer.utils.CommonUtil.showSupportActionBar;

/**
 * Created by shuyu on 2016/11/11.
 */

public abstract class GSYVideoPlayer extends FrameLayout implements View.OnClickListener, View.OnTouchListener, SeekBar.OnSeekBarChangeListener, GSYMediaPlayerListener, TextureView.SurfaceTextureListener {

    public static final String TAG = "GSYVideoPlayer";

    public static final int FULLSCREEN_ID = 83797;

    protected static final int CURRENT_STATE_NORMAL = 0; //正常
    protected static final int CURRENT_STATE_PREPAREING = 1; //准备中
    protected static final int CURRENT_STATE_PLAYING = 2; //播放中
    protected static final int CURRENT_STATE_PLAYING_BUFFERING_START = 3; //开始缓冲
    protected static final int CURRENT_STATE_PAUSE = 5; //暂停
    protected static final int CURRENT_STATE_AUTO_COMPLETE = 6; //自动播放结束
    protected static final int CURRENT_STATE_ERROR = 7; //错误状态

    public static final int FULL_SCREEN_NORMAL_DELAY = 2000;

    protected static int BACKUP_PLAYING_BUFFERING_STATE = -1;

    protected static boolean IF_FULLSCREEN_FROM_NORMAL = false;

    public static boolean IF_RELEASE_WHEN_ON_PAUSE = true;

    public static boolean WIFI_TIP_DIALOG_SHOWED = false;

    protected static long CLICK_QUIT_FULLSCREEN_TIME = 0;


    protected static Timer UPDATE_PROGRESS_TIMER;


    private Context mContext;

    protected ImageView mStartButton;
    protected SeekBar mProgressBar;
    protected ImageView mFullscreenButton;
    protected TextView mCurrentTimeTextView, mTotalTimeTextView;
    protected ViewGroup mTextureViewContainer;
    protected ViewGroup mTopContainer, mBottomContainer;
    protected GSYTextureView mTextureView;
    protected Surface mSurface;
    protected ImageView mBackButton;

    protected String mUrl;
    protected Object[] mObjects;
    protected Map<String, String> mMapHeadData = new HashMap<>();
    protected ProgressTimerTask mProgressTimerTask;
    protected AudioManager mAudioManager; //音频焦点的监听
    protected VideoAllCallBack mVideoAllCallBack;

    protected Handler mHandler = new Handler();

    protected String mPlayTag = ""; //播放的tag，防止错误，因为普通的url也可能重复

    protected int mPlayPosition = -22; //播放的tag，防止错误，因为普通的url也可能重复

    protected float mDownX;//触摸的X

    protected float mDownY; //触摸的Y

    protected float mBrightnessData = -1; //亮度

    protected int mCurrentState = -1; //当前的播放状态

    protected int mDownPosition; //手指放下的位置

    protected int mGestureDownVolume; //手势调节音量的大小

    protected int mScreenWidth; //屏幕宽度

    protected int mScreenHeight; //屏幕高度

    protected int mThreshold = 80; //手势偏差值

    protected int mSeekToInAdvance = -1; //// TODO: 2016/11/13 调过广告

    protected int mRotate = 0; //针对某些视频的旋转信息做了旋转处理

    protected int mSeekTimePosition; //手动改变滑动的位置

    protected long mPauseTime; //保存暂停时的时间

    protected long mCurrentPosition; //当前的播放位置

    protected boolean mTouchingProgressBar = false;

    protected boolean mIfCurrentIsFullscreen = false;

    protected boolean mIsTouchWiget = false;

    protected boolean mChangeVolume = false;//是否改变音量

    protected boolean mChangePosition = false;//是否改变播放进度

    protected boolean mBrightness = false;//是否改变亮度

    protected boolean mFirstTouch = false;//是否首次触摸

    protected boolean mLooping = false;//// TODO: 2016/11/13 循环

    protected boolean mCache = false;//是否播边边缓冲

    protected boolean mActionBar = false;//是否需要在利用window实现全屏幕的时候隐藏actionbar

    protected boolean mStatusBar = false;//是否需要在利用window实现全屏幕的时候隐藏statusbar


    /**
     * 当前UI
     */
    public abstract int getLayoutId();

    /**
     * 开始播放
     */
    public abstract void startPlayLogic();

    public GSYVideoPlayer(Context context) {
        super(context);
        init(context);
    }

    public GSYVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    protected void init(Context context) {
        this.mContext = context;
        View.inflate(context, getLayoutId(), this);
        mStartButton = (ImageView) findViewById(R.id.start);
        mBackButton = (ImageView) findViewById(R.id.back);
        mFullscreenButton = (ImageView) findViewById(R.id.fullscreen);
        mProgressBar = (SeekBar) findViewById(R.id.progress);
        mCurrentTimeTextView = (TextView) findViewById(R.id.current);
        mTotalTimeTextView = (TextView) findViewById(R.id.total);
        mBottomContainer = (ViewGroup) findViewById(R.id.layout_bottom);
        mTextureViewContainer = (RelativeLayout) findViewById(R.id.surface_container);
        mTopContainer = (ViewGroup) findViewById(R.id.layout_top);
        if (isInEditMode())
            return;
        mStartButton.setOnClickListener(this);
        mFullscreenButton.setOnClickListener(this);
        mProgressBar.setOnSeekBarChangeListener(this);
        mBottomContainer.setOnClickListener(this);
        mTextureViewContainer.setOnClickListener(this);
        mProgressBar.setOnTouchListener(this);

        mTextureViewContainer.setOnTouchListener(this);
        mFullscreenButton.setOnTouchListener(this);
        mScreenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * 设置播放URL
     *
     * @param url
     * @param cacheWithPlay 是否边播边缓存
     * @param objects
     * @return
     */
    public boolean setUp(String url, boolean cacheWithPlay, Object... objects) {
        mCache = cacheWithPlay;
        if (isCurrentMediaListener() &&
                (System.currentTimeMillis() - CLICK_QUIT_FULLSCREEN_TIME) < FULL_SCREEN_NORMAL_DELAY)
            return false;
        mCurrentState = CURRENT_STATE_NORMAL;
        if (cacheWithPlay && url.startsWith("http")) {
            HttpProxyCacheServer proxy = GSYVideoManager.getProxy(getContext().getApplicationContext());
            url = proxy.getProxyUrl(url);
        }
        this.mUrl = url;
        this.mObjects = objects;
        setStateAndUi(CURRENT_STATE_NORMAL);
        return true;
    }

    /**
     * 设置播放URL
     *
     * @param url
     * @param cacheWithPlay 是否边播边缓存
     * @param mapHeadData
     * @param objects
     * @return
     */

    public boolean setUp(String url, boolean cacheWithPlay, Map<String, String> mapHeadData, Object... objects) {
        if (setUp(url, cacheWithPlay, objects)) {
            this.mMapHeadData.clear();
            this.mMapHeadData.putAll(mapHeadData);
            return true;
        }
        return false;
    }

    /**
     * 设置播放过程中的回调
     *
     * @param mVideoAllCallBack
     */
    protected void setVideoAllCallBack(VideoAllCallBack mVideoAllCallBack) {
        this.mVideoAllCallBack = mVideoAllCallBack;
    }


    /**
     * 设置播放显示状态
     *
     * @param state
     */
    protected void setStateAndUi(int state) {
        mCurrentState = state;
        switch (mCurrentState) {
            case CURRENT_STATE_NORMAL:
                if (isCurrentMediaListener()) {
                    cancelProgressTimer();
                    GSYVideoManager.instance().releaseMediaPlayer();
                }
                break;
            case CURRENT_STATE_PREPAREING:
                resetProgressAndTime();
                break;
            case CURRENT_STATE_PLAYING:
                startProgressTimer();
                break;
            case CURRENT_STATE_PAUSE:
                startProgressTimer();
                break;
            case CURRENT_STATE_ERROR:
                if (isCurrentMediaListener()) {
                    GSYVideoManager.instance().releaseMediaPlayer();
                }
                break;
            case CURRENT_STATE_AUTO_COMPLETE:
                cancelProgressTimer();
                mProgressBar.setProgress(100);
                mCurrentTimeTextView.setText(mTotalTimeTextView.getText());
                break;
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.start) {
            if (TextUtils.isEmpty(mUrl)) {
                Toast.makeText(getContext(), getResources().getString(R.string.no_url), Toast.LENGTH_SHORT).show();
                return;
            }
            if (mCurrentState == CURRENT_STATE_NORMAL || mCurrentState == CURRENT_STATE_ERROR) {
                if (!mUrl.startsWith("file") && !CommonUtil.isWifiConnected(getContext()) && !WIFI_TIP_DIALOG_SHOWED) {
                    showWifiDialog();
                    return;
                }
                startButtonLogic();
            } else if (mCurrentState == CURRENT_STATE_PLAYING) {
                GSYVideoManager.instance().getMediaPlayer().pause();
                setStateAndUi(CURRENT_STATE_PAUSE);
                if (mVideoAllCallBack != null && isCurrentMediaListener()) {
                    if (mIfCurrentIsFullscreen) {
                        mVideoAllCallBack.onClickStopFullscreen(mUrl, mObjects);
                    } else {
                        mVideoAllCallBack.onClickStop(mUrl, mObjects);
                    }
                }
            } else if (mCurrentState == CURRENT_STATE_PAUSE) {
                if (mVideoAllCallBack != null && isCurrentMediaListener()) {
                    if (mIfCurrentIsFullscreen) {
                        mVideoAllCallBack.onClickResumeFullscreen(mUrl, mObjects);
                    } else {
                        mVideoAllCallBack.onClickResume(mUrl, mObjects);
                    }
                }
                GSYVideoManager.instance().getMediaPlayer().start();
                setStateAndUi(CURRENT_STATE_PLAYING);
            } else if (mCurrentState == CURRENT_STATE_AUTO_COMPLETE) {
                startButtonLogic();
            }
        } else if (i == R.id.surface_container && mCurrentState == CURRENT_STATE_ERROR) {
            if (mVideoAllCallBack != null) {
                mVideoAllCallBack.onClickStartError(mUrl, mObjects);
            }
            prepareVideo();
        }
    }

    protected void showWifiDialog() {
    }

    /**
     * 播放按键的逻辑
     */
    private void startButtonLogic() {
        if (mVideoAllCallBack != null && mCurrentState == CURRENT_STATE_NORMAL) {
            mVideoAllCallBack.onClickStartIcon(mUrl, mObjects);
        } else if (mVideoAllCallBack != null) {
            mVideoAllCallBack.onClickStartError(mUrl, mObjects);
        }
        prepareVideo();
    }

    /**
     * 开始状态视频播放
     */
    protected void prepareVideo() {
        if (GSYVideoManager.instance().listener() != null) {
            GSYVideoManager.instance().listener().onCompletion();
        }
        GSYVideoManager.instance().setListener(this);
        GSYVideoManager.instance().setPlayTag(mPlayTag);
        GSYVideoManager.instance().setPlayPosition(mPlayPosition);
        addTextureView();
        AudioManager mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

        ((Activity) getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        GSYVideoManager.instance().prepare(mUrl, mMapHeadData, mLooping);
        setStateAndUi(CURRENT_STATE_PREPAREING);
    }

    /**
     * 监听是否有外部其他多媒体开始播放
     */
    private AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            releaseAllVideos();
                        }
                    });
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (GSYVideoManager.instance().getMediaPlayer().isPlaying()) {
                        GSYVideoManager.instance().getMediaPlayer().pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    break;
            }
        }
    };


    /**
     * 重置
     */
    public void onVideoReset() {
        setStateAndUi(CURRENT_STATE_NORMAL);
    }

    /**
     * 暂停状态
     */
    public void onVideoPause() {
        if (GSYVideoManager.instance().getMediaPlayer().isPlaying()) {
            setStateAndUi(CURRENT_STATE_PAUSE);
            mPauseTime = System.currentTimeMillis();
            mCurrentPosition = GSYVideoManager.instance().getMediaPlayer().getCurrentPosition();
            if (GSYVideoManager.instance().getMediaPlayer() != null)
                GSYVideoManager.instance().getMediaPlayer().pause();
        }
    }

    /**
     * 恢复暂停状态
     */
    public void onVideoResume() {
        mPauseTime = 0;
        if (mCurrentState == CURRENT_STATE_PAUSE) {
            if (mCurrentPosition > 0 && GSYVideoManager.instance().getMediaPlayer() != null) {
                setStateAndUi(CURRENT_STATE_PLAYING);
                GSYVideoManager.instance().getMediaPlayer().seekTo(mCurrentPosition);
                GSYVideoManager.instance().getMediaPlayer().start();
            }
        }
    }

    /**
     * 添加播放的view
     */
    protected void addTextureView() {
        if (mTextureViewContainer.getChildCount() > 0) {
            mTextureViewContainer.removeAllViews();
        }
        mTextureView = null;
        mTextureView = new GSYTextureView(getContext());
        mTextureView.setSurfaceTextureListener(this);
        mTextureView.setRotation(mRotate);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        mTextureViewContainer.addView(mTextureView, layoutParams);
    }

    /**
     * 设置界面选择
     */
    public void setRotationView(int rotate) {
        this.mRotate = rotate;
        mTextureView.setRotation(rotate);
    }

    public void refreshVideo() {
        if (mTextureView != null) {
            mTextureView.requestLayout();
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurface = new Surface(surface);
        GSYVideoManager.instance().setDisplay(mSurface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        GSYVideoManager.instance().setDisplay(null);
        surface.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    /**
     * 亮度、进度、音频
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int id = v.getId();
        if (id == R.id.fullscreen) {
            return false;
        }
        if (id == R.id.surface_container) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mTouchingProgressBar = true;
                    mDownX = x;
                    mDownY = y;
                    mChangeVolume = false;
                    mChangePosition = false;
                    mBrightness = false;
                    mFirstTouch = true;

                    break;
                case MotionEvent.ACTION_MOVE:
                    float deltaX = x - mDownX;
                    float deltaY = y - mDownY;
                    float absDeltaX = Math.abs(deltaX);
                    float absDeltaY = Math.abs(deltaY);

                    if (mIfCurrentIsFullscreen || mIsTouchWiget) {
                        if (!mChangePosition && !mChangeVolume && !mBrightness) {
                            if (absDeltaX > mThreshold || absDeltaY > mThreshold) {
                                cancelProgressTimer();
                                if (absDeltaX >= mThreshold) {
                                    mChangePosition = true;
                                    mDownPosition = getCurrentPositionWhenPlaying();
                                    if (mVideoAllCallBack != null && isCurrentMediaListener()) {
                                        mVideoAllCallBack.onTouchScreenSeekPosition(mUrl, mObjects);
                                    }
                                } else {
                                    if (mFirstTouch) {
                                        mBrightness = mDownX < mScreenWidth * 0.5f;
                                        mFirstTouch = false;
                                    }
                                    if (!mBrightness) {
                                        mChangeVolume = true;
                                        mGestureDownVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                        if (mVideoAllCallBack != null && isCurrentMediaListener()) {
                                            mVideoAllCallBack.onTouchScreenSeekVolume(mUrl, mObjects);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (mChangePosition) {
                        int totalTimeDuration = getDuration();
                        mSeekTimePosition = (int) (mDownPosition + deltaX * totalTimeDuration / mScreenWidth);
                        if (mSeekTimePosition > totalTimeDuration)
                            mSeekTimePosition = totalTimeDuration;
                        String seekTime = CommonUtil.stringForTime(mSeekTimePosition);
                        String totalTime = CommonUtil.stringForTime(totalTimeDuration);

                        showProgressDialog(deltaX, seekTime, mSeekTimePosition, totalTime, totalTimeDuration);
                    } else if (mChangeVolume) {
                        deltaY = -deltaY;
                        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                        int deltaV = (int) (max * deltaY * 3 / mScreenHeight);
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mGestureDownVolume + deltaV, 0);
                        int volumePercent = (int) (mGestureDownVolume * 100 / max + deltaY * 3 * 100 / mScreenHeight);

                        showVolumeDialog(-deltaY, volumePercent);
                    } else if (!mChangePosition && mBrightness) {
                        float percent = -deltaY / mScreenHeight;
                        onBrightnessSlide(percent);
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    mTouchingProgressBar = false;
                    dismissProgressDialog();
                    dismissVolumeDialog();
                    dismissBrightnessDialog();
                    if (mChangePosition) {
                        GSYVideoManager.instance().getMediaPlayer().seekTo(mSeekTimePosition);
                        int duration = getDuration();
                        int progress = mSeekTimePosition * 100 / (duration == 0 ? 1 : duration);
                        mProgressBar.setProgress(progress);
                    }
                    startProgressTimer();
                    if (mVideoAllCallBack != null && isCurrentMediaListener()) {
                        if (mIfCurrentIsFullscreen) {
                            mVideoAllCallBack.onClickSeekbarFullscreen(mUrl, mObjects);
                        } else {
                            mVideoAllCallBack.onClickSeekbar(mUrl, mObjects);
                        }
                    }
                    break;
            }
        } else if (id == R.id.progress) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    cancelProgressTimer();
                    ViewParent vpdown = getParent();
                    while (vpdown != null) {
                        vpdown.requestDisallowInterceptTouchEvent(true);
                        vpdown = vpdown.getParent();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    startProgressTimer();
                    ViewParent vpup = getParent();
                    while (vpup != null) {
                        vpup.requestDisallowInterceptTouchEvent(false);
                        vpup = vpup.getParent();
                    }
                    mBrightnessData = -1f;
                    break;
            }
        }

        return false;
    }


    protected void showProgressDialog(float deltaX,
                                      String seekTime, int seekTimePosition,
                                      String totalTime, int totalTimeDuration) {
    }

    protected void dismissProgressDialog() {

    }

    protected void showVolumeDialog(float deltaY, int volumePercent) {

    }

    protected void dismissVolumeDialog() {

    }

    protected void showBrightnessDialog(float percent) {

    }

    protected void dismissBrightnessDialog() {

    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    /***
     * 拖动进度条
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (GSYVideoManager.instance().getMediaPlayer() != null && GSYVideoManager.instance().getMediaPlayer().isPlaying()) {
            int time = seekBar.getProgress() * getDuration() / 100;
            GSYVideoManager.instance().getMediaPlayer().seekTo(time);
        }
    }

    @Override
    public void onPrepared() {
        if (mCurrentState != CURRENT_STATE_PREPAREING) return;
        GSYVideoManager.instance().getMediaPlayer().start();
        if (mSeekToInAdvance != -1) {
            GSYVideoManager.instance().getMediaPlayer().seekTo(mSeekToInAdvance);
            mSeekToInAdvance = -1;
        }
        startProgressTimer();
        setStateAndUi(CURRENT_STATE_PLAYING);
    }

    @Override
    public void onAutoCompletion() {
        if (mVideoAllCallBack != null && isCurrentMediaListener()) {
            if (mIfCurrentIsFullscreen) {
                mVideoAllCallBack.onAutoCompleteFullscreen(mUrl, mObjects);
            } else {
                mVideoAllCallBack.onAutoComplete(mUrl, mObjects);
            }
        }
        setStateAndUi(CURRENT_STATE_AUTO_COMPLETE);
        if (mTextureViewContainer.getChildCount() > 0) {
            mTextureViewContainer.removeAllViews();
        }

        if (IF_FULLSCREEN_FROM_NORMAL) {//如果在进入全屏后播放完就初始化自己非全屏的控件
            IF_FULLSCREEN_FROM_NORMAL = false;
            GSYVideoManager.instance().lastListener().onAutoCompletion();
        }
        GSYVideoManager.instance().setLastListener(null);
        AudioManager mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
        ((Activity) getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onCompletion() {
        //make me normal first
        setStateAndUi(CURRENT_STATE_NORMAL);
        if (mTextureViewContainer.getChildCount() > 0) {
            mTextureViewContainer.removeAllViews();
        }

        if (IF_FULLSCREEN_FROM_NORMAL) {//如果在进入全屏后播放完就初始化自己非全屏的控件
            IF_FULLSCREEN_FROM_NORMAL = false;
            GSYVideoManager.instance().lastListener().onCompletion();
        }
        GSYVideoManager.instance().setListener(null);
        GSYVideoManager.instance().setLastListener(null);
        GSYVideoManager.instance().setCurrentVideoHeight(0);
        GSYVideoManager.instance().setCurrentVideoWidth(0);

        AudioManager mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
        ((Activity) getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onBufferingUpdate(int percent) {
        if (mCurrentState != CURRENT_STATE_NORMAL && mCurrentState != CURRENT_STATE_PREPAREING) {
            setTextAndProgress(percent);
        }
    }

    @Override
    public void onSeekComplete() {

    }

    @Override
    public void onError(int what, int extra) {
        if (what != 38 && what != -38) {
            setStateAndUi(CURRENT_STATE_ERROR);
        }
    }

    @Override
    public void onInfo(int what, int extra) {
        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
            BACKUP_PLAYING_BUFFERING_STATE = mCurrentState;
            setStateAndUi(CURRENT_STATE_PLAYING_BUFFERING_START);
        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
            if (BACKUP_PLAYING_BUFFERING_STATE != -1) {
                setStateAndUi(BACKUP_PLAYING_BUFFERING_STATE);
                BACKUP_PLAYING_BUFFERING_STATE = -1;
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
        if (mVideoWidth != 0 && mVideoHeight != 0) {
            mTextureView.requestLayout();
        }
    }

    @Override
    public void onBackFullscreen() {

    }

    protected void startProgressTimer() {
        cancelProgressTimer();
        UPDATE_PROGRESS_TIMER = new Timer();
        mProgressTimerTask = new ProgressTimerTask();
        UPDATE_PROGRESS_TIMER.schedule(mProgressTimerTask, 0, 300);
    }

    protected void cancelProgressTimer() {
        if (UPDATE_PROGRESS_TIMER != null) {
            UPDATE_PROGRESS_TIMER.cancel();
        }
        if (mProgressTimerTask != null) {
            mProgressTimerTask.cancel();
        }

    }

    protected class ProgressTimerTask extends TimerTask {
        @Override
        public void run() {
            if (mCurrentState == CURRENT_STATE_PLAYING || mCurrentState == CURRENT_STATE_PAUSE) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setTextAndProgress(0);
                    }
                });
            }
        }
    }

    protected int getCurrentPositionWhenPlaying() {
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

    protected int getDuration() {
        int duration = 0;
        try {
            duration = (int) GSYVideoManager.instance().getMediaPlayer().getDuration();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return duration;
        }
        return duration;
    }

    protected void setTextAndProgress(int secProgress) {
        int position = getCurrentPositionWhenPlaying();
        int duration = getDuration();
        int progress = position * 100 / (duration == 0 ? 1 : duration);
        setProgressAndTime(progress, secProgress, position, duration);
    }

    protected void setProgressAndTime(int progress, int secProgress, int currentTime, int totalTime) {
        if (!mTouchingProgressBar) {
            if (progress != 0) mProgressBar.setProgress(progress);
        }
        if (secProgress > 95) secProgress = 100;
        if (secProgress != 0) mProgressBar.setSecondaryProgress(secProgress);
        mCurrentTimeTextView.setText(CommonUtil.stringForTime(currentTime));
        mTotalTimeTextView.setText(CommonUtil.stringForTime(totalTime));
    }

    protected void resetProgressAndTime() {
        mProgressBar.setProgress(0);
        mProgressBar.setSecondaryProgress(0);
        mCurrentTimeTextView.setText(CommonUtil.stringForTime(0));
        mTotalTimeTextView.setText(CommonUtil.stringForTime(0));
    }

    public static void releaseAllVideos() {
        if (IF_RELEASE_WHEN_ON_PAUSE) {
            if (GSYVideoManager.instance().listener() != null) {
                GSYVideoManager.instance().listener().onCompletion();
            }
            GSYVideoManager.instance().releaseMediaPlayer();
        } else {
            IF_RELEASE_WHEN_ON_PAUSE = true;
        }
    }

    /**
     * if I am playing release me
     */
    public void release() {
        if (isCurrentMediaListener() &&
                (System.currentTimeMillis() - CLICK_QUIT_FULLSCREEN_TIME) > FULL_SCREEN_NORMAL_DELAY) {
            releaseAllVideos();
        }
    }


    protected boolean isCurrentMediaListener() {
        return GSYVideoManager.instance().listener() != null
                && GSYVideoManager.instance().listener() == this;
    }


    /**
     * 利用window层播放全屏效果
     *
     * @param context
     * @param actionBar 是否有actionBar，有的话需要隐藏
     * @param statusBar 是否有状态bar，有的话需要隐藏
     */
    public void startWindowFullscreen(final Context context, final boolean actionBar, final boolean statusBar) {
        this.mActionBar = actionBar;
        this.mStatusBar = statusBar;
        hideSupportActionBar(context, actionBar, statusBar);

        ViewGroup vp = (ViewGroup) (CommonUtil.scanForActivity(getContext())).findViewById(Window.ID_ANDROID_CONTENT);
        View old = vp.findViewById(FULLSCREEN_ID);
        if (old != null) {
            vp.removeView(old);
        }

        if (mTextureViewContainer.getChildCount() > 0) {
            mTextureViewContainer.removeAllViews();
        }
        try {
            Constructor<GSYVideoPlayer> constructor = (Constructor<GSYVideoPlayer>) GSYVideoPlayer.this.getClass().getConstructor(Context.class);
            GSYVideoPlayer gsyVideoPlayer = constructor.newInstance(getContext());
            gsyVideoPlayer.setId(FULLSCREEN_ID);
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            int w = wm.getDefaultDisplay().getWidth();
            int h = wm.getDefaultDisplay().getHeight();
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(h, w);
            lp.setMargins((w - h) / 2, -(w - h) / 2, 0, 0);
            vp.addView(gsyVideoPlayer, lp);
            gsyVideoPlayer.setUp(mUrl, mCache, mObjects);
            gsyVideoPlayer.setStateAndUi(mCurrentState);
            gsyVideoPlayer.addTextureView();
            gsyVideoPlayer.setRotation(90);
            gsyVideoPlayer.getFullscreenButton().setImageResource(R.drawable.video_shrink);
            gsyVideoPlayer.getFullscreenButton().setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearFullscreenLayout();
                }
            });
            gsyVideoPlayer.getBackButton().setVisibility(VISIBLE);
            gsyVideoPlayer.getBackButton().setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearFullscreenLayout();
                }
            });
            GSYVideoManager.instance().setLastListener(this);
            GSYVideoManager.instance().setListener(gsyVideoPlayer);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 退出window层播放全屏效果
     */
    public void clearFullscreenLayout() {

        showSupportActionBar(mContext, mActionBar, mStatusBar);
        ViewGroup vp = (ViewGroup) (CommonUtil.scanForActivity(getContext())).findViewById(Window.ID_ANDROID_CONTENT);
        View oldF = vp.findViewById(FULLSCREEN_ID);
        GSYVideoPlayer gsyVideoPlayer = null;
        if (oldF != null) {
            gsyVideoPlayer = (GSYVideoPlayer) oldF;
            vp.removeView(oldF);
        }
        mCurrentState = GSYVideoManager.instance().getLastState();
        if (gsyVideoPlayer != null) {
            mCurrentState = gsyVideoPlayer.getCurrentState();
        }
        GSYVideoManager.instance().setListener(GSYVideoManager.instance().lastListener());
        GSYVideoManager.instance().setLastListener(null);
        setStateAndUi(mCurrentState);
        addTextureView();
        CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis();
    }

    /**
     * 滑动改变亮度
     *
     * @param percent
     */
    private void onBrightnessSlide(float percent) {
        if (mBrightnessData < 0) {
            mBrightnessData = ((Activity) (mContext)).getWindow().getAttributes().screenBrightness;
            if (mBrightnessData <= 0.00f) {
                mBrightnessData = 0.50f;
            } else if (mBrightnessData < 0.01f) {
                mBrightnessData = 0.01f;
            }
        }
        WindowManager.LayoutParams lpa = ((Activity) (mContext)).getWindow().getAttributes();
        lpa.screenBrightness = mBrightnessData + percent;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }
        showBrightnessDialog(lpa.screenBrightness);
        ((Activity) (mContext)).getWindow().setAttributes(lpa);
    }

    public boolean isTouchWiget() {
        return mIsTouchWiget;
    }

    public void setIsTouchWiget(boolean isTouchWiget) {
        this.mIsTouchWiget = isTouchWiget;
    }

    /**
     * 获取播放按键
     */
    public ImageView getStartButton() {
        return mStartButton;
    }

    /**
     * 获取全屏按键
     */
    public ImageView getFullscreenButton() {
        return mFullscreenButton;
    }

    /**
     * 获取返回按键
     */
    public ImageView getBackButton() {
        return mBackButton;
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

    public void setPlayPosition(int playPostion) {
        this.mPlayPosition = playPostion;
    }

    /**
     * 退出全屏
     *
     * @return 返回是否全屏
     */
    public static boolean backFromWindowFull(Context context) {
        boolean backFrom = false;
        ViewGroup vp = (ViewGroup) (CommonUtil.scanForActivity(context)).findViewById(Window.ID_ANDROID_CONTENT);
        View oldF = vp.findViewById(FULLSCREEN_ID);
        if (oldF != null) {
            backFrom = true;
            GSYVideoManager.instance().lastListener().onBackFullscreen();
        }
        return backFrom;
    }


}