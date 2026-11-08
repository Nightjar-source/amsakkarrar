# ====================================================================
# R8 — إمساكية كرار 2026
# مراجع رسمية:
#  - developer.android.com/topic/performance/app-optimization/keep-rules-best-practices
#  - developer.android.com/topic/performance/app-optimization/enable-app-optimization
#  - developer.android.com/develop/ui/compose/performance (Compose + R8)
#
# القاعدة: لا نستخدم `-keep` واسعًا. مكتبات Compose/Glance/WorkManager/
# kotlinx.serialization تُوفّر consumer rules خاصة بها، ونضيف فقط ما يعتمد
# على الانعكاس أو يُنشأ بالاسم في وقت التشغيل.
# ====================================================================

# 1) آثار الأخطاء: أسماء الملفات وأرقام الأسطر والـmetadata اللازمة
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault
-renamesourcefileattribute SourceFile

# 2) Navigation 3 + kotlinx.serialization
#    استرجاع الـback stack يعتمد على `subclassesOfSealed<AppRoute>()` وعلى
#    serializers المولّدة، لذا تُبقى مسارات التنقل بلا تقليص.
-keep,allowoptimization class com.karrar.amsakiah.ui.navigation.AppRoute { *; }
-keep,allowoptimization class com.karrar.amsakiah.ui.navigation.AppRoute$* { *; }
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class * {
    public static ** serializer(...);
}
-keepclassmembers class * {
    *** Companion;
}

# 3) Glance AppWidget — تُنشأ المستقبلات والودجتات من المانيفست بالاسم
-keep class com.Nightjar.amsakkarrar.widget.** { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidget { *; }

# 4) WorkManager — العامل يُنشأ بالانعكاس عبر اسم الفئة
-keep class * extends androidx.work.ListenableWorker {
    <init>(...);
}
-dontwarn androidx.work.**

# 5) Compose Multiplatform Resources — الوصول إلى `files/...` بالمسار النصي
#    (cities.json، adhan.mp3، الخطوط) يتم في وقت التشغيل.
-keep,allowoptimization class com.karrar.amsakiah.resources.** { *; }

# 6) شاشات/حالة التطبيق المُرْمَزة عبر kotlinx.serialization (AppSettings)
-keep,allowoptimization class com.karrar.amsakiah.core.settings.AppSettings { *; }
-keep,allowoptimization class com.karrar.amsakiah.core.data.TasbihDhikr { *; }

# 7) خدمات/مستقبلات الإشعارات والأذان — نقاط دخول من المانيفست
-keep class com.Nightjar.amsakkarrar.notifications.** { *; }

# 8) تحسينات R8 لعام 2026:
#    حذف سجلات Verbose/Debug/Info مع إبقاء Warn/Error (آمن بلا افتراضات جانبية)
-maximumremovedandroidloglevel 4
