# IJK three-ABI matched native bundle (2026-11-05)

This bundle rebuilds all nine libraries from merged [CarGuo/ijkplayer commit `896674e7a249f3d87a831adb2ac5c2b31d55aeff`](https://github.com/CarGuo/ijkplayer/commit/896674e7a249f3d87a831adb2ac5c2b31d55aeff), including the earlier output-format sizing fix and the opt-in H.264 crop fallback in [PR #4](https://github.com/CarGuo/ijkplayer/pull/4). It proposes replacing the native tuples imported by GSY PR #4264 after validation. The [PR #2 update record](IJK_PR2_SO_UPDATE.md) remains historical evidence for its original binaries, not runtime evidence for this rebuild.

**DRAFT ONLY: DO NOT MERGE. This bundle is not yet qualified as usable.** The exact original x86_64 libraries now load successfully with a measured 16,384-byte userspace page size after installing the unchanged full HTTPS smoke APK with `--no-incremental` on ext4. The two earlier IncrementalFS installations of the same APK crashed with SIGBUS and remain part of the record. No SO padding or source/library change was needed for the successful load control. The observed installation-mode difference does not establish the underlying SIGBUS cause.

**HTTPS playback: NOT STARTED / BLOCKED BY EMULATOR UI.** After native load succeeded, the Activity did not create a Surface within 15 seconds. The emulator showed keyguard/current-user initialization problems (`currentUser=-10000`), system ANRs and a black screen. No HTTPS request, preparation or decoded frame was reached. A native-load pass is not an HTTPS-playback pass or full 16 KiB runtime qualification.

The auxiliary audio attempts also did not reach HTTPS: the original audio test stalled during system-CA preparation and was stopped by its 225-second outer timeout; the raw-PEM-copy variant was killed by a system-startup ANR during identity validation. Further testing is stopped; these attempts add no HTTPS-playback evidence.

## Provenance and scope

- IJK source tree: `f7f6b78177f8f6462ba66c1a5c8beb3dc4c98203`.
- FFmpeg n5.1.10 plus seven integration patches: `0c8735b51d29dbc74e18c235246d5230bd5db989`.
- OpenSSL 3.5.9: `45e844fa2a14ec92d146bd8f5778ac130b6625fb`.
- libyuv: `a0c7dd3e4b095649881f39152655437b8cde1589`; SoundTouch: `6bf39cd3bf6b0c156267d12446b0d6bdcfcd53c2`.
- Linux x86_64, Android NDK r22b (`22.1.7171670`), `config/module-lite-more.sh`. The verified official NDK archive SHA-256 is `ac3a0421e76f71dd330d0cd55f9d99b9ac864c4c034fc67e0d671d022d4e806b`.
- All nine SOs were freshly compiled. Dependency pins and the ARMv7/Clang 11 OpenSSL `-fno-unroll-loops` fix are unchanged. Identical output bytes do not mean a retained binary was reused.

Use the [pinned source build guide](https://github.com/CarGuo/ijkplayer/blob/896674e7a249f3d87a831adb2ac5c2b31d55aeff/doc/FFMPEG5.md) and `init-android-ffmpeg5.sh`. In the [full build sequence](BUILD_SO_EN.md), use the IJK commit above instead of the historical v14 baseline, then build `arm64`, `armv7a` and `x86_64`. Do not use the old FFmpeg 4 initializer. Retain the upstream [dependency and license notices](https://github.com/CarGuo/ijkplayer/blob/896674e7a249f3d87a831adb2ac5c2b31d55aeff/doc/FFMPEG5_HISTORY.md#tls-behavior-and-dependency-notices) when redistributing.

This update changes native bundles and documentation only. No Java source, backend selection or application default is changed. Legacy `armeabi`/`x86` libraries and RTMP are outside this update.

## Replace each complete tuple

Always ship the matching `libijkffmpeg.so`, `libijksdl.so` and `libijkplayer.so` together. The crop fallback adds checked SDL operations, so a new player with an unrelated old SDL is unsupported.

| ABI | Standalone destination | Additional matching destination |
| --- | --- | --- |
| arm64-v8a | `gsyVideoPlayer-armv64/src/main/jniLibs/arm64-v8a/` | `gsyVideoPlayer-ex_so/src/main/jniLibs/arm64-v8a/` |
| armeabi-v7a | `gsyVideoPlayer-armv7a/src/main/jniLibs/armeabi-v7a/` | `gsyVideoPlayer-ex_so/src/main/jniLibs/armeabi-v7a/` |
| x86_64 | `gsyVideoPlayer-x86_64/src/main/jniLibs/x86_64/` | `gsyVideoPlayer-ex_so/src/main/jniLibs/x86_64/` |

Both destinations for an ABI contain identical bytes. Existing Git file modes are preserved. Select the required ABI modules or `ex_so`; do not package duplicate copies from both module choices.

The player option `mediacodec-h264-crop-workaround` remains **0 by default**. Enable it only before preparing a new player on a validated asynchronous IJK H.264 MediaCodec path, preserving the caller's existing GSY option list. It does not enable hardware decoding. The [option and limitation guide](https://github.com/CarGuo/ijkplayer/blob/896674e7a249f3d87a831adb2ac5c2b31d55aeff/doc/H264_MEDIACODEC_CROP_WORKAROUND.md) describes the bounded crop-only SPS/IDR admission, baseline restoration and resolution-change coexistence. It does not qualify arbitrary seeks, seamless lifecycle transitions, every-frame presentation or all devices. PR #2 alone did not fix the recorded no-output-format-event dynamic-crop case. Keep `mediacodec-handle-resolution-change=1` when the application already uses it. Accepted `NEW_EXTRADATA` transfers ownership to the existing resolution-change path; source description changes stop new crop admission, and the bounded baseline-restore behavior is not an arbitrary seek-history map. Coexistence does not make the legacy resolution-change path loss-free. No Java/default behavior is changed by this bundle.

## HTTPS is compiled in; playback was not reached

HTTPS/TLS and OpenSSL are compiled in, including certificate/hostname verification and HLS nested TLS-option inheritance fixes. **`tls_verify` still defaults to `0`.** A successful default HTTPS connection therefore does not prove server identity was checked.

For authenticated HTTPS, set format option `tls_verify=1` and a trusted PEM `ca_file` before opening the URL. The application owns CA provenance, updates and a readable local CA path. Preserve those options for nested HLS requests; do not give untrusted playlists client-authentication material. Do not disable verification to work around certificate errors. The smoke harness explicitly sets `tls_verify=1` and an app-private CA file assembled from parsed guest-system PEM roots, without a hostname override. This is not automatic Android Java trust-store integration. Strict positive playback and certificate/hostname rejection tests were **NOT REACHED because the Activity Surface was unavailable**. No HTTPS success is claimed.

## 16 KiB and runtime limits

- ARM64 and x86_64: every SO passed 16,384-byte PT_LOAD alignment and GNU_RELRO checks; native minimum API is 21.
- ARMv7: the existing 4,096-byte layout and native API 16 floor are retained. This is not independent 16 KiB device qualification.
- GSY's Java core still requires API 23. Lower native floors do not lower the application's minimum API.
- ELF alignment does not establish APK/AAB packaging alignment, successful loading or playback. Verify the final APK with `zipalign -c -P 16 -v 4 app.apk`, inspect the actual packaged libraries, and test the installed application on the intended 16 KiB system.
- The same full HTTPS smoke APK, SHA-256 `acd037d290e50655be5028e705a5a8b6c68aae7a3683b356f64478e444d76d25`, failed twice with SIGBUS when installed on IncrementalFS. Reinstalling those unchanged APK bytes with `--no-incremental` placed them on ext4; all three original x86_64 libraries then loaded successfully with a measured 16,384-byte userspace page size.
- The official API 35 compatibility image still uses a 4 KiB kernel. This is neither an ARM64 native 16 KiB-kernel test nor physical ARM64/ARMv7 runtime qualification.
- The subsequent Activity Surface timeout, keyguard/current-user problems, system ANRs and black screen prevented HTTPS requests, preparation and frame output. Playback, TLS rejection tests and full GSY application/runtime regression remain unqualified.
- No production SO was padded, rebuilt or otherwise changed for the installation control. All 18 native destinations retain their original preparation hashes. The installation difference is observed evidence, not a proved low-level crash diagnosis.

An independent audit passed all **135 focused SHA, ELF, feature, version and build-manifest checks** against these nine final files. Static checks covered architecture, SONAME, native minimum-API notes, LOAD/RELRO, absence of TEXTREL/RPATH, non-executable stack, minimum-API strong imports, JNI entry points, checked SDL export/import pairing and PR #2 export continuity. All nine stripped outputs matched stripping their corresponding fresh unstripped outputs. These checks are not a complete security audit or an end-to-end application test.

## Exact bytes

The SHA-256 below applies to both module destinations for each ABI.

| ABI | Library | Bytes | SHA-256 |
| --- | --- | ---: | --- |
| arm64-v8a | `libijkffmpeg.so` | 11817008 | `699f8f59324ed0b9b68c395b8a330f4005d9e88e85489cccd034530d4623d73d` |
| arm64-v8a | `libijksdl.so` | 378336 | `9f7603c825af7b3ba1f3aa893223a4201a46b3bf19bce387adaf1027b7bdcc9e` |
| arm64-v8a | `libijkplayer.so` | 598048 | `cb609eb66373cdfd448f554b08f61043a7c76f4eed1fbbe2e2bcf2e62b5158ea` |
| armeabi-v7a | `libijkffmpeg.so` | 10849240 | `ede0450349007ab89bf65351bb60f94412c61ca009b3c96996fdc7faa869bd96` |
| armeabi-v7a | `libijksdl.so` | 326048 | `9e275b7be65b1068f7439ceeca772d6da9a0eff0ae9e126a11e87d50902650f6` |
| armeabi-v7a | `libijkplayer.so` | 410744 | `7cb4f04ea75b457ece1abda5f2960cdcfc229a29ee62e05f3c3ae333d4e11d45` |
| x86_64 | `libijkffmpeg.so` | 16233416 | `3557a976ba734f60bbc05b0110ba145711f7aa16f7e611e9375811aba68e022f` |
| x86_64 | `libijksdl.so` | 462032 | `25ec7ff81b68e8a29555a2bab6413a8301ed95b16fd5e3823a887cf49a2ce7bc` |
| x86_64 | `libijkplayer.so` | 651984 | `a4e3e8c99e1f35c53114564ccdd8fbbb5584c5efa068975f6fd2a8fce633170e` |
