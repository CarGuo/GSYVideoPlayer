# Session lifecycle host regression tests

The candidate `GSYVideoBaseManager.java` passes **48/48** deterministic host-JVM
checks. The baseline at `33070dab1ddebb4c516a40a2cc58af78e49dd252` passes
**4/48**, with 44 failures and a nonzero exit. These are manager-level tests
using Android/player stubs, not an Android build or device RTSP runtime result.
The separate, narrowly scoped emulator evidence is in
[RTSP-EMULATOR-RESULTS.md](../../doc/RTSP-EMULATOR-RESULTS.md).

## Run against a checkout

Requirements: Python 3 and a JDK exposing `jdk.compiler` (JDK 9 or later).
The recorded runs used Java 21 with Java source/target 8. An Android SDK,
Gradle, emulator, server and network access are not needed.

From the repository root:

```sh
python3 tests/rtsp-session/run_host_tests.py
```

By default the runner locates the checkout relative to this directory and tests
its production manager. A different complete manager can be supplied as a
positional argument; `--project-root` selects the checkout providing the eight
reference sources. For example, compare the baseline without changing the
checked-out production file:

```sh
baseline_dir=$(mktemp -d)
git show 33070dab1ddebb4c516a40a2cc58af78e49dd252:gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer/GSYVideoBaseManager.java > "$baseline_dir/GSYVideoBaseManager.java"
python3 tests/rtsp-session/run_host_tests.py "$baseline_dir/GSYVideoBaseManager.java" --project-root . --label baseline
# Expected: nonzero exit, 4 pass and 44 fail
python3 tests/rtsp-session/run_host_tests.py --project-root . --label candidate
# Expected: zero exit, 48 pass and 0 fail
```

Each invocation creates a new isolated temporary build directory and removes it
on exit. To retain generated stubs/classes for inspection, pass
`--output /tmp/gsy-rtsp-host-results`; the runner creates a unique subdirectory
there, avoiding stale classes or overlap between concurrent runs. No production
source is edited, and no existing output directory is cleared.

## What is compiled

The entire supplied manager is copied byte-for-byte and compiled without
rewriting or extracting its methods. `SessionLifecycleTest.java` is unchanged
from the frozen 48-test fixture (SHA-256
`69fc34c087a2cae3fb4c4ed1f88323f868a1df6a5f5e455c4fc77b9a363d7160`).

These eight reference files are read directly from the selected checkout under
`gsyVideoPlayer-java/src/main/java/com/shuyu/gsyvideoplayer`, with
`gsyVideoPlayer-base` supported as the fallback module:

- `video/base/GSYVideoViewBridge.java`
- `listener/GSYMediaPlayerListener.java`
- `model/GSYModel.java`
- `model/VideoOptionModel.java`
- `player/IPlayerManager.java`
- `player/BasePlayerManager.java`
- `player/IPlayerInitSuccessListener.java`
- `cache/ICacheManager.java`

Android Handler/Looper and player dependencies are host-only stubs with explicit
queue ordering and manually controlled time. Injected preparation-failure stack
traces are expected fixture inputs. Source/target 8 checks Java compilation; it
does not verify the Android API surface or actual thread contention.

## Regression coverage

- A replaced at 6 seconds by B on the same synthetic IP and a different port:
  the old 8-second watchdog must not error B; B receives its full own deadline
- Old-player and already-queued callbacks checked at receipt and dispatch,
  including prepared, completion, buffering, seek, error, info and size events
- Repeated buffering starts, timeout disablement and obsolete dequeued timers
- 100 rapid port revisits; release and prepare/release/prepare ordering
- Synchronous and queued preparation failures, stale/current smart fallback,
  same-session fullscreen listener transfer and input-stream preparation

The synthetic URLs never cause network access. The suite does not establish
RTSP protocol handling, native-library compatibility, Android AudioTrack or
MediaCodec behavior, physical-device playback, audible quality, A/V sync, ARM
runtime or 16 KiB page runtime support. Keep these host counts separate from
emulator, native build and device qualification results.
