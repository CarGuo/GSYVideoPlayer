# Recent Playback Features

This document summarizes recent demo and playback changes so maintainers can quickly find the entry points, APIs, and regression scope.

## Entry Points

| Feature | Demo entry | Main classes | Notes |
| --- | --- | --- | --- |
| WebVTT seek preview | `Open VIDEO` | `PreViewGSYVideoPlayer` | Uses a WebVTT thumbnail track, supporting standalone images and sprite crop coordinates. |
| Unified external subtitles | `Custom EXO subtitles`, `Common subtitles non-EXO` | `GSYSubtitleController`, `GSYSubtitleView` | SRT/WebVTT are parsed and rendered by the UI layer and can work across IJK, System, and Media3. |
| Keep last frame on completion | `Keep last frame` | `KeepLastFrameVideo` | Demo-level flag to keep the current render frame after natural playback completion. |
| Screenshot semantics | Filter and MediaCodec demos | `StandardGSYVideoPlayer`, `GSYRenderView` | Keeps video-only screenshots and adds composed player screenshots including UI. |
| GLSurfaceView effects and lifecycle | `Filter` | `DetailFilterActivity`, `GSYVideoGLView*Render` | Cleans up GL renderer lifecycle and adds filter, texture, multi-window, mask, and blur scenes. |
| Multi-URL quality switching | `Seamless switch` | `SmartPickVideo` | Keeps the two-manager approach and improves position sync, timeout, fallback, and temporary manager release. |
| Exo adaptive quality | `EXO adaptive quality` | `ExoAdaptiveTrackActivity`, `GSYExo2MediaPlayer` | Uses one HLS master playlist or DASH MPD and lets Media3 TrackSelector switch video tracks in one media timeline. |
| Graceful player init failure handling | Global capability | `GSYVideoBaseManager`, each `IPlayerManager` | Routes player creation/init failures through error callbacks and cleanup instead of crashing directly. |
| Exo cache lifecycle and GIF cleanup | Global capability | `ExoSourceManager`, `GifCreateHelper` | Tightens Exo cache open/release behavior and cleans GIF generation state more reliably. |
| DLNA/UPnP casting | `Cast Demo` | `CastCapability`, `JupnpDlnaProvider`, `JupnpDlnaSession`, `SampleCastControlVideo`, `CastDemoActivity` | First-class cast capability built on jUPnP 3.0.3 DLNA `AVTransport:1`; `SetAVTransportURI → Play → Seek` preserves the local position when casting mid-playback; ships with an on-device Loopback Receiver for end-to-end smoke testing. |

## Recent Commit Coverage

Recent commits map to the docs like this:

| Commit | Documentation coverage |
| --- | --- |
| `Add Exo adaptive quality demo and docs` | `README*`, `USE*`, `RECENT_FEATURES*`, `UPDATE_VERSION*`, `ARCHITECTURE.md`, `GSYVIDEO_PLAYER_PROJECT_INFO*` |
| `Harden smart quality switching` | Multi-URL quality switching section, architecture layer table, regression checklist |
| `Harden GL renderer lifecycle and demo` | GLSurfaceView effects section, architecture layer table, regression checklist |
| `Fix screenshot callbacks and composed capture` | Screenshot section, composed screenshot APIs, architecture layer table |
| `Add keep last frame demo` | Keep-last-frame section, entry table, regression checklist |
| `feat: add unified subtitle support` | Unified subtitles section, `SUBTITLE_CN.md`, entry table, regression checklist |
| `Add WebVTT seek preview support` | WebVTT seek preview section, entry table, regression checklist |
| `Handle player init failures gracefully` | Graceful player init failure handling entry, changelog |
| `Improve Exo cache lifecycle and GIF cleanup` | Exo cache lifecycle and GIF cleanup entry, changelog |

## Documentation Coverage

Recent playback feature notes are covered in:

- `README_CN.md` / `README.md`: top-level feature summary and recent feature links.
- `doc/USE.md` / `doc/USE_EN.md`: demo entry points and core APIs.
- `doc/UPDATE_VERSION.md` / `doc/UPDATE_VERSION_EN.md`: Unreleased changelog summary.
- `doc/ARCHITECTURE.md`: layer ownership across UI, Manager, Render, and Exo manager.
- `doc/GSYVIDEO_PLAYER_PROJECT_INFO.md` / `doc/GSYVIDEO_PLAYER_PROJECT_INFO_EN.md`: recent feature mapping in the project structure guide.
- `doc/SUBTITLE_CN.md`: unified subtitle guide.
- `doc/KEEP_LAST_FRAME.md` / `doc/KEEP_LAST_FRAME_EN.md`: keep-last-frame guide.
- `doc/RECENT_FEATURES.md` / `doc/RECENT_FEATURES_EN.md`: full recent feature overview, APIs, and regression checklist.

