package com.example.gsyvideoplayer.source;

import android.content.Context;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class CustomSourceTag {

    /**
     * 获取SourceFactory
     */
    static public DataSource.Factory getDataSourceFactory(Context context, boolean preview) {
        return new DefaultDataSourceFactory(context, preview ? null : new DefaultBandwidthMeter(),
                getHttpDataSourceFactory(context, preview));
    }

    static public DataSource.Factory getHttpDataSourceFactory(Context context, boolean preview) {
        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(context,
                "yout tag"), preview ? null : new DefaultBandwidthMeter());
        return dataSourceFactory;
    }
}
