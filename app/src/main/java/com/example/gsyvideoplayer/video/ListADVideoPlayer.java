package com.example.gsyvideoplayer.video;

import android.content.Context;
import android.util.AttributeSet;

import com.shuyu.gsyvideoplayer.listener.GSYMediaPlayerListener;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.video.GSYADVideoPlayer;

public class ListADVideoPlayer extends GSYADVideoPlayer {

    public ListADVideoPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public ListADVideoPlayer(Context context) {
        super(context);
    }

    public ListADVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void startPrepare() {
        GSYMediaPlayerListener listener = getGSYVideoManager().listener();
        super.startPrepare();
        if (listener != null) {
            listener.onAutoCompletion();
        }
    }

    @Override
    public void onAutoCompletion() {
        super.onAutoCompletion();
        if (!isCurrentMediaListener() && mVideoAllCallBack != null) {
            Debuger.printfLog("onAutoComplete");
            mVideoAllCallBack.onAutoComplete(mOriginUrl, mTitle, this);
        }
    }

    @Override
    public void onCompletion() {
        super.onCompletion();
    }
}
