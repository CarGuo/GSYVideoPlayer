#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
MODULE_DIR="$ROOT_DIR/gsyVideoPlayer-rtmp"
BUILD_DIR="$SCRIPT_DIR/build"
SOURCE_DIR="$BUILD_DIR/source"
UPSTREAM_REPOSITORY="https://github.com/ant-media/LibRtmp-Client-for-Android.git"
UPSTREAM_COMMIT="3af67842f517f9c6bbd22b961878624d0c44b713"
NDK_VERSION="22.1.7171670"
CMAKE_VERSION="3.22.1"
MIN_SDK="23"

ANDROID_SDK_DIR="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}"
NDK_DIR="$ANDROID_SDK_DIR/ndk/$NDK_VERSION"
CMAKE_BIN="$ANDROID_SDK_DIR/cmake/$CMAKE_VERSION/bin/cmake"
PATCH_FILE="$SCRIPT_DIR/patches/0001-native-c-correctness.patch"

case "$(uname -s)" in
    Darwin) NDK_HOST="darwin-x86_64" ;;
    Linux) NDK_HOST="linux-x86_64" ;;
    *) echo "Unsupported build host" >&2; exit 1 ;;
esac

LLVM_STRIP="$NDK_DIR/toolchains/llvm/prebuilt/$NDK_HOST/bin/llvm-strip"

for command_name in git; do
    command -v "$command_name" >/dev/null 2>&1 || {
        echo "Missing required command: $command_name" >&2
        exit 1
    }
done

test -x "$CMAKE_BIN" || {
    echo "Missing Android SDK CMake $CMAKE_VERSION at $CMAKE_BIN" >&2
    exit 1
}
test -f "$NDK_DIR/build/cmake/android.toolchain.cmake" || {
    echo "Missing Android NDK $NDK_VERSION at $NDK_DIR" >&2
    exit 1
}
test -x "$LLVM_STRIP" || {
    echo "Missing llvm-strip at $LLVM_STRIP" >&2
    exit 1
}

mkdir -p "$BUILD_DIR"

if [[ ! -d "$SOURCE_DIR/.git" ]]; then
    git clone "$UPSTREAM_REPOSITORY" "$SOURCE_DIR"
fi

SOURCE_REMOTE="$(git -C "$SOURCE_DIR" remote get-url origin)"
if [[ "$SOURCE_REMOTE" != "$UPSTREAM_REPOSITORY" ]]; then
    echo "Unexpected RTMP source origin: $SOURCE_REMOTE" >&2
    exit 1
fi

CURRENT_COMMIT="$(git -C "$SOURCE_DIR" rev-parse HEAD)"
# 2026-08-19: Normalize the script-owned source cache before every build.
# This prevents an unreviewed local C change from leaking into committed so files.
if git -C "$SOURCE_DIR" apply --reverse --check "$PATCH_FILE" >/dev/null 2>&1; then
    git -C "$SOURCE_DIR" apply --reverse "$PATCH_FILE"
fi
if [[ -n "$(git -C "$SOURCE_DIR" status --porcelain)" ]]; then
    echo "Pinned RTMP source cache contains unreviewed changes; remove $SOURCE_DIR and rebuild" >&2
    exit 1
fi
if [[ "$CURRENT_COMMIT" != "$UPSTREAM_COMMIT" ]]; then
    git -C "$SOURCE_DIR" checkout --detach "$UPSTREAM_COMMIT"
fi
git -C "$SOURCE_DIR" apply --check "$PATCH_FILE"
git -C "$SOURCE_DIR" apply "$PATCH_FILE"

# Fail when upstream starts depending on a fixed kernel page size. Numeric
# buffer sizes unrelated to mmap are intentionally not rejected here.
if grep -R -n -E '\bPAGE_SIZE\b|getpagesize\s*\(|sysconf\s*\([^)]*_SC_PAGESIZE|mmap\s*\(' \
    "$SOURCE_DIR/rtmp-client/src/main/cpp"; then
    echo "Page-size-sensitive native code requires manual review" >&2
    exit 1
fi

ABIS=(arm64-v8a x86_64 armeabi-v7a x86)
for abi in "${ABIS[@]}"; do
    # 2026-08-19: Include the pinned NDK in the cache path so a prior build
    # tree can never silently retain a different compiler after a switch.
    ABI_BUILD_DIR="$BUILD_DIR/native/$NDK_VERSION/$abi"
    "$CMAKE_BIN" -S "$SCRIPT_DIR" -B "$ABI_BUILD_DIR" \
        -DCMAKE_TOOLCHAIN_FILE="$NDK_DIR/build/cmake/android.toolchain.cmake" \
        -DRTMP_CLIENT_SOURCE_DIR="$SOURCE_DIR/rtmp-client" \
        -DANDROID_ABI="$abi" \
        -DANDROID_PLATFORM="android-$MIN_SDK" \
        -DANDROID_STL=c++_static \
        -DCMAKE_BUILD_TYPE=Release
    "$CMAKE_BIN" --build "$ABI_BUILD_DIR" --parallel
done

JAVA_OUTPUT_DIR="$MODULE_DIR/src/main/java/io/antmedia/rtmp_client"
JNI_OUTPUT_DIR="$MODULE_DIR/src/main/jniLibs"
LICENSE_OUTPUT_DIR="$MODULE_DIR/src/main/resources/META-INF"
mkdir -p "$JAVA_OUTPUT_DIR" "$JNI_OUTPUT_DIR" "$LICENSE_OUTPUT_DIR"

# These are generated from one pinned upstream revision so the committed
# module and its published sources/native AAR cannot drift independently.
cp "$SOURCE_DIR/rtmp-client/src/main/java/io/antmedia/rtmp_client/RtmpClient.java" "$JAVA_OUTPUT_DIR/"
cp "$SOURCE_DIR/rtmp-client/src/main/java/io/antmedia/rtmp_client/RTMPMuxer.java" "$JAVA_OUTPUT_DIR/"
cp "$SOURCE_DIR/LICENSE" "$LICENSE_OUTPUT_DIR/LICENSE-antmedia"

for abi in "${ABIS[@]}"; do
    mkdir -p "$JNI_OUTPUT_DIR/$abi"
    cp "$BUILD_DIR/native/$NDK_VERSION/$abi/librtmp-jni.so" "$JNI_OUTPUT_DIR/$abi/librtmp-jni.so"
    "$LLVM_STRIP" --strip-debug "$JNI_OUTPUT_DIR/$abi/librtmp-jni.so"
done

(cd "$ROOT_DIR" && ./gradlew :gsyVideoPlayer-rtmp:assembleRelease --no-daemon)
"$SCRIPT_DIR/verify-aar.sh"

echo "Updated and verified the gsyVideoPlayer-rtmp module"
