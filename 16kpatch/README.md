# 16kpatch 使用说明（64位16K + 32位可构建）

## 适用范围
- 基线项目：默认 `ijkplayer`
- 主机环境：Darwin arm64（Apple Silicon）
- NDK：`22.1.7171670`（r22）
- FFmpeg 依赖：`CarGuo/FFmpeg` tag `ijk-n4.3-20260301-007`
- ABI 策略：
  - `arm64-v8a` / `x86_64`：16K page size
  - `armeabi-v7a`：可构建，4K page size

## 补丁文件说明
- `ndk_r22_16k_commit.patch`
  - 当前 `ijkplayer` 主仓改动快照（基于 `9ef1d2b2` 到当前工作树），覆盖构建脚本、64位16K分流、armv7a(r22)兼容、init 固化、README 同步等。
- `ndk_r22_ffmpeg_n4.3_ijk.patch`
  - FFmpeg 侧补丁（`n4.3..ijk-n4.3-20260301-007`），包含 ijk 协议/demuxer 兼容、OpenSSL 探测兼容、导出头、async 注册崩溃修复。
- `ndk_r22_soundtouch.patch`
  - `ijksoundtouch` 的 16K 链接与 STL 兼容补丁。
- `ndk_r22_ijkyuv.patch`
  - `ijkyuv` 的 16K 链接补丁。

## 默认 ijkplayer 项目如何使用（推荐顺序）
1. 获取默认项目
```bash
git clone https://github.com/Bilibili/ijkplayer.git
cd ijkplayer
```

2. 拷贝本目录下 4 个 patch 到任意本地目录（假设为 `/path/to/16kpatch`）

3. 在 `ijkplayer` 根目录应用主补丁
```bash
git apply --check /path/to/16kpatch/ndk_r22_16k_commit.patch
git apply /path/to/16kpatch/ndk_r22_16k_commit.patch
```

4. 分别对第三方目录应用子补丁
```bash
cd ijkmedia/ijksoundtouch
git apply --check /path/to/16kpatch/ndk_r22_soundtouch.patch
git apply /path/to/16kpatch/ndk_r22_soundtouch.patch

cd ../ijkyuv
git apply --check /path/to/16kpatch/ndk_r22_ijkyuv.patch
git apply /path/to/16kpatch/ndk_r22_ijkyuv.patch
```

5. 对 FFmpeg 仓库应用 n4.3 补丁（可选，若直接使用 `ijk-n4.3-20260301-007` tag 可跳过）
```bash
cd /path/to/FFmpeg
git checkout -B codex/n4.3-ijk-arm64 n4.3
git apply --check /path/to/16kpatch/ndk_r22_ffmpeg_n4.3_ijk.patch
git apply /path/to/16kpatch/ndk_r22_ffmpeg_n4.3_ijk.patch
```

6. 回到项目根目录，初始化并编译
```bash
cd ../../
./init-android-openssl.sh
./init-android.sh

cd android/contrib
./compile-openssl.sh arm64
./compile-ffmpeg.sh arm64
./compile-ffmpeg.sh x86_64
./compile-ffmpeg.sh armv7a

cd ..
./compile-ijk.sh arm64
./compile-ijk.sh x86_64
./compile-ijk.sh armv7a
```

## 验证建议
- 64位 16K：
  - `arm64-v8a` 与 `x86_64` 下 `libijkffmpeg.so/libijksdl.so/libijkplayer.so` 的 `PT_LOAD Align` 为 `0x4000`
- 32位 4K：
  - `armeabi-v7a` 下 `libijkffmpeg.so/libijksdl.so/libijkplayer.so` 的 `PT_LOAD Align` 为 `0x1000`
- Stack Canary：`libijkffmpeg.so` 包含 `__stack_chk_fail@LIBC`

## 注意事项
- 生产发布优先 `arm64-v8a`。
- `armeabi-v7a` 主要用于 32 位设备兼容；若目标机型存在 HEVC 软解崩溃风险，建议优先硬解或降低 armv7a 解码能力。
- 若目标仓库已存在同名改动，`git apply --check` 可能失败；请先清理冲突或基于干净分支应用。
