# Compose / Java Demo 测试参考手册

> 用途：在 emulator-5554（**1080×2400**）上对 GSYVideoPlayer demo app 做真实回归测试时，
> **不再每次重新滚屏 + dump + find_xy**。本手册沉淀了：
> 1. MainActivity / ComposeDemoListActivity 的 demo 列表入口与定位策略
> 2. 每个 demo 的 Activity 类名、是否自动起播、关键操作步骤、真起播判定信号
> 3. 配套测试脚本入口与已知坑

> 维护人：每发现新 demo 或 demo 行为变更时同步更新本表。
> 最近一次回归：2026-05-20（I 轮，17 个 Compose demo + 3 个 Java demo 全 PASS）

---

## 0. 设备 / 工具约定

| 项 | 值 |
|---|---|
| ADB 设备号 | `emulator-5554` |
| 屏幕分辨率 | `1080 × 2400`（`adb shell wm size`） |
| App 包名 | `com.example.gsyvideoplayer` |
| 主入口 Activity | `.MainActivity` |
| Compose 列表入口 | `.compose.ComposeDemoListActivity` |
| 启动主页 | `adb -s emulator-5554 shell am start -n com.example.gsyvideoplayer/.MainActivity` |

### 配套脚本

| 脚本 | 用途 |
|---|---|
| `/tmp/find_xy.py` | 输入 `ui.xml + 文本`，输出该文本节点中心点 `X Y`。`text="..."[^>]*?bounds="..."` 不跨节点。 |
| `/tmp/real_test.sh X Y EXP LABEL` | Compose demo 真起播脚本：clear logcat → tap → 8s → 校验 `topResumed==EXP` + logcat 信号 + back |
| `/tmp/locate_and_test.sh "title" EXP LABEL` | 在 ComposeDemoListActivity 上滚屏定位 title → 调 real_test.sh |
| `/tmp/java_test.sh X Y EXP LABEL` | Java demo 真起播脚本：进入后自动找 `resource-id 含 start` 的播放按钮 |

### 真起播判定信号矩阵（logcat -d）

| 内核路径 | onPrepared | CURRENT_STATE_PLAYING | 渲染信号 | 进度信号 |
|---|---|---|---|---|
| **IjkPlayer**（默认） | ≥1 | ≥1 | `MEDIA_INFO_VIDEO_RENDERING_START` | `Net speed:` |
| **EXO**（PlayerFactory.setPlayManager(Exo2PlayerManager)） | ≥1 | ≥1 | `videoSizeChanged` / `videoWidth:` | 无 Net speed |
| **Audio**（raw mp3） | ≥1 | ≥1 | 无 | 无 |
| **Java demo（callback 设 null）** | 可能 0 | ≥1 | `MEDIA_INFO_VIDEO_RENDERING_START` | `Net speed:` |

