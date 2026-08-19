package com.shuyu.gsyvideoplayer.cast.dlna;

import static org.junit.Assert.assertEquals;

import com.shuyu.gsyvideoplayer.cast.CastState;

import org.jupnp.support.model.TransportState;
import org.junit.Test;

/** 13.2.1: Guards the DLNA load-to-play race exposed by the 16 KB loopback release test. */
public class JupnpDlnaSessionStateTest {

    @Test
    public void loading_ignoresTransientStoppedBeforeRemotePlaybackStarts() {
        assertEquals(CastState.LOADING, JupnpDlnaSession.resolvePolledState(
                CastState.LOADING, TransportState.STOPPED));
        assertEquals(CastState.LOADING, JupnpDlnaSession.resolvePolledState(
                CastState.LOADING, TransportState.NO_MEDIA_PRESENT));
    }

    @Test
    public void activePlayback_preservesStoppedAsTerminalState() {
        assertEquals(CastState.STOPPED, JupnpDlnaSession.resolvePolledState(
                CastState.PLAYING, TransportState.STOPPED));
        assertEquals(CastState.STOPPED, JupnpDlnaSession.resolvePolledState(
                CastState.PAUSED, TransportState.NO_MEDIA_PRESENT));
    }

    @Test
    public void loading_stillAcceptsRealActiveStates() {
        assertEquals(CastState.PLAYING, JupnpDlnaSession.resolvePolledState(
                CastState.LOADING, TransportState.PLAYING));
        assertEquals(CastState.BUFFERING, JupnpDlnaSession.resolvePolledState(
                CastState.LOADING, TransportState.TRANSITIONING));
    }
}
