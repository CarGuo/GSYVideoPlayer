package com.example.gsyvideoplayer.video;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gsyvideoplayer.R;
import com.example.gsyvideoplayer.model.SwitchVideoModel;
import com.example.gsyvideoplayer.view.SwitchVideoTypeDialog;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.GSYVideoPlayer;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by shuyu on 2016/12/7.
 */

public class SampleVideo extends StandardGSYVideoPlayer {

    private TextView mMoreScale;

    private TextView mSwitchSize;

    private ImageView mSeekBarImage;

    private Timer mSeekBarImageTimer;

    private List<SwitchVideoModel> mUrlList = new ArrayList<>();

    private ShowSeekBarImageTimerTask mShowSeekBarImageTimerTask;

    //记住切换数据源类型
    private int mType = 0;

    //数据源
    private int mSourcePosition = 0;

    //记录上一个进度图的位置，用于判断是否取数据
    private int mPreSeekPosition = -1;

    //记录进度图变化的帧图片图的偏移时间，避免太频繁进入
    private long mOffsetTime;


    public SampleVideo(Context context) {
        super(context);
        initView();
    }

    public SampleVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        mMoreScale = (TextView) findViewById(R.id.moreScale);
        mSwitchSize = (TextView) findViewById(R.id.switchSize);
        mSeekBarImage = (ImageView) findViewById(R.id.seek_bar_image);

        //切换清晰度
        mMoreScale.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mType == 0) {
                    mType = 1;
                    mMoreScale.setText("16:9");
                    GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_16_9);
                    if (mTextureView != null)
                        mTextureView.requestLayout();
                } else if (mType == 1) {
                    mType = 2;
                    mMoreScale.setText("4:3");
                    GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_4_3);
                    if (mTextureView != null)
                        mTextureView.requestLayout();
                } else if (mType == 2) {
                    mType = 0;
                    mMoreScale.setText("默认比例");
                    GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT);
                    if (mTextureView != null)
                        mTextureView.requestLayout();
                }
            }
        });

        //切换视频清晰度
        mSwitchSize.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showSwitchDialog();
            }
        });

    }

    /**
     * 设置播放URL
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param objects       object[0]目前为title
     * @return
     */
    public boolean setUp(List<SwitchVideoModel> url, boolean cacheWithPlay, Object... objects) {
        mUrlList = url;
        return setUp(url.get(0).getUrl(), cacheWithPlay, objects);
    }

    /**
     * 设置播放URL
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param cachePath     缓存路径，如果是M3U8或者HLS，请设置为false
     * @param objects       object[0]目前为title
     * @return
     */
    public boolean setUp(List<SwitchVideoModel> url, boolean cacheWithPlay, File cachePath, Object... objects) {
        mUrlList = url;
        return setUp(url.get(0).getUrl(), cacheWithPlay, cachePath, objects);
    }

    @Override
    public int getLayoutId() {
        return R.layout.sample_video;
    }

    /**
     * 弹出切换清晰度
     */
    private void showSwitchDialog() {
        SwitchVideoTypeDialog switchVideoTypeDialog = new SwitchVideoTypeDialog(getContext());
        switchVideoTypeDialog.initList(mUrlList, new SwitchVideoTypeDialog.OnListItemClickListener() {
            @Override
            public void onItemClick(int position) {
                final String name = mUrlList.get(position).getName();
                if (mSourcePosition != position) {
                    if ((mCurrentState == GSYVideoPlayer.CURRENT_STATE_PLAYING
                            || mCurrentState == GSYVideoPlayer.CURRENT_STATE_PAUSE)
                            && GSYVideoManager.instance().getMediaPlayer() != null) {
                        final String url = mUrlList.get(position).getUrl();
                        onVideoPause();
                        final long currentPosition = mCurrentPosition;
                        GSYVideoManager.instance().releaseMediaPlayer();
                        cancelProgressTimer();
                        hideAllWidget();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setUp(url, mCache, mCachePath, mObjects);
                                setSeekOnStart(currentPosition);
                                startPlayLogic();
                                cancelProgressTimer();
                                hideAllWidget();
                            }
                        }, 500);
                        mSwitchSize.setText(name);
                        mSourcePosition = position;
                    }
                } else {
                    Toast.makeText(getContext(), "已经是 " + name, Toast.LENGTH_LONG).show();
                }
            }
        });
        switchVideoTypeDialog.show();
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

    @Override
    public void onPrepared() {
        super.onPrepared();
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
