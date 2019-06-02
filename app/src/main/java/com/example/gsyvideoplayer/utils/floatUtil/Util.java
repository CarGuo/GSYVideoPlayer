package com.example.gsyvideoplayer.utils.floatUtil;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import java.lang.reflect.Method;

/**
 * Created by yhao on 2017/12/22.
 * https://github.com/yhaolpz
 */

public class Util {


    static View inflate(Context applicationContext, int layoutId) {
        LayoutInflater inflate = (LayoutInflater) applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflate.inflate(layoutId, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean hasPermission(Context context) {
        return Settings.canDrawOverlays(context);
    }


    private static Point sPoint;

    static int getScreenWidth(Context context) {
        if (sPoint == null) {
            sPoint = new Point();
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getSize(sPoint);
        }
        return sPoint.x;
    }

    static int getScreenHeight(Context context) {
        if (sPoint == null) {
            sPoint = new Point();
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getSize(sPoint);
        }
        return sPoint.y;
    }
}
