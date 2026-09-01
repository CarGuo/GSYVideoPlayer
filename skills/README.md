# GSYVideoPlayer Skills

> Anthropic Skill 规范的技能包（每个子目录一个 `SKILL.md`，带 YAML frontmatter，可被 Claude Code / Trae SOLO / OpenCode / 其他兼容 SDK 加载）。
> **范围**：仅覆盖 [GSYVideoPlayer](file:///D:/workspace/project/GSYVideoPlayer) 仓库对外能力，不涉及具体业务代码。
> **风格**：中文叙述 + 英文原样保留 API / 类名 / 参数名 / 包名。每个技能只讲 **API 签名 + 参数表 + 关键调用序 + 源码/Demo 引用**，不放完整 Activity 代码。

## 加载方式

- **Claude Code**：把整个 [skills/](file:///D:/workspace/project/GSYVideoPlayer/skills) 目录复制或软链到项目 `.claude/skills/`，或直接把仓库根加入工作区，Skill runner 会自动识别 frontmatter。
- **Trae SOLO / 其他兼容 Agent**：读取 `SKILL.md` frontmatter 的 `name` / `description` / `when_to_use`，运行时按 `when_to_use` 匹配用户 query。
- **人类**：作为快速参考手册直接阅读。

## 技能清单（13 个）

| # | Skill | 场景一句话 | 目录 |
|---|---|---|---|
| 01 | `gsy-quickstart` | 从零把播放器塞进一个 Activity/Fragment，跑通播放/暂停/释放 | [skills/01-gsy-quickstart](file:///D:/workspace/project/GSYVideoPlayer/skills/01-gsy-quickstart) |
| 02 | `gsy-option-builder` | 用 `GSYVideoOptionBuilder` 一次性配置播放器（速率/循环/缓存/触摸/封面…） | [skills/02-gsy-option-builder](file:///D:/workspace/project/GSYVideoPlayer/skills/02-gsy-option-builder) |
| 03 | `gsy-fullscreen-orientation` | 全屏 / 反向横屏 / 竖屏视频自适应、`OrientationUtils` 联动 | [skills/03-gsy-fullscreen-orientation](file:///D:/workspace/project/GSYVideoPlayer/skills/03-gsy-fullscreen-orientation) |
| 04 | `gsy-kernel-switch` | Ijk / System / ExoPlayer2(Media3) / Aliyun 四内核切换与差异 | [skills/04-gsy-kernel-switch](file:///D:/workspace/project/GSYVideoPlayer/skills/04-gsy-kernel-switch) |
| 05 | `gsy-list-playback` | RecyclerView / ViewPager2 列表滑动自动播放、复用与漂移全屏 | [skills/05-gsy-list-playback](file:///D:/workspace/project/GSYVideoPlayer/skills/05-gsy-list-playback) |
| 06 | `gsy-cache-and-download` | `ProxyCacheManager` 边播边缓、自定义缓存目录、离线下载 | [skills/06-gsy-cache-and-download](file:///D:/workspace/project/GSYVideoPlayer/skills/06-gsy-cache-and-download) |
| 07 | `gsy-render-and-effects` | `TEXTURE`/`SURFACE`/`GLSURFACE` 渲染、显示比例、GL 滤镜 | [skills/07-gsy-render-and-effects](file:///D:/workspace/project/GSYVideoPlayer/skills/07-gsy-render-and-effects) |
| 08 | `gsy-subtitle-and-preview` | 外挂/内嵌字幕、VTT 缩略图轨、Seek 悬浮预览帧 | [skills/08-gsy-subtitle-and-preview](file:///D:/workspace/project/GSYVideoPlayer/skills/08-gsy-subtitle-and-preview) |
| 09 | `gsy-live-and-ads` | 直播 / RTMP / HLS、贴片广告、静音、切码率、`onResume(false)` | [skills/09-gsy-live-and-ads](file:///D:/workspace/project/GSYVideoPlayer/skills/09-gsy-live-and-ads) |
| 10 | `gsy-cast-and-loopback` | 内核层投屏 SPI (`CastCapability` / `CastProvider`) 与 DLNA 回环 | [skills/10-gsy-cast-and-loopback](file:///D:/workspace/project/GSYVideoPlayer/skills/10-gsy-cast-and-loopback) |
| 11 | `gsy-compose-integration` | Jetpack Compose Wrapper 与 Native 两种范式 | [skills/11-gsy-compose-integration](file:///D:/workspace/project/GSYVideoPlayer/skills/11-gsy-compose-integration) |
| 12 | `gsy-custom-view` | 自定义 UI 组件、控制层、清晰度切换、Danmaku 层、封面 | [skills/12-gsy-custom-view](file:///D:/workspace/project/GSYVideoPlayer/skills/12-gsy-custom-view) |
| 13 | `gsy-proguard-r8` | R8/ProGuard 最小保留规则、Configuration Analyzer、崩溃诊断 | [skills/13-gsy-proguard-r8](file:///D:/workspace/project/GSYVideoPlayer/skills/13-gsy-proguard-r8) |

## SDK 模块速览

| Gradle module | Maven artifactId | 关键内容 |
|---|---|---|
| [gsyVideoPlayer-base](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-base) | `gsyVideoPlayer-base` | `IPlayerManager` / `ICacheManager` / `BasePlayerManager` / `GSYModel` 抽象层 |
| [gsyVideoPlayer-java](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-java) | `gsyVideoPlayer-java` | 主 SDK：`GSYVideoManager`、`StandardGSYVideoPlayer`、`GSYVideoOptionBuilder`、subtitle / preview / render / cast 抽象 |
| [gsyVideoPlayer-armv5 / armv7a / armv64 / ex_so](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-armv7a) | `gsyVideoPlayer-armv7a` 等 | ijkplayer 各 ABI 的 `.so` |
| [gsyVideoPlayer-exo_player2](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-exo_player2) | `GSYVideoPlayer-exo2` | Media3(ExoPlayer) 内核适配 |
| [gsyVideoPlayer-aliplay](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-aliplay) | `GSYVideoPlayer-aliplay` | 阿里云播放器内核适配 |
| [gsyVideoPlayer-cast](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-cast) | `GSYVideoPlayer-cast` | DLNA / jUPnP 投屏 Provider |
| [gsyVideoPlayer-compose](file:///D:/workspace/project/GSYVideoPlayer/gsyVideoPlayer-compose) | `GSYVideoPlayer-compose` | Jetpack Compose Wrapper + Host |

## Demo 索引

- 主入口：[MainActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/MainActivity.java)
- Compose 入口：[ComposeDemoListActivity](file:///D:/workspace/project/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/ComposeDemoListActivity.kt)
- 覆盖能力：详情播放、列表、ViewPager2、Fragment、悬浮窗、Cast、字幕、Danmaku、滤镜、Exo 切源、Aliyun、MediaCodec 智能回退等 51 个 Activity。

## 检索技巧

所有 Skill 内的类/方法/包名/行号均可用 `Grep` / IDE 跳转直达。如遇仓库版本升级导致行号漂移，`Grep -n` 直接以类名/方法名重新定位。
