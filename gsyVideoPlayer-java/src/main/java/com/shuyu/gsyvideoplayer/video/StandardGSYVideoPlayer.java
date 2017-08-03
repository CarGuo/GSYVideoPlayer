package com.shuyu.gsyvideoplayer.video;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.shuyu.gsyvideoplayer.R;
import com.shuyu.gsyvideoplayer.listener.StandardVideoAllCallBack;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.utils.NetworkUtils;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import moe.codeest.enviews.ENDownloadView;
import moe.codeest.enviews.ENPlayView;

import static com.shuyu.gsyvideoplayer.utils.CommonUtil.hideNavKey;


/**
 * 标准播放器
 * Created by shuyu on 2016/11/11.
 */

public class StandardGSYVideoPlayer extends GSYVideoPlayer {

    protected Timer mDismissControlViewTimer;


    protected StandardVideoAllCallBack mStandardVideoAllCallBack;//标准播放器的回调

    protected DismissControlViewTimerTask mDismissControlViewTimerTask;

    protected Dialog mBrightnessDialog;

    protected Dialog mVolumeDialog;

    protected Dialog mProgressDialog;

    protected ProgressBar mDialogProgressBar;

    protected ProgressBar mDialogVolumeProgressBar;

    protected TextView mBrightnessDialogTv;

    protected TextView mDialogSeekTime;

    protected TextView mDialogTotalTime;

    protected ImageView mDialogIcon;

    protected Drawable mBottomProgressDrawable;

    protected Drawable mBottomShowProgressDrawable;

    protected Drawable mBottomShowProgressThumbDrawable;

    protected Drawable mVolumeProgressDrawable;

    protected Drawable mDialogProgressBarDrawable;

    protected int mDialogProgressHighLightColor = -11;

    protected int mDialogProgressNormalColor = -11;

    protected int mDismissControlTime = 2500;


    /**
     * 1.5.0开始加入，如果需要不同布局区分功能，需要重载
     */
    public StandardGSYVideoPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public StandardGSYVideoPlayer(Context context) {
        super(context);
    }

