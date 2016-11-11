package com.shuyu.gsyvideoplayer;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by shuyu on 2016/11/11.
 */

public abstract class GSYVideoPlayer extends FrameLayout implements View.OnClickListener, View.OnTouchListener, SeekBar.OnSeekBarChangeListener, GSYMediaPlayerListener, TextureView.SurfaceTextureListener {

    public static final String TAG = "GSYVideoPlayer";

    protected static final int CURRENT_STATE_NORMAL = 0;
    protected static final int CURRENT_STATE_PREPAREING = 1;
    protected static final int CURRENT_STATE_PLAYING = 2;
    protected static final int CURRENT_STATE_PLAYING_BUFFERING_START = 3;
    protected static final int CURRENT_STATE_PAUSE = 5;
    protected static final int CURRENT_STATE_AUTO_COMPLETE = 6;
    protected static final int CURRENT_STATE_ERROR = 7;
    protected static int BACKUP_PLAYING_BUFFERING_STATE = -1;

    protected boolean mTouchingProgressBar = false;
    protected boolean mIfCurrentIsFullscreen = false;
    protected boolean mIsTouchWiget = false;

    protected static boolean IF_FULLSCREEN_FROM_NORMAL = false;//to prevent infinite looping

    protected static long CLICK_QUIT_FULLSCREEN_TIME = 0;

    public static boolean IF_RELEASE_WHEN_ON_PAUSE = true;
    public static final int FULL_SCREEN_NORMAL_DELAY = 2000;

    public static boolean WIFI_TIP_DIALOG_SHOWED = false;

    protected static Timer UPDATE_PROGRESS_TIMER;
    protected VideoAllCallBack videoAllCallBack;

    protected ImageView startButton;
    protected SeekBar progressBar;
    protected ImageView fullscreenButton;
    protected TextView currentTimeTextView, totalTimeTextView;
    protected ViewGroup textureViewContainer;
    protected ViewGroup topContainer, bottomContainer;
    protected GSYTextureView textureView;
    protected Surface mSurface;
    protected Activity activity;

    protected String mUrl;
    protected Object[] mObjects;
    protected Map<String, String> mMapHeadData = new HashMap<>();
    protected ProgressTimerTask mProgressTimerTask;
    protected AudioManager mAudioManager;

    protected Handler mHandler = new Handler();


    protected float mDownX;
    protected float mDownY;
    protected float brightness = -1;

    protected int mCurrentState = -1;
    protected int mDownPosition;
    protected int mGestureDownVolume;
    protected int mScreenWidth;
    protected int mScreenHeight;
    protected int mThreshold = 80;
    protected int seekToInAdvance = -1;
    private int rotate = 0;

    private long pauseTime;
    private long currentPosition;

    protected boolean mChangeVolume = false;
    protected boolean mChangePosition = false;
    protected boolean mBrightness = false;
    protected boolean firstTouch = false;
    protected boolean mLooping = false;

    protected int mSeekTimePosition;

    public abstract int getLayoutId();

    public GSYVideoPlayer(Context context) {
        super(context);
        init(context);
    }

    public GSYVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    protected void init(Context context) {
        View.inflate(context, getLayoutId(), this);
        startButton = (ImageView) findViewById(R.id.start);
        fullscreenButton = (ImageView) findViewById(R.id.fullscreen);
        progressBar = (SeekBar) findViewById(R.id.progress);
        currentTimeTextView = (TextView) findViewById(R.id.current);
        totalTimeTextView = (TextView) findViewById(R.id.total);
        bottomContainer = (ViewGroup) findViewById(R.id.layout_bottom);
        textureViewContainer = (RelativeLayout) findViewById(R.id.surface_container);
        topContainer = (ViewGroup) findViewById(R.id.layout_top);
        if (isInEditMode())
            return;
        startButton.setOnClickListener(this);
        fullscreenButton.setOnClickListener(this);
        progressBar.setOnSeekBarChangeListener(this);
        bottomContainer.setOnClickListener(this);
        textureViewContainer.setOnClickListener(this);
        progressBar.setOnTouchListener(this);

        textureViewContainer.setOnTouchListener(this);
        fullscreenButton.setOnTouchListener(this);
        mScreenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);

