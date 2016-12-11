package com.shuyu.gsyvideoplayer.video;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.shuyu.gsyvideoplayer.GSYPreViewManager;
import com.shuyu.gsyvideoplayer.GSYTextureView;
import com.shuyu.gsyvideoplayer.R;
import com.shuyu.gsyvideoplayer.utils.Debuger;

/**
 * Created by shuyu on 2016/12/10.
 */

public class CustomGSYVideoPlayer extends StandardGSYVideoPlayer {

    private RelativeLayout mPreviewLayout;

    private GSYTextureView mPreviewTexture;

    //是否因为用户点击
    private boolean mIsFromUser;

    public CustomGSYVideoPlayer(Context context) {
        super(context);
        initView();
    }

    public CustomGSYVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }


    private void initView() {
        mPreviewLayout = (RelativeLayout) findViewById(R.id.preview_layout);
    }

    @Override
    public int getLayoutId() {
        return R.layout.video_layout_custom;
    }


    @Override
    protected void addTextureView() {
        super.addTextureView();

        if (mPreviewLayout.getChildCount() > 0) {
            mPreviewLayout.removeAllViews();
        }
        mPreviewTexture = null;
        mPreviewTexture = new GSYTextureView(getContext());
        mPreviewTexture.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                GSYPreViewManager.instance().setDisplay(new Surface(surface));
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                GSYPreViewManager.instance().setDisplay(null);
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
        mPreviewTexture.setRotation(mRotate);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        mPreviewLayout.addView(mPreviewTexture, layoutParams);
    }

    @Override
    protected void prepareVideo() {
        GSYPreViewManager.instance().prepare(mUrl, mMapHeadData, mLooping, mSpeed);
        super.prepareVideo();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
        super.onProgressChanged(seekBar, progress, fromUser);
        if (fromUser) {
            int width = seekBar.getWidth();
            int offset = (int) (width - (getResources().getDimension(R.dimen.seek_bar_image) / 2)) / 100 * progress;

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mPreviewLayout.getLayoutParams();
            layoutParams.leftMargin = offset;
            //设置帧预览图的显示位置
            mPreviewLayout.setLayoutParams(layoutParams);
            if (GSYPreViewManager.instance().getMediaPlayer() != null && mHadPlay) {
                int time = progress * getDuration() / 100;
                Debuger.printfLog("SEEK TO " + time);
                GSYPreViewManager.instance().getMediaPlayer().seekTo(time);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        super.onStartTrackingTouch(seekBar);
        mIsFromUser = true;
        mPreviewLayout.setVisibility(VISIBLE);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        super.onStopTrackingTouch(seekBar);
        mIsFromUser = false;
        mPreviewLayout.setVisibility(GONE);
    }

    @Override
    protected void setTextAndProgress(int secProgress) {
        if (mIsFromUser) {
            return;
        }
        super.setTextAndProgress(secProgress);
    }
}
