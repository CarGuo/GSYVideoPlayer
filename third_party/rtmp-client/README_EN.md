# GSY RTMP module build

**[中文](README.md)**

## Background

Media3 `media3-datasource-rtmp` contains no native RTMP implementation. It
uses AntMedia `rtmp-client:3.2.0` at runtime. The previous JitPack
`v3.2.0.m2` workaround added only `max-page-size=16384`; its NDK r21e binary
still used the legacy `PT_GNU_RELRO` layout.

Since 2026-08-19, the RTMP Java API and four native ABIs belong to the normal
Android Library Module `gsyVideoPlayer-rtmp`. It follows the same version and
publishing flow as every other GSY module:

- Maven Central: `io.github.carguo:gsyvideoplayer-rtmp:${PROJ_VERSION}`
- GitHub Packages: `com.shuyu:gsyvideoplayer-rtmp:${PROJ_VERSION}`
- Upstream: `https://github.com/ant-media/LibRtmp-Client-for-Android.git`
- Pinned revision: `3af67842f517f9c6bbd22b961878624d0c44b713`
- NDK: `22.1.7171670` (r22b, matching local `gsy-ijk`)
- CMake: `3.22.1`
- minSdk: 23
- ABIs: `arm64-v8a`, `x86_64`, `armeabi-v7a`, and `x86`

Android does not require NDK r28; r28 merely enables 16 KB alignment by
default. This module deliberately keeps r22b and explicitly passes both
`max-page-size=16384` and `common-page-size=16384`. The former aligns
`PT_LOAD`; the latter makes the legacy linker's `PT_GNU_RELRO` end land on a
16 KB boundary.

Per-library installation scans confirmed that the existing r22b-built
`gsy-ijk` binaries already pass Android 17's 16 KB checks, so IJK is not
rebuilt.

## Rebuild the module

```bash
./third_party/rtmp-client/build-module.sh
```

The script pins the upstream revision and toolchain, applies the recorded
native C correctness patch, rebuilds all four ABIs, updates the Java API,
license and binaries under `gsyVideoPlayer-rtmp/src/main`, then builds and
verifies the module's release AAR:

```text
gsyVideoPlayer-rtmp/build/outputs/aar/gsyVideoPlayer-rtmp-release.aar
```

## Verify

```bash
./third_party/rtmp-client/verify-aar.sh
```

All four ABIs must have `PT_LOAD Align = 0x4000`, a 16 KB-aligned GNU_RELRO
end, `BIND_NOW`, stack canary instrumentation, and the NDK r22b identity. The
AAR must also contain both Java API classes consumed by Media3.

Static checks do not replace protocol testing. Before a public release, run a
real RTMP play, stop, and replay cycle on a 16 KB device.
