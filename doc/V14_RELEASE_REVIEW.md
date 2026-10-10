# v14.0.0 发布核对记录 / Release audit

> 文档修订：2026-10-10。修订原因：发布后 README 仍引用已修复的 ARMv7 HTTPS 阻断结论，且更新说明遗漏了 v13.2.1 之后的折叠屏、GL、R8 和接入文档提交。本记录固定发布范围、源码与产物，避免中英文入口再次与发布事实分离。

发布范围为 `v13.2.1..v14.0.0`，共 **27 条提交，含 2 条合并提交**；发布提交为 `4a7abf9caecbf7def7e2279e7fe2e246ef8e583a`。本次文档修订在 master 提交，已发布 tag 的代码与二进制身份保持不变。

- [中文完整更新说明](UPDATE_VERSION.md#v1400-2026-10-10)
- [English changelog](UPDATE_VERSION_EN.md#v1400-2026-10-10)
- [全部提交比较 / Full comparison](https://github.com/CarGuo/GSYVideoPlayer/compare/v13.2.1...v14.0.0)
- [GitHub Release 与 APK](https://github.com/CarGuo/GSYVideoPlayer/releases/tag/v14.0.0)

## 全部提交对应关系 / Every commit

下面逐条覆盖提交内容；合并提交中的版本过渡与已移除实现不作为最终发布特性重复宣传。

| 提交 / Commit | 原始标题 / Subject | 最终文档覆盖 / Final documentation coverage |
| --- | --- | --- |
| [8da01e8e](https://github.com/CarGuo/GSYVideoPlayer/commit/8da01e8e30c342a2819847685fa6588c303f5a28) | docs: list optional cast dependency in direct setup | 直接依赖示例补充可选 cast；README / DEPENDENCIES（Direct setup: optional cast） |
| [58f8133f](https://github.com/CarGuo/GSYVideoPlayer/commit/58f8133f83c9f1b5e1361427fc8a734db72c7d3c) | build(r8): minimize keep rules to reflection surface only | R8 反射/JNI 保留规则精简；FAQ / R8 报告（Reflection-safe R8 rules） |
| [69b9fe80](https://github.com/CarGuo/GSYVideoPlayer/commit/69b9fe80d83ac1d37858ce1ddbd4ad6c1157321f) | docs(skills): add GSYVideoPlayer skill pack (13 skills) and R8 analyzer report | 13 个 skills 与 R8 分析报告（Integration skill pack and analyzer report） |
| [24b475bb](https://github.com/CarGuo/GSYVideoPlayer/commit/24b475bb9a6caf1827a82a4d784e0236e1d47072) | feat(fold): add XML and Compose foldable demos (BOOK/TABLETOP split) | XML / Compose 折叠屏 Demo、WindowManager 1.3.0（Foldable demos） |
| [0352c825](https://github.com/CarGuo/GSYVideoPlayer/commit/0352c825653b409df252705403cdc52edaf8d5b3) | feat(fold): posture-aware fullscreen split for XML demo | XML 全屏 BOOK/TABLETOP 克隆布局（Posture-aware fullscreen） |
| [790aa7a8](https://github.com/CarGuo/GSYVideoPlayer/commit/790aa7a869fc22b9aca4156c295c70c4827ae8db) | fix(fold): refresh layout on configuration change, verified across postures | 配置变化后重新应用折叠姿态（Rotation/unfold layout refresh） |
| [8bacfe67](https://github.com/CarGuo/GSYVideoPlayer/commit/8bacfe679b85da3806b07c68fb4a203e38e4f901) | fix(fold): enable fullscreen entry on click for XML and Compose demos | 折叠屏全屏入口与返回退出（Fullscreen click/Back handling） |
| [94dfdc76](https://github.com/CarGuo/GSYVideoPlayer/commit/94dfdc7681fadc106cf5d47f94e2f4038c62c7f0) | feat(fold): injectable posture for full state-matrix verification | extra_posture 姿态矩阵测试入口（Opt-in posture injection） |
| [2dd19ea4](https://github.com/CarGuo/GSYVideoPlayer/commit/2dd19ea4a23aa6f1915b0cb73898dff35729a9f1) | chore: ignore and untrack app/test_evidence local test scratch | 忽略并取消跟踪本地 test_evidence（Local-only scratch evidence） |
| [647b33d2](https://github.com/CarGuo/GSYVideoPlayer/commit/647b33d28d5e18281df9e1af7cfb15c4d42a8241) | build: upgrade Gradle wrapper to 8.12 and pin R8 to 9.4.14 | Gradle 8.12、R8 9.4.14 固定配置（Build toolchain upgrade） |
| [ab64c751](https://github.com/CarGuo/GSYVideoPlayer/commit/ab64c7518378dbb65096d2ef7db28c95e7825edb) | feat(gl): FBO 多 pass 管线、降采样金字塔与 Bloom 辉光 | FBO 多 pass、金字塔、高斯、Bloom、旧 shader 修正（Multi-pass GL pipeline） |
| [7d821e1d](https://github.com/CarGuo/GSYVideoPlayer/commit/7d821e1dd3477ded5b5ccfbe2935787db1323db5) | feat(gl): LUT 电影级调色 LookupEffect 与纹理资产管线 | LookupEffect、纹理生命周期与三套 LUT（LUT grading and texture lifecycle） |
| [94e288b1](https://github.com/CarGuo/GSYVideoPlayer/commit/94e288b198f3e316cc512d0f4a91e14e4aaca3a8) | feat(gl): 美颜保边磨皮 BeautyEffect 与暖色美白 | BeautyEffect 保边磨皮与暖色美白（Beauty effect） |
| [2afd95b7](https://github.com/CarGuo/GSYVideoPlayer/commit/2afd95b7e29396d374fd91e02a96fa46facb5a52) | feat(gl): Glitch 故障风 / CRT 扫描线与 uTime 动画支持 | uTime、GlitchEffect、CrtEffect（Animated glitch/CRT effects） |
| [1c6bace8](https://github.com/CarGuo/GSYVideoPlayer/commit/1c6bace892e5fd16a2dc4ec131a6346e6ea5db27) | feat(gl): 老电视模拟信号干扰 OldTvSignalEffect | OldTvSignalEffect（Analog-TV interference） |
| [e38bec8a](https://github.com/CarGuo/GSYVideoPlayer/commit/e38bec8a0eb841994e8238776eed544d60b32040) | update | Compose #4259 与首次 FFmpeg n5.1.10 ex_so 集成（Host lifecycle and native migration） |
| [ece13ca8](https://github.com/CarGuo/GSYVideoPlayer/commit/ece13ca87fd1682f4aa3ba97230bf832f23190b6) | Add tested FFmpeg 5.1.10 native libraries and migration documentation | 新 native 库、MJPEG demuxer/parser 与迁移记录；早期产物记录后续被替代（MJPEG/build profile update） |
| [33070dab](https://github.com/CarGuo/GSYVideoPlayer/commit/33070dab1ddebb4c516a40a2cc58af78e49dd252) | Integrate FFmpeg 5 R3 playback libraries and Codec2 fallback | 平台倍速、EOF、Codec2 与 AGP 8.9.1；当时的 SNAPSHOT/发布保护后来移除，不作为最终特性（Platform rate, Codec2 and AGP upgrade） |
| [9eae0cd2](https://github.com/CarGuo/GSYVideoPlayer/commit/9eae0cd2659372af687ddfe490418c5b40416d50) | Integrate RTSP session isolation and matched native libraries | 会话回调/超时隔离、RTSP 配套库与测试（Session isolation and RTSP integration） |
| [0fa42143](https://github.com/CarGuo/GSYVideoPlayer/commit/0fa4214312036505cb0083ad356e748a41fc3774) | fix(compose): 同步 ExoPlayer 轮询缓冲进度到 bufferPercent 与 mBufferPoint (fix #4261) | Compose/View 缓冲同步、缓存 Demo 与 Exo 缓冲展示（Buffer progress and cache demo） |
| [0e3b39f5](https://github.com/CarGuo/GSYVideoPlayer/commit/0e3b39f53028d601e8f88d7de23aee6ea032c97e) | chore(release): prepare v13.3.0 with FFmpeg 5.1.10 across ex_so and armv64/armv7a/x86_64 | 合并 master 缓冲修复，统一独立三 ABI；移除过渡文档/发布保护（Merge and release preparation） |
| [ba4d3fe2](https://github.com/CarGuo/GSYVideoPlayer/commit/ba4d3fe26b47715eaab88a8375f1674a110ae567) | chore(release): bump version to 14.0.0 | 13.3.0 预备版本改为正式 14.0.0，并同步坐标（Final version metadata） |
| [0fd5cc0f](https://github.com/CarGuo/GSYVideoPlayer/commit/0fd5cc0f2f9a1a048274bcc4d404d47d842eba69) | fix(ijk): ship matched audio and RTSP native fixes for FFmpeg 5 | a599f602 音频、起播、Error 状态与 RTSP 修复的成套 native 库（Audio/RTSP tuple replacement） |
| [835ae3c0](https://github.com/CarGuo/GSYVideoPlayer/commit/835ae3c095435dc3ab64b1a74372c83a01bc3ac7) | test(release): record v14 ARMv7 HTTPS blocker and native TLS gate | 原 ARMv7 TLS 问题记录、独立 CA gate、CI 与默认 ABI 打包修正（Regression gate and ABI packaging） |
| [f29be693](https://github.com/CarGuo/GSYVideoPlayer/commit/f29be6935715c8757fbfa37566753ebf9cd76b11) | feat(compose): 暴露 changeTextureViewShowType 支持动态切换显示比例 (#4261) | 动态显示比例 public API 与 Compose 完整控件 Demo（Dynamic aspect ratio API） |
| [f5f573ef](https://github.com/CarGuo/GSYVideoPlayer/commit/f5f573ef082c6c3f161fa2ba953b4b5a519403b3) | fix(native): publish TLS and HLS inheritance repair candidate | ARMv7 编译修复 + HLS TLS 策略继承，三 ABI 全量重编（Final TLS/HLS repaired tuples） |
| [4a7abf9c](https://github.com/CarGuo/GSYVideoPlayer/commit/4a7abf9caecbf7def7e2279e7fe2e246ef8e583a) | Merge ffmpeg-5.0 for v14.0.0 release | 合并到 master，保留最新 Compose API 与最终发布验证（Release merge and verification） |

## 最终 native 身份 / Final native identity

`ffmpeg-5.0` 是迁移分支名，实际基线为 **FFmpeg n5.1.10**，TLS 依赖为 **OpenSSL 3.5.9**。使用以下固定源码，不使用早期 platformrate-r3 或 RTSP 候选的构建标签替代：

| 内容 | 固定源码 |
| --- | --- |
| IJK 构建集成（包含音频/RTSP 与 TLS 编译修复） | [df3f5ca6ed56419e7af04de0fd3a7474bb41950b](https://github.com/CarGuo/ijkplayer/commit/df3f5ca6ed56419e7af04de0fd3a7474bb41950b) |
| FFmpeg 七补丁集成（包含 HLS TLS 继承） | [0c8735b51d29dbc74e18c235246d5230bd5db989](https://github.com/CarGuo/FFmpeg/commit/0c8735b51d29dbc74e18c235246d5230bd5db989) |
| OpenSSL | `45e844fa2a14ec92d146bd8f5778ac130b6625fb`（3.5.9，上游源码未修改） |

`ex_so` 与对应独立 ABI 模块包含下列相同的九库。每个 ABI 的 FFmpeg/player/SDL 必须成套使用。

| ABI | 库 | SHA-256 |
| --- | --- | --- |
| `arm64-v8a` | `libijkffmpeg.so` | `284b474b156a6ae4eaa759793709c9e23a3b95255490116e1c731bb5f7cddab0` |
| `arm64-v8a` | `libijkplayer.so` | `ba0976a83c21c91d93ee4b5a052b21019dae6e5fa50b1b8dd40abc34d5135d91` |
| `arm64-v8a` | `libijksdl.so` | `5d0247ef6c8a6baa16e2722043bd365301c2dc045c8557d9620747fbbc53c195` |
| `armeabi-v7a` | `libijkffmpeg.so` | `a5db96183b8c9b085617ee59310f6a7ac3ded5e8e7c75c9bfc81acadce54fee5` |
| `armeabi-v7a` | `libijkplayer.so` | `413c5fb6179501d445ae71701f3c9bdcbd6fb0e26111b5cc10643826e6b48bd7` |
| `armeabi-v7a` | `libijksdl.so` | `8bf24de7fa3d292a7906c71934c2a1e4d9e8b87108eda1293a6cb52e72fcb19a` |
| `x86_64` | `libijkffmpeg.so` | `0e257e945a66d07096709b5ed40aee6386f0c58857a23ac6235937440748a9e6` |
| `x86_64` | `libijkplayer.so` | `14cd5b18ad588c08fe4c4b7fe18e45970d0b19d6e5fb0087b1269e7fc73d2a26` |
| `x86_64` | `libijksdl.so` | `bb0f809973573989d710f03821819f1204955c62202703a81965c54cce1dea53` |

重编入口见 [BUILD_SO.md](BUILD_SO.md) / [BUILD_SO_EN.md](BUILD_SO_EN.md)，ABI 与依赖选型见 [DEPENDENCIES.md](DEPENDENCIES.md) / [DEPENDENCIES_EN.md](DEPENDENCIES_EN.md)。旧 `armeabi` / `x86` 不属于本次 FFmpeg 5 三 ABI 库组。

## 当前结果 / Current release results

2026-10-10 在原问题设备 Redmi M2104K10AC / Android 13 / API 33 / 4 KiB 页上，确认实际 32 位和 64 位进程后，最终库组通过以下验证。ARMv7 证书加载崩溃已经修复；历史旧库崩溃对照不属于当前发布库的失败结果。

| 验证 | 结果 |
| --- | --- |
| 独立 native CA 加载 | ARMv7 / ARM64 均退出 0 |
| GSY/JNI 播放检查 | ARMv7 15/15；ARM64 15/15 |
| Java 会话代次 / 回调 / 超时回归 | 48/48 |
| Java / Cast / Compose 单测 | 18 个独立用例，Debug/Release 共 36 次执行通过 |
| Debug/Release 构建与 R8 | 通过 |
| OpenSSL 编译选项与缓存守卫 | 6/6 |
| 三 ABI ELF、身份和符号检查 | 85/85 |
| AAR / APK 打包 | 三 ABI，九库与独立模块一致；APK 对齐、签名通过 |
| 双渠道本地发布与 cast 隔离 | 15 模块；30 POM、30 Gradle metadata 坐标检查通过 |
| [master CI](https://github.com/CarGuo/GSYVideoPlayer/actions/runs/38022428300) | 发布提交通过 |
| [Release / GitHub Packages](https://github.com/CarGuo/GSYVideoPlayer/actions/runs/38022776721) | tag 流水线通过 |
| [Maven Central](https://github.com/CarGuo/GSYVideoPlayer/actions/runs/38022776719) | 上传、close/release 流水线通过；2026-10-10 05:08 UTC 核验全部 15 个模块的公开 POM / AAR 均返回 HTTP 200，POM 坐标为 `io.github.carguo:*:14.0.0` |

发布 APK SHA-256：`5656be6b3a2a3202a981a1186051ee60e5bfe17e86997a081dee2c93a831b15b`。

播放检查覆盖本地 A/V prepare、seek 和完成，软件 3× 进度/墙钟比例，50 ms / 200 ms 音频完成，96 kHz 重采样，HTTP/HLS，Exo 回调，TCP RTSP 与初始 OPTIONS/DESCRIBE/SETUP/PLAY 重定向，以及默认/显式校验 HTTPS、可信 HLS 和嵌套错误 CA/SAN 拒绝。每个嵌套负例先通过可信正例；自有 TLS 1.2 服务端确认拒绝端点没有成功握手或 HTTP 请求。详细方法和旧对照见 [TLS README](../tests/tls-native/README.md)，会话回归见 [RTSP README](../tests/rtsp-session/README.md)。

GL 与折叠屏提交包含当时的可视播放和姿态矩阵记录；`extra_posture` 注入证明布局/全屏路径，不替代实体折叠屏传感器与真实铰链的运行验收。历史 R8 评分与 APK 体积只对当时的对照构建成立。

## 本次文档核验 / Documentation checks

2026-10-10 的文档修订核对了上述全部 27 条提交、69 份 Markdown 的仓库链接与章节锚点，以及主要接入文档中的 200 处依赖坐标。README、更新说明、功能索引、构建/发布指南、Compose 与 GL 接入说明、FAQ、skills 和 native 测试说明已同步到 v14.0.0；历史失败记录保留原结果并标明版本范围。

本次仅修改 Markdown；检查代码块闭合与 `git diff --check`，并确认没有代码、构建配置或二进制变更。上表运行与构建结果来自发布提交的验证，本次文档修订没有重新执行设备测试。

## 仍未覆盖 / Remaining qualification

- 当前 x86_64 的设备/模拟器运行与 16 KiB 页设备运行。
- 新库的硬解画面、完整声学输出、长期播放、所有外部网络与所有机型。
- 当前 TLS/HLS 用例没有穷尽 TLS 1.3、所有 key/init/live/redirect 路径。
- 较早 ARM64 库的 0.25×–4×、A/V 2× 和音频内容检查属于历史证据，未在本次新库上完整重跑。

当前设备检查使用静音输出且不设置视频 Surface，验证的是状态、元数据、时间线和网络拒绝行为。不得将其扩写成画面、硬解、主观音质或所有设备都已验证。
