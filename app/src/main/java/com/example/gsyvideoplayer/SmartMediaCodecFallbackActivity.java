package com.example.gsyvideoplayer;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gsyvideoplayer.databinding.ActivitySmartMediaCodecFallbackBinding;
import com.example.gsyvideoplayer.mediacodec.SmartMediaCodecVideo;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.player.IPlayerManager;
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.MediaInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SmartMediaCodecFallbackActivity extends AppCompatActivity {

    private static final String TAG = "SmartMediaCodecDemo";
    private static final String TITLE = "智能硬解降级 High10 样本";
    private static final int MEDIA_INFO_VIDEO_ROTATION_CHANGED = 10001;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ActivitySmartMediaCodecFallbackBinding binding;
    private boolean originMediaCodec;
    private boolean originSmartMediaCodec;
    private Class<? extends IPlayerManager> originPlayerManagerClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySmartMediaCodecFallbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        originMediaCodec = GSYVideoType.isMediaCodec();
        originSmartMediaCodec = GSYVideoType.isSmartMediaCodec();
        IPlayerManager originPlayerManager = PlayerFactory.getPlayManager();
        if (originPlayerManager != null) {
            originPlayerManagerClass = originPlayerManager.getClass();
        }

        PlayerFactory.setPlayManager(IjkPlayerManager.class);
        GSYVideoType.enableMediaCodec();
        GSYVideoType.enableSmartMediaCodec();
        Debuger.enable();

        initPlayer();
        initButtons();
        initBackPressed();
        startTest();
    }

    private void initPlayer() {
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.xxx1);
        binding.detailPlayer.setThumbImageView(imageView);
        binding.detailPlayer.getTitleTextView().setVisibility(View.VISIBLE);
        binding.detailPlayer.getBackButton().setVisibility(View.VISIBLE);
        binding.detailPlayer.getFullscreenButton().setVisibility(View.GONE);
        binding.detailPlayer.setIsTouchWiget(true);
        binding.detailPlayer.setLooping(false);
        binding.detailPlayer.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
        binding.detailPlayer.setSmartMediaCodecEventListener(new SmartMediaCodecVideo.SmartMediaCodecEventListener() {
            @Override
            public void onPrepared() {
                appendLog("onPrepared -> " + decoderSnapshot());
                logDecoderSoon("prepared + 300ms", 300);
            }

            @Override
            public void onInfo(int what, int extra) {
                appendLog("onInfo " + describeInfo(what) + ", extra=" + extra + " -> " + decoderSnapshot());
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    logDecoderSoon("first render + 500ms", 500);
                }
            }

            @Override
            public void onError(int what, int extra) {
                appendLog("onPlayError what=" + what + ", extra=" + extra + " -> " + decoderSnapshot());
            }

            @Override
            public void onAutoCompletion() {
                appendLog("onAutoCompletion -> " + decoderSnapshot());
            }
        });
        binding.detailPlayer.setVideoAllCallBack(new GSYSampleCallBack() {
            @Override
            public void onStartPrepared(String url, Object... objects) {
                appendLog("onStartPrepared -> request MediaCodec + smart fallback");
            }

            @Override
            public void onPrepared(String url, Object... objects) {
                appendLog("VideoAllCallBack.onPrepared");
            }

            @Override
            public void onPlayError(String url, Object... objects) {
                appendLog("VideoAllCallBack.onPlayError");
            }
        });
    }

    private void initButtons() {
        binding.replayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appendLog("----- replay -----");
                startTest();
            }
        });
        binding.clearLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.statusText.setText("");
                appendLog("log cleared");
            }
        });
    }

    private void initBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                binding.detailPlayer.setVideoAllCallBack(null);
                GSYVideoManager.releaseAllVideos();
                finish();
            }
        });
    }

    private void startTest() {
        GSYVideoManager.releaseAllVideos();
        File sampleFile = ensureLocalSampleFile();
        if (sampleFile == null) {
            appendLog("sample copy failed, stop test");
            return;
        }
        String url = sampleFile.getAbsolutePath();
        appendLog("sample=H.264 High 10, yuv420p10le, 640x360, apk assets -> app cache");
        appendLog("file=" + url);
        appendLog("core=IJK, mediaCodec=" + GSYVideoType.isMediaCodec()
            + ", smartMediaCodec=" + GSYVideoType.isSmartMediaCodec());
        appendLog("expect=硬解不支持 High10 时底层/上层只降级一次，最终 VideoDecoder 应为 avcodec");
        binding.detailPlayer.setUp(url, false, TITLE);
        binding.detailPlayer.startPlayLogic();
    }

    private File ensureLocalSampleFile() {
        File outFile = new File(getCacheDir(), "smart_mediacodec_high10.mp4");
        if (outFile.exists() && outFile.length() > 0) {
            return outFile;
        }
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = getAssets().open("smart_mediacodec_high10.mp4");
            outputStream = new FileOutputStream(outFile);
            byte[] buffer = new byte[16 * 1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            return outFile;
        } catch (IOException e) {
            appendLog("copy asset error=" + e.getMessage());
            return null;
        } finally {
            closeQuietly(inputStream);
            closeQuietly(outputStream);
        }
    }

    private void closeQuietly(java.io.Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ignored) {
        }
    }

    private void appendLog(String message) {
        Log.i(TAG, message);
        Debuger.printfLog(TAG + ": " + message);
        binding.statusText.append(message + "\n");
        binding.logScroll.post(new Runnable() {
            @Override
            public void run() {
                binding.logScroll.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void logDecoderSoon(final String from, long delayMs) {
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                appendLog(from + " -> " + decoderSnapshot());
            }
        }, delayMs);
    }

    private String decoderSnapshot() {
        try {
            IPlayerManager playerManager = binding.detailPlayer.getGSYVideoManager().getPlayer();
            if (playerManager == null || playerManager.getMediaPlayer() == null) {
                return "decoder=none";
            }
            IMediaPlayer mediaPlayer = playerManager.getMediaPlayer();
            if (!(mediaPlayer instanceof IjkMediaPlayer)) {
                return "decoder=non-ijk:" + mediaPlayer.getClass().getSimpleName();
            }
            IjkMediaPlayer ijkMediaPlayer = (IjkMediaPlayer) mediaPlayer;
            int decoder = ijkMediaPlayer.getVideoDecoder();
            MediaInfo mediaInfo = ijkMediaPlayer.getMediaInfo();
            String mediaInfoDecoder = mediaInfo == null ? "null" : mediaInfo.mVideoDecoder;
            return "decoderCode=" + decoder + ", mediaInfo=" + mediaInfoDecoder;
        } catch (Exception e) {
            return "decoder=read-error:" + e.getClass().getSimpleName();
        }
    }

    private String describeInfo(int what) {
        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
            return "MEDIA_INFO_VIDEO_RENDERING_START(" + what + ")";
        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
            return "MEDIA_INFO_BUFFERING_START(" + what + ")";
        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
            return "MEDIA_INFO_BUFFERING_END(" + what + ")";
        } else if (what == MEDIA_INFO_VIDEO_ROTATION_CHANGED) {
            return "MEDIA_INFO_VIDEO_ROTATION_CHANGED(" + what + ")";
        }
        return "what=" + what;
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.detailPlayer.onVideoPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.detailPlayer.onVideoResume();
    }

    @Override
    protected void onDestroy() {
        mainHandler.removeCallbacksAndMessages(null);
        binding.detailPlayer.setSmartMediaCodecEventListener(null);
        binding.detailPlayer.setVideoAllCallBack(null);
        GSYVideoManager.releaseAllVideos();
        if (originMediaCodec) {
            GSYVideoType.enableMediaCodec();
        } else {
            GSYVideoType.disableMediaCodec();
        }
        if (originSmartMediaCodec) {
            GSYVideoType.enableSmartMediaCodec();
        } else {
            GSYVideoType.disableSmartMediaCodec();
        }
        if (originPlayerManagerClass != null) {
            PlayerFactory.setPlayManager(originPlayerManagerClass);
        }
        super.onDestroy();
    }
}
