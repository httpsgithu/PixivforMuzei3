-dontwarn java.lang.Module
-dontwarn org.graalvm.nativeimage.hosted.Feature$BeforeAnalysisAccess
-dontwarn org.graalvm.nativeimage.hosted.Feature
-dontwarn org.graalvm.nativeimage.hosted.RuntimeResourceAccess
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keeppackagenames okhttp3.internal.publicsuffix.*
-adaptresourcefilenames okhttp3/internal/publicsuffix/PublicSuffixDatabase.gz