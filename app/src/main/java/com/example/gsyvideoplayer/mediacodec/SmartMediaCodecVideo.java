package com.example.gsyvideoplayer.mediacodec;

import android.content.Context;
import android.util.AttributeSet;

import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

public class SmartMediaCodecVideo extends StandardGSYVideoPlayer {

    private SmartMediaCodecEventListener smartMediaCodecEventListener;

    public SmartMediaCodecVideo(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public SmartMediaCodecVideo(Context context) {
        super(context);
    }

    public SmartMediaCodecVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSmartMediaCodecEventListener(SmartMediaCodecEventListener listener) {
        smartMediaCodecEventListener = listener;
    }

    @Override
    public void onPrepared() {
        super.onPrepared();
        if (smartMediaCodecEventListener != null) {
            smartMediaCodecEventListener.onPrepared();
        }
    }

    @Override
    public void onInfo(int what, int extra) {
        super.onInfo(what, extra);
        if (smartMediaCodecEventListener != null) {
            smartMediaCodecEventListener.onInfo(what, extra);
        }
    }

    @Override
    public void onError(int what, int extra) {
        if (smartMediaCodecEventListener != null) {
            smartMediaCodecEventListener.onError(what, extra);
        }
        super.onError(what, extra);
    }

    @Override
    public void onAutoCompletion() {
        super.onAutoCompletion();
        if (smartMediaCodecEventListener != null) {
            smartMediaCodecEventListener.onAutoCompletion();
        }
    }

    public interface SmartMediaCodecEventListener {
        void onPrepared();

        void onInfo(int what, int extra);

        void onError(int what, int extra);

        void onAutoCompletion();
    }
}
