## 依赖方法

### 1、JCenter 引入方法（推荐）

**你可以选择下面三种的其中一种，在module下的build.gradle添加。**

#### A、直接引入
```
//完整版引入
implementation 'com.shuyu:GSYVideoPlayer:6.0.3'

```

#### B、添加java和你想要的so支持：

```
implementation 'com.shuyu:gsyVideoPlayer-java:6.0.3'

//是否需要ExoPlayer模式
implementation 'com.shuyu:GSYVideoPlayer-exo2:6.0.3'

//根据你的需求ijk模式的so
implementation 'com.shuyu:gsyVideoPlayer-armv5:6.0.3'
implementation 'com.shuyu:gsyVideoPlayer-armv7a:6.0.3'
implementation 'com.shuyu:gsyVideoPlayer-arm64:6.0.3'
implementation 'com.shuyu:gsyVideoPlayer-x64:6.0.3'
implementation 'com.shuyu:gsyVideoPlayer-x86:6.0.3'

```

#### C、支持其他格式协议的（mpeg，rtsp, concat、crypto协议）

A、B普通版本支持263/264/265等，对于mpeg编码会有声音无画面情况。
C 引入的so支持mpeg编码和其他补充协议，但是so包相对变大。
 
```
implementation 'com.shuyu:gsyVideoPlayer-java:6.0.3'

//是否需要ExoPlayer模式
implementation 'com.shuyu:GSYVideoPlayer-exo2:6.0.3'

//更多ijk的编码支持
implementation 'com.shuyu:gsyVideoPlayer-ex_so:6.0.3'

```

--------------------------------------------------------------------------------

### 2、JitPack引入方法

Not Support after 6.x.x 