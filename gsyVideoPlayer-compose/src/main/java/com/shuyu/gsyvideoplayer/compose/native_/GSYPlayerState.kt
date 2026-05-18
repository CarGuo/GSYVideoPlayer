package com.shuyu.gsyvideoplayer.compose.native_

import androidx.compose.runtime.Immutable

enum class GSYPlayState {
    Idle,
    Preparing,
    Playing,
    Buffering,
    Paused,
    Completed,
    Error,
}

@Immutable
data class GSYPlayerSnapshot(
    val state: GSYPlayState = GSYPlayState.Idle,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val bufferPercent: Int = 0,
    val isPlaying: Boolean = false,
    val errorWhat: Int = 0,
    val errorExtra: Int = 0,
    val videoWidth: Int = 0,
    val videoHeight: Int = 0,
    val speed: Float = 1f,
    val isLocked: Boolean = false,
)
