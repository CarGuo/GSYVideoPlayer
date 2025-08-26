package tv.danmaku.ijk.media.exo2;

import android.content.Context;
import android.media.AudioManager;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Message;
import android.view.Surface;

import androidx.annotation.Nullable;
import androidx.media3.exoplayer.SeekParameters;
import androidx.media3.exoplayer.video.PlaceholderSurface;

import com.shuyu.gsyvideoplayer.cache.ICacheManager;
import com.shuyu.gsyvideoplayer.model.GSYModel;
import com.shuyu.gsyvideoplayer.model.VideoOptionModel;
import com.shuyu.gsyvideoplayer.player.BasePlayerManager;

import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * EXOPlayer2
 * Created by guoshuyu on 2018/1/11.
 */

public class Exo2PlayerManager extends BasePlayerManager {

    private Context context;

    private IjkExo2MediaPlayer mediaPlayer;

    private Surface surface;

    private PlaceholderSurface dummySurface;
    
    private SurfaceControlHelper.SurfaceSwitcher surfaceSwitcher;

    private long lastTotalRxBytes = 0;

    private long lastTimeStamp = 0;

    @Override
    public IMediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    @Override
    public void initVideoPlayer(Context context, Message msg, List<VideoOptionModel> optionModelList, ICacheManager cacheManager) {
        this.context = context.getApplicationContext();
        mediaPlayer = new IjkExo2MediaPlayer(context);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        if (dummySurface == null) {
            dummySurface = PlaceholderSurface.newInstance(context, false);
        }
        
        // Initialize SurfaceControl helper for improved surface switching
        if (surfaceSwitcher != null) {
            surfaceSwitcher.release();
        }
        surfaceSwitcher = SurfaceControlHelper.createSurfaceSwitcher(mediaPlayer);
        
        //使用自己的cache模式
        GSYModel gsyModel = (GSYModel) msg.obj;
        try {
            mediaPlayer.setLooping(gsyModel.isLooping());
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
            // Switch to dummy surface using SurfaceControl helper
            if (surfaceSwitcher != null) {
                surfaceSwitcher.switchToSurface(dummySurface);
            } else {
                // Fallback to standard method
                mediaPlayer.setSurface(dummySurface);
            }
        } else {
            Surface holder = (Surface) msg.obj;
            surface = holder;
            
            // Switch to new surface using SurfaceControl helper
            if (surfaceSwitcher != null) {
                surfaceSwitcher.switchToSurface(holder);
            } else {
                // Fallback to standard method
                mediaPlayer.setSurface(holder);
            }
        }
    }

    /**
     * Show video on specified surface with optimal buffer dimensions
     * This method provides enhanced performance when SurfaceControl is available
     * 
     * @param surface Target surface to display video on (null to hide video)
     * @param width Optimal width for buffer sizing (0 if hiding)
     * @param height Optimal height for buffer sizing (0 if hiding)
     */
    public void showDisplayWithDimensions(Surface surface, int width, int height) {
        if (mediaPlayer == null) {
            return;
        }
        
        if (surface == null) {
            // Hide video efficiently with SurfaceControl
            if (surfaceSwitcher != null) {
                surfaceSwitcher.switchToSurfaceWithDimensions(null, 0, 0);
            } else {
                mediaPlayer.setSurface(dummySurface);
            }
            this.surface = null;
        } else {
            // Show video with optimal buffer sizing
            if (surfaceSwitcher != null) {
                surfaceSwitcher.switchToSurfaceWithDimensions(surface, width, height);
            } else {
                mediaPlayer.setSurface(surface);
            }
            this.surface = surface;
        }
    }

    /**
     * Control video visibility without changing the surface
     * Useful for temporarily hiding video without interrupting playback
     * 
     * @param visible true to show video, false to hide video
     */
    public void setVideoVisibility(boolean visible) {
        if (surfaceSwitcher != null) {
            surfaceSwitcher.setVideoVisibility(visible);
        }
        // Note: Standard mode doesn't support visibility control
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
    public void setVolume(float left, float right) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(left, right);
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
            mediaPlayer = null;
        }
        if (dummySurface != null) {
            dummySurface.release();
            dummySurface = null;
        }
        
        // Release SurfaceControl helper resources
        if (surfaceSwitcher != null) {
            surfaceSwitcher.release();
            surfaceSwitcher = null;
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

    /**
     * Check if SurfaceControl is being used for surface switching
     * @return true if using SurfaceControl (API 29+), false if using standard switching
     */
    public boolean isUsingSurfaceControl() {
        return surfaceSwitcher != null && surfaceSwitcher.isUsingSurfaceControl();
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
