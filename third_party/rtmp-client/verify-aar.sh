#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
DEFAULT_AAR="$ROOT_DIR/gsyVideoPlayer-rtmp/build/outputs/aar/gsyVideoPlayer-rtmp-release.aar"
AAR_PATH="${1:-$DEFAULT_AAR}"
ANDROID_SDK_DIR="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}"
NDK_VERSION="22.1.7171670"

case "$(uname -s)" in
    Darwin) NDK_HOST="darwin-x86_64" ;;
    Linux) NDK_HOST="linux-x86_64" ;;
    *) echo "Unsupported host for ELF verification" >&2; exit 1 ;;
esac

READELF="$ANDROID_SDK_DIR/ndk/$NDK_VERSION/toolchains/llvm/prebuilt/$NDK_HOST/bin/llvm-readelf"
test -f "$AAR_PATH" || { echo "AAR not found: $AAR_PATH" >&2; exit 1; }
test -x "$READELF" || { echo "llvm-readelf not found: $READELF" >&2; exit 1; }

VERIFY_DIR="$(mktemp -d "${TMPDIR:-/tmp}/gsy-rtmp-verify.XXXXXX")"
trap 'rm -rf "$VERIFY_DIR"' EXIT
unzip -q "$AAR_PATH" -d "$VERIFY_DIR"

for abi in arm64-v8a x86_64 armeabi-v7a x86; do
    SO_PATH="$VERIFY_DIR/jni/$abi/librtmp-jni.so"
    test -f "$SO_PATH" || { echo "Missing $abi/librtmp-jni.so" >&2; exit 1; }

    LOAD_ALIGNS="$("$READELF" -lW "$SO_PATH" | awk '$1 == "LOAD" { print $NF }')"
    test -n "$LOAD_ALIGNS" || { echo "No PT_LOAD headers for $abi" >&2; exit 1; }
    if echo "$LOAD_ALIGNS" | grep -v '^0x4000$' >/dev/null; then
        echo "$abi has a non-16 KB PT_LOAD alignment:" >&2
        echo "$LOAD_ALIGNS" >&2
        exit 1
    fi

    RELRO_LINE="$("$READELF" -lW "$SO_PATH" | awk '$1 == "GNU_RELRO" { print; exit }')"
    test -n "$RELRO_LINE" || {
        echo "$abi is missing GNU_RELRO" >&2
        exit 1
    }
    RELRO_VADDR="$(echo "$RELRO_LINE" | awk '{ print $3 }')"
    RELRO_MEMSZ="$(echo "$RELRO_LINE" | awk '{ print $6 }')"
    RELRO_END=$((RELRO_VADDR + RELRO_MEMSZ))
    if (( RELRO_END % 16384 != 0 )); then
        printf '%s GNU_RELRO end is not 16 KB aligned: 0x%x\n' "$abi" "$RELRO_END" >&2
        exit 1
    fi
    "$READELF" -dW "$SO_PATH" | grep 'BIND_NOW' >/dev/null || {
        echo "$abi is missing BIND_NOW" >&2
        exit 1
    }
    "$READELF" -sW "$SO_PATH" | grep '__stack_chk_fail' >/dev/null || {
        echo "$abi is missing stack canary instrumentation" >&2
        exit 1
    }
    "$READELF" -n "$SO_PATH" | grep '72 32 32 62' >/dev/null || {
        echo "$abi was not built with the pinned NDK r22b toolchain" >&2
        exit 1
    }

    echo "PASS $abi: PT_LOAD=0x4000, GNU_RELRO end=16 KB, BIND_NOW, stack canary, NDK r22b"
done

for class_path in \
    io/antmedia/rtmp_client/RtmpClient.class \
    io/antmedia/rtmp_client/RTMPMuxer.class; do
    unzip -p "$AAR_PATH" classes.jar > "$VERIFY_DIR/classes.jar"
    unzip -l "$VERIFY_DIR/classes.jar" "$class_path" | grep "$class_path" >/dev/null || {
        echo "Missing public API class: $class_path" >&2
        exit 1
    }
done

unzip -l "$VERIFY_DIR/classes.jar" META-INF/LICENSE-antmedia | grep 'META-INF/LICENSE-antmedia' >/dev/null || {
    echo "Missing pinned upstream license: META-INF/LICENSE-antmedia" >&2
    exit 1
}

echo "PASS module AAR: four ABIs, Media3 RTMP API classes and upstream license are packaged"
