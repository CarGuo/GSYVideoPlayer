package com.shuyu.gsyvideoplayer.video;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.GSYVideoPlayer;
import com.shuyu.gsyvideoplayer.R;
import com.shuyu.gsyvideoplayer.listener.StandardVideoAllCallBack;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 标准播放器
 * Created by shuyu on 2016/11/11.
 */

public class StandardGSYVideoPlayer extends GSYVideoPlayer {


    protected static Timer DISSMISS_CONTROL_VIEW_TIMER;

    protected ProgressBar mBottomProgressBar, mLoadingProgressBar;

    protected TextView mTitleTextView; //title

    protected RelativeLayout mThumbImageViewLayout;//封面父布局

    protected ImageView mCoverImageView; //未使用

    private View mThumbImageView; //封面

    protected Dialog mBrightnessDialog;

    protected TextView mBrightnessDialogTv;

    protected Dialog mVolumeDialog;

    protected ProgressBar mDialogVolumeProgressBar;

    protected StandardVideoAllCallBack mStandardVideoAllCallBack;//标准播放器的回调

    protected DismissControlViewTimerTask mDismissControlViewTimerTask;

    protected Dialog mProgressDialog;
    protected ProgressBar mDialogProgressBar;
    protected TextView mDialogSeekTime;
    protected TextView mDialogTotalTime;
    protected ImageView mDialogIcon;


    public void setStandardVideoAllCallBack(StandardVideoAllCallBack standardVideoAllCallBack) {
        this.mStandardVideoAllCallBack = standardVideoAllCallBack;
        setVideoAllCallBack(standardVideoAllCallBack);
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
        mBottomProgressBar = (ProgressBar) findViewById(R.id.bottom_progressbar);
        mTitleTextView = (TextView) findViewById(R.id.title);
        mThumbImageViewLayout = (RelativeLayout) findViewById(R.id.thumb);
        mCoverImageView = (ImageView) findViewById(R.id.cover);
        mLoadingProgressBar = (ProgressBar) findViewById(R.id.loading);

        mThumbImageViewLayout.setVisibility(GONE);
        mThumbImageViewLayout.setOnClickListener(this);
        mBackButton.setOnClickListener(this);
        if (mThumbImageView != null) {
            mThumbImageViewLayout.removeAllViews();
            resolveThumbImage(mThumbImageView);
        }

    }

