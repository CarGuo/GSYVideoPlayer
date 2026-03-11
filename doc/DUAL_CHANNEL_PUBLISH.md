# 双渠道发布配置指南

## 🎯 问题说明

项目需要同时发布到两个渠道，使用不同的 GROUP ID：

| 渠道 | GROUP ID | 用途 |
|-----|----------|------|
| **GitHub Packages** | `com.shuyu` | 现有用户使用 |
| **Maven Central** | `io.github.carguo` | 公开分发 |

## ✅ 解决方案

### 1. Gradle 配置

在根目录 `gradle.properties` 中定义两个 GROUP ID：

```properties
# GitHub Packages 使用
PROJ_GROUP=com.shuyu

# Maven Central 使用
PROJ_GROUP_MAVEN_CENTRAL=io.github.carguo
```

### 2. 模块发布配置

每个模块的 `build.gradle` 可以同时支持两种发布：

```gradle
// GitHub Packages 发布 (保持原样)
apply from: "$rootDir/gradle/publish.gradle"

// Maven Central 发布 (新增)
apply from: "$rootDir/gradle/maven-central-publish.gradle"

// 发布到 GitHub Packages
publishing {
    repositories {
        maven {
            name = "gsyvideoplayer"
            url = "https://maven.pkg.github.com/CarGuo/GSYVideoPlayer"
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

### 3. 发布结果

**GitHub Packages (现有)**
```gradle
dependencies {
    implementation 'com.shuyu:gsyvideoplayer-java:11.3.0'
}
```

**Maven Central (新的)**
```gradle
dependencies {
    implementation 'io.github.carguo:gsyvideoplayer-java:11.3.0'
}
```

## 📦 发布任务

### GitHub Packages
```bash
# 自动触发 (已有)
git tag v11.3.0
git push origin v11.3.0
```

### Maven Central
```bash
# 自动触发 (新增)
git tag v11.3.0
git push origin v11.3.0
# 两个 workflow 会同时运行
```

或手动触发：
```bash
# GitHub Packages
./gradlew publish

# Maven Central
./gradlew publishMavenCentralPublicationToSonatypeRepository
```

## 🔍 验证配置

运行测试脚本：

```powershell
# 查看所有发布任务
./gradlew tasks --group publishing

# 你会看到：
# - publishReleasePublicationTo... (GitHub Packages)
# - publishMavenCentralPublicationTo... (Maven Central)
```

## 📋 Gradle Tasks 说明

| Task | 用途 | GROUP ID |
|------|------|----------|
| `publish` | 发布到 GitHub Packages | `com.shuyu` |
| `publishMavenCentralPublicationToSonatypeRepository` | 发布到 Maven Central | `io.github.carguo` |
| `publishToSonatype` | 发布到 Sonatype (Maven Central 前置) | `io.github.carguo` |
| `closeAndReleaseSonatypeStagingRepository` | 自动 release 到 Maven Central | `io.github.carguo` |

## 💡 最佳实践

### 1. 同时发布
```bash
# 一条命令同时发布到两个渠道
./gradlew publish publishToSonatype closeAndReleaseSonatypeStagingRepository
```

### 2. 单独发布

**只发布到 GitHub Packages**
```bash
./gradlew publish
```

**只发布到 Maven Central**
```bash
./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository
```

### 3. GitHub Actions 自动化

两个 workflow 会独立运行：
- `.github/workflows/release.yml` → GitHub Packages
- `.github/workflows/publish-maven-central.yml` → Maven Central

都会在打 tag 时触发，互不干扰。

## ⚠️ 注意事项

1. **不同的依赖声明**
   - 新用户应该使用 Maven Central: `io.github.carguo`
   - 老用户继续使用 GitHub Packages: `com.shuyu`

2. **版本同步**
   - 两个渠道使用相同的 `PROJ_VERSION`
   - 确保同时发布相同版本

3. **文档更新**
   - 在 README 中说明两种依赖方式
   - 推荐新用户使用 Maven Central (不需要 token)

## 📝 示例：更新模块配置

以 `gsyVideoPlayer-base` 为例：

```gradle
// gsyVideoPlayer-base/build.gradle
apply from: "$rootDir/gradle/lib.gradle"

// GitHub Packages (保持原样)
apply from: "$rootDir/gradle/publish.gradle"

// Maven Central (新增)
apply from: "$rootDir/gradle/maven-central-publish.gradle"

android {
    namespace 'com.shuyu.gsy.base'
}

dependencies {
    api viewDependencies.ijkplayer
}

// GitHub Packages 仓库配置
publishing {
    repositories {
        maven {
            name = "gsyvideoplayer"
            url = "https://maven.pkg.github.com/CarGuo/GSYVideoPlayer"
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

这样配置后：
- `./gradlew publish` → `com.shuyu:gsyvideoplayer-base:11.3.0` (GitHub)
- `./gradlew publishMavenCentralPublicationToSonatypeRepository` → `io.github.carguo:gsyvideoplayer-base:11.3.0` (Maven Central)

## 🎉 完成

现在你可以：
- ✅ 继续发布到 GitHub Packages (com.shuyu)
- ✅ 同时发布到 Maven Central (io.github.carguo)
- ✅ 两者互不干扰
- ✅ 自动化或手动都支持
