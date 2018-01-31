package com.example.gsyvideoplayer.video;

import android.content.Context;
import android.media.AudioManager;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.danikula.videocache.HttpProxyCacheServer;
import com.example.gsyvideoplayer.video.manager.CustomManager;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoViewBridge;

import java.io.File;

import tv.danmaku.ijk.media.player.IjkLibLoader;

/**
 * 多个同时播放的播放控件
 * Created by guoshuyu on 2018/1/31.
 */

public class MultiSampleVideo extends SampleCoverVideo {

    private final static String TAG = "MultiSampleVideo";

    public MultiSampleVideo(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public MultiSampleVideo(Context context) {
        super(context);
    }

    public MultiSampleVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        post(new Runnable() {
                            @Override
                            public void run() {
                                //DO NOTHING NO STOP
                            }
                        });
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        post(new Runnable() {
                            @Override
                            public void run() {
                                //DO NOTHING NO STOP
                            }
                        });
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        break;
                }
            }
        };
    }

    @Override
    public void setIjkLibLoader(IjkLibLoader libLoader) {

    }

    @Override
    public GSYVideoViewBridge getGSYVideoManager() {
        return CustomManager.getCustomManager(getKey());
    }

    @Override
    protected boolean backFromFull(Context context) {
        return CustomManager.backFromWindowFull(context, getKey());
    }

    @Override
    protected void releaseVideos() {
        CustomManager.releaseAllVideos(getKey());
    }

    @Override
    protected HttpProxyCacheServer getProxy(Context context, File file) {
        return null;
    }

    public String getKey() {
        if (mPlayPosition == -22) {
            throw new IllegalStateException("PlayPosition never set.");
        }
        if (TextUtils.isEmpty(mPlayTag)) {
            throw new IllegalStateException("PlayTag never set.");
        }
        return TAG + mPlayPosition + mPlayTag;
    }
}
