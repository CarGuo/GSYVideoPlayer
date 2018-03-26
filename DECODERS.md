
### 项目是基于ijkplayer编译的so，ijkplayer基于FFPMEG

---------

#### 出于so大小考虑，普通编译so只支持了常用的视频编码，如果需要支持额外类型，可依赖ex_so，如果依旧不满足，可重新编译ijkplayer源码，配置module.sh然后编译so，替换现在项目中的so，注意so的版本要和ijk的java版本一致。编译流程可参考首页编译https的so流程。

---------

#### 说明：如果出现有声音没画面，或者有画面没声音的异常情况，请先了解以下内容。（DEMO中有些列表开启了静音模式，属于正常情况）**

简单来说，mp4并不是视频编码，可以称为视频容器，而H264/H263等这样才是视频编码，AAC为音频编码等。

对于视频相关的，推荐雷宵骅的视频基础：[视音频编解码技术零基础学习方法](http://blog.csdn.net/leixiaohua1020/article/details/18893769)，这里你可以了解到视频和音频相关编码和协议的东西。


项目普通so默认支持的视频编码和音频编码配置可查看[编译配置文件](https://github.com/CarGuo/GSYVideoPlayer/blob/master/module-lite.sh)。

ex_so支持的视频编码和音频编码配置可查看[编译配置文件](https://github.com/CarGuo/GSYVideoPlayer/blob/master/module-lite-more.sh)。

ex_so多支持了mepg、concat协议，crypto协议。



