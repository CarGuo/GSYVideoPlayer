cmake_minimum_required(VERSION 3.22)

project(gsy_rtmp_client LANGUAGES C)

if(NOT DEFINED RTMP_CLIENT_SOURCE_DIR)
    message(FATAL_ERROR "RTMP_CLIENT_SOURCE_DIR must point to the pinned upstream rtmp-client source")
endif()

set(RTMP_CPP_DIR "${RTMP_CLIENT_SOURCE_DIR}/src/main/cpp")

add_library(rtmp-jni SHARED
    "${RTMP_CPP_DIR}/rtmpmuxer.c"
    "${RTMP_CPP_DIR}/librtmp-jni.c"
    "${RTMP_CPP_DIR}/librtmp/amf.c"
    "${RTMP_CPP_DIR}/librtmp/hashswf.c"
    "${RTMP_CPP_DIR}/librtmp/log.c"
    "${RTMP_CPP_DIR}/librtmp/parseurl.c"
    "${RTMP_CPP_DIR}/librtmp/rtmp.c"
    "${RTMP_CPP_DIR}/flvmuxer/xiecc_rtmp.c"
)

target_include_directories(rtmp-jni PRIVATE
    "${RTMP_CPP_DIR}"
    "${RTMP_CPP_DIR}/librtmp"
    "${RTMP_CPP_DIR}/flvmuxer"
)

target_compile_features(rtmp-jni PRIVATE c_std_99)
target_compile_definitions(rtmp-jni PRIVATE NO_CRYPTO _FORTIFY_SOURCE=2)
target_compile_options(rtmp-jni PRIVATE
    -fstack-protector-strong
    "-ffile-prefix-map=${RTMP_CLIENT_SOURCE_DIR}=."
    "-fdebug-prefix-map=${RTMP_CLIENT_SOURCE_DIR}=."
)

# 2026-08-19: NDK r22b does not enable flexible page sizes by default. Keep
# both Android-documented flags explicit: max-page-size aligns PT_LOAD, while
# common-page-size makes the PT_GNU_RELRO end 16 KB aligned as well.
target_link_options(rtmp-jni PRIVATE
    "-Wl,-z,max-page-size=16384"
    "-Wl,-z,common-page-size=16384"
    "-Wl,-z,relro"
    "-Wl,-z,now"
)

find_library(ANDROID_LOG_LIBRARY log REQUIRED)
target_link_libraries(rtmp-jni PRIVATE "${ANDROID_LOG_LIBRARY}")
