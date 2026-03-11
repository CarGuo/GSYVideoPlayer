# Maven Central 自动发布指南

## 🎯 概述

这个指南帮助你将项目自动发布到 Maven Central，并提供本地调试 GitHub Actions 的方法。

---

## 📋 前提准备

### 1. 注册 Maven Central 账号

Maven Central 已迁移到新的门户系统：

1. 访问 https://central.sonatype.com/
2. 使用 GitHub 账号登录
3. 创建 Namespace (例如: `io.github.carguo`)
4. 验证 Namespace 所有权 (通过 GitHub repo 或 DNS)

### 2. 准备 GPG 密钥

如果已有 Kleopatra 生成的密钥:

```powershell
# 导出私钥 (会提示输入密码)
gpg --armor --export-secret-keys YOUR_KEY_ID > private-key.asc

# 查看密钥 ID
gpg --list-secret-keys --keyid-format=long

# 将私钥转为 base64 (用于 GitHub Secrets)
$content = Get-Content private-key.asc -Raw
$bytes = [System.Text.Encoding]::UTF8.GetBytes($content)
[Convert]::ToBase64String($bytes) | Set-Clipboard
```

### 3. 配置 GitHub Secrets

在你的 GitHub 仓库设置 Secrets (Settings → Secrets and variables → Actions):

| Secret 名称 | 说明 | 获取方式 |
|------------|------|---------|
| `MAVEN_CENTRAL_USERNAME` | Maven Central 用户名 | 从 https://central.sonatype.com/ Account 页面获取 |
| `MAVEN_CENTRAL_PASSWORD` | Maven Central 密码/Token | 从 https://central.sonatype.com/ 生成 User Token |
| `GPG_PRIVATE_KEY` | GPG 私钥 (base64) | 使用上面的命令导出 |
| `GPG_PASSPHRASE` | GPG 密钥密码 | 你的 GPG 密钥密码 |

**重要**: Maven Central 新系统建议使用 User Token 而非密码

---

## 🚀 使用方法

### 自动触发 (推荐)

打 tag 时自动发布:

```bash
git tag v1.0.0
git push origin v1.0.0
```

### 手动触发

1. 进入 GitHub Actions 页面
2. 选择 "Publish to Maven Central" workflow
3. 点击 "Run workflow"
4. 输入版本号
5. 点击 "Run workflow" 确认

---

## 🔧 本地调试 GitHub Actions

### 方法 1: 使用 act (推荐)

`act` 允许你在本地运行 GitHub Actions。

#### 安装 act

```powershell
# 使用 Chocolatey
choco install act-cli

# 或使用 Scoop
scoop install act

# 或下载二进制文件
# https://github.com/nektos/act/releases
```

#### 基本使用

```powershell
# 列出所有 workflows
act -l

# 运行特定 workflow (使用 workflow dispatch)
act workflow_dispatch -W .github/workflows/publish-maven-central.yml

# 使用 secrets (创建 .secrets 文件)
act -s MAVEN_CENTRAL_USERNAME=youruser -s MAVEN_CENTRAL_PASSWORD=yourpass

# 或使用 secrets 文件
# 创建 .secrets 文件:
# MAVEN_CENTRAL_USERNAME=youruser
# MAVEN_CENTRAL_PASSWORD=yourpass
# GPG_PASSPHRASE=yourpassphrase
act workflow_dispatch --secret-file .secrets

# 使用输入参数
act workflow_dispatch -W .github/workflows/publish-maven-central.yml --input version=1.0.0

# Dry-run (不实际执行)
act -n

# 使用更大的 Docker 镜像 (包含更多工具)
act -P ubuntu-latest=catthehacker/ubuntu:full-latest
```

#### 调试技巧

```powershell
# 详细日志
act -v

# 交互式 shell (workflow 失败时)
act workflow_dispatch --container-architecture linux/amd64 -s GITHUB_TOKEN=fake

# 重用 Docker 容器 (加快调试)
act --reuse
```

