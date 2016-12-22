package com.example.gsyvideoplayer.video;

import android.content.Context;
import android.util.AttributeSet;

import com.example.gsyvideoplayer.R;

/**
 * Created by shuyu on 2016/12/23.
 */

public class LandLayoutVideo extends SampleVideo {

    /**
     * 1.5开始加入，必须重载
     */
    public LandLayoutVideo(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public LandLayoutVideo(Context context) {
        super(context);
    }

    public LandLayoutVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int getLayoutId() {
        if (mIfCurrentIsFullscreen) {
            return R.layout.sample_video_land;
        }
        return R.layout.sample_video;
    }

}
