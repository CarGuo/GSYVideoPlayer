# 智能硬解降级 + IJK 切 Surface 花屏 — 调研与阶段结论

> 调研日期：2026-05-23
> 来源 Issue：[CarGuo/GSYVideoPlayer#4247](https://github.com/CarGuo/GSYVideoPlayer/issues/4247)
> 状态：已实现并验证智能硬解失败降级软解；切 Surface 花屏修复暂不进入当前提交。
> 验证设备：jfxgpjeul7lrpjkz（M2104K10AC，MTK，Android 13）
> 关联文件：
> - [IjkPlayerManager.java](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/player/IjkPlayerManager.java)
> - [GSYVideoBaseManager.java](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/GSYVideoBaseManager.java)
> - [GSYVideoType.java](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/utils/GSYVideoType.java)
> - [GSYTextureView.java](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/render/view/GSYTextureView.java)
> - [BasePlayerManager.java](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-base/src/main/java/com/shuyu/gsyvideoplayer/player/BasePlayerManager.java)

---

## 一、问题 1：智能硬解 / 硬解失败回退软解

### 1.1 用户原始诉求

> 智能硬解，硬解失败自动退回软解；**怎么知道这次失败是因为硬解码导致的失败？如果其他失败你也降级软解，合不合适？**

—— 答案：**不合适**。需要"组合判据"，不能简单用 `onError` 一刀切。

### 1.2 IJK 错误码语义（关键）

来源：[bilibili/ijkplayer · ijkplayer_android_def.h](https://github.com/bilibili/ijkplayer/blob/master/ijkmedia/ijkplayer/android/ijkplayer_android_def.h)，对应项目里 [IMediaPlayer.MEDIA_ERROR_*](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/GSYVideoBaseManager.java#L665) 的常量定义。

```c
enum media_error_type {
    MEDIA_ERROR_UNKNOWN                            = 1,        // 未知
    MEDIA_ERROR_SERVER_DIED                        = 100,      // 进程级 mediaserver 挂了
    MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200,      // 不能 progressive 播
    MEDIA_ERROR_IO                                 = -1004,    // 网络/IO 失败
    MEDIA_ERROR_MALFORMED                          = -1007,    // 流损坏
    MEDIA_ERROR_UNSUPPORTED                        = -1010,    // 编解码不支持 ★
    MEDIA_ERROR_TIMED_OUT                          = -110,     // 超时
    MEDIA_ERROR_IJK_PLAYER                         = -10000,   // IJK 内部错误（含 codec）
};
```

| ext1 (what)                            | 含义                          | 是否硬解相关 | 是否应回退软解               |
| -------------------------------------- | ----------------------------- | ------------ | ---------------------------- |
| `MEDIA_ERROR_UNKNOWN = 1`              | 未知                          | 可能         | 看 ext2                      |
| `MEDIA_ERROR_SERVER_DIED = 100`        | 进程级 mediaserver 挂了       | ❌            | 不该（重启播放器才对）       |
| `MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200` | 文件特性问题   | ❌            | 不该                         |
| `MEDIA_ERROR_IO = -1004`               | 网络 / IO                     | ❌            | 不该（重连网络才对）         |
| `MEDIA_ERROR_MALFORMED = -1007`        | 流损坏（软解也会挂）          | ❌            | 不该                         |
| **`MEDIA_ERROR_UNSUPPORTED = -1010`**  | **编解码器不支持**            | ✅            | **应回退** ★                 |
| `MEDIA_ERROR_TIMED_OUT = -110`         | 超时                          | ❌            | 不该                         |
| `MEDIA_ERROR_IJK_PLAYER = -10000`      | IJK 内部错误                  | 模糊         | 看 ext2 + 解码状态           |

社区典型硬解失败日志（来自 [issue #1093](https://github.com/bilibili/ijkplayer/issues/1093)、[issue #1248](https://github.com/Bilibili/ijkplayer/issues/1248)）：

```
E/MediaCodec: Codec reported an error. (omx error 0x80001001, internalError -1010)   ← MEDIA_ERROR_UNSUPPORTED
E/MediaCodec: Codec reported an error. (omx error 0x80001009, internalError ...)
[OMX.qcom.video.decoder.avc] ERROR(0x80001009)
```

[yuazhen 在 issue #1248 的回复](https://github.com/Bilibili/ijkplayer/issues/1248#issuecomment-215593825) 已确认：`omx error 0x80001001 / internalError -1010` 就是 MediaCodec 不支持当前流的标志。

### 1.3 怎么"确知本次在硬解"

不能只看 `GSYVideoType.isMediaCodec()`：IJK native 即使你设了 `mediacodec=1`，**MediaCodecList 没匹配上时会自动 fallback 到 avcodec**（[issue #1248 yuazhen 评论原话](https://github.com/Bilibili/ijkplayer/issues/1248#issuecomment-215469765)："那些手机的 MediaCodec 不支持那么高的分辨率吧，所以 ijkplayer fallback 到 avcodec"）。

**判定运行时是否硬解**主要有 2 个 Java 可用口子：

| 信号                                                              | 来源                          | 用途                              |
| ----------------------------------------------------------------- | ----------------------------- | --------------------------------- |
| `IjkMediaPlayer.getVideoDecoder()` = 1/2       | `FFP_PROPV_DECODER_AVCODEC=1` / `FFP_PROPV_DECODER_MEDIACODEC=2` | onInfo/onError 时同步 double-check |
| `IjkMediaPlayer.getMediaInfo().mVideoDecoder` 字符串 `"MediaCodec"` / `"avcodec"` | Java 封装                     | 可读性高，作为兜底判断             |

当前 `io.github.carguo:gsyijkjava:1.0.0` 的 `IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED` 已占用 `10001`，所以 Java 层不依赖 `MEDIA_INFO_VIDEO_DECODER_OPEN` 作为硬解判据。

### 1.4 推荐判据：组合 AND，不是 OR

```
willFallbackToSoftDecode = (
       playerManager instanceof IjkPlayerManager
    && GSYVideoType.isSmartMediaCodec()
    && hardwareDecodeInUse                       // (A) 这次确实在硬解（来自 getVideoDecoder/getMediaInfo）
    && errorLooksLikeCodec(what, extra)          // (B) what∈{-1010, -10000 且 ext2 形似 codec 内部码}
    && !alreadyFallbackedThisSession             // (C) 单次保护
)
```

每条单独说：

- **(A)** 必须等 `onInfo(MEDIA_INFO_VIDEO_DECODER_OPEN, extra)` 回调 `extra==1` 才算确知硬解。极少数情况 `onError` 在 `onInfo(10001)` 之前到（codec configure 阶段），可用 `_getPropertyLong` 兜底确认。
- **(B)** **只有 `MEDIA_ERROR_UNSUPPORTED (-1010)` 是 100% 该回退**；`MEDIA_ERROR_IJK_PLAYER (-10000)` 配合 ext2 看具体子码。其他错误码（IO/SERVER_DIED/MALFORMED/TIMED_OUT）一律走原 [GSYVideoBaseManager.onError](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/GSYVideoBaseManager.java#L665) 的原生路径上抛业务层。
- **(C)** 每个 GSYModel 一个 `smartFallbackTriggered` flag，回退过一次禁止再回退，否则坏流软解也挂 → 又回硬解 → 死循环。

### 1.5 事前预防（可选）

- 在 `initVideoPlayer` 之前用 `MediaCodecList.findDecoderForFormat()` 预判：当前 mime + 分辨率 + profile/level 无人能解 → 干脆不开 mediacodec
- HEVC / VP9 这类高端格式按 [csdn 文章统计](https://blog.csdn.net/sprite/article/details/155208454) H.265 硬解支持率约 70%、VP9 约 50%，可做白名单 / 黑名单

### 1.6 回退动作建议（仅在真触发时执行）

1. 缓存 `getCurrentPosition()` 作为续播点
2. release 当前 IjkMediaPlayer
3. 临时 `GSYVideoType.disableMediaCodec()`（仅本次实例，避免影响全局）
4. 用同一个 GSYModel 重新走 `initVideoPlayer` 重建 mediaPlayer
5. 重绑全部 listener（onCompletion/onBufferingUpdate/onPrepared/onSeekComplete/onError/onInfo/onVideoSizeChanged）
6. 用缓存的 `lastSurface` 重新 `setSurface`
7. `prepareAsync()` → `start()` 后 `seekTo(pendingResumePositionMs)` 续播
8. 还原 `GSYVideoType` 全局标志位

### 1.7 风险点 / 边界情况

- ⚠️ **不能在 native crash（SIGSEGV）路径回退**——java 层根本接不到，已经 tombstone
- ⚠️ 直播流 / HLS 的回退续播位置可能拿不到准（live stream `getCurrentPosition()` 可能返回 0）
- ⚠️ 回退期间 UI 上要给用户反馈（loading 圈），否则会感知到一段黑屏
- ⚠️ 建议作为**新可选 API**（`GSYVideoType.enableSmartMediaCodec()`），默认关闭，不污染存量

### 1.8 可行性结论

✅ **已做整套安全兜底**：native 优先底层降级，Java 层保留最后一道一次性重建保护。关键是判据要细分，不能 onError 一把梭。

当前实现要点：

- 新增 `GSYVideoType.enableSmartMediaCodec()` / `disableSmartMediaCodec()` / `isSmartMediaCodec()`，默认关闭。
- GSY 的 `IjkPlayerManager` 在 `enableMediaCodec()` + `enableSmartMediaCodec()` 同时开启时，向 native 传入 `mediacodec-auto-fallback=1`。
- 本地 `gsy-ijk` 新增 `mediacodec-auto-fallback` player option，默认 0；开启后若 MediaCodec 在首帧前失败，native 层清理硬解实例并转 `ffp_video_thread()` 走 avcodec。
- native 转软解成功时，上层不会收到错误；`getVideoDecoder()` 会从 `FFP_PROPV_DECODER_MEDIACODEC` 切到 `FFP_PROPV_DECODER_AVCODEC`。
- 若 native 仍然上抛明确的 `MEDIA_ERROR_UNSUPPORTED (-1010)`，Java 层才触发一次软解重建兜底。
- 触发后同一个 `GSYModel` 只回退一次，通过内部标记让本次重建跳过 MediaCodec option，不修改全局 `GSYVideoType.isMediaCodec()`。
- 回退时释放当前 IJK 实例和 cache manager，复用当前 Surface、URL、headers、loop、speed、cache 配置，`onPrepared` 后自动 start，并尽量 seek 回原播放进度。
- `BufferedInputStream` 输入源不自动回退，避免流已消费后无法安全重建。
- 如果软解重建后仍失败，错误继续走原有 `onError` 路径上抛业务层。

---

## 二、问题 2：IJK 硬解切换 Surface 花屏

### 2.1 用户原始诉求

> ijk 硬解切换 surface 做屏的时候会出现花屏问题好像 …… **网上应该很多讨论，你找过资料吗？**

—— 我之前没认真找。下面是补的资料。

### 2.2 真机实测（基于 origin/master 原版，无任何代码改动）

设备：`jfxgpjeul7lrpjkz` (M2104K10AC, MTK MT6779, Android 13)
入口：[RecyclerView3Activity](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/app/src/main/java/com/example/gsyvideoplayer/RecyclerView3Activity.java#L124-L130)（demo 中唯一调用 `enableMediaCodec()` 的页面）
触发路径：列表小窗口 ↔ 普通窗口反复切换（IJK 输出 surface 在 SurfaceA / SurfaceB 间替换）

```
I IJKMEDIA: VideoCodec: MediaCodec, OMX.MTK.VIDEO.DECODER.AVC   ← 硬解
D IJKMEDIA: SDL_AMediaCodecJava_configure_surface               ← 切 surface 第 1 次
D MediaCodec: [c2.mtk.avc.decoder] configure
D IJKMEDIA: SDL_AMediaCodecJava_configure_surface               ← 第 2 次
D MediaCodec: [c2.mtk.avc.decoder] configure
... 共 5 次（3 轮 swipe），全部 configure 成功，无 BufferQueue abandoned/disconnect ...
```

**MTK 设备实测无视觉花屏。但社区反馈集中在 Qualcomm / Kirin / 部分高端 MTK G 系列**，单台设备不能下"通杀"结论。

### 2.3 社区根因 — 不在 java 层，在 native 层

**重磅资料：** [bilibili/ijkplayer Pull Request #4395 修复Android切换Surface后导致的硬解码失败问题](https://github.com/bilibili/ijkplayer/pull/4395/files)

PR diff 关键片段：

```c
if (result == RE_INIT_MEDIA_CODEC && (
       mp->ffplayer->mediacodec_all_videos
    || mp->ffplayer->mediacodec_avc
    || mp->ffplayer->mediacodec_hevc
    || mp->ffplayer->mediacodec_mpeg2)) {
    // 触发 native 层重新 init MediaCodec
}
```

修复**不在 java 层**（不在 [GSYTextureView.onSurfaceTextureAvailable](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/render/view/GSYTextureView.java#L62-L79)），而在 IJK native：
- `ffp_set_video_surface` / `ffpipenode_android_mediacodec_vdec` 在 surface 切换时返回 `RE_INIT_MEDIA_CODEC`
- 让 native 层正确丢弃旧 mediacodec 实例并 reconfigure 到新 native window

也就是说 **IJK 自己 surface 切换是有 bug 的**（旧 mediacodec 没正确丢弃 + 重 configure），需要 native 层补丁才能根治。

### 2.4 相关佐证

- [issue #1324 android 用硬解码播放器切后台恢复问题](https://github.com/bilibili/ijkplayer/issues/1324)：surfaceview 重建后重绑 player 导致黑屏几秒。**与切 surface 同根**。
- [csdn《Android ijkplayer 硬解码不生效问题排查与优化实践》](https://devpress.csdn.net/avi/69862d9e0a2f6a37c590692d.html)：明确列出"`SurfaceView`/`TextureView` 不渲染或显示绿色噪点"作为硬解失败现象之一，并指出与 Surface 协作机制相关。
- 厂商兼容性表（同上文章）：

  | 品牌 | 常见问题                            | 解决方案                |
  | ---- | ----------------------------------- | ----------------------- |
  | 华为 | COLOR_FormatYUV420Planar 失效       | 强制使用 Flexible 格式  |
  | 小米 | Surface 提前释放                    | 增加引用计数检查        |
  | 三星 | 4K 视频支持不全                     | 降级到 1080P 解码       |

### 2.5 java 层只能"缓解"，根因在 native

GSY 的 [GSYTextureView.onSurfaceTextureAvailable](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/render/view/GSYTextureView.java#L62-L79) 在 `enableMediaCodecTexture()` 复用分支只调 `setSurfaceTexture(mSaveTexture)`，但 `mSurface` 这个 java 对象**没重建**——它包装的 native window 在 TextureView detach 期间状态可能已失效。把这个旧 `Surface` setSurface 给 IjkPlayer，IJK 再丢给 MediaCodec → 解码到失效 buffer slot → 表现为花屏 / 绿屏。

```java
// gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/render/view/GSYTextureView.java
@Override
public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
    if (GSYVideoType.isMediaCodecTexture()) {
        if (mSaveTexture == null) {
            mSaveTexture = surface;
            mSurface = new Surface(surface);
        } else {
            setSurfaceTexture(mSaveTexture);
            // ⚠️ mSurface 还是旧对象，没重建
        }
        if (mIGSYSurfaceListener != null) {
            mIGSYSurfaceListener.onSurfaceAvailable(mSurface);   // ← 把旧 Surface 再回调上去
        }
    } else {
        mSurface = new Surface(surface);
        if (mIGSYSurfaceListener != null) {
            mIGSYSurfaceListener.onSurfaceAvailable(mSurface);
        }
    }
}
```

### 2.6 三种修法对比

| 修法                                            | 优点                                   | 缺点                                                                 |
| ----------------------------------------------- | -------------------------------------- | -------------------------------------------------------------------- |
| **A. java 层重建 Surface**                       | 不碰 native；无需重编 .so              | 解不了 native mediacodec 实例不丢弃带来的脏帧；强制 reconfigure 一拍卡顿；**严格 BufferQueue 芯片（高通/麒麟）依然可能花屏** |
| **B. 移植 PR #4395 到 ijk native**               | **从根上解花屏**                       | 要重编 ijk native（跨 ABI armv7a/arm64/x86）；CarGuo fork 可能没合这个 patch；维护边界外 |
| **C. java 层重建 Surface + 强制 setSurface 触发 native 重 configure** | 折中；MTK 实测可行                     | 没在 Qualcomm/Kirin 设备验证；不能下"通杀"结论                       |

### 2.7 我之前的判断错误（自我修正）

- 单设备样本（MTK）+ 没找到 PR #4395 → 误判为"java 层重建 Surface 即可"
- "看不见花屏" ≠ "花屏不会发生"——需要在高通 / 麒麟设备至少各跑一台
- native 层补丁才是根治路径，java 层重建 Surface 顶多是 80% 场景的缓解

### 2.8 阶段结论

⚠️ **当前提交不包含切 Surface 花屏修复**：

- 试过移植 [PR #4395](https://github.com/bilibili/ijkplayer/pull/4395/files) 的 reconfigure 思路，但它会在大窗口 / 小窗口切换时引入 seek / loading 观感，用户体验不可接受。
- 继续尝试 `MediaCodec.setOutputSurface()` 后，当前 MTK 真机场景出现返回普通窗口黑屏，说明该方案在 GSY 的 `TextureView` 复用链路下不稳定。
- 因此本轮已经回退所有 Surface reconfigure / `setOutputSurface` / 强制关键帧相关改动，只保留智能硬解降级能力。
- 第二问题需要重新单独设计：先固定可复现脚本和截图 / log 判据，再评估 Java 复用 SurfaceTexture、native reconfigure、或按设备能力开关的组合方案。

---

## 三、关键参考资料

| #   | 类别            | 来源                                                                 | 价值          |
| --- | --------------- | -------------------------------------------------------------------- | ------------- |
| 1   | IJK 错误码定义  | [bilibili/ijkplayer · ijkplayer_android_def.h](https://github.com/bilibili/ijkplayer/blob/master/ijkmedia/ijkplayer/android/ijkplayer_android_def.h) | ★★★★★         |
| 2   | **花屏根因 PR** | [PR #4395 修复 Android 切换 Surface 后硬解码失败](https://github.com/bilibili/ijkplayer/pull/4395/files) | ★★★★★         |
| 3   | 硬解失败案例    | [issue #1093 h264 hw decode failed](https://github.com/bilibili/ijkplayer/issues/1093) | ★★★★          |
| 4   | 硬解 fallback   | [issue #1248 画面卡顿 + yuazhen 回复 fallback to avcodec](https://github.com/Bilibili/ijkplayer/issues/1248) | ★★★★          |
| 5   | 错误码 -10000   | [issue #1338 Error (-10000,0)](https://github.com/bilibili/ijkplayer/issues/1338) | ★★★           |
| 6   | 后台恢复黑屏    | [issue #1324 切后台恢复问题](https://github.com/bilibili/ijkplayer/issues/1324) | ★★★           |
| 7   | 硬解失效排查    | [csdn 《Android ijkplayer 硬解码不生效问题排查与优化实践》](https://devpress.csdn.net/avi/69862d9e0a2f6a37c590692d.html) | ★★★★          |
| 8   | 硬解配置        | [csdn 《Android ijkplayer 硬解码不生效问题排查与解决方案》](https://devpress.csdn.net/avi/69862d9f0a2f6a37c590692e.html) | ★★★           |
| 9   | 性能优化        | [csdn 《ijkplayer 实战：如何优化 Android 视频播放性能》](https://blog.csdn.net/sprite/article/details/155208454) | ★★            |

---

## 四、当前仓库状态

- 代码：已新增 Java 层智能硬解开关、native `gsy-ijk` 首帧前硬解失败转软解、Java 一次性软解重建兜底
- 设备：jfxgpjeul7lrpjkz 已 uninstall 之前误装的改动版 APK
- 测试脚本：`/tmp/validate_smart_mediacodec.sh`（外置临时脚本，未污染仓库），可在确定方案后继续作为回归脚手架
- 关联沉淀文档：[JAVA_TEST_PLAYBOOK.md](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/doc/JAVA_TEST_PLAYBOOK.md)、[doc/test_scripts/java_basic_regression.sh](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/doc/test_scripts/java_basic_regression.sh)

---

## 五、待决策清单

### 5.1 智能硬解

- [x] native 层优先在首帧前硬解失败时转 avcodec；Java 层采用保守白名单 + 单次保护：仅 `-1010` 明确不支持时自动回退
- [ ] 是否需要 §1.5 的 `MediaCodecList` 事前预判？
- [x] 作为新可选 API `GSYVideoType.enableSmartMediaCodec()`，默认关闭
- [ ] 直播流 / HLS 的回退续播策略（`getCurrentPosition()` 不可靠时怎么处理）？

### 5.2 切 Surface 花屏

- [x] 完成资料调研并定位到 IJK native / SurfaceTexture 复用相关风险
- [x] 回退会引入 seek/loading 或黑屏的实验实现
- [ ] 重新设计无 seek、无黑屏、可按设备灰度控制的方案
- [ ] 高通 / 麒麟设备实测

### 5.3 测试覆盖

- [ ] 至少要覆盖的设备组合：MTK ✅ 已测、Qualcomm 待补、Kirin 待补
- [ ] 要覆盖的流：H.264 1080P ✅、H.265 1080P 待补、4K H.265 待补、HLS live 待补
- [ ] 回归脚本是否纳入 [doc/test_scripts/](file:///Users/guoshuyu/workspace/android/GSYVideoPlayer/doc/test_scripts/) 沉淀（参数化 device id）？
