package com.shuyu.gsyvideoplayer.cast.dlna;

import static org.junit.Assert.assertTrue;

import com.shuyu.gsyvideoplayer.cast.CastMediaInfo;

import org.junit.Test;

/** 13.2.1: Guards the metadata boundary moved into the optional cast module. */
public class DidlLiteTest {

    @Test
    public void build_containsMediaIdentityAndProtocolInfo() {
        CastMediaInfo media = new CastMediaInfo(
                "https://example.com/video.mp4",
                "GSY Cast Test",
                "video/mp4",
                60_000L);

        String xml = DidlLite.build(media);

        assertTrue(xml.contains("GSY Cast Test"));
        assertTrue(xml.contains("https://example.com/video.mp4"));
        assertTrue(xml.contains("http-get:*:video/mp4:*"));
    }
}
