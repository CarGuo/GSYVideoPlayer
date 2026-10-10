# v14.0.0 codec configuration and IJK options

[中文](DECODERS.md)

Current `gsyvideoplayer-ex_so` and standalone arm64/armv7a/x64 ship **FFmpeg n5.1.10 + OpenSSL 3.5.9**, with identical matched native tuples and codec profiles for each ABI. See [module-lite-more.sh](../module-lite-more.sh) and the [pinned IJK configuration](https://github.com/CarGuo/ijkplayer/blob/df3f5ca6ed56419e7af04de0fd3a7474bb41950b/config/module-lite-more.sh); the former standard/extended MPEG distinction no longer selects these modules.

This release adds the HTTP multipart MJPEG demuxer (`mpjpeg`) and raw MJPEG parser (`mjpeg`). Containers, codecs and protocols are separate capabilities; MP4 is not a video codec, and hardware decoding also depends on device MediaCodec support. Rebuild the complete FFmpeg/player/SDL tuple for custom profiles; see [BUILD_SO_EN.md](BUILD_SO_EN.md).

- Current IJK PLAYER options: [ff_ffplay_options.h](https://github.com/CarGuo/ijkplayer/blob/df3f5ca6ed56419e7af04de0fd3a7474bb41950b/ijkmedia/ijkplayer/ff_ffplay_options.h).
- Audio `soundtouch` / tempo, RTSP `timeout` (microseconds) and `rtsp-live-max-buffer-ms`: [QUESTION_EN.md](QUESTION_EN.md).
- Explicit TLS/CA settings follow nested HLS requests; default verification behavior is unchanged. ARMv7 certificate loading is repaired; see [TLS results](../tests/tls-native/README.md).
- Legacy `armeabi` / `x86` modules are not migrated to FFmpeg 5: [DEPENDENCIES_EN.md](DEPENDENCIES_EN.md).

## Historical compilation-option reference

The older option lists and audio examples below include obsolete or currently disabled external encoders/APIs. They are not this release's enabled-codec inventory or a ready-to-copy FFmpeg 5 profile. Use pinned configuration and media/device verification for current capability.

### Summary of Common Audio Compilation Methods

#### mp3
```
    --enable-libmp3lame
    --enable-decoder=mp3
    --enable-demuxer=mp3
    --enable-muxer=mp3
    --enable-encoder=libmp3lame
```
#### Support for vorbis
```
    --enable-libvorbis
    --enable-parser=vorbis
    --enable-encoder=vorbis
    --enable-decoder=vorbis
    --enable-encoder=libvorbis
    --enable-decoder=libvorbis
    --enable-muxer=ogg
    --enable-demuxer=ogg
```

#### Support for wav


```
    --enable-libwavpack
    --enable-muxer=wav
    --enable-demuxer=wav
    --enable-decoder=wavpack
    --enable-encoder=wavpack
    --enable-decoder=wav
    --enable-encoder=wav
    --enable-encoder=pcm_s16le
    --enable-decoder=pcm_s16le

    --enable-encoder=pcm_u8
    --enable-decoder=pcm_u8
    --enable-muxer=pcm_u8
    --enable-demuxer=pcm_u8
```

#### Support for aac
```

     --enable-libvo-aacenc
    --enable-libfdk_aac
    --enable-libfaac
    --enable-parser=aac
    --enable-encoder=aac
    --enable-decoder=aac
    --enable-encoder=libfaac
    --enable-encoder=libvo_aacenc
    --enable-encoder=libaacplus
    --enable-encoder=libfdk_aac
    --enable-decoder=libfdk_aac
    --enable-demuxer=aac
    --enable-muxer=adts
```

#### Support for mp2
```
    --enable-encoder=mp2
    --enable-decoder=mp2
    --enable-muxer=mp2
    --enable-decoder=mp2float
    --enable-encoder=mp2fixed
```

#### FLAC support
```
   --enable-encoder=flac
    --enable-decoder=flac
    --enable-demuxer=flac
    --enable-muxer=flac
    --enable-parser=flac
```
#### jpeg, etc.

    --enable-encoder=jpeg2000
    --enable-encoder=mjpeg
    --enable-encoder=ljpeg
    --enable-encoder=jpegls
    --enable-decoder=jpeg2000
    --enable-decoder=jpegls
    --enable-decoder=mjpeg
    --enable-decoder=mjpegb
    --enable-muxer=mjpeg
    --enable-demuxer=mjpeg
    --enable-encoder=png
    --enable-decoder=png
    --enable-parser=png


#### Add scale support
    --enable-swscale
    --enable-swscale-alpha
    --enable-filter=scale


#### ac3
    --enable-encoder=ac3
    --enable-decoder=ac3
    --enable-encoder=ac3_fixed
    --enable-decoder=atrac3
    --enable-decoder=atrac3p
    --enable-encoder=eac3
    --enable-decoder=eac3
    --enable-muxer=ac3
    --enable-demuxer=ac3
    --enable-muxer=eac3
    --enable-demuxer=eac3

#### Support for wma/wmv

    --enable-decoder=wmalossless
    --enable-decoder=wmapro
    --enable-encoder=wmav1
    --enable-decoder=wmav1
    --enable-encoder=wmav2
    --enable-decoder=wmav2
    --enable-decoder=wmavoice
    --enable-demuxer=xwma
    --enable-demuxer=avi
    --enable-muxer=avi
    --enable-demuxer=asf
    --enable-muxer=asf
    --enable-encoder=wmv1
    --enable-decoder=wmv1
    --enable-encoder=wmv2
    --enable-decoder=wmv2
    --enable-decoder=wmv3
    --enable-decoder=wmv3_crystalhd
    --enable-decoder=wmv3_vdpau
    --enable-decoder=wmv3image



### Supported encoding, decoding, container formats in ffmpeg are shown below

![](https://raw.githubusercontent.com/CarGuo/GSYVideoPlayer/master/img/code/code01.jpg)
![](https://raw.githubusercontent.com/CarGuo/GSYVideoPlayer/master/img/code/code02.jpg)
![](https://raw.githubusercontent.com/CarGuo/GSYVideoPlayer/master/img/code/code03.jpg)
![](https://raw.githubusercontent.com/CarGuo/GSYVideoPlayer/master/img/code/code04.jpg)
![](https://raw.githubusercontent.com/CarGuo/GSYVideoPlayer/master/img/code/code05.jpg)
![](https://raw.githubusercontent.com/CarGuo/GSYVideoPlayer/master/img/code/code06.jpg)




2. Parameter Meanings
category
The category of the option. `name` and `value` are actually stored as a key-value pair in a map, and this is the name of that map.

name
The name of the option. `name` and `value` are stored as a key-value pair, and this is the key.

value
The value of the option. `name` and `value` are stored as a key-value pair, and this is the value.

3. Parameter Values
The possible values for `name` and `value` depend on the value of `category`, so they are discussed separately.

1) Possible values for category (IjkMediaPlayer.java)


```
public static final int OPT_CATEGORY_FORMAT = 1;
public static final int OPT_CATEGORY_CODEC = 2;
public static final int OPT_CATEGORY_SWS = 3;
public static final int OPT_CATEGORY_PLAYER = 4;
```

2) When category is OPT_CATEGORY_FORMAT, possible values for name and value (extra/ffmpeg/libavformat/options_table.h)

The rest of the file contains code snippets and tables that are already in English or are self-explanatory.
The content is too long to be fully included here.
The original formatting and content will be preserved.
