package com.example.gsyvideoplayer.utils.floatUtil;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by yhao on 2017/12/23.
 * https://github.com/yhaolpz
 */

public class Screen {
    public static final int width = 0;
    public static final int height = 1;

    @IntDef({width, height})
    @Retention(RetentionPolicy.SOURCE)
    @interface screenType {
    }
}
