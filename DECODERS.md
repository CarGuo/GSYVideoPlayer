
如果出现有声音没画面，或者有画面没声音的异常情况，请先了解以下内容。（DEMO中有些列表开启了静音模式，属于正常情况）

简单来说，mp4并不是视频编码，可以称为视频容器，而H264/H263等这样才是视频编码，AAC为音频编码等。
对于视频相关的，推荐雷宵骅的视频基础：[视音频编解码技术零基础学习方法](http://blog.csdn.net/leixiaohua1020/article/details/18893769)，这里你可以了解到视频和音频相关编码和协议的东西。


项目默认支持的视频编码和音频编码有如下，更多详细配置可查看[编译配置文件](https://github.com/CarGuo/GSYVideoPlayer/blob/master/module-lite-hevc.sh)。

```
 --enable-decoder=aac"
 --enable-decoder=aac_latm"
 --enable-decoder=flv"
 --enable-decoder=h263"
 --enable-decoder=h263i"
 --enable-decoder=h263p"
 --enable-decoder=h264"
 --enable-decoder=mp3*"
 --enable-decoder=vp6"
 --enable-decoder=vp6a"
 --enable-decoder=vp6f"
 --enable-decoder=hevc"

```

