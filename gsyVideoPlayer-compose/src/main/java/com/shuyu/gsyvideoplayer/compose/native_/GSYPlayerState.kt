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
    /**
     * 视频 SAR（Sample Aspect Ratio）分子。
     * 来源：[com.shuyu.gsyvideoplayer.video.base.GSYVideoView.getVideoSarNum]。
     * 用于像素长宽不等的源（多发于 H.264/HLS/DASH 等），UI 在画面变换时可参考此值修正纵横比。
     */
    val videoSarNum: Int = 0,
    /**
     * 视频 SAR 分母。来源：[com.shuyu.gsyvideoplayer.video.base.GSYVideoView.getVideoSarDen]。
     */
    val videoSarDen: Int = 0,
    /**
     * 实时网速（bytes/sec）。来源：[com.shuyu.gsyvideoplayer.video.base.GSYVideoView.getNetSpeed]。
     * 注意：开启缓存代理时，缓存命中后该值仍可能 > 0（本地代理读盘也算），
     * 完全播放本地文件时回 0。
     */
    val netSpeed: Long = 0L,
    /**
     * [netSpeed] 的人类可读形态（如 "128 KB/s"）。
     * 来源：[com.shuyu.gsyvideoplayer.video.base.GSYVideoView.getNetSpeedText]。
     */
    val netSpeedText: String = "",
    /**
     * 当前是否在读取本地缓存文件（vs 网络流）。
     * 来源：[com.shuyu.gsyvideoplayer.GSYVideoBaseManager.isCacheFile]。
     * 用于"缓存命中"小红点 / 上行节流等业务判断。
     */
    val isCacheReady: Boolean = false,
    val speed: Float = 1f,
    val isLocked: Boolean = false,
)