        activity = (Activity) context;
    }


    protected void setVideoAllCallBack(VideoAllCallBack videoAllCallBack) {
        this.videoAllCallBack = videoAllCallBack;
    }

    public boolean setUp(String url, boolean cacheWithPlay, Object... objects) {
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

    public boolean setUp(String url, boolean cacheWithPlay, Map<String, String> mapHeadData, Object... objects) {
        if (setUp(url, cacheWithPlay, objects)) {
            this.mMapHeadData.clear();
            this.mMapHeadData.putAll(mapHeadData);
            return true;
        }
        return false;
    }

    //set ui
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
                progressBar.setProgress(100);
                currentTimeTextView.setText(totalTimeTextView.getText());
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
                if (videoAllCallBack != null && isCurrentMediaListener()) {
                    if (mIfCurrentIsFullscreen) {
                        videoAllCallBack.onClickStopFullscreen(mUrl, mObjects);
                    } else {
                        videoAllCallBack.onClickStop(mUrl, mObjects);
                    }
                }
            } else if (mCurrentState == CURRENT_STATE_PAUSE) {
                if (videoAllCallBack != null && isCurrentMediaListener()) {
                    if (mIfCurrentIsFullscreen) {
                        videoAllCallBack.onClickResumeFullscreen(mUrl, mObjects);
                    } else {
                        videoAllCallBack.onClickResume(mUrl, mObjects);
                    }
                }
                GSYVideoManager.instance().getMediaPlayer().start();
                setStateAndUi(CURRENT_STATE_PLAYING);
            } else if (mCurrentState == CURRENT_STATE_AUTO_COMPLETE) {
                startButtonLogic();
            }
        } else if (i == R.id.surface_container && mCurrentState == CURRENT_STATE_ERROR) {
            if (videoAllCallBack != null) {
                videoAllCallBack.onClickStartError(mUrl, mObjects);
            }
            prepareVideo();
        }
    }

    public void showWifiDialog() {

    }

    private void startButtonLogic() {
        if (videoAllCallBack != null && mCurrentState == CURRENT_STATE_NORMAL) {
            videoAllCallBack.onClickStartIcon(mUrl, mObjects);
        } else if (videoAllCallBack != null) {
            videoAllCallBack.onClickStartError(mUrl, mObjects);
        }
        prepareVideo();
    }

    protected void prepareVideo() {
        if (GSYVideoManager.instance().listener() != null) {
            GSYVideoManager.instance().listener().onCompletion();
        }
        GSYVideoManager.instance().setListener(this);
        addTextureView();
        AudioManager mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

        ((Activity) getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        GSYVideoManager.instance().prepare(mUrl, mMapHeadData, mLooping);
        setStateAndUi(CURRENT_STATE_PREPAREING);
    }

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


    public void onVideoReset() {
        setStateAndUi(CURRENT_STATE_NORMAL);
    }

    public void onVideoPause() {
        if (GSYVideoManager.instance().getMediaPlayer().isPlaying()) {
            setStateAndUi(CURRENT_STATE_PAUSE);
            pauseTime = System.currentTimeMillis();
            currentPosition = GSYVideoManager.instance().getMediaPlayer().getCurrentPosition();
            if (GSYVideoManager.instance().getMediaPlayer() != null)
                GSYVideoManager.instance().getMediaPlayer().pause();
        }
    }

    public void onVideoResume() {
        pauseTime = 0;
        if (mCurrentState == CURRENT_STATE_PAUSE) {
            if (currentPosition > 0 && GSYVideoManager.instance().getMediaPlayer() != null) {
                setStateAndUi(CURRENT_STATE_PLAYING);
                GSYVideoManager.instance().getMediaPlayer().seekTo(currentPosition);
                GSYVideoManager.instance().getMediaPlayer().start();
            }
        }
    }


    protected void addTextureView() {
        if (textureViewContainer.getChildCount() > 0) {
            textureViewContainer.removeAllViews();
        }
        textureView = null;
        textureView = new GSYTextureView(getContext());
        textureView.setSurfaceTextureListener(this);
        textureView.setRotation(rotate);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        textureViewContainer.addView(textureView, layoutParams);
    }


    public void setRotationView(int rotate) {
        this.rotate = rotate;
        textureView.setRotation(rotate);
    }

    public void refreshVideo() {
        if (textureView != null) {
            textureView.requestLayout();
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
        surface.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

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
                    firstTouch = true;

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
                                    if (videoAllCallBack != null && isCurrentMediaListener()) {
                                        videoAllCallBack.onTouchScreenSeekPosition(mUrl, mObjects);
                                    }
                                } else {
                                    if (firstTouch) {
                                        mBrightness = mDownX < mScreenWidth * 0.5f;
                                        firstTouch = false;
                                    }
                                    if (!mBrightness) {
                                        mChangeVolume = true;
                                        mGestureDownVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                        if (videoAllCallBack != null && isCurrentMediaListener()) {
                                            videoAllCallBack.onTouchScreenSeekVolume(mUrl, mObjects);
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
                        progressBar.setProgress(progress);
                    }
                    startProgressTimer();
                    if (videoAllCallBack != null && isCurrentMediaListener()) {
                        if (mIfCurrentIsFullscreen) {
                            videoAllCallBack.onClickSeekbarFullscreen(mUrl, mObjects);
                        } else {
                            videoAllCallBack.onClickSeekbar(mUrl, mObjects);
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
                    brightness = -1f;
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
        if (seekToInAdvance != -1) {
            GSYVideoManager.instance().getMediaPlayer().seekTo(seekToInAdvance);
            seekToInAdvance = -1;
        }
        startProgressTimer();
        setStateAndUi(CURRENT_STATE_PLAYING);
    }

    @Override
    public void onAutoCompletion() {
        if (videoAllCallBack != null && isCurrentMediaListener()) {
            if (mIfCurrentIsFullscreen) {
                videoAllCallBack.onAutoCompleteFullscreen(mUrl, mObjects);
            } else {
                videoAllCallBack.onAutoComplete(mUrl, mObjects);
            }
        }
        setStateAndUi(CURRENT_STATE_AUTO_COMPLETE);
        if (textureViewContainer.getChildCount() > 0) {
            textureViewContainer.removeAllViews();
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
        if (textureViewContainer.getChildCount() > 0) {
            textureViewContainer.removeAllViews();
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
            rotate = extra;
            if (textureView != null)
                textureView.setRotation(rotate);
        }
    }

    @Override
    public void onVideoSizeChanged() {
        int mVideoWidth = GSYVideoManager.instance().getCurrentVideoWidth();
        int mVideoHeight = GSYVideoManager.instance().getCurrentVideoHeight();
        if (mVideoWidth != 0 && mVideoHeight != 0) {
            textureView.requestLayout();
        }
    }

    @Override
    public void onBackFullscreen() {
        mCurrentState = GSYVideoManager.instance().getLastState();
        setStateAndUi(mCurrentState);
        addTextureView();
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
            if (progress != 0) progressBar.setProgress(progress);
        }
        if (secProgress > 95) secProgress = 100;
        if (secProgress != 0) progressBar.setSecondaryProgress(secProgress);
        currentTimeTextView.setText(CommonUtil.stringForTime(currentTime));
        totalTimeTextView.setText(CommonUtil.stringForTime(totalTime));
    }

    protected void resetProgressAndTime() {
        progressBar.setProgress(0);
        progressBar.setSecondaryProgress(0);
        currentTimeTextView.setText(CommonUtil.stringForTime(0));
        totalTimeTextView.setText(CommonUtil.stringForTime(0));
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
     * 滑动改变亮度
     *
     * @param percent
     */
    private void onBrightnessSlide(float percent) {
        if (brightness < 0) {
            brightness = activity.getWindow().getAttributes().screenBrightness;
            if (brightness <= 0.00f) {
                brightness = 0.50f;
            } else if (brightness < 0.01f) {
                brightness = 0.01f;
            }
        }
        WindowManager.LayoutParams lpa = activity.getWindow().getAttributes();
        lpa.screenBrightness = brightness + percent;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }
        showBrightnessDialog(lpa.screenBrightness);
        activity.getWindow().setAttributes(lpa);
    }

    public boolean isTouchWiget() {
        return mIsTouchWiget;
    }

    public void setIsTouchWiget(boolean isTouchWiget) {
        this.mIsTouchWiget = isTouchWiget;
    }

    public ImageView getStartButton() {
        return startButton;
    }

    public ImageView getFullscreenButton() {
        return fullscreenButton;
    }
}