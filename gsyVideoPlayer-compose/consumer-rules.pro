# Consumer ProGuard rules for gsyvideoplayer-compose.
#
# Compose 库自身不需要额外保留规则：
#   - androidx.compose.* 由 Google 官方 Compose 库随依赖自带 consumer rules；
#   - 本模块只是在 Compose 之上的薄包装，没有反射 / Native 调用 / Service / 序列化点。
#
# 该文件存在的目的，是满足上层 lib.gradle 中 `consumerProguardFiles "consumer-rules.pro"`
# 的引用，避免下游依赖方在打开 R8/Proguard 时出现 "Missing consumer ProGuard file" 警告。