Build, dependency, SO, publishing, decoder, and FAQ documents are not forced to repeat these playback feature notes because their scope is not demo entry points or playback architecture.

## WebVTT Seek Preview

Seek preview no longer extracts many frames from the original video on the client. The app can use a generated WebVTT thumbnail track. Each cue can point to a separate image or a sprite region:

```text
WEBVTT

00:00:00.000 --> 00:00:05.000
thumbs.jpg#xywh=0,0,160,90
```

Main API:

```java
player.setOpenPreView(true);
player.setPreviewVttUrl("https://example.com/thumbs.vtt");
```

Library classes include `GSYVideoPreviewVttParser`, `GSYVideoPreviewProvider`, and `GSYVideoPreviewFrame`.

## Unified External Subtitles

External subtitles are now a UI overlay capability instead of a Media3-only media source merge. `GSYSubtitleController` loads, parses, and refreshes SRT/WebVTT subtitles by playback position. Failures clear the subtitle view but do not interrupt video playback.

Basic usage:

```java
GSYSubtitleSource source = new GSYSubtitleSource.Builder("https://example.com/subtitle.srt")
    .setMimeType(GSYSubtitleMime.APPLICATION_SUBRIP)
    .setLanguage("zh")
    .setLabel("Chinese")
    .setDefault(true)
    .build();

videoPlayer.setSubtitleSource(source);
videoPlayer.setSubtitleEnabled(true);
```

See [SUBTITLE_CN.md](SUBTITLE_CN.md) for the detailed Chinese subtitle guide.

## Keep Last Frame

`KeepLastFrameVideo` is a demo-level implementation. It overrides the natural completion UI transition and keeps the render view instead of returning to the cover immediately. See [KEEP_LAST_FRAME_EN.md](KEEP_LAST_FRAME_EN.md) for details.

```java
keepLastFrameVideo.setKeepLastFrameWhenComplete(true);
```

This is not a global default. Before moving it into base components, confirm the app's cover policy, surface release policy, and replay behavior.

## Screenshots

Existing video-only screenshot APIs stay unchanged:

```java
player.taskShotPic(listener);
player.saveFrame(file, listener);
```

Composed player screenshots include the video frame and player UI:

```java
player.taskShotPicWithView(listener);
player.saveFrameWithView(file, listener);
```

Current fixes:

- SurfaceView PixelCopy now reports every failure path through `null` or `success=false`.
- TextureView, SurfaceView, and GLSurfaceView save callbacks now reflect the actual file write result.
- Composed screenshots capture the video frame first and then draw player UI over it, which is useful for subtitle overlays and controls.

## GLSurfaceView Effects

GL changes are mainly in `DetailFilterActivity` and `GSYVideoGLView*Render`:

- The demo switches to `GSYVideoType.GLSURFACE` while active and restores the previous render type on exit.
- `GSYVideoGLViewBaseRender` and `GSYVideoGLViewSimpleRender` have safer release and screenshot callbacks.
- The `Filter` demo covers regular filters, texture watermark, multi-window playback, image mask, and blurred background scenes.

## Multi-URL Quality Switching

`SmartPickVideo` still targets multiple standalone URLs, such as separate quality URLs or next-episode URLs. It is not standard HLS/DASH ABR. It preloads the target URL with a temporary manager, seeks to the current playback position, and commits when ready.

Current hardening:

- Records the latest playback position to avoid jumping back to 0.
- Adds seek tolerance, retry, and timeout protection.
- Falls back to the original playback and releases the temporary manager on failure.

If the server can provide an HLS master playlist or DASH MPD, prefer the Exo adaptive quality demo below.

## Exo Adaptive Quality

The `EXO adaptive quality` demo uses one HLS master URL or one DASH MPD URL. Exo/Media3 can use multiple bitrate tracks in the same track group for adaptive playback.

Built-in test streams:

- HLS master: `https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8`
- DASH MPD: `https://dash.akamaized.net/envivio/EnvivioDash3/manifest.mpd`

New APIs:

```java
GSYExoVideoManager.instance().getVideoTrackInfoList();
GSYExoVideoManager.instance().clearVideoTrackOverride();
GSYExoVideoManager.instance().setVideoTrackOverride(groupIndex, trackIndex);
```

Notes:

- `Auto` lets TrackSelector pick tracks based on bandwidth, buffer, and device capability.
- Fixed quality uses `TrackSelectionOverride` for a specific video track.
- Clearing the override restores adaptive playback.

## Graceful Player Init Failures

Player initialization hardening is mainly in `GSYVideoBaseManager` and each core `IPlayerManager`. When IJK, System, Exo, or AliPlayer creation/init fails, the flow tries to report `onError` and release resources instead of throwing directly into the app.

There is no standalone demo entry for this global safety behavior. Regression should cover invalid URLs, missing codec capability, or intentionally failed init paths and verify the app enters the error state without crashing.

