package com.example.gsyvideoplayer.exo;

import android.content.Context;
import android.media.AudioManager;
import android.os.Message;
import android.view.Surface;

import com.google.android.exoplayer2.video.DummySurface;
import com.shuyu.gsyvideoplayer.cache.ICacheManager;
import com.shuyu.gsyvideoplayer.model.VideoOptionModel;
import com.shuyu.gsyvideoplayer.player.BasePlayerManager;
import com.shuyu.gsyvideoplayer.utils.Debuger;

import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by guoshuyu on 2018/5/16.
 * 自定义player管理器，装载自定义exo player，实现无缝切换效果
 */
public class GSYExoPlayerManager extends BasePlayerManager {

    private GSYExo2MediaPlayer mediaPlayer;

    private Surface surface;

    private DummySurface dummySurface;

    @Override
    public IMediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    @Override
    public void initVideoPlayer(Context context, Message msg, List<VideoOptionModel> optionModelList, ICacheManager cacheManager) {
        mediaPlayer = new GSYExo2MediaPlayer(context);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        if (dummySurface == null) {
            dummySurface = DummySurface.newInstanceV17(context, false);
        }
        try {
            mediaPlayer.setLooping(((GSYExoModel) msg.obj).isLooping());
            Debuger.printfError("###### " + ((GSYExoModel) msg.obj).getOverrideExtension());
            mediaPlayer.setOverrideExtension(((GSYExoModel) msg.obj).getOverrideExtension());
            mediaPlayer.setDataSource(((GSYExoModel) msg.obj).getUrls(), ((GSYExoModel) msg.obj).getMapHeadData(), ((GSYExoModel) msg.obj).index, ((GSYExoModel) msg.obj).isCache());
            //很遗憾，EXO2的setSpeed只能在播放前生效
            if (((GSYExoModel) msg.obj).getSpeed() != 1 && ((GSYExoModel) msg.obj).getSpeed() > 0) {
                mediaPlayer.setSpeed(((GSYExoModel) msg.obj).getSpeed(), 1);
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
        if (mediaPlayer != null) {
            try {
                mediaPlayer.setSpeed(speed, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setNeedMute(boolean needMute) {
        if (mediaPlayer != null) {
            if (needMute) {
                mediaPlayer.setVolume(0, 0);
            } else {
                mediaPlayer.setVolume(1, 1);
            }
        }
    }


    @Override
    public void releaseSurface() {
        if (surface != null) {
            //surface.release();
            surface = null;
        }
    }

    @Override
    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.setSurface(null);
            mediaPlayer.release();
        }
        if (dummySurface != null) {
            dummySurface.release();
            dummySurface = null;
        }
    }

    @Override
    public int getBufferedPercentage() {
        return -1;
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

    @Override
    public long getNetSpeed() {
        return 0;
    }

    @Override
    public void setSpeedPlaying(float speed, boolean soundTouch) {

    }

    @Override
    public void start() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    @Override
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    @Override
    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    @Override
    public int getVideoWidth() {
        if (mediaPlayer != null) {
            return mediaPlayer.getVideoWidth();
        }
        return 0;
    }

    @Override
    public int getVideoHeight() {
        if (mediaPlayer != null) {
            return mediaPlayer.getVideoHeight();
        }
        return 0;
    }

    @Override
    public boolean isPlaying() {
        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        }
        return false;
    }

    @Override
    public void seekTo(long time) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(time);
        }
    }

    @Override
    public long getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public long getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public int getVideoSarNum() {
        if (mediaPlayer != null) {
            return mediaPlayer.getVideoSarNum();
        }
        return 1;
    }

    @Override
    public int getVideoSarDen() {
        if (mediaPlayer != null) {
            return mediaPlayer.getVideoSarDen();
        }
        return 1;
    }

    @Override
    public boolean isSurfaceSupportLockCanvas() {
        return false;
    }
}
