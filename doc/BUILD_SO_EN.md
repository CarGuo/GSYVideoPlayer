
##### 1. Foreword

Why compile? Because the default IJK doesn't even include HTTPS support? Why? For love, which doesn't grieve easily... this must be love ((/- -)/.

Compiling IJK's dynamic link libraries is definitely the biggest blow for Windows users. It often feels like you've pulled down your pants halfway, only to find the zipper stuck, and it hurts. You're in a frustrating situation (ノಠ益ಠ)ノ彡┻━┻. The official documentation even says: ```on Cygwin (unmaintained)```. A victory for Linux and Mac. If you insist, you might encounter these issues:

* When installing Cygwin, remember to select `make`.

* `$'\r': command not found : dos2unix xxx`. You'll have to run this on every `.sh` file you're going to compile. Think you're done? Nope, the generated `version.sh` and `configure` files need it too.

* `mingw32-gcc.exe: error: CreateProcess: No such file or directory`. This is said to be because the file path is too long, and IJK's path is inherently deep. So you have to set up the IJK environment all over again. At this point, I gave up treatment (ˉ￣～).

##### 2. VMware + Ubuntu Compilation Environment
　
If you're a Windows user, it's highly recommended to use a virtual machine. Ubuntu is a common choice, and with VMtools, it's a smooth setup. I won't go into the details of setting up the environment; you can follow this guide: [《VMware Installation Tutorial for Ubuntu, Setting up Android Development Environment on Linux》](http://blog.csdn.net/linchaolong/article/details/52802401). It's good to have it ready; you'll need it someday. Note: For NDK r10e and above, try to allocate a larger virtual disk, or you'll have to go through the trouble of expanding it later.
　
> Compiling on a Mac environment is also perfect and highly recommended. (Newer Mac systems might face some permission compatibility issues with older NDK versions. It's advisable to allow everything in the settings and click through any pop-ups multiple times). **It is recommended to use `sudo spctl --master-disable` to trust all sources on Mac and allow execution.**
>
> **The NDK version must be `android-ndk-r13b`. Don't ask why.**
> **For newer macOS versions, use the lowest supported version like `android-ndk-r10e`, depending on your system.**
>
> Execute `sudo spctl --master-disable` to disable security checks.

- Analysis from a [GitHub issue](https://github.com/bilibili/ijkplayer/issues/5113#issuecomment-1288378800):
  "I picked up Ijkplayer again these past two days and encountered the same problem as the original poster while compiling Ijkplayer with openssl on macOS Monterey. I found this thread through Google. After careful analysis, I discovered that the problem is caused by some missing function implementations in the `libcrypto.a` static library. Reading the output during the failed compilation, I noticed two actions to create the `libcrypto.a` library, one after the other. A successful compilation only has one creation log. I suspect it's a make multithreading issue, where two threads are creating the static library, causing one to fail. I found the place where the number of make threads is set in `do-compile-openssl.sh`, passed by the `IJK_MAKE_FLAG` variable. Searching for `IJK_MAKE_FLAG`, I found it's set in the `do-detect-env.sh` script. On Darwin (i.e., Apple) platforms, it's set by getting the thread count via the system command `sysctl -n machdep.cpu.thread_count`. I forced it to 1, and the problem disappeared, so it should be solved. The root cause is likely that the makefile for the version of openssl used by Ijkplayer is not written rigorously enough, making multithreaded compilation risky."<br/>
  **Specifically, change `IJK_MAKE_FLAG` in `android/contrib/tools/do-detect-env.sh` to: `export IJK_MAKE_FLAG=-j1`**

- Also note that a python2 execution environment is required. If your Mac defaults to python3, you can just download and install a python2 package.

- For M1 machines, you can refer to: https://www.jianshu.com/p/22405a692c18 , path: `/Users/xxx/env/android-sdk/ndk/android-ndk-xxxx/ndk-build`
  For higher versions:
  ```sh
    #!/bin/sh
    DIR="$(cd "$(dirname "$0")" && pwd)"
    $DIR/build/ndk-build "$@"

    Change to:
    #!/bin/sh
    DIR="$(cd "$(dirname "$0")" && pwd)"
    arch -x86_64 /bin/bash $DIR/build/ndk-build "$@"
  ```
  For lower versions:
  ```sh
    HOST_ARCH=$(uname -m)
    case $HOST_ARCH in
        i?86) HOST_ARCH=x86;;
        x86_64|amd64) HOST_ARCH=x86_64;;
        *) echo "ERROR: Unknown host CPU architecture: $HOST_ARCH"
           exit 1
    esac
    log "HOST_ARCH=$HOST_ARCH"
    Add `arm64` to the `x86_64|amd64)` line, making it `x86_64|amd64|arm64)`.
  ```
- If you encounter an outdated `awk` issue, just delete it from `prebuilt/darwin-x86_64/bin`.
- If you get `yasm/nasm not found or too old. Use --disable-yasm for a crippled build.` on macOS, install it: `brew install yasm`.

# New Configuration on macOS:

By default, IJKPlayer recommends `android-ndk-r10e`, which is why I use it as an example—it's "ancient." On modern macOS, if you want to use `android-ndk-r10e`, you will "probably" need to:

- Download it from the web, as the Android Studio SDK Manager doesn't support downloading r10.
- Add `android-ndk-r10e` to your environment variables: `export ANDROID_NDK="$HOME/Library/Android/sdk/ndk/android-ndk-r10e"`.
- Modify the build script: change `IJK_MAKE_FLAG` in `android/contrib/tools/do-detect-env.sh` to `export IJK_MAKE_FLAG=-j1`.
- Execute `sudo spctl --master-disable` to disable macOS security checks, **allowing execution from any source on Mac.**
- Set the default python environment to python2.
- If you have an outdated `awk` problem, delete it from `prebuilt/darwin-x86_64/bin`.
- If `yasm/nasm not found or too old. Use --disable-yasm for a crippled build.`, install it with `brew install yasm`.
- If `ndk-build` cannot be executed, you can modify `./compile-ijk.sh` as follows:
```patch
       case $SUB_CMD in
         prof)
-            $ANDROID_NDK/ndk-build $FF_MAKEFLAGS
+           arch -x86_64 /bin/bash  $ANDROID_NDK/ndk-build $FF_MAKEFLAGS
         ;;
         clean)
-            $ANDROID_NDK/ndk-build clean
+           arch -x86_64 /bin/bash  $ANDROID_NDK/ndk-build clean
         ;;
         rebuild)
-            $ANDROID_NDK/ndk-build clean
-            $ANDROID_NDK/ndk-build $FF_MAKEFLAGS
+           arch -x86_64 /bin/bash  $ANDROID_NDK/ndk-build clean
+           arch -x86_64 /bin/bash  $ANDROID_NDK/ndk-build $FF_MAKEFLAGS
         ;;
         *)
-            $ANDROID_NDK/ndk-build $FF_MAKEFLAGS
+          arch -x86_64 /bin/bash   $ANDROID_NDK/ndk-build $FF_MAKEFLAGS
         ;;
     esac
 }
```

### Upgrading to r21

To compile with r21, you will theoretically encounter the following issues with IJKPlayer:

- In `tools/do-detect-env.sh`, add the NDK versions you want to support: `11*|12*|13*|14*|15*|16*|21*|22*|25*|26*|27*)`
- `Bad file descriptor error: invalid argument '-std=c99'`: You need to remove all instances of `LOCAL_CFLAGS += -std=c99` from all `Android.mk` files, as GCC was removed in r18.
- `Invalid NDK_TOOLCHAIN_VERSION value: 4.9`: Also because GCC has been removed, you need to remove `NDK_TOOLCHAIN_VERSION=4.9` from `Application.mk`.
- `APP_STL := stlport_static is not supported`: `stlport` was also removed in r18, so you need to change `APP_STL` in `Application.mk` to `APP_STL := c++_static`, using the libc++ implementation.
- Starting from r21, `ndk-bundle/build/tools/make-standalone-toolchain.sh` calls `ndk-bundle/build/tools/make-standalone-toolchain.py`, so you need to change `FF_ANDROID_PLATFORM` in `tools/do-compile-ffmpeg.sh` to `FF_ANDROID_PLATFORM=android-21`. The same applies to `tools/do-compile-openssl.sh`.

### Upgrading to r22

When upgrading to r22, you also need to add `-WI,-Bsymbolic` to solve issues: `FF_EXTRA_LDFLAGS="$FF_EXTRA_LDFLAGS -WI,-Bsymbolic"`

### New FFMpeg Version

- Before upgrading, you first need to manually delete the `openssl-arm64` file under `contrib`.
- Then, modify the git link and version in `init-android-openssl`.
- After initialization, modify `FF_CFG_FLAGS="$FF_CFG_FLAGS --prefix=$FF_PREFIX"` in `do-compile-openssl.sh`.
- Finally, when compiling ffmpeg, adjust the `check_lib openssl openssl/ssl.h OPENSSL_init_ssl -lssl -lcrypto` line in the `configure` file.

### Support for 16K Compilation

https://juejin.cn/post/7396306532671094793

##### 3. Compiling .so with HTTPS support

At this point, things get much simpler, as it's a native environment. Now it's just a mechanical process (✿‿).

* Open the terminal and install the required programs.

```
sudo apt-get install git
sudo apt-get install yasm
```

* Download the IJKPlayer source code, and `cd` to the root directory of ijk in the terminal.
* Switch to a release version tag, or you can just use master.

```
git checkout -B k0.7.5  // Refer to ijk's dependency library version for this version number
```

* Scripted compilation process

**1. Choose your configuration before compiling**

If you prefer more codecs/formats:

```
cd config
rm module.sh
ln -s module-default.sh module.sh
```

If you prefer fewer codecs/formats for a smaller binary size (including hevc function):

```
cd config
rm module.sh
ln -s module-lite-hevc.sh module.sh
```

If you prefer fewer codecs/formats for a smaller binary size (by default):

```
cd config
rm module.sh
ln -s module-lite.sh module.sh
```

**2. Initialize openSSL and FFMPEG**

This will download the corresponding code, so it might take some time.

```
./init-android-openssl.sh
./init-android.sh
```

**3. Compile**

`cd` to `android/contrib`, execute the clean command, and then compile the required .so files. Here, `all` generates all versions. If you only need a specific version, you can replace `all` with `armv7a` or the version you need. Compilation will take a while, so you can go chat with someone. ︿(￣︶￣)︿

```
./compile-openssl.sh clean // Clean
./compile-ffmpeg.sh clean  // Clean
./compile-openssl.sh all   // Compile
./compile-ffmpeg.sh all    // Compile
```

**4. Generate the corresponding .so files**

Switch to the `android` directory and execute the script to generate the .so files. It should be a smooth process.

```
./compile-ijk.sh all
```

Three .so files are generated for each type. It is said that if the ffmpeg .so file is larger than 3MB, it's a sign that HTTPS support was successfully included!

![](http://upload-images.jianshu.io/upload_images/3673902-94aa88c1c80c49e2.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

#### Final Notes

1. Don't forget to add the following configuration to your `build.gradle` file, otherwise your app won't be able to find your .so files.

![](http://upload-images.jianshu.io/upload_images/3673902-c471beae629dca12.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

2. If you think your .so files are too large, you can use the NDK filter shown in the image below to control which .so files you want to support. For details, see [Android Pitfalls Collection (and Gradle) Part 2](http://www.jianshu.com/p/86e4b336c17d).

![](http://upload-images.jianshu.io/upload_images/3673902-3a1b270f55739657.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

3. If you need to compile support for other playback types, you can enable them by modifying the configuration in `module.sh`.

**4. [Compiling IJKplayer for RTSP, RMVB support](http://www.jianshu.com/p/bd289e25d272)**

The relevant .so files have already been integrated into the player. If you need them, you can get them directly from the link below. Click me, click me. <(￣︶￣)>

https://github.com/CarGuo/GSYVideoPlayer

![Long time no see](http://upload-images.jianshu.io/upload_images/3673902-129730c7753bf611.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
