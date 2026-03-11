# 双渠道发布架构说明

## 📊 发布流程对比

```
┌─────────────────────────────────────────────────────────────┐
│                      Git Tag 触发                            │
│                   git tag v11.3.0                           │
│                git push origin v11.3.0                      │
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
    │   11.3.0                │  │   11.3.0                 │
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
    implementation 'com.shuyu:gsyvideoplayer-java:11.3.0'
}
```

### 场景 2: Maven Central 用户 (公开访问)
```gradle
repositories {
    mavenCentral()  // 不需要额外配置!
}

dependencies {
    implementation 'io.github.carguo:gsyvideoplayer-java:11.3.0'
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
