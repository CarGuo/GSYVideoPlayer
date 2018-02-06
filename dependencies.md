## 依赖方法

### 1、JCenter 引入方法（推荐）

**你可以选择下面三种的其中一种，在module下的build.gradle添加。**

#### A、直接引入
```
//完整版引入
compile 'com.shuyu:GSYVideoPlayer:4.0.0-beat1'

```

#### B、添加java和你想要的so支持：

```
compile 'com.shuyu:gsyVideoPlayer-java:4.0.0-beat1'

//根据你的需求
compile 'com.shuyu:gsyVideoPlayer-armv5:4.0.0-beat1'
compile 'com.shuyu:gsyVideoPlayer-armv7a:4.0.0-beat1'
compile 'com.shuyu:gsyVideoPlayer-arm64:4.0.0-beat1'
compile 'com.shuyu:gsyVideoPlayer-x64:4.0.0-beat1'
compile 'com.shuyu:gsyVideoPlayer-x86:4.0.0-beat1'

```

#### C、支持其他格式协议的（mpeg，rtsp, concat、crypto协议）

A、B普通版本支持263/264/265等，对于mpeg编码会有声音无画面情况。
C 引入的so支持mpeg编码和其他补充协议，但是so包相对变大。
 
```
compile 'com.shuyu:gsyVideoPlayer-java:4.0.0-beat1'

compile 'com.shuyu:gsyVideoPlayer-ex_so:4.0.0-beat1'

```

#### D、支持使用ijkPlayer的so

```
compile 'tv.danmaku.ijk.media:ijkplayer-armv7a:0.8.4'
compile 'tv.danmaku.ijk.media:ijkplayer-armv5:0.8.4'
compile 'tv.danmaku.ijk.media:ijkplayer-arm64:0.8.3'
compile 'tv.danmaku.ijk.media:ijkplayer-x86:0.8.4'
compile 'tv.danmaku.ijk.media:ijkplayer-x86_64:0.8.4'
```

--------------------------------------------------------------------------------

### 2、JitPack引入方法 (4.0.0-beat1开始目前不支持)

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
compile 'com.github.CarGuo.GSYVideoPlayer:GSYVideoPlayer:v4.0.0-beat1'

```

#### B、添加java和你想要的so支持：

```

compile 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-java:v4.0.0-beat1'

//根据你的需求
compile 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-x64:v4.0.0-beat1'
compile 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-x86:v4.0.0-beat1'
compile 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-arm64:v4.0.0-beat1'
compile 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-armv5:v4.0.0-beat1'
compile 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-armv7a:v4.0.0-beat1'

```

#### C、支持其他格式协议的（mpeg，rtsp, concat、crypto协议）

A、B普通版本支持263/264/265等，对于mpeg编码会有声音无画面情况。
C 方法引入的so支持mpeg编码和其他补充协议，但是so包相对变大。
 
```
compile 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-java:v4.0.0-beat1'

compile 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-ex_so:v4.0.0-beat1'

```
