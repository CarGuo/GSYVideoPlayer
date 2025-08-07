package com.example.gsyvideoplayer.video;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.example.gsyvideoplayer.R;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.utils.NetworkUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.util.Timer;
import java.util.TimerTask;

import static com.shuyu.gsyvideoplayer.utils.CommonUtil.hideNavKey;

/**
 * 多窗体下的悬浮窗页面支持Video
 * Created by shuyu on 2017/12/25.
 */

public class FloatingVideo extends StandardGSYVideoPlayer {

    protected Timer mDismissControlViewTimer;

    /**
     * 1.5.0开始加入，如果需要不同布局区分功能，需要重载
     */
    public FloatingVideo(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public FloatingVideo(Context context) {
        super(context);
    }

    public FloatingVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        if (getActivityContext() != null) {
            this.mContext = getActivityContext();
        } else {
            this.mContext = context;
        }

        initInflate(mContext);

        mTextureViewContainer = (ViewGroup) findViewById(R.id.surface_container);
        mStartButton = findViewById(R.id.start);

        if (isInEditMode())
            return;
        mScreenWidth = getActivityContext().getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getActivityContext().getResources().getDisplayMetrics().heightPixels;
        // AudioManager is now handled by the base class GSYAudioFocusManager
        mStartButton = findViewById(com.shuyu.gsyvideoplayer.R.id.start);
        mStartButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickStartIcon();
            }
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_floating_video;
    }


    @Override
    protected void startPrepare() {
        if (getGSYVideoManager().listener() != null) {
            getGSYVideoManager().listener().onCompletion();
        }
        getGSYVideoManager().setListener(this);
        getGSYVideoManager().setPlayTag(mPlayTag);
        getGSYVideoManager().setPlayPosition(mPlayPosition);
        // Audio focus is now handled by the base class GSYAudioFocusManager
        //((Activity) getActivityContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mBackUpPlayingBufferState = -1;
        getGSYVideoManager().prepare(mUrl, mMapHeadData, mLooping, mSpeed, mCache, mCachePath, null);
        setStateAndUi(CURRENT_STATE_PREPAREING);
    }

    @Override
    public void onAutoCompletion() {
        setStateAndUi(CURRENT_STATE_AUTO_COMPLETE);

        mSaveChangeViewTIme = 0;

        if (mTextureViewContainer.getChildCount() > 0) {
            mTextureViewContainer.removeAllViews();
        }

        if (!mIfCurrentIsFullscreen)
            getGSYVideoManager().setLastListener(null);
        // Audio focus is now handled by the base class GSYAudioFocusManager
        //((Activity) getActivityContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        releaseNetWorkState();

        if (mVideoAllCallBack != null && isCurrentMediaListener()) {
            Debuger.printfLog("onAutoComplete");
            mVideoAllCallBack.onAutoComplete(mOriginUrl, mTitle, this);
        }
    }

    @Override
    public void onCompletion() {
        //make me normal first
        setStateAndUi(CURRENT_STATE_NORMAL);

        mSaveChangeViewTIme = 0;

        if (mTextureViewContainer.getChildCount() > 0) {
            mTextureViewContainer.removeAllViews();
        }

        if (!mIfCurrentIsFullscreen) {
            getGSYVideoManager().setListener(null);
            getGSYVideoManager().setLastListener(null);
        }
        getGSYVideoManager().setCurrentVideoHeight(0);
        getGSYVideoManager().setCurrentVideoWidth(0);

        // Audio focus is now handled by the base class GSYAudioFocusManager
        //((Activity) getActivityContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        releaseNetWorkState();

    }


    @Override
    protected Context getActivityContext() {
        return getContext();
    }


    @Override
    protected boolean isShowNetConfirm() {
        return false;
    }

    @Override
    protected void showWifiDialog() {
        if (!NetworkUtils.isAvailable(mContext)) {
            //Toast.makeText(mContext, getResources().getString(R.string.no_net), Toast.LENGTH_LONG).show();
            startPlayLogic();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivityContext());
        builder.setMessage(getResources().getString(com.shuyu.gsyvideoplayer.R.string.tips_not_wifi));
        builder.setPositiveButton(getResources().getString(com.shuyu.gsyvideoplayer.R.string.tips_not_wifi_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startPlayLogic();
            }
        });
        builder.setNegativeButton(getResources().getString(com.shuyu.gsyvideoplayer.R.string.tips_not_wifi_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog =  builder.create();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        } else {
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        alertDialog.show();
    }
}
