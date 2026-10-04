# Platform-rate R3 current status

Candidate: `13.3.0-ffmpeg5-platformrate-r3-codec2-java-r1-SNAPSHOT`. Native: `v0.8.8-carguo-35-gd5c0b4eb-platformrate-r3`. Production Java: CODEC2_JAVA_R1; gsyijkjava remains 1.0.0. Six player/SDL libraries changed; the three FFmpeg libraries remain exact exp3.

The full source build and package checks have passed; exact results are in the [build report](PLATFORM_RATE_R3_BUILD_RESULT.md) and [validation manifest](platformrate-r3-validation.json).

The exact R3 transition suite (2 cases), Tail7 (7 cases / 446 samples), and Widget3 (3 actual GSY widget/manager cases) have independent physical passes. Five local rates and Mux HTTPS-HLS passed their unchanged assertions. Functional7’s original 6/7 full-suite failure remains: the Apple sample failed DNS before TLS and is excluded as an unavailable source. No 7/7 result is claimed.

Physical scope is Pixel8/API35/AArch64/4096-byte pages. API23+ platform rates are the supported fix; pre23 non1× is outside scope. Other ABIs have compile/static validation only. Static 16KiB alignment is not physical 16KiB device testing. Full demo UI, acoustics/full A/V synchronization, and broad-device certification remain outside the tested scope.

An earlier exp3 x86_64 emulator run failed an audio-only completion-timing assertion; its cause was not established. R3 has not been rerun there, so this work does not establish resolution of that historical observation.

Read the [upgrade/build/test guide](PLATFORM_RATE_R3_UPGRADE.md), [native contract](gsy-platformrate-r3-candidate.json), and [nine-library hashes](platformrate-r3.SHA256SUMS). Maven artifacts, tags and releases remain unpublished; source branch commits are separate.
