package com.shuyu.gsyvideoplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * 用于显示video的，做了横屏与竖屏的匹配，还有需要rotation需求的
 * Created by shuyu on 2016/11/11.
 */

public class GSYTextureView extends TextureView {
    public GSYTextureView(Context context) {
        super(context);
    }

    public GSYTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
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
        }
        setMeasuredDimension(width, height);
    }
}