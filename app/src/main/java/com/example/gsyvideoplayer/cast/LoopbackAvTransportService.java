package com.example.gsyvideoplayer.cast;

import android.content.Context;
import android.util.Log;

import org.jupnp.model.types.ErrorCode;
import org.jupnp.model.types.UnsignedIntegerFourBytes;
import org.jupnp.support.avtransport.AVTransportException;
import org.jupnp.support.avtransport.AbstractAVTransportService;
import org.jupnp.support.model.DeviceCapabilities;
import org.jupnp.support.model.MediaInfo;
import org.jupnp.support.model.PositionInfo;
import org.jupnp.support.model.StorageMedium;
import org.jupnp.support.model.TransportAction;
import org.jupnp.support.model.TransportInfo;
import org.jupnp.support.model.TransportSettings;
import org.jupnp.support.model.TransportState;
import org.jupnp.support.model.TransportStatus;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.util.Locale;

/**
 * Loopback AVTransport 实现：只承担"接到 SetAVTransportURI/Play/Pause/Seek/Stop 就把命令
 * 派发给 {@link CastReceiverFloatingWindow}"这一件事。
 *
 * <p>本类跑在 :dlna 独立进程内，因此调用 CastReceiverFloatingWindow 是同进程内的直接方法调用，
 * 不需要 IPC。CastReceiverFloatingWindow.post* 内部会切主线程 —— 而 UpnpService 的 action
 * 派发线程本身就是 jUPnP 内部 worker，切一次主线程是必须的。
 *
 * <p>状态字段（TransportState/PositionInfo/MediaInfo）保持简单，仅够 sender 侧不报错；
 * 真实进度回填不在 M7-a-5 范围内。
 *
 * <p>父类 {@link AbstractAVTransportService} 已带 {@code @UpnpAction} + {@code @UpnpInputArgument}
 * 等注解，子类只 override 方法体，注解直接继承。因此无需在类头补 @UpnpService，
 * 由 {@link LoopbackDeviceFactory} 中的 AnnotationLocalServiceBinder 扫描父类拿到 metadata。
 */
public class LoopbackAvTransportService extends AbstractAVTransportService {

    private static final String TAG = "LoopbackAvTransport";

    private final Context appContext;

    private volatile String currentUri = "";
    private volatile String currentMetadata = "";
    private volatile TransportState state = TransportState.NO_MEDIA_PRESENT;

    public LoopbackAvTransportService(Context appContext) {
        this.appContext = appContext.getApplicationContext();
    }

    // ---- URI + Play ----

    @Override
    public void setAVTransportURI(UnsignedIntegerFourBytes instanceId,
                                  String currentURI,
                                  String currentURIMetaData) throws AVTransportException {
        Log.i(TAG, "setAVTransportURI: " + currentURI);
        // 安全检查：ijkplayer/FFmpeg 支持 file:/content:/pipe:/crypto: 多协议，任意 URL 会形成
        // SSRF 或本地文件读取面。DLNA 语义只允许网络 URL，所以在入口就白名单化。
        // 拒绝原因不告知对端细节，避免给攻击者做 host 探测的信道。
        if (!isSafeMediaUrl(currentURI)) {
            Log.w(TAG, "setAVTransportURI REJECTED (unsafe scheme/host): " + currentURI);
            throw new AVTransportException(ErrorCode.ARGUMENT_VALUE_INVALID,
                    "URI scheme not allowed");
        }
        this.currentUri = currentURI == null ? "" : currentURI;
        this.currentMetadata = currentURIMetaData == null ? "" : currentURIMetaData;
        this.state = TransportState.STOPPED;
        // 有的 sender 在 SetAVTransportURI 之后再单独发 Play；有的（含内核 JupnpDlnaSession）直接就
        // 在 SetURI 之后自动 Play。这里保守起见：SetURI 就直接开播，Play 再次调用等价 resume。
        final String title = extractTitleFromDidl(currentMetadata);
        CastReceiverFloatingWindow.postShowAndPlay(appContext, this.currentUri, title);
        this.state = TransportState.PLAYING;
    }

    @Override
    public void setNextAVTransportURI(UnsignedIntegerFourBytes instanceId,
                                      String nextURI,
                                      String nextURIMetaData) throws AVTransportException {
        // Loopback 不支持 gapless / queue，忽略即可。
    }

    @Override
    public void play(UnsignedIntegerFourBytes instanceId, String speed) throws AVTransportException {
        Log.i(TAG, "play speed=" + speed);
        if (currentUri.isEmpty()) {
            throw new AVTransportException(ErrorCode.INVALID_ACTION, "no URI set");
        }
        CastReceiverFloatingWindow.postPlay();
        this.state = TransportState.PLAYING;
    }

