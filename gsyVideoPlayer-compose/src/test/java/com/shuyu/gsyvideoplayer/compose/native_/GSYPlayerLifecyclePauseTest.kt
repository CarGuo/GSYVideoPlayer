package com.shuyu.gsyvideoplayer.compose.native_

import android.content.Context
import android.media.MediaPlayer
import android.os.Looper
import android.view.ContextThemeWrapper
import com.shuyu.gsyvideoplayer.GSYVideoBaseManager
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import com.shuyu.gsyvideoplayer.video.base.GSYVideoViewBridge
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** 14.0.0 / #4268: Buffer notifications must preserve lifecycle pause/resume intent. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
@LooperMode(LooperMode.Mode.PAUSED)
class GSYPlayerLifecyclePauseTest {

    private lateinit var backend: RecordingManager
    private lateinit var player: TestHost

    @Before
    fun setUp() {
        backend = RecordingManager()
        val context = ContextThemeWrapper(
            RuntimeEnvironment.getApplication(),
            androidx.appcompat.R.style.Theme_AppCompat,
        )
        player = TestHost(context)
        backend.setListener(player)
        // Exercise the same global lifecycle entry points used by Compose and Wrapper.
        GSYVideoManager.instance().setListener(player)
    }

    @After
    fun tearDown() {
        player.release()
        shadowOf(Looper.getMainLooper()).idle()
        GSYVideoManager.changeManager(null)
    }

    @Test
    fun lifecyclePauseSurvivesRepeatedBufferingAndLateBufferEnd() {
        player.enterPlaying()
        backend.isBackendPlaying = true
        bufferStart()
        bufferStart()
        assertEquals(GSYVideoView.CURRENT_STATE_PLAYING_BUFFERING_START, player.currentState)

        GSYVideoManager.onPause()
        assertEquals(1, backend.pauseCalls)
        assertFalse(backend.isBackendPlaying)

        bufferEnd()
        assertEquals(GSYVideoView.CURRENT_STATE_PAUSE, player.currentState)

        GSYVideoManager.onResume()
        assertEquals(GSYVideoView.CURRENT_STATE_PLAYING, player.currentState)
        assertEquals(1, backend.startCalls)
        assertEquals(listOf(2_300L), backend.seekTargets)
        assertTrue(backend.isBackendPlaying)
    }

    @Test
    fun lifecycleResumeBeforeBufferEndKeepsPlayingIntent() {
        player.enterPlaying()
        backend.isBackendPlaying = true
        bufferStart()
        GSYVideoManager.onPause()
        // Exo can report another buffering start when pause changes playWhenReady.
        bufferStart()
        assertEquals(GSYVideoView.CURRENT_STATE_PAUSE, player.currentState)

        GSYVideoManager.onResume(false)
        bufferEnd()

        assertEquals(GSYVideoView.CURRENT_STATE_PLAYING, player.currentState)
        assertEquals(1, backend.startCalls)
        assertTrue(backend.seekTargets.isEmpty())
        assertTrue(backend.isBackendPlaying)
    }

    @Test
    fun bufferingCanBePausedWhenBackendIsNotCurrentlyPlaying() {
        player.enterPlaying()
        bufferStart()
        backend.isBackendPlaying = false

        GSYVideoManager.onPause()
        bufferEnd()

        assertEquals(1, backend.pauseCalls)
        assertEquals(GSYVideoView.CURRENT_STATE_PAUSE, player.currentState)

        GSYVideoManager.onResume()
        assertEquals(1, backend.startCalls)
        assertEquals(GSYVideoView.CURRENT_STATE_PLAYING, player.currentState)
    }

    @Test
    fun pauseDuringPreparationIsAppliedAfterPreparedAndCanResume() {
        player.enterPreparing()

        GSYVideoManager.onPause()
        assertEquals(GSYVideoView.CURRENT_STATE_PREPAREING, player.currentState)
        assertEquals(0, backend.pauseCalls)
        assertEquals(0, backend.startCalls)

        player.onPrepared()
        assertEquals(GSYVideoView.CURRENT_STATE_PAUSE, player.currentState)
        assertEquals(1, backend.startCalls)
        assertEquals(1, backend.pauseCalls)
        assertFalse(backend.isBackendPlaying)

        GSYVideoManager.onResume()
        assertEquals(GSYVideoView.CURRENT_STATE_PLAYING, player.currentState)
        assertEquals(2, backend.startCalls)
    }

    @Test
    fun preparedWithoutAutoStartStillPausesAnAlreadyPlayingBackend() {
        player.setStartAfterPrepared(false)
        player.enterPreparing()
        backend.isBackendPlaying = true

        // onPrepared first sets PAUSE, then calls onVideoPause to stop the backend.
        player.onPrepared()

        assertEquals(GSYVideoView.CURRENT_STATE_PAUSE, player.currentState)
        assertEquals(1, backend.pauseCalls)
        assertEquals(0, backend.startCalls)
        assertFalse(backend.isBackendPlaying)
    }

    private fun bufferStart() {
        player.onInfo(MediaPlayer.MEDIA_INFO_BUFFERING_START, 0)
    }

    private fun bufferEnd() {
        player.onInfo(MediaPlayer.MEDIA_INFO_BUFFERING_END, 0)
    }

    private inner class TestHost(context: Context) : GSYComposeHostPlayer(context) {
        override fun getGSYVideoManager(): GSYVideoViewBridge = backend

        fun enterPlaying() {
            mHadPlay = true
            mHadPrepared = true
            setStateAndUi(GSYVideoView.CURRENT_STATE_PLAYING)
        }

        fun enterPreparing() {
            mHadPlay = false
            mHadPrepared = false
            setStateAndUi(GSYVideoView.CURRENT_STATE_PREPAREING)
        }

        // Rendering and connectivity are independent of the playback state transitions.
        override fun addTextureView() {}
        override fun createNetWorkState() {}
        override fun listenerNetWorkState() {}
    }

    private class RecordingManager : GSYVideoBaseManager() {
        var isBackendPlaying = false
        var pauseCalls = 0
        var startCalls = 0
        val seekTargets = mutableListOf<Long>()

        init {
            init()
        }

        override fun isPlaying(): Boolean = isBackendPlaying

        override fun getCurrentPosition(): Long = 2_300L

        override fun pause() {
            pauseCalls++
            isBackendPlaying = false
        }

        override fun start() {
            startCalls++
            isBackendPlaying = true
        }

        override fun seekTo(time: Long) {
            seekTargets += time
        }
    }
}
