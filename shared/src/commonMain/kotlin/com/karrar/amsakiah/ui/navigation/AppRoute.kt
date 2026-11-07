package com.karrar.amsakiah.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

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
