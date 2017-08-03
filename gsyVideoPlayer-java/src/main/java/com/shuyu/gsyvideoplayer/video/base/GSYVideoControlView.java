package com.shuyu.gsyvideoplayer.video.base;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.AudioManager;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.R;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.shuyu.gsyvideoplayer.utils.Debuger;

import java.util.Timer;
import java.util.TimerTask;


import static com.shuyu.gsyvideoplayer.utils.CommonUtil.hideNavKey;

/**
 * ui显示和控制层
 * Created by guoshuyu on 2017/8/2.
 */

public abstract class GSYVideoControlView extends GSYVideoView implements View.OnClickListener, View.OnTouchListener, SeekBar.OnSeekBarChangeListener {

    //进度定时器
    protected Timer updateProcessTimer;

    //定时器任务
    protected ProgressTimerTask mProgressTimerTask;

    //触摸的X
    protected float mDownX;

    //触摸的Y
    protected float mDownY;

    //移动的Y
    protected float mMoveY;

    //亮度
    protected float mBrightnessData = -1;

    //触摸滑动进度的比例系数
    protected float mSeekRatio = 1;

    //手指放下的位置
    protected int mDownPosition;

    //手势调节音量的大小
    protected int mGestureDownVolume;

    //手势偏差值
    protected int mThreshold = 80;

    //手动改变滑动的位置
    protected int mSeekTimePosition;

    //手动滑动的起始偏移位置
    protected int mSeekEndOffset;

    //退出全屏显示的案件图片
    protected int mShrinkImageRes = -1;

    //全屏显示的案件图片
    protected int mEnlargeImageRes = -1;

    //触摸的是否进度条
    protected boolean mTouchingProgressBar = false;

    //是否改变音量
    protected boolean mChangeVolume = false;

    //是否改变播放进度
    protected boolean mChangePosition = false;

    //触摸显示虚拟按键
    protected boolean mShowVKey = false;

    //是否改变亮度
    protected boolean mBrightness = false;

    //是否首次触摸
    protected boolean mFirstTouch = false;

    //是否隐藏虚拟按键
    protected boolean mHideKey = true;

    //是否需要显示流量提示
    protected boolean mNeedShowWifiTip = true;

    //是否支持非全屏滑动触摸有效
    protected boolean mIsTouchWiget = true;

    //是否支持全屏滑动触摸有效
    protected boolean mIsTouchWigetFull = true;

    //播放按键
    protected View mStartButton;

    //进度条
    protected SeekBar mProgressBar;

    //全屏按键
    protected ImageView mFullscreenButton;

    //时间显示
    protected TextView mCurrentTimeTextView, mTotalTimeTextView;

    //顶部和底部区域
    protected ViewGroup mTopContainer, mBottomContainer;

    //返回按键
    protected ImageView mBackButton;

    public GSYVideoControlView(@NonNull Context context) {
        super(context);
    }

