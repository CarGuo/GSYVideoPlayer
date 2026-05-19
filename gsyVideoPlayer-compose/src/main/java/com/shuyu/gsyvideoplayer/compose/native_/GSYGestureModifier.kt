package com.shuyu.gsyvideoplayer.compose.native_

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.view.WindowManager
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.abs

// =====================================================================================
// R3 P1-1 ｜ GSYGestureModifier
//
// 设计目标：
//   把 GSYVideoControlView 里那套"竖向左半屏调亮度 / 竖向右半屏调音量 / 横向调进度"
//   的手势完整迁移到 Compose 端，作为 [Modifier.gsyGestureControl] 暴露给业务层。
//
// 与原生 GSYVideoControlView 的差异：
//   - 原 ControlView 把手势状态写在自己的字段 (`mChangeVolume / mBrightness / mChangePosition`)
//     上，且 dialog 由内置 popup 弹出。
//     Compose 端需要"无状态 + 可定制 UI"——dialog 由调用方传 [onCenterToast] 自行渲染。
//   - 锁屏判定来自 [GSYPlayerController.snapshot.isLocked]：锁屏激活时手势完全屏蔽。
//   - 手势 down → drag → up 的"边沿事件"通过回调暴露：
//       [onGestureUpdate] (按下时持续触发)，[onGestureCommit] (抬手时触发，仅 seek 时携 commit 值)
// =====================================================================================

/**
 * 手势类型，回调给 UI 层渲染中央 toast / 进度浮层。
 */
enum class GSYGestureType { Volume, Brightness, Progress, None }

/**
 * 手势实时更新数据。所有数值统一用 [0f, 1f] 比例表示（除 [seekTimeMs]）。
 *
 * @property type 当前手势类型
 * @property progress 当前手势已达到的归一化值（音量/亮度比例 0~1）；progress 类型时 = 当前 seek 目标 / duration
 * @property seekTimeMs 仅 [GSYGestureType.Progress] 有效：当前 seek 的目标毫秒
 * @property durationMs 仅 [GSYGestureType.Progress] 有效：总长毫秒
 */
data class GSYGestureUpdate(
    val type: GSYGestureType,
    val progress: Float,
    val seekTimeMs: Long = 0L,
    val durationMs: Long = 0L,
)

/**
 * 给视频区域加 GSY 三向手势。
 *
 * 行为契约：
 *   1. **垂直 drag**：起始 X < 屏宽一半 → 调亮度；否则调音量。屏蔽规则：
 *      - `enableVolume = false` 关掉右半屏纵向手势；
 *      - `enableBrightness = false` 关掉左半屏纵向手势。
 *   2. **水平 drag**：调播放进度（仅当 `enableSeek = true` 且 controller 有 duration > 0）。
 *      - drag 中只更新 [onGestureUpdate]，松手时调 `controller.seekTo(target)` 并触发
 *        [onGestureCommit]。
 *   3. **锁屏**：`controller.snapshot.value.isLocked == true` 时，**所有**手势短路返回，
 *      连 down 都不消费（保留 Compose 上层 click/tap 通过）。
 *
 * 主线程契约：所有 controller 调用都在 Compose recomposition 主线程，符合 Java 内核要求。
 *
 * @param controller 提供 snapshot.isLocked / duration / seekTo
 * @param enableSeek 是否允许"横向调进度"
 * @param enableVolume 是否允许"右半屏纵向调音量"
 * @param enableBrightness 是否允许"左半屏纵向调亮度"
 * @param onGestureUpdate 手势进行中持续触发，业务用以渲染中央 toast；松手时不再触发
 * @param onGestureCommit 仅 progress 手势在松手 seek 落定时触发（包含最终 seek 时间）
 */
