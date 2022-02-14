## 依赖方法


#### 7.0 版本开始使用了anndroidx，support版本请看6.x.x，请查看：[--- 版本更新说明 - 入口](https://github.com/CarGuo/GSYVideoPlayer/blob/master/doc/UPDATE_VERSION.md)。

JitPack引入方法

#### First、在project下的build.gradle添加
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
        maven { url "https://maven.aliyun.com/repository/public" }
	}
}
```

```
 //完整版引入
 implementation 'com.github.CarGuo.GSYVideoPlayer:GSYVideoPlayer:v8.1.7-jitpack'
```

#### B、添加java和你想要的so支持：

```
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-java:v8.1.9-release-jitpack'

 //是否需要ExoPlayer模式
 implementation 'com.github.CarGuo.GSYVideoPlayer:GSYVideoPlayer-exo2:v8.1.9-release-jitpack'

 //根据你的需求ijk模式的so
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-arm64:v8.1.9-release-jitpack'
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-armv7a:v8.1.9-release-jitpack'
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-armv5:v8.1.9-release-jitpack'
 implementation 'com.github.CarGuo.GSYVideoPlayer:GSYVideoPlayer-x86:v8.1.9-release-jitpack'
 implementation 'com.github.CarGuo.GSYVideoPlayer:GSYVideoPlayer-x64:v8.1.9-release-jitpack'
```

#### C、支持其他格式协议的（mpeg，rtsp, concat、crypto协议）

A、B普通版本支持263/264/265等，对于mpeg编码会有声音无画面情况。
C 引入的so支持mpeg编码和其他补充协议，但是so包相对变大。

```
 implementation 'com.github.CarGuo.GSYVideoPlayer:GSYVideoPlayer-java:v8.1.9-release-jitpack'

 //是否需要ExoPlayer模式
 implementation 'com.github.CarGuo.GSYVideoPlayer:GSYVideoPlayer-exo2:v8.1.9-release-jitpack'

 //更多ijk的编码支持
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-ex_so:v8.1.9-release-jitpack'

```

--------------------------------------------------------------------------------
--------------------------------------------------------------------------------

### 1、JCenter 引入方法

**你可以选择下面三种的其中一种，在module下的build.gradle添加。**

#### A、直接引入
```
//完整版引入
implementation 'com.shuyu:GSYVideoPlayer:8.1.2'

```

#### B、添加java和你想要的so支持：

```
implementation 'com.shuyu:gsyVideoPlayer-java:8.1.2'

//是否需要ExoPlayer模式
implementation 'com.shuyu:GSYVideoPlayer-exo2:8.1.2'

//根据你的需求ijk模式的so
implementation 'com.shuyu:gsyVideoPlayer-armv5:8.1.2'
implementation 'com.shuyu:gsyVideoPlayer-armv7a:8.1.2'
implementation 'com.shuyu:gsyVideoPlayer-arm64:8.1.2'
implementation 'com.shuyu:gsyVideoPlayer-x64:8.1.2'
implementation 'com.shuyu:gsyVideoPlayer-x86:8.1.2'

```

#### C、支持其他格式协议的（mpeg，rtsp, concat、crypto协议）

A、B普通版本支持263/264/265等，对于mpeg编码会有声音无画面情况。
C 引入的so支持mpeg编码和其他补充协议，但是so包相对变大。

```
implementation 'com.shuyu:gsyVideoPlayer-java:8.1.2'

//是否需要ExoPlayer模式
implementatcon 'com.shuyu:GSYVideoPlayer-exo2:8.1.2'

//更多ijk的编码支持
implementation 'com.shuyu:gsyVideoPlayer-ex_so:8.1.2'

```
