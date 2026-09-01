---
name: gsy-proguard-r8
description: 在启用了 R8 优化 / Full Mode / Configuration Analyzer 的项目里，为 GSYVideoPlayer 配置正确、最小面积的保留规则。
when_to_use: minifyEnabled true 之后运行崩溃；集成 AGP 8.6+/Media3 需要迁移规则；用 R8 Configuration Analyzer 做 keep 规则精简。
inputs:
  - name: kernel
    description: 使用的内核（ijk/exo/aliyun）
    required: true
outputs:
  - proguard-rules.pro 覆盖三处反射热点 + JNI 层 + 序列化，release 构建不崩。
references:
  - file: app/proguard-rules.pro
  - file: doc/R8_ANALYZER_REPORT.md
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/player/PlayerFactory.java
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cache/CacheFactory.java
  - file: gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/video/base/GSYBaseVideoPlayer.java
---

# gsy-proguard-r8

## 三个必须的反射热点

分别在源代码里：
1. **PlayerFactory** [#L11-L13](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/player/PlayerFactory.java#L11-L13) —— `Class.newInstance()` 反射构造 `IPlayerManager` 实现。
2. **CacheFactory** —— 同上，构造 `ICacheManager` 实现（见 [CacheFactory](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/cache/CacheFactory.java)）。
3. **GSYBaseVideoPlayer.startWindowFullscreen** —— 通过 `getConstructor(Context, Boolean)` 复刻自身，实现"全屏时另起一份 player"，见 [GSYBaseVideoPlayer](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/video/base/GSYBaseVideoPlayer.java)。

## 推荐 `proguard-rules.pro`（IJK 内核，含 Media3 抑警）

```proguard
# ---- ijkplayer JNI ----
-keep class tv.danmaku.ijk.media.player.** { *; }
-dontwarn tv.danmaku.ijk.media.player.**

# ---- GSYVideoPlayer 反射热点 ----
-keep class * extends com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer {
    public <init>(android.content.Context);
    public <init>(android.content.Context, java.lang.Boolean);
}
-keep class * implements com.shuyu.gsyvideoplayer.player.IPlayerManager {
    public <init>();
}
-keep interface com.shuyu.gsyvideoplayer.player.IPlayerManager { *; }
-keep class * implements com.shuyu.gsyvideoplayer.cache.ICacheManager {
    public <init>();
}
-keep interface com.shuyu.gsyvideoplayer.cache.ICacheManager { *; }

# ---- Media3 aar 已自带 consumer rules，仅抑警 ----
-dontwarn androidx.media3.**
-dontwarn com.google.android.exoplayer2.**

# ---- 常规序列化 ----
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
```

完整版参照仓库当前的 [proguard-rules.pro](file:///D:/workspace/project/GSYVideoPlayer/app/proguard-rules.pro)。

## Aliyun 内核追加

```proguard
-keep class com.aliyun.player.** { *; }
-keep class com.cicada.player.** { *; }
-dontwarn com.aliyun.player.**
```

## R8 Configuration Analyzer 使用

AGP 8.6.x 未内置独立任务，但可以：

1. 在 `settings.gradle` 里覆盖 R8 版本到 9.4.14+：
   ```groovy
   buildscript {
     dependencies { classpath 'com.android.tools:r8:9.4.14' }
   }
   ```
2. 用 system property 触发报告输出（AGP 9.3+ 或 R8 9.4+）：
   ```
   -Dcom.android.tools.r8.dumpkeepradiushtmltodirectory=D:/absolute/path/report
   ```
3. `./gradlew :app:assembleRelease` 后打开 report/index.html 查看 Blast Radius。
4. 用仓库脚本 [build/r8-analyzer/score.mjs](file:///D:/workspace/project/GSYVideoPlayer/build/r8-analyzer/score.mjs) 提取 Shrinking / Optimization / Obfuscation 三档分数并做前后对比。

## 分数对比示例

见 [doc/R8_ANALYZER_REPORT.md](file:///D:/workspace/project/GSYVideoPlayer/doc/R8_ANALYZER_REPORT.md)：本项目将 `-keep class com.shuyu.gsyvideoplayer.** { *; }` 全量 keep 收敛成 3 条精准 keep 后，Shrinking / Optimization / Obfuscation 三档分数、live class/method 数、APK 尺寸对比。

## 校验清单（发版前）

| 步骤 | 命令/操作 |
|---|---|
| release 构建 | `./gradlew :app:assembleRelease` |
| 直接安装真机 | `adb install -r app/build/outputs/apk/release/*.apk` |
| 手动跑主流程 | 详情播放 → 全屏 → 手势 → seek → 缓存命中 → 列表 → 广告 → 投屏（若集成） |
| logcat | `adb logcat -s System.err AndroidRuntime GSYVideoPlayer` 无 `ClassNotFoundException` / `NoSuchMethodException` |

## 常见崩溃 -> 规则映射

| 崩溃症状 | 原因 | 增补规则 |
|---|---|---|
| `NoSuchMethodException: xxxVideoPlayer.<init>(Context, Boolean)` | 未保留自定义 View 的 Boolean 构造 | `-keep class * extends GSYBaseVideoPlayer` 那条 |
| `InstantiationException` on switching kernel | Manager 实现类被去除或改名 | `-keep class * implements IPlayerManager` |
| `UnsatisfiedLinkError` `libijkplayer.so` | ijkplayer JNI 回调找不到 Java 类 | `-keep class tv.danmaku.ijk.media.player.** { *; }` |
| `ClassNotFoundException: androidx.media3...*` | Exo/Media3 未抑警且 aar consumer 未生效 | `-dontwarn androidx.media3.**` + 检查 aar 是否 include |
| List 页 seek 后 seek 失效 | 序列化模型被混淆 | 保留 `Serializable` / `Parcelable` |

## 常见坑

- 用 `-allowaccessmodification` + `Full Mode` 时，父子类构造器可见性会被改写；`-keep <init>` 一定要写。
- `consumer-rules.pro` 只在 aar 打包时打入，本地源码模块不生效；如果 SDK 是源码依赖，需要把 keep 规则同时放到 app `proguard-rules.pro`。
- Media3 aar 会覆盖旧的 `com.google.android.exoplayer2.**`，两者规则并存无害，但只用其一时可只保留对应 `-dontwarn`。
