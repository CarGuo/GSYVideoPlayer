# 建议添加到项目 README 的依赖说明

## 📦 依赖配置 (两种方式)

### 方式 1: Maven Central (推荐 - 公开访问)

```gradle
dependencies {
    // 核心库
    implementation 'io.github.carguo:gsyvideoplayer-java:11.3.0'
    
    // 基础库
    implementation 'io.github.carguo:gsyvideoplayer-base:11.3.0'
    
    // 其他变体
    implementation 'io.github.carguo:gsyvideoplayer-armv7a:11.3.0'
    implementation 'io.github.carguo:gsyvideoplayer-arm64:11.3.0'
    implementation 'io.github.carguo:gsyvideoplayer-x86:11.3.0'
    // ...
}
```

**优点**: 
- ✅ 不需要配置 GitHub token
- ✅ 公开访问
- ✅ 与其他 Maven Central 依赖一致

---

### 方式 2: GitHub Packages (现有方式)

```gradle
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/CarGuo/GSYVideoPlayer")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.token") ?: System.getenv("TOKEN")
        }
    }
}

dependencies {
    implementation 'com.shuyu:gsyvideoplayer-java:11.3.0'
}
```

**注意**: 
- ⚠️ 需要 GitHub token
- ⚠️ 需要额外配置 credentials

---

### 两种方式的区别

| | Maven Central | GitHub Packages |
|---|--------------|-----------------|
| GROUP ID | `io.github.carguo` | `com.shuyu` |
| 访问方式 | 公开 | 需要 token |
| 配置复杂度 | 简单 | 中等 |
| 推荐场景 | 新项目、公开项目 | 私有项目、已有配置 |

---

### 迁移说明

如果你当前使用 `com.shuyu`，可以继续使用，无需修改。

如果想切换到 Maven Central:
1. 移除 GitHub Packages 的 repository 配置
2. 将 `com.shuyu` 替换为 `io.github.carguo`
3. 完成！不需要 token 配置
