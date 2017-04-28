package com.shuyu.gsyvideoplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.shuyu.gsyvideoplayer.utils.GSYVideoType;

/**
 * Created by shuyu on 2016/12/6.
 */

@SuppressLint("AppCompatCustomView")
public class GSYImageCover extends ImageView {


    private boolean fullView;

    private int originW, originH;

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

        if (originW == 0 || originH == 0) {
            originW = widthS;
            originH = heightS;
        }

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

        boolean rotate = (getRotation() != 0 && getRotation() % 90 == 0 && Math.abs(getRotation()) != 180);
        if (rotate) {
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
                if (originH < originW) {
                    if (width > heightS) {
                        height = (int) (height * ((float) width / heightS));
                        width = heightS;
                    }
                } else {
                    if (width > heightS) {
                        height = (int) (height / ((float) width / heightS));
                        width = heightS;
                    }
                }
            } else {
                //如果旋转后的宽度大于高度
                if (height > widthS) {
                    width = (int) (width * ((float) height / widthS));
                    height = widthS;
                }
            }
        }

        //如果设置了比例
        if (GSYVideoType.getShowType() == GSYVideoType.SCREEN_TYPE_16_9) {
            if (height > width) {
                width = height * 9 / 16;
            } else {
                height = width * 9 / 16;
            }
        } else if (GSYVideoType.getShowType() == GSYVideoType.SCREEN_TYPE_4_3) {
            if (height > width) {
                width = height * 3 / 4;
            } else {
                height = width * 3 / 4;
            }
        }

        fullView = (GSYVideoType.getShowType() == GSYVideoType.SCREEN_TYPE_FULL);

        //上面会调整一变全屏，这里如果要全屏裁减，就整另外一边
        if (fullView) {
            if (rotate && getRotation() != 0) {
                if (width > height) {
                    if (height < originW) {
                        width = (int) (width * ((float) originW / height));
                        height = originW;
                    } else if (width < originH) {
                        height = (int) (height * ((float) originH / width));
                        width = originH;
                    }
                } else {
                    if (width < originH) {
                        height = (int) (height * ((float) originH / width));
                        width = originH;
                    } else if (height < originW) {
                        width = (int) (width * ((float) originW / height));
                        height = originW;
                    }
                }
            } else {
                if (height > width) {
                    if (width < widthS) {
                        height = (int) (height * ((float) widthS / width));
                        width = widthS;
                    } else {
                        width = (int) (width * ((float) heightS / height));
                        height = heightS;
                    }
                } else {
                    if (height < heightS) {
                        width = (int) (width * ((float) heightS / height));
                        height = heightS;
                    } else {
                        height = (int) (height * ((float) widthS / width));
                        width = widthS;
                    }
                }
            }
        }

        setMeasuredDimension(width, height);
    }

}
