package com.example.gsyvideoplayer.switchplay;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.example.gsyvideoplayer.R;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

public class SwitchVideo extends StandardGSYVideoPlayer {

    private TextView detailBtn;

    public SwitchVideo(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public SwitchVideo(Context context) {
        super(context);
    }

    public SwitchVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        detailBtn = (TextView) findViewById(R.id.detail_btn);
        detailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInPlayingState()) {
                    SwitchUtil.savePlayState(SwitchVideo.this);
                    getGSYVideoManager().setLastListener(SwitchVideo.this);
                    //fixme 页面跳转是，元素共享，效果会有一个中间中间控件的存在
                    //fixme 这时候中间控件 CURRENT_STATE_PLAYING，会触发 startProgressTimer
                    //FIXME 但是没有cancel
                    SwitchDetailActivity.startTActivity((Activity) getContext(), SwitchVideo.this);
                }
            }
        });
        if (mIfCurrentIsFullscreen) {
            detailBtn.setVisibility(GONE);
        }

    }

    @Override
    public int getLayoutId() {
        return R.layout.switch_video;
    }


    public void setSwitchUrl(String url) {
        mUrl = url;
        mOriginUrl = url;
    }

    public void setSwitchCache(boolean cache) {
        mCache = cache;
    }

    public void setSwitchTitle(String title) {
        mTitle = title;
    }

    public void setSurfaceToPlay() {
        addTextureView();
        getGSYVideoManager().setListener(this);
        checkoutState();
    }

    public SwitchVideo saveState() {
        SwitchVideo switchVideo = new SwitchVideo(getContext());
        cloneParams(this, switchVideo);
        return switchVideo;
    }

    public void cloneState(SwitchVideo switchVideo) {
        cloneParams(switchVideo, this);
    }

}
