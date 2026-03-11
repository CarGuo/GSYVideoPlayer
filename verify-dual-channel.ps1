# 验证双渠道发布配置
# 这个脚本检查 GitHub Packages 和 Maven Central 配置是否正确

Write-Host "🔍 双渠道发布配置验证工具" -ForegroundColor Cyan
Write-Host "================================`n" -ForegroundColor Cyan

# 检查 gradle.properties
Write-Host "📋 步骤 1/4: 检查 gradle.properties..." -ForegroundColor Cyan

$gradleProps = Get-Content gradle.properties -Raw

if ($gradleProps -match 'PROJ_GROUP=com\.shuyu') {
    Write-Host "   ✅ GitHub Packages GROUP ID: com.shuyu" -ForegroundColor Green
} else {
    Write-Host "   ❌ 未找到 PROJ_GROUP=com.shuyu" -ForegroundColor Red
}

if ($gradleProps -match 'PROJ_GROUP_MAVEN_CENTRAL=io\.github\.carguo') {
    Write-Host "   ✅ Maven Central GROUP ID: io.github.carguo" -ForegroundColor Green
} else {
    Write-Host "   ❌ 未找到 PROJ_GROUP_MAVEN_CENTRAL=io.github.carguo" -ForegroundColor Red
}

Write-Host ""

# 检查 gradle 配置文件
Write-Host "📋 步骤 2/4: 检查 gradle 配置文件..." -ForegroundColor Cyan

if (Test-Path "gradle\publish.gradle") {
    Write-Host "   ✅ gradle\publish.gradle 存在 (GitHub Packages)" -ForegroundColor Green
} else {
    Write-Host "   ❌ gradle\publish.gradle 不存在" -ForegroundColor Red
}

if (Test-Path "gradle\maven-central-publish.gradle") {
    Write-Host "   ✅ gradle\maven-central-publish.gradle 存在 (Maven Central)" -ForegroundColor Green
} else {
    Write-Host "   ❌ gradle\maven-central-publish.gradle 不存在" -ForegroundColor Red
}

Write-Host ""

# 检查 workflow 文件
Write-Host "📋 步骤 3/4: 检查 GitHub Actions workflows..." -ForegroundColor Cyan

if (Test-Path ".github\workflows\release.yml") {
    Write-Host "   ✅ release.yml 存在 (GitHub Packages)" -ForegroundColor Green
} else {
    Write-Host "   ❌ release.yml 不存在" -ForegroundColor Red
}

if (Test-Path ".github\workflows\publish-maven-central.yml") {
    Write-Host "   ✅ publish-maven-central.yml 存在 (Maven Central)" -ForegroundColor Green
} else {
    Write-Host "   ❌ publish-maven-central.yml 不存在" -ForegroundColor Red
}

Write-Host ""

# 列出所有发布任务
Write-Host "📋 步骤 4/4: 列出可用的发布任务..." -ForegroundColor Cyan
Write-Host "   (这可能需要一些时间...)`n" -ForegroundColor Gray

./gradlew tasks --group publishing 2>&1 | Select-String -Pattern "publish" | ForEach-Object {
    $line = $_.Line.Trim()
    if ($line -match "publish.*Release.*") {
        Write-Host "   📦 GitHub Packages: $line" -ForegroundColor Cyan
    } elseif ($line -match "publish.*MavenCentral.*") {
        Write-Host "   🌐 Maven Central: $line" -ForegroundColor Green
    } elseif ($line -match "publishToSonatype|closeAndRelease") {
        Write-Host "   🌐 Maven Central: $line" -ForegroundColor Green
    } elseif ($line -match "^publish\s") {
        Write-Host "   📦 GitHub Packages: $line" -ForegroundColor Cyan
    } elseif ($line -notmatch "Publishing|---") {
        Write-Host "   🔹 $line" -ForegroundColor Gray
    }
}

Write-Host ""

# 总结
Write-Host "✨ 验证完成!" -ForegroundColor Green
Write-Host ""
Write-Host "📝 配置总结:" -ForegroundColor Cyan
Write-Host "   GitHub Packages" -ForegroundColor White
Write-Host "      GROUP ID: com.shuyu" -ForegroundColor Gray
Write-Host "      配置文件: gradle/publish.gradle" -ForegroundColor Gray
Write-Host "      Workflow: .github/workflows/release.yml" -ForegroundColor Gray
Write-Host "      发布命令: ./gradlew publish" -ForegroundColor Gray
Write-Host ""
Write-Host "   Maven Central" -ForegroundColor White
Write-Host "      GROUP ID: io.github.carguo" -ForegroundColor Gray
Write-Host "      配置文件: gradle/maven-central-publish.gradle" -ForegroundColor Gray
Write-Host "      Workflow: .github/workflows/publish-maven-central.yml" -ForegroundColor Gray
Write-Host "      发布命令: ./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository" -ForegroundColor Gray
Write-Host ""

Write-Host "🚀 下一步:" -ForegroundColor Cyan
Write-Host "   1. 在模块的 build.gradle 中同时引入两个配置文件" -ForegroundColor White
Write-Host "   2. 设置 GitHub Secrets (MAVEN_CENTRAL_USERNAME 等)" -ForegroundColor White
Write-Host "   3. 打 tag 触发自动发布: git tag v11.3.0 && git push origin v11.3.0" -ForegroundColor White
Write-Host ""

Write-Host "📚 文档:" -ForegroundColor Cyan
Write-Host "   - 快速指南: doc\QUICK_START_MAVEN_CENTRAL.md" -ForegroundColor White
Write-Host "   - 双渠道配置: doc\DUAL_CHANNEL_PUBLISH.md" -ForegroundColor White
Write-Host "   - 架构说明: doc\ARCHITECTURE.md" -ForegroundColor White
Write-Host ""
