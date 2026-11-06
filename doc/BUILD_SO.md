# IJK / FFmpeg 5 原生库构建

[English](BUILD_SO_EN.md)

> 当前三 ABI 原生库包含输出尺寸、H.264 crop 和 EOS 三轮修复，固定源码为已合并提交 `df9e456a`。功能选项、HTTPS/TLS、16 KiB 打包要求和配套哈希见[原生库说明](IJK_THREE_ABI_SO_UPDATE.md)。使用下列 FFmpeg 5 / NDK r22b 构建入口，不沿用旧 FFmpeg 4 / NDK r10-r13 教程。

## 固定源码与工具

- IJK 构建集成：[df9e456a4ef4c770bc97bb179d58a0cf9d529011](https://github.com/CarGuo/ijkplayer/commit/df9e456a4ef4c770bc97bb179d58a0cf9d529011)。
- FFmpeg：[0c8735b51d29dbc74e18c235246d5230bd5db989](https://github.com/CarGuo/FFmpeg/commit/0c8735b51d29dbc74e18c235246d5230bd5db989)，基线 **n5.1.10**，七个 IJK 集成补丁。
- OpenSSL **3.5.9**：`45e844fa2a14ec92d146bd8f5778ac130b6625fb`；初始化脚本同时固定 libyuv / SoundTouch。
- 构建环境： Linux x86_64、Android NDK **22.1.7171670（r22b）**、Bash、Git、Perl、GNU Make、sha256sum、NASM 或 YASM。不沿用旧 macOS 手工修改 NDK 的教程。

## 在新目录构建

初始化入口是 `init-android-ffmpeg5.sh`，不是历史 `init-android.sh` / `init-android-openssl.sh`。使用新目录避免覆盖既有旧依赖；脚本会校验固定提交与已集成补丁，不重复应用补丁。

```sh
git clone --branch ffmpeg-5.0 https://github.com/CarGuo/ijkplayer.git ijkplayer-ffmpeg5-v14
cd ijkplayer-ffmpeg5-v14
git checkout --detach df9e456a4ef4c770bc97bb179d58a0cf9d529011
./init-android-ffmpeg5.sh

export ANDROID_NDK=/absolute/path/to/android-ndk-r22b
export FFMPEG5_SOURCE="$PWD/extra/ffmpeg5"
export OPENSSL_SOURCE="$PWD/extra/openssl-3.5.9"

for abi in arm64 armv7a x86_64; do
  android/contrib/compile-ffmpeg5.sh "$abi" || exit
  (cd android && ./compile-ijk.sh "$abi") || exit
done
```

构建脚本明确选择 `config/module-lite-more.sh`，当前三 ABI 的独立库与 `ex_so` 使用同一配置。可通过 `FFMPEG5_SOURCE_REPOSITORY` 指定包含固定 FFmpeg 提交的本地仓库或 bundle；这不允许换用任意提交，其他依赖仍需固定源码或缓存。

OpenSSL 的 ARMv7 / Clang 11 构建包含 `-fno-unroll-loops` 并将选项纳入 TLS 缓存标识，这是证书加载崩溃的编译修复；ARM64/x86_64 不添加这一 ABI 专用选项，OpenSSL 上游源码不改。不要复用旧 TLS 缓存替代新库。

## 成套替换与打包

| 构建参数 | Android ABI | GSY 独立模块 |
| --- | --- | --- |
| `arm64` | `arm64-v8a` | `gsyVideoPlayer-armv64`（artifact 为 `gsyvideoplayer-arm64`） |
| `armv7a` | `armeabi-v7a` | `gsyVideoPlayer-armv7a` |
| `x86_64` | `x86_64` | `gsyVideoPlayer-x86_64`（artifact 为 `gsyvideoplayer-x64`） |

每个 ABI 一起替换 `libijkffmpeg.so`、`libijkplayer.so`、`libijksdl.so`，同步到 `gsyVideoPlayer-ex_so/src/main/jniLibs/<abi>` 和独立模块对应目录。不能只更新 FFmpeg 库或混入旧 player/SDL。旧 `armeabi` / `x86` 与 RTMP 库不属于这套构建。

ARM64/x86_64 的 16 KiB ELF 对齐需要静态检查与 APK 打包对齐检查；这些结果不能代替 16 KiB 页设备运行。当前 Java 核心最低 API 23，原生构建保留的 ABI API floor 不等于整套播放器支持旧 Android API。


## 历史构建参考

- [FFmpeg n4.3 / NDK r22 补丁](../16kpatch/README.md)：旧产物复现，不适用于本版 FFmpeg 5。
- [v13.2.1 时的旧构建教程](https://github.com/CarGuo/GSYVideoPlayer/blob/v13.2.1/doc/BUILD_SO.md)：保留历史环境和旧 API 参考。


自定义格式、TLS选项和许可证要求见[固定源码构建指南](https://github.com/CarGuo/ijkplayer/blob/df9e456a4ef4c770bc97bb179d58a0cf9d529011/doc/FFMPEG5.md)。
