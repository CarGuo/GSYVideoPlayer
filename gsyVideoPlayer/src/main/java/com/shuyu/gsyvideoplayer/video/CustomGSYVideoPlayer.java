package com.shuyu.gsyvideoplayer.video;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.GSYVideoPlayer;
import com.shuyu.gsyvideoplayer.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by shuyu on 2016/12/10.
 */

public class CustomGSYVideoPlayer extends StandardGSYVideoPlayer {


    private ImageView mSeekBarImage;

    private ShowSeekBarImageTimerTask mShowSeekBarImageTimerTask;

    private Timer mSeekBarImageTimer;

    //记录上一个进度图的位置，用于判断是否取数据
    private int mPreSeekPosition = -1;

    //记录进度图变化的帧图片图的偏移时间，避免太频繁进入
    private long mOffsetTime;

    public CustomGSYVideoPlayer(Context context) {
        super(context);
        initView();
    }

    public CustomGSYVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }


    private void initView() {
        mSeekBarImage = (ImageView) findViewById(R.id.seek_bar_image);
    }

    @Override
    public int getLayoutId() {
        return R.layout.video_layout_custom;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        super.onProgressChanged(seekBar, progress, fromUser);
        if ((mCurrentState == GSYVideoPlayer.CURRENT_STATE_PLAYING
                || mCurrentState == GSYVideoPlayer.CURRENT_STATE_PAUSE)
                && GSYVideoManager.instance().getMediaPlayer() != null) {

            int width = seekBar.getWidth();
            int offset = (int) (width - (getResources().getDimension(R.dimen.seek_bar_image) / 2)) / 100 * progress;

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mSeekBarImage.getLayoutParams();
            layoutParams.leftMargin = offset;
            //设置帧预览图的显示位置
            mSeekBarImage.setLayoutParams(layoutParams);

            long currentTime = System.currentTimeMillis();

            if (fromUser && (mPreSeekPosition == -1 || Math.abs(progress - mPreSeekPosition) > 2)
                    && (currentTime - mOffsetTime) > 400) {
                //开始预览帧小图
                startSeekBarImageTimer(seekBar.getProgress());
                mPreSeekPosition = progress;
                mOffsetTime = currentTime;
            }

        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        super.onStartTrackingTouch(seekBar);
        mSeekBarImage.setVisibility(VISIBLE);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        super.onStopTrackingTouch(seekBar);
        cancelSeekBarImageTimer();
        mSeekBarImage.setVisibility(GONE);
        mPreSeekPosition = -1;
    }

    private void startSeekBarImageTimer(int progress) {
        cancelSeekBarImageTimer();
        mSeekBarImageTimer = new Timer();
        mShowSeekBarImageTimerTask = new ShowSeekBarImageTimerTask(progress);
        mSeekBarImageTimer.schedule(mShowSeekBarImageTimerTask, 10);
    }

    private void cancelSeekBarImageTimer() {
        if (mShowSeekBarImageTimerTask != null) {
            mShowSeekBarImageTimerTask.cancel();
        }
        if (mSeekBarImageTimer != null) {
            mSeekBarImageTimer.cancel();
        }

    }

    /**
     * 获取帧预览图任务
     **/
    protected class ShowSeekBarImageTimerTask extends TimerTask {

        int progress;

        ShowSeekBarImageTimerTask(int progress) {
            this.progress = progress;
        }

        @Override
        public void run() {
            if (!TextUtils.isEmpty(mUrl)) {
                try {
                    int time = progress * getDuration() / 100 * 1000;
                    //获取帧图片
                    if (GSYVideoManager.instance().getMediaMetadataRetriever() != null) {
                        final Bitmap bitmap = GSYVideoManager.instance().getMediaMetadataRetriever()
                                .getFrameAtTime(time, MediaMetadataRetriever.OPTION_CLOSEST);
                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (bitmap != null) {
                                    //显示
                                    mSeekBarImage.setImageBitmap(bitmap);
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
