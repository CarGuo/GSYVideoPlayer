
### 1、Jitpack 引入方法

⚠️**因为依赖方式 2 的原因，目前 jitpack 依赖路径名称有调整**

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

**你可以选择下面三种的其中一种，在module下的build.gradle添加。**


#### A、直接引入
```
 //完整版引入

 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer:v10.1.0'


 //是否需要AliPlayer模式
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-aliplay:v10.1.0'
```

#### B、添加java和你想要的so支持：

```
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-java:v10.1.0'

 //是否需要ExoPlayer模式
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-exo2:v10.1.0'

 //是否需要AliPlayer模式
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-aliplay:v10.1.0'

 //根据你的需求ijk模式的so
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-arm64:v10.1.0'
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-armv7a:v10.1.0'
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-armv5:v10.1.0'
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-x86:v10.1.0'
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-x64:v10.1.0'
```

#### C、支持其他格式协议的（mpeg，rtsp, concat、crypto协议，支持 16k Page Size）

A、B普通版本支持263/264/265等，对于mpeg编码会有声音无画面情况。
C 引入的so支持mpeg编码和其他补充协议，但是so包相对变大。

```
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-java:v10.1.0'

 //是否需要ExoPlayer模式
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-exo2:v10.1.0'

 //是否需要AliPlayer模式
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-aliplay:v10.1.0'

 //更多ijk的编码支持
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-ex_so:v10.1.0'

```


### 2、新增 Github Package 依赖方式

**由于 Jitpack 经常存在历史包随机丢失问题，所以新增 Github Package 依赖方式，使用方式如下**：

> 不过 github package 访问需要 token 去 access 比较麻烦，但是胜在稳定


```
allprojects {
    repositories {
		...
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
    }
}
```
- 生成自己 token 的方式可见：https://docs.github.com/zh/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens

> 理论上就是右上角头像 - Settings - Developer Settings - Personal access tokens -  tokens (classic) -  Generate new token（classic）- read:packages
> 记得过期时间选择永久

**你可以选择下面三种的其中一种，在module下的build.gradle添加。**

#### A、直接引入
```
 //完整版引入
 implementation 'com.shuyu:gsyvideoplayer:10.1.0'


 //是否需要AliPlayer模式
 implementation 'com.shuyu:gsyvideoplayer-aliplay:10.1.0'
```

#### B、添加java和你想要的so支持：

```
 implementation 'com.shuyu:gsyvideoplayer-java:10.1.0'

 //是否需要ExoPlayer模式
 implementation 'com.shuyu:gsyvideoplayer-exo2:10.1.0'

 //是否需要AliPlayer模式
 implementation 'com.shuyu:gsyvideoplayer-aliplay:10.1.0'

 //根据你的需求ijk模式的so
 implementation 'com.shuyu:gsyvideoplayer-armv5:10.1.0'
 implementation 'com.shuyu:gsyvideoplayer-armv7a:10.1.0'
 implementation 'com.shuyu:gsyvideoplayer-arm64:10.1.0'
 implementation 'com.shuyu:gsyvideoplayer-x86:10.1.0'
 implementation 'com.shuyu:gsyvideoplayer-x64:10.1.0'
```

#### C、支持其他格式协议的（mpeg，rtsp, concat、crypto协议，支持 16k Page Size）

A、B普通版本支持263/264/265等，对于mpeg编码会有声音无画面情况。
C 引入的so支持mpeg编码和其他补充协议，但是so包相对变大。

```
 implementation 'com.shuyu:gsyvideoplayer-java:10.1.0'

 //是否需要ExoPlayer模式
 implementation 'com.shuyu:gsyvideoplayer-exo2:10.1.0'


 //是否需要AliPlayer模式
 implementation 'com.shuyu:gsyvideoplayer-aliplay:10.1.0'

 //更多ijk的编码支持
 implementation 'com.shuyu:gsyvideoplayer-ex_so:10.1.0'

```
