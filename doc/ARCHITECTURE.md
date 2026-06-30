# 架构说明

## 近期播放能力补充

近期播放相关修改主要遵循两个原则：播放内核只负责媒体解码和时间线，跨内核能力尽量放在播放器 UI 或 GSY 管理层；Demo 级能力保持入口清晰，避免改变全局默认行为。

| 能力 | 所在层级 | 设计说明 |
| --- | --- | --- |
| 通用外挂字幕 | UI overlay | `GSYSubtitleController` 根据播放器进度驱动 `GSYSubtitleView`，SRT/WebVTT 加载失败只影响字幕显示，不影响内核 prepare/play。 |
| WebVTT 预览 | Demo + preview provider | `PreViewGSYVideoPlayer` 只消费 `GSYVideoPreviewProvider`，缩略图生成和 sprite 坐标由服务端或业务侧提供。 |
| 截图 | Render + Standard player | `taskShotPic/saveFrame` 保持视频帧语义，`taskShotPicWithView/saveFrameWithView` 表达包含播放器 UI 的组合截图语义。 |
| GLSurfaceView 效果 | Render | GL renderer 负责纹理、滤镜、截图和 release；Demo 进入时切 GL 渲染，退出恢复原 render type。 |
| 多 URL 清晰度切换 | Demo player + manager | `SmartPickVideo` 使用临时 manager 预加载新 URL，同步位置后提交；异常时回退原播放。 |
| Exo 自适应清晰度 | Exo manager | HLS master / DASH MPD 使用同一个媒体时间线，清晰度自动选择交给 Media3 TrackSelector；固定清晰度通过 TrackSelectionOverride 实现。 |
| 完成后保留最后一帧 | Demo player | `KeepLastFrameVideo` 只作为 Demo 级验证，不改变基础播放器默认完成态和释放策略。 |
| 播放器初始化失败处理 | Manager + Player | `GSYVideoBaseManager` 和各 `IPlayerManager` 将内核创建/初始化异常收敛到错误回调和资源清理。 |
| Exo cache 与 GIF 清理 | Cache + Utils | `ExoSourceManager` 管理 Exo cache 生命周期，`GifCreateHelper` 负责 GIF 生成状态和临时资源清理。 |

更多入口、API 和回归说明见 [RECENT_FEATURES.md](RECENT_FEATURES.md)。

## 双渠道发布架构说明

## 📊 发布流程对比

```
┌─────────────────────────────────────────────────────────────┐
│                      Git Tag 触发                            │
│                   git tag v13.1.0                           │
│                git push origin v13.1.0                      │
└─────────────────┬───────────────────────────┬───────────────┘
                  │                           │
    ┌─────────────▼─────────────┐  ┌─────────▼──────────────┐
    │   release.yml (已有)      │  │ publish-maven-central  │
    │                           │  │    .yml (新增)         │
    │ GitHub Packages 发布      │  │  Maven Central 发布    │
    └─────────────┬─────────────┘  └──────────┬─────────────┘
                  │                           │
    ┌─────────────▼─────────────┐  ┌─────────▼──────────────┐
    │  publish.gradle           │  │ maven-central-publish  │
    │                           │  │    .gradle             │
    │  GROUP: com.shuyu        │  │  GROUP: io.github.     │
    │                           │  │         carguo         │
    │  Publication: release     │  │  Publication:          │
    │                           │  │    mavenCentral        │
    └─────────────┬─────────────┘  └──────────┬─────────────┘
                  │                           │
                  ▼                           ▼
    ┌─────────────────────────┐  ┌──────────────────────────┐
    │   GitHub Packages       │  │    Maven Central         │
    │                         │  │                          │
    │ com.shuyu:              │  │ io.github.carguo:        │
    │   gsyvideoplayer-java:  │  │   gsyvideoplayer-java:   │
    │   13.1.0                │  │   13.1.0                 │
    └─────────────────────────┘  └──────────────────────────┘
```

## 🔧 配置文件结构

```
GSYVideoPlayer/
├── gradle.properties
│   ├── PROJ_GROUP=com.shuyu                    ← GitHub Packages
│   └── PROJ_GROUP_MAVEN_CENTRAL=io.github.carguo ← Maven Central
│
├── gradle/
│   ├── publish.gradle                          ← GitHub Packages 配置
│   │   └── publication "release"
│   │       └── group = PROJ_GROUP (com.shuyu)
│   │
│   └── maven-central-publish.gradle            ← Maven Central 配置
│       └── publication "mavenCentral"
│           └── group = PROJ_GROUP_MAVEN_CENTRAL (io.github.carguo)
│
├── gsyVideoPlayer-java/
│   ├── build.gradle
│   │   ├── apply from: "$rootDir/gradle/publish.gradle"
│   │   ├── apply from: "$rootDir/gradle/maven-central-publish.gradle"
│   │   └── publishing { repositories { maven { ... } } }
│   │
│   └── gradle.properties
│       └── PROJ_ARTIFACTID=gsyvideoplayer-java
│
└── .github/workflows/
    ├── release.yml                             ← GitHub Packages workflow
    │   └── ./gradlew publish
    │
    └── publish-maven-central.yml               ← Maven Central workflow
        └── ./gradlew publishToSonatype closeAndRelease...
```

## 📝 Publication 命名区分

为了避免冲突，两个发布使用**不同的 publication 名称**：

| 配置文件 | Publication 名称 | 用途 |
|---------|-----------------|------|
| `publish.gradle` | `release` | GitHub Packages |
| `maven-central-publish.gradle` | `mavenCentral` | Maven Central |

这样在 Gradle tasks 中可以明确区分：
```bash
# GitHub Packages
./gradlew publishReleasePublicationToGsyvideoplayerRepository

# Maven Central
./gradlew publishMavenCentralPublicationToSonatypeRepository
```

## 🎯 用户使用场景

### 场景 1: GitHub 用户 (需要 token)
```gradle
repositories {
    maven {
        url = "https://maven.pkg.github.com/CarGuo/GSYVideoPlayer"
        credentials {
            username = "your-github-username"
            password = "your-github-token"
        }
    }
}

dependencies {
    implementation 'com.shuyu:gsyvideoplayer-java:13.1.0'
}
```

### 场景 2: Maven Central 用户 (公开访问)
```gradle
repositories {
    mavenCentral()  // 不需要额外配置!
}

dependencies {
    implementation 'io.github.carguo:gsyvideoplayer-java:13.1.0'
}
```

## 💡 关键设计

1. **独立的 GROUP ID** - 通过 `PROJ_GROUP_MAVEN_CENTRAL` 实现
2. **独立的 Publication** - 避免命名冲突
3. **独立的 Workflow** - 互不干扰，可以单独触发
4. **共享的 VERSION** - 确保版本一致性

## ⚠️ 迁移建议

对于用户：
- **老用户**: 继续使用 `com.shuyu` (无需修改)
- **新用户**: 推荐使用 `io.github.carguo` (不需要 GitHub token)

在 README 中提供两种依赖方式，并说明区别。
