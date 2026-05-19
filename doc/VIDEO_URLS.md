# Demo Video URLs Registry

This document tracks the curated demo video URLs used by the `app` module. All
hard-coded URLs in demo activities (Java + Kotlin/Compose) reference the
central registry [DemoVideoUrls.java](../app/src/main/java/com/example/gsyvideoplayer/utils/DemoVideoUrls.java)
to keep the showcase reproducible.

This registry only governs the `app` (demo) module. The library modules
(`gsyVideoPlayer-java`, `gsyVideoPlayer-exo2`, `gsyVideoPlayer-compose`, etc.)
do not ship any URL constants.

## Reachable URLs (in use)

| Constant            | URL                                                                                       | Notes                       |
|---------------------|-------------------------------------------------------------------------------------------|-----------------------------|
| `MP4_BBB`           | `https://www.w3schools.com/html/mov_bbb.mp4`                                              | Default MP4 (Big Buck Bunny)|
| `HLS_MUX`           | `https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8`                                       | Default HLS test stream     |
| `HLS_BIPBOP_GEAR1`  | `http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear1/prog_index.m3u8` | Apple bipbop gear1 (low)    |
| `HLS_BIPBOP_GEAR3`  | `http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear3/prog_index.m3u8` | Apple bipbop gear3 (high)   |
| `SAMPLE_GSY`        | `https://res.exexm.com/cw_145225549855002`                                                | GSY sample (legacy demo)    |
| `DASH_ENVIVIO`      | `https://dash.akamaized.net/envivio/EnvivioDash3/manifest.mpd`                            | DASH manifest               |
| `SUBTITLE_SRT`      | `http://img.cdn.guoshuyu.cn/subtitle2.srt`                                                | SRT subtitle sample         |
| `SUBTITLE_VTT`      | `https://stdlwcdn.lwcdn.com/i/8fdb4e20-8ebb-4590-8844-dae39680d837/160p.vtt`              | WebVTT subtitle sample      |

Logical aliases:

- `DEFAULT_VIDEO`        = `MP4_BBB`
- `DEFAULT_HLS`          = `HLS_MUX`
- `DEFAULT_AD`           = `HLS_MUX`
- `DEFAULT_FEATURE`      = `MP4_BBB`
- `LIST_HORIZONTAL`      = `MP4_BBB`
- `LIST_VERTICAL`        = `HLS_MUX`
- `LIST_BBB_FALLBACK`    = `MP4_BBB`
- `SHORT_FORM_FALLBACK`  = `MP4_BBB`

## Removed URLs (unreachable, replaced by same-semantic ones)

| Removed URL                                                                       | Status (probe) | Replacement                         |
|-----------------------------------------------------------------------------------|----------------|--------------------------------------|
| `http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4`                               | connect fail (000) | `MP4_BBB` (mov_bbb.mp4)         |
| `https://cos.icxl.xyz/.../IMG_0382.MP4` (legacy GSY sample)                       | 404            | `MP4_BBB`                            |
| `http://las-tech.org.cn/.../flipfit-cdn.../video_h1.m3u8`                         | 404            | `HLS_MUX` (mux test-streams)         |
| `http://7xjmzj.com1.z0.glb.clouddn.com/20171026175005_JObCxCE2.mp4`               | 404            | `HLS_MUX`                            |

The replacement strategy preserves protocol/format semantics: MP4 → MP4,
HLS → HLS. The original string literal of the live URL is kept inside
`DemoVideoUrls.java`; demo activities only reference the constant.

## Comments cleanup policy

Several demo activities historically contained large blocks of commented-out
URLs accumulated during years of debugging. This pass:

- Cleared the L617-L681 dead-URL graveyard inside [DetailPlayer.java](../app/src/main/java/com/example/gsyvideoplayer/DetailPlayer.java).
- Cleared per-file 1-3 line dead-URL comments in:
  - [DetailFilterActivity.java](../app/src/main/java/com/example/gsyvideoplayer/DetailFilterActivity.java)
  - [DetailControlActivity.java](../app/src/main/java/com/example/gsyvideoplayer/DetailControlActivity.java)
  - [DetailTransparentActivity.java](../app/src/main/java/com/example/gsyvideoplayer/DetailTransparentActivity.java)
  - [WebDetailActivity.java](../app/src/main/java/com/example/gsyvideoplayer/WebDetailActivity.java)
  - [SimpleDetailActivityMode1.java](../app/src/main/java/com/example/gsyvideoplayer/simple/SimpleDetailActivityMode1.java)

Other comment-only references that still describe valid protocol features
(e.g. `rawresource://`, `assets:///`, `ijkhttphook:` prefixes) are kept as
documentation hints.

## Adding new URLs

1. Probe the URL with `curl -fIL` (or `curl --range 0-1024 -o /dev/null`) and
   confirm it returns 200/206/302/304.
2. Add a `public static final String` to [DemoVideoUrls.java](../app/src/main/java/com/example/gsyvideoplayer/utils/DemoVideoUrls.java).
3. From Kotlin/Compose code, reference via `DemoVideoUrls.NAME` directly or
   delegate through a `const val` (Java compile-time constants are
   propagable as Kotlin `const val`).
4. Re-run `./gradlew :app:assembleDebug :gsyVideoPlayer-compose:assembleDebug`.

## Verification

- `:app:assembleDebug`              BUILD SUCCESSFUL
- `:gsyVideoPlayer-compose:assembleDebug` BUILD SUCCESSFUL
- Emulator smoke: `MainActivity` → `DetailControlActivity` / `DetailPlayer`
  reach `GSYVideoPlayer changeUiToNormal` + CCodec setup formats, no
  `player_error` / `FATAL` in logcat.
- Monkey 100 events on `com.example.gsyvideoplayer`, 0 FATAL.
