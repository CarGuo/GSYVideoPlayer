# =====================================================================
# GSYVideoPlayer 优化后的 R8 规则
# 基于 R8 Configuration Analyzer 报告 + 代码架构反射点审阅生成
# 目标：最大程度减小 APK 体积 / 提升优化得分，同时保证零崩溃
# ---------------------------------------------------------------------
# 反射点清单（决定下面 keep 规则的依据）：
#   1) PlayerFactory#getPlayManager()  -> Class.newInstance()
#      需要保留 IPlayerManager 全部实现类的无参构造
#   2) CacheFactory#getCacheManager()  -> Class.newInstance()
#      需要保留 ICacheManager 全部实现类的无参构造
#   3) GSYBaseVideoPlayer#startWindowFullscreen / showSmallVideo
#      -> getClass().getConstructor(Context.class[, Boolean.class]).newInstance(...)
#      需要保留 GSYBaseVideoPlayer 所有子类的 (Context) 与 (Context,Boolean) 构造
#   4) FloatToast 使用反射访问 android.widget.Toast 私有字段
#      属 android.* framework，R8 不裁剪，无需规则
#   5) Serializable / Enum / R$* / native / View 三个构造为通用反射入口
# =====================================================================

# ---------- 优化开关（配合 proguard-android-optimize.txt 生效） ----------
-optimizationpasses 5
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-allowaccessmodification
-repackageclasses ''
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# ---------- 保留的属性（异常栈 / 泛型 / 注解 / 行号） ----------
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions,InnerClasses,EnclosingMethod
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---------- JNI / 枚举 / 资源 / Serializable ----------
-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class **.R$* { *; }

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# JSON 反射构造
-keepclassmembers class * {
    public <init>(org.json.JSONObject);
}

# ---------- View 三大构造，防 XML 反射失败 ----------
-keepclasseswithmembers class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# ---------- GSYVideoPlayer 反射点：全屏 / 小窗 clone 播放器 ----------
# 任何 GSYBaseVideoPlayer 子类都可能被反射实例化
-keep class * extends com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer {
    public <init>(android.content.Context);
    public <init>(android.content.Context, java.lang.Boolean);
}
# 该基类内部 cloneParams 使用直接字段访问（非反射），无需 keep 全部成员

# ---------- GSYVideoPlayer 反射点：PlayerFactory / CacheFactory ----------
# IPlayerManager 与 ICacheManager 的所有实现类，其无参构造必须保留
-keep class * implements com.shuyu.gsyvideoplayer.player.IPlayerManager {
    public <init>();
}
-keep class * implements com.shuyu.gsyvideoplayer.cache.ICacheManager {
    public <init>();
}

# 基础接口本身也保留（避免子类实现的方法签名被 R8 因基类被裁剪而错剪）
-keep interface com.shuyu.gsyvideoplayer.player.IPlayerManager { *; }
-keep interface com.shuyu.gsyvideoplayer.cache.ICacheManager { *; }

# ---------- ijkplayer JNI 桥接 ----------
# ijk 侧通过 JNI 反射回调 Java 层，必须完整保留
-keep class tv.danmaku.ijk.media.player.** { *; }
-dontwarn tv.danmaku.ijk.media.player.**

# ---------- Media3 / ExoPlayer ----------
# Media3 官方 consumer-rules 已随 aar 附带；这里仅补 dontwarn
-dontwarn androidx.media3.**
-dontwarn com.google.android.exoplayer2.**

# ---------- OkHttp / okio 常规抑制 ----------
-dontwarn okhttp3.internal.platform.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ---------- Glide ----------
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$** { *; }

# ---------- 阿里云播放器 SDK（含 native，需保留符号） ----------
-keep class com.alivc.**  { *; }
-keep class com.aliyun.** { *; }
-keep class com.cicada.** { *; }
-dontwarn com.alivc.**
-dontwarn com.aliyun.**
-dontwarn com.cicada.**

# ---------- 阿里支付回调（示例中使用） ----------
-keep class com.shuyu.alipay.** { *; }

# =====================================================================
# 说明：相较于原始规则，以下条目已按 R8 分析报告移除或收窄，
# 因为它们要么被更精确的规则覆盖（subsumed），要么零匹配（unused）：
#   - `-dontpreverify`             （对 D8/R8 无意义）
#   - `-ignorewarnings`            （掩盖真实警告，改用精准 -dontwarn）
#   - 通配式的 View get*/set* 保留 （对 R8 优化伤害极大且非反射需要）
#   - `-keep class ...video.**/utils.**/player.** { *; }` 整包保留
#     （已由针对反射点的定向 keep 精细替代）
#   - `-keep class androidx.media3.** {*;}` 与 `exoplayer2.** {*;}` 整包保留
#     （官方 aar 自带 consumer rules，无需再全量保留）
#   - `**On*Event / **On*Listener` 通配保留
#     （项目内未使用 findViewById+setOnXxxEvent 字符串反射）
#   - `-keep class **$$ViewBinder`、ButterKnife 相关
#     （项目已迁离 ButterKnife，报告显示 unused）
#   - 重复的 native / enum / Serializable 规则（合并去重）
# =====================================================================
