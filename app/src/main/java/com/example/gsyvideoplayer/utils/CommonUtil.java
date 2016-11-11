package com.example.gsyvideoplayer.utils;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by shuyu on 2016/11/11.
 */

public class CommonUtil {

    public static void setViewHeight(View view, int width, int height) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (null == layoutParams)
            return;
        layoutParams.width = width;
        layoutParams.height = height;
        view.setLayoutParams(layoutParams);
    }


}
