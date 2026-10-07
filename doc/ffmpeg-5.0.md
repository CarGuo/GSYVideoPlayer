# FFmpeg5 当前 RTSP follow-up 候选

状态：`RTSP_EMULATOR_VERIFIED_3X_LIMITATION_PHYSICAL_UNTESTED_NOT_RELEASE`。

API 29、x86_64 软件模拟器、4 KiB guest pages 上的前瞻 RTSP 门禁为 **12 个执行用例通过，1 个 MediaCodec 用例不适用**。单独修正后的倍率测试在 0.25×、0.5×、1×、2× 通过，但候选与上一分支基线的 **3× 均失败，原因未确定**；可见 seek 测试通过。这不代表全部兼容性通过，也不是 release 资格。

请以 [RTSP 集成范围与源码锁定](RTSP-FOLLOWUP.md)、[模拟器精确结果与限制](RTSP-EMULATOR-RESULTS.md)、[原生契约](rtsp-followup-native.json) 和 [九库哈希](rtsp-followup.SHA256SUMS) 为当前 RTSP 候选依据。Java manager 与三个 ABI 的九个原生库必须使用同一候选集合。主机 session 回归是候选 48/48、基线 4/48，仅覆盖确定性 JVM stub 测试。

`rtsp-live-max-buffer-ms` 默认 `0` 关闭，仅对已知直播按需启用 `500..60000` ms；可能丢帧、跳帧或出现音频间隙，不承诺严格字节上限或端到端延迟 SLA。通用 Content-Base 多服务器仍不支持，未增加自动重连。物理设备、硬解、ARM runtime、16 KiB runtime 未验收；外部 HTTPS/HLS smoke 未执行。此前失败记录保持失败，不因后续 observer 通过而改判。

## 历史 platform-rate R3 集成记录

以下保留此前文档原文，仅说明旧候选的构建与验证范围，不覆盖上面的 RTSP 状态与限制。

# FFmpeg5 当前集成

完整源码构建与包检查已通过，具体结果见[构建报告](PLATFORM_RATE_R3_BUILD_RESULT.md)和[验证清单](platformrate-r3-validation.json)。

请阅读[platform-rate R3升级、构建与验证指南](PLATFORM_RATE_R3_UPGRADE.md)。当前集成版本为 `13.3.0-ffmpeg5-platformrate-r3-codec2-java-r1-SNAPSHOT`，Java修复与九个原生库须使用同一候选集合。

[当前状态](PLATFORM_RATE_R3_STATUS.md) · [九库哈希](platformrate-r3.SHA256SUMS) · [原生契约](gsy-platformrate-r3-candidate.json)
