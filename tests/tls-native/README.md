# Native TLS certificate-loading regression

14.0.0 release review found a reproducible ARMv7 native crash while reading a
valid certificate. Both default HTTPS playback and explicit `tls_verify=1`
playback failed on Redmi M2104K10AC / Android 13 / API 33 / 4096-byte pages.
The matching ARM64 cases passed. A separate C process reproduces the failure
with only the shipped `libijkffmpeg.so`, removing the Java manager, JNI playback
threads and network server from the failing path.

The trusted-certificate call chain reaches
`SSL_CTX_load_verify_locations` → `PEM_X509_INFO_read_bio_ex` →
`OSSL_DECODER_CTX_new_for_pkey` → `ossl_method_store_do_all` →
`alg_copy` → `CRYPTO_memdup`, with an invalid source pointer `0xa` and a
12-byte copy. The exact source/build cause still requires native investigation;
the crash is not evidence that upstream OpenSSL itself needs a source patch.

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
