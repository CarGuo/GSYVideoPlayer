# 托管方式

目前有三种托管方式：

- MavenCentral : 11.0.0 版本以后才有，所有基类包都发布托管在这里
- Github Package ： 9.1.0 版本开始才有， 但是 11.0.0 之前， GSYIjkJava 的基础依赖还是在托管在 jitpack
- Jitpack IO ：依然会发布，但是存在托管平台随机丢包

### 1、mavenCentral 引用(推荐)

由于 jitpack 不断丢包，目前已迁移至 MavenCentral，使用方式如下：

#### First 添加

```groovy
allprojects {
    repositories {
        ///...
        mavenCentral()
        maven { url "https://maven.aliyun.com/repository/public" }
    }
}
```

**你可以选择下面三种的其中一种，在module下的build.gradle添加。**

#### A、直接引入

```groovy
 //完整版引入

implementation 'io.github.carguo:gsyvideoplayer:11.2.0'


//是否需要AliPlayer模式
implementation 'io.github.carguo:gsyvideoplayer-aliplay:11.2.0'
```

#### B、添加java和你想要的so支持：

```groovy
 implementation 'io.github.carguo:gsyvideoplayer-java:11.2.0'

 //是否需要ExoPlayer模式
 implementation 'io.github.carguo:gsyvideoplayer-exo2:11.2.0'

 //是否需要AliPlayer模式
 implementation 'io.github.carguo:gsyvideoplayer-aliplay:11.2.0'

 //根据你的需求ijk模式的so
 implementation 'io.github.carguo:gsyvideoplayer-arm64:11.2.0'
 implementation 'io.github.carguo:gsyvideoplayer-armv7a:11.2.0'
 implementation 'io.github.carguo:gsyvideoplayer-armv5:11.2.0'
 implementation 'io.github.carguo:gsyvideoplayer-x86:11.2.0'
 implementation 'io.github.carguo:gsyvideoplayer-x64:11.2.0'
```

#### C、支持其他格式协议的（mpeg，rtsp, concat、crypto协议，支持 16k Page Size）

A、B普通版本支持263/264/265等，对于mpeg编码会有声音无画面情况。
C 引入的so支持mpeg编码和其他补充协议，但是so包相对变大。

```groovy
 implementation 'io.github.carguo:gsyvideoplayer-java:11.2.0'

 //是否需要ExoPlayer模式
 implementation 'io.github.carguo:gsyvideoplayer-exo2:11.2.0'

 //是否需要AliPlayer模式
 implementation 'io.github.carguo:gsyvideoplayer-aliplay:11.2.0'

 //更多ijk的编码支持
 implementation 'io.github.carguo:gsyvideoplayer-ex_so:11.2.0'

```

### 2、Github Package 依赖方式(推荐)

**由于 Jitpack 经常存在历史包随机丢失问题，所以新增 Github Package 依赖方式，使用方式如下**：

> 不过 github package 访问需要 token 去 access 比较麻烦，但是胜在稳定

```groovy
allprojects {
    repositories {
		//...
        maven {
            url 'https://maven.pkg.github.com/CarGuo/GSYVideoPlayer'

            // You can also use your own GitHub account and token
            // For convenience, I have provided a token for an infrequently used account here
            credentials {
                // your github name
                username = 'carsmallguo'
                // your github generate new token
                password = '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'
            }
        }
        maven {
            url "https://maven.aliyun.com/repository/public"
        }
        mavenCentral()
    }
}
```

- 生成自己 token
  的方式可见：https://docs.github.com/zh/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens

> 理论上就是右上角头像 - Settings - Developer Settings - Personal access tokens - tokens (classic) -
> Generate new token（classic）- read:packages
> 记得过期时间选择永久

**你可以选择下面三种的其中一种，在module下的build.gradle添加。**

#### A、直接引入

```groovy
 //完整版引入
 implementation 'com.shuyu:gsyvideoplayer:11.2.0'


 //是否需要AliPlayer模式
 implementation 'com.shuyu:gsyvideoplayer-aliplay:11.2.0'
```

#### B、添加java和你想要的so支持：

```groovy
 implementation 'com.shuyu:gsyvideoplayer-java:11.2.0'

 //是否需要ExoPlayer模式
 implementation 'com.shuyu:gsyvideoplayer-exo2:11.2.0'

 //是否需要AliPlayer模式
 implementation 'com.shuyu:gsyvideoplayer-aliplay:11.2.0'

 //根据你的需求ijk模式的so
 implementation 'com.shuyu:gsyvideoplayer-armv5:11.2.0'
 implementation 'com.shuyu:gsyvideoplayer-armv7a:11.2.0'
 implementation 'com.shuyu:gsyvideoplayer-arm64:11.2.0'
 implementation 'com.shuyu:gsyvideoplayer-x86:11.2.0'
 implementation 'com.shuyu:gsyvideoplayer-x64:11.2.0'
```

#### C、支持其他格式协议的（mpeg，rtsp, concat、crypto协议，支持 16k Page Size）

A、B普通版本支持263/264/265等，对于mpeg编码会有声音无画面情况。
C 引入的so支持mpeg编码和其他补充协议，但是so包相对变大。

```groovy
 implementation 'com.shuyu:gsyvideoplayer-java:11.2.0'

 //是否需要ExoPlayer模式
 implementation 'com.shuyu:gsyvideoplayer-exo2:11.2.0'


 //是否需要AliPlayer模式
 implementation 'com.shuyu:gsyvideoplayer-aliplay:11.2.0'

 //更多ijk的编码支持
 implementation 'com.shuyu:gsyvideoplayer-ex_so:11.2.0'

```

### 3、Jitpack 引入方法（会继续发布，但不是很推荐）

历史包可能会出现随机丢包，而且还不好补充，可见 [#4144](https://github.com/CarGuo/GSYVideoPlayer/issues/4144)：

#### First、在project下的build.gradle添加

```groovy
allprojects {
    repositories {
		//...
        maven { url 'https://jitpack.io' }
        maven { url "https://maven.aliyun.com/repository/public" }
        mavenCentral()
    }
}
```

**你可以选择下面三种的其中一种，在module下的build.gradle添加。**

#### A、直接引入

```groovy
 //完整版引入

 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer:v11.2.0'


 //是否需要AliPlayer模式
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-aliplay:v11.2.0'
```

#### B、添加java和你想要的so支持：

```groovy
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-java:v11.2.0'

 //是否需要ExoPlayer模式
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-exo2:v11.2.0'

 //是否需要AliPlayer模式
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-aliplay:v11.2.0'

 //根据你的需求ijk模式的so
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-arm64:v11.2.0'
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-armv7a:v11.2.0'
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-armv5:v11.2.0'
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-x86:v11.2.0'
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-x64:v11.2.0'
```

#### C、支持其他格式协议的（mpeg，rtsp, concat、crypto协议，支持 16k Page Size）

A、B普通版本支持263/264/265等，对于mpeg编码会有声音无画面情况。
C 引入的so支持mpeg编码和其他补充协议，但是so包相对变大。

```groovy
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-java:v11.2.0'

 //是否需要ExoPlayer模式
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-exo2:v11.2.0'

 //是否需要AliPlayer模式
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-aliplay:v11.2.0'

 //更多ijk的编码支持
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-ex_so:v11.2.0'

```
