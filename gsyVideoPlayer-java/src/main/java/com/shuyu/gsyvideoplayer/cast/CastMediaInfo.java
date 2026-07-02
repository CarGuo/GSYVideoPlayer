package com.shuyu.gsyvideoplayer.cast;

/**
 * 待投屏的媒体信息，对齐 androidx.media3.common.MediaItem 的最小子集。
 * <p>不可变、线程安全，可自由跨线程传递给 provider 在后台线程处理。</p>
 * <ul>
 *   <li>url ── 媒体资源地址，需要远端设备能够访问到（DLNA 场景通常是发送方局域网 HTTP URL）</li>
 *   <li>title ── DIDL-Lite dc:title，用于远端 UI 展示</li>
 *   <li>mimeType ── 例如 "video/mp4"，DLNA 需要写入 protocolInfo</li>
 *   <li>durationMs ── 已知时长；未知给 0，位置轮询会用 GetPositionInfo 覆盖</li>
 *   <li>startPositionMs ── 起播位置（毫秒）。对齐 Chromecast LOAD.currentTime 与
 *       Media3 MediaItem.ClippingConfiguration.startPositionMs。0 表示从头播。
 *       DLNA provider 会在 SetAVTransportURI + Play 成功后自动 Seek 过去。</li>
 * </ul>
 */
public final class CastMediaInfo {

    private final String url;
    private final String title;
    private final String mimeType;
    private final long durationMs;
    private final long startPositionMs;

    public CastMediaInfo(String url, String title, String mimeType, long durationMs) {
        this(url, title, mimeType, durationMs, 0L);
    }

    public CastMediaInfo(String url, String title, String mimeType, long durationMs, long startPositionMs) {
        this.url = url;
        this.title = title;
        this.mimeType = mimeType;
        this.durationMs = durationMs;
        this.startPositionMs = Math.max(0L, startPositionMs);
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getMimeType() {
        return mimeType;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public long getStartPositionMs() {
        return startPositionMs;
    }
}
