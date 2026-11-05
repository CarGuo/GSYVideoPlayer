# IJK PR #2 player-only native update

This update imports the H.264 output-size fix from [CarGuo/ijkplayer PR #2](https://github.com/CarGuo/ijkplayer/pull/2), merged as [`247dd7db9a59722137e586e7a1d077053e4b0a08`](https://github.com/CarGuo/ijkplayer/commit/247dd7db9a59722137e586e7a1d077053e4b0a08).

When MediaCodec reports `INFO_OUTPUT_FORMAT_CHANGED`, IJK now propagates the H.264 visible dimensions, including crop-only and one-axis changes. HEVC behavior, invalid-size handling and the legacy TI policy remain covered by the original PR tests. This change does not recreate the decoder or supply format notifications that a device does not emit.

## Files and provenance

Only `libijkplayer.so` changes, in both `gsyVideoPlayer-ex_so` and the matching standalone ABI module. The FFmpeg and SDL bytes remain identical to GSY commit `04a036565e03f843a5ba89926e7ff1a64e0dbec2`; the legacy `armeabi`/`x86` libraries and RTMP are unchanged. Original file modes are retained.

| ABI | New `libijkplayer.so` SHA-256 |
| --- | --- |
| arm64-v8a | `01c4dfb802aca43f75f60073610786011d40e39f95d94cb8f4762ef759026374` |
| armeabi-v7a | `0fd6b0ef7352c88700ebf2b92e0fcf5476f6d4513af52b95c691cbae1c4ac0f1` |
| x86_64 | `1fbd80172e70fde982fc393299276bb00b2ba51fccff38cc96721ac4402ebd5a` |

The supplied candidate archive is `ijk-pr2-candidate-three-abi-20261104.zip`, SHA-256 `0a01cc595504de5b25b6a049b58e13f33e0f71a6f142a226a68c3ef0840edc59`. Its source identity is the merged PR above, with baseline `e96feb988e41d686059212f82a74921d20160590`. The previous GSY source pin `df3f5ca6ed56419e7af04de0fd3a7474bb41950b` differs from that baseline only in documentation. The PR production delta changes the output-size adapter and its two format-change call sites; it does not change FFmpeg or SDL production sources.

All nine supplied libraries were checked against their manifest, and the unchanged dependency bytes were checked against the current GSY Git blobs. The two ARM candidates also match the libraries used by the earlier isolated device tests. No fixed-fixture decoder-rebuild experiment is included. The candidate binaries were supplied by the original build task; this desktop validation did not rebuild the Android libraries or independently reproduce that complete build.

## Validation on 2026-11-04

- Original output-size C regression: normal host compilation and ASan/UBSan execution passed. Coverage includes H.264 crop-only, repeated and one-axis changes, rotation, invalid dimensions, HEVC and TI behavior. These tests validate the sizing code, not Android codec execution.
- Three-ABI static checks: file hashes, ELF architecture, SONAME, player exports/imports and bundled dependencies checked. ARM64/x86_64 player LOAD alignment is 16 KiB; ARMv7 is 4 KiB. This is not a 16 KiB page-device runtime certification.
- Redmi M2104K10AC, Android 13/API 33, ARM64: the exact candidate completed fresh ordinary H.264 and HEVC playback runs, each taking 8.110 seconds from prepared to completion for an eight-second clip. Actual hardware decoders were `c2.mtk.avc.decoder` and `c2.mtk.hevc.decoder`. Installed APK and library identities were checked. These ordinary runs collected playback/geometry evidence, not PNG pixel acceptance.
- Earlier ARM64 and ARMv7 runs of these same candidate libraries completed ordinary H.264/HEVC playback. ARMv7 execution was verified as 32-bit. Separate ARM64 cold-start controls for cropped B/C clips passed the frozen full-PNG classifier.
- Prior emulator software-MediaCodec validation was reported by the build task. Its original A/B runtime files were not included in this handoff and were not independently rerun here; it is not counted as additional physical-device or x86_64 runtime coverage.

The Redmi dynamic-crop fixture produces an initial format notification, but no subsequent crop-change notification. Its picture therefore retains the initial crop in both baseline and candidate. This device cannot demonstrate the dynamic trigger fixed by PR #2; that limitation remains recorded rather than being reported as a successful crop test. It does not require a decoder-rebuild change in this update.

Scope is the original format-notification sizing fix. No claim is made that arbitrary dynamic crop, seamless decoder recreation, every-frame presentation or all device families are now supported. Other compatibility investigations are separate from this SO update.
