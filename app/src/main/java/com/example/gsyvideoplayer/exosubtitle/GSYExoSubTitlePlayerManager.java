package com.example.gsyvideoplayer.exosubtitle;

import android.content.Context;
import android.media.AudioManager;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Message;
import android.view.Surface;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.SeekParameters;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.video.DummySurface;
import com.shuyu.gsyvideoplayer.cache.ICacheManager;
import com.shuyu.gsyvideoplayer.model.GSYModel;
import com.shuyu.gsyvideoplayer.model.VideoOptionModel;
import com.shuyu.gsyvideoplayer.player.BasePlayerManager;
import com.shuyu.gsyvideoplayer.utils.Debuger;

import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by guoshuyu on 2018/5/16.
 * 自定义player管理器，装载自定义exo player，实现无缝切换效果
 */
public class GSYExoSubTitlePlayerManager extends BasePlayerManager {

    private Context context;

    private GSYExoSubTitlePlayer mediaPlayer;

    private Surface surface;

    private DummySurface dummySurface;

    private long lastTotalRxBytes = 0;

    private long lastTimeStamp = 0;

    @Override
    public IMediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    @Override
    public void initVideoPlayer(Context context, Message msg, List<VideoOptionModel> optionModelList, ICacheManager cacheManager) {
        this.context = context.getApplicationContext();
        mediaPlayer = new GSYExoSubTitlePlayer(context);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        if (dummySurface == null) {
            dummySurface = DummySurface.newInstanceV17(context, false);
        }
        //使用自己的cache模式
        GSYExoSubTitleModel gsyModel = (GSYExoSubTitleModel) msg.obj;
        try {
            mediaPlayer.setLooping(gsyModel.isLooping());
            if (gsyModel.getSubTitle() != null) {
                mediaPlayer.setSubTitile(gsyModel.getSubTitle());
            }
            mediaPlayer.setPreview(gsyModel.getMapHeadData() != null && gsyModel.getMapHeadData().size() > 0);
            if (gsyModel.isCache() && cacheManager != null) {
                //通过管理器处理
                cacheManager.doCacheLogic(context, mediaPlayer, gsyModel.getUrl(), gsyModel.getMapHeadData(), gsyModel.getCachePath());
            } else {
                //通过自己的内部缓存机制
                mediaPlayer.setCache(gsyModel.isCache());
                mediaPlayer.setCacheDir(gsyModel.getCachePath());
                mediaPlayer.setOverrideExtension(gsyModel.getOverrideExtension());
                mediaPlayer.setDataSource(context, Uri.parse(gsyModel.getUrl()), gsyModel.getMapHeadData());
            }
            if (gsyModel.getSpeed() != 1 && gsyModel.getSpeed() > 0) {
                mediaPlayer.setSpeed(gsyModel.getSpeed(), 1);
            }
            if (gsyModel.getTextOutput() != null) {
                mediaPlayer.setTextOutput(gsyModel.getTextOutput());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        initSuccess(gsyModel);
    }

    @Override
    public void showDisplay(final Message msg) {
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
    public void setSpeed(final float speed, final boolean soundTouch) {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.setSpeed(speed, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setNeedMute(final boolean needMute) {
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
        lastTotalRxBytes = 0;
        lastTimeStamp = 0;
    }

    @Override
    public int getBufferedPercentage() {
        if (mediaPlayer != null) {
            return mediaPlayer.getBufferedPercentage();
        }
        return 0;
    }

    @Override
    public long getNetSpeed() {
        if (mediaPlayer != null) {
            return getNetSpeed(context);
        }
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

    public void addTextOutputPlaying(TextOutput textOutput) {
        if(mediaPlayer != null) {
            mediaPlayer.addTextOutputPlaying(textOutput);
        }
    }

    public void removeTextOutput(TextOutput textOutput) {
        if(mediaPlayer != null) {
            mediaPlayer.removeTextOutput(textOutput);
        }
    }

    /**
     * 设置seek 的临近帧。
     **/
    public void setSeekParameter(@Nullable SeekParameters seekParameters) {
        if (mediaPlayer != null) {
            mediaPlayer.setSeekParameter(seekParameters);
        }
    }


    private long getNetSpeed(Context context) {
        if (context == null) {
            return 0;
        }
        long nowTotalRxBytes = TrafficStats.getUidRxBytes(context.getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);//转为KB
        long nowTimeStamp = System.currentTimeMillis();
        long calculationTime = (nowTimeStamp - lastTimeStamp);
        if (calculationTime == 0) {
            return calculationTime;
        }
        //毫秒转换
        long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / calculationTime);
        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;
        return speed;
    }
}
