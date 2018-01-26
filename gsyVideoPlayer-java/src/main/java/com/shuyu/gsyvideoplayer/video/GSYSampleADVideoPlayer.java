package com.shuyu.gsyvideoplayer.video;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.shuyu.gsyvideoplayer.R;
import com.shuyu.gsyvideoplayer.model.GSYVideoModel;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 广告播放
 * Created by guoshuyu on 2018/1/26.
 */

public class GSYSampleADVideoPlayer extends ListGSYVideoPlayer {

    protected View mJumpAd;

    protected ViewGroup mWidgetContainer;

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
        mWidgetContainer = (ViewGroup) findViewById(R.id.widget_container);
        mJumpAd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        });
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
     * @param position
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
     * @param position
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
     * @param position
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
     * @param position
     * @param cachePath     缓存路径，如果是M3U8或者HLS，请设置为false
     * @param mapHeadData   http header
     * @param changeState
     * @return
     */
    @Override
    protected boolean setUp(List<GSYVideoModel> url, boolean cacheWithPlay, int position, File cachePath, Map<String, String> mapHeadData, boolean changeState) {
        initSetupModel(url.get(position));
        return super.setUp(url, cacheWithPlay, position, cachePath, mapHeadData, changeState);
    }

    @Override
    public void onPrepared() {
        super.onPrepared();
        isFirstPrepared = true;
        changeAdUIState();
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
     * 针对每个播放数据源的类型，设置状态
     * @param gsyVideoModel
     */
    protected void initSetupModel(GSYVideoModel gsyVideoModel) {
        if (gsyVideoModel instanceof GSYADVideoModel) {
            GSYADVideoModel gsyadVideoModel = (GSYADVideoModel) gsyVideoModel;
            isAdModel = (gsyadVideoModel.getType() == GSYADVideoModel.TYPE_AD);
        }
        changeAdUIState();
    }

    /**
     * 根据是否广告url修改ui显示状态
     */
    protected void changeAdUIState() {
        mJumpAd.setVisibility((isFirstPrepared && isAdModel) ? VISIBLE : GONE);
        mWidgetContainer.setVisibility((isFirstPrepared && isAdModel) ? GONE : VISIBLE);
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

        public static int TYPE_NORMAL = 0;

        public static int TYPE_AD = 1;

        private int mType = TYPE_NORMAL;

        public GSYADVideoModel(String url, String title, int type) {
            super(url, title);
            mType = type;
        }

        public int getType() {
            return mType;
        }

        public void setmType(int type) {
            this.mType = type;
        }
    }
}