## Exo Cache Lifecycle And GIF Cleanup

`ExoSourceManager` now handles Exo cache open/reuse/release more carefully to reduce stale resource risks. `GifCreateHelper` also cleans temporary state more reliably after GIF creation finishes, fails, or is cancelled.

There is no standalone entry for this capability. Regression should cover Exo cache playback, leaving and re-entering a page, and GIF creation/failure paths in the filter demo.

## Regression Checklist

Run at least:

```bash
./gradlew :gsyVideoPlayer-java:testDebugUnitTest :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Manual checks:

- `Open VIDEO`: drag the seek bar and confirm WebVTT preview frames appear.
- `Custom EXO subtitles`, `Common subtitles non-EXO`: local SRT, local VTT, and network SRT can switch; failures do not stop playback.
- `Keep last frame`: toggle the flag and confirm completion keeps the last frame or returns to cover.
- `Filter`: switch filters and GL scenes; playback, screenshots, and GIF creation should not crash.
- `Seamless switch`: switch multiple URLs and confirm it does not jump back to 0.
- `EXO adaptive quality`: HLS and DASH play, tracks are listed, and auto/fixed quality switching works.
- Playback failure and init exceptions: confirm they route to error callbacks and do not crash the app.
- Exo cache and GIF: confirm exit/re-enter/failure paths clean resources.
- `Cast Demo`: pick a DLNA device and confirm the local player collapses into a remote-control overlay; on disconnect the local player resumes at the last known remote position. Enable the `Loopback Receiver` for TV-less smoke tests.

## DLNA/UPnP Casting

Cast capability lives inside `gsyVideoPlayer-java` as a first-class kernel SPI — no separate publishing module. The default implementation speaks DLNA `AVTransport:1` on top of jUPnP 3.0.3. The three SPI interfaces are:

- `CastCapability` — entry point, obtained via `GSYVideoBaseManager.getCastCapability()`.
- `CastProvider` — device discovery. Exposes `startDiscovery(CastListener)` / `stopDiscovery()`.
- `CastSession` — a single cast session lifecycle: `setMediaItem(CastMediaInfo)` → `play/pause/stop/seekTo` → `disconnect()`.

Casting from a mid-playback position:

```java
CastCapability cast = GSYVideoManager.instance().getCastCapability();
cast.getProvider().startDiscovery(new CastListener() {
    @Override public void onDeviceFound(CastDevice device) { /* show in a list */ }
});

// When the user picks a device:
long localPositionMs = videoPlayer.getCurrentPositionWhenPlaying();
CastMediaInfo media = new CastMediaInfo(url, title, "video/mp4", /*durationMs*/ 0L, localPositionMs);
CastSession session = cast.connect(selectedDevice);
session.setMediaItem(media);  // SPI internally does SetAVTransportURI → Play → Seek(localPositionMs)
```

Demo: `MainActivity` exposes a dedicated `Cast Demo` entry (`CastDemoActivity`) that provides both the DLNA device picker and the Loopback Receiver toggle. The demo player `SampleCastControlVideo` collapses into a remote-control overlay once casting succeeds — the local surface and audio are released, and the local player resumes at the last known remote position after disconnect.

On-device Loopback Receiver:
- `DevReceiverService` runs in a dedicated `:dlna` process and registers a jUPnP `LocalDevice` (`urn:schemas-upnp-org:device:MediaRenderer:1`) together with `LoopbackAvTransportService` + `LoopbackRenderingControlService`.
- `CastReceiverFloatingWindow` renders the incoming stream in a SYSTEM_ALERT_WINDOW floating window (`CastReceiverPlayer` embeds an IJK kernel).
- `getPositionInfo` / `getTransportInfo` report real progress and state, so the sender's 1 Hz polling shows the actual remote position.
- Service ↔ Activity state changes are synchronised via `setPackage` private broadcasts (`ACTION_STATE_READY` / `ACTION_STATE_STOPPED` / `ACTION_STATE_ERROR`). `RECEIVER_NOT_EXPORTED` is applied on Android 13+.

Dependency toggle: the cast dependency is declared as `deps.jupnp` in [gradle/dependencies.gradle](../gradle/dependencies.gradle) (`org.jupnp:org.jupnp:3.0.3` + `org.jupnp:org.jupnp.support:3.0.3`) and is only pulled in when the cast source set of `gsyVideoPlayer-java` is enabled; downstream projects that do not use cast pay zero AAR increment (see [DEPENDENCIES_EN.md](DEPENDENCIES_EN.md)).

See [CAST_FEATURE_PLAN.md](CAST_FEATURE_PLAN.md) and [CAST_TEST_PLAYBOOK.md](CAST_TEST_PLAYBOOK.md) for the capability goals and pass/fail criteria.
