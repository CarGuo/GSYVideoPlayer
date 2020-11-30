
##### 1、前言

为什么要编译？因为默认IJK居然不带HTTPS？Why？因为爱情，不会轻易悲伤···这就是爱吧((/- -)/。

编译IJK的动态链接库，绝对是对于win党最大的打击，经常是裤子脱了一半，发现拉链卡住，而且还很痛，上也不是，不上也不是的蛋碎感(ノಠ益ಠ)ノ彡┻━┻，官方都说了：```on Cygwin (unmaintained)```，Linux和Mac的胜利。如果你坚持，也许你就会遇上它们：

* 安装Cygwin的时候，记得把make选上。

* $'\r': 未找到命令 : dos2unix xxx，把所有你会编译到的.sh都轮一遍，你以为完了？不，编译生成出来的version.sh和configure也需要。

* mingw32-gcc.exe: error: CreateProcess: No such file or directory，据说是文件路径太深了，然而IJK的路径，本来就好深，所以你要把IJK的环境在整一遍，反正到这里我就放弃了治疗啦(ˉ￣～) 。

##### 2、VMware + Ubuntu编译环境
　
如果作为一个win党，头顶青天，推荐把虚拟机搬出来了，一般装的是Ubuntu，加上VMtools，妥妥的，环境搭建我就不唠叨了，这里走起[《VMware安装Ubuntu教程，Linux下搭建Android开发环境》](http://blog.csdn.net/linchaolong/article/details/52802401)。家中常备，总有一天你会用得上。注意NDK r10e以上，虚拟机硬盘尽量选大一点，不然后面扩展容量又是一番功夫。
　
> **Mac 环境下编译也是完美，强烈推荐。（新版本 Mac 系统用旧版 ndk 会面临一些权限兼容问题，建议在设置上全部允许，弹出来的多点几次吧）**　
>
>**ndk 版本必须是 `android-ndk-r13b` ，不要问为什么。**

##### 3、编译带HTTPS的so

到了这里你就简单多了，因为是亲生的啊，这时候就是机械化流程啦(✿‿)。

* 打开终端命令，安装需要程序。

```
sudo apt-get install git
sudo apt-get install yasm
```

* 下载IJKPlayer源码，在命令终端里cd到ijk的根目录
* 切换到release版本的tag下，当然，你也可以直接在master

```
git checkout -B k0.7.5  //这个版本参考ijk的依赖库版本
```

* 流程化脚本编译

**1、编译前选择你的配置**

If you prefer more codec/format

```
cd config
rm module.sh
ln -s module-default.sh module.sh
```

If you prefer less codec/format for smaller binary size (include hevc function)

```
cd config
rm module.sh
ln -s module-lite-hevc.sh module.sh
```

If you prefer less codec/format for smaller binary size (by default)

```
cd config
rm module.sh
ln -s module-lite.sh module.sh
```

**2、初始化openSSL和FFMPEG**

这里会同步下载对应的代码，所以可能会比较耗时哟，

```
./init-android-openssl.sh
./init-android.sh
```

**3、编译**

cd到android/contrib下，执行清除命令，然后编译需要的so，这里的all是生成所有版本的，如果只需要对应版本的，可以把all替换成armv7a等你需要的版本。编译需要一段时间，这时候你可以找个妹子先吹吹水。︿(￣︶￣)︿

```
./compile-openssl.sh clean//清除
./compile-ffmpeg.sh clean//清除
./compile-openssl.sh all//编译
./compile-ffmpeg.sh all//编译
```

**4、生成对应so**

切换到android目录下，执行脚本就可以生成so啦，感觉一路顺畅，一泻千里有木有。

```
./compile-ijk.sh all
```

生成的so每种类型有三个，传说，ffmepg的so大于3M就是https成功的标志啦！

![](http://upload-images.jianshu.io/upload_images/3673902-94aa88c1c80c49e2.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

#### 最后注意

1、最后不要忘记，在你的gradle里面加入下方的配置哟，不然app会找不到你的so的。

![](http://upload-images.jianshu.io/upload_images/3673902-c471beae629dca12.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

2、如果你觉得你的包so太大了，可以使用下图的ndk过滤，随时控制你想支持的so，具体可参看[Android蹲坑的疑难杂症集锦（兼Gradle） 二](http://www.jianshu.com/p/86e4b336c17d)

![](http://upload-images.jianshu.io/upload_images/3673902-3a1b270f55739657.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

3、如果需要编译其他播放类型支持，可以修改module.sh下的配置来使能。

**4、[IJKplayer 编译rtsp、rmvb支持](http://www.jianshu.com/p/bd289e25d272)**

相关的so已经集成到播放器里面，需要的可以直接拿出来用，下方链接走起<(￣︶￣)>，点我点我。

https://github.com/CarGuo/GSYVideoPlayer

![多日♂不见](http://upload-images.jianshu.io/upload_images/3673902-129730c7753bf611.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
