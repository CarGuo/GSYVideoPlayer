package com.shuyu.aliplay;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.Size;

import com.aliyun.player.AliPlayer;
import com.aliyun.player.AliPlayerFactory;
import com.aliyun.player.IPlayer;
import com.aliyun.player.bean.ErrorInfo;
import com.aliyun.player.bean.InfoBean;
import com.aliyun.player.bean.InfoCode;
import com.aliyun.player.source.UrlSource;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import tv.danmaku.ijk.media.player.AbstractMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.MediaInfo;
import tv.danmaku.ijk.media.player.misc.ITrackInfo;

/**
 * 阿里播放器
 * Created by https://github.com/iuhni on 2022/05/16.
 * @link https://github.com/CarGuo/GSYVideoPlayer/issues/3575
 */
public class AliMediaPlayer extends AbstractMediaPlayer {

    private String TAG = AliMediaPlayer.class.getSimpleName();

    protected Context mAppContext;
    protected Surface mSurface;
    protected AliPlayer mInternalPlayer;
    protected Map<String, String> mHeaders = new HashMap<>();
    protected String mDataSource;
    protected UrlSource mMediaSource;
    protected int mVideoWidth;
    protected int mVideoHeight;

    //原视频的buffered
    private long mVideoBufferedPosition = 0;
    //原视频的currentPosition
    private long mCurrentPosition = 0;

    public AliMediaPlayer(Context context) {
        mAppContext = context.getApplicationContext();
    }

    @Override
    public void setDisplay(SurfaceHolder sh) {
        if (sh == null)
            setSurface(null);
        else
            setSurface(sh.getSurface());
    }

    @Override
    public void setSurface(Surface surface) {
        mSurface = surface;
        if (mInternalPlayer != null) {
            if (surface != null && !surface.isValid()) {
                mSurface = null;
            }
            mInternalPlayer.setSurface(surface);
        }
    }

