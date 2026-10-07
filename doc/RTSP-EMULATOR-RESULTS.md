# RTSP emulator results and remaining 3× limitation

Evidence date: 2026-10-05. Status:
**RTSP_EMULATOR_VERIFIED_3X_LIMITATION_PHYSICAL_UNTESTED_NOT_RELEASE**.

The final prospective RTSP gate passed **12/12 executed cases**, with **one
MediaCodec case NOT_APPLICABLE**. This is an API 29 x86_64 software-emulator,
4 KiB guest-page result using generated TCP/H.264/AAC fixtures. It is not an
all-tests-pass or release result. The separate corrected 3× rate test remains
failed in both the candidate and the previous-branch baseline.

## Final RTSP gate

The exact final run is `interval-api29-rtsp`. Instrumentation took 124.01 seconds.
Candidate production/native sources were unchanged during the observer revisions.
Independent replay verified **118 rendered phase captures**.

| Case | Result |
| --- | --- |
| Default pause control, cap disabled | PASS |
| Live pause/recovery, software decoder, cap 1200 ms | PASS |
| Live pause/recovery, MediaCodec | NOT_APPLICABLE; unmodified production selector found no eligible AVC decoder |
| Same-IP/different-port switching | PASS |
| OPTIONS redirect | PASS |
| DESCRIBE redirect | PASS |
| SETUP redirect | PASS |
| Initial PLAY redirect | PASS |
| Relative-redirect rejection and reopen | PASS |
| Midstream timeout error | PASS |
| Old-watchdog session switch | PASS |
| 200000-microsecond timeout and reopen | PASS |
| Obsolete-timeout session switch | PASS |

MediaCodec exclusion is not a pass and does not qualify physical hardware.
Audio evidence is limited to AudioTrack session creation and production buffer
logs; it does not establish audible quality or full A/V synchronization.

## Prospective capture-interval observer

The final observer was frozen before execution. It replaced the previous
150 ms copy-duration cutoff with full capture timing uncertainty, rather than
relabeling that earlier run. A copy's possible timing interval contributes to
conservative frame-age and growth bounds.

- Four-second baseline phase, unchanged three-second pause and eight-second
  recovery phase
- Ten fixed 200 ms capture slots in each phase's fixed final two-second window
- No retries or catch-up captures; missed/crossed slots remain unknown intervals
- Fixed ten-slot upper-median bounds, including unknown slots; at least eight
  complete captures plus 500 ms edge/worst-case-gap coverage required
- Stable source origin/identity, predecessor timing and monotonic scheduling
  checked; semantic failures cannot be demoted to unknown captures
- Original targets unchanged: default control growth **at least 2000 ms**;
  cap-on recovery growth **at most 1500 ms**, observed within **8 seconds**

All four scored windows were complete: **10/10, 10/10, 10/10, 10/10**. The
118 replayed phase captures include captures outside these four scored windows.

| Configuration | Conservative age-growth interval | Acceptance |
| --- | --- | --- |
| Default control, cap 0 | 3102.057726..3127.240780 ms | Lower bound ≥ 2000 ms: PASS |
| Cap 1200 ms | 498.015044..518.638789 ms | Upper bound ≤ 1500 ms within 8 s: PASS |

Case-local native option logs matched the requested cap. Recovery logging was
absent in the cap-off control and present in the cap-on case. This is a bounded
differential fixture result, not a continuous or end-to-end latency SLA.

## Corrected rate and seek evidence

The separate rate oracle used a 12-second local clip, a pre-EOF media-position
window of 1000..9000 ms, at least 6000 ms measured media span and at least
3000 ms EOF headroom. It retained the ±20% rate criterion and independently
checked completion timing and rendered progress. Native applied-rate logs alone
were not treated as proof of achieved speed.

| Requested rate | Candidate measured interval | Candidate rate result | Previous-branch rate result |
| --- | --- | --- | --- |
| 0.25× | 0.2463310422..0.2463768116× | PASS | PASS |
| 0.5× | 0.4974151355× | PASS | PASS |
| 1× | 0.9994846689..0.9997422680× | PASS | PASS |
| 2× | 1.9346968950..1.9351737737× | PASS | PASS |
| 3× | 2.3375191424..2.3382352941× | FAIL; below 2.4× floor | FAIL; 2.1647982063×, also below 2.4× |

The candidate 3× completion bound passed; the baseline 3× completion bound
failed. Both 3× rate gates failed. The baseline comparison shows the failure is
not unique to the candidate, but does not establish its cause, prove emulator
causality or establish a general absence of regressions. Compatibility remains
partially unqualified.

