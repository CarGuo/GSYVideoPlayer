
### 项目是基于ijkplayer编译的so，ijkplayer基于FFPMEG

---------

#### 出于so大小考虑，普通编译so只支持了常用的视频编码，如果需要支持额外类型，可依赖ex_so，如果依旧不满足，可重新编译ijkplayer源码，配置module.sh然后编译so，替换现在项目中的so，注意so的版本要和ijk的java版本一致。编译流程可参考首页编译https的so流程。

---------

#### 说明：如果出现有声音没画面，或者有画面没声音的异常情况，请先了解以下内容。（DEMO中有些列表开启了静音模式，属于正常情况）**

简单来说，mp4并不是视频编码，可以称为视频容器，而H264/H263等这样才是视频编码，AAC为音频编码等。

对于视频相关的，推荐雷宵骅的视频基础：[视音频编解码技术零基础学习方法](http://blog.csdn.net/leixiaohua1020/article/details/18893769)，这里你可以了解到视频和音频相关编码和协议的东西。


项目普通so默认支持的视频编码和音频编码配置可查看[编译配置文件](https://github.com/CarGuo/GSYVideoPlayer/blob/master/module-lite.sh)。

ex_so支持的视频编码和音频编码配置可查看[编译配置文件](https://github.com/CarGuo/GSYVideoPlayer/blob/master/module-lite-more.sh)。

*ex_so多支持了mepg、concat协议，crypto协议*。



### 常用音频编译方式小结

#### mp3
```
    --enable-libmp3lame 
    --enable-decoder=mp3 
    --enable-demuxer=mp3 
    --enable-muxer=mp3
    --enable-encoder=libmp3lame
```  
#### 支持vorbis
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

#### 支持wav


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

#### 支持aac
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

#### 支持mp2
```
    --enable-encoder=mp2 
    --enable-decoder=mp2 
    --enable-muxer=mp2 
    --enable-decoder=mp2float 
    --enable-encoder=mp2fixed 
```

#### flac 支持
```
   --enable-encoder=flac
    --enable-decoder=flac
    --enable-demuxer=flac
    --enable-muxer=flac
    --enable-parser=flac
```
#### jpeg等

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
    
    
#### 添加scale的支持    
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

#### 支持wma/wmv

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



### ffmpeg 支持的编码，解码，容器等格式如下图

![](https://raw.githubusercontent.com/CarGuo/GSYVideoPlayer/master/img/code/code01.jpg)
![](https://raw.githubusercontent.com/CarGuo/GSYVideoPlayer/master/img/code/code02.jpg)
![](https://raw.githubusercontent.com/CarGuo/GSYVideoPlayer/master/img/code/code03.jpg)
![](https://raw.githubusercontent.com/CarGuo/GSYVideoPlayer/master/img/code/code04.jpg)
![](https://raw.githubusercontent.com/CarGuo/GSYVideoPlayer/master/img/code/code05.jpg)
![](https://raw.githubusercontent.com/CarGuo/GSYVideoPlayer/master/img/code/code06.jpg)




2. 参数含义
category
选项类别，name 和 value 实际是一种 KV 对的 MAP 方式存储，这里是对应 MAP 的名称。

name
选项名称，name 和 value 实际是一种 KV 对的 MAP 方式存储，这里就是 K。

value
选项值，name 和 value 实际是一种 KV 对的 MAP 方式存储，这里就是 V。

3. 参数取值
name 和 value 的可取值范围同 category 的值有关，故分类讨论。

1) category 可取参数(IjkMediaPlayer.java)


```
public static final int OPT_CATEGORY_FORMAT = 1;
public static final int OPT_CATEGORY_CODEC = 2;
public static final int OPT_CATEGORY_SWS = 3;
public static final int OPT_CATEGORY_PLAYER = 4;
```

2) category 值为 OPT_CATEGORY_FORMAT 时， name 和 value 取值(extra/ffmpeg/libavformat/options_table.h)

```
static const AVOption avformat_options[] = {
{"avioflags", NULL, OFFSET(avio_flags), AV_OPT_TYPE_FLAGS, {.i64 = DEFAULT }, INT_MIN, INT_MAX, D|E, "avioflags"},
{"direct", "reduce buffering", 0, AV_OPT_TYPE_CONST, {.i64 = AVIO_FLAG_DIRECT }, INT_MIN, INT_MAX, D|E, "avioflags"},
{"probesize", "set probing size", OFFSET(probesize), AV_OPT_TYPE_INT64, {.i64 = 5000000 }, 32, INT64_MAX, D},
{"formatprobesize", "number of bytes to probe file format", OFFSET(format_probesize), AV_OPT_TYPE_INT, {.i64 = PROBE_BUF_MAX}, 0, INT_MAX-1, D},
{"packetsize", "set packet size", OFFSET(packet_size), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, 0, INT_MAX, E},
{"fflags", NULL, OFFSET(flags), AV_OPT_TYPE_FLAGS, {.i64 = AVFMT_FLAG_FLUSH_PACKETS | AVFMT_FLAG_AUTO_BSF }, INT_MIN, INT_MAX, D|E, "fflags"},
{"flush_packets", "reduce the latency by flushing out packets immediately", 0, AV_OPT_TYPE_CONST, {.i64 = AVFMT_FLAG_FLUSH_PACKETS }, INT_MIN, INT_MAX, E, "fflags"},
{"ignidx", "ignore index", 0, AV_OPT_TYPE_CONST, {.i64 = AVFMT_FLAG_IGNIDX }, INT_MIN, INT_MAX, D, "fflags"},
{"genpts", "generate pts", 0, AV_OPT_TYPE_CONST, {.i64 = AVFMT_FLAG_GENPTS }, INT_MIN, INT_MAX, D, "fflags"},
{"nofillin", "do not fill in missing values that can be exactly calculated", 0, AV_OPT_TYPE_CONST, {.i64 = AVFMT_FLAG_NOFILLIN }, INT_MIN, INT_MAX, D, "fflags"},
{"noparse", "disable AVParsers, this needs nofillin too", 0, AV_OPT_TYPE_CONST, {.i64 = AVFMT_FLAG_NOPARSE }, INT_MIN, INT_MAX, D, "fflags"},
{"igndts", "ignore dts", 0, AV_OPT_TYPE_CONST, {.i64 = AVFMT_FLAG_IGNDTS }, INT_MIN, INT_MAX, D, "fflags"},
{"discardcorrupt", "discard corrupted frames", 0, AV_OPT_TYPE_CONST, {.i64 = AVFMT_FLAG_DISCARD_CORRUPT }, INT_MIN, INT_MAX, D, "fflags"},
{"sortdts", "try to interleave outputted packets by dts", 0, AV_OPT_TYPE_CONST, {.i64 = AVFMT_FLAG_SORT_DTS }, INT_MIN, INT_MAX, D, "fflags"},
{"keepside", "don't merge side data", 0, AV_OPT_TYPE_CONST, {.i64 = AVFMT_FLAG_KEEP_SIDE_DATA }, INT_MIN, INT_MAX, D, "fflags"},
{"fastseek", "fast but inaccurate seeks", 0, AV_OPT_TYPE_CONST, {.i64 = AVFMT_FLAG_FAST_SEEK }, INT_MIN, INT_MAX, D, "fflags"},
{"latm", "enable RTP MP4A-LATM payload", 0, AV_OPT_TYPE_CONST, {.i64 = AVFMT_FLAG_MP4A_LATM }, INT_MIN, INT_MAX, E, "fflags"},
{"nobuffer", "reduce the latency introduced by optional buffering", 0, AV_OPT_TYPE_CONST, {.i64 = AVFMT_FLAG_NOBUFFER }, 0, INT_MAX, D, "fflags"},
{"seek2any", "allow seeking to non-keyframes on demuxer level when supported", OFFSET(seek2any), AV_OPT_TYPE_BOOL, {.i64 = 0 }, 0, 1, D},
{"bitexact", "do not write random/volatile data", 0, AV_OPT_TYPE_CONST, { .i64 = AVFMT_FLAG_BITEXACT }, 0, 0, E, "fflags" },
{"shortest", "stop muxing with the shortest stream", 0, AV_OPT_TYPE_CONST, { .i64 = AVFMT_FLAG_SHORTEST }, 0, 0, E, "fflags" },
{"autobsf", "add needed bsfs automatically (delays header until each stream's first packet is written)", 0, AV_OPT_TYPE_CONST, { .i64 = AVFMT_FLAG_AUTO_BSF }, 0, 0, E, "fflags" },
{"analyzeduration", "specify how many microseconds are analyzed to probe the input", OFFSET(max_analyze_duration), AV_OPT_TYPE_INT64, {.i64 = 0 }, 0, INT64_MAX, D},
{"cryptokey", "decryption key", OFFSET(key), AV_OPT_TYPE_BINARY, {.dbl = 0}, 0, 0, D},
{"indexmem", "max memory used for timestamp index (per stream)", OFFSET(max_index_size), AV_OPT_TYPE_INT, {.i64 = 1<<20 }, 0, INT_MAX, D},
{"rtbufsize", "max memory used for buffering real-time frames", OFFSET(max_picture_buffer), AV_OPT_TYPE_INT, {.i64 = 3041280 }, 0, INT_MAX, D}, /* defaults to 1s of 15fps 352x288 YUYV422 video */
{"fdebug", "print specific debug info", OFFSET(debug), AV_OPT_TYPE_FLAGS, {.i64 = DEFAULT }, 0, INT_MAX, E|D, "fdebug"},
{"ts", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_FDEBUG_TS }, INT_MIN, INT_MAX, E|D, "fdebug"},
{"max_delay", "maximum muxing or demuxing delay in microseconds", OFFSET(max_delay), AV_OPT_TYPE_INT, {.i64 = -1 }, -1, INT_MAX, E|D},
{"start_time_realtime", "wall-clock time when stream begins (PTS==0)", OFFSET(start_time_realtime), AV_OPT_TYPE_INT64, {.i64 = AV_NOPTS_VALUE}, INT64_MIN, INT64_MAX, E},
{"fpsprobesize", "number of frames used to probe fps", OFFSET(fps_probe_size), AV_OPT_TYPE_INT, {.i64 = -1}, -1, INT_MAX-1, D},
{"audio_preload", "microseconds by which audio packets should be interleaved earlier", OFFSET(audio_preload), AV_OPT_TYPE_INT, {.i64 = 0}, 0, INT_MAX-1, E},
{"chunk_duration", "microseconds for each chunk", OFFSET(max_chunk_duration), AV_OPT_TYPE_INT, {.i64 = 0}, 0, INT_MAX-1, E},
{"chunk_size", "size in bytes for each chunk", OFFSET(max_chunk_size), AV_OPT_TYPE_INT, {.i64 = 0}, 0, INT_MAX-1, E},
/* this is a crutch for avconv, since it cannot deal with identically named options in different contexts.
 * to be removed when avconv is fixed */
{"f_err_detect", "set error detection flags (deprecated; use err_detect, save via avconv)", OFFSET(error_recognition), AV_OPT_TYPE_FLAGS, {.i64 = AV_EF_CRCCHECK }, INT_MIN, INT_MAX, D, "err_detect"},
{"err_detect", "set error detection flags", OFFSET(error_recognition), AV_OPT_TYPE_FLAGS, {.i64 = AV_EF_CRCCHECK }, INT_MIN, INT_MAX, D, "err_detect"},
{"crccheck", "verify embedded CRCs", 0, AV_OPT_TYPE_CONST, {.i64 = AV_EF_CRCCHECK }, INT_MIN, INT_MAX, D, "err_detect"},
{"bitstream", "detect bitstream specification deviations", 0, AV_OPT_TYPE_CONST, {.i64 = AV_EF_BITSTREAM }, INT_MIN, INT_MAX, D, "err_detect"},
{"buffer", "detect improper bitstream length", 0, AV_OPT_TYPE_CONST, {.i64 = AV_EF_BUFFER }, INT_MIN, INT_MAX, D, "err_detect"},
{"explode", "abort decoding on minor error detection", 0, AV_OPT_TYPE_CONST, {.i64 = AV_EF_EXPLODE }, INT_MIN, INT_MAX, D, "err_detect"},
{"ignore_err", "ignore errors", 0, AV_OPT_TYPE_CONST, {.i64 = AV_EF_IGNORE_ERR }, INT_MIN, INT_MAX, D, "err_detect"},
{"careful",    "consider things that violate the spec, are fast to check and have not been seen in the wild as errors", 0, AV_OPT_TYPE_CONST, {.i64 = AV_EF_CAREFUL }, INT_MIN, INT_MAX, D, "err_detect"},
{"compliant",  "consider all spec non compliancies as errors", 0, AV_OPT_TYPE_CONST, {.i64 = AV_EF_COMPLIANT }, INT_MIN, INT_MAX, D, "err_detect"},
{"aggressive", "consider things that a sane encoder shouldn't do as an error", 0, AV_OPT_TYPE_CONST, {.i64 = AV_EF_AGGRESSIVE }, INT_MIN, INT_MAX, D, "err_detect"},
{"use_wallclock_as_timestamps", "use wallclock as timestamps", OFFSET(use_wallclock_as_timestamps), AV_OPT_TYPE_BOOL, {.i64 = 0}, 0, 1, D},
{"skip_initial_bytes", "set number of bytes to skip before reading header and frames", OFFSET(skip_initial_bytes), AV_OPT_TYPE_INT64, {.i64 = 0}, 0, INT64_MAX-1, D},
{"correct_ts_overflow", "correct single timestamp overflows", OFFSET(correct_ts_overflow), AV_OPT_TYPE_BOOL, {.i64 = 1}, 0, 1, D},
{"flush_packets", "enable flushing of the I/O context after each packet", OFFSET(flush_packets), AV_OPT_TYPE_BOOL, {.i64 = 1}, 0, 1, E},
{"metadata_header_padding", "set number of bytes to be written as padding in a metadata header", OFFSET(metadata_header_padding), AV_OPT_TYPE_INT, {.i64 = -1}, -1, INT_MAX, E},
{"output_ts_offset", "set output timestamp offset", OFFSET(output_ts_offset), AV_OPT_TYPE_DURATION, {.i64 = 0}, -INT64_MAX, INT64_MAX, E},
{"max_interleave_delta", "maximum buffering duration for interleaving", OFFSET(max_interleave_delta), AV_OPT_TYPE_INT64, { .i64 = 10000000 }, 0, INT64_MAX, E },
{"f_strict", "how strictly to follow the standards (deprecated; use strict, save via avconv)", OFFSET(strict_std_compliance), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, D|E, "strict"},
{"strict", "how strictly to follow the standards", OFFSET(strict_std_compliance), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, D|E, "strict"},
{"very", "strictly conform to a older more strict version of the spec or reference software", 0, AV_OPT_TYPE_CONST, {.i64 = FF_COMPLIANCE_VERY_STRICT }, INT_MIN, INT_MAX, D|E, "strict"},
{"strict", "strictly conform to all the things in the spec no matter what the consequences", 0, AV_OPT_TYPE_CONST, {.i64 = FF_COMPLIANCE_STRICT }, INT_MIN, INT_MAX, D|E, "strict"},
{"normal", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_COMPLIANCE_NORMAL }, INT_MIN, INT_MAX, D|E, "strict"},
{"unofficial", "allow unofficial extensions", 0, AV_OPT_TYPE_CONST, {.i64 = FF_COMPLIANCE_UNOFFICIAL }, INT_MIN, INT_MAX, D|E, "strict"},
{"experimental", "allow non-standardized experimental variants", 0, AV_OPT_TYPE_CONST, {.i64 = FF_COMPLIANCE_EXPERIMENTAL }, INT_MIN, INT_MAX, D|E, "strict"},
{"max_ts_probe", "maximum number of packets to read while waiting for the first timestamp", OFFSET(max_ts_probe), AV_OPT_TYPE_INT, { .i64 = 50 }, 0, INT_MAX, D },
{"avoid_negative_ts", "shift timestamps so they start at 0", OFFSET(avoid_negative_ts), AV_OPT_TYPE_INT, {.i64 = -1}, -1, 2, E, "avoid_negative_ts"},
{"auto",              "enabled when required by target format",    0, AV_OPT_TYPE_CONST, {.i64 = AVFMT_AVOID_NEG_TS_AUTO },              INT_MIN, INT_MAX, E, "avoid_negative_ts"},
{"disabled",          "do not change timestamps",                  0, AV_OPT_TYPE_CONST, {.i64 = 0 },                                    INT_MIN, INT_MAX, E, "avoid_negative_ts"},
{"make_non_negative", "shift timestamps so they are non negative", 0, AV_OPT_TYPE_CONST, {.i64 = AVFMT_AVOID_NEG_TS_MAKE_NON_NEGATIVE }, INT_MIN, INT_MAX, E, "avoid_negative_ts"},
{"make_zero",         "shift timestamps so they start at 0",       0, AV_OPT_TYPE_CONST, {.i64 = AVFMT_AVOID_NEG_TS_MAKE_ZERO },         INT_MIN, INT_MAX, E, "avoid_negative_ts"},
{"dump_separator", "set information dump field separator", OFFSET(dump_separator), AV_OPT_TYPE_STRING, {.str = ", "}, CHAR_MIN, CHAR_MAX, D|E},
{"codec_whitelist", "List of decoders that are allowed to be used", OFFSET(codec_whitelist), AV_OPT_TYPE_STRING, { .str = NULL },  CHAR_MIN, CHAR_MAX, D },
{"format_whitelist", "List of demuxers that are allowed to be used", OFFSET(format_whitelist), AV_OPT_TYPE_STRING, { .str = NULL },  CHAR_MIN, CHAR_MAX, D },
{"protocol_whitelist", "List of protocols that are allowed to be used", OFFSET(protocol_whitelist), AV_OPT_TYPE_STRING, { .str = NULL },  CHAR_MIN, CHAR_MAX, D },
{"protocol_blacklist", "List of protocols that are not allowed to be used", OFFSET(protocol_blacklist), AV_OPT_TYPE_STRING, { .str = NULL },  CHAR_MIN, CHAR_MAX, D },
{"max_streams", "maximum number of streams", OFFSET(max_streams), AV_OPT_TYPE_INT, { .i64 = 1000 }, 0, INT_MAX, D },
{NULL},
};
```
其中每一行含义是
{name, help, offset, type, default_val, min, max, flags, unit}
而我们只需关注加粗字体的内容即可。

此间 name 应之前 Java 方法 setOption(int category, String name, long value) 处 name。min 与 max 应 value 的取值范围。

Java 方法只有两种，区别仅于 value 的类型。因 C 中所有非 String 类型被统一封装为 long 所致。

3) category 值为 OPT_CATEGORY_CODEC 时， name 和 value 取值(extra/ffmpeg/libavcodec/options_table.h)

```
static const AVOption avcodec_options[] = {
{"b", "set bitrate (in bits/s)", OFFSET(bit_rate), AV_OPT_TYPE_INT64, {.i64 = AV_CODEC_DEFAULT_BITRATE }, 0, INT64_MAX, A|V|E},
{"ab", "set bitrate (in bits/s)", OFFSET(bit_rate), AV_OPT_TYPE_INT64, {.i64 = 128*1000 }, 0, INT_MAX, A|E},
{"bt", "Set video bitrate tolerance (in bits/s). In 1-pass mode, bitrate tolerance specifies how far "
       "ratecontrol is willing to deviate from the target average bitrate value. This is not related "
       "to minimum/maximum bitrate. Lowering tolerance too much has an adverse effect on quality.",
       OFFSET(bit_rate_tolerance), AV_OPT_TYPE_INT, {.i64 = AV_CODEC_DEFAULT_BITRATE*20 }, 1, INT_MAX, V|E},
{"flags", NULL, OFFSET(flags), AV_OPT_TYPE_FLAGS, {.i64 = DEFAULT }, 0, UINT_MAX, V|A|S|E|D, "flags"},
{"unaligned", "allow decoders to produce unaligned output", 0, AV_OPT_TYPE_CONST, { .i64 = AV_CODEC_FLAG_UNALIGNED }, INT_MIN, INT_MAX, V | D, "flags" },
{"mv4", "use four motion vectors per macroblock (MPEG-4)", 0, AV_OPT_TYPE_CONST, {.i64 = AV_CODEC_FLAG_4MV }, INT_MIN, INT_MAX, V|E, "flags"},
{"qpel", "use 1/4-pel motion compensation", 0, AV_OPT_TYPE_CONST, {.i64 = AV_CODEC_FLAG_QPEL }, INT_MIN, INT_MAX, V|E, "flags"},
{"loop", "use loop filter", 0, AV_OPT_TYPE_CONST, {.i64 = AV_CODEC_FLAG_LOOP_FILTER }, INT_MIN, INT_MAX, V|E, "flags"},
{"qscale", "use fixed qscale", 0, AV_OPT_TYPE_CONST, {.i64 = AV_CODEC_FLAG_QSCALE }, INT_MIN, INT_MAX, 0, "flags"},
#if FF_API_GMC
{"gmc", "use gmc", 0, AV_OPT_TYPE_CONST, {.i64 = CODEC_FLAG_GMC }, INT_MIN, INT_MAX, V|E, "flags"},
#endif
#if FF_API_MV0
{"mv0", "always try a mb with mv=<0,0>", 0, AV_OPT_TYPE_CONST, {.i64 = CODEC_FLAG_MV0 }, INT_MIN, INT_MAX, V|E, "flags"},
#endif
#if FF_API_INPUT_PRESERVED
{"input_preserved", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = CODEC_FLAG_INPUT_PRESERVED }, INT_MIN, INT_MAX, 0, "flags"},
#endif
{"pass1", "use internal 2-pass ratecontrol in first  pass mode", 0, AV_OPT_TYPE_CONST, {.i64 = AV_CODEC_FLAG_PASS1 }, INT_MIN, INT_MAX, 0, "flags"},
{"pass2", "use internal 2-pass ratecontrol in second pass mode", 0, AV_OPT_TYPE_CONST, {.i64 = AV_CODEC_FLAG_PASS2 }, INT_MIN, INT_MAX, 0, "flags"},
{"gray", "only decode/encode grayscale", 0, AV_OPT_TYPE_CONST, {.i64 = AV_CODEC_FLAG_GRAY }, INT_MIN, INT_MAX, V|E|D, "flags"},
#if FF_API_EMU_EDGE
{"emu_edge", "do not draw edges", 0, AV_OPT_TYPE_CONST, {.i64 = CODEC_FLAG_EMU_EDGE }, INT_MIN, INT_MAX, 0, "flags"},
#endif
{"psnr", "error[?] variables will be set during encoding", 0, AV_OPT_TYPE_CONST, {.i64 = AV_CODEC_FLAG_PSNR }, INT_MIN, INT_MAX, V|E, "flags"},
{"truncated", "Input bitstream might be randomly truncated", 0, AV_OPT_TYPE_CONST, {.i64 = AV_CODEC_FLAG_TRUNCATED }, INT_MIN, INT_MAX, V|D, "flags"},
#if FF_API_NORMALIZE_AQP
{"naq", "normalize adaptive quantization", 0, AV_OPT_TYPE_CONST, {.i64 = CODEC_FLAG_NORMALIZE_AQP }, INT_MIN, INT_MAX, V|E, "flags"},
#endif
{"ildct", "use interlaced DCT", 0, AV_OPT_TYPE_CONST, {.i64 = AV_CODEC_FLAG_INTERLACED_DCT }, INT_MIN, INT_MAX, V|E, "flags"},
{"low_delay", "force low delay", 0, AV_OPT_TYPE_CONST, {.i64 = AV_CODEC_FLAG_LOW_DELAY }, INT_MIN, INT_MAX, V|D|E, "flags"},
{"global_header", "place global headers in extradata instead of every keyframe", 0, AV_OPT_TYPE_CONST, {.i64 = AV_CODEC_FLAG_GLOBAL_HEADER }, INT_MIN, INT_MAX, V|A|E, "flags"},
{"bitexact", "use only bitexact functions (except (I)DCT)", 0, AV_OPT_TYPE_CONST, {.i64 = AV_CODEC_FLAG_BITEXACT }, INT_MIN, INT_MAX, A|V|S|D|E, "flags"},
{"aic", "H.263 advanced intra coding / MPEG-4 AC prediction", 0, AV_OPT_TYPE_CONST, {.i64 = AV_CODEC_FLAG_AC_PRED }, INT_MIN, INT_MAX, V|E, "flags"},
{"ilme", "interlaced motion estimation", 0, AV_OPT_TYPE_CONST, {.i64 = AV_CODEC_FLAG_INTERLACED_ME }, INT_MIN, INT_MAX, V|E, "flags"},
{"cgop", "closed GOP", 0, AV_OPT_TYPE_CONST, {.i64 = AV_CODEC_FLAG_CLOSED_GOP }, INT_MIN, INT_MAX, V|E, "flags"},
{"output_corrupt", "Output even potentially corrupted frames", 0, AV_OPT_TYPE_CONST, {.i64 = AV_CODEC_FLAG_OUTPUT_CORRUPT }, INT_MIN, INT_MAX, V|D, "flags"},
{"fast", "allow non-spec-compliant speedup tricks", 0, AV_OPT_TYPE_CONST, {.i64 = AV_CODEC_FLAG2_FAST }, INT_MIN, INT_MAX, V|E, "flags2"},
{"noout", "skip bitstream encoding", 0, AV_OPT_TYPE_CONST, {.i64 = AV_CODEC_FLAG2_NO_OUTPUT }, INT_MIN, INT_MAX, V|E, "flags2"},
{"ignorecrop", "ignore cropping information from sps", 0, AV_OPT_TYPE_CONST, {.i64 = AV_CODEC_FLAG2_IGNORE_CROP }, INT_MIN, INT_MAX, V|D, "flags2"},
{"local_header", "place global headers at every keyframe instead of in extradata", 0, AV_OPT_TYPE_CONST, {.i64 = AV_CODEC_FLAG2_LOCAL_HEADER }, INT_MIN, INT_MAX, V|E, "flags2"},
{"chunks", "Frame data might be split into multiple chunks", 0, AV_OPT_TYPE_CONST, {.i64 = AV_CODEC_FLAG2_CHUNKS }, INT_MIN, INT_MAX, V|D, "flags2"},
{"showall", "Show all frames before the first keyframe", 0, AV_OPT_TYPE_CONST, {.i64 = AV_CODEC_FLAG2_SHOW_ALL }, INT_MIN, INT_MAX, V|D, "flags2"},
{"export_mvs", "export motion vectors through frame side data", 0, AV_OPT_TYPE_CONST, {.i64 = AV_CODEC_FLAG2_EXPORT_MVS}, INT_MIN, INT_MAX, V|D, "flags2"},
{"skip_manual", "do not skip samples and export skip information as frame side data", 0, AV_OPT_TYPE_CONST, {.i64 = AV_CODEC_FLAG2_SKIP_MANUAL}, INT_MIN, INT_MAX, V|D, "flags2"},
{"ass_ro_flush_noop", "do not reset ASS ReadOrder field on flush", 0, AV_OPT_TYPE_CONST, {.i64 = AV_CODEC_FLAG2_RO_FLUSH_NOOP}, INT_MIN, INT_MAX, S|D, "flags2"},
#if FF_API_MOTION_EST
{"me_method", "set motion estimation method", OFFSET(me_method), AV_OPT_TYPE_INT, {.i64 = ME_EPZS }, INT_MIN, INT_MAX, V|E, "me_method"},
{"zero", "zero motion estimation (fastest)", 0, AV_OPT_TYPE_CONST, {.i64 = ME_ZERO }, INT_MIN, INT_MAX, V|E, "me_method" },
{"full", "full motion estimation (slowest)", 0, AV_OPT_TYPE_CONST, {.i64 = ME_FULL }, INT_MIN, INT_MAX, V|E, "me_method" },
{"epzs", "EPZS motion estimation", 0, AV_OPT_TYPE_CONST, {.i64 = ME_EPZS }, INT_MIN, INT_MAX, V|E, "me_method" },
{"esa", "esa motion estimation (alias for full)", 0, AV_OPT_TYPE_CONST, {.i64 = ME_FULL }, INT_MIN, INT_MAX, V|E, "me_method" },
{"tesa", "tesa motion estimation", 0, AV_OPT_TYPE_CONST, {.i64 = ME_TESA }, INT_MIN, INT_MAX, V|E, "me_method" },
{"dia", "diamond motion estimation (alias for EPZS)", 0, AV_OPT_TYPE_CONST, {.i64 = ME_EPZS }, INT_MIN, INT_MAX, V|E, "me_method" },
{"log", "log motion estimation", 0, AV_OPT_TYPE_CONST, {.i64 = ME_LOG }, INT_MIN, INT_MAX, V|E, "me_method" },
{"phods", "phods motion estimation", 0, AV_OPT_TYPE_CONST, {.i64 = ME_PHODS }, INT_MIN, INT_MAX, V|E, "me_method" },
{"x1", "X1 motion estimation", 0, AV_OPT_TYPE_CONST, {.i64 = ME_X1 }, INT_MIN, INT_MAX, V|E, "me_method" },
{"hex", "hex motion estimation", 0, AV_OPT_TYPE_CONST, {.i64 = ME_HEX }, INT_MIN, INT_MAX, V|E, "me_method" },
{"umh", "umh motion estimation", 0, AV_OPT_TYPE_CONST, {.i64 = ME_UMH }, INT_MIN, INT_MAX, V|E, "me_method" },
{"iter", "iter motion estimation", 0, AV_OPT_TYPE_CONST, {.i64 = ME_ITER }, INT_MIN, INT_MAX, V|E, "me_method" },
#endif
{"time_base", NULL, OFFSET(time_base), AV_OPT_TYPE_RATIONAL, {.dbl = 0}, 0, INT_MAX},
{"g", "set the group of picture (GOP) size", OFFSET(gop_size), AV_OPT_TYPE_INT, {.i64 = 12 }, INT_MIN, INT_MAX, V|E},
{"ar", "set audio sampling rate (in Hz)", OFFSET(sample_rate), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, 0, INT_MAX, A|D|E},
{"ac", "set number of audio channels", OFFSET(channels), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, 0, INT_MAX, A|D|E},
{"cutoff", "set cutoff bandwidth", OFFSET(cutoff), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, A|E},
{"frame_size", NULL, OFFSET(frame_size), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, 0, INT_MAX, A|E},
{"frame_number", NULL, OFFSET(frame_number), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX},
{"delay", NULL, OFFSET(delay), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX},
{"qcomp", "video quantizer scale compression (VBR). Constant of ratecontrol equation. "
          "Recommended range for default rc_eq: 0.0-1.0",
          OFFSET(qcompress), AV_OPT_TYPE_FLOAT, {.dbl = 0.5 }, -FLT_MAX, FLT_MAX, V|E},
{"qblur", "video quantizer scale blur (VBR)", OFFSET(qblur), AV_OPT_TYPE_FLOAT, {.dbl = 0.5 }, -1, FLT_MAX, V|E},
{"qmin", "minimum video quantizer scale (VBR)", OFFSET(qmin), AV_OPT_TYPE_INT, {.i64 = 2 }, -1, 69, V|E},
{"qmax", "maximum video quantizer scale (VBR)", OFFSET(qmax), AV_OPT_TYPE_INT, {.i64 = 31 }, -1, 1024, V|E},
{"qdiff", "maximum difference between the quantizer scales (VBR)", OFFSET(max_qdiff), AV_OPT_TYPE_INT, {.i64 = 3 }, INT_MIN, INT_MAX, V|E},
{"bf", "set maximum number of B-frames between non-B-frames", OFFSET(max_b_frames), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, -1, INT_MAX, V|E},
{"b_qfactor", "QP factor between P- and B-frames", OFFSET(b_quant_factor), AV_OPT_TYPE_FLOAT, {.dbl = 1.25 }, -FLT_MAX, FLT_MAX, V|E},
#if FF_API_RC_STRATEGY
{"rc_strategy", "ratecontrol method", OFFSET(rc_strategy), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|E},
#endif
#if FF_API_PRIVATE_OPT
{"b_strategy", "strategy to choose between I/P/B-frames", OFFSET(b_frame_strategy), AV_OPT_TYPE_INT, {.i64 = 0 }, INT_MIN, INT_MAX, V|E},
{"ps", "RTP payload size in bytes", OFFSET(rtp_payload_size), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|E},
#endif
#if FF_API_STAT_BITS
{"mv_bits", NULL, OFFSET(mv_bits), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX},
{"header_bits", NULL, OFFSET(header_bits), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX},
{"i_tex_bits", NULL, OFFSET(i_tex_bits), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX},
{"p_tex_bits", NULL, OFFSET(p_tex_bits), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX},
{"i_count", NULL, OFFSET(i_count), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX},
{"p_count", NULL, OFFSET(p_count), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX},
{"skip_count", NULL, OFFSET(skip_count), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX},
{"misc_bits", NULL, OFFSET(misc_bits), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX},
{"frame_bits", NULL, OFFSET(frame_bits), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX},
#endif
{"codec_tag", NULL, OFFSET(codec_tag), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX},
{"bug", "work around not autodetected encoder bugs", OFFSET(workaround_bugs), AV_OPT_TYPE_FLAGS, {.i64 = FF_BUG_AUTODETECT }, INT_MIN, INT_MAX, V|D, "bug"},
{"autodetect", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_BUG_AUTODETECT }, INT_MIN, INT_MAX, V|D, "bug"},
#if FF_API_OLD_MSMPEG4
{"old_msmpeg4", "some old lavc-generated MSMPEG4v3 files (no autodetection)", 0, AV_OPT_TYPE_CONST, {.i64 = FF_BUG_OLD_MSMPEG4 }, INT_MIN, INT_MAX, V|D, "bug"},
#endif
{"xvid_ilace", "Xvid interlacing bug (autodetected if FOURCC == XVIX)", 0, AV_OPT_TYPE_CONST, {.i64 = FF_BUG_XVID_ILACE }, INT_MIN, INT_MAX, V|D, "bug"},
{"ump4", "(autodetected if FOURCC == UMP4)", 0, AV_OPT_TYPE_CONST, {.i64 = FF_BUG_UMP4 }, INT_MIN, INT_MAX, V|D, "bug"},
{"no_padding", "padding bug (autodetected)", 0, AV_OPT_TYPE_CONST, {.i64 = FF_BUG_NO_PADDING }, INT_MIN, INT_MAX, V|D, "bug"},
{"amv", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_BUG_AMV }, INT_MIN, INT_MAX, V|D, "bug"},
#if FF_API_AC_VLC
{"ac_vlc", "illegal VLC bug (autodetected per FOURCC)", 0, AV_OPT_TYPE_CONST, {.i64 = FF_BUG_AC_VLC }, INT_MIN, INT_MAX, V|D, "bug"},
#endif
{"qpel_chroma", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_BUG_QPEL_CHROMA }, INT_MIN, INT_MAX, V|D, "bug"},
{"std_qpel", "old standard qpel (autodetected per FOURCC/version)", 0, AV_OPT_TYPE_CONST, {.i64 = FF_BUG_STD_QPEL }, INT_MIN, INT_MAX, V|D, "bug"},
{"qpel_chroma2", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_BUG_QPEL_CHROMA2 }, INT_MIN, INT_MAX, V|D, "bug"},
{"direct_blocksize", "direct-qpel-blocksize bug (autodetected per FOURCC/version)", 0, AV_OPT_TYPE_CONST, {.i64 = FF_BUG_DIRECT_BLOCKSIZE }, INT_MIN, INT_MAX, V|D, "bug"},
{"edge", "edge padding bug (autodetected per FOURCC/version)", 0, AV_OPT_TYPE_CONST, {.i64 = FF_BUG_EDGE }, INT_MIN, INT_MAX, V|D, "bug"},
{"hpel_chroma", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_BUG_HPEL_CHROMA }, INT_MIN, INT_MAX, V|D, "bug"},
{"dc_clip", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_BUG_DC_CLIP }, INT_MIN, INT_MAX, V|D, "bug"},
{"ms", "work around various bugs in Microsoft's broken decoders", 0, AV_OPT_TYPE_CONST, {.i64 = FF_BUG_MS }, INT_MIN, INT_MAX, V|D, "bug"},
{"trunc", "truncated frames", 0, AV_OPT_TYPE_CONST, {.i64 = FF_BUG_TRUNCATED}, INT_MIN, INT_MAX, V|D, "bug"},
{"iedge", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_BUG_IEDGE }, INT_MIN, INT_MAX, V|D, "bug"},
{"strict", "how strictly to follow the standards", OFFSET(strict_std_compliance), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, A|V|D|E, "strict"},
{"very", "strictly conform to a older more strict version of the spec or reference software", 0, AV_OPT_TYPE_CONST, {.i64 = FF_COMPLIANCE_VERY_STRICT }, INT_MIN, INT_MAX, A|V|D|E, "strict"},
{"strict", "strictly conform to all the things in the spec no matter what the consequences", 0, AV_OPT_TYPE_CONST, {.i64 = FF_COMPLIANCE_STRICT }, INT_MIN, INT_MAX, A|V|D|E, "strict"},
{"normal", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_COMPLIANCE_NORMAL }, INT_MIN, INT_MAX, A|V|D|E, "strict"},
{"unofficial", "allow unofficial extensions", 0, AV_OPT_TYPE_CONST, {.i64 = FF_COMPLIANCE_UNOFFICIAL }, INT_MIN, INT_MAX, A|V|D|E, "strict"},
{"experimental", "allow non-standardized experimental things", 0, AV_OPT_TYPE_CONST, {.i64 = FF_COMPLIANCE_EXPERIMENTAL }, INT_MIN, INT_MAX, A|V|D|E, "strict"},
{"b_qoffset", "QP offset between P- and B-frames", OFFSET(b_quant_offset), AV_OPT_TYPE_FLOAT, {.dbl = 1.25 }, -FLT_MAX, FLT_MAX, V|E},
{"err_detect", "set error detection flags", OFFSET(err_recognition), AV_OPT_TYPE_FLAGS, {.i64 = 0 }, INT_MIN, INT_MAX, A|V|D, "err_detect"},
{"crccheck", "verify embedded CRCs", 0, AV_OPT_TYPE_CONST, {.i64 = AV_EF_CRCCHECK }, INT_MIN, INT_MAX, A|V|D, "err_detect"},
{"bitstream", "detect bitstream specification deviations", 0, AV_OPT_TYPE_CONST, {.i64 = AV_EF_BITSTREAM }, INT_MIN, INT_MAX, A|V|D, "err_detect"},
{"buffer", "detect improper bitstream length", 0, AV_OPT_TYPE_CONST, {.i64 = AV_EF_BUFFER }, INT_MIN, INT_MAX, A|V|D, "err_detect"},
{"explode", "abort decoding on minor error detection", 0, AV_OPT_TYPE_CONST, {.i64 = AV_EF_EXPLODE }, INT_MIN, INT_MAX, A|V|D, "err_detect"},
{"ignore_err", "ignore errors", 0, AV_OPT_TYPE_CONST, {.i64 = AV_EF_IGNORE_ERR }, INT_MIN, INT_MAX, A|V|D, "err_detect"},
{"careful",    "consider things that violate the spec, are fast to check and have not been seen in the wild as errors", 0, AV_OPT_TYPE_CONST, {.i64 = AV_EF_CAREFUL }, INT_MIN, INT_MAX, A|V|D, "err_detect"},
{"compliant",  "consider all spec non compliancies as errors", 0, AV_OPT_TYPE_CONST, {.i64 = AV_EF_COMPLIANT }, INT_MIN, INT_MAX, A|V|D, "err_detect"},
{"aggressive", "consider things that a sane encoder should not do as an error", 0, AV_OPT_TYPE_CONST, {.i64 = AV_EF_AGGRESSIVE }, INT_MIN, INT_MAX, A|V|D, "err_detect"},
{"has_b_frames", NULL, OFFSET(has_b_frames), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, 0, INT_MAX},
{"block_align", NULL, OFFSET(block_align), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, 0, INT_MAX},
#if FF_API_PRIVATE_OPT
{"mpeg_quant", "use MPEG quantizers instead of H.263", OFFSET(mpeg_quant), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|E},
#endif
#if FF_API_MPV_OPT
{"qsquish", "deprecated, use encoder private options instead", OFFSET(rc_qsquish), AV_OPT_TYPE_FLOAT, {.dbl = DEFAULT }, 0, 99, V|E},
{"rc_qmod_amp",  "deprecated, use encoder private options instead", OFFSET(rc_qmod_amp), AV_OPT_TYPE_FLOAT, {.dbl = DEFAULT }, -FLT_MAX, FLT_MAX, V|E},
{"rc_qmod_freq", "deprecated, use encoder private options instead", OFFSET(rc_qmod_freq), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|E},
#endif
{"rc_override_count", NULL, OFFSET(rc_override_count), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX},
#if FF_API_MPV_OPT
{"rc_eq", "deprecated, use encoder private options instead", OFFSET(rc_eq), AV_OPT_TYPE_STRING, {.str = NULL}, CHAR_MIN, CHAR_MAX, V|E},
#endif
{"maxrate", "maximum bitrate (in bits/s). Used for VBV together with bufsize.", OFFSET(rc_max_rate), AV_OPT_TYPE_INT64, {.i64 = DEFAULT }, 0, INT_MAX, V|A|E},
{"minrate", "minimum bitrate (in bits/s). Most useful in setting up a CBR encode. It is of little use otherwise.",
            OFFSET(rc_min_rate), AV_OPT_TYPE_INT64, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|A|E},
{"bufsize", "set ratecontrol buffer size (in bits)", OFFSET(rc_buffer_size), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, A|V|E},
#if FF_API_MPV_OPT
{"rc_buf_aggressivity", "deprecated, use encoder private options instead", OFFSET(rc_buffer_aggressivity), AV_OPT_TYPE_FLOAT, {.dbl = 1.0 }, -FLT_MAX, FLT_MAX, V|E},
#endif
{"i_qfactor", "QP factor between P- and I-frames", OFFSET(i_quant_factor), AV_OPT_TYPE_FLOAT, {.dbl = -0.8 }, -FLT_MAX, FLT_MAX, V|E},
{"i_qoffset", "QP offset between P- and I-frames", OFFSET(i_quant_offset), AV_OPT_TYPE_FLOAT, {.dbl = 0.0 }, -FLT_MAX, FLT_MAX, V|E},
#if FF_API_MPV_OPT
{"rc_init_cplx", "deprecated, use encoder private options instead", OFFSET(rc_initial_cplx), AV_OPT_TYPE_FLOAT, {.dbl = DEFAULT }, -FLT_MAX, FLT_MAX, V|E},
#endif
{"dct", "DCT algorithm", OFFSET(dct_algo), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, 0, INT_MAX, V|E, "dct"},
{"auto", "autoselect a good one", 0, AV_OPT_TYPE_CONST, {.i64 = FF_DCT_AUTO }, INT_MIN, INT_MAX, V|E, "dct"},
{"fastint", "fast integer", 0, AV_OPT_TYPE_CONST, {.i64 = FF_DCT_FASTINT }, INT_MIN, INT_MAX, V|E, "dct"},
{"int", "accurate integer", 0, AV_OPT_TYPE_CONST, {.i64 = FF_DCT_INT }, INT_MIN, INT_MAX, V|E, "dct"},
{"mmx", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_DCT_MMX }, INT_MIN, INT_MAX, V|E, "dct"},
{"altivec", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_DCT_ALTIVEC }, INT_MIN, INT_MAX, V|E, "dct"},
{"faan", "floating point AAN DCT", 0, AV_OPT_TYPE_CONST, {.i64 = FF_DCT_FAAN }, INT_MIN, INT_MAX, V|E, "dct"},
{"lumi_mask", "compresses bright areas stronger than medium ones", OFFSET(lumi_masking), AV_OPT_TYPE_FLOAT, {.dbl = 0 }, -FLT_MAX, FLT_MAX, V|E},
{"tcplx_mask", "temporal complexity masking", OFFSET(temporal_cplx_masking), AV_OPT_TYPE_FLOAT, {.dbl = 0 }, -FLT_MAX, FLT_MAX, V|E},
{"scplx_mask", "spatial complexity masking", OFFSET(spatial_cplx_masking), AV_OPT_TYPE_FLOAT, {.dbl = 0 }, -FLT_MAX, FLT_MAX, V|E},
{"p_mask", "inter masking", OFFSET(p_masking), AV_OPT_TYPE_FLOAT, {.dbl = 0 }, -FLT_MAX, FLT_MAX, V|E},
{"dark_mask", "compresses dark areas stronger than medium ones", OFFSET(dark_masking), AV_OPT_TYPE_FLOAT, {.dbl = 0 }, -FLT_MAX, FLT_MAX, V|E},
{"idct", "select IDCT implementation", OFFSET(idct_algo), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, 0, INT_MAX, V|E|D, "idct"},
{"auto", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_IDCT_AUTO }, INT_MIN, INT_MAX, V|E|D, "idct"},
{"int", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_IDCT_INT }, INT_MIN, INT_MAX, V|E|D, "idct"},
{"simple", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_IDCT_SIMPLE }, INT_MIN, INT_MAX, V|E|D, "idct"},
{"simplemmx", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_IDCT_SIMPLEMMX }, INT_MIN, INT_MAX, V|E|D, "idct"},
{"arm", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_IDCT_ARM }, INT_MIN, INT_MAX, V|E|D, "idct"},
{"altivec", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_IDCT_ALTIVEC }, INT_MIN, INT_MAX, V|E|D, "idct"},
#if FF_API_ARCH_SH4
{"sh4", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_IDCT_SH4 }, INT_MIN, INT_MAX, V|E|D, "idct"},
#endif
{"simplearm", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_IDCT_SIMPLEARM }, INT_MIN, INT_MAX, V|E|D, "idct"},
{"simplearmv5te", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_IDCT_SIMPLEARMV5TE }, INT_MIN, INT_MAX, V|E|D, "idct"},
{"simplearmv6", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_IDCT_SIMPLEARMV6 }, INT_MIN, INT_MAX, V|E|D, "idct"},
{"simpleneon", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_IDCT_SIMPLENEON }, INT_MIN, INT_MAX, V|E|D, "idct"},
#if FF_API_ARCH_ALPHA
{"simplealpha", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_IDCT_SIMPLEALPHA }, INT_MIN, INT_MAX, V|E|D, "idct"},
#endif
#if FF_API_UNUSED_MEMBERS
{"ipp", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_IDCT_IPP }, INT_MIN, INT_MAX, V|E|D, "idct"},
#endif /* FF_API_UNUSED_MEMBERS */
{"xvid", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_IDCT_XVID }, INT_MIN, INT_MAX, V|E|D, "idct"},
{"xvidmmx", "deprecated, for compatibility only", 0, AV_OPT_TYPE_CONST, {.i64 = FF_IDCT_XVID }, INT_MIN, INT_MAX, V|E|D, "idct"},
{"faani", "floating point AAN IDCT", 0, AV_OPT_TYPE_CONST, {.i64 = FF_IDCT_FAAN }, INT_MIN, INT_MAX, V|D|E, "idct"},
{"simpleauto", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_IDCT_SIMPLEAUTO }, INT_MIN, INT_MAX, V|E|D, "idct"},
{"slice_count", NULL, OFFSET(slice_count), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX},
{"ec", "set error concealment strategy", OFFSET(error_concealment), AV_OPT_TYPE_FLAGS, {.i64 = 3 }, INT_MIN, INT_MAX, V|D, "ec"},
{"guess_mvs", "iterative motion vector (MV) search (slow)", 0, AV_OPT_TYPE_CONST, {.i64 = FF_EC_GUESS_MVS }, INT_MIN, INT_MAX, V|D, "ec"},
{"deblock", "use strong deblock filter for damaged MBs", 0, AV_OPT_TYPE_CONST, {.i64 = FF_EC_DEBLOCK }, INT_MIN, INT_MAX, V|D, "ec"},
{"favor_inter", "favor predicting from the previous frame", 0, AV_OPT_TYPE_CONST, {.i64 = FF_EC_FAVOR_INTER }, INT_MIN, INT_MAX, V|D, "ec"},
{"bits_per_coded_sample", NULL, OFFSET(bits_per_coded_sample), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, 0, INT_MAX},
#if FF_API_PRIVATE_OPT
{"pred", "prediction method", OFFSET(prediction_method), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|E, "pred"},
{"left", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_PRED_LEFT }, INT_MIN, INT_MAX, V|E, "pred"},
{"plane", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_PRED_PLANE }, INT_MIN, INT_MAX, V|E, "pred"},
{"median", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_PRED_MEDIAN }, INT_MIN, INT_MAX, V|E, "pred"},
#endif
{"aspect", "sample aspect ratio", OFFSET(sample_aspect_ratio), AV_OPT_TYPE_RATIONAL, {.dbl = 0}, 0, 10, V|E},
{"sar",    "sample aspect ratio", OFFSET(sample_aspect_ratio), AV_OPT_TYPE_RATIONAL, {.dbl = 0}, 0, 10, V|E},
{"debug", "print specific debug info", OFFSET(debug), AV_OPT_TYPE_FLAGS, {.i64 = DEFAULT }, 0, INT_MAX, V|A|S|E|D, "debug"},
{"pict", "picture info", 0, AV_OPT_TYPE_CONST, {.i64 = FF_DEBUG_PICT_INFO }, INT_MIN, INT_MAX, V|D, "debug"},
{"rc", "rate control", 0, AV_OPT_TYPE_CONST, {.i64 = FF_DEBUG_RC }, INT_MIN, INT_MAX, V|E, "debug"},
{"bitstream", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_DEBUG_BITSTREAM }, INT_MIN, INT_MAX, V|D, "debug"},
{"mb_type", "macroblock (MB) type", 0, AV_OPT_TYPE_CONST, {.i64 = FF_DEBUG_MB_TYPE }, INT_MIN, INT_MAX, V|D, "debug"},
{"qp", "per-block quantization parameter (QP)", 0, AV_OPT_TYPE_CONST, {.i64 = FF_DEBUG_QP }, INT_MIN, INT_MAX, V|D, "debug"},
#if FF_API_DEBUG_MV
{"mv", "motion vector", 0, AV_OPT_TYPE_CONST, {.i64 = FF_DEBUG_MV }, INT_MIN, INT_MAX, V|D, "debug"},
#endif
{"dct_coeff", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_DEBUG_DCT_COEFF }, INT_MIN, INT_MAX, V|D, "debug"},
{"green_metadata", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_DEBUG_GREEN_MD }, INT_MIN, INT_MAX, V|D, "debug"},
{"skip", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_DEBUG_SKIP }, INT_MIN, INT_MAX, V|D, "debug"},
{"startcode", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_DEBUG_STARTCODE }, INT_MIN, INT_MAX, V|D, "debug"},
#if FF_API_UNUSED_MEMBERS
{"pts", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_DEBUG_PTS }, INT_MIN, INT_MAX, V|D, "debug"},
#endif /* FF_API_UNUSED_MEMBERS */
{"er", "error recognition", 0, AV_OPT_TYPE_CONST, {.i64 = FF_DEBUG_ER }, INT_MIN, INT_MAX, V|D, "debug"},
{"mmco", "memory management control operations (H.264)", 0, AV_OPT_TYPE_CONST, {.i64 = FF_DEBUG_MMCO }, INT_MIN, INT_MAX, V|D, "debug"},
{"bugs", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_DEBUG_BUGS }, INT_MIN, INT_MAX, V|D, "debug"},
#if FF_API_DEBUG_MV
{"vis_qp", "visualize quantization parameter (QP), lower QP are tinted greener", 0, AV_OPT_TYPE_CONST, {.i64 = FF_DEBUG_VIS_QP }, INT_MIN, INT_MAX, V|D, "debug"},
{"vis_mb_type", "visualize block types", 0, AV_OPT_TYPE_CONST, {.i64 = FF_DEBUG_VIS_MB_TYPE }, INT_MIN, INT_MAX, V|D, "debug"},
#endif
{"buffers", "picture buffer allocations", 0, AV_OPT_TYPE_CONST, {.i64 = FF_DEBUG_BUFFERS }, INT_MIN, INT_MAX, V|D, "debug"},
{"thread_ops", "threading operations", 0, AV_OPT_TYPE_CONST, {.i64 = FF_DEBUG_THREADS }, INT_MIN, INT_MAX, V|A|D, "debug"},
{"nomc", "skip motion compensation", 0, AV_OPT_TYPE_CONST, {.i64 = FF_DEBUG_NOMC }, INT_MIN, INT_MAX, V|A|D, "debug"},
#if FF_API_VISMV
{"vismv", "visualize motion vectors (MVs) (deprecated)", OFFSET(debug_mv), AV_OPT_TYPE_FLAGS, {.i64 = DEFAULT }, 0, INT_MAX, V|D, "debug_mv"},
{"pf", "forward predicted MVs of P-frames", 0, AV_OPT_TYPE_CONST, {.i64 = FF_DEBUG_VIS_MV_P_FOR }, INT_MIN, INT_MAX, V|D, "debug_mv"},
{"bf", "forward predicted MVs of B-frames", 0, AV_OPT_TYPE_CONST, {.i64 = FF_DEBUG_VIS_MV_B_FOR }, INT_MIN, INT_MAX, V|D, "debug_mv"},
{"bb", "backward predicted MVs of B-frames", 0, AV_OPT_TYPE_CONST, {.i64 = FF_DEBUG_VIS_MV_B_BACK }, INT_MIN, INT_MAX, V|D, "debug_mv"},
#endif
{"cmp", "full-pel ME compare function", OFFSET(me_cmp), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|E, "cmp_func"},
{"subcmp", "sub-pel ME compare function", OFFSET(me_sub_cmp), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|E, "cmp_func"},
{"mbcmp", "macroblock compare function", OFFSET(mb_cmp), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|E, "cmp_func"},
{"ildctcmp", "interlaced DCT compare function", OFFSET(ildct_cmp), AV_OPT_TYPE_INT, {.i64 = FF_CMP_VSAD }, INT_MIN, INT_MAX, V|E, "cmp_func"},
{"dia_size", "diamond type & size for motion estimation", OFFSET(dia_size), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|E},
{"last_pred", "amount of motion predictors from the previous frame", OFFSET(last_predictor_count), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|E},
#if FF_API_PRIVATE_OPT
{"preme", "pre motion estimation", OFFSET(pre_me), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|E},
#endif
{"precmp", "pre motion estimation compare function", OFFSET(me_pre_cmp), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|E, "cmp_func"},
{"sad", "sum of absolute differences, fast", 0, AV_OPT_TYPE_CONST, {.i64 = FF_CMP_SAD }, INT_MIN, INT_MAX, V|E, "cmp_func"},
{"sse", "sum of squared errors", 0, AV_OPT_TYPE_CONST, {.i64 = FF_CMP_SSE }, INT_MIN, INT_MAX, V|E, "cmp_func"},
{"satd", "sum of absolute Hadamard transformed differences", 0, AV_OPT_TYPE_CONST, {.i64 = FF_CMP_SATD }, INT_MIN, INT_MAX, V|E, "cmp_func"},
{"dct", "sum of absolute DCT transformed differences", 0, AV_OPT_TYPE_CONST, {.i64 = FF_CMP_DCT }, INT_MIN, INT_MAX, V|E, "cmp_func"},
{"psnr", "sum of squared quantization errors (avoid, low quality)", 0, AV_OPT_TYPE_CONST, {.i64 = FF_CMP_PSNR }, INT_MIN, INT_MAX, V|E, "cmp_func"},
{"bit", "number of bits needed for the block", 0, AV_OPT_TYPE_CONST, {.i64 = FF_CMP_BIT }, INT_MIN, INT_MAX, V|E, "cmp_func"},
{"rd", "rate distortion optimal, slow", 0, AV_OPT_TYPE_CONST, {.i64 = FF_CMP_RD }, INT_MIN, INT_MAX, V|E, "cmp_func"},
{"zero", "0", 0, AV_OPT_TYPE_CONST, {.i64 = FF_CMP_ZERO }, INT_MIN, INT_MAX, V|E, "cmp_func"},
{"vsad", "sum of absolute vertical differences", 0, AV_OPT_TYPE_CONST, {.i64 = FF_CMP_VSAD }, INT_MIN, INT_MAX, V|E, "cmp_func"},
{"vsse", "sum of squared vertical differences", 0, AV_OPT_TYPE_CONST, {.i64 = FF_CMP_VSSE }, INT_MIN, INT_MAX, V|E, "cmp_func"},
{"nsse", "noise preserving sum of squared differences", 0, AV_OPT_TYPE_CONST, {.i64 = FF_CMP_NSSE }, INT_MIN, INT_MAX, V|E, "cmp_func"},
#if CONFIG_SNOW_ENCODER
{"w53", "5/3 wavelet, only used in snow", 0, AV_OPT_TYPE_CONST, {.i64 = FF_CMP_W53 }, INT_MIN, INT_MAX, V|E, "cmp_func"},
{"w97", "9/7 wavelet, only used in snow", 0, AV_OPT_TYPE_CONST, {.i64 = FF_CMP_W97 }, INT_MIN, INT_MAX, V|E, "cmp_func"},
#endif
{"dctmax", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_CMP_DCTMAX }, INT_MIN, INT_MAX, V|E, "cmp_func"},
{"chroma", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_CMP_CHROMA }, INT_MIN, INT_MAX, V|E, "cmp_func"},
{"msad", "sum of absolute differences, median predicted", 0, AV_OPT_TYPE_CONST, {.i64 = FF_CMP_MEDIAN_SAD }, INT_MIN, INT_MAX, V|E, "cmp_func"},
{"pre_dia_size", "diamond type & size for motion estimation pre-pass", OFFSET(pre_dia_size), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|E},
{"subq", "sub-pel motion estimation quality", OFFSET(me_subpel_quality), AV_OPT_TYPE_INT, {.i64 = 8 }, INT_MIN, INT_MAX, V|E},
#if FF_API_AFD
{"dtg_active_format", NULL, OFFSET(dtg_active_format), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX},
#endif
{"me_range", "limit motion vectors range (1023 for DivX player)", OFFSET(me_range), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|E},
#if FF_API_QUANT_BIAS
{"ibias", "intra quant bias", OFFSET(intra_quant_bias), AV_OPT_TYPE_INT, {.i64 = FF_DEFAULT_QUANT_BIAS }, INT_MIN, INT_MAX, V|E},
{"pbias", "inter quant bias", OFFSET(inter_quant_bias), AV_OPT_TYPE_INT, {.i64 = FF_DEFAULT_QUANT_BIAS }, INT_MIN, INT_MAX, V|E},
#endif
{"global_quality", NULL, OFFSET(global_quality), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|A|E},
#if FF_API_CODER_TYPE
{"coder", NULL, OFFSET(coder_type), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|E, "coder"},
{"vlc", "variable length coder / Huffman coder", 0, AV_OPT_TYPE_CONST, {.i64 = FF_CODER_TYPE_VLC }, INT_MIN, INT_MAX, V|E, "coder"},
{"ac", "arithmetic coder", 0, AV_OPT_TYPE_CONST, {.i64 = FF_CODER_TYPE_AC }, INT_MIN, INT_MAX, V|E, "coder"},
{"raw", "raw (no encoding)", 0, AV_OPT_TYPE_CONST, {.i64 = FF_CODER_TYPE_RAW }, INT_MIN, INT_MAX, V|E, "coder"},
{"rle", "run-length coder", 0, AV_OPT_TYPE_CONST, {.i64 = FF_CODER_TYPE_RLE }, INT_MIN, INT_MAX, V|E, "coder"},
#if FF_API_UNUSED_MEMBERS
{"deflate", "deflate-based coder", 0, AV_OPT_TYPE_CONST, {.i64 = FF_CODER_TYPE_DEFLATE }, INT_MIN, INT_MAX, V|E, "coder"},
#endif /* FF_API_UNUSED_MEMBERS */
#endif /* FF_API_CODER_TYPE */
#if FF_API_PRIVATE_OPT
{"context", "context model", OFFSET(context_model), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|E},
#endif
{"slice_flags", NULL, OFFSET(slice_flags), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX},
#if FF_API_XVMC
{"xvmc_acceleration", NULL, OFFSET(xvmc_acceleration), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX},
#endif /* FF_API_XVMC */
{"mbd", "macroblock decision algorithm (high quality mode)", OFFSET(mb_decision), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, 0, 2, V|E, "mbd"},
{"simple", "use mbcmp", 0, AV_OPT_TYPE_CONST, {.i64 = FF_MB_DECISION_SIMPLE }, INT_MIN, INT_MAX, V|E, "mbd"},
{"bits", "use fewest bits", 0, AV_OPT_TYPE_CONST, {.i64 = FF_MB_DECISION_BITS }, INT_MIN, INT_MAX, V|E, "mbd"},
{"rd", "use best rate distortion", 0, AV_OPT_TYPE_CONST, {.i64 = FF_MB_DECISION_RD }, INT_MIN, INT_MAX, V|E, "mbd"},
#if FF_API_STREAM_CODEC_TAG
{"stream_codec_tag", NULL, OFFSET(stream_codec_tag), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX},
#endif
#if FF_API_PRIVATE_OPT
{"sc_threshold", "scene change threshold", OFFSET(scenechange_threshold), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|E},
#endif
#if FF_API_MPV_OPT
{"lmin", "deprecated, use encoder private options instead", OFFSET(lmin), AV_OPT_TYPE_INT, {.i64 =  0 }, 0, INT_MAX, V|E},
{"lmax", "deprecated, use encoder private options instead", OFFSET(lmax), AV_OPT_TYPE_INT, {.i64 =  0 }, 0, INT_MAX, V|E},
#endif
#if FF_API_PRIVATE_OPT
{"nr", "noise reduction", OFFSET(noise_reduction), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|E},
#endif
{"rc_init_occupancy", "number of bits which should be loaded into the rc buffer before decoding starts", OFFSET(rc_initial_buffer_occupancy), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|E},
{"flags2", NULL, OFFSET(flags2), AV_OPT_TYPE_FLAGS, {.i64 = DEFAULT}, 0, UINT_MAX, V|A|E|D, "flags2"},
#if FF_API_ERROR_RATE
{"error", NULL, OFFSET(error_rate), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|E},
#endif
{"threads", "set the number of threads", OFFSET(thread_count), AV_OPT_TYPE_INT, {.i64 = 1 }, 0, INT_MAX, V|A|E|D, "threads"},
{"auto", "autodetect a suitable number of threads to use", 0, AV_OPT_TYPE_CONST, {.i64 = 0 }, INT_MIN, INT_MAX, V|E|D, "threads"},
#if FF_API_MPV_OPT
{"me_threshold", "motion estimation threshold", OFFSET(me_threshold), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|E},
{"mb_threshold", "macroblock threshold", OFFSET(mb_threshold), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|E},
#endif
{"dc", "intra_dc_precision", OFFSET(intra_dc_precision), AV_OPT_TYPE_INT, {.i64 = 0 }, -8, 16, V|E},
{"nssew", "nsse weight", OFFSET(nsse_weight), AV_OPT_TYPE_INT, {.i64 = 8 }, INT_MIN, INT_MAX, V|E},
{"skip_top", "number of macroblock rows at the top which are skipped", OFFSET(skip_top), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|D},
{"skip_bottom", "number of macroblock rows at the bottom which are skipped", OFFSET(skip_bottom), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|D},
{"profile", NULL, OFFSET(profile), AV_OPT_TYPE_INT, {.i64 = FF_PROFILE_UNKNOWN }, INT_MIN, INT_MAX, V|A|E, "profile"},
{"unknown", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_PROFILE_UNKNOWN }, INT_MIN, INT_MAX, V|A|E, "profile"},
{"aac_main", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_PROFILE_AAC_MAIN }, INT_MIN, INT_MAX, A|E, "profile"},
{"aac_low", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_PROFILE_AAC_LOW }, INT_MIN, INT_MAX, A|E, "profile"},
{"aac_ssr", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_PROFILE_AAC_SSR }, INT_MIN, INT_MAX, A|E, "profile"},
{"aac_ltp", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_PROFILE_AAC_LTP }, INT_MIN, INT_MAX, A|E, "profile"},
{"aac_he", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_PROFILE_AAC_HE }, INT_MIN, INT_MAX, A|E, "profile"},
{"aac_he_v2", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_PROFILE_AAC_HE_V2 }, INT_MIN, INT_MAX, A|E, "profile"},
{"aac_ld", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_PROFILE_AAC_LD }, INT_MIN, INT_MAX, A|E, "profile"},
{"aac_eld", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_PROFILE_AAC_ELD }, INT_MIN, INT_MAX, A|E, "profile"},
{"mpeg2_aac_low", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_PROFILE_MPEG2_AAC_LOW }, INT_MIN, INT_MAX, A|E, "profile"},
{"mpeg2_aac_he", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_PROFILE_MPEG2_AAC_HE }, INT_MIN, INT_MAX, A|E, "profile"},
{"dts", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_PROFILE_DTS }, INT_MIN, INT_MAX, A|E, "profile"},
{"dts_es", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_PROFILE_DTS_ES }, INT_MIN, INT_MAX, A|E, "profile"},
{"dts_96_24", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_PROFILE_DTS_96_24 }, INT_MIN, INT_MAX, A|E, "profile"},
{"dts_hd_hra", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_PROFILE_DTS_HD_HRA }, INT_MIN, INT_MAX, A|E, "profile"},
{"dts_hd_ma", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_PROFILE_DTS_HD_MA }, INT_MIN, INT_MAX, A|E, "profile"},
{"mpeg4_sp",   NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_PROFILE_MPEG4_SIMPLE }, INT_MIN, INT_MAX, V|E, "profile"},
{"mpeg4_core", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_PROFILE_MPEG4_CORE }, INT_MIN, INT_MAX, V|E, "profile"},
{"mpeg4_main", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_PROFILE_MPEG4_MAIN }, INT_MIN, INT_MAX, V|E, "profile"},
{"mpeg4_asp",  NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_PROFILE_MPEG4_ADVANCED_SIMPLE }, INT_MIN, INT_MAX, V|E, "profile"},
{"main10",  NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_PROFILE_HEVC_MAIN_10 }, INT_MIN, INT_MAX, V|E, "profile"},
{"level", NULL, OFFSET(level), AV_OPT_TYPE_INT, {.i64 = FF_LEVEL_UNKNOWN }, INT_MIN, INT_MAX, V|A|E, "level"},
{"unknown", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_LEVEL_UNKNOWN }, INT_MIN, INT_MAX, V|A|E, "level"},
{"lowres", "decode at 1= 1/2, 2=1/4, 3=1/8 resolutions", OFFSET(lowres), AV_OPT_TYPE_INT, {.i64 = 0 }, 0, INT_MAX, V|A|D},
#if FF_API_PRIVATE_OPT
{"skip_threshold", "frame skip threshold", OFFSET(frame_skip_threshold), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|E},
{"skip_factor", "frame skip factor", OFFSET(frame_skip_factor), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|E},
{"skip_exp", "frame skip exponent", OFFSET(frame_skip_exp), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|E},
{"skipcmp", "frame skip compare function", OFFSET(frame_skip_cmp), AV_OPT_TYPE_INT, {.i64 = FF_CMP_DCTMAX }, INT_MIN, INT_MAX, V|E, "cmp_func"},
#endif
#if FF_API_MPV_OPT
{"border_mask", "deprecated, use encoder private options instead", OFFSET(border_masking), AV_OPT_TYPE_FLOAT, {.dbl = DEFAULT }, -FLT_MAX, FLT_MAX, V|E},
#endif
{"mblmin", "minimum macroblock Lagrange factor (VBR)", OFFSET(mb_lmin), AV_OPT_TYPE_INT, {.i64 = FF_QP2LAMBDA * 2 }, 1, FF_LAMBDA_MAX, V|E},
{"mblmax", "maximum macroblock Lagrange factor (VBR)", OFFSET(mb_lmax), AV_OPT_TYPE_INT, {.i64 = FF_QP2LAMBDA * 31 }, 1, FF_LAMBDA_MAX, V|E},
#if FF_API_PRIVATE_OPT
{"mepc", "motion estimation bitrate penalty compensation (1.0 = 256)", OFFSET(me_penalty_compensation), AV_OPT_TYPE_INT, {.i64 = 256 }, INT_MIN, INT_MAX, V|E},
#endif
{"skip_loop_filter", "skip loop filtering process for the selected frames", OFFSET(skip_loop_filter), AV_OPT_TYPE_INT, {.i64 = AVDISCARD_DEFAULT }, INT_MIN, INT_MAX, V|D, "avdiscard"},
{"skip_idct"       , "skip IDCT/dequantization for the selected frames",    OFFSET(skip_idct),        AV_OPT_TYPE_INT, {.i64 = AVDISCARD_DEFAULT }, INT_MIN, INT_MAX, V|D, "avdiscard"},
{"skip_frame"      , "skip decoding for the selected frames",               OFFSET(skip_frame),       AV_OPT_TYPE_INT, {.i64 = AVDISCARD_DEFAULT }, INT_MIN, INT_MAX, V|D, "avdiscard"},
{"none"            , "discard no frame",                    0, AV_OPT_TYPE_CONST, {.i64 = AVDISCARD_NONE    }, INT_MIN, INT_MAX, V|D, "avdiscard"},
{"default"         , "discard useless frames",              0, AV_OPT_TYPE_CONST, {.i64 = AVDISCARD_DEFAULT }, INT_MIN, INT_MAX, V|D, "avdiscard"},
{"noref"           , "discard all non-reference frames",    0, AV_OPT_TYPE_CONST, {.i64 = AVDISCARD_NONREF  }, INT_MIN, INT_MAX, V|D, "avdiscard"},
{"bidir"           , "discard all bidirectional frames",    0, AV_OPT_TYPE_CONST, {.i64 = AVDISCARD_BIDIR   }, INT_MIN, INT_MAX, V|D, "avdiscard"},
{"nokey"           , "discard all frames except keyframes", 0, AV_OPT_TYPE_CONST, {.i64 = AVDISCARD_NONKEY  }, INT_MIN, INT_MAX, V|D, "avdiscard"},
{"nointra"         , "discard all frames except I frames",  0, AV_OPT_TYPE_CONST, {.i64 = AVDISCARD_NONINTRA}, INT_MIN, INT_MAX, V|D, "avdiscard"},
{"all"             , "discard all frames",                  0, AV_OPT_TYPE_CONST, {.i64 = AVDISCARD_ALL     }, INT_MIN, INT_MAX, V|D, "avdiscard"},
{"bidir_refine", "refine the two motion vectors used in bidirectional macroblocks", OFFSET(bidir_refine), AV_OPT_TYPE_INT, {.i64 = 1 }, 0, 4, V|E},
#if FF_API_PRIVATE_OPT
{"brd_scale", "downscale frames for dynamic B-frame decision", OFFSET(brd_scale), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, 0, 10, V|E},
#endif
{"keyint_min", "minimum interval between IDR-frames", OFFSET(keyint_min), AV_OPT_TYPE_INT, {.i64 = 25 }, INT_MIN, INT_MAX, V|E},
{"refs", "reference frames to consider for motion compensation", OFFSET(refs), AV_OPT_TYPE_INT, {.i64 = 1 }, INT_MIN, INT_MAX, V|E},
#if FF_API_PRIVATE_OPT
{"chromaoffset", "chroma QP offset from luma", OFFSET(chromaoffset), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|E},
#endif
{"trellis", "rate-distortion optimal quantization", OFFSET(trellis), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, INT_MIN, INT_MAX, V|A|E},
#if FF_API_UNUSED_MEMBERS
{"sc_factor", "multiplied by qscale for each frame and added to scene_change_score", OFFSET(scenechange_factor), AV_OPT_TYPE_INT, {.i64 = 6 }, 0, INT_MAX, V|E},
#endif /* FF_API_UNUSED_MEMBERS */
{"mv0_threshold", NULL, OFFSET(mv0_threshold), AV_OPT_TYPE_INT, {.i64 = 256 }, 0, INT_MAX, V|E},
#if FF_API_PRIVATE_OPT
{"b_sensitivity", "adjust sensitivity of b_frame_strategy 1", OFFSET(b_sensitivity), AV_OPT_TYPE_INT, {.i64 = 40 }, 1, INT_MAX, V|E},
#endif
{"compression_level", NULL, OFFSET(compression_level), AV_OPT_TYPE_INT, {.i64 = FF_COMPRESSION_DEFAULT }, INT_MIN, INT_MAX, V|A|E},
#if FF_API_PRIVATE_OPT
{"min_prediction_order", NULL, OFFSET(min_prediction_order), AV_OPT_TYPE_INT, {.i64 = -1 }, INT_MIN, INT_MAX, A|E},
{"max_prediction_order", NULL, OFFSET(max_prediction_order), AV_OPT_TYPE_INT, {.i64 = -1 }, INT_MIN, INT_MAX, A|E},
{"timecode_frame_start", "GOP timecode frame start number, in non-drop-frame format", OFFSET(timecode_frame_start), AV_OPT_TYPE_INT64, {.i64 = -1 }, -1, INT64_MAX, V|E},
#endif
{"bits_per_raw_sample", NULL, OFFSET(bits_per_raw_sample), AV_OPT_TYPE_INT, {.i64 = DEFAULT }, 0, INT_MAX},
{"channel_layout", NULL, OFFSET(channel_layout), AV_OPT_TYPE_UINT64, {.i64 = DEFAULT }, 0, UINT64_MAX, A|E|D, "channel_layout"},
{"request_channel_layout", NULL, OFFSET(request_channel_layout), AV_OPT_TYPE_UINT64, {.i64 = DEFAULT }, 0, UINT64_MAX, A|D, "request_channel_layout"},
{"rc_max_vbv_use", NULL, OFFSET(rc_max_available_vbv_use), AV_OPT_TYPE_FLOAT, {.dbl = 0 }, 0.0, FLT_MAX, V|E},
{"rc_min_vbv_use", NULL, OFFSET(rc_min_vbv_overflow_use),  AV_OPT_TYPE_FLOAT, {.dbl = 3 },     0.0, FLT_MAX, V|E},
{"ticks_per_frame", NULL, OFFSET(ticks_per_frame), AV_OPT_TYPE_INT, {.i64 = 1 }, 1, INT_MAX, A|V|E|D},
{"color_primaries", "color primaries", OFFSET(color_primaries), AV_OPT_TYPE_INT, {.i64 = AVCOL_PRI_UNSPECIFIED }, 1, INT_MAX, V|E|D, "color_primaries_type"},
{"bt709",       "BT.709",         0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_PRI_BT709 },        INT_MIN, INT_MAX, V|E|D, "color_primaries_type"},
{"unknown",     "Unspecified",    0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_PRI_UNSPECIFIED },  INT_MIN, INT_MAX, V|E|D, "color_primaries_type"},
{"bt470m",      "BT.470 M",       0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_PRI_BT470M },       INT_MIN, INT_MAX, V|E|D, "color_primaries_type"},
{"bt470bg",     "BT.470 BG",      0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_PRI_BT470BG },      INT_MIN, INT_MAX, V|E|D, "color_primaries_type"},
{"smpte170m",   "SMPTE 170 M",    0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_PRI_SMPTE170M },    INT_MIN, INT_MAX, V|E|D, "color_primaries_type"},
{"smpte240m",   "SMPTE 240 M",    0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_PRI_SMPTE240M },    INT_MIN, INT_MAX, V|E|D, "color_primaries_type"},
{"film",        "Film",           0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_PRI_FILM },         INT_MIN, INT_MAX, V|E|D, "color_primaries_type"},
{"bt2020",      "BT.2020",        0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_PRI_BT2020 },       INT_MIN, INT_MAX, V|E|D, "color_primaries_type"},
{"smpte428",    "SMPTE 428-1",    0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_PRI_SMPTE428 },     INT_MIN, INT_MAX, V|E|D, "color_primaries_type"},
{"smpte428_1",  "SMPTE 428-1",    0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_PRI_SMPTE428 },     INT_MIN, INT_MAX, V|E|D, "color_primaries_type"},
{"smpte431",    "SMPTE 431-2",    0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_PRI_SMPTE431 },     INT_MIN, INT_MAX, V|E|D, "color_primaries_type"},
{"smpte432",    "SMPTE 422-1",    0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_PRI_SMPTE432 },     INT_MIN, INT_MAX, V|E|D, "color_primaries_type"},
{"jedec-p22",   "JEDEC P22",      0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_PRI_JEDEC_P22 },    INT_MIN, INT_MAX, V|E|D, "color_primaries_type"},
{"unspecified", "Unspecified",    0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_PRI_UNSPECIFIED },  INT_MIN, INT_MAX, V|E|D, "color_primaries_type"},
{"color_trc", "color transfer characteristics", OFFSET(color_trc), AV_OPT_TYPE_INT, {.i64 = AVCOL_TRC_UNSPECIFIED }, 1, INT_MAX, V|E|D, "color_trc_type"},
{"bt709",        "BT.709",           0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_TRC_BT709 },        INT_MIN, INT_MAX, V|E|D, "color_trc_type"},
{"unknown",      "Unspecified",      0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_TRC_UNSPECIFIED },  INT_MIN, INT_MAX, V|E|D, "color_trc_type"},
{"gamma22",      "BT.470 M",         0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_TRC_GAMMA22 },      INT_MIN, INT_MAX, V|E|D, "color_trc_type"},
{"gamma28",      "BT.470 BG",        0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_TRC_GAMMA28 },      INT_MIN, INT_MAX, V|E|D, "color_trc_type"},
{"smpte170m",    "SMPTE 170 M",      0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_TRC_SMPTE170M },    INT_MIN, INT_MAX, V|E|D, "color_trc_type"},
{"smpte240m",    "SMPTE 240 M",      0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_TRC_SMPTE240M },    INT_MIN, INT_MAX, V|E|D, "color_trc_type"},
{"linear",       "Linear",           0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_TRC_LINEAR },       INT_MIN, INT_MAX, V|E|D, "color_trc_type"},
{"log100",       "Log",              0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_TRC_LOG },          INT_MIN, INT_MAX, V|E|D, "color_trc_type"},
{"log316",       "Log square root",  0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_TRC_LOG_SQRT },     INT_MIN, INT_MAX, V|E|D, "color_trc_type"},
{"iec61966-2-4", "IEC 61966-2-4",    0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_TRC_IEC61966_2_4 }, INT_MIN, INT_MAX, V|E|D, "color_trc_type"},
{"bt1361e",      "BT.1361",          0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_TRC_BT1361_ECG },   INT_MIN, INT_MAX, V|E|D, "color_trc_type"},
{"iec61966-2-1", "IEC 61966-2-1",    0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_TRC_IEC61966_2_1 }, INT_MIN, INT_MAX, V|E|D, "color_trc_type"},
{"bt2020-10",    "BT.2020 - 10 bit", 0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_TRC_BT2020_10 },    INT_MIN, INT_MAX, V|E|D, "color_trc_type"},
{"bt2020-12",    "BT.2020 - 12 bit", 0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_TRC_BT2020_12 },    INT_MIN, INT_MAX, V|E|D, "color_trc_type"},
{"smpte2084",    "SMPTE 2084",       0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_TRC_SMPTE2084 },    INT_MIN, INT_MAX, V|E|D, "color_trc_type"},
{"smpte428",     "SMPTE 428-1",      0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_TRC_SMPTE428 },     INT_MIN, INT_MAX, V|E|D, "color_trc_type"},
{"arib-std-b67", "ARIB STD-B67",     0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_TRC_ARIB_STD_B67 }, INT_MIN, INT_MAX, V|E|D, "color_trc_type"},
{"unspecified",  "Unspecified",      0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_TRC_UNSPECIFIED },  INT_MIN, INT_MAX, V|E|D, "color_trc_type"},
{"log",          "Log",              0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_TRC_LOG },          INT_MIN, INT_MAX, V|E|D, "color_trc_type"},
{"log_sqrt",     "Log square root",  0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_TRC_LOG_SQRT },     INT_MIN, INT_MAX, V|E|D, "color_trc_type"},
{"iec61966_2_4", "IEC 61966-2-4",    0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_TRC_IEC61966_2_4 }, INT_MIN, INT_MAX, V|E|D, "color_trc_type"},
{"bt1361",       "BT.1361",          0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_TRC_BT1361_ECG },   INT_MIN, INT_MAX, V|E|D, "color_trc_type"},
{"iec61966_2_1", "IEC 61966-2-1",    0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_TRC_IEC61966_2_1 }, INT_MIN, INT_MAX, V|E|D, "color_trc_type"},
{"bt2020_10bit", "BT.2020 - 10 bit", 0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_TRC_BT2020_10 },    INT_MIN, INT_MAX, V|E|D, "color_trc_type"},
{"bt2020_12bit", "BT.2020 - 12 bit", 0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_TRC_BT2020_12 },    INT_MIN, INT_MAX, V|E|D, "color_trc_type"},
{"smpte428_1",   "SMPTE 428-1",      0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_TRC_SMPTE428 },     INT_MIN, INT_MAX, V|E|D, "color_trc_type"},
{"colorspace", "color space", OFFSET(colorspace), AV_OPT_TYPE_INT, {.i64 = AVCOL_SPC_UNSPECIFIED }, 0, INT_MAX, V|E|D, "colorspace_type"},
{"rgb",         "RGB",         0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_SPC_RGB },         INT_MIN, INT_MAX, V|E|D, "colorspace_type"},
{"bt709",       "BT.709",      0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_SPC_BT709 },       INT_MIN, INT_MAX, V|E|D, "colorspace_type"},
{"unknown",     "Unspecified", 0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_SPC_UNSPECIFIED }, INT_MIN, INT_MAX, V|E|D, "colorspace_type"},
{"fcc",         "FCC",         0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_SPC_FCC },         INT_MIN, INT_MAX, V|E|D, "colorspace_type"},
{"bt470bg",     "BT.470 BG",   0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_SPC_BT470BG },     INT_MIN, INT_MAX, V|E|D, "colorspace_type"},
{"smpte170m",   "SMPTE 170 M", 0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_SPC_SMPTE170M },   INT_MIN, INT_MAX, V|E|D, "colorspace_type"},
{"smpte240m",   "SMPTE 240 M", 0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_SPC_SMPTE240M },   INT_MIN, INT_MAX, V|E|D, "colorspace_type"},
{"ycgco",       "YCGCO",       0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_SPC_YCGCO },       INT_MIN, INT_MAX, V|E|D, "colorspace_type"},
{"bt2020nc",    "BT.2020 NCL", 0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_SPC_BT2020_NCL },  INT_MIN, INT_MAX, V|E|D, "colorspace_type"},
{"bt2020c",     "BT.2020 CL",  0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_SPC_BT2020_CL },   INT_MIN, INT_MAX, V|E|D, "colorspace_type"},
{"smpte2085",   "SMPTE 2085",  0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_SPC_SMPTE2085 },   INT_MIN, INT_MAX, V|E|D, "colorspace_type"},
{"unspecified", "Unspecified", 0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_SPC_UNSPECIFIED }, INT_MIN, INT_MAX, V|E|D, "colorspace_type"},
{"ycocg",       "YCGCO",       0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_SPC_YCGCO },       INT_MIN, INT_MAX, V|E|D, "colorspace_type"},
{"bt2020_ncl",  "BT.2020 NCL", 0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_SPC_BT2020_NCL },  INT_MIN, INT_MAX, V|E|D, "colorspace_type"},
{"bt2020_cl",   "BT.2020 CL",  0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_SPC_BT2020_CL },   INT_MIN, INT_MAX, V|E|D, "colorspace_type"},
{"color_range", "color range", OFFSET(color_range), AV_OPT_TYPE_INT, {.i64 = AVCOL_RANGE_UNSPECIFIED }, 0, INT_MAX, V|E|D, "color_range_type"},
{"unknown", "Unspecified",     0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_RANGE_UNSPECIFIED }, INT_MIN, INT_MAX, V|E|D, "color_range_type"},
{"tv", "MPEG (219*2^(n-8))",   0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_RANGE_MPEG },        INT_MIN, INT_MAX, V|E|D, "color_range_type"},
{"pc", "JPEG (2^n-1)",         0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_RANGE_JPEG },        INT_MIN, INT_MAX, V|E|D, "color_range_type"},
{"unspecified", "Unspecified", 0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_RANGE_UNSPECIFIED }, INT_MIN, INT_MAX, V|E|D, "color_range_type"},
{"mpeg", "MPEG (219*2^(n-8))", 0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_RANGE_MPEG },        INT_MIN, INT_MAX, V|E|D, "color_range_type"},
{"jpeg", "JPEG (2^n-1)",       0, AV_OPT_TYPE_CONST, {.i64 = AVCOL_RANGE_JPEG },        INT_MIN, INT_MAX, V|E|D, "color_range_type"},
{"chroma_sample_location", "chroma sample location", OFFSET(chroma_sample_location), AV_OPT_TYPE_INT, {.i64 = AVCHROMA_LOC_UNSPECIFIED }, 0, INT_MAX, V|E|D, "chroma_sample_location_type"},
{"unknown",     "Unspecified", 0, AV_OPT_TYPE_CONST, {.i64 = AVCHROMA_LOC_UNSPECIFIED }, INT_MIN, INT_MAX, V|E|D, "chroma_sample_location_type"},
{"left",        "Left",        0, AV_OPT_TYPE_CONST, {.i64 = AVCHROMA_LOC_LEFT },        INT_MIN, INT_MAX, V|E|D, "chroma_sample_location_type"},
{"center",      "Center",      0, AV_OPT_TYPE_CONST, {.i64 = AVCHROMA_LOC_CENTER },      INT_MIN, INT_MAX, V|E|D, "chroma_sample_location_type"},
{"topleft",     "Top-left",    0, AV_OPT_TYPE_CONST, {.i64 = AVCHROMA_LOC_TOPLEFT },     INT_MIN, INT_MAX, V|E|D, "chroma_sample_location_type"},
{"top",         "Top",         0, AV_OPT_TYPE_CONST, {.i64 = AVCHROMA_LOC_TOP },         INT_MIN, INT_MAX, V|E|D, "chroma_sample_location_type"},
{"bottomleft",  "Bottom-left", 0, AV_OPT_TYPE_CONST, {.i64 = AVCHROMA_LOC_BOTTOMLEFT },  INT_MIN, INT_MAX, V|E|D, "chroma_sample_location_type"},
{"bottom",      "Bottom",      0, AV_OPT_TYPE_CONST, {.i64 = AVCHROMA_LOC_BOTTOM },      INT_MIN, INT_MAX, V|E|D, "chroma_sample_location_type"},
{"unspecified", "Unspecified", 0, AV_OPT_TYPE_CONST, {.i64 = AVCHROMA_LOC_UNSPECIFIED }, INT_MIN, INT_MAX, V|E|D, "chroma_sample_location_type"},
{"log_level_offset", "set the log level offset", OFFSET(log_level_offset), AV_OPT_TYPE_INT, {.i64 = 0 }, INT_MIN, INT_MAX },
{"slices", "set the number of slices, used in parallelized encoding", OFFSET(slices), AV_OPT_TYPE_INT, {.i64 = 0 }, 0, INT_MAX, V|E},
{"thread_type", "select multithreading type", OFFSET(thread_type), AV_OPT_TYPE_FLAGS, {.i64 = FF_THREAD_SLICE|FF_THREAD_FRAME }, 0, INT_MAX, V|A|E|D, "thread_type"},
{"slice", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_THREAD_SLICE }, INT_MIN, INT_MAX, V|E|D, "thread_type"},
{"frame", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_THREAD_FRAME }, INT_MIN, INT_MAX, V|E|D, "thread_type"},
{"audio_service_type", "audio service type", OFFSET(audio_service_type), AV_OPT_TYPE_INT, {.i64 = AV_AUDIO_SERVICE_TYPE_MAIN }, 0, AV_AUDIO_SERVICE_TYPE_NB-1, A|E, "audio_service_type"},
{"ma", "Main Audio Service", 0, AV_OPT_TYPE_CONST, {.i64 = AV_AUDIO_SERVICE_TYPE_MAIN },              INT_MIN, INT_MAX, A|E, "audio_service_type"},
{"ef", "Effects",            0, AV_OPT_TYPE_CONST, {.i64 = AV_AUDIO_SERVICE_TYPE_EFFECTS },           INT_MIN, INT_MAX, A|E, "audio_service_type"},
{"vi", "Visually Impaired",  0, AV_OPT_TYPE_CONST, {.i64 = AV_AUDIO_SERVICE_TYPE_VISUALLY_IMPAIRED }, INT_MIN, INT_MAX, A|E, "audio_service_type"},
{"hi", "Hearing Impaired",   0, AV_OPT_TYPE_CONST, {.i64 = AV_AUDIO_SERVICE_TYPE_HEARING_IMPAIRED },  INT_MIN, INT_MAX, A|E, "audio_service_type"},
{"di", "Dialogue",           0, AV_OPT_TYPE_CONST, {.i64 = AV_AUDIO_SERVICE_TYPE_DIALOGUE },          INT_MIN, INT_MAX, A|E, "audio_service_type"},
{"co", "Commentary",         0, AV_OPT_TYPE_CONST, {.i64 = AV_AUDIO_SERVICE_TYPE_COMMENTARY },        INT_MIN, INT_MAX, A|E, "audio_service_type"},
{"em", "Emergency",          0, AV_OPT_TYPE_CONST, {.i64 = AV_AUDIO_SERVICE_TYPE_EMERGENCY },         INT_MIN, INT_MAX, A|E, "audio_service_type"},
{"vo", "Voice Over",         0, AV_OPT_TYPE_CONST, {.i64 = AV_AUDIO_SERVICE_TYPE_VOICE_OVER },        INT_MIN, INT_MAX, A|E, "audio_service_type"},
{"ka", "Karaoke",            0, AV_OPT_TYPE_CONST, {.i64 = AV_AUDIO_SERVICE_TYPE_KARAOKE },           INT_MIN, INT_MAX, A|E, "audio_service_type"},
{"request_sample_fmt", "sample format audio decoders should prefer", OFFSET(request_sample_fmt), AV_OPT_TYPE_SAMPLE_FMT, {.i64=AV_SAMPLE_FMT_NONE}, -1, INT_MAX, A|D, "request_sample_fmt"},
{"pkt_timebase", NULL, OFFSET(pkt_timebase), AV_OPT_TYPE_RATIONAL, {.dbl = 0 }, 0, INT_MAX, 0},
{"sub_charenc", "set input text subtitles character encoding", OFFSET(sub_charenc), AV_OPT_TYPE_STRING, {.str = NULL}, CHAR_MIN, CHAR_MAX, S|D},
{"sub_charenc_mode", "set input text subtitles character encoding mode", OFFSET(sub_charenc_mode), AV_OPT_TYPE_FLAGS, {.i64 = FF_SUB_CHARENC_MODE_AUTOMATIC}, -1, INT_MAX, S|D, "sub_charenc_mode"},
{"do_nothing",  NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_SUB_CHARENC_MODE_DO_NOTHING},  INT_MIN, INT_MAX, S|D, "sub_charenc_mode"},
{"auto",        NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_SUB_CHARENC_MODE_AUTOMATIC},   INT_MIN, INT_MAX, S|D, "sub_charenc_mode"},
{"pre_decoder", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_SUB_CHARENC_MODE_PRE_DECODER}, INT_MIN, INT_MAX, S|D, "sub_charenc_mode"},
#if FF_API_ASS_TIMING
{"sub_text_format", "set decoded text subtitle format", OFFSET(sub_text_format), AV_OPT_TYPE_INT, {.i64 = FF_SUB_TEXT_FMT_ASS_WITH_TIMINGS}, 0, 1, S|D, "sub_text_format"},
#else
{"sub_text_format", "set decoded text subtitle format", OFFSET(sub_text_format), AV_OPT_TYPE_INT, {.i64 = FF_SUB_TEXT_FMT_ASS}, 0, 1, S|D, "sub_text_format"},
#endif
{"ass",              NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_SUB_TEXT_FMT_ASS},              INT_MIN, INT_MAX, S|D, "sub_text_format"},
#if FF_API_ASS_TIMING
{"ass_with_timings", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = FF_SUB_TEXT_FMT_ASS_WITH_TIMINGS}, INT_MIN, INT_MAX, S|D, "sub_text_format"},
#endif
{"refcounted_frames", NULL, OFFSET(refcounted_frames), AV_OPT_TYPE_BOOL, {.i64 = 0}, 0, 1, A|V|D },
#if FF_API_SIDEDATA_ONLY_PKT
{"side_data_only_packets", NULL, OFFSET(side_data_only_packets), AV_OPT_TYPE_BOOL, { .i64 = 1 }, 0, 1, A|V|E },
#endif
{"apply_cropping", NULL, OFFSET(apply_cropping), AV_OPT_TYPE_BOOL, { .i64 = 1 }, 0, 1, V | D },
{"skip_alpha", "Skip processing alpha", OFFSET(skip_alpha), AV_OPT_TYPE_BOOL, {.i64 = 0 }, 0, 1, V|D },
{"field_order", "Field order", OFFSET(field_order), AV_OPT_TYPE_INT, {.i64 = AV_FIELD_UNKNOWN }, 0, 5, V|D|E, "field_order" },
{"progressive", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = AV_FIELD_PROGRESSIVE }, 0, 0, V|D|E, "field_order" },
{"tt", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = AV_FIELD_TT }, 0, 0, V|D|E, "field_order" },
{"bb", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = AV_FIELD_BB }, 0, 0, V|D|E, "field_order" },
{"tb", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = AV_FIELD_TB }, 0, 0, V|D|E, "field_order" },
{"bt", NULL, 0, AV_OPT_TYPE_CONST, {.i64 = AV_FIELD_BT }, 0, 0, V|D|E, "field_order" },
{"dump_separator", "set information dump field separator", OFFSET(dump_separator), AV_OPT_TYPE_STRING, {.str = NULL}, CHAR_MIN, CHAR_MAX, A|V|S|D|E},
{"codec_whitelist", "List of decoders that are allowed to be used", OFFSET(codec_whitelist), AV_OPT_TYPE_STRING, { .str = NULL },  CHAR_MIN, CHAR_MAX, A|V|S|D },
{"pixel_format", "set pixel format", OFFSET(pix_fmt), AV_OPT_TYPE_PIXEL_FMT, {.i64=AV_PIX_FMT_NONE}, -1, INT_MAX, 0 },
{"video_size", "set video size", OFFSET(width), AV_OPT_TYPE_IMAGE_SIZE, {.str=NULL}, 0, INT_MAX, 0 },
{"max_pixels", "Maximum number of pixels", OFFSET(max_pixels), AV_OPT_TYPE_INT64, {.i64 = INT_MAX }, 0, INT_MAX, A|V|S|D|E },
{"hwaccel_flags", NULL, OFFSET(hwaccel_flags), AV_OPT_TYPE_FLAGS, {.i64 = AV_HWACCEL_FLAG_IGNORE_LEVEL }, 0, UINT_MAX, V|D, "hwaccel_flags"},
{"ignore_level", "ignore level even if the codec level used is unknown or higher than the maximum supported level reported by the hardware driver", 0, AV_OPT_TYPE_CONST, { .i64 = AV_HWACCEL_FLAG_IGNORE_LEVEL }, INT_MIN, INT_MAX, V | D, "hwaccel_flags" },
{"allow_high_depth", "allow to output YUV pixel formats with a different chroma sampling than 4:2:0 and/or other than 8 bits per component", 0, AV_OPT_TYPE_CONST, {.i64 = AV_HWACCEL_FLAG_ALLOW_HIGH_DEPTH }, INT_MIN, INT_MAX, V | D, "hwaccel_flags"},
{"allow_profile_mismatch", "attempt to decode anyway if HW accelerated decoder's supported profiles do not exactly match the stream", 0, AV_OPT_TYPE_CONST, {.i64 = AV_HWACCEL_FLAG_ALLOW_PROFILE_MISMATCH }, INT_MIN, INT_MAX, V | D, "hwaccel_flags"},
{NULL},
};
```

4) category 值为 OPT_CATEGORY_SWS 时， name 和 value 取值(extra/ffmpeg/libswscale/options.c)

```
static const AVOption swscale_options[] = {
    { "sws_flags",       "scaler flags",                  OFFSET(flags),     AV_OPT_TYPE_FLAGS,  { .i64  = SWS_BICUBIC        }, 0,      UINT_MAX,        VE, "sws_flags" },
    { "fast_bilinear",   "fast bilinear",                 0,                 AV_OPT_TYPE_CONST,  { .i64  = SWS_FAST_BILINEAR  }, INT_MIN, INT_MAX,        VE, "sws_flags" },
    { "bilinear",        "bilinear",                      0,                 AV_OPT_TYPE_CONST,  { .i64  = SWS_BILINEAR       }, INT_MIN, INT_MAX,        VE, "sws_flags" },
    { "bicubic",         "bicubic",                       0,                 AV_OPT_TYPE_CONST,  { .i64  = SWS_BICUBIC        }, INT_MIN, INT_MAX,        VE, "sws_flags" },
    { "experimental",    "experimental",                  0,                 AV_OPT_TYPE_CONST,  { .i64  = SWS_X              }, INT_MIN, INT_MAX,        VE, "sws_flags" },
    { "neighbor",        "nearest neighbor",              0,                 AV_OPT_TYPE_CONST,  { .i64  = SWS_POINT          }, INT_MIN, INT_MAX,        VE, "sws_flags" },
    { "area",            "averaging area",                0,                 AV_OPT_TYPE_CONST,  { .i64  = SWS_AREA           }, INT_MIN, INT_MAX,        VE, "sws_flags" },
    { "bicublin",        "luma bicubic, chroma bilinear", 0,                 AV_OPT_TYPE_CONST,  { .i64  = SWS_BICUBLIN       }, INT_MIN, INT_MAX,        VE, "sws_flags" },
    { "gauss",           "Gaussian",                      0,                 AV_OPT_TYPE_CONST,  { .i64  = SWS_GAUSS          }, INT_MIN, INT_MAX,        VE, "sws_flags" },
    { "sinc",            "sinc",                          0,                 AV_OPT_TYPE_CONST,  { .i64  = SWS_SINC           }, INT_MIN, INT_MAX,        VE, "sws_flags" },
    { "lanczos",         "Lanczos",                       0,                 AV_OPT_TYPE_CONST,  { .i64  = SWS_LANCZOS        }, INT_MIN, INT_MAX,        VE, "sws_flags" },
    { "spline",          "natural bicubic spline",        0,                 AV_OPT_TYPE_CONST,  { .i64  = SWS_SPLINE         }, INT_MIN, INT_MAX,        VE, "sws_flags" },
    { "print_info",      "print info",                    0,                 AV_OPT_TYPE_CONST,  { .i64  = SWS_PRINT_INFO     }, INT_MIN, INT_MAX,        VE, "sws_flags" },
    { "accurate_rnd",    "accurate rounding",             0,                 AV_OPT_TYPE_CONST,  { .i64  = SWS_ACCURATE_RND   }, INT_MIN, INT_MAX,        VE, "sws_flags" },
    { "full_chroma_int", "full chroma interpolation",     0,                 AV_OPT_TYPE_CONST,  { .i64  = SWS_FULL_CHR_H_INT }, INT_MIN, INT_MAX,        VE, "sws_flags" },
    { "full_chroma_inp", "full chroma input",             0,                 AV_OPT_TYPE_CONST,  { .i64  = SWS_FULL_CHR_H_INP }, INT_MIN, INT_MAX,        VE, "sws_flags" },
    { "bitexact",        "",                              0,                 AV_OPT_TYPE_CONST,  { .i64  = SWS_BITEXACT       }, INT_MIN, INT_MAX,        VE, "sws_flags" },
    { "error_diffusion", "error diffusion dither",        0,                 AV_OPT_TYPE_CONST,  { .i64  = SWS_ERROR_DIFFUSION}, INT_MIN, INT_MAX,        VE, "sws_flags" },

    { "srcw",            "source width",                  OFFSET(srcW),      AV_OPT_TYPE_INT,    { .i64 = 16                 }, 1,       INT_MAX,        VE },
    { "srch",            "source height",                 OFFSET(srcH),      AV_OPT_TYPE_INT,    { .i64 = 16                 }, 1,       INT_MAX,        VE },
    { "dstw",            "destination width",             OFFSET(dstW),      AV_OPT_TYPE_INT,    { .i64 = 16                 }, 1,       INT_MAX,        VE },
    { "dsth",            "destination height",            OFFSET(dstH),      AV_OPT_TYPE_INT,    { .i64 = 16                 }, 1,       INT_MAX,        VE },
    { "src_format",      "source format",                 OFFSET(srcFormat), AV_OPT_TYPE_PIXEL_FMT,{ .i64 = DEFAULT          }, 0,       INT_MAX, VE },
    { "dst_format",      "destination format",            OFFSET(dstFormat), AV_OPT_TYPE_PIXEL_FMT,{ .i64 = DEFAULT          }, 0,       INT_MAX, VE },
    { "src_range",       "source is full range",          OFFSET(srcRange),  AV_OPT_TYPE_BOOL,   { .i64 = DEFAULT            }, 0,       1,              VE },
    { "dst_range",       "destination is full range",     OFFSET(dstRange),  AV_OPT_TYPE_BOOL,   { .i64 = DEFAULT            }, 0,       1,              VE },
    { "param0",          "scaler param 0",                OFFSET(param[0]),  AV_OPT_TYPE_DOUBLE, { .dbl = SWS_PARAM_DEFAULT  }, INT_MIN, INT_MAX,        VE },
    { "param1",          "scaler param 1",                OFFSET(param[1]),  AV_OPT_TYPE_DOUBLE, { .dbl = SWS_PARAM_DEFAULT  }, INT_MIN, INT_MAX,        VE },

    { "src_v_chr_pos",   "source vertical chroma position in luma grid/256"  ,      OFFSET(src_v_chr_pos), AV_OPT_TYPE_INT, { .i64 = -513 }, -513,      512,             VE },
    { "src_h_chr_pos",   "source horizontal chroma position in luma grid/256",      OFFSET(src_h_chr_pos), AV_OPT_TYPE_INT, { .i64 = -513 }, -513,      512,             VE },
    { "dst_v_chr_pos",   "destination vertical chroma position in luma grid/256"  , OFFSET(dst_v_chr_pos), AV_OPT_TYPE_INT, { .i64 = -513 }, -513,      512,             VE },
    { "dst_h_chr_pos",   "destination horizontal chroma position in luma grid/256", OFFSET(dst_h_chr_pos), AV_OPT_TYPE_INT, { .i64 = -513 }, -513,      512,             VE },

    { "sws_dither",      "set dithering algorithm",       OFFSET(dither),    AV_OPT_TYPE_INT,    { .i64  = SWS_DITHER_AUTO    }, 0,       NB_SWS_DITHER,  VE, "sws_dither" },
    { "auto",            "leave choice to sws",           0,                 AV_OPT_TYPE_CONST,  { .i64  = SWS_DITHER_AUTO    }, INT_MIN, INT_MAX,        VE, "sws_dither" },
    { "bayer",           "bayer dither",                  0,                 AV_OPT_TYPE_CONST,  { .i64  = SWS_DITHER_BAYER   }, INT_MIN, INT_MAX,        VE, "sws_dither" },
    { "ed",              "error diffusion",               0,                 AV_OPT_TYPE_CONST,  { .i64  = SWS_DITHER_ED      }, INT_MIN, INT_MAX,        VE, "sws_dither" },
    { "a_dither",        "arithmetic addition dither",    0,                 AV_OPT_TYPE_CONST,  { .i64  = SWS_DITHER_A_DITHER}, INT_MIN, INT_MAX,        VE, "sws_dither" },
    { "x_dither",        "arithmetic xor dither",         0,                 AV_OPT_TYPE_CONST,  { .i64  = SWS_DITHER_X_DITHER}, INT_MIN, INT_MAX,        VE, "sws_dither" },
    { "gamma",           "gamma correct scaling",         OFFSET(gamma_flag),AV_OPT_TYPE_BOOL,   { .i64  = 0                  }, 0,       1,              VE },
    { "alphablend",      "mode for alpha -> non alpha",   OFFSET(alphablend),AV_OPT_TYPE_INT,    { .i64  = SWS_ALPHA_BLEND_NONE}, 0,       SWS_ALPHA_BLEND_NB-1, VE, "alphablend" },
    { "none",            "ignore alpha",                  0,                 AV_OPT_TYPE_CONST,  { .i64  = SWS_ALPHA_BLEND_NONE}, INT_MIN, INT_MAX,       VE, "alphablend" },
    { "uniform_color",   "blend onto a uniform color",    0,                 AV_OPT_TYPE_CONST,  { .i64  = SWS_ALPHA_BLEND_UNIFORM},INT_MIN, INT_MAX,     VE, "alphablend" },
    { "checkerboard",    "blend onto a checkerboard",     0,                 AV_OPT_TYPE_CONST,  { .i64  = SWS_ALPHA_BLEND_CHECKERBOARD},INT_MIN, INT_MAX,     VE, "alphablend" },

    { NULL }
};
```

5) category 值为 OPT_CATEGORY_PLAYER 时， name 和 value 取值(ijkmedia/ijkplayer/ff_ffplay_options.h)

```
static const AVOption ffp_context_options[] = {
    // original options in ffplay.c
    // FFP_MERGE: x, y, s, fs
    { "an",                             "disable audio",
        OPTION_OFFSET(audio_disable),   OPTION_INT(0, 0, 1) },
    { "vn",                             "disable video",
        OPTION_OFFSET(video_disable),   OPTION_INT(0, 0, 1) },
    // FFP_MERGE: sn, ast, vst, sst
    // TODO: ss
    { "nodisp",                         "disable graphical display",
        OPTION_OFFSET(display_disable), OPTION_INT(0, 0, 1) },
    { "volume",                         "set startup volume 0=min 100=max",
        OPTION_OFFSET(startup_volume),   OPTION_INT(100, 0, 100) },
    // FFP_MERGE: f, pix_fmt, stats
    { "fast",                           "non spec compliant optimizations",
        OPTION_OFFSET(fast),            OPTION_INT(0, 0, 1) },
    // FFP_MERGE: genpts, drp, lowres, sync, autoexit, exitonkeydown, exitonmousedown
    { "loop",                           "set number of times the playback shall be looped",
        OPTION_OFFSET(loop),            OPTION_INT(1, INT_MIN, INT_MAX) },
    { "infbuf",                         "don't limit the input buffer size (useful with realtime streams)",
        OPTION_OFFSET(infinite_buffer), OPTION_INT(0, 0, 1) },
    { "framedrop",                      "drop frames when cpu is too slow",
        OPTION_OFFSET(framedrop),       OPTION_INT(0, -1, 120) },
    { "seek-at-start",                  "set offset of player should be seeked",
        OPTION_OFFSET(seek_at_start),       OPTION_INT64(0, 0, INT_MAX) },
    { "subtitle",                       "decode subtitle stream",
        OPTION_OFFSET(subtitle),        OPTION_INT(0, 0, 1) },
    // FFP_MERGE: window_title
#if CONFIG_AVFILTER
    { "af",                             "audio filters",
        OPTION_OFFSET(afilters),        OPTION_STR(NULL) },
    { "vf0",                            "video filters 0",
        OPTION_OFFSET(vfilter0),        OPTION_STR(NULL) },
#endif
    { "rdftspeed",                      "rdft speed, in msecs",
        OPTION_OFFSET(rdftspeed),       OPTION_INT(0, 0, INT_MAX) },
    // FFP_MERGE: showmode, default, i, codec, acodec, scodec, vcodec
    // TODO: autorotate

    { "find_stream_info",               "read and decode the streams to fill missing information with heuristics" ,
        OPTION_OFFSET(find_stream_info),    OPTION_INT(1, 0, 1) },

    // extended options in ff_ffplay.c
    { "max-fps",                        "drop frames in video whose fps is greater than max-fps",
        OPTION_OFFSET(max_fps),         OPTION_INT(31, -1, 121) },

    { "overlay-format",                 "fourcc of overlay format",
        OPTION_OFFSET(overlay_format),  OPTION_INT(SDL_FCC_RV32, INT_MIN, INT_MAX),
        .unit = "overlay-format" },
    { "fcc-_es2",                       "", 0, OPTION_CONST(SDL_FCC__GLES2), .unit = "overlay-format" },
    { "fcc-i420",                       "", 0, OPTION_CONST(SDL_FCC_I420), .unit = "overlay-format" },
    { "fcc-yv12",                       "", 0, OPTION_CONST(SDL_FCC_YV12), .unit = "overlay-format" },
    { "fcc-rv16",                       "", 0, OPTION_CONST(SDL_FCC_RV16), .unit = "overlay-format" },
    { "fcc-rv24",                       "", 0, OPTION_CONST(SDL_FCC_RV24), .unit = "overlay-format" },
    { "fcc-rv32",                       "", 0, OPTION_CONST(SDL_FCC_RV32), .unit = "overlay-format" },

    { "start-on-prepared",                  "automatically start playing on prepared",
        OPTION_OFFSET(start_on_prepared),   OPTION_INT(1, 0, 1) },

    { "video-pictq-size",                   "max picture queue frame count",
        OPTION_OFFSET(pictq_size),          OPTION_INT(VIDEO_PICTURE_QUEUE_SIZE_DEFAULT,
                                                       VIDEO_PICTURE_QUEUE_SIZE_MIN,
                                                       VIDEO_PICTURE_QUEUE_SIZE_MAX) },

    { "max-buffer-size",                    "max buffer size should be pre-read",
        OPTION_OFFSET(dcc.max_buffer_size), OPTION_INT(MAX_QUEUE_SIZE, 0, MAX_QUEUE_SIZE) },
    { "min-frames",                         "minimal frames to stop pre-reading",
        OPTION_OFFSET(dcc.min_frames),      OPTION_INT(DEFAULT_MIN_FRAMES, MIN_MIN_FRAMES, MAX_MIN_FRAMES) },
    { "first-high-water-mark-ms",           "first chance to wakeup read_thread",
        OPTION_OFFSET(dcc.first_high_water_mark_in_ms),
        OPTION_INT(DEFAULT_FIRST_HIGH_WATER_MARK_IN_MS,
                   DEFAULT_FIRST_HIGH_WATER_MARK_IN_MS,
                   DEFAULT_LAST_HIGH_WATER_MARK_IN_MS) },
    { "next-high-water-mark-ms",            "second chance to wakeup read_thread",
        OPTION_OFFSET(dcc.next_high_water_mark_in_ms),
        OPTION_INT(DEFAULT_NEXT_HIGH_WATER_MARK_IN_MS,
                   DEFAULT_FIRST_HIGH_WATER_MARK_IN_MS,
                   DEFAULT_LAST_HIGH_WATER_MARK_IN_MS) },
    { "last-high-water-mark-ms",            "last chance to wakeup read_thread",
        OPTION_OFFSET(dcc.last_high_water_mark_in_ms),
        OPTION_INT(DEFAULT_LAST_HIGH_WATER_MARK_IN_MS,
                   DEFAULT_FIRST_HIGH_WATER_MARK_IN_MS,
                   DEFAULT_LAST_HIGH_WATER_MARK_IN_MS) },

    { "packet-buffering",                   "pause output until enough packets have been read after stalling",
        OPTION_OFFSET(packet_buffering),    OPTION_INT(1, 0, 1) },
    { "sync-av-start",                      "synchronise a/v start time",
        OPTION_OFFSET(sync_av_start),       OPTION_INT(1, 0, 1) },
    { "iformat",                            "force format",
        OPTION_OFFSET(iformat_name),        OPTION_STR(NULL) },
    { "no-time-adjust",                     "return player's real time from the media stream instead of the adjusted time",
        OPTION_OFFSET(no_time_adjust),      OPTION_INT(0, 0, 1) },
    { "preset-5-1-center-mix-level",        "preset center-mix-level for 5.1 channel",
        OPTION_OFFSET(preset_5_1_center_mix_level), OPTION_DOUBLE(M_SQRT1_2, -32, 32) },

    { "enable-accurate-seek",                      "enable accurate seek",
        OPTION_OFFSET(enable_accurate_seek),       OPTION_INT(0, 0, 1) },
    { "accurate-seek-timeout",                      "accurate seek timeout",
        OPTION_OFFSET(accurate_seek_timeout),       OPTION_INT(MAX_ACCURATE_SEEK_TIMEOUT, 0, MAX_ACCURATE_SEEK_TIMEOUT) },
    { "skip-calc-frame-rate",                      "don't calculate real frame rate",
        OPTION_OFFSET(skip_calc_frame_rate),       OPTION_INT(0, 0, 1) },
    { "get-frame-mode",                      "warning, this option only for get frame",
        OPTION_OFFSET(get_frame_mode),       OPTION_INT(0, 0, 1) },
    { "async-init-decoder",                  "async create decoder",
        OPTION_OFFSET(async_init_decoder),   OPTION_INT(0, 0, 1) },
    { "video-mime-type",                    "default video mime type",
        OPTION_OFFSET(video_mime_type),     OPTION_STR(NULL) },

        // iOS only options
    { "videotoolbox",                       "VideoToolbox: enable",
        OPTION_OFFSET(videotoolbox),        OPTION_INT(0, 0, 1) },
    { "videotoolbox-max-frame-width",       "VideoToolbox: max width of output frame",
        OPTION_OFFSET(vtb_max_frame_width), OPTION_INT(0, 0, INT_MAX) },
    { "videotoolbox-async",                 "VideoToolbox: use kVTDecodeFrame_EnableAsynchronousDecompression()",
        OPTION_OFFSET(vtb_async),           OPTION_INT(0, 0, 1) },
    { "videotoolbox-wait-async",            "VideoToolbox: call VTDecompressionSessionWaitForAsynchronousFrames()",
        OPTION_OFFSET(vtb_wait_async),      OPTION_INT(1, 0, 1) },
    { "videotoolbox-handle-resolution-change",          "VideoToolbox: handle resolution change automatically",
        OPTION_OFFSET(vtb_handle_resolution_change),    OPTION_INT(0, 0, 1) },

    // Android only options
    { "mediacodec",                             "MediaCodec: enable H264 (deprecated by 'mediacodec-avc')",
        OPTION_OFFSET(mediacodec_avc),          OPTION_INT(0, 0, 1) },
    { "mediacodec-auto-rotate",                 "MediaCodec: auto rotate frame depending on meta",
        OPTION_OFFSET(mediacodec_auto_rotate),  OPTION_INT(0, 0, 1) },
    { "mediacodec-all-videos",                  "MediaCodec: enable all videos",
        OPTION_OFFSET(mediacodec_all_videos),   OPTION_INT(0, 0, 1) },
    { "mediacodec-avc",                         "MediaCodec: enable H264",
        OPTION_OFFSET(mediacodec_avc),          OPTION_INT(0, 0, 1) },
    { "mediacodec-hevc",                        "MediaCodec: enable HEVC",
        OPTION_OFFSET(mediacodec_hevc),         OPTION_INT(0, 0, 1) },
    { "mediacodec-mpeg2",                       "MediaCodec: enable MPEG2VIDEO",
        OPTION_OFFSET(mediacodec_mpeg2),        OPTION_INT(0, 0, 1) },
    { "mediacodec-mpeg4",                       "MediaCodec: enable MPEG4",
        OPTION_OFFSET(mediacodec_mpeg4),        OPTION_INT(0, 0, 1) },
    { "mediacodec-handle-resolution-change",                    "MediaCodec: handle resolution change automatically",
        OPTION_OFFSET(mediacodec_handle_resolution_change),     OPTION_INT(0, 0, 1) },
    { "opensles",                           "OpenSL ES: enable",
        OPTION_OFFSET(opensles),            OPTION_INT(0, 0, 1) },
    { "soundtouch",                           "SoundTouch: enable",
        OPTION_OFFSET(soundtouch_enable),            OPTION_INT(0, 0, 1) },
    { "mediacodec-sync",                 "mediacodec: use msg_queue for synchronise",
        OPTION_OFFSET(mediacodec_sync),           OPTION_INT(0, 0, 1) },
    { "mediacodec-default-name",          "mediacodec default name",
        OPTION_OFFSET(mediacodec_default_name),      OPTION_STR(NULL) },
    { "ijkmeta-delay-init",          "ijkmeta delay init",
        OPTION_OFFSET(ijkmeta_delay_init),      OPTION_INT(0, 0, 1) },
    { "render-wait-start",          "render wait start",
        OPTION_OFFSET(render_wait_start),      OPTION_INT(0, 0, 1) },

    { NULL }
};
```
