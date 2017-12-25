package com.example.gsyvideoplayer.utils.floatUtil;

import android.content.Context;
import android.graphics.Point;
import android.provider.Settings;
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

    public static boolean hasPermission(Context context) {
        Boolean result;
        try {
            Class clazz = Settings.class;
            Method canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context.class);
            result = (Boolean) canDrawOverlays.invoke(null, context);
        } catch (Exception e) {
            result = false;
        }
        return result;
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
