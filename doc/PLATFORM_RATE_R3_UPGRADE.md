# FFmpeg 5 / platform-rate R3 升级、构建与验证

本分支候选版本为 `13.3.0-ffmpeg5-platformrate-r3-codec2-java-r1-SNAPSHOT`，原生版本为 `v0.8.8-carguo-35-gd5c0b4eb-platformrate-r3`。源码分支与 Maven 制品、标签、Release 是不同的发布对象；当前 Maven 制品、标签和 Release 尚未发布。README 与依赖文档中的既有稳定坐标不包含本候选。

## 一起集成 Java 修复与九个原生库

使用本分支的 `gsyVideoPlayer-java` 和 `gsyVideoPlayer-ex_so` 模块，或使用同一候选生成的匹配 AAR/POM。`gsyVideoPlayer-ex_so/src/main/jniLibs/` 包含 `arm64-v8a`、`armeabi-v7a`、`x86_64`，每个 ABI 均有 `libijkffmpeg.so`、`libijkplayer.so`、`libijksdl.so`。请按[九库 SHA-256 清单](platformrate-r3.SHA256SUMS)和[源码/原生契约](gsy-platformrate-r3-candidate.json)核对完整集合；不要混用不同版本的 player/SDL。

本次六个 player/SDL 库包含累计 R3 改动；三个 FFmpeg 库与此前 exp3 的字节一致。Java 模块须同时包含 `com.shuyu.gsyvideoplayer.player.IjkPlayerManager` 和 `GSYIjkMediaCodecSelector` 的本分支实现。仅替换 SO 不会带入 Codec2 修复。`io.github.carguo:gsyijkjava:1.0.0` 保持不变。

Codec2 选择保持原 IJK 选择器优先。只有原选择器返回 null 且 API ≥29 时，才检查支持目标 MIME 的常规、非编码器、硬件加速且非软件 C2 解码器，并排除要求 secure/tunneled 的组件。应用初始化回调仍可替换选择器。

## 平台范围

R3 使用 API ≥23 的平台播放参数，扩大 AudioTrack 容量至最小值的八倍，并按实际确认的倍速调整 EOF 截止时间；原有播放头与结束保护仍保留。已验证的本地倍速为 0.25×、0.5×、1×、2×、3×。

当前 GSY 库的 minSdk 为23，Demo 为26。直接集成 IJK 原生库时，API22及以下的非1×播放不在此次修复的支持范围；不能把本次结果外推为所有旧平台的倍速保证。API29以下仍使用原解码器选择路径。

## 从源码构建

使用 JDK17、仓库固定的 Gradle8.12、Android SDK36，以及项目声明的插件/依赖版本。`local.properties` 或 Android SDK 环境变量应指向本机 SDK。依赖缓存完整时可加 `--offline`；首次构建须先正常解析项目声明的依赖。保持已提交的 SNAPSHOT 版本，不要覆盖 `PROJ_VERSION` 或启用 `releaseIntent`。

在仓库根目录构建主要集成模块与 Demo：

```sh
./gradlew --no-daemon --no-configuration-cache --no-parallel --max-workers=1 \
  verifyReleaseVersion verifyReleaseGuardTests \
  :gsyVideoPlayer:assembleRelease :gsyVideoPlayer-java:assembleRelease \
  :gsyVideoPlayer-ex_so:assembleRelease :app:assembleDebug
./gradlew --no-daemon --no-configuration-cache --no-parallel --max-workers=1 \
  :gsyVideoPlayer-java:testDebugUnitTest :gsyVideoPlayer-cast:testDebugUnitTest \
  verifyCastDependencyIsolation verifyReleaseGuardTests
python3 gradle/tests/release-guard-integration.py /path/to/gradle-8.12/bin/gradle
```

完整候选还构建 `gsyVideoPlayer-base`、`gsyVideoPlayer-proxy_cache`、`gsyVideoPlayer-exo_player2`、`gsyVideoPlayer-rtmp`、`gsyVideoPlayer-cast`、`gsyVideoPlayer-aliplay`、`gsyVideoPlayer-compose` 的 `assembleRelease`。这十个库模块均可用 `:<模块名>:generatePomFileForReleasePublication` 生成本地 POM。AAR 位于各模块 `build/outputs/aar/`，POM 位于 `build/publications/release/`，标准 Demo APK 位于 `app/build/outputs/apk/debug/`。

演示用签名不能作为应用的生产签名。项目集成应使用应用自己的签名配置；交付归档不包含私钥。Gradle 的远端制品发布、发布签名与 Release 任务继续由版本防护阻止，本地构建/POM生成不等于发布到 Maven。

本次验证使用串行构建和受限 JVM，并保留所有失败尝试。堆大小是验证环境的配置，不是项目在任意机器上的通用最低要求；请按可用内存设置 `org.gradle.jvmargs`。

## 实际通过的验证

完整源码构建与包检查已通过，具体结果见[构建报告](PLATFORM_RATE_R3_BUILD_RESULT.md)和[验证清单](platformrate-r3-validation.json)。

当前 R3 真机证据来自 Pixel8 / Android API35 / AArch64 进程，页大小4096字节：

- 五档本地倍速和 Mux HTTPS-HLS 的播放进度、结束等原始断言通过，倍速检查包含平台实际回读
- 原 Functional7 矩阵保留6/7的整套失败记录；Apple样例在进入TLS前DNS解析失败，作为不可用测试源排除。正常HTTPS-HLS验证使用已通过的Mux样例，未把Apple条目改写为通过
- 两个倍速/seek转换用例通过
- Tail7七个原有1×用例及其446条采样通过
- Widget3三个用例通过：实际GSY控件/管理器的软件与硬件播放生命周期，以及软件旋转传播；包含暂停/恢复、seek、自然结束、重播与释放等对应断言

这不是任意设备、任意媒体的完整认证。其他ABI目前只有编译/静态验证；完整Demo UI、可听音质/完整音画同步和广泛厂商行为未由上述套件覆盖。64位ELF的16KiB对齐和APK的16KiB ZIP对齐属于静态检查，不能等同于16KiB页设备实测。真机页大小为4096字节。

此前exp3的x86_64模拟器运行曾未通过纯音频结束计时断言，原因未确定。R3没有在该环境复测，因此本次工作不据此认定那条历史现象已解决，也不把它表述为已确认的R3产品缺陷。

## 来源与核对

FFmpeg 源码提交：[ff8749026210](https://github.com/CarGuo/FFmpeg/commit/ff87490262107e004f6a16765484e36e1973d705)。IJK 源码提交：[bb1d1b9fa070](https://github.com/CarGuo/ijkplayer/commit/bb1d1b9fa07019155c88fe69fea9db896a108d0f)。这些提交记录本次代码与文档的发布位置；原生库内嵌 R3 版本和实际编译输入哈希保留原值。

当前构建源码清单包含765个文件，SHA-256为 `325ebbb56ae553376a7f9e817dcc2de2e964931d8e357ec1ce1af5529b370790`。该清单描述编译时输入；本指南等后续文档更新单独记录，不改写历史编译输入或失败回执。[当前状态](PLATFORM_RATE_R3_STATUS.md)、[原生哈希](platformrate-r3.SHA256SUMS)和[原生契约](gsy-platformrate-r3-candidate.json)共同标明候选身份。契约中的 `source_freeze_runtime_review_pending` 仅记录冻结时状态，当前结果以本指南及最终验证清单为准。
