# 13.2.1: jUPnP carries optional OSGi component metadata annotations. Android never loads
# these annotations, but R8 still resolves their class literals while shrinking release builds.
-dontwarn org.osgi.service.component.annotations.**
-dontwarn org.osgi.service.metatype.annotations.**

# 13.2.1: AnnotationLocalServiceBinder builds local UPnP services through reflection. Keep the
# annotation types/attributes and only the annotated members it reads; do not keep all jUPnP code.
-keepattributes *Annotation*,Signature
-keep @interface org.jupnp.binding.annotations.**
-keepnames @org.jupnp.binding.annotations.UpnpService class **
-keepclassmembers class ** {
    @org.jupnp.binding.annotations.UpnpStateVariable <fields>;
    @org.jupnp.binding.annotations.UpnpStateVariable <methods>;
    @org.jupnp.binding.annotations.UpnpAction <methods>;
}
-keepclassmembers @org.jupnp.binding.annotations.UpnpService class ** {
    public <init>(...);
    public <fields>;
    public <methods>;
}

# Standard service action annotations name getters on their org.jupnp.support.model return objects.
-keepclassmembers class org.jupnp.support.model.** {
    public <methods>;
}

# Built-in UpnpService.stringConvertibleTypes are Class values. The binder requires each live
# jUPnP type to retain its public String constructor; member-only keeping still lets unused types go.
-keepclassmembers,allowoptimization class org.jupnp.** {
    public <init>(java.lang.String);
}

# AbstractDatatype reflects on each concrete subclass's generic superclass to recover its value
# type. These small signature endpoints cannot be optimized/class-merged, but may still be renamed.
-keep,allowobfuscation class org.jupnp.model.types.AbstractDatatype
-keep,allowobfuscation class ** extends org.jupnp.model.types.AbstractDatatype

# UpnpHeader.Type stores header implementations as Class values and instantiates them reflectively.
-keepclassmembers class ** extends org.jupnp.model.message.header.UpnpHeader {
    public <init>(...);
}
