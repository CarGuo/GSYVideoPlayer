# IJK：耳机播放第二次无声怎么采证

适用于 [#4270](https://github.com/CarGuo/GSYVideoPlayer/issues/4270) 这类问题：**第一次可听，退出播放器页面再打开同一视频，画面继续走但听不见**。这里的工具只读采证：不安装 APK、不清 logcat、不改音量、路由、播放器或系统设置。需要故障机、原来的耳机、已授权的 ADB 和 Python 3；模拟器不能替代这条耳机路由。

## 先采一组 AudioTrack

准备好已经能复现问题的 APK，连接同一副耳机，保持相同音量和输出设备。运行：

```sh
adb devices
python3 tests/ijk-audio/capture_audio.py \
  --serial '设备序列号' --backend audiotrack --route bluetooth \
  --out "$(mktemp -d)/gsy-audio-track"
```

有线耳机改成 `--route wired`，USB 耳机用 `usb`。参数只是实验标签，不自动切播放器或路由。

1. 先停在 Demo 的「简单播放」菜单，按终端提示记基线。
2. 打开「简单播放器」，保持第一次播放，按 Enter 采集，再记录此时有无声音。
3. 返回菜单再打开同一页面。同样在进度继续走时按 Enter，哪怕没有声音也不要先暂停、切耳机或退出页面；再记录第二次是否听到声音。

先完成这组即可定位下一步。需要对照时，用独立目录再采外放、OpenSL ES 或 Exo：保持同机、同耳机（外放对照除外）、同一媒体和相同的退出/重进动作。若怀疑网络或 HLS，另用含已知声音的本地样例做一组，别拿换视频的结果直接判断耳机路由。

## 先看什么

| 要区分的现象 | 优先看 |
|---|---|
| 第二次没有创建 AudioTrack 或写入报错 | 对照两次的 `audio_session_id`、`not all data copied`、AudioTrack/AudioFlinger 初始化和异常。没有日志不等于写入正常。 |
| 第二次有 track，但帧或位置不走 | 若系统 dump 暴露了同一 session 的计数，比较两次时间戳明确的 AudioFlinger 快照，先查暂停、欠载或 track 异常。计数没暴露时只能标未知。 |
| 同一 track 在走，仍听不到 | 核对 Audio Policy 的实际输出路由、音量/mute、焦点和应用的 PCM 来源。track 计数前进不是听到声音的证明。 |
| OpenSL ES 有声音但晚 | 单独测起播延时。旧实现会预填最多 255 个 10 ms 静音缓冲；约 2.55 秒是队列量，不是故障机实测值，也不能解释 AudioTrack 无声。 |

`report.json` 标记步骤、实际采集时刻、哪些命令失败；`logcat.txt` 和各服务快照是本地原件。不同 ROM 的字段、权限会不同，采集完整也不等于掌握第一笔有效 PCM 或耳机实际出声时刻。需要分享时**只发脱敏后的相关片段**，注明 GSY/APK 版本、机型、Android 版本、耳机类型和首、次结果。不要把整份系统 dump 或设备标识公开贴到 Issue。

工具测试（模拟 ADB，不是真机验证）：`python3 -B -m unittest discover -s tests/ijk-audio -v`。
