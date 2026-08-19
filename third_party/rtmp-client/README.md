# GSY RTMP 模块构建说明

**[English](README_EN.md)**

## 背景

Media3 的 `media3-datasource-rtmp` 不包含原生 RTMP 实现，运行时依赖
AntMedia `rtmp-client:3.2.0`。此前使用的 JitPack `v3.2.0.m2` 只增加了
`max-page-size=16384`，其 NDK r21e 产物仍保留旧 `PT_GNU_RELRO` 布局。

2026-08-19 起，RTMP Java API 和四 ABI 原生库归属于正式 Android Library
Module `gsyVideoPlayer-rtmp`，与其他 GSY 模块使用同一版本和发布流程：

- Maven Central：`io.github.carguo:gsyvideoplayer-rtmp:${PROJ_VERSION}`
- GitHub Packages：`com.shuyu:gsyvideoplayer-rtmp:${PROJ_VERSION}`
- 上游仓库：`https://github.com/ant-media/LibRtmp-Client-for-Android.git`
- 固定提交：`3af67842f517f9c6bbd22b961878624d0c44b713`
- NDK：`22.1.7171670`（r22b，与本地 `gsy-ijk` 一致）
- CMake：`3.22.1`
- minSdk：23
- ABI：`arm64-v8a`、`x86_64`、`armeabi-v7a`、`x86`

Android 官方不要求必须使用 NDK r28；r28 只是默认启用 16 KB 对齐。本模块
继续使用 r22b，并显式传入 `max-page-size=16384` 和
`common-page-size=16384`：前者对齐 `PT_LOAD`，后者保证旧 linker 生成的
`PT_GNU_RELRO` 结束地址也落在 16 KB 边界。

逐颗隔离安装扫描已经确认，现有 r22b `gsy-ijk` 三个 64 位 so 能通过
Android 17 的 16 KB 检查，所以无需重编 IJK。

## 重新构建模块

```bash
./third_party/rtmp-client/build-module.sh
```

脚本固定上游提交和工具链，应用已记录的原生 C 正确性补丁，重建四 ABI，
更新 `gsyVideoPlayer-rtmp/src/main` 中的 Java API、许可证和 so，然后执行
模块 `release` AAR 构建与校验。产物为：

```text
gsyVideoPlayer-rtmp/build/outputs/aar/gsyVideoPlayer-rtmp-release.aar
```

## 验证

```bash
./third_party/rtmp-client/verify-aar.sh
```

四 ABI 必须同时满足：所有 `PT_LOAD Align = 0x4000`、`GNU_RELRO` 结束地址
16 KB 对齐、`BIND_NOW`、stack canary、NDK r22b 标识；AAR 还必须包含
Media3 使用的两个 Java API 类。

静态校验不能替代协议测试。正式发布前仍应在 16 KB 设备上完成真实 RTMP
拉流、停止和重新拉流回归。
