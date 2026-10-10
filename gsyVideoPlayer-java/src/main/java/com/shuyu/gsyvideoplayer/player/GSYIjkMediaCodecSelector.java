package com.shuyu.gsyvideoplayer.player;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;
import android.util.Log;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/** Preserves IJK's legacy choices, adding only verified clear hardware Codec2 fallback. */
final class GSYIjkMediaCodecSelector implements IjkMediaPlayer.OnMediaCodecSelectListener {
    static final GSYIjkMediaCodecSelector INSTANCE = new GSYIjkMediaCodecSelector();

    @Override public String onMediaCodecSelect(IMediaPlayer player, String mime, int profile, int level) {
        String legacy = IjkMediaPlayer.DefaultMediaCodecSelector.sInstance
                .onMediaCodecSelect(player, mime, profile, level);
        if (legacy != null || Build.VERSION.SDK_INT < 29 || mime == null || mime.isEmpty()) {
            return legacy;
        }
        return Api29.select(mime);
    }

    @android.annotation.TargetApi(29)
    private static final class Api29 {
        static String select(String mime) {
        try {
            for (MediaCodecInfo codec : new MediaCodecList(MediaCodecList.REGULAR_CODECS).getCodecInfos()) {
                try {
                    String name = codec.getName();
                    if (name == null || !name.startsWith("c2.") || codec.isEncoder()
                            || !codec.isHardwareAccelerated() || codec.isSoftwareOnly()) {
                        continue;
                    }
                    String[] types = codec.getSupportedTypes();
                    if (types == null) continue;
                    for (String type : types) {
                        if (type == null || !mime.equalsIgnoreCase(type)) continue;
                        MediaCodecInfo.CodecCapabilities caps = codec.getCapabilitiesForType(type);
                        if (caps != null
                                && !caps.isFeatureRequired(MediaCodecInfo.CodecCapabilities.FEATURE_SecurePlayback)
                                && !caps.isFeatureRequired(MediaCodecInfo.CodecCapabilities.FEATURE_TunneledPlayback)) {
                            Log.i("GSYCodec2Selector", "Selected clear hardware decoder: " + name);
                            return name;
                        }
                    }
                } catch (RuntimeException ignored) {
                    // Unreadable capabilities do not qualify a candidate; try the next one.
                }
            }
        } catch (RuntimeException ignored) {
            // Preserve the original null result when the platform catalog is unavailable.
        }
        return null;
    }
    }
}
