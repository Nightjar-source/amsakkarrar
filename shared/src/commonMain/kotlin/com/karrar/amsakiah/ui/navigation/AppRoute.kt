package com.karrar.amsakiah.ui.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * مسارات Navigation 3.
 *
 * نستخدم `sealed interface` + `@Serializable` حتى يعمل استرجاع الـback stack
 * على أندرويد وiOS دون الحاجة إلى انعكاس (reflection) غير متاح على Kotlin/Native.
 */
@Serializable
sealed interface AppRoute : NavKey {

    @Serializable
    data object Today : AppRoute

    @Serializable
    data object Settings : AppRoute

    @Serializable
    data object Calendar : AppRoute

    @Serializable
    data object Qibla : AppRoute

    @Serializable
    data object Duas : AppRoute

    @Serializable
    data object Tasbih : AppRoute

    @Serializable
    data object Fiqh : AppRoute

    /** شاشة فرعية تُفتح من الضبط — ليست تبويبًا ثامنًا. */
    @Serializable
    data object Widgets : AppRoute
}

/**
 * إعداد `rememberNavBackStack` لاسترجاع الـback stack بعد قتل العملية.
 *
 * Navigation 3 يطلب تسجيلًا صريحًا لكل نوع من `NavKey` (تعددية مفتوحة)،
 * وهذا ما يجعل الحالة تبقى بعد إعادة إنشاء النشاط على أندرويد وiOS.
 */
val appNavConfiguration: SavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(AppRoute.Today::class, AppRoute.Today.serializer())
            subclass(AppRoute.Settings::class, AppRoute.Settings.serializer())
            subclass(AppRoute.Calendar::class, AppRoute.Calendar.serializer())
            subclass(AppRoute.Qibla::class, AppRoute.Qibla.serializer())
            subclass(AppRoute.Duas::class, AppRoute.Duas.serializer())
            subclass(AppRoute.Tasbih::class, AppRoute.Tasbih.serializer())
            subclass(AppRoute.Fiqh::class, AppRoute.Fiqh.serializer())
            subclass(AppRoute.Widgets::class, AppRoute.Widgets.serializer())
        }
    }
}

/** تحويل وجهة شريط التنقل إلى مسار. */
fun AppDestination.toRoute(): AppRoute = when (this) {
    AppDestination.TODAY -> AppRoute.Today
    AppDestination.SETTINGS -> AppRoute.Settings
    AppDestination.CALENDAR -> AppRoute.Calendar
    AppDestination.QIBLA -> AppRoute.Qibla
    AppDestination.DUAS -> AppRoute.Duas
    AppDestination.TASBIH -> AppRoute.Tasbih
    AppDestination.FIQH -> AppRoute.Fiqh
}

/** الوجهة المطابقة للمسار الحالي (لإبراز التبويب). */
fun AppRoute.toDestination(): AppDestination? = when (this) {
    AppRoute.Today -> AppDestination.TODAY
    AppRoute.Settings -> AppDestination.SETTINGS
    AppRoute.Calendar -> AppDestination.CALENDAR
    AppRoute.Qibla -> AppDestination.QIBLA
    AppRoute.Duas -> AppDestination.DUAS
    AppRoute.Tasbih -> AppDestination.TASBIH
    AppRoute.Fiqh -> AppDestination.FIQH
    AppRoute.Widgets -> AppDestination.SETTINGS
}
