package com.shuyu.gsyvideoplayer.video;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.shuyu.gsyvideoplayer.R;
import com.shuyu.gsyvideoplayer.model.GSYVideoModel;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 只支持每个片头广告播放的类
 * 其实就是根据实体，判断播放列表中哪个是广告，哪个不是，从而处理不同的UI显示效果
 * Created by guoshuyu on 2018/1/26.
 */

public class GSYSampleADVideoPlayer extends ListGSYVideoPlayer {

    protected View mJumpAd;

    protected ViewGroup mWidgetContainer;

    protected TextView mADTime;

    protected boolean isAdModel = false;

    protected boolean isFirstPrepared = false;

    public GSYSampleADVideoPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public GSYSampleADVideoPlayer(Context context) {
        super(context);
    }

    public GSYSampleADVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        mJumpAd = findViewById(R.id.jump_ad);
        mADTime = (TextView) findViewById(R.id.ad_time);
        mWidgetContainer = (ViewGroup) findViewById(R.id.widget_container);
        if (mJumpAd != null) {
            mJumpAd.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    playNext();
                }
            });
        }

    }

    @Override
    public int getLayoutId() {
        return R.layout.video_layout_sample_ad;
    }

    /**
     * 如果需要片头广告的，请用setAdUp
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param position      需要播放的位置
     * @return
     */
    @Override
    public boolean setUp(List<GSYVideoModel> url, boolean cacheWithPlay, int position) {
        return setUp(url, cacheWithPlay, position, null);
    }

    /**
     * 如果需要片头广告的，请用setAdUp
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param position      需要播放的位置
     * @param cachePath     缓存路径，如果是M3U8或者HLS，请设置为false
     * @return
     */
    @Override
    public boolean setUp(List<GSYVideoModel> url, boolean cacheWithPlay, int position, File cachePath) {
        return setUp(url, cacheWithPlay, position, cachePath, new HashMap<String, String>());
    }

    /**
     * 如果需要片头广告的，请用setAdUp
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param position      需要播放的位置
     * @param cachePath     缓存路径，如果是M3U8或者HLS，请设置为false
     * @param mapHeadData   http header
     * @return
     */
    @Override
    public boolean setUp(List<GSYVideoModel> url, boolean cacheWithPlay, int position, File cachePath, Map<String, String> mapHeadData) {
        return setUp(url, cacheWithPlay, position, cachePath, mapHeadData, true);
    }

    /**
     * 如果需要片头广告的，请用setAdUp
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param position      需要播放的位置
     * @param cachePath     缓存路径，如果是M3U8或者HLS，请设置为false
     * @param mapHeadData   http header
     * @param changeState   切换的时候释放surface
     * @return
     */
    @Override
    protected boolean setUp(List<GSYVideoModel> url, boolean cacheWithPlay, int position, File cachePath, Map<String, String> mapHeadData, boolean changeState) {
        GSYVideoModel gsyVideoModel = url.get(position);
        if (gsyVideoModel instanceof GSYADVideoModel) {
            GSYADVideoModel gsyadVideoModel = (GSYADVideoModel) gsyVideoModel;
            if (gsyadVideoModel.isSkip() && position < (url.size() - 1)) {
                return setUp(url, cacheWithPlay, position + 1, cachePath, mapHeadData, changeState);
            }
            isAdModel = (gsyadVideoModel.getType() == GSYADVideoModel.TYPE_AD);
        }
        changeAdUIState();
        return super.setUp(url, cacheWithPlay, position, cachePath, mapHeadData, changeState);
    }

    @Override
    public void onPrepared() {
        super.onPrepared();
        isFirstPrepared = true;
        changeAdUIState();
    }

    @Override
    protected void updateStartImage() {
        if (mStartButton != null) {
            if (mStartButton instanceof ImageView) {
                ImageView imageView = (ImageView) mStartButton;
                if (mCurrentState == CURRENT_STATE_PLAYING) {
                    imageView.setImageResource(R.drawable.video_click_pause_selector);
                } else if (mCurrentState == CURRENT_STATE_ERROR) {
                    imageView.setImageResource(R.drawable.video_click_play_selector);
                } else {
                    imageView.setImageResource(R.drawable.video_click_play_selector);
                }
            }
        }
    }

    /**
     * 广告期间不需要双击
     */
    @Override
    protected void touchDoubleUp() {
        if (isAdModel) {
            return;
        }
        super.touchDoubleUp();
    }

    /**
     * 广告期间不需要触摸
     */
    @Override
    protected void touchSurfaceMove(float deltaX, float deltaY, float y) {
        if (mChangePosition && isAdModel) {
            return;
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
            if (isAdModel && absDeltaX >= mThreshold && Math.abs(screenWidth - mDownX) > mSeekEndOffset) {
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
        if (mChangePosition && isAdModel) {
            return;
        }
        super.touchSurfaceUp();

    }


    @Override
    protected void hideAllWidget() {
        if (isFirstPrepared && isAdModel) {
            return;
        }
        super.hideAllWidget();
    }

    @Override
    protected void setProgressAndTime(int progress, int secProgress, int currentTime, int totalTime) {
        super.setProgressAndTime(progress, secProgress, currentTime, totalTime);
        if (mADTime != null && currentTime > 0) {
            int totalSeconds = totalTime / 1000;
            int currentSeconds = currentTime / 1000;
            mADTime.setText("" + (totalSeconds - currentSeconds));
        }
    }

    @Override
    protected void cloneParams(GSYBaseVideoPlayer from, GSYBaseVideoPlayer to) {
        super.cloneParams(from, to);
        GSYSampleADVideoPlayer sf = (GSYSampleADVideoPlayer) from;
        GSYSampleADVideoPlayer st = (GSYSampleADVideoPlayer) to;
        st.isAdModel = sf.isAdModel;
        st.isFirstPrepared = sf.isFirstPrepared;
        st.changeAdUIState();
    }


    /**
     * 根据是否广告url修改ui显示状态
     */
    protected void changeAdUIState() {
        if (mJumpAd != null) {
            mJumpAd.setVisibility((isFirstPrepared && isAdModel) ? VISIBLE : GONE);
        }
        if (mADTime != null) {
            mADTime.setVisibility((isFirstPrepared && isAdModel) ? VISIBLE : GONE);
        }
        if (mWidgetContainer != null) {
            mWidgetContainer.setVisibility((isFirstPrepared && isAdModel) ? GONE : VISIBLE);
        }
        if (mBottomContainer != null) {
            int color = (isFirstPrepared && isAdModel) ? Color.TRANSPARENT : getContext().getResources().getColor(R.color.bottom_container_bg);
            mBottomContainer.setBackgroundColor(color);
        }
        if (mCurrentTimeTextView != null) {
            mCurrentTimeTextView.setVisibility((isFirstPrepared && isAdModel) ? INVISIBLE : VISIBLE);
        }
        if (mTotalTimeTextView != null) {
            mTotalTimeTextView.setVisibility((isFirstPrepared && isAdModel) ? INVISIBLE : VISIBLE);
        }
        if (mProgressBar != null) {
            mProgressBar.setVisibility((isFirstPrepared && isAdModel) ? INVISIBLE : VISIBLE);
            mProgressBar.setEnabled(!(isFirstPrepared && isAdModel));
        }
    }


    /******************对外接口*******************/

    /**
     * 带片头广告的，setAdUp
     *
     * @param url
     * @param cacheWithPlay
     * @param position
     * @return
     */
    public boolean setAdUp(ArrayList<GSYADVideoModel> url, boolean cacheWithPlay, int position) {
        return setUp((ArrayList<GSYVideoModel>) url.clone(), cacheWithPlay, position);
    }

    /**
     * 带片头广告的，setAdUp
     *
     * @param url
     * @param cacheWithPlay
     * @param position
     * @param cachePath
     * @return
     */
    public boolean setAdUp(ArrayList<GSYADVideoModel> url, boolean cacheWithPlay, int position, File cachePath) {
        return setUp((ArrayList<GSYVideoModel>) url.clone(), cacheWithPlay, position, cachePath);
    }

    /**
     * 带片头广告的，setAdUp
     *
     * @param url
     * @param cacheWithPlay
     * @param position
     * @param cachePath
     * @param mapHeadData
     * @return
     */
    public boolean setAdUp(ArrayList<GSYADVideoModel> url, boolean cacheWithPlay, int position, File cachePath, Map<String, String> mapHeadData) {
        return setUp((ArrayList<GSYVideoModel>) url.clone(), cacheWithPlay, position, cachePath, mapHeadData);
    }

    public static class GSYADVideoModel extends GSYVideoModel {
        /**
         * 正常
         */
        public static int TYPE_NORMAL = 0;

        /**
         * 广告
         */
        public static int TYPE_AD = 1;

        /**
         * 类型
         */
        private int mType = TYPE_NORMAL;

        /**
         * 是否跳过
         */
        private boolean isSkip;

        /**
         * @param url   播放url
         * @param title 标题
         * @param type  类型 广告还是正常类型
         */
        public GSYADVideoModel(String url, String title, int type) {
            this(url, title, type, false);
        }

        /**
         * @param url    播放url
         * @param title  标题
         * @param type   类型 广告还是正常类型
         * @param isSkip 是否跳过
         */
        public GSYADVideoModel(String url, String title, int type, boolean isSkip) {
            super(url, title);
            this.mType = type;
            this.isSkip = isSkip;
        }

        public int getType() {
            return mType;
        }

        public void setType(int type) {
            this.mType = type;
        }

        public boolean isSkip() {
            return isSkip;
        }

        public void setSkip(boolean skip) {
            isSkip = skip;
        }
    }
}
