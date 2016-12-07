package com.shuyu.gsyvideoplayer.utils;

/**
 * Created by shuyu on 2016/12/7.
 */

public class CommonType {

    public final static int SCREEN_TYPE_DEFAULT = 0;

    public final static int SCREEN_TYPE_16_9 = 1;

    public final static int SCREEN_TYPE_4_3 = 2;

    private static int TYPE = SCREEN_TYPE_DEFAULT;

    public static int getShowType() {
        return TYPE;
    }

    /**
     * 设置显示比例
     */
    public static void setShowType(int type) {
        TYPE = type;
    }
}
