# RTSP follow-up candidate: scope and source pins

Status: **RTSP_EMULATOR_VERIFIED_3X_LIMITATION_PHYSICAL_UNTESTED_NOT_RELEASE**.

The API 29 x86_64 software-emulator RTSP gate passed 12 executed cases; one
MediaCodec case was explicitly not applicable. Separate corrected rate tests
pass at 0.25×, 0.5×, 1× and 2×, but fail at 3× for both the candidate and the
previous-branch baseline. The cause is unproven. This is not an all-compatibility
pass or release qualification. See [exact emulator results](RTSP-EMULATOR-RESULTS.md).

## Source and artifact pairing

The RTSP candidate builds on these public source baselines:

- FFmpeg: `ff87490262107e004f6a16765484e36e1973d705`
- IJK: `bb1d1b9fa07019155c88fe69fea9db896a108d0f`
- GSY: `33070dab1ddebb4c516a40a2cc58af78e49dd252`

Exact RTSP source publication pins:

- FFmpeg: `c157591220febd96fdcffb05283f63dd38627081`
- IJK: `b3a5d9e597d1b9e445927dda30b435589f0ef181`

Reconstructed local build snapshot commits are provenance identifiers, separate
from these source publication pins. Preserve the distinction between the
original source baselines, local build snapshots and final RTSP commits.

Use the matching GSY Java manager and all nine native libraries from the same
candidate set: `libijkffmpeg.so`, `libijkplayer.so` and `libijksdl.so` for
`armeabi-v7a`, `arm64-v8a` and `x86_64`. The
[native contract](rtsp-followup-native.json) and
[SHA-256 list](rtsp-followup.SHA256SUMS) identify that set. Do not mix libraries
from the older platform-rate R3 candidate with this RTSP set. Building/checking
three ABIs does not establish runtime coverage on all three.

The GSY manager source SHA-256 is
`ba93b5c07b312b21bd62f0b76ab8ace9649a6edb5683f4b1d462160e0aa63de7`.
The unchanged full class passes the [host session suite](../tests/rtsp-session/README.md)
48/48; the published baseline passes 4/48. Host stubs are not Android runtime.

## Included behavior

1. GSY generation/player guards prevent obsolete callbacks and watchdogs from
   affecting a replacement session. Repeated buffering events do not stack old
   timeouts, and failed initialization does not rearm a stale watchdog.
2. FFmpeg initialization redirects cover OPTIONS, DESCRIBE, SETUP and initial
   PLAY, including session/SDP cleanup and an eight-redirect limit. A different
   port is a different origin; old authentication/Session state must not leak
   to the new origin. Relative Location values remain rejected.
3. IJK preserves RTSP timeout options and reports fatal RTSP I/O errors through
   its existing drain/error path. This does not add automatic reconnection.
4. An opt-in native compressed-queue recovery policy can discard stale live
   media and restart at a keyframe after queue accumulation.

## Opt-in queue recovery

`rtsp-live-max-buffer-ms` is a PLAYER-category option. Default `0` leaves this
policy disabled. Supported nonzero values are `500..60000` milliseconds. For
known live RTSP only, configure it before preparation, for example:

```java
ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER,
        "rtsp-live-max-buffer-ms", 1200);
```

GSY's IJK backend accepts the same PLAYER-category option through
`VideoOptionModel`. The matching native libraries are required; setting an option
on an older binary does not install its implementation.

The policy is guarded to opt-in RTSP with unknown/nonpositive duration at normal
1× playback. Known VOD, other protocols, seek, explicit pause and stepping keep
their previous read behavior. Recovery invalidates old queue generations and
waits for an eligible video keyframe; stale audio is dropped to that restart.
It may cause frame drops, visible jumps or audio gaps. Missing timestamps weaken
time-age reasoning; waiting for a keyframe can exceed the configured cap.

This is neither a strict byte-allocation cap nor an end-to-end latency SLA.
The byte fallback permits one oversized audio/key packet plus flush markers;
camera/server, TCP, decoder, audio output and display delay are outside the
local compressed-packet span. A finite successful fixture does not prove a
continuous latency guarantee or universal camera behavior.

## Timeout, ports and known protocol limits

FFmpeg 5 RTSP FORMAT `timeout` is in **microseconds**. For example,
`20_000_000L` means 20 seconds; `20000` means only 20 milliseconds. It is not a
strict whole-operation deadline: synchronous system DNS resolution cannot be
preempted by the ordinary FFmpeg interrupt callback. A fresh open resolves DNS
again; existing TCP sessions are not migrated to a new address.

Direct A-port → B-port → A-port opens and explicit initialization 302 redirects
are distinct from multi-server Content-Base/SDP control URLs. The latter remain
unsupported as general multi-server RTSP sessions. An absolute control URL may
legitimately be handled by the original gateway; it must not be silently treated
as a Location redirect. Playback-time REDIRECT requests, delayed PLAY after
`initial_pause`, and redirects during subsequent pause/seek PLAY are outside
this initialization fix. Reopen remains an application responsibility after
disconnection or session expiry.

## Qualification boundaries

- Runtime evidence: API 29, x86_64 software emulator, 4 KiB guest pages,
  generated TCP/H.264/AAC fixtures
- No physical-device, hardware-decoder, ARM runtime or 16 KiB runtime acceptance
- No audible-quality, full A/V-sync, real-camera/TLS/UDP/H.265 runtime claim
- Optional external HTTPS/HLS smoke was not run
- 3× rate remains failed; matching baseline failure does not establish cause or
  prove absence of regression
- Earlier failed runs retain their original artifact/oracle outcomes; later
  prospective observer results do not retroactively turn them green

The earlier platform-rate R3 documents remain historical evidence for that
candidate. Their build results or claims do not supersede the current RTSP
scope and exclusions.
