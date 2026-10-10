# Maven Central 自动发布与本地核对

> v14.0.0 / 2026-10-10：本文依据当前 Gradle 配置与工作流修订，替换旧默认目标、签名任务及固定同步时间的说法。

## 当前实现

本项目使用 Gradle Nexus Publish Plugin 2.0.0 与 Central Portal OSSRH Staging API。对应 [Sonatype 官方兼容指南](https://central.sonatype.org/publish/publish-portal-ossrh-staging-api/) 的 Nexus staging 流程，由插件创建 staging、上传、close/release；不要只执行不带目标参数的 `publishToSonatype`。

| 配置 | 作用 |
| --- | --- |
| [build.gradle](../build.gradle) | Nexus 插件、Central staging/snapshot endpoint、cast 发布隔离检查 |
| [gradle.properties](../gradle.properties) | `PROJ_VERSION` 与两组 GROUP ID |
| [maven-central-publish.gradle](../gradle/maven-central-publish.gradle) | `PUBLISH_TARGET=mavenCentral` 时创建 `mavenCentral` publication 并签名 |
| [publish.gradle](../gradle/publish.gradle) | 默认 `PUBLISH_TARGET=github` 时创建 `release` publication |
| [publish-maven-central.yml](../.github/workflows/publish-maven-central.yml) | JDK 21、导入 GPG、上传和 close/release |

两组 publication 在不同 Gradle 调用中生成，版本相同、group 不同。模块同时应用两个脚本，不用替换旧脚本破坏另一渠道。

## 账号、Namespace 与 Secrets

发布账号应持有 `io.github.carguo` Namespace，认证使用 **Central Portal User Token**，不是网页登录密码或旧 OSSRH token。配置四个 Secrets：

- `MAVEN_CENTRAL_USERNAME`：Portal User Token username。
- `MAVEN_CENTRAL_PASSWORD`：同一 token password。
- `GPG_PRIVATE_KEY`：ASCII-armored 私钥的 base64。
- `GPG_PASSPHRASE`：私钥口令。

本地认证也可用 Gradle 属性 `ossrhUsername` / `ossrhPassword`；签名脚本当前读取上述两个 GPG 环境变量。不要把凭据提交到仓库。

导出已用于签名的密钥，例如：

```sh
gpg --armor --export-secret-keys YOUR_KEY_ID > private-key.asc
# 把 private-key.asc 的完整内容转换为 base64，写入 GPG_PRIVATE_KEY secret。
# 按 Central 当前 GPG 要求提供对应公钥。
```

[GPG 要求](https://central.sonatype.org/publish/requirements/gpg/)与 [Portal token](https://central.sonatype.org/publish/generate-portal-token/)以官方说明为准。

## 本地构建、POM 与签名核对

分别核对两个目标，不上传远端：

```sh
./gradlew publishToMavenLocal verifyCastDependencyIsolation -PPUBLISH_TARGET=github
./gradlew publishToMavenLocal verifyCastDependencyIsolation -PPUBLISH_TARGET=mavenCentral
```

核对全部模块版本、artifactId、内部依赖 group、AAR 实际 ABI 与配套 so；默认整包/Java 不应引入 jUPnP/Jetty，只有 cast 保留它们。签名需先提供 GPG 环境变量，再运行：

```sh
./gradlew signMavenCentralPublication -PPUBLISH_TARGET=mavenCentral
```

`GPG_PASSPHRASE` 单独存在不会创建 signing 配置；私钥与口令须同时提供。旧 `signReleasePublication` 不是当前 Central 目标的签名任务。历史 PowerShell 辅助脚本未适配此流程，使用以上准确命令。

## 发布

`v*` tag 触发当前工作流。创建新 tag 前确认 `PROJ_VERSION` 与 tag 去掉 `v` 后一致，并确认 tag 指向已经验证的提交；已发布版本不覆盖。

本地实际上传和关闭/release 命令与 CI 一致：

```sh
./gradlew publishMavenCentralPublicationToSonatypeRepository closeAndReleaseSonatypeStagingRepository -PPUBLISH_TARGET=mavenCentral
```

手动 workflow 允许填写 version，但当前脚本并不据此修改属性或自动切换 ref；实际版本仍取所选 ref 的 `PROJ_VERSION`。选择正确 ref 后再核对。

## 结果确认与排查

- **认证失败**：核对 Portal token 与 Namespace 权限，不能用旧 OSSRH token。
- **签名任务缺失或无签名**：核对 `PUBLISH_TARGET=mavenCentral`、两个 GPG 环境变量和签名日志。
- **任务 NO-SOURCE / 未上传**：核对目标参数、publication 创建与每个模块的实际上传任务。
- **staging 失败**：查看 initialize、upload、close 和 release 的实际日志与 Portal deployment 状态。
- **公开仓库仍 404**：确认发布状态与目标版本，在 `repo.maven.apache.org/maven2/io/github/carguo/<artifact>/<version>/` 检查 POM/AAR；上传或工作流成功不等于已可下载，不保证固定同步时间。
- **重复发布**：已存在的同版本产物不覆盖；发布修正应使用新的版本身份。

v14.0.0 的实际工作流、产物和验证范围见 [V14_RELEASE_REVIEW.md](V14_RELEASE_REVIEW.md)。更多目标配置见 [DUAL_CHANNEL_PUBLISH.md](DUAL_CHANNEL_PUBLISH.md)，使用方依赖见 [DEPENDENCIES.md](DEPENDENCIES.md)。
