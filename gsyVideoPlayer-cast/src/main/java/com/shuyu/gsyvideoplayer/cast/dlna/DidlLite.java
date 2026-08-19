package com.shuyu.gsyvideoplayer.cast.dlna;

import com.shuyu.gsyvideoplayer.cast.CastMediaInfo;

import org.jupnp.support.contentdirectory.DIDLParser;
import org.jupnp.support.model.DIDLContent;
import org.jupnp.support.model.ProtocolInfo;
import org.jupnp.support.model.Res;
import org.jupnp.support.model.item.Item;
import org.jupnp.support.model.item.VideoItem;

/**
 * DIDL-Lite metadata helper。
 * <p>用于把 {@link CastMediaInfo} 转成 DLNA SetAVTransportURI 需要的 didl-lite XML 字符串，
 * MimeType 从 {@link CastMediaInfo#getMimeType()} 取，若为空则默认 "video/*"。</p>
 * <p>纯计算，无 IO，可在任意线程调用。</p>
 */
final class DidlLite {

    private DidlLite() {
    }

    static String build(CastMediaInfo media) {
        String mime = media.getMimeType();
        if (mime == null || mime.isEmpty()) {
            mime = "video/*";
        }
        String protocolInfo = "http-get:*:" + mime + ":*";
        Res res = new Res(new ProtocolInfo(protocolInfo), null, media.getUrl());
        String title = media.getTitle();
        if (title == null || title.isEmpty()) {
            title = "GSYVideoPlayer";
        }
        Item item = new VideoItem("gsy-item-0", "0", title, "GSYVideoPlayer", res);
        DIDLContent content = new DIDLContent();
        content.addItem(item);
        try {
            return new DIDLParser().generate(content);
        } catch (Exception e) {
            return "";
        }
    }
}
