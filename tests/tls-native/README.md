# Native TLS certificate-loading regression

The initial 14.0.0 release review found a reproducible ARMv7 native crash while reading a
valid certificate. Both default HTTPS playback and explicit `tls_verify=1`
playback failed on Redmi M2104K10AC / Android 13 / API 33 / 4096-byte pages.
The matching ARM64 cases passed. A separate C process reproduces the failure
with only the shipped `libijkffmpeg.so`, removing the Java manager, JNI playback
threads and network server from the failing path.

The trusted-certificate call chain reaches
`SSL_CTX_load_verify_locations` → `PEM_X509_INFO_read_bio_ex` →
`OSSL_DECODER_CTX_new_for_pkey` → `ossl_method_store_do_all` →
`alg_copy` → `CRYPTO_memdup`, with an invalid source pointer `0xa` and a
12-byte copy. Compiler-output controls traced the observed ARMv7 behavior to
NDK r22b / Clang 11 optimization. The IJK builder now adds
`-fno-unroll-loops` only for OpenSSL on that compiler/ABI pair and binds it into
the TLS cache identity; upstream OpenSSL code remains unmodified.

## Current combined candidate

The current candidate includes the ARMv7 compiler workaround and FFmpeg
[`0c8735b51d29dbc74e18c235246d5230bd5db989`](https://github.com/CarGuo/FFmpeg/commit/0c8735b51d29dbc74e18c235246d5230bd5db989),
which preserves explicit TLS policy through nested HLS resource opens and
keepalive fallback. It copies the explicit `tls_verify`, `ca_file`, `cafile`,
`verifyhost`, `cert_file` and `key_file` policy, preserving explicit empty proxy
settings. It does not enable verification by default. As for HTTP redirects,
explicit peer-name and client-authentication settings follow nested resources;
do not supply client-authentication files to an untrusted playlist. The exact
source-level limits are in the [FFmpeg regression guide](https://github.com/CarGuo/FFmpeg/blob/0c8735b51d29dbc74e18c235246d5230bd5db989/doc/tests/hls-tls-options.md).

Because HLS policy changes are shared FFmpeg code, all three migrated ABI tuples
(ARMv7, ARM64 and x86_64) are rebuilt together with their matching player and SDL
libraries. Both `gsyVideoPlayer-ex_so` and the corresponding standalone ABI module
receive the same tuple bytes. The previous ARMv7-only publication plan was not
shipped; old ARM64/x86_64 bytes are not retained in this combined update. Legacy
`armeabi`, `x86`, and RTMP libraries remain unchanged.

### Current ARMv7 device scope

The current ARMv7 tuple was exercised on a physical Pixel 5 / API 30 in a
verified 32-bit Java/JNI process in the
2026-10-10 TLS/HLS run.
The 16-case suite reports **15 passed, 1 failed**;
the sole failure is the reproduced original ARMv7 CA-loading crash (SIGSEGV,
exit 139). The overall matrix remains failed. Candidate CA loading, default and
verified direct HTTPS, wrong-CA/DNS/IP rejection, and nested-segment wrong-CA/SAN
rejection passed. Every nested negative had its own trusted positive and then
rejected with no successful TLS handshake or HTTP request.

Trusted AAC HLS playback fetched the master, media playlist and all 13 segments
at 1x and 3x. The 24.021-second media completed at 3x in 8.754 seconds, with a
sampled media-position slope of 3.0028x; all 35 interior speed readings were 3.
The 1x slope was 1.0004x; paired elapsed and slope ratios were 2.8310 and 3.0015.
These are public-player position/timing measurements, not direct AudioTrack
hardware timestamps or captured acoustic output. Three official OpenSSL ARMv7
source-test executables (sparse-array, property and user-property) passed with
complete TAP against separately linked static libcrypto, not dynamic IJK.

The preceding 14-pass/2-fail attempt remains a failure under its original
pre-start getter oracle. Only harness observation timing changed for the rerun;
native library bytes and native clock code did not. The current tested ARMv7
`libijkffmpeg.so` SHA-256 is
`a5db96183b8c9b085617ee59310f6a7ac3ded5e8e7c75c9bfc81acadce54fee5`.
Keep its FFmpeg/player/SDL tuple matched; the older FFmpeg hash below identifies
a different candidate.

### Current ARM64 device scope

The separate 2026-10-10 ARM64 run
exercised the exact final ARM64 tuple on a
physical Pixel 5 / API 30 in a verified 64-bit Java/JNI main process. It reports
**12 passed, 1 failed** across 13 cases. Its sole failure reproduces the original
ARMv7 CA-load crash in an isolated 32-bit child; the overall matrix remains
failed. The old ARM64 CA-load control is separately identified and is not the
current candidate. Final ARM64 CA loading, default/trusted direct HTTPS,
wrong-CA/DNS/IP rejection, trusted HLS 1x/3x playback and nested-segment
wrong-CA/SAN rejection passed. Each negative had its own trusted positive and
then rejected with no successful TLS handshake or HTTP request.

Both ARM64 HLS runs fetched master/media playlists and all 13 segments. The
24.021-second media completed at 3x in 8.585 seconds with a sampled
media-position slope of 2.9853x; all 35 interior speed readings were 3. The 1x
slope was 0.9988x and all 109 interior speed readings were 1. Paired elapsed
and slope ratios were 2.8892 and 2.9891. These are public-player timing/position
measurements, not direct AudioTrack hardware timestamps or acoustic capture.
Official OpenSSL unit executables were not run on ARM64; the ARMv7 unit results
above remain separate. The current tested ARM64 `libijkffmpeg.so` SHA-256 is
`284b474b156a6ae4eaa759793709c9e23a3b95255490116e1c731bb5f7cddab0`.
Keep the final FFmpeg/player/SDL tuple matched.

### Shared qualification limits

Current x86_64 libraries have build/static verification only, with no runtime
result for those bytes. The earlier pre-TLS/HLS ARM64 14-pass/0-fail/3-skip
audio result remains historical; the new bounded TLS/HLS run does not repeat
that broader audio gate. Current ARMv7 and ARM64 evidence does not establish
TLS 1.3, all HLS key/init/live/redirect paths, video/hardware decoding, acoustic
quality, external networks, long sessions, all devices or 16 KiB-page runtime.
Host/source tests, sanitizer checks and ELF alignment are separate evidence,
not device-runtime or whole-release passes.

## Historical direct-HTTPS comparison

The earlier Pixel 3 / API 28 run
used the ARMv7-only candidate built against the
previous FFmpeg pin `c157591220febd96fdcffb05283f63dd38627081`. On the same runtime
and valid CA, the original library crashed at `0xa` after CA loading began
(exit 139), while that candidate returned `Loaded=1`, exit 0. Its actual 32-bit
`IjkMediaPlayer` completed two-second AAC/M4A over owned loopback TLS 1.2 with
default options and explicit verified TLS. Wrong-CA, DNS-name and IP-name cases
had successful trusted prerequisites and then rejected with the expected
certificate alerts, no HTTP request and no preparation/completion.
The original ARM64 CA-load control passed. The suite was 8 passed / 1 failed;
the failure was the original ARMv7 negative control, never a passing CA load.

Those earlier bytes were `libijkffmpeg.so` SHA-256
`a9c7f45f845e48486ae2cb3c3cb72b4529ebfd02be3bb5f666165e590cdc7e0a`.
That run did not cover HLS inheritance and does not qualify the current rebuilt
combined candidate. Do not substitute its libraries for the new matched tuple.

## Run

Requires Python 3, OpenSSL CLI, Android NDK and an authorized adb device.
The runner builds tiny ABI-specific probes, creates a disposable certificate,
and loads the **current repository library bytes** on the device. It does not
install an app or change playback options. Each run cleans its own device
directory and local private key.

```sh
python3 tests/tls-native/run_device_check.py \
  --ndk "$ANDROID_NDK" --serial DEVICE_SERIAL \
  --abis arm64-v8a armeabi-v7a \
  --report app/test_evidence/tls-native.json
```

Without `--abis`, the runner checks every migrated ABI supported by the device
and records the remaining ABIs as not run. Use an x86_64 device/emulator for
that ABI. `--library-dir` can select a directory containing ABI subdirectories,
including extracted AAR native libraries. Reports bind each result to its SHA-256.

Exit zero requires every selected ABI to load the certificate successfully.
A native crash is a failure, not a skip. Rebuild and qualify the affected
FFmpeg/player/SDL set before merging or tagging the candidate. Do not substitute
old libraries, remove ARMv7, or disable TLS verification to pass this gate.

This focused check does not certify the full TLS handshake, hostname rejection,
all certificate types, hardware decoding or 16 KiB-page runtime. It complements
the Java session regressions and the device playback checks.
