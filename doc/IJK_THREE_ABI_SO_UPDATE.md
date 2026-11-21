# IJK native libraries: crop, EOS, SoundTouch, HTTPS and 16 KiB

The ARM64, ARMv7 and x86_64 native bundles include the three MediaCodec repairs below. MediaCodec baseline: [CarGuo/ijkplayer `df9e456a4ef4c770bc97bb179d58a0cf9d529011`](https://github.com/CarGuo/ijkplayer/commit/df9e456a4ef4c770bc97bb179d58a0cf9d529011).

The SDL bundles also apply the [IJK AudioTrack large-buffer patch](patches/ijk-audiotrack-large-buffer.patch) to that baseline. It allows up to 131,072 playback-accounting spans, allocating only what the negotiated buffer needs. The Android AudioTrack buffer, routing and audio backend are unchanged. Only `libijksdl.so` is rebuilt. Apply the patch to the IJK source before building the SDL module; keep the FFmpeg and player libraries from the same native bundle.

## Included repairs

| Repair | Behavior |
| --- | --- |
| [Output-size/crop propagation, IJK #2](https://github.com/CarGuo/ijkplayer/pull/2) | Correct H.264 crop-only and single-axis size updates when MediaCodec reports an output-format event. This does not recreate the codec or handle a missing format event. |
| [Opt-in H.264 crop rebuild, IJK #4](https://github.com/CarGuo/ijkplayer/pull/4) | For a supported crop-only SPS/PPS+IDR transition, hold the access unit, drain the old decoder generation, recreate and replay once. Includes seek-baseline restoration, NEW_EXTRADATA ownership and checked SDL operations. |
| [EOS input progress, IJK #5](https://github.com/CarGuo/ijkplayer/pull/5) | Allow output to continue while EOS waits for an input slot, instead of holding the output codec mutex or announcing a reconfigure request. Retry/error handling, guards, counters and actual completion conditions are preserved. |

The EOS repair applies automatically to asynchronous MediaCodec input, including AVC/HEVC with the crop workaround disabled. It does not change synchronous execution, Java APIs, timeout values or default options.

## SoundTouch audio continuity

The player update for [#4274](https://github.com/CarGuo/GSYVideoPlayer/issues/4274) prevents small audio timestamp rounding differences from repeatedly flushing SoundTouch and breaking up otherwise continuous audio. Real timestamp discontinuities, format changes and seeks still reset the transform. This applies only when SoundTouch is active and does not change player options or audio routing. Only `libijkplayer.so` was rebuilt; the matching FFmpeg and SDL binaries are unchanged.

## Enable the crop workaround

`mediacodec-h264-crop-workaround` defaults to **0**. Set it before preparing a new player when an affected H.264 stream/device needs the workaround. The path must already use asynchronous IJK MediaCodec decoding; this option does not enable hardware decoding and does not apply to HEVC, software decoding or synchronous input.

For a direct IJK player:

```java
ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER,
        "mediacodec-h264-crop-workaround", 1L);
```

In GSY, add the equivalent `VideoOptionModel` to a **copy of the manager's existing option list**, replacing only a duplicate entry for this option. Do not replace unrelated caller options. The [source integration guide](https://github.com/CarGuo/ijkplayer/blob/df9e456a4ef4c770bc97bb179d58a0cf9d529011/doc/H264_MEDIACODEC_CROP_WORKAROUND.md#build-and-enable) contains the complete list-preserving example. Set the option to 0 before preparing a fresh player to disable it.

Keep `mediacodec-handle-resolution-change=1` if the caller already uses it. Accepted `NEW_EXTRADATA` transfers ownership to the existing resolution-change path. Source-description changes stop future crop admission; this does not disable the existing resolution-change handler.

### Supported input and limits

- Crop admission is limited to a bounded progressive 8-bit 4:2:0 H.264 subset with one complete SPS/PPS pair before one IDR slice. PPS and non-crop SPS content must stay compatible. Annex B and supported avcC framing are handled; unsupported parameter sets or framing do not enter this workaround.
- After a committed crop change, a real flush can restore the pre-workaround configuration before reconsidering the new access unit. This is baseline restoration, not an arbitrary seek-configuration history. A later headerless target might not identify its required configuration.
- A new crop transition needs a real Surface. Replacing a Surface or changing an authoritative stream description does not guarantee seamless or loss-free presentation. Existing codec failure paths remain failures rather than reporting a false completion.
- The EOS repair has no separate option. Its general asynchronous-EOS behavior should not be confused with the crop option's default-off scope.

## Use complete matching native tuples

Package matching `libijkffmpeg.so`, `libijksdl.so` and `libijkplayer.so` for every selected ABI. The player uses `SDL_AMediaCodecJava_releaseChecked` and `SDL_AMediaFormatJava_setBufferChecked`; an unrelated older SDL is unsupported.

| ABI | Standalone module directory | Matching `ex_so` directory |
| --- | --- | --- |
| arm64-v8a | `gsyVideoPlayer-armv64/src/main/jniLibs/arm64-v8a/` | `gsyVideoPlayer-ex_so/src/main/jniLibs/arm64-v8a/` |
| armeabi-v7a | `gsyVideoPlayer-armv7a/src/main/jniLibs/armeabi-v7a/` | `gsyVideoPlayer-ex_so/src/main/jniLibs/armeabi-v7a/` |
| x86_64 | `gsyVideoPlayer-x86_64/src/main/jniLibs/x86_64/` | `gsyVideoPlayer-ex_so/src/main/jniLibs/x86_64/` |

Both directories for an ABI contain identical bytes. Select standalone ABI modules or `ex_so`, avoiding duplicate native copies. Legacy `armeabi`/`x86` and RTMP libraries are outside this update. Native changes do not select an application backend or alter caller defaults. A repository merge does not publish a new Maven release.

## HTTPS and server authentication

All three ABIs include OpenSSL, HTTPS and TLS, with certificate/hostname verification and explicit TLS-policy inheritance for nested HLS requests. **`tls_verify` defaults to `0`: default HTTPS does not authenticate the server.**

For authenticated HTTPS, set these FORMAT options before opening the URL:

```java
ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "tls_verify", 1L);
ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "ca_file", trustedPemPath);
```

GSY callers can pass the same FORMAT options through their existing `VideoOptionModel` list. The application must provide a readable PEM CA file with trusted provenance and updates. This OpenSSL backend does not automatically use Android's Java trust store. Keep explicit TLS policy for nested HLS, do not disable verification to bypass certificate errors, and do not provide client-authentication material to untrusted playlists.

OpenSSL 3.5 can reject old TLS versions and weak certificates. The native update does not lower those security settings. The ARMv7 certificate-loading fix is retained.

## ABI and 16 KiB packaging

| ABI | ELF LOAD page layout | Native minimum API |
| --- | ---: | ---: |
| arm64-v8a | 16,384 bytes | 21 |
| x86_64 | 16,384 bytes | 21 |
| armeabi-v7a | 4,096 bytes | 16 |

GSY's Java core still requires API 23; lower native floors do not lower the application's minimum SDK. ARMv7 retains its existing 32-bit 4 KiB layout.

16 KiB support also requires correct packaging of the final APK/AAB. Keep the selected ABI's matching libraries, use an Android build-tool configuration that supports 16 KiB native-library packaging, and check the final APK alignment, for example:

```sh
zipalign -c -P 16 -v 4 app.apk
```

ELF segment alignment and APK ZIP-entry alignment are separate requirements. Do not infer final application compatibility from the native library filenames or an ABI selection alone.

## Source and dependency pins

- IJK player (including SoundTouch): [`bf8e381780dc2046687dc135a78df3e24d05d7ca`](https://github.com/CarGuo/ijkplayer/commit/bf8e381780dc2046687dc135a78df3e24d05d7ca).
- IJK MediaCodec/SDL baseline: `df9e456a4ef4c770bc97bb179d58a0cf9d529011`, tree `0752500118251c3a7a71a9698da28e4ff516c3fc`.
- FFmpeg n5.1.10 plus integration patches: `0c8735b51d29dbc74e18c235246d5230bd5db989`.
- OpenSSL 3.5.9: `45e844fa2a14ec92d146bd8f5778ac130b6625fb`.
- libyuv: `a0c7dd3e4b095649881f39152655437b8cde1589`; SoundTouch: `6bf39cd3bf6b0c156267d12446b0d6bdcfcd53c2`.
- Toolchain: Android NDK r22b `22.1.7171670`, original per-ABI configurations. The ARMv7/Clang 11 OpenSSL `-fno-unroll-loops` setting is retained.

Use the [FFmpeg 5 build guide](https://github.com/CarGuo/ijkplayer/blob/df9e456a4ef4c770bc97bb179d58a0cf9d529011/doc/FFMPEG5.md), `init-android-ffmpeg5.sh` and the pins above. Do not use the old FFmpeg 4 initializer. Retain the [dependency and license notices](https://github.com/CarGuo/ijkplayer/blob/df9e456a4ef4c770bc97bb179d58a0cf9d529011/doc/FFMPEG5_HISTORY.md#tls-behavior-and-dependency-notices) when redistributing.

ARM64's embedded player label remains `ijk-896674e7-eos-paired-20261105`; ARMv7/x86_64 use `ijk-272f9fb-eos-final-20261106`. The labels predate the SoundTouch update and are not source revisions. Use the exact file hashes below to identify the bundle rather than interpreting the version label as a commit identifier.

## Exact files

Each SHA-256 applies to both module destinations for its ABI.

| ABI | Library | Bytes | SHA-256 |
| --- | --- | ---: | --- |
| arm64-v8a | `libijkffmpeg.so` | 11817008 | `699f8f59324ed0b9b68c395b8a330f4005d9e88e85489cccd034530d4623d73d` |
| arm64-v8a | `libijkplayer.so` | 598256 | `50ad0623f67487b2ebb5e26b12594e3014c5659c90309c2109a8e6822d0bbbcf` |
| arm64-v8a | `libijksdl.so` | 378496 | `4f391461189dc67b5234bfc0a7c5fd572e8cc32be07360786167faa2933e686f` |
| armeabi-v7a | `libijkffmpeg.so` | 10849240 | `ede0450349007ab89bf65351bb60f94412c61ca009b3c96996fdc7faa869bd96` |
| armeabi-v7a | `libijkplayer.so` | 410920 | `c956df8bce3fbb1b6875900e9d1e8299f95f2da71870d35d42e6c3915c28c425` |
| armeabi-v7a | `libijksdl.so` | 326192 | `0b04baf163210255d56631668e33a4f2496319581cc911ba636e485ec51183dc` |
| x86_64 | `libijkffmpeg.so` | 16233416 | `3557a976ba734f60bbc05b0110ba145711f7aa16f7e611e9375811aba68e022f` |
| x86_64 | `libijkplayer.so` | 652080 | `fcaca09fde9d38273be6b67edefbfda2e7cb43918617419eed790b7f5da71a07` |
| x86_64 | `libijksdl.so` | 462160 | `eb5585223149a32096609602961e049995970428f2949ee62549255bff7a53fd` |
