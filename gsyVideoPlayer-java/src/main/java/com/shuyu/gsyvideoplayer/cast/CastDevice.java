package com.shuyu.gsyvideoplayer.cast;

/**
 * 描述一台可投屏的远端设备（DLNA MediaRenderer / 未来的 Chromecast / AirPlay …）。
 * <p>纯数据类，不可变、线程安全，可跨线程传递（provider 后台线程发现 → 主线程回调）。</p>
 * <ul>
 *   <li>id ── 全局唯一标识（DLNA 对应 UDN，如 "uuid:xxxx-xxxx"）</li>
 *   <li>name ── 用户可见名称（DLNA 的 friendlyName）</li>
 *   <li>address ── 主机 IP / 域名</li>
 *   <li>port ── 端口，未知给 0</li>
 *   <li>protocol ── 协议标识，与 {@link CastProvider#getProtocol()} 对齐，如 "dlna"</li>
 *   <li>raw ── 附加原始信息，例如 jUPnP 的 org.jupnp.model.meta.Device 对象；上层不要依赖具体类型</li>
 * </ul>
 */
public final class CastDevice {

    private final String id;
    private final String name;
    private final String address;
    private final int port;
    private final String protocol;
    private final Object raw;

    public CastDevice(String id, String name, String address, int port, String protocol, Object raw) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.port = port;
        this.protocol = protocol;
        this.raw = raw;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getProtocol() {
        return protocol;
    }

    public Object getRaw() {
        return raw;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CastDevice)) return false;
        CastDevice that = (CastDevice) o;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return protocol != null ? protocol.equals(that.protocol) : that.protocol == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (protocol != null ? protocol.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CastDevice{" + protocol + ":" + name + "@" + address + ":" + port + "}";
    }
}
