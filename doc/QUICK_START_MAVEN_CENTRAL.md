# Maven Central 发布快速开始

> v14.0.0 / 2026-10-10：使用当前双渠道目标配置；准确任务和实际工作流见 [DUAL_CHANNEL_PUBLISH.md](DUAL_CHANNEL_PUBLISH.md)。

## 1. 配置凭据与签名

在仓库 Actions Secrets 中配置：

| Secret | 内容 |
| --- | --- |
| `MAVEN_CENTRAL_USERNAME` | Central Portal User Token 的 username |
| `MAVEN_CENTRAL_PASSWORD` | 同一 User Token 的 password |
| `GPG_PRIVATE_KEY` | base64 编码的 ASCII-armored 私钥 |
| `GPG_PASSPHRASE` | 私钥口令 |

Namespace `io.github.carguo` 须由发布账号持有。导出和签名说明见 [完整指南](MAVEN_CENTRAL_PUBLISH.md)。

## 2. 核对模块与版本

模块同时应用 [GitHub 发布脚本](../gradle/publish.gradle) 与 [Central 发布脚本](../gradle/maven-central-publish.gradle)。版本来自根 `PROJ_VERSION`，artifactId 来自模块属性；不要用手动 workflow 的 version 输入代替属性修改。

```sh
./gradlew publishToMavenLocal verifyCastDependencyIsolation -PPUBLISH_TARGET=mavenCentral
```

检查生成 POM 的 `io.github.carguo` group、相同版本和内部依赖，以及可选 cast 的隔离。

## 3. 发布新版本

先完成构建/验证并提交版本修改，再对相应提交创建新 annotated `v*` tag。已发布的 v14.0.0 不重打；tag 会同时触发 [Release](../.github/workflows/release.yml) 和 [Publish to Maven Central](../.github/workflows/publish-maven-central.yml)。

Central 的实际任务为：

```sh
./gradlew publishMavenCentralPublicationToSonatypeRepository closeAndReleaseSonatypeStagingRepository -PPUBLISH_TARGET=mavenCentral
```

该命令会真实上传；本地核对使用第 2 步的 MavenLocal 命令。检查上传、close/release、Portal 状态及公开 POM/AAR 下载结果，不能只看总结步骤的成功文字。

## 更多资料

[完整指南](MAVEN_CENTRAL_PUBLISH.md) · [双渠道配置](DUAL_CHANNEL_PUBLISH.md) · [依赖接入](DEPENDENCIES.md) · [v14 发布核对](V14_RELEASE_REVIEW.md)
