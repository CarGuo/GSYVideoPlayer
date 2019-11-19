package com.shuyu.gsyvideoplayer.video;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.shuyu.gsyvideoplayer.GSYVideoADManager;
import com.shuyu.gsyvideoplayer.R;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoViewBridge;


/**
 * Created by guoshuyu on 2018/2/1.
 */

public class GSYADVideoPlayer extends StandardGSYVideoPlayer {

    protected View mJumpAd;

    protected TextView mADTime;

    protected boolean isFirstPrepared;

    public GSYADVideoPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public GSYADVideoPlayer(Context context) {
        super(context);
    }

    public GSYADVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        mJumpAd = findViewById(R.id.jump_ad);
        mADTime = (TextView) findViewById(R.id.ad_time);
        if (mJumpAd != null) {
            mJumpAd.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getGSYVideoManager().listener() != null) {
                        getGSYVideoManager().listener().onAutoCompletion();
                    }
                }
            });
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.video_layout_ad;
    }

    @Override
    public GSYVideoViewBridge getGSYVideoManager() {
        GSYVideoADManager.instance().initContext(getContext().getApplicationContext());
        return GSYVideoADManager.instance();
    }

    @Override
    protected boolean backFromFull(Context context) {
        return GSYVideoADManager.backFromWindowFull(context);
    }

    @Override
    protected void releaseVideos() {
        GSYVideoADManager.releaseAllVideos();
    }

    @Override
    protected int getFullId() {
        return GSYVideoADManager.FULLSCREEN_ID;
    }

    @Override
    protected int getSmallId() {
        return GSYVideoADManager.SMALL_ID;
    }

    @Override
    public void onPrepared() {
        super.onPrepared();
        isFirstPrepared = true;
        changeAdUIState();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start) {
            if (mCurrentState == CURRENT_STATE_ERROR) {
                clickStartIcon();
            }
        } else {
            super.onClick(v);
        }
    }

    @Override
    protected void updateStartImage() {
        if (mStartButton != null) {
            if (mStartButton instanceof ImageView) {
                ImageView imageView = (ImageView) mStartButton;
                if (mCurrentState == CURRENT_STATE_PLAYING) {
                    imageView.setImageResource(R.drawable.empty_drawable);
                } else if (mCurrentState == CURRENT_STATE_ERROR) {
                    imageView.setImageResource(R.drawable.video_click_error_selector);
                } else {
                    imageView.setImageResource(R.drawable.empty_drawable);
                }
            }
        }
    }


    /**
     * 广告期间不需要双击
     */
    @Override
    protected void touchDoubleUp() {
    }

    /**
     * 广告期间不需要触摸
     */
    @Override
    protected void touchSurfaceMove(float deltaX, float deltaY, float y) {
        if (mChangePosition) {
        } else {
            super.touchSurfaceMove(deltaX, deltaY, y);
        }
    }

    /**
     * 广告期间不需要触摸
     */
    @Override
    protected void touchSurfaceMoveFullLogic(float absDeltaX, float absDeltaY) {
        if ((absDeltaX > mThreshold || absDeltaY > mThreshold)) {
            int screenWidth = CommonUtil.getScreenWidth(getContext());
            if (absDeltaX >= mThreshold && Math.abs(screenWidth - mDownX) > mSeekEndOffset) {
                //防止全屏虚拟按键
                mChangePosition = true;
                mDownPosition = getCurrentPositionWhenPlaying();
            } else {
                super.touchSurfaceMoveFullLogic(absDeltaX, absDeltaY);
            }
        }
    }

    /**
     * 广告期间不需要触摸
     */
    @Override
    protected void touchSurfaceUp() {
        if (mChangePosition) {
            return;
        }
        super.touchSurfaceUp();

    }


    @Override
    protected void hideAllWidget() {
        if (isFirstPrepared) {
            return;
        }
        super.hideAllWidget();
    }

    @Override
    protected void setProgressAndTime(int progress, int secProgress, int currentTime, int totalTime, boolean forceChange) {
        super.setProgressAndTime(progress, secProgress, currentTime, totalTime, forceChange);
        if (mADTime != null && currentTime > 0) {
            int totalSeconds = totalTime / 1000;
            int currentSeconds = currentTime / 1000;
            mADTime.setText("" + (totalSeconds - currentSeconds));
        }
    }

    @Override
    protected void cloneParams(GSYBaseVideoPlayer from, GSYBaseVideoPlayer to) {
        super.cloneParams(from, to);
        GSYADVideoPlayer sf = (GSYADVideoPlayer) from;
        GSYADVideoPlayer st = (GSYADVideoPlayer) to;
        st.isFirstPrepared = sf.isFirstPrepared;
        st.changeAdUIState();
    }

    @Override
    public void release() {
        super.release();
        if (mADTime != null) {
            mADTime.setVisibility(GONE);
        }
    }

    /**
     * 根据是否广告url修改ui显示状态
     */
    protected void changeAdUIState() {
        if (mJumpAd != null) {
            mJumpAd.setVisibility((isFirstPrepared) ? VISIBLE : GONE);
        }
        if (mADTime != null) {
            mADTime.setVisibility((isFirstPrepared) ? VISIBLE : GONE);
        }
        if (mBottomContainer != null) {
            int color = (isFirstPrepared) ? Color.TRANSPARENT : getContext().getResources().getColor(R.color.bottom_container_bg);
            mBottomContainer.setBackgroundColor(color);
        }
        if (mCurrentTimeTextView != null) {
            mCurrentTimeTextView.setVisibility((isFirstPrepared) ? INVISIBLE : VISIBLE);
        }
        if (mTotalTimeTextView != null) {
            mTotalTimeTextView.setVisibility((isFirstPrepared) ? INVISIBLE : VISIBLE);
        }
        if (mProgressBar != null) {
            mProgressBar.setVisibility((isFirstPrepared) ? INVISIBLE : VISIBLE);
            mProgressBar.setEnabled(!(isFirstPrepared));
        }
    }

    /**
     * 移除广告播放的全屏
     */
    public void removeFullWindowViewOnly() {
        ViewGroup vp = (ViewGroup) (CommonUtil.scanForActivity(getContext())).findViewById(Window.ID_ANDROID_CONTENT);
        View old = vp.findViewById(getFullId());
        if (old != null) {
            if (old.getParent() != null) {
                ViewGroup viewGroup = (ViewGroup) old.getParent();
                vp.removeView(viewGroup);
            }
        }
        mIfCurrentIsFullscreen = false;
    }

}
