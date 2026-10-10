# v14.0.0 依赖接入摘要

本版默认 ABI 为 `arm64-v8a` / `armeabi-v7a` / `x86_64`；旧 `armeabi` / `x86` 未升级到 FFmpeg 5。完整坐标与组合规则见 [DEPENDENCIES.md](DEPENDENCIES.md)。

## 📦 依赖配置 (两种方式)

### 方式 1: Maven Central (推荐 - 公开访问)

```gradle
dependencies {
    // 核心库
    implementation 'io.github.carguo:gsyvideoplayer-java:14.0.0'

    // java 已通过 api 引入 base，无需重复声明

    // 按需选择 ABI；也可用 ex_so 代替下面三行
    implementation 'io.github.carguo:gsyvideoplayer-armv7a:14.0.0'
    implementation 'io.github.carguo:gsyvideoplayer-arm64:14.0.0'
    implementation 'io.github.carguo:gsyvideoplayer-x64:14.0.0'

    // Compose 可选支持
    implementation 'io.github.carguo:gsyvideoplayer-compose:14.0.0'

    // RTMP 可选直接依赖；gsyvideoplayer-exo2 已通过 api 传递
    implementation 'io.github.carguo:gsyvideoplayer-rtmp:14.0.0'

    // DLNA/UPnP 投屏可选依赖；默认播放器不包含 Jetty（该模块 minSdk 26）
    implementation 'io.github.carguo:gsyvideoplayer-cast:14.0.0'
    // ...
}
```

**优点**:
- ✅ 不需要配置 GitHub token
- ✅ 公开访问
- ✅ 与其他 Maven Central 依赖一致

---

### 方式 2: GitHub Packages (现有方式)

```gradle
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/CarGuo/GSYVideoPlayer")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.token") ?: System.getenv("TOKEN")
        }
    }
}

dependencies {
    implementation 'com.shuyu:gsyvideoplayer-java:14.0.0'
    implementation 'com.shuyu:gsyvideoplayer-ex_so:14.0.0'
    // 仅需要 DLNA/UPnP 投屏时添加
    implementation 'com.shuyu:gsyvideoplayer-cast:14.0.0'
}
```

**注意**: 
- ⚠️ 需要 GitHub token
- ⚠️ 需要额外配置 credentials

---

### 两种方式的区别

| | Maven Central | GitHub Packages |
|---|--------------|-----------------|
| GROUP ID | `io.github.carguo` | `com.shuyu` |
| 访问方式 | 公开 | 需要 token |
| 配置复杂度 | 简单 | 中等 |
| 推荐场景 | 新项目、公开项目 | 私有项目、已有配置 |

---

### 迁移说明

如果你当前使用 `com.shuyu`，可以继续使用，无需修改。

如果想切换到 Maven Central:
1. 移除 GitHub Packages 的 repository 配置
2. 将 `com.shuyu` 替换为 `io.github.carguo`
3. 完成！不需要 token 配置
