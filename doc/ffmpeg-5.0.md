# FFmpeg 5 native migration candidate

The `ffmpeg-5.0` branch name denotes the migration series. These binaries use
**FFmpeg 5.1.10**, with the IJK compatibility patches and `module-lite-more.sh`.
This is a bounded migration candidate, not an all-device release certification.

## Scope and identity

- GSY baseline: `1c6bace892e5fd16a2dc4ec131a6346e6ea5db27`.
- IJK source baseline: `c92e1e6496c5a479ec93b7b22bf7aa36f1faf31e`, plus the companion FFmpeg 5 migration changes.
- FFmpeg source: `n5.1.10`, commit `19feb712f5c1821d8a3fa1ad63c5bd2e3b9672eb`, plus IJK hooks and multipart-JPEG EOF patches.
- OpenSSL remains the existing `1.1.1w` baseline, commit `e04bd3433fd84e1861bf258ea37928d9845e6a86`.
- Final profile SHA-256: `db17894337914a375415e45073ee913bcc132795f05c86b8df73863e6244572c`.
- Changed native files are only the `libijkffmpeg.so`, `libijkplayer.so` and `libijksdl.so` triples under `gsyVideoPlayer-ex_so/src/main/jniLibs/{arm64-v8a,armeabi-v7a,x86_64}`.
- The existing `armeabi` and `x86` triples and other distribution modules remain unchanged. They are not FFmpeg 5 migration outputs. GSY Java/runtime code is unchanged.

The profile adds the MJPEG parser and multipart-JPEG demuxer. Keep each ABI's
three libraries together; do not mix the new FFmpeg library with an old player
or SDL library. Verify the checked-in candidate from the repository root:

```sh
sha256sum -c doc/ffmpeg-5.0.SHA256SUMS
sha256sum module-lite-more.sh
```

## Native build

Use the companion [IJK `ffmpeg-5.0` branch and build notes](https://github.com/CarGuo/ijkplayer/blob/ffmpeg-5.0/doc/FFMPEG5.md),
not stock IJK or vanilla FFmpeg. The corresponding patched source is also
published on [CarGuo/FFmpeg `ffmpeg-5.0`](https://github.com/CarGuo/FFmpeg/tree/ffmpeg-5.0).
The IJK additive initialization script checks out the pinned CarGuo fork commit
and verifies that both FFmpeg patches are already present. The tested toolchain was Android NDK r22b (`22.1.7171670`).

```sh
# Run from the migrated ijkplayer repository root.
./init-android-ffmpeg5.sh
export ANDROID_NDK=/absolute/path/to/android-ndk-r22b
export FFMPEG5_SOURCE="$PWD/extra/ffmpeg5"
export OPENSSL_SOURCE="$PWD/extra/openssl5"
for abi in arm64 armv7a x86_64; do
  android/contrib/compile-ffmpeg5.sh "$abi"
  (cd android && ./compile-ijk.sh "$abi")
done
```

The native API levels are 21 for ARM64/x86_64 and 16 for ARMv7. The FFmpeg 5
entry point explicitly selects `config/module-lite-more.sh`, removes the
obsolete avresample switch, and requires the IJK demuxers, Annex-B filters,
HTTPS, MJPEG parser and multipart-JPEG demuxer. The legacy initialization path
does not automatically select FFmpeg 5. Rebuilding requires the companion
source changes; this GSY branch only packages their tested outputs.

GSY checks used `:app:assembleDebug`, `:gsyVideoPlayer-ex_so:assembleDebug`,
`:gsyVideoPlayer-java:testDebugUnitTest` and
`:gsyVideoPlayer-cast:testDebugUnitTest`. The demo debug APK and ex_so debug AAR
both embed all nine exact candidate libraries. The Java/cast tests passed
14/14. Packaging and unit tests do not establish full demo UI coverage.

## Recorded validation and remaining limits

The checks below refer to the exact nine-library set in the checksum file.
ARM64 and x86_64 ELF `PT_LOAD` alignment is 16 KB; ARMv7 alignment is 4 KB.
Static ABI, imports/exports, SONAME and RELRO checks passed for all nine files.

- **Official Android 15/API 35 x86_64 16 KB emulation image:** runtime loading and required registrations passed; core IJK playback passed 21/21 with 52 additional callback assertions; additional codec cases passed 9/9; scoped RTSP/RTMP/MJPEG cases passed 8/8. Both system and application page-size checks reported 16384. This is official 16 KB emulation, not a physical ARM64 device.
- **16 KB rotation limitation:** actual displayed GSY frames passed 6/8 cases: all four software angles and MediaCodec 90/180 degrees. MediaCodec 0/270 degrees remained black in that run. A fresh image later crashed `system_server` before test-app installation, so the missing cases and sustained-run coverage were not closed on 16 KB.
- **Android 11/API 30 x86_64 4 KB emulator:** HTTPS trusted-CA/wrong-CA tests passed 2/2; strict RTSP UDP passed with server-confirmed transport; MediaCodec synchronous/asynchronous decode, seek and completion passed. Actual displayed GSY rotation passed 8/8 across software and MediaCodec at 0/90/180/270 degrees. These results do not replace the missing 16 KB checks.
- **4 KB sustained execution:** 1202.4 seconds of MediaCodec playback and 50/50 video creation/release cycles passed. All 236 samples had positive output frame rate. The decoder was the emulator component `OMX.android.goldfish.h264.decoder`, not a vendor hardware decoder. Native-heap window medians grew approximately 0.457 MiB after warmup and 0.133 MiB over release cycles; this is not a zero-leak claim.

Runtime coverage is primarily x86_64. ARM64/ARMv7 playback, real-device codec
drivers, physical audiovisual synchronization and long-running camera/NVR
workloads still need device testing. Fault-injection builds are separate
test-only artifacts and are not included in these production triples.
RTSP/RTMP interruption recovery used explicit release and a new player; bounded
disconnect callbacks and automatic reconnect were not established. Runtime
hardware-to-software fallback does not retain the entire prior GOP and can
wait for a later keyframe. Native embedded `mov_text` callbacks remain
unavailable with the selected minimal profile. Other ABI/module distributions
and iOS are outside this migration's validated scope.

OpenSSL 1.1.1w is an old retained dependency. The existing TLS verification
default is disabled; the explicit HTTPS tests enable `tls_verify=1` and test
CA-chain validation. The FFmpeg 5.1.10 OpenSSL backend does not verify hostname
identity, so these tests do not establish complete HTTPS authentication or an
updated TLS security policy.
