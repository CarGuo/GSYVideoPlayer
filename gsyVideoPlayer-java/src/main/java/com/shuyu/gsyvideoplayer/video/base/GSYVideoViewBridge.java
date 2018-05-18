package com.shuyu.gsyvideoplayer.video.base;

import android.content.Context;
import android.view.Surface;

import com.shuyu.gsyvideoplayer.listener.GSYMediaPlayerListener;

import java.io.File;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Manager 与 View之间的接口
 * Created by guoshuyu on 2018/1/25.
 */

public interface GSYVideoViewBridge {

    GSYMediaPlayerListener listener();

    GSYMediaPlayerListener lastListener();

    void setListener(GSYMediaPlayerListener listener);

    void setLastListener(GSYMediaPlayerListener lastListener);

    String getPlayTag();

    void setPlayTag(String playTag);

    int getPlayPosition();

    void setPlayPosition(int playPosition);

    void prepare(final String url, final Map<String, String> mapHeadData, boolean loop, float speed, boolean cache, File cachePath);

    IMediaPlayer getMediaPlayer();

    void releaseMediaPlayer();

    void setCurrentVideoHeight(int currentVideoHeight);

    void setCurrentVideoWidth(int currentVideoWidth);

    int getCurrentVideoWidth();

    int getCurrentVideoHeight();

    void setDisplay(Surface holder);

    void releaseSurface(Surface surface);

    void setSpeed(float speed, boolean soundTouch);

    int getLastState();

    void setLastState(int lastState);

    boolean isCacheFile();

    void clearCache(Context context, String url);

}
