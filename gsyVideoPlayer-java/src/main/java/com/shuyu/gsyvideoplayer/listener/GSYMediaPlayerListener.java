package com.shuyu.gsyvideoplayer.listener;

public interface GSYMediaPlayerListener {
    void onPrepared();

    void onAutoCompletion();

    void onCompletion();

    void onBufferingUpdate(int percent);

    void onSeekComplete();

    void onError(int what, int extra);

    void onInfo(int what, int extra);

    void onVideoSizeChanged();

    void onBackFullscreen();

    void onVideoPause();

    void onVideoResume();
}
