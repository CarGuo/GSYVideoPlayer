package com.shuyu.gsyvideoplayer.utils;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by shuyu on 2016/11/23.
 */

public class Debuger {
    
    static final String LOG_TAG = "GSYVideoPlayer";
    
    static boolean DEBUG_TAG = false;

    public static void enable() {
        DEBUG_TAG = true;
    }

    public static void disable() {
        DEBUG_TAG = false;
    }

    public static boolean getDebugMode() {
        return DEBUG_TAG;
    }

    public static void printfLog(String tag, String log) {
        if (DEBUG_TAG && log != null) {
            if (!TextUtils.isEmpty(log))
                Log.i(tag, log);
        }
    }

    public static void printfLog(String log) {
        printfLog(LOG_TAG, log);
    }

    public static void printfWarning(String tag, String log) {
        if (DEBUG_TAG && log != null) {
            if (!TextUtils.isEmpty(log))
                Log.w(tag, log);
        }
    }

    public static void printfWarning(String log) {
        printfWarning(LOG_TAG, log);
    }

    public static void printfError(String log) {
        if (DEBUG_TAG) {
            if (!TextUtils.isEmpty(log))
                Log.e(LOG_TAG, log);
        }
    }

    public static void printfError(String Tag, String log) {
        if (DEBUG_TAG) {
            if (!TextUtils.isEmpty(log))
                Log.e(Tag, log);
        }
    }

    public static void printfError(String log, Exception e) {
        if (DEBUG_TAG) {
            if (!TextUtils.isEmpty(log))
                Log.e(LOG_TAG, log);
            e.printStackTrace();
        }
    }

    public static void Toast(Activity activity, String log) {
        if (DEBUG_TAG) {
            if (!TextUtils.isEmpty(log))
                Toast.makeText(activity, log, Toast.LENGTH_SHORT).show();
        }
    }
}
