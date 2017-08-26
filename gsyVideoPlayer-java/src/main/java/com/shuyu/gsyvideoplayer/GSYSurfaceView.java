package com.shuyu.gsyvideoplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

import com.shuyu.gsyvideoplayer.utils.MeasureHelper;

/**
 *
 * Created by guoshuyu on 2017/8/26.
 */

public class GSYSurfaceView extends SurfaceView {
    private MeasureHelper measureHelper;

    public GSYSurfaceView(Context context) {
        super(context);
        init();
    }

    public GSYSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        measureHelper = new MeasureHelper(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int videoWidth = GSYVideoManager.instance().getCurrentVideoWidth();
        int videoHeight = GSYVideoManager.instance().getCurrentVideoHeight();

        int videoSarNum = GSYVideoManager.instance().getMediaPlayer().getVideoSarNum();
        int videoSarDen = GSYVideoManager.instance().getMediaPlayer().getVideoSarDen();

        if (videoWidth > 0 && videoHeight > 0) {
            measureHelper.setVideoSampleAspectRatio(videoSarNum, videoSarDen);
            measureHelper.setVideoSize(videoWidth, videoHeight);
        }
        measureHelper.setVideoRotation((int)getRotation());
        measureHelper.doMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(measureHelper.getMeasuredWidth(), measureHelper.getMeasuredHeight());
    }

    public int getSizeH() {
        return measureHelper.getMeasuredHeight();
    }

    public int getSizeW() {
        return measureHelper.getMeasuredWidth();
    }
}