    public StandardGSYVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);


        if (mBottomProgressDrawable != null) {
            mBottomProgressBar.setProgressDrawable(mBottomProgressDrawable);
        }

        if (mBottomShowProgressDrawable != null) {
            mProgressBar.setProgressDrawable(mBottomProgressDrawable);
        }

        if (mBottomShowProgressThumbDrawable != null) {
            mProgressBar.setThumb(mBottomShowProgressThumbDrawable);
        }

    }

    /**
     * 设置播放URL
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param title         title
     * @return
     */
    @Override
    public boolean setUp(String url, boolean cacheWithPlay, String title) {
        return setUp(url, cacheWithPlay, (File) null, title);
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
    @Override
    public boolean setUp(String url, boolean cacheWithPlay, File cachePath, String title) {
        if (super.setUp(url, cacheWithPlay, cachePath, title)) {
            if (title != null && mTitleTextView != null) {
                mTitleTextView.setText(title);
            }
            if (mIfCurrentIsFullscreen) {
                mFullscreenButton.setImageResource(getShrinkImageRes());
            } else {
                mFullscreenButton.setImageResource(getEnlargeImageRes());
                mBackButton.setVisibility(View.GONE);
            }
            return true;
        }
        return false;
    }

    @Override
    public int getLayoutId() {
        return R.layout.video_layout_standard;
    }

    @Override
    protected void setStateAndUi(int state) {
        super.setStateAndUi(state);
        switch (mCurrentState) {
            case CURRENT_STATE_NORMAL:
                changeUiToNormal();
                cancelDismissControlViewTimer();
                break;
            case CURRENT_STATE_PREPAREING:
                changeUiToPrepareingShow();
                startDismissControlViewTimer();
                break;
            case CURRENT_STATE_PLAYING:
                changeUiToPlayingShow();
                startDismissControlViewTimer();
                break;
            case CURRENT_STATE_PAUSE:
                changeUiToPauseShow();
                cancelDismissControlViewTimer();
                break;
            case CURRENT_STATE_ERROR:
                changeUiToError();
                break;
            case CURRENT_STATE_AUTO_COMPLETE:
                changeUiToCompleteShow();
                cancelDismissControlViewTimer();
                break;
            case CURRENT_STATE_PLAYING_BUFFERING_START:
                changeUiToPlayingBufferingShow();
                break;
        }
    }



    @Override
    protected void showWifiDialog() {
        if (!NetworkUtils.isAvailable(mContext)) {
            Toast.makeText(mContext, getResources().getString(R.string.no_net), Toast.LENGTH_LONG).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivityContext());
        builder.setMessage(getResources().getString(R.string.tips_not_wifi));
        builder.setPositiveButton(getResources().getString(R.string.tips_not_wifi_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startPlayLogic();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.tips_not_wifi_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }


    @Override
    public void startPlayLogic() {
        if (mStandardVideoAllCallBack != null) {
            Debuger.printfLog("onClickStartThumb");
            mStandardVideoAllCallBack.onClickStartThumb(mOriginUrl, mTitle, StandardGSYVideoPlayer.this);
        }
        prepareVideo();
        startDismissControlViewTimer();
    }

    @Override
    protected void onClickUiToggle() {
        if (mIfCurrentIsFullscreen && mLockCurScreen && mNeedLockFull) {
            mLockScreen.setVisibility(VISIBLE);
            return;
        }
        if (mCurrentState == CURRENT_STATE_PREPAREING) {
            if (mBottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToPrepareingClear();
            } else {
                changeUiToPrepareingShow();
            }
        } else if (mCurrentState == CURRENT_STATE_PLAYING) {
            if (mBottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToPlayingClear();
            } else {
                changeUiToPlayingShow();
            }
        } else if (mCurrentState == CURRENT_STATE_PAUSE) {
            if (mBottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToPauseClear();
            } else {
                changeUiToPauseShow();
            }
        } else if (mCurrentState == CURRENT_STATE_AUTO_COMPLETE) {
            if (mBottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToCompleteClear();
            } else {
                changeUiToCompleteShow();
            }
        } else if (mCurrentState == CURRENT_STATE_PLAYING_BUFFERING_START) {
            if (mBottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToPlayingBufferingClear();
            } else {
                changeUiToPlayingBufferingShow();
            }
        }
    }
    @Override
    @SuppressWarnings("ResourceType")
    protected void showProgressDialog(float deltaX, String seekTime, int seekTimePosition, String totalTime, int totalTimeDuration) {
        if (mProgressDialog == null) {
            View localView = LayoutInflater.from(getActivityContext()).inflate(R.layout.video_progress_dialog, null);
            mDialogProgressBar = ((ProgressBar) localView.findViewById(R.id.duration_progressbar));
            if (mDialogProgressBarDrawable != null) {
                mDialogProgressBar.setProgressDrawable(mDialogProgressBarDrawable);
            }
            mDialogSeekTime = ((TextView) localView.findViewById(R.id.tv_current));
            mDialogTotalTime = ((TextView) localView.findViewById(R.id.tv_duration));
            mDialogIcon = ((ImageView) localView.findViewById(R.id.duration_image_tip));
            mProgressDialog = new Dialog(getActivityContext(), R.style.video_style_dialog_progress);
            mProgressDialog.setContentView(localView);
            mProgressDialog.getWindow().addFlags(Window.FEATURE_ACTION_BAR);
            mProgressDialog.getWindow().addFlags(32);
            mProgressDialog.getWindow().addFlags(16);
            mProgressDialog.getWindow().setLayout(getWidth(), getHeight());
            if (mDialogProgressNormalColor != -11) {
                mDialogTotalTime.setTextColor(mDialogProgressNormalColor);
            }
            if (mDialogProgressHighLightColor != -11) {
                mDialogSeekTime.setTextColor(mDialogProgressHighLightColor);
            }
            WindowManager.LayoutParams localLayoutParams = mProgressDialog.getWindow().getAttributes();
            localLayoutParams.gravity = Gravity.TOP;
            localLayoutParams.width = getWidth();
            localLayoutParams.height = getHeight();
            int location[] = new int[2];
            getLocationOnScreen(location);
            localLayoutParams.x = location[0];
            localLayoutParams.y = location[1];
            mProgressDialog.getWindow().setAttributes(localLayoutParams);
        }
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }

        mDialogSeekTime.setText(seekTime);
        mDialogTotalTime.setText(" / " + totalTime);
        if (totalTimeDuration > 0)
            mDialogProgressBar.setProgress(seekTimePosition * 100 / totalTimeDuration);
        if (deltaX > 0) {
            mDialogIcon.setBackgroundResource(R.drawable.video_forward_icon);
        } else {
            mDialogIcon.setBackgroundResource(R.drawable.video_backward_icon);
        }

    }

    @Override
    protected void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    protected void showVolumeDialog(float deltaY, int volumePercent) {
        if (mVolumeDialog == null) {
            View localView = LayoutInflater.from(getActivityContext()).inflate(R.layout.video_volume_dialog, null);
            mDialogVolumeProgressBar = ((ProgressBar) localView.findViewById(R.id.volume_progressbar));
            if (mVolumeProgressDrawable != null) {
                mDialogVolumeProgressBar.setProgressDrawable(mVolumeProgressDrawable);
            }
            mVolumeDialog = new Dialog(getActivityContext(), R.style.video_style_dialog_progress);
            mVolumeDialog.setContentView(localView);
            mVolumeDialog.getWindow().addFlags(8);
            mVolumeDialog.getWindow().addFlags(32);
            mVolumeDialog.getWindow().addFlags(16);
            mVolumeDialog.getWindow().setLayout(-2, -2);
            WindowManager.LayoutParams localLayoutParams = mVolumeDialog.getWindow().getAttributes();
            localLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            localLayoutParams.width = getWidth();
            localLayoutParams.height = getHeight();
            int location[] = new int[2];
            getLocationOnScreen(location);
            localLayoutParams.x = location[0];
            localLayoutParams.y = location[1];
            mVolumeDialog.getWindow().setAttributes(localLayoutParams);
        }
        if (!mVolumeDialog.isShowing()) {
            mVolumeDialog.show();
        }

        mDialogVolumeProgressBar.setProgress(volumePercent);
    }

    @Override
    protected void dismissVolumeDialog() {
        if (mVolumeDialog != null) {
            mVolumeDialog.dismiss();
            mVolumeDialog = null;
        }
    }

    @Override
    protected void showBrightnessDialog(float percent) {
        if (mBrightnessDialog == null) {
            View localView = LayoutInflater.from(getActivityContext()).inflate(R.layout.video_brightness, null);
            mBrightnessDialogTv = (TextView) localView.findViewById(R.id.app_video_brightness);
            mBrightnessDialog = new Dialog(getActivityContext(), R.style.video_style_dialog_progress);
            mBrightnessDialog.setContentView(localView);
            mBrightnessDialog.getWindow().addFlags(8);
            mBrightnessDialog.getWindow().addFlags(32);
            mBrightnessDialog.getWindow().addFlags(16);
            mBrightnessDialog.getWindow().setLayout(-2, -2);
            WindowManager.LayoutParams localLayoutParams = mBrightnessDialog.getWindow().getAttributes();
            localLayoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
            localLayoutParams.width = getWidth();
            localLayoutParams.height = getHeight();
            int location[] = new int[2];
            getLocationOnScreen(location);
            localLayoutParams.x = location[0];
            localLayoutParams.y = location[1];
            mBrightnessDialog.getWindow().setAttributes(localLayoutParams);
        }
        if (!mBrightnessDialog.isShowing()) {
            mBrightnessDialog.show();
        }
        if (mBrightnessDialogTv != null)
            mBrightnessDialogTv.setText((int) (percent * 100) + "%");
    }

    @Override
    protected void dismissBrightnessDialog() {
        if (mBrightnessDialog != null) {
            mBrightnessDialog.dismiss();
            mBrightnessDialog = null;
        }
    }


    @Override
    public GSYBaseVideoPlayer startWindowFullscreen(Context context, boolean actionBar, boolean statusBar) {
        GSYBaseVideoPlayer gsyBaseVideoPlayer = super.startWindowFullscreen(context, actionBar, statusBar);
        if (gsyBaseVideoPlayer != null) {
            StandardGSYVideoPlayer gsyVideoPlayer = (StandardGSYVideoPlayer) gsyBaseVideoPlayer;
            gsyVideoPlayer.setStandardVideoAllCallBack(mStandardVideoAllCallBack);
            gsyVideoPlayer.setLockClickListener(mLockClickListener);
            gsyVideoPlayer.setNeedLockFull(isNeedLockFull());
            initFullUI(gsyVideoPlayer);
            //比如你自定义了返回案件，但是因为返回按键底层已经设置了返回事件，所以你需要在这里重新增加的逻辑
        }
        return gsyBaseVideoPlayer;
    }


    @Override
    public GSYBaseVideoPlayer showSmallVideo(Point size, boolean actionBar, boolean statusBar) {
        GSYBaseVideoPlayer gsyBaseVideoPlayer = super.showSmallVideo(size, actionBar, statusBar);
        if (gsyBaseVideoPlayer != null) {
            StandardGSYVideoPlayer gsyVideoPlayer = (StandardGSYVideoPlayer) gsyBaseVideoPlayer;
            gsyVideoPlayer.setIsTouchWiget(false);//小窗口不能点击
            gsyVideoPlayer.setStandardVideoAllCallBack(mStandardVideoAllCallBack);
        }
        return gsyBaseVideoPlayer;
    }

    @Override
    protected void startDismissControlViewTimer() {
        cancelDismissControlViewTimer();
        mDismissControlViewTimer = new Timer();
        mDismissControlViewTimerTask = new DismissControlViewTimerTask();
        mDismissControlViewTimer.schedule(mDismissControlViewTimerTask, mDismissControlTime);
    }

    @Override
    protected void cancelDismissControlViewTimer() {
        if (mDismissControlViewTimer != null) {
            mDismissControlViewTimer.cancel();
            mDismissControlViewTimer = null;
        }
        if (mDismissControlViewTimerTask != null) {
            mDismissControlViewTimerTask.cancel();
            mDismissControlViewTimerTask = null;
        }

    }

    @Override
    protected void hideAllWidget() {
        mBottomContainer.setVisibility(View.INVISIBLE);
        mTopContainer.setVisibility(View.INVISIBLE);
        mBottomProgressBar.setVisibility(View.VISIBLE);
        mStartButton.setVisibility(View.INVISIBLE);
    }


    //Unified management Ui
    private void changeUiToNormal() {
        Debuger.printfLog("changeUiToNormal");
        mTopContainer.setVisibility(View.VISIBLE);
        mBottomContainer.setVisibility(View.INVISIBLE);
        mStartButton.setVisibility(View.VISIBLE);
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        if (mLoadingProgressBar instanceof ENDownloadView) {
            ((ENDownloadView) mLoadingProgressBar).reset();
        }
        mThumbImageViewLayout.setVisibility(View.VISIBLE);
        mBottomProgressBar.setVisibility(View.INVISIBLE);
        mLockScreen.setVisibility((mIfCurrentIsFullscreen && mNeedLockFull) ? VISIBLE : GONE);
        updateStartImage();
    }

    private void changeUiToPrepareingShow() {
        Debuger.printfLog("changeUiToPrepareingShow");
        mTopContainer.setVisibility(View.VISIBLE);
        mBottomContainer.setVisibility(View.VISIBLE);
        mStartButton.setVisibility(View.INVISIBLE);
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        if (mLoadingProgressBar instanceof ENDownloadView) {
            ENDownloadView enDownloadView = (ENDownloadView) mLoadingProgressBar;
            if (enDownloadView.getCurrentState() == ENDownloadView.STATE_PRE) {
                ((ENDownloadView) mLoadingProgressBar).start();
            }
        }
        mThumbImageViewLayout.setVisibility(View.INVISIBLE);
        mBottomProgressBar.setVisibility(View.INVISIBLE);
        mLockScreen.setVisibility(GONE);
    }

    private void changeUiToPrepareingClear() {
        Debuger.printfLog("changeUiToPrepareingClear");
        mTopContainer.setVisibility(View.INVISIBLE);
        mBottomContainer.setVisibility(View.INVISIBLE);
        mStartButton.setVisibility(View.INVISIBLE);
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        if (mLoadingProgressBar instanceof ENDownloadView) {
            ((ENDownloadView) mLoadingProgressBar).reset();
        }
        mThumbImageViewLayout.setVisibility(View.INVISIBLE);
        mBottomProgressBar.setVisibility(View.INVISIBLE);
        mLockScreen.setVisibility(GONE);
    }

    private void changeUiToPlayingShow() {
        Debuger.printfLog("changeUiToPlayingShow");
        mTopContainer.setVisibility(View.VISIBLE);
        mBottomContainer.setVisibility(View.VISIBLE);
        mStartButton.setVisibility(View.VISIBLE);
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        if (mLoadingProgressBar instanceof ENDownloadView) {
            ((ENDownloadView) mLoadingProgressBar).reset();
        }
        mThumbImageViewLayout.setVisibility(View.INVISIBLE);
        mBottomProgressBar.setVisibility(View.INVISIBLE);
        mLockScreen.setVisibility((mIfCurrentIsFullscreen && mNeedLockFull) ? VISIBLE : GONE);
        updateStartImage();
    }

    private void changeUiToPlayingClear() {
        Debuger.printfLog("changeUiToPlayingClear");
        changeUiToClear();
        mBottomProgressBar.setVisibility(View.VISIBLE);
    }

    private void changeUiToPauseShow() {
        Debuger.printfLog("changeUiToPauseShow");
        mTopContainer.setVisibility(View.VISIBLE);
        mBottomContainer.setVisibility(View.VISIBLE);
        mStartButton.setVisibility(View.VISIBLE);
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        if (mLoadingProgressBar instanceof ENDownloadView) {
            ((ENDownloadView) mLoadingProgressBar).reset();
        }
        mThumbImageViewLayout.setVisibility(View.INVISIBLE);
        //mCoverImageView.setVisibility(View.INVISIBLE);
        mBottomProgressBar.setVisibility(View.INVISIBLE);
        mLockScreen.setVisibility((mIfCurrentIsFullscreen && mNeedLockFull) ? VISIBLE : GONE);
        updateStartImage();
        updatePauseCover();
    }

    private void changeUiToPauseClear() {
        Debuger.printfLog("changeUiToPauseClear");
        changeUiToClear();
        mBottomProgressBar.setVisibility(View.VISIBLE);
        updatePauseCover();
    }

    private void changeUiToPlayingBufferingShow() {
        Debuger.printfLog("changeUiToPlayingBufferingShow");
        mTopContainer.setVisibility(View.VISIBLE);
        mBottomContainer.setVisibility(View.VISIBLE);
        mStartButton.setVisibility(View.INVISIBLE);
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        if (mLoadingProgressBar instanceof ENDownloadView) {
            ENDownloadView enDownloadView = (ENDownloadView) mLoadingProgressBar;
            if (enDownloadView.getCurrentState() == ENDownloadView.STATE_PRE) {
                ((ENDownloadView) mLoadingProgressBar).start();
            }
        }
        mThumbImageViewLayout.setVisibility(View.INVISIBLE);
        mBottomProgressBar.setVisibility(View.INVISIBLE);
        mLockScreen.setVisibility(GONE);
    }

    private void changeUiToPlayingBufferingClear() {
        Debuger.printfLog("changeUiToPlayingBufferingClear");
        mTopContainer.setVisibility(View.INVISIBLE);
        mBottomContainer.setVisibility(View.INVISIBLE);
        mStartButton.setVisibility(View.INVISIBLE);
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        if (mLoadingProgressBar instanceof ENDownloadView) {
            ENDownloadView enDownloadView = (ENDownloadView) mLoadingProgressBar;
            if (enDownloadView.getCurrentState() == ENDownloadView.STATE_PRE) {
                ((ENDownloadView) mLoadingProgressBar).start();
            }
        }
        mThumbImageViewLayout.setVisibility(View.INVISIBLE);
        mBottomProgressBar.setVisibility(View.VISIBLE);
        mLockScreen.setVisibility(GONE);
        updateStartImage();
    }

    private void changeUiToClear() {
        Debuger.printfLog("changeUiToClear");
        mTopContainer.setVisibility(View.INVISIBLE);
        mBottomContainer.setVisibility(View.INVISIBLE);
        mStartButton.setVisibility(View.INVISIBLE);
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        if (mLoadingProgressBar instanceof ENDownloadView) {
            ((ENDownloadView) mLoadingProgressBar).reset();
        }
        mThumbImageViewLayout.setVisibility(View.INVISIBLE);
        mBottomProgressBar.setVisibility(View.INVISIBLE);
        mLockScreen.setVisibility(GONE);
    }

    private void changeUiToCompleteShow() {
        Debuger.printfLog("changeUiToCompleteShow");
        mTopContainer.setVisibility(View.VISIBLE);
        mBottomContainer.setVisibility(View.VISIBLE);
        mStartButton.setVisibility(View.VISIBLE);
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        if (mLoadingProgressBar instanceof ENDownloadView) {
            ((ENDownloadView) mLoadingProgressBar).reset();
        }
        mThumbImageViewLayout.setVisibility(View.VISIBLE);
        mBottomProgressBar.setVisibility(View.INVISIBLE);
        mLockScreen.setVisibility((mIfCurrentIsFullscreen && mNeedLockFull) ? VISIBLE : GONE);
        updateStartImage();
    }

    private void changeUiToCompleteClear() {
        Debuger.printfLog("changeUiToCompleteClear");
        mTopContainer.setVisibility(View.INVISIBLE);
        mBottomContainer.setVisibility(View.INVISIBLE);
        mStartButton.setVisibility(View.VISIBLE);
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        if (mLoadingProgressBar instanceof ENDownloadView) {
            ((ENDownloadView) mLoadingProgressBar).reset();
        }
        mThumbImageViewLayout.setVisibility(View.VISIBLE);
        mBottomProgressBar.setVisibility(View.VISIBLE);
        mLockScreen.setVisibility((mIfCurrentIsFullscreen && mNeedLockFull) ? VISIBLE : GONE);
        updateStartImage();
    }

    private void changeUiToError() {
        Debuger.printfLog("changeUiToError");
        mTopContainer.setVisibility(View.INVISIBLE);
        mBottomContainer.setVisibility(View.INVISIBLE);
        mStartButton.setVisibility(View.VISIBLE);
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        if (mLoadingProgressBar instanceof ENDownloadView) {
            ((ENDownloadView) mLoadingProgressBar).reset();
        }
        mThumbImageViewLayout.setVisibility(View.INVISIBLE);
        mBottomProgressBar.setVisibility(View.INVISIBLE);
        mLockScreen.setVisibility((mIfCurrentIsFullscreen && mNeedLockFull) ? VISIBLE : GONE);
        updateStartImage();
    }

    protected void updateStartImage() {
        if (mStartButton instanceof ENPlayView) {
            ENPlayView enPlayView = (ENPlayView) mStartButton;
            enPlayView.setDuration(500);
            if (mCurrentState == CURRENT_STATE_PLAYING) {
                enPlayView.play();
                //mStartButton.setImageResource(R.drawable.video_click_pause_selector);
            } else if (mCurrentState == CURRENT_STATE_ERROR) {
                enPlayView.pause();
                //mStartButton.setImageResource(R.drawable.video_click_error_selector);
            } else {
                enPlayView.pause();
                //mStartButton.setImageResource(R.drawable.video_click_play_selector);
            }
        }
    }


    private void updatePauseCover() {
        if ((mFullPauseBitmap == null || mFullPauseBitmap.isRecycled()) && mShowPauseCover) {
            try {
                initCover();
            } catch (Exception e) {
                e.printStackTrace();
                mFullPauseBitmap = null;
            }
        }
    }

    /**
     * 全屏的UI逻辑
     */
    private void initFullUI(StandardGSYVideoPlayer standardGSYVideoPlayer) {

        if (mBottomProgressDrawable != null) {
            standardGSYVideoPlayer.setBottomProgressBarDrawable(mBottomProgressDrawable);
        }

        if (mBottomShowProgressDrawable != null && mBottomShowProgressThumbDrawable != null) {
            standardGSYVideoPlayer.setBottomShowProgressBarDrawable(mBottomShowProgressDrawable,
                    mBottomShowProgressThumbDrawable);
        }

        if (mVolumeProgressDrawable != null) {
            standardGSYVideoPlayer.setDialogVolumeProgressBar(mVolumeProgressDrawable);
        }

        if (mDialogProgressBarDrawable != null) {
            standardGSYVideoPlayer.setDialogProgressBar(mDialogProgressBarDrawable);
        }

        if (mDialogProgressHighLightColor >= 0 && mDialogProgressNormalColor >= 0) {
            standardGSYVideoPlayer.setDialogProgressColor(mDialogProgressHighLightColor, mDialogProgressNormalColor);
        }
    }

    private class DismissControlViewTimerTask extends TimerTask {

        @Override
        public void run() {
            if (mCurrentState != CURRENT_STATE_NORMAL
                    && mCurrentState != CURRENT_STATE_ERROR
                    && mCurrentState != CURRENT_STATE_AUTO_COMPLETE) {
                if (getActivityContext() != null) {
                    ((Activity) getActivityContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideAllWidget();
                            mLockScreen.setVisibility(GONE);
                            if (mHideKey && mIfCurrentIsFullscreen && mShowVKey) {
                                hideNavKey(mContext);
                            }
                        }
                    });
                }
            }
        }
    }

    /**
     * 初始化为正常状态
     */
    public void initUIState() {
        setStateAndUi(CURRENT_STATE_NORMAL);
    }

    /**
     * 底部进度条-弹出的
     */
    public void setBottomShowProgressBarDrawable(Drawable drawable, Drawable thumb) {
        mBottomShowProgressDrawable = drawable;
        mBottomShowProgressThumbDrawable = thumb;
        if (mProgressBar != null) {
            mProgressBar.setProgressDrawable(drawable);
            mProgressBar.setThumb(thumb);
        }
    }

    /**
     * 底部进度条-非弹出
     */
    public void setBottomProgressBarDrawable(Drawable drawable) {
        mBottomProgressDrawable = drawable;
        if (mBottomProgressBar != null) {
            mBottomProgressBar.setProgressDrawable(drawable);
        }
    }

    /**
     * 声音进度条
     */
    public void setDialogVolumeProgressBar(Drawable drawable) {
        mVolumeProgressDrawable = drawable;
    }


    /**
     * 中间进度条
     */
    public void setDialogProgressBar(Drawable drawable) {
        mDialogProgressBarDrawable = drawable;
    }

    /**
     * 中间进度条字体颜色
     */
    public void setDialogProgressColor(int highLightColor, int normalColor) {
        mDialogProgressHighLightColor = highLightColor;
        mDialogProgressNormalColor = normalColor;
    }



    /**
     * 设置触摸显示控制ui的消失时间
     *
     * @param dismissControlTime 毫秒，默认2500
     */
    public void setDismissControlTime(int dismissControlTime) {
        this.mDismissControlTime = dismissControlTime;
    }

    public int getDismissControlTime() {
        return mDismissControlTime;
    }


    public void setStandardVideoAllCallBack(StandardVideoAllCallBack standardVideoAllCallBack) {
        this.mStandardVideoAllCallBack = standardVideoAllCallBack;
        setVideoAllCallBack(standardVideoAllCallBack);
    }

}
