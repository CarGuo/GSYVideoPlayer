package com.shuyu.gsyvideoplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by shuyu on 2016/12/6.
 */

public class GSYImageCover extends ImageView {

    public GSYImageCover(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GSYImageCover(Context context) {
        super(context);
    }

    public GSYImageCover(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int videoWidth = GSYVideoManager.instance().getCurrentVideoWidth();
        int videoHeight = GSYVideoManager.instance().getCurrentVideoHeight();

        int width = getDefaultSize(videoWidth, widthMeasureSpec);
        int height = getDefaultSize(videoHeight, heightMeasureSpec);

        int widthS = getDefaultSize(videoWidth, widthMeasureSpec);
        int heightS = getDefaultSize(videoHeight, heightMeasureSpec);


        if (videoWidth > 0 && videoHeight > 0) {

            int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                width = widthSpecSize;
                height = heightSpecSize;

                if (videoWidth * height < width * videoHeight) {
                    width = height * videoWidth / videoHeight;
                } else if (videoWidth * height > width * videoHeight) {
                    height = width * videoHeight / videoWidth;
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                width = widthSpecSize;
                height = width * videoHeight / videoWidth;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    height = heightSpecSize;
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                height = heightSpecSize;
                width = height * videoWidth / videoHeight;
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    width = widthSpecSize;
                }
            } else {
                width = videoWidth;
                height = videoHeight;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    height = heightSpecSize;
                    width = height * videoWidth / videoHeight;
                }
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    width = widthSpecSize;
                    height = width * videoHeight / videoWidth;
                }
            }
        } else {
            // no size yet, just adopt the given spec sizes
        }

        if (getRotation() != 0 && getRotation() % 90 == 0) {
            if (widthS < heightS) {
                if (width > height) {
                    width = (int) (width * (float) widthS / height);
                    height = widthS;
                } else {
                    height = (int) (height * (float) width / widthS);
                    width = widthS;
                }
            } else {
                if (width > height) {
                    height = (int) (height * (float) width / widthS);
                    width = widthS;
                } else {
                    width = (int) (width * (float) widthS / height);
                    height = widthS;
                }
            }

            //如果旋转后的高度大于宽度
            if (width > height) {
                //如果视频的旋转后，width（高度）大于控件高度，需要压缩下高度
                if (width > heightS) {
                    width = heightS;
                    height = (int) (height * (float) (width / heightS));
                }
            } else {
                //如果旋转后的宽度大于高度
                if (height > widthS) {
                    height = widthS;
                    width = (int) (width * (float) (height / widthS));
                }
            }
        }
        setMeasuredDimension(width, height);
    }

}
