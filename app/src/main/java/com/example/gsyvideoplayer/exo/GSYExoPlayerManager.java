package com.example.gsyvideoplayer.exo;

import android.content.Context;
import android.media.AudioManager;
import android.os.Message;
import android.view.Surface;

import com.google.android.exoplayer2.video.DummySurface;
import com.shuyu.gsyvideoplayer.model.GSYModel;
import com.shuyu.gsyvideoplayer.model.VideoOptionModel;
import com.shuyu.gsyvideoplayer.player.IPlayerManager;

import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by guoshuyu on 2018/5/16.
 * 自定义player管理器，装载自定义exo player，实现无缝切换效果
 */
public class GSYExoPlayerManager implements IPlayerManager {

    private GSYExo2MediaPlayer mediaPlayer;

    private Surface surface;

    private DummySurface dummySurface;

    @Override
    public IMediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    @Override
    public void initVideoPlayer(Context context, Message msg, List<VideoOptionModel> optionModelList) {
        mediaPlayer = new GSYExo2MediaPlayer(context);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        if (dummySurface == null) {
            dummySurface = DummySurface.newInstanceV17(context, false);
        }
        try {
            mediaPlayer.setLooping(((GSYModel) msg.obj).isLooping());
            mediaPlayer.setDataSource(((GSYExoModel) msg.obj).getUrls(), ((GSYModel) msg.obj).getMapHeadData());
            //很遗憾，EXO2的setSpeed只能在播放前生效
            if (((GSYModel) msg.obj).getSpeed() != 1 && ((GSYModel) msg.obj).getSpeed() > 0) {
                mediaPlayer.setSpeed(((GSYModel) msg.obj).getSpeed(), 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showDisplay(Message msg) {
        if (mediaPlayer == null) {
            return;
        }
        if (msg.obj == null) {
            mediaPlayer.setSurface(dummySurface);
        } else {
            Surface holder = (Surface) msg.obj;
            surface = holder;
            mediaPlayer.setSurface(holder);
        }
    }

    @Override
    public void setSpeed(float speed, boolean soundTouch) {
        //很遗憾，EXO2的setSpeed只能在播放前生效
        //Debuger.printfError("很遗憾，目前EXO2的setSpeed只能在播放前设置生效");
        try {
            mediaPlayer.setSpeed(speed, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setNeedMute(boolean needMute) {
        if(mediaPlayer != null) {
            if (needMute) {
                mediaPlayer.setVolume(0, 0);
            } else {
                mediaPlayer.setVolume(1, 1);
            }
        }
    }


    @Override
    public void releaseSurface() {

    }

    @Override
    public void release() {
        if(mediaPlayer != null) {
            mediaPlayer.setSurface(null);
            mediaPlayer.release();
        }
        if (dummySurface != null) {
            dummySurface.release();
            dummySurface = null;
        }
    }


    /**
     * 上一集
     */
    public void previous() {
        if (mediaPlayer == null) {
            return;
        }
        mediaPlayer.previous();
    }

    /**
     * 下一集
     */
    public void next() {
        if (mediaPlayer == null) {
            return;
        }
        mediaPlayer.next();
    }
}
