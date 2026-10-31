# ====================================================================
# Optimal R8 & ProGuard Rules for Caliq5 (Google R8 2026-2027 Guidelines)
# Reference: https://developer.android.com/topic/performance/app-optimization/keep-rules-best-practices
# ====================================================================

# 1. Stack Traces & Debugging (Preserve line numbers for crash logs)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# 2. Kotlin Serialization (Preserve model structures & serializers)
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
    @kotlinx.serialization.Serializable <init>(...);
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializer field *;
}

# 3. Glance AppWidget & WorkManager
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver
-keep class * extends androidx.glance.appwidget.GlanceAppWidget
-keep class com.Nightjar.caliq5.widget.** { *; }
-keepclassmembers class com.Nightjar.caliq5.widget.** { *; }
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-dontwarn androidx.work.**

# 4. Compose Multiplatform & Lifecycle Preservation
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-dontwarn kotlinx.coroutines.**
-dontwarn okio.**

# 5. Modern R8 (Google 2026/2027 Guidelines): Safely remove logs without side-effect assumption risks
# Removes Verbose (2), Debug (3), Info (4) while safely preserving Warn (5) and Error (6)
-maximumremovedandroidloglevel 4
