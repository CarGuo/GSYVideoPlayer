package com.shuyu.gsyvideoplayer.compose.native_

import org.junit.Assert.assertEquals
import org.junit.Test

class GSYPlayerControllerBufferTest {

    @Test
    fun exoPollingBufferUsedWhenHostBufferPointIsZero() {
        // Issue #4261: Exo2PlayerManager + ExoPlayerCacheManager never fires onBufferingUpdate,
        // so hostBufferPoint stays 0 while managerBufferedPercentage reports 0..100.
        assertEquals(
            42,
            resolveBufferPercent(
                state = GSYPlayState.Playing,
                managerBufferedPercentage = 42,
                hostBufferPoint = 0,
            ),
        )
        assertEquals(
            68,
            resolveBufferPercent(
                state = GSYPlayState.Buffering,
                managerBufferedPercentage = 68,
                hostBufferPoint = 0,
            ),
        )
        assertEquals(
            85,
            resolveBufferPercent(
                state = GSYPlayState.Paused,
                managerBufferedPercentage = 85,
                hostBufferPoint = 0,
            ),
        )
    }

    @Test
    fun exoPollingBufferAbove94RoundsUpTo100() {
        assertEquals(
            100,
            resolveBufferPercent(
                state = GSYPlayState.Playing,
                managerBufferedPercentage = 95,
                hostBufferPoint = 0,
            ),
        )
        assertEquals(
            94,
            resolveBufferPercent(
                state = GSYPlayState.Playing,
                managerBufferedPercentage = 94,
                hostBufferPoint = 0,
            ),
        )
    }

    @Test
    fun ijkCallbackBufferUsedWhenManagerReturnsNegativeOne() {
        // IjkPlayerManager / SystemPlayerManager return -1 from getBufferedPercentage()
        // and update hostBufferPoint via onBufferingUpdate.
        assertEquals(
            55,
            resolveBufferPercent(
                state = GSYPlayState.Playing,
                managerBufferedPercentage = -1,
                hostBufferPoint = 55,
            ),
        )
    }

    @Test
    fun idleAndPreparingIgnoreStaleManagerBufferedPercentage() {
        assertEquals(
            0,
            resolveBufferPercent(
                state = GSYPlayState.Idle,
                managerBufferedPercentage = 80,
                hostBufferPoint = 0,
            ),
        )
        assertEquals(
            0,
            resolveBufferPercent(
                state = GSYPlayState.Preparing,
                managerBufferedPercentage = 80,
                hostBufferPoint = 0,
            ),
        )
    }
}
