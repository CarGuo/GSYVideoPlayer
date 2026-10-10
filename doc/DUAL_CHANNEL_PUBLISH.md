# 双渠道发布配置指南（v14.0.0）

> 2026-10-10 校正：两个渠道共享版本，但由 `PUBLISH_TARGET` 分别选择 publication；同一次 Gradle 调用不会创建两个 publication。实际配置见 [publish.gradle](../gradle/publish.gradle)、[maven-central-publish.gradle](../gradle/maven-central-publish.gradle) 和 [根构建配置](../build.gradle)。

| 渠道 | GROUP ID | PUBLISH_TARGET | Publication |
| --- | --- | --- | --- |
| GitHub Packages | `com.shuyu` | `github`（默认） | `release` |
| Maven Central | `io.github.carguo` | `mavenCentral` | `mavenCentral` |

当前 `PROJ_VERSION=14.0.0`，各模块 artifactId 以自身 `gradle.properties` 为准。模块同时应用两个脚本；目标属性决定本次创建哪一个 publication 和内部依赖坐标。跨渠道发布任务有禁用守卫，不能省略 Maven Central 的目标参数。

## 本地核对

以下命令生成本地产物及 POM，不上传远端；两组分别执行：

```sh
./gradlew publishToMavenLocal verifyCastDependencyIsolation -PPUBLISH_TARGET=github
./gradlew publishToMavenLocal verifyCastDependencyIsolation -PPUBLISH_TARGET=mavenCentral
```

Maven Central 签名使用 `GPG_PRIVATE_KEY`（base64 ASCII-armored 私钥）和 `GPG_PASSPHRASE` 两个环境变量；凭据配置见 [完整指南](MAVEN_CENTRAL_PUBLISH.md)。检查所有模块版本一致，内部坐标使用对应 group，默认 java/整包没有 jUPnP/Jetty，只有可选 cast 包含这些依赖。

## 实际上传任务

GitHub Packages 使用 `GITHUB_ACTOR` / `GITHUB_TOKEN`：

```sh
./gradlew publishReleasePublicationToGsyvideoplayerRepository -PPUBLISH_TARGET=github
```

Maven Central 使用 Portal User Token 及 GPG 环境变量：

```sh
./gradlew publishMavenCentralPublicationToSonatypeRepository closeAndReleaseSonatypeStagingRepository -PPUBLISH_TARGET=mavenCentral
```

本项目通过 Gradle Nexus Publish Plugin 2.0.0 管理 staging 创建、关闭与 release，配置的是 Central Portal OSSRH Staging API。参考 [Sonatype 官方兼容指南](https://central.sonatype.org/publish/publish-portal-ossrh-staging-api/)。只有上传成功或 Gradle 命令成功，不等于用户已能从公开仓库下载；检查 staging/Portal 状态与最终 POM/AAR。

## tag 与工作流

[release.yml](../.github/workflows/release.yml) 在 `v*` tag 上构建 Release APK、创建 GitHub Release 并发布 GitHub Packages；[publish-maven-central.yml](../.github/workflows/publish-maven-central.yml) 同时发布 Maven Central。两者使用 JDK 21，独立运行；普通 master 文档提交只触发 CI。

为新版本先修改 `PROJ_VERSION`、核对对应提交，再创建与版本一致的 annotated tag：

```sh
# 替换为尚未发布的新版本；已发布的 v14.0.0 不重新创建或覆盖。
release_tag="v<new-version>"
git tag -a "$release_tag" -m "$release_tag"
git push origin "$release_tag"
```

当前手动 workflow 的 `version` 输入不会自动改写 `PROJ_VERSION`；发布前必须核对所选 ref 的属性，不能把输入值当作最终产物版本。

## 使用方

推荐 Maven Central：

```groovy
repositories { mavenCentral() }
dependencies {
    implementation 'io.github.carguo:gsyvideoplayer:14.0.0'
    // 可选投屏：API 26
    implementation 'io.github.carguo:gsyvideoplayer-cast:14.0.0'
}
```

GitHub Packages 改用 `com.shuyu` 并配置个人 `read:packages` 凭据。不要在同一应用同时引入两组相同播放器模块，避免重复类/so。ABI 和模块组合见 [DEPENDENCIES.md](DEPENDENCIES.md)，本版发布流水线与产物身份见 [V14_RELEASE_REVIEW.md](V14_RELEASE_REVIEW.md)。
