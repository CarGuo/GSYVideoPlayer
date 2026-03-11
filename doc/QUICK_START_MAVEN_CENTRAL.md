# 🚀 快速开始: Maven Central 自动发布

## 一句话总结
将手动的 `gradlew publishToMavenLocal` + `mc.ps1` + 手动上传流程，变成 **打 tag 即自动发布**。

## 📦 双渠道发布

项目支持同时发布到两个渠道：

| 渠道 | GROUP ID | 说明 |
|-----|----------|------|
| **GitHub Packages** | `com.shuyu` | 需要 GitHub token，现有配置 |
| **Maven Central** | `io.github.carguo` | 公开访问，新增配置 |

详见 [双渠道发布指南](./DUAL_CHANNEL_PUBLISH.md)

---

## ⚡ 5 分钟设置

### 1️⃣ 配置 GitHub Secrets
在 GitHub 仓库设置 4 个 Secrets:
- `MAVEN_CENTRAL_USERNAME` - Maven Central 用户名
- `MAVEN_CENTRAL_PASSWORD` - Maven Central Token
- `GPG_PRIVATE_KEY` - GPG 私钥 (base64)
- `GPG_PASSPHRASE` - GPG 密钥密码

### 2️⃣ 更新模块配置
在需要发布到 Maven Central 的模块 `build.gradle` 中，**添加**:
```gradle
// 保留原有的 GitHub Packages 配置
apply from: "$rootDir/gradle/publish.gradle"

// 新增 Maven Central 配置
apply from: "$rootDir/gradle/maven-central-publish.gradle"
```

**注意**：不要替换，是**同时使用**两个配置！这样可以：
- ✅ 继续发布到 GitHub Packages (`com.shuyu`)
- ✅ 同时发布到 Maven Central (`io.github.carguo`)

详见 [双渠道发布指南](./DUAL_CHANNEL_PUBLISH.md)

### 3️⃣ 发布!
```bash
git tag v1.0.0
git push origin v1.0.0
```

搞定! 🎉

---

## 📖 详细文档
查看 [完整指南](./MAVEN_CENTRAL_PUBLISH.md) 了解:
- ✅ 如何注册 Maven Central
- ✅ 如何导出 GPG 密钥
- ✅ 如何本地调试 (act 工具)
- ✅ 常见问题解决

---

## 🧪 本地测试

### 方法 1: 测试脚本 (推荐)
```powershell
# 设置环境变量
$env:GPG_PASSPHRASE = "your-passphrase"
$env:MAVEN_CENTRAL_USERNAME = "your-username"
$env:MAVEN_CENTRAL_PASSWORD = "your-token"

# 运行测试
.\test-maven-publish.ps1
```

### 方法 2: act 工具
```powershell
# 安装
choco install act-cli

# 测试 workflow
act workflow_dispatch -W .github/workflows/publish-maven-central.yml
```

---

## 🆚 新旧对比

| | ❌ 旧流程 | ✅ 新流程 |
|---|---|---|
| 步骤 | 1. 本地构建<br>2. mc.ps1 签名<br>3. 手动上传 | 1. `git push origin v1.0.0`<br>**完** |
| 时间 | ~30 分钟 | ~5 分钟 (自动) |
| 易错 | 容易忘记步骤 | 全自动 |
| 环境 | 需要本地配置 | GitHub 云端 |

---

## 🔗 相关文件
- [GitHub Actions Workflow](./.github/workflows/publish-maven-central.yml)
- [Gradle 配置](./gradle/maven-central-publish.gradle)
- [测试脚本](./test-maven-publish.ps1)
- [完整文档](./MAVEN_CENTRAL_PUBLISH.md)
