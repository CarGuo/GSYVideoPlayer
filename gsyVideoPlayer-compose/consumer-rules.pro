# Consumer ProGuard rules for gsyvideoplayer-compose.
#
# 该模块是 Compose 之上的薄包装：没有反射 / Native 调用 / Service / 序列化点，
# androidx.compose.* 由 Google 官方 Compose 库随依赖自带 consumer rules，因此
# 大体上无须额外保留规则。
#
# 但是为了避免下游依赖方在打开 R8/Proguard 时把【公开 API 入口】或【回调签名】
# 误优化（例如把 GSYComposeHostPlayer 重命名 / 把 GSYPlayerEvent 当成"未使用"
# 内联掉），这里给出最小化的兜底 keep。这些规则 **不会** 阻止 R8 优化模块内部
# 实现，只是冻结对外契约的类名 + 公共方法签名。
#
# 该文件同时满足 lib.gradle 中 `consumerProguardFiles "consumer-rules.pro"` 的
# 引用要求，避免下游出现 "Missing consumer ProGuard file" 警告。

# ---- 公共 API：Native 模式入口 + 状态/事件 ----
-keep public class com.shuyu.gsyvideoplayer.compose.native_.GSYComposeHostPlayer { *; }
-keep public class com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerController { *; }
-keep public class com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerSnapshot { *; }
-keep public class com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerSurface { *; }

# GSYPlayerEvent 是 sealed hierarchy，全部子类都需要保留以便 SharedFlow 的下游
# 模式匹配（when / is 检查）在 R8 之后仍能命中具体类型。
-keep class com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerEvent { *; }
-keep class com.shuyu.gsyvideoplayer.compose.native_.GSYPlayerEvent$* { *; }

# ---- 公共 API：Wrapper 模式入口 ----
-keep class com.shuyu.gsyvideoplayer.compose.wrapper.GSYVideoPlayerViewKt { *; }
-keep class com.shuyu.gsyvideoplayer.compose.wrapper.GSYAnyVideoPlayerViewKt { *; }

# ---- 公共 API：生命周期桥 ----
-keep class com.shuyu.gsyvideoplayer.compose.common.LifecycleBridgeKt { *; }