通用统一过滤式（与 [real_test.sh](file:///tmp/real_test.sh) 一致）：
```bash
PREP=$(echo "$LOG"   | grep -c   "onPrepared")
PLAY=$(echo "$LOG"   | grep -c   "CURRENT_STATE_PLAYING")
RENDER=$(echo "$LOG" | grep -cE  "MEDIA_INFO_VIDEO_RENDERING_START|videoSizeChanged|videoWidth:")
NET=$(echo "$LOG"    | grep -c   "Net speed:")
```

---

## 1. MainActivity（Java demo 入口）

> **重要**：item text 显示时被系统大写化（"打开Video" → "打开VIDEO"，"List列表" → "LIST列表"）。
> find_xy.py 必须用大写 keyword 才能匹中。

冷启动 MainActivity，**任务 ID 每次重启会变**，但 demo 项 X 都是 357，cy 列出的是首次 dump 的中心 Y（如卡片高度无改动则可直接复用）。

### 第一屏（顶部，`adb shell am start` 后立刻可见）

| Demo 文本 | 跳转 Activity | (X,Y) | 自动起播 | 备注 |
|---|---|---|---|---|
| 简单播放 | `SimplePlayer.SimpleActivity` | (357, 388) | 是 | 无导航 bar |
| **打开VIDEO** | [`PlayActivity`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/PlayActivity.java#L181) | (357, 567) | 是 | 经典 demo，callback=null 不打 onPrepared 但 render+net 充足 |
| 带控制DEMO | `DetailControlActivity` | (357, 746) | 是 | |
| 完成保留最后一帧 | `KeepLastFrameDemoActivity` | (357, 925) | 是 | |
| 透明 | `DetailTransparentActivity` | (357, 1104) | 是 | |
| 无UI界面 | `PlayEmptyControlActivity` | (357, 1283) | 是 | |
| **滤镜** | [`DetailFilterActivity`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/DetailFilterActivity.java) | (357, 1462) | 是 | GLSL 滤镜，验证 GSYVideoType GL Surface 不被污染 |
| 带广告 | `DetailADPlayer` | (357, 1641) | 是 | |
| 带广告2 | `DetailADPlayer2` | (357, 1820) | 是 | |
| 无缝切换 | `SwitchListVideoActivity` | (357, 1999) | 列表，需点列表项 | |
| **LIST列表** | [`ListVideoActivity`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/ListVideoActivity.java) | (357, 2178) | 是（列表自动起播第一项） | |
| LIST全屏和小窗口列表 | `ListVideo2Activity` | (357, 2347) | 是 | |

### 滚屏 1 后（按 `swipe 540 1900 540 700 500` × 3）

| Demo 文本 | 跳转 Activity | (X,Y) |
|---|---|---|
| 多任务支持 | `RecyclerViewActivity` | (357, 345) |
| 列表详情切换 | `RecyclerView2Activity` | (357, 524) |
| 列表带广告模式 | `ListADVideoActivity` | (357, 703) |
| 硬解码支持 | `RecyclerView3Activity` | (357, 882) |
| 缓存下载支持 | `DetailDownloadPlayer` | (357, 1061) |
| EXO特有缓存下载支持 | `DetailDownloadExoPlayer` | (357, 1240) |
| 普通 ACTIVITY | `DetailNormalActivityPlayer` | (357, 1419) |
| 自定义EXO | `DetailExoListPlayer` | (357, 1598) |
| EXO自适应清晰度 | `ExoAdaptiveTrackActivity` | (357, 1777) |
| 自定义EXO支持字幕 | `ExoSubtitleActivity` | (357, 1956) |
| 通用字幕非EXO | `SubtitleDetailPlayer` | (357, 2135) |
| 音频 | `AudioDetailPlayer` | (357, 2314) |

### 滚屏 2 后（再 swipe × 3）

| Demo 文本 | 跳转 Activity | (X,Y) |
|---|---|---|
| 缓存下载支持 | `DetailDownloadPlayer` | (357, 263) |
| EXO特有缓存下载支持 | `DetailDownloadExoPlayer` | (357, 442) |
| 普通 ACTIVITY | `DetailNormalActivityPlayer` | (357, 621) |
| 自定义EXO | `DetailExoListPlayer` | (357, 800) |
| EXO自适应清晰度 | `ExoAdaptiveTrackActivity` | (357, 979) |
| 自定义EXO支持字幕 | `ExoSubtitleActivity` | (357, 1158) |
| 通用字幕非EXO | `SubtitleDetailPlayer` | (357, 1337) |
| 音频 | `AudioDetailPlayer` | (357, 1516) |
| IJK 内核 | `KernelDemoActivity` | (357, 1695) |
| PROXY 缓存 | `ProxyCacheActivity` | (357, 1874) |
| **CLEAR CACHE** | （按钮） | (357, 2053) |
| **COMPOSE DEMO 大全** | [`.compose.ComposeDemoListActivity`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/ComposeDemoListActivity.kt) | **(357, 2232)** |

> **快捷**：MainActivity 任意位置后**滚 2 次到底，COMPOSE DEMO 大全 永远在 (357, 2232) 附近**。

---

## 2. ComposeDemoListActivity（Compose demo 列表，**25 个**）

> 列表 LazyColumn，**卡片高度随 subtitle 长度变化**（多行卡 ≈ 296 px，单行卡 ≈ 178 px），
> 因此**绝对 Y 坐标不稳定**。推荐用 [locate_and_test.sh](file:///tmp/locate_and_test.sh) 滚屏定位。
>
> 但首屏 / 第二屏 demo 在不滚动时坐标稳定，列在表里以备 emergency 直接 tap。

### 25 个 demo 完整索引（源：[ComposeDemoListActivity.kt#L71-L191](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/ComposeDemoListActivity.kt#L71-L191)）

| # | Title | Activity | 自动起播 | 内核 | 已知操作 |
|---|---|---|---|---|---|
| 0 | P0 · Wrapper 基础 | [`BasicWrapperActivity`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/BasicWrapperActivity.kt) | 是 | Ijk | AndroidView 包装 |
| 1 | P0 · Native 详情 | [`DetailNativeActivity`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/DetailNativeActivity.kt) | 是 | Ijk | 经典 |
| 2 | P0 · Native 完整控件层 | [`FullFeatureNativeActivity`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/FullFeatureNativeActivity.kt) | 是 | Ijk | 自绘控件 |
| 3 | P1 · Native 列表 | [`ListPlayNativeActivity`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/ListPlayNativeActivity.kt) | **否** | Ijk | autoPlay=false，需点列表项 |
| 4 | P1 · Native 切换 URL | [`SwitchUrlActivity`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/SwitchUrlActivity.kt) | 是 | Ijk | |
| 5 | P1 · Native 多窗口 | [`MultiWindowActivity`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/MultiWindowActivity.kt) | **否** | Ijk | autoPlay=false，3 Surface 互斥，需点窗口 |
| 6 | P1 · Native 自动连播 | [`AutoPlayListActivity`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/AutoPlayListActivity.kt) | 是 | Ijk | 段间 release+attach |
| 7 | P1 · Native 列表 + 内层全屏 | [`ListWithFullscreenActivity`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/ListWithFullscreenActivity.kt) | 列表型，需点项 | Ijk | |
| 8 | P5 · Native 滤镜 | [`DetailFilterComposeActivity`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/DetailFilterComposeActivity.kt) | 是 | Ijk | withHost 注入 setEffectFilter |
| 9 | P5 · Native 缓存 / 下载 | [`CacheDownloadComposeActivity`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/CacheDownloadComposeActivity.kt) | 是 | Ijk + Proxy | derivedStateOf→remember 已修复 |
| 10 | P5 · Native 字幕 | [`SubtitleComposeActivity`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/SubtitleComposeActivity.kt) | 是 | **强制 Ijk**（PlayerFactory.setPlayManager(IjkPlayerManager)） | |
| 11 | P5 · Native Seamless 切换 | [`SwitchSeamlessComposeActivity`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/SwitchSeamlessComposeActivity.kt) | 列表型，需点 item1 起播 | Ijk | controller 跨页复用 |
| 12 | P5 · Native 前贴片广告 | [`AdInListComposeActivity`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/AdInListComposeActivity.kt) | 是 | Ijk | AD→AutoComplete→正片，buffer 较慢需 16s |
| 13 | P5 · Native Compose 自绘弹幕 | [`DanmakuComposeActivity`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/DanmakuComposeActivity.kt) | 是 | Ijk | Canvas + textMeasurer |
| 14 | **P5 · Native EXO 多源切换** | [`ExoSwitchSourceComposeActivity`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/ExoSwitchSourceComposeActivity.kt#L66-L72) | 是 | **EXO**（PlayerFactory.setPlayManager(Exo2PlayerManager)） | onDestroy 反射还原 IjkPlayerManager（**P0 防回归核心**） |
| 15 | P5 · Wrapper 真并行多窗口 | [`MultiWindowParallelComposeActivity`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/MultiWindowParallelComposeActivity.kt) | 列表型 | Ijk + CustomManager | |
| 16 | P5-2 · 竖屏短视频 (VerticalPager) | [`VerticalShortVideoComposeActivity`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/VerticalShortVideoComposeActivity.kt) | 是 | Ijk | VerticalPager 单 controller |
| 17 | P5-2 · 悬浮窗（画中画） | [`FloatingWindowComposeActivity`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/FloatingWindowComposeActivity.kt) | **按钮交互** | Ijk | 需 SYSTEM_ALERT_WINDOW 权限 |
| 18 | P5-2 · 多类型列表 | [`MoreTypeComposeActivity`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/MoreTypeComposeActivity.kt) | 列表型 | Ijk | |
| 19 | P5-2 · 图文混排（视频 + WebView） | [`WebDetailComposeActivity`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/WebDetailComposeActivity.kt) | 是 | Ijk | WebView 共存 |
| 20 | P5-2 · 纯音频播放 | [`AudioOnlyComposeActivity`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/AudioOnlyComposeActivity.kt) | **按钮交互** | Ijk(audio) | raw://test33.mp3，进页面后点"播放"按钮；信号只有 prep+play 无 render |
| 21 | P5-2 · 自定义 URL / 本地文件 | [`LocalFileComposeActivity`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/LocalFileComposeActivity.kt) | **按钮交互** | Ijk | "▶ 起播"按钮，"Release"软释放后可再次起播（本次拆分修复点） |
| 22 | P5-2 · MediaCodec 硬解切换 | [`MediaCodecComposeActivity`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/MediaCodecComposeActivity.kt) | **按钮交互** | Ijk + MediaCodec | "▶ 起播"按钮 + "切换硬解 + 重 setUp" |
| 23 | P5-2 · 自定义主题 Controls | [`CustomControlsThemeComposeActivity`](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/CustomControlsThemeComposeActivity.kt) | 是 | Ijk | Slider seek + 主题切换 |
| 24 | P5-2 · 自定义主题 Controls（重复行？看源码确认） | 同上 | | | |

> 实际是 24 个 entries（index 0..23），上表 #24 重复行可忽略。

### 滚屏首屏稳定坐标（仅参考，重启后可能错位）

| # | Title | (X,Y) |
|---|---|---|
| 0 | P0 · Wrapper 基础 | (257, 419) |
| 1 | P0 · Native 详情 | (237, 715) |
| 2 | P0 · Native 完整控件层 | (301, 1011) |
| 3 | P1 · Native 列表 | (237, 1307) |
| 4 | P1 · Native 切换 URL | (281, 1603) |
| 5 | P1 · Native 多窗口 | (259, 1899) |
| 6 | P1 · Native 自动连播 | (280, 2195) |

### 推荐定位方式

```bash
# 通用：滚屏直至 dump 包含 title 文本
bash /tmp/locate_and_test.sh "P5 · Native EXO 多源切换" "ExoSwitchSourceComposeActivity" "I-EXO"
```

---

## 3. 已知坑（Compose 端）

| 坑 | 位置 | 处理方式 |
|---|---|---|
| `controller.release()` 单调置 `released=true` 让控制器一次性失效 | [GSYPlayerController.kt#L703-L756](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/native_/GSYPlayerController.kt#L703-L756) | I 轮拆为 `release()` 软释放 + `dispose()` 永久销毁。已 commit `74c8a0eb` |
| EXO demo 退出未还原 PlayerFactory 会污染 Java 端 | [ExoSwitchSourceComposeActivity.kt#L66-L72](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/ExoSwitchSourceComposeActivity.kt#L66-L72) | onDestroy 反射写回 IjkPlayerManager，回归已验证 |
| Audio demo 多次 release 触发崩溃 | [AudioOnlyComposeActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/AudioOnlyComposeActivity.kt) | H 轮已删重复 release |
| Cache demo derivedStateOf 频繁重组 | [CacheDownloadComposeActivity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/compose/host/CacheDownloadComposeActivity.kt) | 改为 remember |
| Slider 拖拽路径 | [GSYDefaultControls.kt#L144](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-compose/src/main/java/com/shuyu/gsyvideoplayer/compose/native_/GSYDefaultControls.kt#L144) | `dragging = v.coerceIn(0f, 1f)` 边界已校验 |
| zsh 不 word-split unquoted vars | shell 调用 | 用 `awk '{print $1}'` / `$2` 拆 X Y 再传 |
| python regex `[^/]*bounds=` 跨节点失配 | [find_xy.py](file:///tmp/find_xy.py) | 改 `[^>]*?` |
| swipe 速率 100ms 不生效 | shell | 用 400-500ms |

---

## 4. 标准回归 Checklist（下次执行直接抄）

```bash
# 0. 重启 emulator app
adb -s emulator-5554 shell am force-stop com.example.gsyvideoplayer
adb -s emulator-5554 shell am start    -n com.example.gsyvideoplayer/.MainActivity

# 1. Java demo 基线（独立冷启动）
bash /tmp/java_test.sh 357 567  PlayActivity        JAVA-Play
bash /tmp/java_test.sh 357 1462 DetailFilterActivity JAVA-Filter
bash /tmp/java_test.sh 357 2178 ListVideoActivity   JAVA-List

# 2. 进 Compose demo 大全
adb -s emulator-5554 shell am start -n com.example.gsyvideoplayer/.compose.ComposeDemoListActivity

# 3. 自动起播 demo（17 个）按 locate_and_test 顺序跑
bash /tmp/locate_and_test.sh "P0 · Native 详情"            DetailNativeActivity              D2
bash /tmp/locate_and_test.sh "P5 · Native 字幕"            SubtitleComposeActivity           Sub
bash /tmp/locate_and_test.sh "P5 · Native EXO 多源切换"    ExoSwitchSourceComposeActivity    EXO
bash /tmp/locate_and_test.sh "P0 · Native 详情"            DetailNativeActivity              D2-after-EXO   # P0 防回归
bash /tmp/locate_and_test.sh "P5 · Native 滤镜"            DetailFilterComposeActivity       Filter
bash /tmp/locate_and_test.sh "P5 · Native 缓存 / 下载"     CacheDownloadComposeActivity      Cache
bash /tmp/locate_and_test.sh "P0 · Native 完整控件层"      FullFeatureNativeActivity         Full
bash /tmp/locate_and_test.sh "P0 · Wrapper 基础"           BasicWrapperActivity              Wrap
bash /tmp/locate_and_test.sh "P1 · Native 切换 URL"        SwitchUrlActivity                 SwitchUrl
bash /tmp/locate_and_test.sh "P5 · Native 前贴片广告"      AdInListComposeActivity           AD          # 16s buffer
bash /tmp/locate_and_test.sh "P5 · Native Compose 自绘弹幕" DanmakuComposeActivity            Danmaku
bash /tmp/locate_and_test.sh "P5-2 · 竖屏短视频 (VerticalPager)" VerticalShortVideoComposeActivity Vertical
bash /tmp/locate_and_test.sh "P5-2 · 图文混排（视频 + WebView）" WebDetailComposeActivity      Web
bash /tmp/locate_and_test.sh "P5-2 · 自定义主题 Controls"  CustomControlsThemeComposeActivity Theme

# 4. 手动按钮 demo（5 个）：进入页面后找 "▶ 起播" / "播放" 按钮，本地脚本暂未自动化
#    Audio / LocalFile / MediaCodec / Float / Seamless（item1）
#    Audio：prep+play 即 PASS（无 render/net）
#    LocalFile：▶起播 → Release → ▶起播（验证 release/dispose 拆分）
#    MediaCodec：▶起播 → 切换硬解 + 重 setUp
#    Float：需开 SYSTEM_ALERT_WINDOW 权限

# 5. 列表型 demo（5 个）：进页面后点 item1
#    List / MultiWindow / AutoPlay / ListFull / MoreType / MultiPara

# 6. 整盘 crash check
adb -s emulator-5554 logcat -d -b crash | grep -c "FATAL EXCEPTION"   # 应 == 0
adb -s emulator-5554 logcat -d         | grep -c "ANR in"             # 应 == 0
```

### PASS 判定阈值

| Demo 类别 | PASS 条件 |
|---|---|
| 普通 Ijk 自动起播 | `prep ≥ 1 && play ≥ 1 && render ≥ 1 && net ≥ 1` |
| EXO 自动起播 | `prep ≥ 1 && play ≥ 1 && render ≥ 1`（无 net） |
| Audio | `prep ≥ 1 && play ≥ 1` |
| Java demo（callback=null） | `play ≥ 1 && render ≥ 1 && net ≥ 1` |
| 全局 | `FATAL EXCEPTION == 0 && ANR == 0` |

---

## 5. 历史回归记录

| 轮次 | 日期 | 范围 | 结果 |
|---|---|---|---|
| H | 2026-05-19 | P0 修复 | release/dispose 设计 bug 发现 |
| **I** | **2026-05-20** | **17 Compose + 3 Java demo + lib 修复** | **全 PASS, 0 crash 0 ANR. commit `74c8a0eb`** |
