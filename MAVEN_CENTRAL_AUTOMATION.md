# Maven Central 自动发布

## 🎯 改进内容

原来的手动流程:
1. `gradlew publishToMavenLocal` 
2. `mc.ps1` 重签名
3. 手动上传到 Maven Central

现在的自动流程:
1. `git tag v1.0.0 && git push origin v1.0.0`
2. **完成!** 🎉

## 📦 双渠道发布

支持同时发布到两个渠道，使用不同的 GROUP ID：

| 渠道 | GROUP ID | 依赖示例 |
|-----|----------|---------|
| **GitHub Packages** | `com.shuyu` | `implementation 'com.shuyu:gsyvideoplayer-java:11.3.0'` |
| **Maven Central** | `io.github.carguo` | `implementation 'io.github.carguo:gsyvideoplayer-java:11.3.0'` |

两个渠道**互不干扰**，可以同时使用！

详见 [双渠道发布指南](./doc/DUAL_CHANNEL_PUBLISH.md)

## 📚 文档

- [快速开始指南](./doc/QUICK_START_MAVEN_CENTRAL.md) - 5 分钟设置
- [完整文档](./doc/MAVEN_CENTRAL_PUBLISH.md) - 详细说明和本地调试

## 🧪 本地调试

### 快速测试
```powershell
.\test-maven-publish.ps1
```

### 使用 act 调试 GitHub Actions
```powershell
# 安装 act (一次性)
choco install act-cli

# 复制并配置 secrets
copy .secrets.example .secrets
# 编辑 .secrets 填入你的凭证

# 运行 workflow
act workflow_dispatch -W .github\workflows\publish-maven-central.yml --secret-file .secrets
```

## 📁 新增文件

- `.github/workflows/publish-maven-central.yml` - GitHub Actions workflow
- `gradle/maven-central-publish.gradle` - Maven Central 发布配置
- `test-maven-publish.ps1` - 本地测试脚本
- `.secrets.example` - act 测试用 secrets 模板
- `doc/QUICK_START_MAVEN_CENTRAL.md` - 快速指南
- `doc/MAVEN_CENTRAL_PUBLISH.md` - 完整文档

## ⚙️ 如何使用

### 1. 设置 GitHub Secrets

在 GitHub 仓库设置这 4 个 Secrets:
- `MAVEN_CENTRAL_USERNAME`
- `MAVEN_CENTRAL_PASSWORD`
- `GPG_PRIVATE_KEY`
- `GPG_PASSPHRASE`

### 2. 更新模块配置

在要发布到 Maven Central 的模块 `build.gradle` 中，**添加**:

```gradle
// 保留原有的 GitHub Packages 配置
apply from: "$rootDir/gradle/publish.gradle"

// 新增 Maven Central 配置 (不是替换!)
apply from: "$rootDir/gradle/maven-central-publish.gradle"

// 保留原有的 publishing 配置
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
- GitHub Packages: `com.shuyu:module:version`
- Maven Central: `io.github.carguo:module:version`

### 3. 发布

```bash
git tag v1.0.0
git push origin v1.0.0
```

或手动触发: GitHub → Actions → "Publish to Maven Central" → Run workflow

---

详见 [文档](./doc/QUICK_START_MAVEN_CENTRAL.md)