    public GSYVideoControlView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GSYVideoControlView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public GSYVideoControlView(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    protected void init(Context context) {
        super.init(context);

        mStartButton = findViewById(R.id.start);
        mBackButton = (ImageView) findViewById(R.id.back);
        mFullscreenButton = (ImageView) findViewById(R.id.fullscreen);
        mProgressBar = (SeekBar) findViewById(R.id.progress);
        mCurrentTimeTextView = (TextView) findViewById(R.id.current);
        mTotalTimeTextView = (TextView) findViewById(R.id.total);
        mBottomContainer = (ViewGroup) findViewById(R.id.layout_bottom);
        mTopContainer = (ViewGroup) findViewById(R.id.layout_top);

        if (isInEditMode())
            return;

        if (mStartButton != null) {
            mStartButton.setOnClickListener(this);
        }

        if (mFullscreenButton != null) {
            mFullscreenButton.setOnClickListener(this);
            mFullscreenButton.setOnTouchListener(this);
        }

        if (mProgressBar != null) {
            mProgressBar.setOnSeekBarChangeListener(this);
        }

        if(mBottomContainer != null) {
            mBottomContainer.setOnClickListener(this);
        }

        if(mTextureViewContainer != null) {
            mTextureViewContainer.setOnClickListener(this);
            mTextureViewContainer.setOnTouchListener(this);
        }

        if(mProgressBar != null) {
            mProgressBar.setOnTouchListener(this);
        }

        mSeekEndOffset = CommonUtil.dip2px(getActivityContext(), 50);
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
                    releasePauseCover();
                    mBuffterPoint = 0;
                }
                if (mAudioManager != null) {
                    mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
                }
                releaseNetWorkState();
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
        if (mHideKey && mIfCurrentIsFullscreen) {
            hideNavKey(mContext);
        }
        if (i == R.id.start) {
            if (TextUtils.isEmpty(mUrl)) {
                Toast.makeText(getActivityContext(), getResources().getString(R.string.no_url), Toast.LENGTH_SHORT).show();
                return;
            }
            if (mCurrentState == CURRENT_STATE_NORMAL || mCurrentState == CURRENT_STATE_ERROR) {
                if (!mUrl.startsWith("file") && !CommonUtil.isWifiConnected(getContext())
                        && mNeedShowWifiTip) {
                    showWifiDialog();
                    return;
                }
                startButtonLogic();
            } else if (mCurrentState == CURRENT_STATE_PLAYING) {
                GSYVideoManager.instance().getMediaPlayer().pause();
                setStateAndUi(CURRENT_STATE_PAUSE);
                if (mVideoAllCallBack != null && isCurrentMediaListener()) {
                    if (mIfCurrentIsFullscreen) {
                        Debuger.printfLog("onClickStopFullscreen");
                        mVideoAllCallBack.onClickStopFullscreen(mOriginUrl, mTitle, this);
                    } else {
                        Debuger.printfLog("onClickStop");
                        mVideoAllCallBack.onClickStop(mOriginUrl, mTitle, this);
                    }
                }
            } else if (mCurrentState == CURRENT_STATE_PAUSE) {
                if (mVideoAllCallBack != null && isCurrentMediaListener()) {
                    if (mIfCurrentIsFullscreen) {
                        Debuger.printfLog("onClickResumeFullscreen");
                        mVideoAllCallBack.onClickResumeFullscreen(mOriginUrl, mTitle, this);
                    } else {
                        Debuger.printfLog("onClickResume");
                        mVideoAllCallBack.onClickResume(mOriginUrl, mTitle, this);
                    }
                }
                GSYVideoManager.instance().getMediaPlayer().start();
                setStateAndUi(CURRENT_STATE_PLAYING);
            } else if (mCurrentState == CURRENT_STATE_AUTO_COMPLETE) {
                startButtonLogic();
            }
        } else if (i == R.id.surface_container && mCurrentState == CURRENT_STATE_ERROR) {
            if (mVideoAllCallBack != null) {
                Debuger.printfLog("onClickStartError");
                mVideoAllCallBack.onClickStartError(mOriginUrl, mTitle, this);
            }
            prepareVideo();
        }
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
                    mMoveY = 0;
                    mChangeVolume = false;
                    mChangePosition = false;
                    mShowVKey = false;
                    mBrightness = false;
                    mFirstTouch = true;

                    break;
                case MotionEvent.ACTION_MOVE:
                    float deltaX = x - mDownX;
                    float deltaY = y - mDownY;
                    float absDeltaX = Math.abs(deltaX);
                    float absDeltaY = Math.abs(deltaY);

                    if ((mIfCurrentIsFullscreen && mIsTouchWigetFull)
                            || (mIsTouchWiget && !mIfCurrentIsFullscreen)) {
                        if (!mChangePosition && !mChangeVolume && !mBrightness) {
                            if (absDeltaX > mThreshold || absDeltaY > mThreshold) {
                                cancelProgressTimer();
                                if (absDeltaX >= mThreshold) {
                                    //防止全屏虚拟按键
                                    int screenWidth = CommonUtil.getScreenWidth(getContext());
                                    if (Math.abs(screenWidth - mDownX) > mSeekEndOffset) {
                                        mChangePosition = true;
                                        mDownPosition = getCurrentPositionWhenPlaying();
                                    } else {
                                        mShowVKey = true;
                                    }
                                } else {
                                    int screenHeight = CommonUtil.getScreenHeight(getContext());
                                    boolean noEnd = Math.abs(screenHeight - mDownY) > mSeekEndOffset;
                                    if (mFirstTouch) {
                                        mBrightness = (mDownX < mScreenWidth * 0.5f) && noEnd;
                                        mFirstTouch = false;
                                    }
                                    if (!mBrightness) {
                                        mChangeVolume = noEnd;
                                        mGestureDownVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                    }
                                    mShowVKey = !noEnd;
                                }
                            }
                        }
                    }
                    if (mChangePosition) {
                        int totalTimeDuration = getDuration();
                        mSeekTimePosition = (int) (mDownPosition + (deltaX * totalTimeDuration / mScreenWidth) / mSeekRatio);
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
                        if (Math.abs(deltaY) > mThreshold) {
                            float percent = (-deltaY / mScreenHeight);
                            onBrightnessSlide(percent);
                            mDownY = y;
                        }
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    mTouchingProgressBar = false;
                    dismissProgressDialog();
                    dismissVolumeDialog();
                    dismissBrightnessDialog();
                    if (mChangePosition && GSYVideoManager.instance().getMediaPlayer() != null && (mCurrentState == CURRENT_STATE_PLAYING || mCurrentState == CURRENT_STATE_PAUSE)) {
                        GSYVideoManager.instance().getMediaPlayer().seekTo(mSeekTimePosition);
                        int duration = getDuration();
                        int progress = mSeekTimePosition * 100 / (duration == 0 ? 1 : duration);
                        mProgressBar.setProgress(progress);
                        if (mVideoAllCallBack != null && isCurrentMediaListener()) {
                            Debuger.printfLog("onTouchScreenSeekPosition");
                            mVideoAllCallBack.onTouchScreenSeekPosition(mOriginUrl, mTitle, this);
                        }
                    } else if (mBrightness) {
                        if (mVideoAllCallBack != null && isCurrentMediaListener()) {
                            Debuger.printfLog("onTouchScreenSeekLight");
                            mVideoAllCallBack.onTouchScreenSeekLight(mOriginUrl, mTitle, this);
                        }
                    } else if (mChangeVolume) {
                        if (mVideoAllCallBack != null && isCurrentMediaListener()) {
                            Debuger.printfLog("onTouchScreenSeekVolume");
                            mVideoAllCallBack.onTouchScreenSeekVolume(mOriginUrl, mTitle, this);
                        }
                    }
                    startProgressTimer();
                    //不要和隐藏虚拟按键后，滑出虚拟按键冲突
                    if (mHideKey && mShowVKey) {
                        return true;
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

    /**
     * 显示暂停切换显示的bitmap
     */
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

    /**
     * 销毁暂停切换显示的bitmap
     */
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
    

    protected void startProgressTimer() {
        cancelProgressTimer();
        updateProcessTimer = new Timer();
        mProgressTimerTask = new ProgressTimerTask();
        updateProcessTimer.schedule(mProgressTimerTask, 0, 300);
    }

    protected void cancelProgressTimer() {
        if (updateProcessTimer != null) {
            updateProcessTimer.cancel();
        }
        if (mProgressTimerTask != null) {
            mProgressTimerTask.cancel();
        }

    }

    private class ProgressTimerTask extends TimerTask {
        @Override
        public void run() {
            if (mCurrentState == CURRENT_STATE_PLAYING || mCurrentState == CURRENT_STATE_PAUSE) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        setTextAndProgress(0);
                    }
                });
            }
        }
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
        if (secProgress > 94) secProgress = 100;
        if (secProgress != 0 && !mCacheFile) {
            mProgressBar.setSecondaryProgress(secProgress);
        }
        mTotalTimeTextView.setText(CommonUtil.stringForTime(totalTime));
        if (currentTime > 0)
            mCurrentTimeTextView.setText(CommonUtil.stringForTime(currentTime));
    }


    protected void resetProgressAndTime() {
        mProgressBar.setProgress(0);
        mProgressBar.setSecondaryProgress(0);
        mCurrentTimeTextView.setText(CommonUtil.stringForTime(0));
        mTotalTimeTextView.setText(CommonUtil.stringForTime(0));
    }


    protected void loopSetProgressAndTime() {
        mProgressBar.setProgress(0);
        mProgressBar.setSecondaryProgress(0);
        mCurrentTimeTextView.setText(CommonUtil.stringForTime(0));
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
        if (mVideoAllCallBack != null && isCurrentMediaListener()) {
            if (isIfCurrentIsFullscreen()) {
                Debuger.printfLog("onClickSeekbarFullscreen");
                mVideoAllCallBack.onClickSeekbarFullscreen(mOriginUrl, mTitle, this);
            } else {
                Debuger.printfLog("onClickSeekbar");
                mVideoAllCallBack.onClickSeekbar(mOriginUrl, mTitle, this);
            }
        }
        if (GSYVideoManager.instance().getMediaPlayer() != null && mHadPlay) {
            try {
                int time = seekBar.getProgress() * getDuration() / 100;
                GSYVideoManager.instance().getMediaPlayer().seekTo(time);
            } catch (Exception e) {
                Debuger.printfWarning(e.toString());
            }
        }
    }

    @Override
    public void onPrepared() {
        super.onPrepared();
        if (mCurrentState != CURRENT_STATE_PREPAREING) return;
        startProgressTimer();
    }
    
    
    @Override
    public void onBufferingUpdate(int percent) {
        if (mCurrentState != CURRENT_STATE_NORMAL && mCurrentState != CURRENT_STATE_PREPAREING) {
            if (percent != 0) {
                setTextAndProgress(percent);
                mBuffterPoint = percent;
                Debuger.printfLog("Net speed: " + getNetSpeedText() + " percent " + percent);
            }
            //循环清除进度
            if (mLooping && mHadPlay && percent == 0 && mProgressBar.getProgress() >= (mProgressBar.getMax() - 1)) {
                loopSetProgressAndTime();
            }
        }
    }

    /**
     * 滑动改变亮度
     *
     * @param percent
     */
    private void onBrightnessSlide(float percent) {
        mBrightnessData = ((Activity) (mContext)).getWindow().getAttributes().screenBrightness;
        if (mBrightnessData <= 0.00f) {
            mBrightnessData = 0.50f;
        } else if (mBrightnessData < 0.01f) {
            mBrightnessData = 0.01f;
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

    
    protected abstract void showWifiDialog();
    
    protected abstract void showProgressDialog(float deltaX,
                                               String seekTime, int seekTimePosition,
                                               String totalTime, int totalTimeDuration);
    protected abstract void dismissProgressDialog();

    protected abstract void showVolumeDialog(float deltaY, int volumePercent);

    protected abstract void dismissVolumeDialog();

    protected abstract void showBrightnessDialog(float percent);

    protected abstract void dismissBrightnessDialog();
    
    
    /**
     * 获取播放按键
     */
    public View getStartButton() {
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


    public int getEnlargeImageRes() {
        if (mShrinkImageRes == -1) {
            return R.drawable.video_enlarge;
        }
        return mEnlargeImageRes;
    }

    /**
     * 设置右下角 显示切换到全屏 的按键资源
     * 必须在setUp之前设置
     * 不设置使用默认
     */
    public void setEnlargeImageRes(int mEnlargeImageRes) {
        this.mEnlargeImageRes = mEnlargeImageRes;
    }

    public int getShrinkImageRes() {
        if (mShrinkImageRes == -1) {
            return R.drawable.video_shrink;
        }
        return mShrinkImageRes;
    }

    /**
     * 设置右下角 显示退出全屏 的按键资源
     * 必须在setUp之前设置
     * 不设置使用默认
     */
    public void setShrinkImageRes(int mShrinkImageRes) {
        this.mShrinkImageRes = mShrinkImageRes;
    }
    /**
     * 是否可以全屏滑动界面改变进度，声音等
     * 默认 true
     */
    public void setIsTouchWigetFull(boolean isTouchWigetFull) {
        this.mIsTouchWigetFull = isTouchWigetFull;
    }


    public boolean isIfCurrentIsFullscreen() {
        return mIfCurrentIsFullscreen;
    }

    public void setIfCurrentIsFullscreen(boolean ifCurrentIsFullscreen) {
        this.mIfCurrentIsFullscreen = ifCurrentIsFullscreen;
    }

    public boolean isHideKey() {
        return mHideKey;
    }

    /**
     * 全屏隐藏虚拟按键，默认打开
     */
    public void setHideKey(boolean hideKey) {
        this.mHideKey = hideKey;
    }

    public boolean isNeedShowWifiTip() {
        return mNeedShowWifiTip;
    }


    public boolean isTouchWiget() {
        return mIsTouchWiget;
    }

    /**
     * 是否可以滑动界面改变进度，声音等
     * 默认true
     */
    public void setIsTouchWiget(boolean isTouchWiget) {
        this.mIsTouchWiget = isTouchWiget;
    }

    public boolean isTouchWigetFull() {
        return mIsTouchWigetFull;
    }

    /**
     * 是否需要显示流量提示,默认true
     */
    public void setNeedShowWifiTip(boolean needShowWifiTip) {
        this.mNeedShowWifiTip = needShowWifiTip;
    }

    /**
     * 调整触摸滑动快进的比例
     *
     * @param seekRatio 滑动快进的比例，默认1。数值越大，滑动的产生的seek越小
     */
    public void setSeekRatio(float seekRatio) {
        if (seekRatio < 0) {
            return;
        }
        this.mSeekRatio = seekRatio;
    }

    public float getSeekRatio() {
        return mSeekRatio;
    }

}