### 方法 2: 本地模拟脚本

创建一个本地测试脚本 `test-publish.ps1`:

```powershell
# test-publish.ps1
# 模拟 GitHub Actions 环境变量

$env:MAVEN_CENTRAL_USERNAME = "your-username"
$env:MAVEN_CENTRAL_PASSWORD = "your-password"
$env:GPG_PASSPHRASE = "your-gpg-passphrase"

# 可选: 导入 GPG 密钥
# gpg --import path/to/private-key.asc

# 测试构建和发布到本地
Write-Host "Testing build and publish to MavenLocal..."
./gradlew clean publishToMavenLocal

# 检查输出
$mavenLocalPath = "$env:USERPROFILE\.m2\repository"
Write-Host "Check artifacts at: $mavenLocalPath"

# 测试签名
Write-Host "`nTesting signing..."
./gradlew signReleasePublication

Write-Host "`nDone! Check the output above for errors."
```

### 方法 3: 分步验证

不使用完整 workflow，分步测试:

```powershell
# 1. 测试构建
./gradlew clean build

# 2. 测试发布到本地 (不需要凭证)
./gradlew publishToMavenLocal

# 3. 测试签名 (需要 GPG 设置)
$env:GPG_PASSPHRASE = "your-passphrase"
./gradlew signReleasePublication

# 4. 查看将要发布的内容
./gradlew publishToMavenLocal --dry-run

# 5. 查看所有发布任务
./gradlew tasks --group publishing
```

---

## 📝 更新现有模块

如果你想让现有模块使用新的 Maven Central 发布配置，替换 `build.gradle` 中的:

```gradle
// 将
apply from: "$rootDir/gradle/publish.gradle"

// 替换为
apply from: "$rootDir/gradle/maven-central-publish.gradle"
```

或者同时支持两者:

```gradle
// GitHub Packages
apply from: "$rootDir/gradle/publish.gradle"

// Maven Central  
apply from: "$rootDir/gradle/maven-central-publish.gradle"
```

---

## ⚠️ 注意事项

### GPG 密钥要求

- 密钥长度至少 2048 位
- 必须上传公钥到密钥服务器:
  ```bash
  gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
  gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID
  ```

### 首次发布

- Maven Central 审核可能需要几小时到一天
- 之后的发布会自动同步 (约 10-30 分钟)

### 版本号

- SNAPSHOT 版本会发布到 snapshot 仓库
- Release 版本会发布到正式仓库并自动 release

### 本地调试限制

`act` 的限制:
- 某些 GitHub-specific 功能可能不可用
- 需要 Docker (Windows 上建议使用 WSL2)
- 大型 workflow 可能很慢

---

## 🔍 故障排查

### GPG 签名失败

```bash
# 检查密钥
gpg --list-keys

# 测试签名
echo "test" > test.txt
gpg --sign test.txt
rm test.txt*
```

### Maven Central 认证失败

1. 确认 User Token 正确
2. 验证 Namespace 已激活
3. 检查 Secrets 是否正确设置

### act 运行失败

```powershell
# 更新 act
choco upgrade act-cli

# 使用最新的 runner 镜像
act -P ubuntu-latest=catthehacker/ubuntu:full-latest

# 检查 Docker 是否运行
docker ps
```

---

## 📚 参考资源

- [Maven Central Portal](https://central.sonatype.com/)
- [Maven Central 发布指南](https://central.sonatype.org/publish/publish-guide/)
- [act GitHub](https://github.com/nektos/act)
- [Gradle Nexus Publish Plugin](https://github.com/gradle-nexus/publish-plugin)

---

## 💡 保留原有 mc.ps1 脚本

原有的 `mc.ps1` 脚本仍然可用作备份方案。新的自动化流程本质上做了相同的事情:
1. ✅ 构建 artifacts
2. ✅ GPG 签名
3. ✅ 生成 checksums
4. ✅ 上传到 Maven Central

区别在于 GitHub Actions 会自动完成所有步骤。
