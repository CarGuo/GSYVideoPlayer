package com.shuyu.gsyvideoplayer.compose.native_

import android.os.Looper
import android.view.ContextThemeWrapper
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.concurrent.TimeUnit

/** 14.1.0 / #4267: Published speed must follow Host configuration across setup paths. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
@LooperMode(LooperMode.Mode.PAUSED)
class GSYPlayerControllerSpeedTest {

    private lateinit var controller: GSYPlayerController
    private lateinit var player: GSYComposeHostPlayer

    @Before
    fun setUp() {
        controller = GSYPlayerController()
        val context = ContextThemeWrapper(
            RuntimeEnvironment.getApplication(),
            androidx.appcompat.R.style.Theme_AppCompat,
        )
        player = GSYComposeHostPlayer(context)
    }

    @After
    fun tearDown() {
        controller.dispose()
        shadowOf(Looper.getMainLooper()).idle()
        GSYVideoManager.changeManager(null)
    }

    @Test
    fun secondSetupPublishesDefaultSpeedImmediately() {
        controller.attachHost(player)
        controller.setUp("https://example.com/video-a.mp4")
        controller.setSpeed(3f)
        assertSpeed(3f)

        controller.setUp("https://example.com/video-b.mp4")

        // Do not advance the looper: setup must publish its applied configuration now.
        assertSpeed(1f)
    }

    @Test
    fun builderSpeedIsPublishedOnAttachAndOnReplacement() {
        controller.setUp(
            GSYVideoOptionBuilder()
                .setUrl("https://example.com/video-a.mp4")
                .setSpeed(2f),
        )

        controller.attachHost(player)
        assertSpeed(2f)

        controller.setUp(
            GSYVideoOptionBuilder()
                .setUrl("https://example.com/video-b.mp4")
                .setSpeed(1.5f),
        )

        assertSpeed(1.5f)
    }

    @Test
    fun pollingPublishesSpeedChangedThroughHost() {
        controller.attachHost(player)
        controller.setUp("https://example.com/video-a.mp4")

        controller.withHost { it.setSpeed(1.5f) }
        shadowOf(Looper.getMainLooper()).idleFor(500, TimeUnit.MILLISECONDS)

        assertSpeed(1.5f)
    }

    private fun assertSpeed(expected: Float) {
        assertEquals("Host speed", expected, player.speed, 0f)
        assertEquals("Snapshot speed", expected, controller.snapshot.value.speed, 0f)
        assertEquals("StateFlow speed", expected, controller.stateFlow.value.speed, 0f)
        assertEquals(controller.snapshot.value, controller.stateFlow.value)
    }
}
