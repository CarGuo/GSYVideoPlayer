# The public RTMP classes are referenced directly by Media3. Native methods
# follow explicit JNI names, so preserving their names keeps JNI resolution
# stable for optimized consumer builds.
-keepclasseswithmembernames class io.antmedia.rtmp_client.** {
    native <methods>;
}
