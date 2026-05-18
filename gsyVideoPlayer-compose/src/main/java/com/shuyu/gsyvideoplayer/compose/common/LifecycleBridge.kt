package com.shuyu.gsyvideoplayer.compose.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * 将宿主的生命周期事件桥接到一个回调，
 * 用于在 Compose 中处理 onPause / onResume / onDestroy 等。
 *
 * 设计选型说明（与 androidx.lifecycle:lifecycle-runtime-compose 2.8+ 自带的
 * `LifecycleEventEffect(event = ON_RESUME) { ... }` 的关系）：
 *
 * - 官方的 `LifecycleEventEffect` 一次只接收**一个特定** `Lifecycle.Event`，
 *   想同时处理 ON_PAUSE / ON_RESUME / ON_DESTROY 需要写 3 次 Effect。
 * - 本桥接 `LifecycleEffect { event -> when(event) { ... } }` 一次拿到**所有** event，
 *   非常贴合 Wrapper 模式（同一个 player 实例跨 ON_RESUME / ON_PAUSE / ON_DESTROY 联动）的写法。
 *
 * 因此两者并非简单替代关系——**本桥接保留**，新代码若只关心单个事件、推荐直接用官方
 * `LifecycleEventEffect`；需要多事件集中分发时再用本工具。
 */
@Composable
fun LifecycleEffect(onEvent: (Lifecycle.Event) -> Unit) {
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event -> onEvent(event) }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
}
