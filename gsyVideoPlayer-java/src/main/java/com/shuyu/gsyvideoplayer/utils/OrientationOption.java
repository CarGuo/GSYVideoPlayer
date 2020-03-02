package com.shuyu.gsyvideoplayer.utils;

/**
 * 处理屏幕旋转的配置
 */
public class OrientationOption {


    private int mNormalLandAngleStart = 230;
    private int mNormalLandAngleEnd = 310;

    private int mNormalPortraitAngleStart = 30;
    private int mNormalPortraitAngleEnd = 330;

    private int mReverseLandAngleStart = 30;
    private int mReverseLandAngleEnd = 95;


    public int getNormalLandAngleStart() {
        return mNormalLandAngleStart;
    }

    public void setNormalLandAngleStart(int normalLandAngleStart) {
        this.mNormalLandAngleStart = normalLandAngleStart;
    }

    public int getNormalLandAngleEnd() {
        return mNormalLandAngleEnd;
    }

    public void setNormalLandAngleEnd(int normalLandAngleEnd) {
        this.mNormalLandAngleEnd = normalLandAngleEnd;
    }

    public int getNormalPortraitAngleStart() {
        return mNormalPortraitAngleStart;
    }

    public void setNormalPortraitAngleStart(int normalPortraitAngleStart) {
        this.mNormalPortraitAngleStart = normalPortraitAngleStart;
    }

    public int getNormalPortraitAngleEnd() {
        return mNormalPortraitAngleEnd;
    }

    public void setNormalPortraitAngleEnd(int normalPortraitAngleEnd) {
        this.mNormalPortraitAngleEnd = normalPortraitAngleEnd;
    }

    public int getReverseLandAngleStart() {
        return mReverseLandAngleStart;
    }

    public void setReverseLandAngleStart(int reverseLandAngleStart) {
        this.mReverseLandAngleStart = reverseLandAngleStart;
    }

    public int getReverseLandAngleEnd() {
        return mReverseLandAngleEnd;
    }

    public void setReverseLandAngleEnd(int reverseLandAngleEnd) {
        this.mReverseLandAngleEnd = reverseLandAngleEnd;
    }
}
