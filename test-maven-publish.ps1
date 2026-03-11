# 本地测试 Maven Central 发布流程
# 这个脚本帮助你在本地测试发布配置，无需实际上传到 Maven Central

param(
    [switch]$DryRun,
    [switch]$SignOnly,
    [string]$Version = ""
)

Write-Host "🔧 Maven Central 发布测试工具" -ForegroundColor Cyan
Write-Host "================================`n" -ForegroundColor Cyan

# 检查环境变量
$missingVars = @()
if (-not $env:GPG_PASSPHRASE) { $missingVars += "GPG_PASSPHRASE" }
if (-not $SignOnly) {
    if (-not $env:MAVEN_CENTRAL_USERNAME) { $missingVars += "MAVEN_CENTRAL_USERNAME" }
    if (-not $env:MAVEN_CENTRAL_PASSWORD) { $missingVars += "MAVEN_CENTRAL_PASSWORD" }
}

if ($missingVars.Count -gt 0 -and -not $DryRun) {
    Write-Host "⚠️  缺少环境变量:" -ForegroundColor Yellow
    $missingVars | ForEach-Object { Write-Host "   - $_" -ForegroundColor Yellow }
    Write-Host "`n提示: 你可以设置这些环境变量或使用 -DryRun 参数进行模拟测试`n" -ForegroundColor Yellow
    
    $continue = Read-Host "是否继续? (y/N)"
    if ($continue -ne "y") {
        exit
    }
}

# 显示配置
Write-Host "📋 当前配置:" -ForegroundColor Green
Write-Host "   Maven Central 用户名: $(if ($env:MAVEN_CENTRAL_USERNAME) { $env:MAVEN_CENTRAL_USERNAME } else { '(未设置)' })" -ForegroundColor Gray
Write-Host "   GPG 密钥: $(if ($env:GPG_PASSPHRASE) { '✓ 已设置' } else { '✗ 未设置' })" -ForegroundColor Gray
Write-Host "   模式: $(if ($DryRun) { 'Dry Run (模拟)' } elseif ($SignOnly) { '仅签名' } else { '完整测试' })" -ForegroundColor Gray
if ($Version) {
    Write-Host "   版本: $Version" -ForegroundColor Gray
}
Write-Host ""

# 步骤 1: 清理
Write-Host "🧹 步骤 1/5: 清理旧构建..." -ForegroundColor Cyan
./gradlew clean
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 清理失败" -ForegroundColor Red
    exit 1
}
Write-Host "✅ 清理完成`n" -ForegroundColor Green

# 步骤 2: 构建
Write-Host "🔨 步骤 2/5: 构建项目..." -ForegroundColor Cyan
./gradlew build
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 构建失败" -ForegroundColor Red
    exit 1
}
Write-Host "✅ 构建完成`n" -ForegroundColor Green

# 步骤 3: 发布到本地
Write-Host "📦 步骤 3/5: 发布到 MavenLocal..." -ForegroundColor Cyan
./gradlew publishToMavenLocal
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 发布到 MavenLocal 失败" -ForegroundColor Red
    exit 1
}
Write-Host "✅ 发布到 MavenLocal 完成" -ForegroundColor Green

# 显示发布的文件
$mavenLocal = "$env:USERPROFILE\.m2\repository"
Write-Host "   📁 文件位置: $mavenLocal" -ForegroundColor Gray
Write-Host ""

# 步骤 4: 测试签名
if ($env:GPG_PASSPHRASE) {
    Write-Host "✍️  步骤 4/5: 测试 GPG 签名..." -ForegroundColor Cyan
    ./gradlew signReleasePublication
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ GPG 签名失败" -ForegroundColor Red
        Write-Host "   提示: 检查 GPG_PASSPHRASE 是否正确" -ForegroundColor Yellow
        exit 1
    }
    Write-Host "✅ GPG 签名成功`n" -ForegroundColor Green
} else {
    Write-Host "⏭️  步骤 4/5: 跳过签名 (未设置 GPG_PASSPHRASE)`n" -ForegroundColor Yellow
}

# 步骤 5: 列出所有发布任务
Write-Host "📋 步骤 5/5: 可用的发布任务..." -ForegroundColor Cyan
./gradlew tasks --group publishing
Write-Host ""

# 显示下一步
Write-Host "✨ 测试完成!" -ForegroundColor Green
Write-Host "`n📝 下一步:" -ForegroundColor Cyan
Write-Host "   1. 检查 MavenLocal 中的文件: $mavenLocal" -ForegroundColor White
Write-Host "   2. 如果一切正常，设置 GitHub Secrets" -ForegroundColor White
Write-Host "   3. 推送 tag 触发自动发布: git tag v1.0.0 && git push origin v1.0.0" -ForegroundColor White
Write-Host ""

# 如果是 dry-run，显示实际命令
if ($DryRun) {
    Write-Host "🔍 实际会执行的命令:" -ForegroundColor Yellow
    Write-Host "   ./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository" -ForegroundColor Gray
    Write-Host ""
}

# 显示使用 act 进行本地 GitHub Actions 测试的提示
Write-Host "💡 提示: 想在本地测试 GitHub Actions?" -ForegroundColor Cyan
Write-Host "   安装 act: choco install act-cli" -ForegroundColor Gray
Write-Host "   运行: act workflow_dispatch -W .github\workflows\publish-maven-central.yml" -ForegroundColor Gray
Write-Host ""
