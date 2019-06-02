package com.example.gsyvideoplayer.utils.floatUtil;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by yhao on 17-11-14.
 * 7.1及以上需申请权限
 */

class FloatPhone extends FloatView {

    private final Context mContext;

    private final WindowManager mWindowManager;
    private final WindowManager.LayoutParams mLayoutParams;
    private View mView;
    private int mX, mY;

    FloatPhone(Context applicationContext) {
        mContext = applicationContext;
        mWindowManager = (WindowManager) applicationContext.getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams();
    }

    @Override
    public void setSize(int width, int height) {
        mLayoutParams.width = width;
        mLayoutParams.height = height;
    }

    @Override
    public void setView(View view) {
        int layout_type;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layout_type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layout_type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.type = layout_type;
        mLayoutParams.windowAnimations = 0;
        mView = view;
    }

    @Override
    public void setGravity(int gravity, int xOffset, int yOffset) {
        mLayoutParams.gravity = gravity;
        mLayoutParams.x = mX = xOffset;
        mLayoutParams.y = mY = yOffset;
    }


    @Override
    public void init() {
        if (Util.hasPermission(mContext)) {
            mLayoutParams.format = PixelFormat.RGBA_8888;
            mWindowManager.addView(mView, mLayoutParams);
        } else {
            FloatActivity.request(mContext, new PermissionListener() {
                @Override
                public void onSuccess() {
                    mLayoutParams.format = PixelFormat.RGBA_8888;
                    mWindowManager.addView(mView, mLayoutParams);
                }

                @Override
                public void onFail() {

                }
            });
        }
    }

    @Override
    public void dismiss() {
        mWindowManager.removeView(mView);
    }

    @Override
    public void updateXY(int x, int y) {
        mLayoutParams.x = mX = x;
        mLayoutParams.y = mY = y;
        mWindowManager.updateViewLayout(mView, mLayoutParams);
    }

    @Override
    void updateX(int x) {
        mLayoutParams.x = mX = x;
        mWindowManager.updateViewLayout(mView, mLayoutParams);

    }

    @Override
    void updateY(int y) {
        mLayoutParams.y = mY = y;
        mWindowManager.updateViewLayout(mView, mLayoutParams);
    }

    @Override
    int getX() {
        return mX;
    }

    @Override
    int getY() {
        return mY;
    }
}
