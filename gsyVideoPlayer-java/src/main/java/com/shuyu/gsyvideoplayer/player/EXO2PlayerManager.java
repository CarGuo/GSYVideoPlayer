package com.shuyu.gsyvideoplayer.player;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Message;
import android.view.Surface;

import com.shuyu.gsyvideoplayer.model.GSYModel;
import com.shuyu.gsyvideoplayer.model.VideoOptionModel;
import com.shuyu.gsyvideoplayer.utils.Debuger;

import java.util.List;

import tv.danmaku.ijk.media.exo2.IjkExo2MediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * EXOPlayer2
 * Created by guoshuyu on 2018/1/11.
 */

public class EXO2PlayerManager implements IPlayerManager {

    private IjkExo2MediaPlayer mediaPlayer;

    private Surface surface;

    @Override
    public IMediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    @Override
    public void initVideoPlayer(Context context, Message msg, List<VideoOptionModel> optionModelList) {
        //目前EXO2在频繁的切换Surface时会可能出现 (queueBuffer: BufferQueue has been abandoned)
        mediaPlayer = new IjkExo2MediaPlayer(context);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(context, Uri.parse(((GSYModel) msg.obj).getUrl()), ((GSYModel) msg.obj).getMapHeadData());
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
            mediaPlayer.setSurface(null);
            if (surface != null) {
                surface.release();
                surface = null;
            }
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
            mediaPlayer.release();
        }
    }
}
