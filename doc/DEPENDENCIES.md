# 托管方式

**[Click to see the English version](DEPENDENCIES_EN.md)**

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

implementation 'io.github.carguo:gsyvideoplayer:13.1.0'


//是否需要AliPlayer模式
implementation 'io.github.carguo:gsyvideoplayer-aliplay:13.1.0'
```

#### B、添加java和你想要的so支持：

```groovy
 implementation 'io.github.carguo:gsyvideoplayer-java:13.1.0'

 //是否需要ExoPlayer模式
 implementation 'io.github.carguo:gsyvideoplayer-exo2:13.1.0'

 //是否需要AliPlayer模式
 implementation 'io.github.carguo:gsyvideoplayer-aliplay:13.1.0'

 //根据你的需求ijk模式的so
 implementation 'io.github.carguo:gsyvideoplayer-arm64:13.1.0'
 implementation 'io.github.carguo:gsyvideoplayer-armv7a:13.1.0'
 implementation 'io.github.carguo:gsyvideoplayer-armv5:13.1.0'
 implementation 'io.github.carguo:gsyvideoplayer-x86:13.1.0'
 implementation 'io.github.carguo:gsyvideoplayer-x64:13.1.0'
```

#### C、支持其他格式协议的（mpeg，rtsp, concat、crypto协议，支持 16k Page Size）

A、B普通版本支持263/264/265等，对于mpeg编码会有声音无画面情况。
C 引入的so支持mpeg编码和其他补充协议，但是so包相对变大。

```groovy
 implementation 'io.github.carguo:gsyvideoplayer-java:13.1.0'

 //是否需要ExoPlayer模式
 implementation 'io.github.carguo:gsyvideoplayer-exo2:13.1.0'

 //是否需要AliPlayer模式
 implementation 'io.github.carguo:gsyvideoplayer-aliplay:13.1.0'

 //更多ijk的编码支持
 implementation 'io.github.carguo:gsyvideoplayer-ex_so:13.1.0'

```

#### D、Jetpack Compose 支持（可选）

```groovy
 implementation 'io.github.carguo:gsyvideoplayer-compose:13.1.0'
```

`gsyvideoplayer-compose` 依赖 `gsyvideoplayer-java`，如果需要 ExoPlayer / AliPlayer / 指定 IJK so，仍按上面的方式额外引入对应模块。

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
 implementation 'com.shuyu:gsyvideoplayer:13.1.0'


 //是否需要AliPlayer模式
 implementation 'com.shuyu:gsyvideoplayer-aliplay:13.1.0'
```

#### B、添加java和你想要的so支持：

```groovy
 implementation 'com.shuyu:gsyvideoplayer-java:13.1.0'

 //是否需要ExoPlayer模式
 implementation 'com.shuyu:gsyvideoplayer-exo2:13.1.0'

 //是否需要AliPlayer模式
 implementation 'com.shuyu:gsyvideoplayer-aliplay:13.1.0'

 //根据你的需求ijk模式的so
 implementation 'com.shuyu:gsyvideoplayer-armv5:13.1.0'
 implementation 'com.shuyu:gsyvideoplayer-armv7a:13.1.0'
 implementation 'com.shuyu:gsyvideoplayer-arm64:13.1.0'
 implementation 'com.shuyu:gsyvideoplayer-x86:13.1.0'
 implementation 'com.shuyu:gsyvideoplayer-x64:13.1.0'
```

#### C、支持其他格式协议的（mpeg，rtsp, concat、crypto协议，支持 16k Page Size）

A、B普通版本支持263/264/265等，对于mpeg编码会有声音无画面情况。
C 引入的so支持mpeg编码和其他补充协议，但是so包相对变大。

```groovy
 implementation 'com.shuyu:gsyvideoplayer-java:13.1.0'

 //是否需要ExoPlayer模式
 implementation 'com.shuyu:gsyvideoplayer-exo2:13.1.0'


 //是否需要AliPlayer模式
 implementation 'com.shuyu:gsyvideoplayer-aliplay:13.1.0'

 //更多ijk的编码支持
 implementation 'com.shuyu:gsyvideoplayer-ex_so:13.1.0'

```