    @Override
    public boolean setUp(String url, boolean cacheWithPlay, Object... objects) {
        if (objects.length == 0) return false;
        if (super.setUp(url, cacheWithPlay, objects)) {
            mTitleTextView.setText(objects[0].toString());
            if (mIfCurrentIsFullscreen) {
                mFullscreenButton.setImageResource(R.drawable.video_shrink);
            } else {
                mFullscreenButton.setImageResource(R.drawable.video_enlarge);
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
                mBottomProgressBar.setProgress(100);
                break;
            case CURRENT_STATE_PLAYING_BUFFERING_START:
                changeUiToPlayingBufferingShow();
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int id = v.getId();
        if (id == R.id.surface_container) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    startDismissControlViewTimer();
                    if (mChangePosition) {
                        int duration = getDuration();
                        int progress = mSeekTimePosition * 100 / (duration == 0 ? 1 : duration);
                        mBottomProgressBar.setProgress(progress);
                    }
                    if (!mChangePosition && !mChangeVolume && !mBrightness) {
                        onClickUiToggle();
                    }
                    break;
            }
        } else if (id == R.id.progress) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    cancelDismissControlViewTimer();
                    break;
                case MotionEvent.ACTION_UP:
                    startDismissControlViewTimer();
                    break;
            }
        }
        return super.onTouch(v, event);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int i = v.getId();
        if (i == R.id.thumb) {
            if (TextUtils.isEmpty(mUrl)) {
                Toast.makeText(getContext(), getResources().getString(R.string.no_url), Toast.LENGTH_SHORT).show();
                return;
            }
            if (mCurrentState == CURRENT_STATE_NORMAL) {
                if (!mUrl.startsWith("file") && !CommonUtil.isWifiConnected(getContext()) && !WIFI_TIP_DIALOG_SHOWED) {
                    showWifiDialog();
                    return;
                }
                startPlayLogic();
            } else if (mCurrentState == CURRENT_STATE_AUTO_COMPLETE) {
                onClickUiToggle();
            }
        } else if (i == R.id.surface_container) {
            if (mStandardVideoAllCallBack != null && isCurrentMediaListener()) {
                if (mIfCurrentIsFullscreen) {
                    mStandardVideoAllCallBack.onClickBlankFullscreen(mUrl, mObjects);
                } else {
                    mStandardVideoAllCallBack.onClickBlank(mUrl, mObjects);
                }
            }
            startDismissControlViewTimer();
        }
    }

    @Override
    public void showWifiDialog() {
        super.showWifiDialog();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getResources().getString(R.string.tips_not_wifi));
        builder.setPositiveButton(getResources().getString(R.string.tips_not_wifi_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startPlayLogic();
                WIFI_TIP_DIALOG_SHOWED = true;
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
            mStandardVideoAllCallBack.onClickStartThumb(mUrl, mObjects);
        }
        prepareVideo();
        startDismissControlViewTimer();
    }

    private void onClickUiToggle() {
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
    protected void setProgressAndTime(int progress, int secProgress, int currentTime, int totalTime) {
        super.setProgressAndTime(progress, secProgress, currentTime, totalTime);
        if (progress != 0) mBottomProgressBar.setProgress(progress);
        if (secProgress != 0) mBottomProgressBar.setSecondaryProgress(secProgress);
    }

    @Override
    protected void resetProgressAndTime() {
        super.resetProgressAndTime();
        mBottomProgressBar.setProgress(0);
        mBottomProgressBar.setSecondaryProgress(0);
    }

    //Unified management Ui
    private void changeUiToNormal() {
        mTopContainer.setVisibility(View.VISIBLE);
        mBottomContainer.setVisibility(View.INVISIBLE);
        mStartButton.setVisibility(View.VISIBLE);
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        mThumbImageViewLayout.setVisibility(View.VISIBLE);
        mCoverImageView.setVisibility(View.VISIBLE);
        mBottomProgressBar.setVisibility(View.INVISIBLE);
        updateStartImage();
    }

    private void changeUiToPrepareingShow() {
        mTopContainer.setVisibility(View.VISIBLE);
        mBottomContainer.setVisibility(View.VISIBLE);
        mStartButton.setVisibility(View.INVISIBLE);
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        mThumbImageViewLayout.setVisibility(View.INVISIBLE);
        mCoverImageView.setVisibility(View.VISIBLE);
        mBottomProgressBar.setVisibility(View.INVISIBLE);
    }

    private void changeUiToPrepareingClear() {
        mTopContainer.setVisibility(View.INVISIBLE);
        mBottomContainer.setVisibility(View.INVISIBLE);
        mStartButton.setVisibility(View.INVISIBLE);
        mThumbImageViewLayout.setVisibility(View.INVISIBLE);
        mBottomProgressBar.setVisibility(View.INVISIBLE);
        mCoverImageView.setVisibility(View.VISIBLE);
    }

    private void changeUiToPlayingShow() {
        mTopContainer.setVisibility(View.VISIBLE);
        mBottomContainer.setVisibility(View.VISIBLE);
        mStartButton.setVisibility(View.VISIBLE);
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        mThumbImageViewLayout.setVisibility(View.INVISIBLE);
        mCoverImageView.setVisibility(View.INVISIBLE);
        mBottomProgressBar.setVisibility(View.INVISIBLE);
        updateStartImage();
    }

    private void changeUiToPlayingClear() {
        changeUiToClear();
        mBottomProgressBar.setVisibility(View.VISIBLE);
    }

    private void changeUiToPauseShow() {
        mTopContainer.setVisibility(View.VISIBLE);
        mBottomContainer.setVisibility(View.VISIBLE);
        mStartButton.setVisibility(View.VISIBLE);
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        mThumbImageViewLayout.setVisibility(View.INVISIBLE);
        mCoverImageView.setVisibility(View.INVISIBLE);
        mBottomProgressBar.setVisibility(View.INVISIBLE);
        updateStartImage();
    }

    private void changeUiToPauseClear() {
        changeUiToClear();
        mBottomProgressBar.setVisibility(View.VISIBLE);
    }

    private void changeUiToPlayingBufferingShow() {
        mTopContainer.setVisibility(View.VISIBLE);
        mBottomContainer.setVisibility(View.VISIBLE);
        mStartButton.setVisibility(View.INVISIBLE);
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        mThumbImageViewLayout.setVisibility(View.INVISIBLE);
        mCoverImageView.setVisibility(View.INVISIBLE);
        mBottomProgressBar.setVisibility(View.INVISIBLE);
    }

    private void changeUiToPlayingBufferingClear() {
        mTopContainer.setVisibility(View.INVISIBLE);
        mBottomContainer.setVisibility(View.INVISIBLE);
        mStartButton.setVisibility(View.INVISIBLE);
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        mThumbImageViewLayout.setVisibility(View.INVISIBLE);
        mCoverImageView.setVisibility(View.INVISIBLE);
        mBottomProgressBar.setVisibility(View.VISIBLE);
        updateStartImage();
    }

    private void changeUiToClear() {
        mTopContainer.setVisibility(View.INVISIBLE);
        mBottomContainer.setVisibility(View.INVISIBLE);
        mStartButton.setVisibility(View.INVISIBLE);
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        mThumbImageViewLayout.setVisibility(View.INVISIBLE);
        mCoverImageView.setVisibility(View.INVISIBLE);
        mBottomProgressBar.setVisibility(View.INVISIBLE);
    }

    private void changeUiToCompleteShow() {
        mTopContainer.setVisibility(View.VISIBLE);
        mBottomContainer.setVisibility(View.VISIBLE);
        mStartButton.setVisibility(View.VISIBLE);
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        mThumbImageViewLayout.setVisibility(View.VISIBLE);
        mCoverImageView.setVisibility(View.INVISIBLE);
        mBottomProgressBar.setVisibility(View.INVISIBLE);
        updateStartImage();
    }

    private void changeUiToCompleteClear() {
        mTopContainer.setVisibility(View.INVISIBLE);
        mBottomContainer.setVisibility(View.INVISIBLE);
        mStartButton.setVisibility(View.VISIBLE);
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        mThumbImageViewLayout.setVisibility(View.VISIBLE);
        mCoverImageView.setVisibility(View.INVISIBLE);
        mBottomProgressBar.setVisibility(View.VISIBLE);
        updateStartImage();
    }

    private void changeUiToError() {
        mTopContainer.setVisibility(View.INVISIBLE);
        mBottomContainer.setVisibility(View.INVISIBLE);
        mStartButton.setVisibility(View.VISIBLE);
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        mThumbImageViewLayout.setVisibility(View.INVISIBLE);
        mCoverImageView.setVisibility(View.VISIBLE);
        mBottomProgressBar.setVisibility(View.INVISIBLE);
        updateStartImage();
    }

    private void updateStartImage() {
        if (mCurrentState == CURRENT_STATE_PLAYING) {
            mStartButton.setImageResource(R.drawable.video_click_pause_selector);
        } else if (mCurrentState == CURRENT_STATE_ERROR) {
            mStartButton.setImageResource(R.drawable.video_click_error_selector);
        } else {
            mStartButton.setImageResource(R.drawable.video_click_play_selector);
        }
    }

    @Override
    protected void showProgressDialog(float deltaX, String seekTime, int seekTimePosition, String totalTime, int totalTimeDuration) {
        super.showProgressDialog(deltaX, seekTime, seekTimePosition, totalTime, totalTimeDuration);
        if (mProgressDialog == null) {
            View localView = LayoutInflater.from(getContext()).inflate(R.layout.video_progress_dialog, null);
            mDialogProgressBar = ((ProgressBar) localView.findViewById(R.id.duration_progressbar));
            mDialogSeekTime = ((TextView) localView.findViewById(R.id.tv_current));
            mDialogTotalTime = ((TextView) localView.findViewById(R.id.tv_duration));
            mDialogIcon = ((ImageView) localView.findViewById(R.id.duration_image_tip));
            mProgressDialog = new Dialog(getContext(), R.style.video_style_dialog_progress);
            mProgressDialog.setContentView(localView);
            mProgressDialog.getWindow().addFlags(Window.FEATURE_ACTION_BAR);
            mProgressDialog.getWindow().addFlags(32);
            mProgressDialog.getWindow().addFlags(16);
            mProgressDialog.getWindow().setLayout(-2, -2);
            WindowManager.LayoutParams localLayoutParams = mProgressDialog.getWindow().getAttributes();
            localLayoutParams.gravity = 49;
            localLayoutParams.y = getResources().getDimensionPixelOffset(R.dimen.video_progress_dialog_margin_top);
            mProgressDialog.getWindow().setAttributes(localLayoutParams);
        }
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }

        mDialogSeekTime.setText(seekTime);
        mDialogTotalTime.setText(" / " + totalTime);
        mDialogProgressBar.setProgress(seekTimePosition * 100 / totalTimeDuration);
        if (deltaX > 0) {
            mDialogIcon.setBackgroundResource(R.drawable.video_forward_icon);
        } else {
            mDialogIcon.setBackgroundResource(R.drawable.video_backward_icon);
        }

    }

    @Override
    protected void dismissProgressDialog() {
        super.dismissProgressDialog();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void showVolumeDialog(float deltaY, int volumePercent) {
        super.showVolumeDialog(deltaY, volumePercent);
        if (mVolumeDialog == null) {
            View localView = LayoutInflater.from(getContext()).inflate(R.layout.video_volume_dialog, null);
            mDialogVolumeProgressBar = ((ProgressBar) localView.findViewById(R.id.volume_progressbar));
            mVolumeDialog = new Dialog(getContext(), R.style.video_style_dialog_progress);
            mVolumeDialog.setContentView(localView);
            mVolumeDialog.getWindow().addFlags(8);
            mVolumeDialog.getWindow().addFlags(32);
            mVolumeDialog.getWindow().addFlags(16);
            mVolumeDialog.getWindow().setLayout(-2, -2);
            WindowManager.LayoutParams localLayoutParams = mVolumeDialog.getWindow().getAttributes();
            localLayoutParams.gravity = 19;
            localLayoutParams.x = getContext().getResources().getDimensionPixelOffset(R.dimen.video_volume_dialog_margin_left);
            mVolumeDialog.getWindow().setAttributes(localLayoutParams);
        }
        if (!mVolumeDialog.isShowing()) {
            mVolumeDialog.show();
        }

        mDialogVolumeProgressBar.setProgress(volumePercent);
    }

    @Override
    protected void dismissVolumeDialog() {
        super.dismissVolumeDialog();
        if (mVolumeDialog != null) {
            mVolumeDialog.dismiss();
        }
    }

    @Override
    protected void showBrightnessDialog(float percent) {
        if (mBrightnessDialog == null) {
            View localView = LayoutInflater.from(getContext()).inflate(R.layout.video_brightness, null);
            mBrightnessDialogTv = (TextView) localView.findViewById(R.id.app_video_brightness);
            mBrightnessDialog = new Dialog(getContext(), R.style.video_style_dialog_progress);
            mBrightnessDialog.setContentView(localView);
            mBrightnessDialog.getWindow().addFlags(8);
            mBrightnessDialog.getWindow().addFlags(32);
            mBrightnessDialog.getWindow().addFlags(16);
            mBrightnessDialog.getWindow().setLayout(-2, -2);
            WindowManager.LayoutParams localLayoutParams = mBrightnessDialog.getWindow().getAttributes();
            localLayoutParams.gravity = Gravity.CENTER;
            localLayoutParams.x = ViewGroup.LayoutParams.WRAP_CONTENT;
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
        super.dismissVolumeDialog();
        if (mBrightnessDialog != null) {
            mBrightnessDialog.dismiss();
        }
    }


    private void startDismissControlViewTimer() {
        cancelDismissControlViewTimer();
        DISSMISS_CONTROL_VIEW_TIMER = new Timer();
        mDismissControlViewTimerTask = new DismissControlViewTimerTask();
        DISSMISS_CONTROL_VIEW_TIMER.schedule(mDismissControlViewTimerTask, 2500);
    }

    private void cancelDismissControlViewTimer() {
        if (DISSMISS_CONTROL_VIEW_TIMER != null) {
            DISSMISS_CONTROL_VIEW_TIMER.cancel();
        }
        if (mDismissControlViewTimerTask != null) {
            mDismissControlViewTimerTask.cancel();
        }

    }

    protected class DismissControlViewTimerTask extends TimerTask {

        @Override
        public void run() {
            if (mCurrentState != CURRENT_STATE_NORMAL
                    && mCurrentState != CURRENT_STATE_ERROR
                    && mCurrentState != CURRENT_STATE_AUTO_COMPLETE) {
                if (getContext() != null && getContext() instanceof Activity) {
                    ((Activity) getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mBottomContainer.setVisibility(View.INVISIBLE);
                            mTopContainer.setVisibility(View.INVISIBLE);
                            mBottomProgressBar.setVisibility(View.VISIBLE);
                            mStartButton.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        }
    }

    private void resolveThumbImage(View thumb) {
        mThumbImageViewLayout.addView(thumb);
        ViewGroup.LayoutParams layoutParams = thumb.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        thumb.setLayoutParams(layoutParams);
    }

    /***
     * 设置封面
     */
    public void setThumbImageView(View view) {
        if (mThumbImageViewLayout != null) {
            mThumbImageView = view;
            resolveThumbImage(view);
        }
    }

    /***
     * 清除封面
     */
    public void clearThumbImageView() {
        if (mThumbImageViewLayout != null) {
            mThumbImageViewLayout.removeAllViews();
        }
    }

    /**
     * 回去title
     */
    public TextView getTitleTextView() {
        return mTitleTextView;
    }

    @Override
    public void onBackFullscreen() {
        clearFullscreenLayout();
    }

}
