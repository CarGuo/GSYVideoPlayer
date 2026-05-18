package com.shuyu.gsyvideoplayer.compose.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * 将宿主的生命周期事件桥接到一个回调，
 * 用于在 Compose 中处理 onPause / onResume / onDestroy 等。
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