fun Modifier.gsyGestureControl(
    controller: GSYPlayerController,
    enableSeek: Boolean = true,
    enableVolume: Boolean = true,
    enableBrightness: Boolean = true,
    onGestureUpdate: (GSYGestureUpdate) -> Unit = {},
    onGestureCommit: (GSYGestureUpdate) -> Unit = {},
): Modifier = composed {
    val context = LocalContext.current
    val view = LocalView.current
    val activity = remember(context) { context.findActivityOrNull() }
    val audioManager = remember(context) {
        context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }

    // 容器尺寸：用于决定"左半 vs 右半"以及 drag 比例换算
    var size by remember { mutableStateOf(IntSize.Zero) }

    // 当前手势会话状态（仅在一次 down → up 间有效）
    var sessionType by remember { mutableStateOf(GSYGestureType.None) }
    var startX by remember { mutableStateOf(0f) }
    var startVolume by remember { mutableStateOf(0) }
    var startBrightness by remember { mutableStateOf(0f) }
    var startPosition by remember { mutableStateOf(0L) }
    var lastSeekTarget by remember { mutableStateOf(0L) }
    var lastUpdate by remember { mutableStateOf(GSYGestureUpdate(GSYGestureType.None, 0f)) }

    /**
     * 安全读取当前锁屏态。controller.snapshot 在 Compose 上下文里就是 mutableStateOf，
     * 我们只在事件入口（onDragStart）读取，避免在 pointerInput lambda 里重新订阅。
     */
    fun isLocked(): Boolean = controller.snapshot.value.isLocked

    fun horizontalEnabled(): Boolean = enableSeek &&
        !isLocked() &&
        controller.snapshot.value.duration > 0L

    fun verticalEnabled(startXLocal: Float): Boolean {
        if (isLocked()) return false
        if (size.width <= 0) return false
        val isLeftHalf = startXLocal < size.width / 2f
        return if (isLeftHalf) enableBrightness else enableVolume
    }

    this
        .onSizeChanged { size = it }
        .pointerInput(controller, enableSeek) {
            // 横向 drag → 调进度（松手 seek）
            detectHorizontalDragGestures(
                onDragStart = { offset ->
                    if (!horizontalEnabled()) {
                        sessionType = GSYGestureType.None
                        return@detectHorizontalDragGestures
                    }
                    sessionType = GSYGestureType.Progress
                    startPosition = controller.snapshot.value.currentPosition
                    lastSeekTarget = startPosition
                },
                onDragEnd = {
                    if (sessionType == GSYGestureType.Progress) {
                        controller.seekTo(lastSeekTarget)
                        onGestureCommit(lastUpdate.copy(type = GSYGestureType.Progress))
                    }
                    sessionType = GSYGestureType.None
                },
                onDragCancel = { sessionType = GSYGestureType.None },
                onHorizontalDrag = { change, dragAmount ->
                    if (sessionType != GSYGestureType.Progress) return@detectHorizontalDragGestures
                    change.consume()
                    val duration = controller.snapshot.value.duration.coerceAtLeast(0L)
                    if (duration <= 0L || size.width <= 0) return@detectHorizontalDragGestures
                    // 与原 ControlView 一致：水平拖到底 = 当前位置 ± duration
                    val totalDeltaX = change.position.x - change.previousPosition.x + (lastSeekTarget - startPosition).toFloat()
                    // 简单线性映射：1 屏宽 = 整片 duration（与 GSYVideoControlView.touchSurfaceMove 同思路）
                    val deltaMs = (dragAmount / size.width.toFloat() * duration).toLong()
                    lastSeekTarget = (lastSeekTarget + deltaMs).coerceIn(0L, duration)
                    val progress = lastSeekTarget.toFloat() / duration.toFloat()
                    val update = GSYGestureUpdate(
                        type = GSYGestureType.Progress,
                        progress = progress,
                        seekTimeMs = lastSeekTarget,
                        durationMs = duration,
                    )
                    lastUpdate = update
                    onGestureUpdate(update)
                },
            )
        }
        .pointerInput(controller, enableVolume, enableBrightness) {
            detectVerticalDragGestures(
                onDragStart = { offset ->
                    startX = offset.x
                    if (!verticalEnabled(startX)) {
                        sessionType = GSYGestureType.None
                        return@detectVerticalDragGestures
                    }
                    val isLeftHalf = startX < size.width / 2f
                    if (isLeftHalf) {
                        // 亮度
                        sessionType = GSYGestureType.Brightness
                        startBrightness = activity?.window?.attributes?.screenBrightness
                            ?.takeIf { it >= 0f } ?: 0.5f
                    } else {
                        // 音量
                        sessionType = GSYGestureType.Volume
                        startVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
                    }
                },
                onDragEnd = {
                    if (sessionType != GSYGestureType.None) {
                        // 音量/亮度无 commit 概念（每次拖动都已实时设过），只清状态
                        sessionType = GSYGestureType.None
                    }
                },
                onDragCancel = { sessionType = GSYGestureType.None },
                onVerticalDrag = { change, dragAmount ->
                    if (sessionType == GSYGestureType.None) return@detectVerticalDragGestures
                    change.consume()
                    val height = size.height.coerceAtLeast(1)
                    when (sessionType) {
                        GSYGestureType.Volume -> {
                            val am = audioManager ?: return@detectVerticalDragGestures
                            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                            // 与 GSYVideoControlView.touchSurfaceMove 同算法：deltaY = -dragAmount，
                            // ratio = max * (-dragAmount) * 3 / height
                            val accumDelta = (change.position.y - change.previousPosition.y).let {
                                // 取本帧与上一帧 y 差值积累，非纯瞬时
                                it + (-dragAmount)
                            }
                            // 直接按 dragAmount 累加更稳：每像素映射 (3 * max / height) 步
                            val stepFloat = (-dragAmount * 3f * max / height)
                            val newVol = (am.getStreamVolume(AudioManager.STREAM_MUSIC) + stepFloat.toInt())
                                .coerceIn(0, max)
                            am.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                            val progress = newVol.toFloat() / max.toFloat()
                            val update = GSYGestureUpdate(
                                type = GSYGestureType.Volume,
                                progress = progress,
                            )
                            lastUpdate = update
                            onGestureUpdate(update)
                        }
                        GSYGestureType.Brightness -> {
                            val act = activity ?: return@detectVerticalDragGestures
                            // 算法与 GSYVideoControlView.onBrightnessSlide：
                            // percent = -dragAmount / height；total = startBrightness + percent；clamp [0.01,1]
                            val percent = -dragAmount / height.toFloat()
                            val cur = act.window.attributes.screenBrightness
                                .takeIf { it >= 0f } ?: 0.5f
                            val target = (cur + percent).coerceIn(0.01f, 1f)
                            val lp: WindowManager.LayoutParams = act.window.attributes
                            lp.screenBrightness = target
                            act.window.attributes = lp
                            val update = GSYGestureUpdate(
                                type = GSYGestureType.Brightness,
                                progress = target,
                            )
                            lastUpdate = update
                            onGestureUpdate(update)
                        }
                        else -> Unit
                    }
                },
            )
        }
}

/**
 * 从 Context 寻找 Activity（穿过 ContextWrapper 链）。
 * findActivity 在 androidx 里没有公共版本，自己写一个。
 */
private fun Context.findActivityOrNull(): Activity? {
    var ctx: Context? = this
    while (ctx is android.content.ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
