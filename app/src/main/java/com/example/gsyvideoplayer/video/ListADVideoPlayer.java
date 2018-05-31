package com.example.gsyvideoplayer.video;

import android.content.Context;
import android.util.AttributeSet;

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
        if (getGSYVideoManager().listener() != null) {
            getGSYVideoManager().listener().onAutoCompletion();
        }
        super.startPrepare();
    }

    @Override
    public void onCompletion() {
        super.onCompletion();
    }
}
