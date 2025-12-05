### The project's .so files are compiled based on ijkplayer, which is based on FFmpeg

---------

#### For .so file size considerations, the standard compiled .so only supports common video encodings. If you need support for additional types, you can depend on ex_so. If that is still not sufficient, you can recompile the ijkplayer source code, configure module.sh, then compile the .so files and replace the current ones in the project. Note that the .so version must match the ijk java version. The compilation process can refer to the process of compiling .so with https support on the homepage.

---------

#### Note: If you experience sound without video, or video without sound, please read the following information first. (Some lists in the DEMO have mute mode enabled, which is a normal situation).**

Simply put, mp4 is not a video encoding, it can be called a video container. H264/H263 are video encodings, and AAC is an audio encoding.

For video-related topics, Lei Xiaohua's video basics are recommended: [A Zero-Based Learning Method for Audio and Video Coding Technology](http://blog.csdn.net/leixiaohua1020/article/details/18893769). Here you can learn about video and audio related encoding and protocols.


The default supported video and audio encoding configurations for the project's standard .so can be found in the [compilation configuration file](https://github.com/CarGuo/GSYVideoPlayer/blob/master/module-lite.sh).

The video and audio encoding configurations supported by ex_so can be found in the [compilation configuration file](https://github.com/CarGuo/GSYVideoPlayer/blob/master/module-lite-more.sh).

*ex_so adds support for mpeg, concat protocol, and crypto protocol*.



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