    @Override
    public void pause(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        Log.i(TAG, "pause");
        CastReceiverFloatingWindow.postPause();
        this.state = TransportState.PAUSED_PLAYBACK;
    }

    @Override
    public void stop(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        Log.i(TAG, "stop");
        CastReceiverFloatingWindow.postStop();
        this.state = TransportState.STOPPED;
    }

    @Override
    public void seek(UnsignedIntegerFourBytes instanceId, String unit, String target) throws AVTransportException {
        Log.i(TAG, "seek unit=" + unit + " target=" + target);
        if ("REL_TIME".equalsIgnoreCase(unit) || "ABS_TIME".equalsIgnoreCase(unit)) {
            long ms = parseTimeToMillis(target);
            if (ms >= 0) {
                CastReceiverFloatingWindow.postSeek(ms);
            }
        }
    }

    // ---- 状态查询：回填 CastReceiverFloatingWindow 内 IJK 的真实进度 / 状态 ----

    @Override
    public MediaInfo getMediaInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        long durationMs = CastReceiverFloatingWindow.getDurationMs();
        String duration = formatDlnaTime(durationMs);
        // MediaInfo(uri, metadata, nrTracks, mediaDuration, playMedium)
        return new MediaInfo(currentUri, currentMetadata,
                new UnsignedIntegerFourBytes(1L), duration,
                org.jupnp.support.model.StorageMedium.NETWORK);
    }

    @Override
    public TransportInfo getTransportInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        // 优先用 receiver 内 IJK 的真实状态；若 IJK 不可用（悬浮窗未建立、权限被拒等），
        // 退回缓存的 state（setAVTransportURI/play/pause 里手动维护的那份）。
        TransportState real = mapReceiverStateToTransportState(
                CastReceiverFloatingWindow.getTransportState());
        if (real != null) {
            return new TransportInfo(real, TransportStatus.OK, "1");
        }
        return new TransportInfo(state, TransportStatus.OK, "1");
    }

    @Override
    public PositionInfo getPositionInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        long positionMs = CastReceiverFloatingWindow.getCurrentPositionMs();
        long durationMs = CastReceiverFloatingWindow.getDurationMs();
        String duration = formatDlnaTime(durationMs);
        String relTime  = formatDlnaTime(positionMs);
        // PositionInfo(track, trackDuration, trackMetaData, trackURI, relTime)
        return new PositionInfo(1L, duration, currentMetadata, currentUri, relTime);
    }

    /**
     * 把 {@link CastReceiverFloatingWindow} 定义的整数状态翻译到 jUPnP {@link TransportState}。
     * <p>关键映射：BUFFERING 归 TRANSITIONING（DLNA/UPnP 标准里没有 BUFFERING，
     * TRANSITIONING 语义等价——正在从 STOPPED → PLAYING 的过渡态）；
     * ERROR 归 STOPPED（AVTransport 规范里没有 ERROR 状态，
     * 播放器错误由 TransportStatus 而不是 TransportState 表达）。</p>
     */
    private static TransportState mapReceiverStateToTransportState(int receiverState) {
        switch (receiverState) {
            case CastReceiverFloatingWindow.TRANSPORT_STATE_PLAYING:
                return TransportState.PLAYING;
            case CastReceiverFloatingWindow.TRANSPORT_STATE_PAUSED:
                return TransportState.PAUSED_PLAYBACK;
            case CastReceiverFloatingWindow.TRANSPORT_STATE_BUFFERING:
                return TransportState.TRANSITIONING;
            case CastReceiverFloatingWindow.TRANSPORT_STATE_ERROR:
            case CastReceiverFloatingWindow.TRANSPORT_STATE_STOPPED:
                return TransportState.STOPPED;
            default:
                return null;
        }
    }

    /** ms → "H:MM:SS" DLNA REL_TIME/TrackDuration 格式；毫秒/负数一律归 0。 */
    private static String formatDlnaTime(long ms) {
        if (ms < 0L) ms = 0L;
        long total = ms / 1000L;
        long h = total / 3600L;
        long m = (total % 3600L) / 60L;
        long s = total % 60L;
        return String.format(Locale.US, "%d:%02d:%02d", h, m, s);
    }

    @Override
    public DeviceCapabilities getDeviceCapabilities(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return new DeviceCapabilities(new StorageMedium[]{StorageMedium.NETWORK});
    }

    @Override
    public TransportSettings getTransportSettings(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return new TransportSettings();
    }

    @Override
    public void record(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        // no-op
    }

    @Override
    public void next(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        // no-op
    }

    @Override
    public void previous(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        // no-op
    }

    @Override
    public void setPlayMode(UnsignedIntegerFourBytes instanceId, String newPlayMode) throws AVTransportException {
        // no-op
    }

    @Override
    public void setRecordQualityMode(UnsignedIntegerFourBytes instanceId, String newRecordQualityMode) throws AVTransportException {
        // no-op
    }

    @Override
    protected TransportAction[] getCurrentTransportActions(UnsignedIntegerFourBytes instanceId) throws Exception {
        return new TransportAction[]{
                TransportAction.Play,
                TransportAction.Pause,
                TransportAction.Stop,
                TransportAction.Seek
        };
    }

    @Override
    public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
        // Loopback 只暴露一个虚拟 instance，与 getDefaultInstanceID() 对齐。
        return new UnsignedIntegerFourBytes[]{new UnsignedIntegerFourBytes(0L)};
    }

    // ---- helpers ----

    /**
     * 判断 sender 送来的媒体 URL 是否可以放行播放。
     * <ul>
     *   <li>只允许 scheme=http/https；file/content/pipe/crypto/data 等一律拒绝（ijkplayer/FFmpeg 均支持）</li>
     *   <li>host 必须存在，且不能是回环地址（127.0.0.0/8、::1）、任意地址（0.0.0.0、::）、
     *       本地链路地址（169.254/16、fe80::/10）——这些都是本机资源</li>
     *   <li>不做站点局域网（10/8、172.16/12、192.168/16）拒绝。理由：DLNA 部署本身就在同一
     *       家庭 LAN 内，媒体源经常就是同网段 NAS/网关</li>
     *   <li>纯字面 host 走 InetAddress 解析；DNS 名走同样的地址判断</li>
     * </ul>
     * <p>该方法只做入口过滤，属于"减少攻击面"层面的防御，不能替代 receiver 侧对 media 播放器
     * 的沙盒隔离。若未来 receiver 与 sender 不再是同一 App 进程，还需要在 media 播放器层配一层
     * 网络 ACL。</p>
     */
    static boolean isSafeMediaUrl(String url) {
        if (url == null || url.isEmpty()) return false;
        final URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            return false;
        }
        String scheme = uri.getScheme();
        if (scheme == null) return false;
        scheme = scheme.toLowerCase(Locale.ROOT);
        if (!"http".equals(scheme) && !"https".equals(scheme)) return false;
        String host = uri.getHost();
        if (host == null || host.isEmpty()) return false;
        // IPv6 字面：URI.getHost 会去括号，直接解析。
        try {
            InetAddress[] addrs = InetAddress.getAllByName(host);
            if (addrs.length == 0) return false;
            for (InetAddress addr : addrs) {
                if (isForbiddenAddress(addr)) return false;
            }
            return true;
        } catch (Throwable t) {
            // 解析失败：不放行。安全侧优先于可用性。
            Log.w(TAG, "isSafeMediaUrl: host resolve failed for " + host, t);
            return false;
        }
    }

    private static boolean isForbiddenAddress(InetAddress addr) {
        if (addr == null) return true;
        if (addr.isLoopbackAddress()) return true;   // 127.0.0.0/8, ::1
        if (addr.isAnyLocalAddress()) return true;   // 0.0.0.0, ::
        if (addr.isLinkLocalAddress()) return true;  // 169.254/16, fe80::/10
        if (addr.isMulticastAddress()) return true;  // 播放媒体不该走 multicast
        // IPv4-mapped IPv6（如 ::ffff:127.0.0.1）会被上面的 isLoopback 已经覆盖；这里再对
        // IPv6 显式 site-local（fec0::/10, deprecated）挡一下，防止 IPv4 已废弃 host 混过。
        if (addr instanceof Inet6Address && addr.isSiteLocalAddress()) return true;
        // IPv4 私网（isSiteLocalAddress 会 true）不做拒绝——见方法注释。
        if (addr instanceof Inet4Address) return false;
        return false;
    }

    /** DIDL-Lite &lt;dc:title&gt;…&lt;/dc:title&gt; 抽取；抓不到就返回 null。 */
    private static String extractTitleFromDidl(String didl) {
        if (didl == null || didl.isEmpty()) return null;
        int start = didl.indexOf("<dc:title>");
        if (start < 0) return null;
        int end = didl.indexOf("</dc:title>", start);
        if (end < 0) return null;
        return didl.substring(start + "<dc:title>".length(), end).trim();
    }

    /** "HH:MM:SS(.mmm)" → ms；解析失败返回 -1。 */
    private static long parseTimeToMillis(String s) {
        if (s == null || s.isEmpty()) return -1;
        try {
            String[] parts = s.split(":");
            if (parts.length != 3) return -1;
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            double sec = Double.parseDouble(parts[2]);
            return (long) ((h * 3600 + m * 60 + sec) * 1000);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
