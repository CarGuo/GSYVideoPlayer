package com.example.gsyvideoplayer.utils;

public final class DemoVideoUrls {

    private DemoVideoUrls() {
    }

    public static final String MP4_BBB =
            "https://www.w3schools.com/html/mov_bbb.mp4";

    public static final String HLS_MUX =
            "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8";

    public static final String HLS_BIPBOP_GEAR1 =
            "http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear1/prog_index.m3u8";

    public static final String HLS_BIPBOP_GEAR3 =
            "http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear3/prog_index.m3u8";

    public static final String SAMPLE_GSY =
            "https://res.exexm.com/cw_145225549855002";

    public static final String DASH_ENVIVIO =
            "https://dash.akamaized.net/envivio/EnvivioDash3/manifest.mpd";

    public static final String SUBTITLE_SRT =
            "http://img.cdn.guoshuyu.cn/subtitle2.srt";

    public static final String SUBTITLE_VTT =
            "https://stdlwcdn.lwcdn.com/i/8fdb4e20-8ebb-4590-8844-dae39680d837/160p.vtt";

    public static final String DEFAULT_VIDEO = MP4_BBB;
    public static final String DEFAULT_HLS = HLS_MUX;
    public static final String DEFAULT_AD = HLS_MUX;
    public static final String DEFAULT_FEATURE = MP4_BBB;
    public static final String LIST_HORIZONTAL = MP4_BBB;
    public static final String LIST_VERTICAL = HLS_MUX;
    public static final String LIST_BBB_FALLBACK = MP4_BBB;
    public static final String SHORT_FORM_FALLBACK = MP4_BBB;
}
