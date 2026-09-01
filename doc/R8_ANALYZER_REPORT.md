# R8 Configuration Analyzer 前后对比

> 生成日期：2026-09-01
> R8 版本：`com.android.tools:r8:9.4.14`（通过 [settings.gradle](../settings.gradle) 的 `pluginManagement.buildscript` 注入，本地临时；AGP 保持 8.6.1）
> 构建目标：`:app:assembleRelease`（Ijk + Exo/Media3 + Aliyun 全套播放器）
> 对比对象：本次提交 `58f8133f build(r8): minimize keep rules to reflection surface only` **之前 (`8da01e8e`) 与之后 (HEAD)** 的 `app/proguard-rules.pro`

---

## 1. 三项官方评分

评分定义（引自 R8 Configuration Analyzer HTML 报告的前端源码）：

```
score(%) = 100 - (disallowCount / liveItems) * 100
liveItems = liveClasses + liveFields + liveMethods
disallowCount = 满足 DONT_SHRINK / DONT_OPTIMIZE / DONT_OBFUSCATE 约束的 kept item 数
```

评分色带：`>= 80 绿`、`60 – 79 黄`、`< 60 红`。

| 指标 | BEFORE (旧规则) | AFTER (新规则) | 变化 |
| :--- | :--- | :--- | :--- |
| **Shrinking Score** | 63.07 % 🟡 | **83.64 % 🟢** | **+20.58 %** |
| **Optimization Score** | 0.00 % 🔴 | 0.00 % 🔴 | 0.00 % |
| **Obfuscation Score** | 63.34 % 🟡 | **83.96 % 🟢** | **+20.62 %** |

Shrinking / Obfuscation 双双从"中段黄色"跨到"顶段绿色"，评分**都提升约 20 个百分点**。

Optimization 保持 0.00 % 的原因：`-optimizations !class/merging/*,!code/simplification/arithmetic,!field/*` 里的 `!class/merging/*` 依旧全局关闭了类合并，被 R8 报告识别为"全局 disallow optimization"。这是**主动保留**的策略，因为项目里 Ijk / Aliyun native 反射对类合并高度敏感；如果后续做逐步放开，可以先去掉 `!class/merging/*` 再评估。

---

## 2. R8 处理体量对比

| 项 | BEFORE | AFTER | 变化 |
| :--- | ---: | ---: | ---: |
| Live classes | 20,749 | **17,002** | −3,747 (−18.06 %) |
| Live fields | 72,915 | **60,239** | −12,676 (−17.38 %) |
| Live methods | 122,130 | **97,843** | −24,287 (−19.89 %) |
| Live 总量 | 215,794 | **175,084** | **−40,710 (−18.87 %)** |
| Keep rules 总数 | 364 | 353 | −11 |
| Global keep rules | 1 | 1 | 0 |

R8 需要保护的存活对象**整体减少 ~19 %**，直接体现在 dex 更小、验证更快、启动更快。

---

## 3. 构建产物体积

| 项 | BEFORE | AFTER | 变化 |
| :--- | ---: | ---: | ---: |
| `app-release.apk` 大小 | 73,576,903 B (70.17 MiB) | 72,364,429 B (69.01 MiB) | **−1,212,474 B (−1.16 MiB)** |

APK 里 4 个 ijk `.so`（libijkffmpeg / libijkplayer / libijksdl / libndkbitmap）合计约 65 MiB，**不受 R8 影响**。真正被 R8 撬动的 dex + res 层，估算体积从 ~5.2 MiB 降到 ~4.0 MiB，**下降约 23 %**。

---

## 4. 关键规则改动一览

| 类别 | BEFORE | AFTER | 依据 |
| :--- | :--- | :--- | :--- |
| 全包保留 | `-keep class com.shuyu.gsyvideoplayer.** { *; }`、`-keep class tv.danmaku.ijk.** { *; }`、`-keep class androidx.media3.** { *; }`、`-keep class com.google.android.exoplayer2.** { *; }` | 只保留反射入口 + JNI 包（`ijk.media.player`） | 冗余整包保留是 shrinking 分数最大的杀手 |
| 反射入口 | 未显式声明 | 三条精准 keep：`GSYBaseVideoPlayer` 子类构造 / `IPlayerManager` 与 `ICacheManager` 无参构造 | 见 [PlayerFactory.java](../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/player/PlayerFactory.java)、[CacheFactory.java](../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cache/CacheFactory.java)、[GSYBaseVideoPlayer.java](../gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/video/base/GSYBaseVideoPlayer.java) |
| 通配符 | `void *(**On*Event)` / `void *(**On*Listener)` | 移除 | 项目内未使用字符串反射注册回调 |
| ButterKnife | 保留 5 条相关 rule | 移除 | 报告 `unused` |
| Attribute | 重复的 `EnclosingMethod` / `-dontpreverify` / `-ignorewarnings` | 合并去重、加 `-renamesourcefileattribute` | R8 优化建议 |
| Optimization 开关 | 只有 `-optimizationpasses 5` | 追加 `-allowaccessmodification`、`-repackageclasses ''` | 需搭配 `proguard-android-optimize.txt` |
| Media3 / Exo | 整包保留 | 换成 `-dontwarn`，交给 aar 自带的 consumer-rules | Media3 官方 aar 已含正确规则 |

完整规则见提交 `58f8133f` 的 [app/proguard-rules.pro](../app/proguard-rules.pro)；实操指引已同步到 [doc/QUESTION.md](QUESTION.md#L36-L68) 与 [doc/QUESTION_EN.md](QUESTION_EN.md#L36-L70)。

---

## 5. 复现步骤

```powershell
# 一次性把 R8 9.4.14 注入到 pluginManagement（本地已注入，暂未提交）
# 见 settings.gradle 顶部的 pluginManagement 块

# 生成一次报告到指定目录（目录必须先存在）
mkdir build\r8-analyzer\after
.\gradlew.bat :app:clean :app:assembleRelease --no-daemon `
  "-Dcom.android.tools.r8.dumpkeepradiushtmltodirectory=D:\workspace\project\GSYVideoPlayer\build\r8-analyzer\after"

# 提取三项分数（脚本使用 protobufjs 复刻报告前端的公式）
cd build\r8-analyzer ; npm i protobufjs@7.2.4 --no-audit --no-fund
node score.mjs .
```

`score.mjs` 位于 [build/r8-analyzer/score.mjs](../build/r8-analyzer/score.mjs)，只从 HTML 内嵌的 `keepradius-proto` schema + `keepradius-data` base64 payload 反序列化 `KeepRadiusContainer`，遍历 `keptClassInfoTable / keptFieldInfoTable / keptMethodInfoTable` 并按 R8 官方公式计算三项 disallow 计数与分数。

---

## 6. 后续优化空间

- **Optimization 分数**：若能在 native 反射充分回归后**放开 `!class/merging/*`**，Optimization 有望从 0 → 60~80。建议先在 Ijk 全平台真机 / Aliyun / Exo 三个 Activity 做 A/B 测试。
- **`-repackageclasses`**：目前用空字符串把所有类塞入默认包，可尝试改为具体前缀（如 `o`）以进一步压缩类名字符串。
- **资源缩减**：release 未启用 `shrinkResources true`，可在真机回归通过后开启，通常再省 300 KB - 1 MB。
- **`-dontwarn`**：新规则遗留了 3 条来自 Jetty StatisticsServlet 的 `java.lang.management.*` 缺失警告，可以补 `-dontwarn org.eclipse.jetty.**` 使输出更干净。
