# Maven Central 自动发布（v14.0.0）

本项目在 `v*` tag 上分别触发 GitHub Release / GitHub Packages 和 Maven Central 工作流。版本来自根 `gradle.properties` 的 `PROJ_VERSION`，当前为 `14.0.0`；不同 group 通过 `PUBLISH_TARGET` 分两次 Gradle 调用发布。

| 渠道 | 依赖示例 |
| --- | --- |
| GitHub Packages | `com.shuyu:gsyvideoplayer:14.0.0` |
| Maven Central | `io.github.carguo:gsyvideoplayer:14.0.0` |

- [双渠道配置与准确命令](doc/DUAL_CHANNEL_PUBLISH.md)
- [快速开始](doc/QUICK_START_MAVEN_CENTRAL.md)
- [凭据、签名、staging 与故障排查](doc/MAVEN_CENTRAL_PUBLISH.md)
- [v14 全部提交、发布产物与验证记录](doc/V14_RELEASE_REVIEW.md)

## 必要配置

现有发布模块同时应用 `gradle/publish.gradle` 和 `gradle/maven-central-publish.gradle`。GitHub Packages 使用工作流的 `GITHUB_TOKEN`；Maven Central 配置 `MAVEN_CENTRAL_USERNAME`、`MAVEN_CENTRAL_PASSWORD`、`GPG_PRIVATE_KEY`、`GPG_PASSPHRASE` 四个 Secrets，凭据应为 Central Portal User Token。

## 本地验证

```sh
./gradlew publishToMavenLocal verifyCastDependencyIsolation -PPUBLISH_TARGET=github
./gradlew publishToMavenLocal verifyCastDependencyIsolation -PPUBLISH_TARGET=mavenCentral
```

这些命令不上传远端。上传/close/release 任务应按双渠道指南显式指定目标。旧 `test-maven-publish.ps1` / `mc.ps1` 为历史辅助脚本，未按当前双目标签名流程更新，不作为本版推荐验证入口。

## 发布结果确认

打新 tag 前核对版本与提交；已发布的 `v14.0.0` 不覆盖。检查两条工作流各自的最终结果、GitHub APK、Central staging/Portal 状态和公开 POM/AAR。流水线上传成功不能代替公开仓库可下载确认。
