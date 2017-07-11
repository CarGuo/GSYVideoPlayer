package com.shuyu.gsyvideoplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.utils.MeasureHelper;

/**
 * Created by shuyu on 2016/12/6.
 */

@SuppressLint("AppCompatCustomView")
public class GSYImageCover extends ImageView {

    private MeasureHelper measureHelper;

    public GSYImageCover(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GSYImageCover(Context context) {
        super(context);
        init();
    }

    public GSYImageCover(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        measureHelper = new MeasureHelper(this);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int videoWidth = GSYVideoManager.instance().getCurrentVideoWidth();
        int videoHeight = GSYVideoManager.instance().getCurrentVideoHeight();
        if (videoWidth > 0 && videoHeight > 0) {
            measureHelper.setVideoSize(videoWidth, videoHeight);
        }
        measureHelper.setVideoRotation((int) getRotation());
        measureHelper.doMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(measureHelper.getMeasuredWidth(), measureHelper.getMeasuredHeight());
    }

}
