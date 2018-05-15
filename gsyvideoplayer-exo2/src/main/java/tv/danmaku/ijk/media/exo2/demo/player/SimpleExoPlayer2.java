package tv.danmaku.ijk.media.exo2.demo.player;


import android.annotation.TargetApi;
import android.support.annotation.Nullable;

import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.analytics.AnalyticsCollector;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.util.Clock;

@TargetApi(16)
public class SimpleExoPlayer2 extends SimpleExoPlayer {

    protected SimpleExoPlayer2(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager) {
        super(renderersFactory, trackSelector, loadControl, drmSessionManager);
    }

    protected SimpleExoPlayer2(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, AnalyticsCollector.Factory analyticsCollectorFactory) {
        super(renderersFactory, trackSelector, loadControl, drmSessionManager, analyticsCollectorFactory);
    }

    protected SimpleExoPlayer2(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, AnalyticsCollector.Factory analyticsCollectorFactory, Clock clock) {
        super(renderersFactory, trackSelector, loadControl, drmSessionManager, analyticsCollectorFactory, clock);
    }

    public synchronized static SimpleExoPlayer2 createSimpleExoPlayer2(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager) {
        return new SimpleExoPlayer2(renderersFactory, trackSelector, loadControl, drmSessionManager);
    }
}
