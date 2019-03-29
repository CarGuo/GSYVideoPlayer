package com.example.gsyvideoplayer.utils.floatUtil;

import android.animation.TimeInterpolator;
import android.content.Context;
import androidx.annotation.LayoutRes;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yhao on 2017/12/22.
 * https://github.com/yhaolpz
 */

public class FloatWindow {

    private FloatWindow() {

    }

    private static final String mDefaultTag = "default_float_window_tag";
    private static Map<String, IFloatWindow> mFloatWindowMap;

    public static IFloatWindow get() {
        return get(mDefaultTag);
    }

    public static IFloatWindow get(@NonNull String tag) {
        return mFloatWindowMap == null ? null : mFloatWindowMap.get(tag);
    }

    private static B mBuilder = null;

    @MainThread
    public static B with(@NonNull Context applicationContext) {
        return mBuilder = new B(applicationContext);
    }

    public static void destroy() {
        destroy(mDefaultTag);
    }

    public static void destroy(String tag) {
        if (mFloatWindowMap == null || !mFloatWindowMap.containsKey(tag)) {
            return;
        }
        mFloatWindowMap.get(tag).dismiss();
        mFloatWindowMap.remove(tag);
    }

    public static class B {
        Context mApplicationContext;
        View mView;
        private int mLayoutId;
        int mWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
        int mHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
        int gravity = Gravity.TOP | Gravity.START;
        int xOffset;
        int yOffset;
        boolean mShow = true;
        Class[] mActivities;
        int mMoveType = MoveType.fixed;
        long mDuration = 300;
        TimeInterpolator mInterpolator;
        private String mTag = mDefaultTag;

        private B() {

        }

        B(Context applicationContext) {
            mApplicationContext = applicationContext;
        }

        public B setView(@NonNull View view) {
            mView = view;
            return this;
        }

        public B setView(@LayoutRes int layoutId) {
            mLayoutId = layoutId;
            return this;
        }

        public B setWidth(int width) {
            mWidth = width;
            return this;
        }

        public B setHeight(int height) {
            mHeight = height;
            return this;
        }

        public B setWidth(@Screen.screenType int screenType, float ratio) {
            mWidth = (int) ((screenType == Screen.width ?
                    Util.getScreenWidth(mApplicationContext) :
                    Util.getScreenHeight(mApplicationContext)) * ratio);
            return this;
        }


        public B setHeight(@Screen.screenType int screenType, float ratio) {
            mHeight = (int) ((screenType == Screen.width ?
                    Util.getScreenWidth(mApplicationContext) :
                    Util.getScreenHeight(mApplicationContext)) * ratio);
            return this;
        }


        public B setX(int x) {
            xOffset = x;
            return this;
        }

        public B setY(int y) {
            yOffset = y;
            return this;
        }

        public B setX(@Screen.screenType int screenType, float ratio) {
            xOffset = (int) ((screenType == Screen.width ?
                    Util.getScreenWidth(mApplicationContext) :
                    Util.getScreenHeight(mApplicationContext)) * ratio);
            return this;
        }

        public B setY(@Screen.screenType int screenType, float ratio) {
            yOffset = (int) ((screenType == Screen.width ?
                    Util.getScreenWidth(mApplicationContext) :
                    Util.getScreenHeight(mApplicationContext)) * ratio);
            return this;
        }


        /**
         * 设置 Activity 过滤器，用于指定在哪些界面显示悬浮窗，默认全部界面都显示
         *
         * @param show       　过滤类型,子类类型也会生效
         * @param activities 　过滤界面
         */
        public B setFilter(boolean show, @NonNull Class... activities) {
            mShow = show;
            mActivities = activities;
            return this;
        }


        public B setMoveType(@MoveType.MOVE_TYPE int moveType) {
            mMoveType = moveType;
            return this;
        }

        public B setMoveStyle(long duration, @Nullable TimeInterpolator interpolator) {
            mDuration = duration;
            mInterpolator = interpolator;
            return this;
        }

        public B setTag(@NonNull String tag) {
            mTag = tag;
            return this;
        }

        public void build() {
            if (mFloatWindowMap == null) {
                mFloatWindowMap = new HashMap<>();
            }
            if (mFloatWindowMap.containsKey(mTag)) {
                throw new IllegalArgumentException("FloatWindow of this tag has been added, Please set a new tag for the new FloatWindow");
            }
            if (mView == null && mLayoutId == 0) {
                throw new IllegalArgumentException("View has not been set!");
            }
            if (mView == null) {
                mView = Util.inflate(mApplicationContext, mLayoutId);
            }
            IFloatWindow floatWindowImpl = new IFloatWindowImpl(this);
            mFloatWindowMap.put(mTag, floatWindowImpl);
        }

    }
}
