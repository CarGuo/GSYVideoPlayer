# Hosting Methods

**[Click to see the Chinese version](DEPENDENCIES.md)**

There are currently three hosting methods:

- MavenCentral: Available after version 11.0.0, all base class packages are published and hosted here.
- Github Package: Available from version 9.1.0, but before version 11.0.0, the basic dependencies of GSYIjkJava are still hosted on jitpack.
- Jitpack IO: Will continue to be released, but there is a random loss of packages on the hosting platform.

### 1. MavenCentral Reference (Recommended)

Since jitpack keeps losing packages, it has been migrated to MavenCentral. The usage is as follows:

#### First Add

```groovy
allprojects {
    repositories {
        ///...
        mavenCentral()
        maven { url "https://maven.aliyun.com/repository/public" }
    }
}
```

**You can choose one of the following three and add it to the build.gradle under the module.**

#### A. Direct Introduction

```groovy
 //Complete version introduction

implementation 'io.github.carguo:gsyvideoplayer:13.2.1'


//Whether AliPlayer mode is needed
implementation 'io.github.carguo:gsyvideoplayer-aliplay:13.2.1'
```

#### B. Add java and the so support you want:

```groovy
 implementation 'io.github.carguo:gsyvideoplayer-java:13.2.1'

 //Whether ExoPlayer mode is needed
 implementation 'io.github.carguo:gsyvideoplayer-exo2:13.2.1'

 //Optional direct RTMP module; exo2 already exposes it transitively
 implementation 'io.github.carguo:gsyvideoplayer-rtmp:13.2.1'

 //Optional cast implementation; omit it when casting is unused (real minSdk: 26)
 implementation 'io.github.carguo:gsyvideoplayer-cast:13.2.1'

 //Whether AliPlayer mode is needed
 implementation 'io.github.carguo:gsyvideoplayer-aliplay:13.2.1'

 //so of ijk mode according to your needs
 implementation 'io.github.carguo:gsyvideoplayer-arm64:13.2.1'
 implementation 'io.github.carguo:gsyvideoplayer-armv7a:13.2.1'
 implementation 'io.github.carguo:gsyvideoplayer-armv5:13.2.1'
 implementation 'io.github.carguo:gsyvideoplayer-x86:13.2.1'
 implementation 'io.github.carguo:gsyvideoplayer-x64:13.2.1'
```

#### C. Support other format protocols (mpeg, rtsp, concat, crypto protocols, support 16k Page Size)

A and B normal versions support 263/264/265, etc. For mpeg encoding, there will be sound but no picture.
The so introduced by C supports mpeg encoding and other supplementary protocols, but the so package is relatively larger.

```groovy
 implementation 'io.github.carguo:gsyvideoplayer-java:13.2.1'

 //Whether ExoPlayer mode is needed
 implementation 'io.github.carguo:gsyvideoplayer-exo2:13.2.1'

 //Whether AliPlayer mode is needed
 implementation 'io.github.carguo:gsyvideoplayer-aliplay:13.2.1'

 //More ijk encoding support
 implementation 'io.github.carguo:gsyvideoplayer-ex_so:13.2.1'

```

#### D. Jetpack Compose Support (Optional)

```groovy
 implementation 'io.github.carguo:gsyvideoplayer-compose:13.2.1'
```

`gsyvideoplayer-compose` depends on `gsyvideoplayer-java`; if you need ExoPlayer, AliPlayer, or a specific IJK so package, keep adding the matching module above.

### 2. Github Package Dependency Method (Recommended)

**Since Jitpack often has the problem of random loss of historical packages, a new Github Package dependency method is added. The usage is as follows**:

> However, accessing github package requires a token to access, which is more troublesome, but it is stable.

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

- To generate your own token, you can see: https://docs.github.com/zh/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens

> In theory, it is the avatar in the upper right corner - Settings - Developer Settings - Personal access tokens - tokens (classic) -
> Generate new token (classic) - read:packages
> Remember to choose permanent for the expiration time

**You can choose one of the following three and add it to the build.gradle under the module.**

#### A. Direct Introduction

```groovy
 //Complete version introduction
 implementation 'com.shuyu:gsyvideoplayer:13.2.1'


 //Whether AliPlayer mode is needed
 implementation 'com.shuyu:gsyvideoplayer-aliplay:13.2.1'
```

#### B. Add java and the so support you want:

```groovy
 implementation 'com.shuyu:gsyvideoplayer-java:13.2.1'

 //Whether ExoPlayer mode is needed
 implementation 'com.shuyu:gsyvideoplayer-exo2:13.2.1'

 //Optional direct RTMP module; exo2 already exposes it transitively
 implementation 'com.shuyu:gsyvideoplayer-rtmp:13.2.1'

 //Optional cast implementation; omit it when casting is unused (real minSdk: 26)
 implementation 'com.shuyu:gsyvideoplayer-cast:13.2.1'

 //Whether AliPlayer mode is needed
 implementation 'com.shuyu:gsyvideoplayer-aliplay:13.2.1'

 //so of ijk mode according to your needs
 implementation 'com.shuyu:gsyvideoplayer-armv5:13.2.1'
 implementation 'com.shuyu:gsyvideoplayer-armv7a:13.2.1'
 implementation 'com.shuyu:gsyvideoplayer-arm64:13.2.1'
 implementation 'com.shuyu:gsyvideoplayer-x86:13.2.1'
 implementation 'com.shuyu:gsyvideoplayer-x64:13.2.1'
```