#### D、Jetpack Compose 支持（可选）

```groovy
 implementation 'com.shuyu:gsyvideoplayer-compose:13.1.0'
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

 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer:v13.1.0'


 //是否需要AliPlayer模式
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-aliplay:v13.1.0'
```

#### B、添加java和你想要的so支持：

```groovy
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-java:v13.1.0'

 //是否需要ExoPlayer模式
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-exo2:v13.1.0'

 //是否需要AliPlayer模式
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-aliplay:v13.1.0'

 //根据你的需求ijk模式的so
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-arm64:v13.1.0'
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-armv7a:v13.1.0'
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-armv5:v13.1.0'
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-x86:v13.1.0'
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-x64:v13.1.0'
```

#### C、支持其他格式协议的（mpeg，rtsp, concat、crypto协议，支持 16k Page Size）

A、B普通版本支持263/264/265等，对于mpeg编码会有声音无画面情况。
C 引入的so支持mpeg编码和其他补充协议，但是so包相对变大。

```groovy
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-java:v13.1.0'

 //是否需要ExoPlayer模式
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-exo2:v13.1.0'

 //是否需要AliPlayer模式
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-aliplay:v13.1.0'

 //更多ijk的编码支持
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-ex_so:v13.1.0'

```

## 投屏（DLNA/UPnP）依赖

投屏能力做在 `gsyVideoPlayer-java` 内部，不再单独发布 module。默认实现走 jUPnP 3.0.3 的 DLNA `AVTransport:1` 协议，所以 **只有真正需要投屏的下游** 需要额外引入 jUPnP，其他集成方 AAR 零增量。

依赖坐标（Maven Central）：

```groovy
dependencies {
    // 内核：包含 CastCapability / CastProvider / CastSession SPI + JupnpDlnaProvider 默认实现
    implementation 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-java:v13.1.0'

    // 投屏能力：DLNA/UPnP 走 jUPnP 3.0.3
    implementation 'org.jupnp:org.jupnp:3.0.3'
    implementation 'org.jupnp:org.jupnp.support:3.0.3'
}
```

集中版本管理放在 [gradle/dependencies.gradle](../gradle/dependencies.gradle) 的 `deps.jupnp`：

```groovy
deps.jupnp = [
    core   : "org.jupnp:org.jupnp:3.0.3",
    support: "org.jupnp:org.jupnp.support:3.0.3",
]
```

注意事项：

- 网络权限：接收端和发送端需要 `INTERNET`、`ACCESS_WIFI_STATE`、`CHANGE_WIFI_MULTICAST_STATE`、`ACCESS_NETWORK_STATE`。同一 Wi-Fi 局域网内 SSDP 才能发现设备。
- Android 独立进程：Demo 的 `Loopback Receiver` 在独立 `:dlna` 进程，避免 jUPnP static 状态污染主进程；下游自集成时可参考 [DevReceiverService](../app/src/main/java/com/example/gsyvideoplayer/cast/DevReceiverService.java)。
- Android 13+ 需要给内部广播加 `RECEIVER_NOT_EXPORTED`，参考 [CastReceiverManager](../app/src/main/java/com/example/gsyvideoplayer/cast/CastReceiverManager.java)。
- 悬浮窗接收端需要 `SYSTEM_ALERT_WINDOW` 权限，正式集成建议改成 `Activity` 或 `Surface` 呈现，不必沿用悬浮窗。
- 不需要投屏就不用引入 jUPnP；`CastCapability` 会走空实现，SPI 保留原样。

更多能力目标与测试判据见 [CAST_FEATURE_PLAN.md](CAST_FEATURE_PLAN.md) 与 [CAST_TEST_PLAYBOOK.md](CAST_TEST_PLAYBOOK.md)。
