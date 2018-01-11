package com.shuyu.gsyvideoplayer.player;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Message;
import android.view.Surface;

import com.shuyu.gsyvideoplayer.model.GSYModel;
import com.shuyu.gsyvideoplayer.model.VideoOptionModel;
import com.shuyu.gsyvideoplayer.utils.Debuger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import tv.danmaku.ijk.media.exo.IjkExoMediaPlayer;
import tv.danmaku.ijk.media.exo.demo.player.DemoPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * EXOPlayer1
 * Created by guoshuyu on 2018/1/11.
 */
@Deprecated
public class EXOPlayerManager implements IPlayerManager {

    private IjkExoMediaPlayer mediaPlayer;

    @Override
    public IMediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    @Override
    public void initVideoPlayer(Context context, Message msg, List<VideoOptionModel> optionModelList) {
        mediaPlayer = new IjkExoMediaPlayer(context);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(context, Uri.parse(((GSYModel) msg.obj).getUrl()), ((GSYModel) msg.obj).getMapHeadData());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showDisplay(Message msg) {
        if (mediaPlayer == null) {
            return;
        }
        Class<?> classType = mediaPlayer.getClass();
        DemoPlayer demoPlayer = null;
        try {
            Field field = classType.getDeclaredField("mInternalPlayer");
            field.setAccessible(true); // 抑制Java对修饰符的检查
            demoPlayer = (DemoPlayer) field.get(mediaPlayer);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (msg.obj == null) {
            /*if (demoPlayer != null && demoPlayer.getPlayWhenReady()) {
                demoPlayer.setSelectedTrack(0, -1);
            }*/
            mediaPlayer.setSurface(null);
        } else {
            Surface holder = (Surface) msg.obj;
            mediaPlayer.setSurface(holder);
            if (mediaPlayer != null && mediaPlayer.getDuration() > 30
                    && mediaPlayer.getCurrentPosition() < mediaPlayer.getDuration()) {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 20);
            }
            /*if (mediaPlayer != null && holder.isValid()) {
                if (demoPlayer != null && demoPlayer.getPlayWhenReady()) {
                    demoPlayer.setSelectedTrack(0, 0);
                }

            }*/
        }
    }

    @Override
    public void setSpeed(float speed, boolean soundTouch) {
        Debuger.printfError("EXOPlayer1 not support setSpeed, Please User EXOPlayer2");
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
    public void release() {
        if(mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}