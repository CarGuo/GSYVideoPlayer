## 依赖方法

### 1、JCenter 引入方法（推荐）

**你可以选择下面三种的其中一种，在module下的build.gradle添加。**

#### A、直接引入
```
//完整版引入
compile 'com.shuyu:GSYVideoPlayer:2.1.0'

```

#### B、添加java和你想要的so支持：

```
compile 'com.shuyu:gsyVideoPlayer-java:2.1.0'

//根据你的需求
compile 'com.shuyu:gsyVideoPlayer-armv5:2.1.0'
compile 'com.shuyu:gsyVideoPlayer-armv7a:2.1.0'
compile 'com.shuyu:gsyVideoPlayer-arm64:2.1.0'
compile 'com.shuyu:gsyVideoPlayer-x64:2.1.0'
compile 'com.shuyu:gsyVideoPlayer-x86:2.1.0'

```

#### C、支持其他格式协议的（mpeg，rtsp, concat、crypto协议）

A、B普通版本支持263/264/265等，对于mpeg编码会有声音无画面情况。
C 引入的so支持mpeg编码和其他补充协议，但是so包相对变大。
 
```
compile 'com.shuyu:gsyVideoPlayer-java:2.1.0' 

compile 'com.shuyu:gsyVideoPlayer-ex_so:2.1.0' 

```

#### D、支持使用ijkPlayer的so

```
compile 'tv.danmaku.ijk.media:ijkplayer-armv7a:0.8.3'
compile 'tv.danmaku.ijk.media:ijkplayer-armv5:0.8.3'
compile 'tv.danmaku.ijk.media:ijkplayer-arm64:0.8.3'
compile 'tv.danmaku.ijk.media:ijkplayer-x86:0.8.3'
compile 'tv.danmaku.ijk.media:ijkplayer-x86_64:0.8.3'
```

--------------------------------------------------------------------------------

### 2、JitPack引入方法

#### First、在project下的build.gradle添加
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

#### Sencond、在module下的build.gradle添加

**你可以选择下面三种的其中一种，在module下的build.gradle添加。**

#### A、直接引入
```
//完整版引入
compile 'com.github.CarGuo.GSYVideoPlayer:GSYVideoPlayer:v2.1.0'

```

#### B、添加java和你想要的so支持：

```

compile 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-java:v2.1.0'

//根据你的需求
compile 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-x64:v2.1.0'
compile 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-x86:v2.1.0'
compile 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-arm64:v2.1.0'
compile 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-armv5:v2.1.0'
compile 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-armv7a:v2.1.0'

```

#### C、支持其他格式协议的（mpeg，rtsp, concat、crypto协议）

A、B普通版本支持263/264/265等，对于mpeg编码会有声音无画面情况。
C 方法引入的so支持mpeg编码和其他补充协议，但是so包相对变大。
 
```
compile 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-java:v2.1.0'

compile 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-ex_so:v2.1.0'

```