#### C. Support other format protocols (mpeg, rtsp, concat, crypto protocols, support 16k Page Size)

A and B normal versions support 263/264/265, etc. For mpeg encoding, there will be sound but no picture.
The so introduced by C supports mpeg encoding and other supplementary protocols, but the so package is relatively larger.

```groovy
 implementation 'com.shuyu:gsyvideoplayer-java:13.2.1'

 //Whether ExoPlayer mode is needed
 implementation 'com.shuyu:gsyvideoplayer-exo2:13.2.1'


 //Whether AliPlayer mode is needed
 implementation 'com.shuyu:gsyvideoplayer-aliplay:13.2.1'

 //More ijk encoding support
 implementation 'com.shuyu:gsyvideoplayer-ex_so:13.2.1'

```

#### D. Jetpack Compose Support (Optional)

```groovy
 implementation 'com.shuyu:gsyvideoplayer-compose:13.2.1'
```

### 3. Jitpack Introduction Method (will continue to be released, but not highly recommended)

Historical packages may have random packet loss, and it is not easy to supplement, see [#4144](https://github.com/CarGuo/GSYVideoPlayer/issues/4144):

#### First, add in the build.gradle under the project

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

**You can choose one of the following three and add it to the build.gradle under the module.**

#### A. Direct Introduction

```groovy
 //Complete version introduction

 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer:v13.2.1'


 //Whether AliPlayer mode is needed
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-aliplay:v13.2.1'
```

#### B. Add java and the so support you want:

```groovy
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-java:v13.2.1'

 //Whether ExoPlayer mode is needed
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-exo2:v13.2.1'

 //Optional cast implementation (real minSdk: 26)
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-cast:v13.2.1'

 //Whether AliPlayer mode is needed
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-aliplay:v13.2.1'

 //so of ijk mode according to your needs
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-arm64:v13.2.1'
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-armv7a:v13.2.1'
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-armv5:v13.2.1'
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-x86:v13.2.1'
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-x64:v13.2.1'
```

#### C. Support other format protocols (mpeg, rtsp, concat, crypto protocols, support 16k Page Size)

A and B normal versions support 263/264/265, etc. For mpeg encoding, there will be sound but no picture.
The so introduced by C supports mpeg encoding and other supplementary protocols, but the so package is relatively larger.

```groovy
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-java:v13.2.1'

 //Whether ExoPlayer mode is needed
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-exo2:v13.2.1'

 //Whether AliPlayer mode is needed
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-aliplay:v13.2.1'

 //More ijk encoding support
 implementation 'com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-ex_so:v13.2.1'

```

## Cast (DLNA/UPnP) Dependencies

Starting with 13.2.1, the protocol-neutral `CastCapability` / `CastProvider` / `CastSession`
SPI remains in `gsyvideoplayer-java`, while jUPnP/Jetty and the default DLNA implementation
live in the independently published `gsyvideoplayer-cast` module. Default player consumers no
longer resolve jUPnP or Jetty.

Maven Central coordinates:

```groovy
dependencies {
    implementation 'io.github.carguo:gsyvideoplayer:13.2.1'
    implementation 'io.github.carguo:gsyvideoplayer-cast:13.2.1'
}
```

Do not declare jUPnP or Jetty manually; the cast POM exposes one tested dependency set.

Notes:

- Minimum SDK: the default `gsyvideoplayer` artifact is constrained to API 23 by Media3 1.10.1. Adding the cast module raises the real minimum to API 26 because of Jetty 9.4.53.
- Network permissions: both sender and receiver need `INTERNET`, `ACCESS_WIFI_STATE`, `CHANGE_WIFI_MULTICAST_STATE`, and `ACCESS_NETWORK_STATE`. Devices only discover each other on the same Wi-Fi LAN via SSDP.
- Android separate process: the demo `Loopback Receiver` runs in a dedicated `:dlna` process to keep jUPnP's static state out of the main process. See [DevReceiverService](../app/src/main/java/com/example/gsyvideoplayer/cast/DevReceiverService.java) when self-integrating.
- Android 13+ requires `RECEIVER_NOT_EXPORTED` for internal broadcasts. See [CastReceiverManager](../app/src/main/java/com/example/gsyvideoplayer/cast/CastReceiverManager.java).
- The floating-window receiver requires `SYSTEM_ALERT_WINDOW`. Production integrations should render the incoming stream in an `Activity` or `Surface` instead of a floating window.
- If you do not need casting, omit `gsyvideoplayer-cast`; the core SPI remains available for other providers.

See [CAST_FEATURE_PLAN.md](CAST_FEATURE_PLAN.md) and [CAST_TEST_PLAYBOOK.md](CAST_TEST_PLAYBOOK.md) for the capability goals and pass/fail criteria.