    @Override
    public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        if (headers != null) {
            mHeaders.clear();
            mHeaders.putAll(headers);
        }
        setDataSource(context, uri);
    }

    @Override
    public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setDataSource(mAppContext, Uri.parse(path));
    }

    @Override
    public void setDataSource(Context context, Uri uri) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mDataSource = uri.toString();
        UrlSource urlSource = new UrlSource();
        urlSource.setUri(uri.toString());//播放地址，可以是第三方点播地址，或阿里云直播服务中的拉流地址。
        mMediaSource = urlSource;
    }


    @Override
    public void setDataSource(FileDescriptor fd) throws IOException, IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException("no support");
    }


    @Override
    public String getDataSource() {
        return mDataSource;
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        if (mInternalPlayer != null)
            throw new IllegalStateException("can't prepare a prepared player");
        prepareAsyncInternal();

        isWaitOnSeekComplete = false;
    }

    @Override
    public void start() throws IllegalStateException {
        if (mInternalPlayer == null)
            return;
        mInternalPlayer.start();
    }

    @Override
    public void stop() throws IllegalStateException {
        if (mInternalPlayer == null)
            return;
        mInternalPlayer.stop();
    }

    @Override
    public void pause() throws IllegalStateException {
        if (mInternalPlayer == null)
            return;
        mInternalPlayer.pause();
    }

    @Override
    public void setScreenOnWhilePlaying(boolean screenOn) {

    }

    @Override
    public int getVideoWidth() {
        return mVideoWidth;
    }

    @Override
    public int getVideoHeight() {
        return mVideoHeight;
    }

    @Override
    public boolean isPlaying() {
        if (mInternalPlayer == null) {
            return false;
        }

        return mPlayState == IPlayer.started;
    }

    /**
     * 是否等待Seek完成
     */
    private boolean isWaitOnSeekComplete = false;

    @Override
    public void seekTo(long msec) throws IllegalStateException {
        if (mInternalPlayer == null) {
            return;
        }

        if (msec < 0 || msec > getDuration()) {
            return;
        }

        isWaitOnSeekComplete = true;
        mCurrentPosition = msec;
        mInternalPlayer.seekTo(msec);
    }

    @Override
    public long getCurrentPosition() {
        if (mInternalPlayer == null)
            return 0;
        return mCurrentPosition;
    }

    @Override
    public long getDuration() {
        if (mInternalPlayer == null)
            return 0;
        return mInternalPlayer.getDuration();
    }

    @Override
    public int getVideoSarNum() {
        return 1;
    }

    @Override
    public int getVideoSarDen() {
        return 1;
    }

    @Override
    public void reset() {

        if (mInternalPlayer != null) {
            stop();
            mInternalPlayer.release();
            mInternalPlayer = null;
        }
        mSurface = null;
        mDataSource = null;
        mVideoWidth = 0;
        mVideoHeight = 0;
        mPlayState = IPlayer.unknow;
    }

    @Override
    public void release() {
        if (mInternalPlayer != null) {
            reset();
        }
    }


    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        if (mInternalPlayer != null)
            mInternalPlayer.setVolume((leftVolume + rightVolume) / 2);
    }


    protected int audioSessionId = AudioManager.AUDIO_SESSION_ID_GENERATE;

    @Override
    public int getAudioSessionId() {
        return audioSessionId;
    }

    @Override
    public MediaInfo getMediaInfo() {
        return null;
    }

    @Override
    public void setLogEnabled(boolean enable) {

    }

    @Override
    public boolean isPlayable() {
        return false;
    }

    @Override
    public void setAudioStreamType(int streamtype) {

    }

    @Override
    public void setKeepInBackground(boolean keepInBackground) {

    }

    @Override
    public void setWakeMode(Context context, int mode) {

    }

    protected boolean isLooping = false;

    @Override
    public void setLooping(boolean looping) {
        isLooping = looping;
    }

    @Override
    public boolean isLooping() {
        return isLooping;
    }

    @Override
    public ITrackInfo[] getTrackInfo() {
        return new ITrackInfo[0];
    }


    private String deviceId;

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }


    protected void prepareAsyncInternal() {
        new Handler(Looper.myLooper()).post(
            new Runnable() {
                @Override
                public void run() {
                    boolean preferExtensionDecoders = true;
                    boolean useExtensionRenderers = true;//是否开启扩展
                    //创建播放器
                    mInternalPlayer = AliPlayerFactory.createAliPlayer(mAppContext);
                    //Android播放器SDK提供了H.264、H.265的硬解码能力，同时提供了enableHardwareDecoder提供开关。默认开，并且在硬解初始化失败时，自动切换为软解，保证视频的正常播放。示例如下：
//                        //开启硬解。默认开启
//                        mInternalPlayer.enableHardwareDecoder(GSYVideoType.isMediaCodec());

                    if (mSurface != null) {
                        mInternalPlayer.setSurface(mSurface);
                    }
                    //埋点日志上报功能默认开启，当traceId设置为DisableAnalytics时，则关闭埋点日志上报。当traceId设置为其他参数时，则开启埋点日志上报。
                    //建议传递traceId，便于跟踪日志。traceId为设备或用户的唯一标识符，通常为imei或idfa。
                    if (deviceId != null && deviceId.length() > 0) {
                        mInternalPlayer.setTraceId(deviceId);
                    }

                    // headers[0]="Host:xxx.com";//比如需要设置Host到header中。
                    // config.setCustomHeaders(headers);
                    if (mHeaders != null && mHeaders.size() > 0 && mInternalPlayer.getConfig() != null) {
                        String[] itemsArray = new String[mHeaders.size()];
                        int i = 0;
                        for (Map.Entry<String, String> entry : mHeaders.entrySet()) {
                            itemsArray[i] = entry.getKey() + ":" + entry.getValue();
                            i++;
                        }
                        mInternalPlayer.getConfig().setCustomHeaders(itemsArray);
                    }
                    //设置准备回调
                    mInternalPlayer.setOnPreparedListener(onPreparedListener);
                    //第一帧显示
                    mInternalPlayer.setOnRenderingStartListener(onRenderingStartListener);

                    //播放器出错监听
                    mInternalPlayer.setOnErrorListener(onErrorListener);
//                    //播放器加载回调
                    mInternalPlayer.setOnLoadingStatusListener(onLoadingStatusListener);
//                    mAliyunRenderView.setOnTrackReadyListenenr(new VideoPlayerOnTrackReadyListenner(this));
//                    //播放器状态
                    mInternalPlayer.setOnStateChangedListener(onStateChangedListener);
//                    //播放结束
                    mInternalPlayer.setOnCompletionListener(onCompletionListener);
//                    //播放信息监听
                    mInternalPlayer.setOnInfoListener(onInfoListener);
//                    //trackChange监听
//                    mAliyunRenderView.setOnTrackChangedListener(new VideoPlayerTrackChangedListener(this));
//                    //字幕显示和隐藏
//                    mAliyunRenderView.setOnSubtitleDisplayListener(new VideoPlayerSubtitleDeisplayListener(this));
//                    //seek结束事件
                    mInternalPlayer.setOnSeekCompleteListener(onSeekCompleteListener);
//                    //截图监听事件
//                    mAliyunRenderView.setOnSnapShotListener(new VideoPlayerOnSnapShotListener(this));
//                    //sei监听事件
//                    mAliyunRenderView.setOnSeiDataListener(new VideoPlayerOnSeiDataListener(this));
//                    mAliyunRenderView.setOnVerifyTimeExpireCallback(new VideoPlayerOnVerifyStsCallback(this));

                    //设置视频宽高变化监听
                    mInternalPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener);

                    mInternalPlayer.setLoop(isLooping);


                    mInternalPlayer.setDataSource(mMediaSource);
                    mInternalPlayer.prepare();
//                    mInternalPlayer.setPlayWhenReady(false);
                }
            }
        );
    }

    /**
     * 是否带上header
     */
    protected boolean isPreview = false;

    /**
     * 是否需要带上header
     * setDataSource之前生效
     */
    public void setPreview(boolean preview) {
        isPreview = preview;
    }

    public boolean isPreview() {
        return isPreview;
    }


    /**
     * 倍速播放
     *
     * @param speed 倍速播放，默认为1
     */
    public void setSpeed(@Size(min = 0) float speed) {
        if (mInternalPlayer != null) {
            mInternalPlayer.setSpeed(speed);
        }
    }

    public float getSpeed() {
        return mInternalPlayer.getSpeed();
    }

    public int getBufferedPercentage() {
        if (mInternalPlayer == null) {
            return 0;
        }
        int percent = 0;
        if (getDuration() > 0) {
            percent = (int) ((float) mVideoBufferedPosition / getDuration() * 100f);
        }
        //Log.e(TAG, "getBufferedPercentage percent = " + percent + " mVideoBufferedPosition = " + mVideoBufferedPosition + " getDuration = " + getDuration());
        return percent;
    }


    protected boolean isPreparing = true;

    private IPlayer.OnPreparedListener onPreparedListener = new IPlayer.OnPreparedListener() {
        @Override
        public void onPrepared() {
            if (isPreparing) {
                notifyOnPrepared();
                isPreparing = false;
            }
        }
    };

    private IPlayer.OnRenderingStartListener onRenderingStartListener = new IPlayer.OnRenderingStartListener() {
        @Override
        public void onRenderingStart() {
            //Log.e(TAG, "onRenderingStartListener ");
        }
    };
    private IPlayer.OnErrorListener onErrorListener = new IPlayer.OnErrorListener() {
        @Override
        public void onError(ErrorInfo errorInfo) {
            notifyOnError(IMediaPlayer.MEDIA_ERROR_UNKNOWN, IMediaPlayer.MEDIA_ERROR_UNKNOWN);
            //Log.e(TAG, "onErrorListener " + errorInfo.getCode().toString() + errorInfo.getMsg() + errorInfo.getExtra());
        }
    };

    private IPlayer.OnInfoListener onInfoListener = new IPlayer.OnInfoListener() {
        @Override
        public void onInfo(InfoBean infoBean) {
            sourceVideoPlayerInfo(infoBean);
            //Log.e(TAG, "onInfoListener " + infoBean.getCode().toString() + infoBean.getExtraValue() + infoBean.getExtraMsg());
        }
    };

    private long netSpeedLong = -1;

    /**
     * 原视频Info
     */
    private void sourceVideoPlayerInfo(InfoBean infoBean) {
        if (isWaitOnSeekComplete) {
            return;
        }
        if (infoBean.getCode() == InfoCode.CurrentDownloadSpeed) {
            //当前下载速度
            netSpeedLong = infoBean.getExtraValue();
            //Log.e(TAG, "sourceVideoPlayerInfo CurrentDownloadSpeed = " + netSpeedLong);
        } else if (infoBean.getCode() == InfoCode.BufferedPosition) {
            //更新bufferedPosition
            mVideoBufferedPosition = infoBean.getExtraValue();
        } else if (infoBean.getCode() == InfoCode.CurrentPosition) {
            //更新currentPosition
            mCurrentPosition = infoBean.getExtraValue();
        }
    }

    public long getNetSpeed() {
        return netSpeedLong;
    }


    private IPlayer.OnLoadingStatusListener onLoadingStatusListener = new IPlayer.OnLoadingStatusListener() {
        @Override
        public void onLoadingBegin() {
            //Log.e(TAG, "onLoadingStatusListener onLoadingBegin ");
            notifyOnInfo(IMediaPlayer.MEDIA_INFO_BUFFERING_START, 0);
        }

        @Override
        public void onLoadingProgress(int percent, float netSpeed) {
            //Log.e(TAG, "onLoadingStatusListener onLoadingProgress percent = " + percent + " netSpeed = " + netSpeed);
        }

        @Override
        public void onLoadingEnd() {
            //Log.e(TAG, "onLoadingStatusListener onLoadingEnd ");
            notifyOnInfo(IMediaPlayer.MEDIA_INFO_BUFFERING_END, 0);
        }
    };

    //视频播放状态
    private int mPlayState = IPlayer.unknow;

    private IPlayer.OnStateChangedListener onStateChangedListener = new IPlayer.OnStateChangedListener() {
        @Override
        public void onStateChanged(int i) {
            mPlayState = i;
            //Log.e(TAG, "onStateChangedListener onStateChanged " + i);
        }
    };


    private IPlayer.OnCompletionListener onCompletionListener = new IPlayer.OnCompletionListener() {
        @Override
        public void onCompletion() {
            notifyOnCompletion();
            //Log.e(TAG, "onCompletionListener onCompletion ");
        }
    };

    private IPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener = new IPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(int width, int height) {
            mVideoWidth = width;
            mVideoHeight = height;
            notifyOnVideoSizeChanged(width, height, 1, 1);

            //Log.e(TAG, "onVideoSizeChangedListener " + width + " " + height);
        }
    };

    private IPlayer.OnSeekCompleteListener onSeekCompleteListener = new IPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete() {
            isWaitOnSeekComplete = false;
            //Log.e(TAG, "onSeekCompleteListener onSeekComplete ");
        }
    };
}
