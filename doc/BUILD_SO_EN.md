# IJK / FFmpeg 5 native builds

[中文](BUILD_SO.md)

> The current three-ABI bundle includes output sizing, H.264 crop and EOS repairs, pinned to merged source `df9e456a`. See the [native-library guide](IJK_THREE_ABI_SO_UPDATE.md) for options, HTTPS/TLS policy, 16 KiB packaging and matching hashes. Use the FFmpeg 5 / NDK r22b build entry below instead of the old FFmpeg 4 / NDK r10-r13 tutorial.

## Source and toolchain identity

- IJK integration: [df9e456a4ef4c770bc97bb179d58a0cf9d529011](https://github.com/CarGuo/ijkplayer/commit/df9e456a4ef4c770bc97bb179d58a0cf9d529011).
- FFmpeg: [0c8735b51d29dbc74e18c235246d5230bd5db989](https://github.com/CarGuo/FFmpeg/commit/0c8735b51d29dbc74e18c235246d5230bd5db989), **n5.1.10** plus seven IJK integration patches.
- OpenSSL **3.5.9**: `45e844fa2a14ec92d146bd8f5778ac130b6625fb`. Initialization also pins libyuv / SoundTouch.
- Build environment: Linux x86_64, Android NDK **22.1.7171670 (r22b)**, Bash, Git, Perl, GNU Make, sha256sum, NASM or YASM. Qualify other hosts separately rather than applying historical manual NDK edits.

## Build in a new directory

Use `init-android-ffmpeg5.sh`, rather than the historical `init-android.sh` / `init-android-openssl.sh`. A new directory avoids replacing old dependencies. The initializer checks immutable commits and already-integrated patches without applying them again.

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

The builder explicitly selects `config/module-lite-more.sh`, shared by the three migrated standalone ABIs and `ex_so`. `FFMPEG5_SOURCE_REPOSITORY` may select a local repository/bundle containing the exact pin; it does not allow another revision or remove dependency pin/cache requirements.

OpenSSL adds `-fno-unroll-loops` only for ARMv7 with Clang 11 and binds it into the TLS cache identity, repairing certificate-loading code generation. ARM64/x86_64 receive no ABI-specific extra flag; upstream OpenSSL source is unchanged. Do not substitute an old TLS cache for rebuilt libraries.

## Replace matched tuples and verify

| Build argument | Android ABI | GSY standalone module |
| --- | --- | --- |
| `arm64` | `arm64-v8a` | `gsyVideoPlayer-armv64` (artifact: `gsyvideoplayer-arm64`) |
| `armv7a` | `armeabi-v7a` | `gsyVideoPlayer-armv7a` |
| `x86_64` | `x86_64` | `gsyVideoPlayer-x86_64` (artifact: `gsyvideoplayer-x64`) |

Replace `libijkffmpeg.so`, `libijkplayer.so` and `libijksdl.so` together for each ABI, in both `gsyVideoPlayer-ex_so/src/main/jniLibs/<abi>` and the corresponding standalone module. Do not mix old player/SDL libraries with a new FFmpeg library. Legacy `armeabi` / `x86` and RTMP libraries are outside this tuple.

ARM64/x86_64 16 KiB ELF alignment and APK packaging alignment need separate inspection; neither qualifies 16 KiB-page device runtime. The Java core requires API 23, independent of the lower ABI-specific native build floors.


## Historical references

- [FFmpeg n4.3 / NDK r22 patches](../16kpatch/README.md): reproduce older artifacts, not this FFmpeg 5 release.
- [The v13.2.1 build tutorial](https://github.com/CarGuo/GSYVideoPlayer/blob/v13.2.1/doc/BUILD_SO_EN.md): historical toolchains and APIs.


For custom formats, TLS policy and license requirements, use the [pinned source build guide](https://github.com/CarGuo/ijkplayer/blob/df9e456a4ef4c770bc97bb179d58a0cf9d529011/doc/FFMPEG5.md).