The corrected visible seek test passed **1/1**: a 2000 ms seek produced rendered
frame progress and a position consistent with the target. Rate and seek used
the same host candidate as final RTSP, but a **different instrumentation APK**.

## Exact tested artifacts

These are SHA-256 identities recomputed from the preserved APK bytes. A matching
host does not make distinct test APKs interchangeable.

| Role | SHA-256 |
| --- | --- |
| Candidate host, final RTSP and corrected rate/seek | `1f73c198688427305f46ecd37dd6806042a972f63f475062ec4f57e8a0eeb9f2` |
| Final RTSP interval-observer instrumentation | `8b2c49ae225229b1987870f0322cd195796bd1814ef5569976ee3cb7e7d3e72a` |
| Corrected rate/seek instrumentation; also used for baseline control | `eb68ba5b20ffee51f0b49c4802d874691ffc04a8338fdd03d62463d99a8c6def` |
| Previous-branch baseline host | `f23a146f3a1dd6008132de3f6286e3e6b9330eaf5a875b682c78ef7872469a4a` |

The final RTSP and corrected rate/seek host hashes are identical. The baseline
host is different and retains its own native tuple. See the
[native contract](rtsp-followup-native.json) and
[native hashes](rtsp-followup.SHA256SUMS) for the RTSP candidate library set.

## Retained receipt and raw-log identities

The following hashes were recomputed from the preserved raw files. They identify
the evidence behind this public summary; they are not claims that the raw bundle
is published in this repository.

| Evidence | SHA-256 |
| --- | --- |
| Final execution receipt | `cbac2312df41118db4b47da1245f6b6429a8f09f428a13484c9c4199fabdf910` |
| Independent final RTSP runtime replay receipt | `1be86a86cdc4e22d633067e1a925d6f869a770049a700e320f76761dede4d013` |
| Final RTSP raw instrumentation log | `5c23bfa033253dcf495fb552880663d5b9c996ef8ff39567d85afbd82fbc97c2` |
| Corrected candidate rate raw instrumentation log | `898766a1f96e062d5a709739742f34347d5a081f7f9f550b71a6b1a9b1e5bbc4` |
| Corrected seek raw instrumentation log | `571bea1f389d046df49fa69acda9ed6f4e5733418d09138555332f6daf081b3a` |
| Baseline rate raw instrumentation log | `0a059260e875d32ac019555c193678d10712d72cb5e5bc02d437a5366555b9d2` |

## Earlier runs remain unchanged evidence

| Run | Original result retained |
| --- | --- |
| `rtsp-run1` (API 35) | FAILED: 2 pass, 11 fail |
| `local-audio-control1` | Startup failure/ANR; zero cases |
| `local-api29-control1` | Short-window rate run: 5 pass, 1 fail |
| `rtsp-api29-run1` | 10 RTSP pass, 2 fail, 1 not applicable |
| `latency-api29-diagnostic1` | Two targeted diagnostic cases pass; not a full gate |
| `final-api29-rtsp` | Superseded copy-cutoff oracle: 8 pass, 4 capture failures, 1 not applicable |
| `final-api29-rates` | Corrected rates: 4 pass, 3× fail |
| `final-api29-seek` | Corrected visible seek: pass |
| `baseline-api29-rates` | Previous-branch rates: 4 pass, 3× fail; 3× completion also fails |
| `interval-api29-rtsp` | New prospective RTSP gate: 12 pass, 1 not applicable |

The two-case diagnostic and final interval run do not retroactively turn earlier
failures green. Each result belongs to its exact artifact, runtime and oracle.
No full-device run or broader compatibility conclusion is inferred from them.

## Remaining boundaries

- No physical-device or hardware-decoder qualification
- No ARM runtime or 16 KiB guest-page runtime testing
- No audible-quality, full A/V-sync or panel-scanout qualification
- No real-camera, TLS, UDP or H.265 Android runtime compatibility claim
- Optional external HTTPS/HLS smoke not run
- Default queue guard remains off (`0`); opt-in `500..60000` ms recovery can drop
  stale media, jump at keyframes and cause audio gaps
- No strict byte cap or continuous/end-to-end latency SLA
- General multi-server Content-Base remains unsupported; no auto reconnect
- 3× rate qualification failed; the cause remains unknown

For source pairing, option semantics and integration limits, see
[RTSP-FOLLOWUP.md](RTSP-FOLLOWUP.md). Historical platform-rate R3 results retain
their own scope and do not override these limitations.
