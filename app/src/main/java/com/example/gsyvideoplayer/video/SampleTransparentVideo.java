package com.example.gsyvideoplayer.video;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.gsyvideoplayer.R;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;


public class SampleTransparentVideo extends SampleControlVideo {


    /**
     * 1.5.0开始加入，如果需要不同布局区分功能，需要重载
     */
    public SampleTransparentVideo(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public SampleTransparentVideo(Context context) {
        super(context);
    }

    public SampleTransparentVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void init(Context context) {
        super.init(context);
        // 设置透明背景或者其他颜色
        setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected void addTextureView() {
        super.addTextureView();
        if (mTextureView.getShowView() instanceof TextureView) {
            // 设置透明，如果有需要，不需要可以不设置，只空白的地方透明
            // mTextureView.getShowView().setAlpha(0.5f);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.sample_transparent_video;
    }


    /**
     * 全屏时将对应处理参数逻辑赋给全屏播放器
     *
     * @param context
     * @param actionBar
     * @param statusBar
     * @return
     */
    @Override
    public GSYBaseVideoPlayer startWindowFullscreen(Context context, boolean actionBar, boolean statusBar) {
        SampleTransparentVideo sampleVideo = (SampleTransparentVideo) super.startWindowFullscreen(context, actionBar, statusBar);
        ///如果全屏时也需要透明或者特殊颜色
        ((ViewGroup)sampleVideo.getParent()).setBackgroundColor(Color.TRANSPARENT);
        return sampleVideo;
    }

}
