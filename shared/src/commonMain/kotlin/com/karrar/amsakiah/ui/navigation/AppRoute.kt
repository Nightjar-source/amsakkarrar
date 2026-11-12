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
    data object Calendar : AppRoute

    /** نافذة «المفاتيح» — أيقونات لشاشات فرعية. */
    @Serializable
    data object Keys : AppRoute

    @Serializable
    data object Fiqh : AppRoute

    @Serializable
    data object Settings : AppRoute

    /** فرعية «المفاتيح» — الأدعية والمناجاة. */
    @Serializable
    data object Duas : AppRoute

    /** فرعية «المفاتيح» — القرآن الكريم (المصحف الشريف). */
    @Serializable
    data object Quran : AppRoute

    /** فرعية «المفاتيح» — المسبحة. */
    @Serializable
    data object Tasbih : AppRoute

    /** فرعية «المفاتيح» — البوصلة والمراقد المقدسة. */
    @Serializable
    data object Qibla : AppRoute

    /** فرعية «المفاتيح» — أعمال اليوم (مهام وملاحظات). */
    @Serializable
    data object Tasks : AppRoute

    /** شاشة فرعية تُفتح من الضبط — ليست تبويبًا. */
    @Serializable
    data object Widgets : AppRoute

    /** فرعية «المفاتيح» — فهرس مفاتيح الجنان (الأجزاء والأبواب والأدعية). */
    @Serializable
    data object Jinan : AppRoute

    /** فرعية «المفاتيح» — قارئ الجنان الموحّد (لأي دعاء وشهر). */
    @Serializable
    data class JinanReader(val contentId: Int, val title: String) : AppRoute

    /** «أعمال اليوم» — أعمال الليلة/اليوم/يوم الأسبوع من مفاتيح الجنان. */
    @Serializable
    data object DailyDeeds : AppRoute
}

/**
 * إعداد `rememberNavBackStack` لاسترجاع الـback stack بعد قتل العملية.
 */
val appNavConfiguration: SavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(AppRoute.Today::class, AppRoute.Today.serializer())
            subclass(AppRoute.Calendar::class, AppRoute.Calendar.serializer())
            subclass(AppRoute.Keys::class, AppRoute.Keys.serializer())
            subclass(AppRoute.Fiqh::class, AppRoute.Fiqh.serializer())
            subclass(AppRoute.Settings::class, AppRoute.Settings.serializer())
            subclass(AppRoute.Duas::class, AppRoute.Duas.serializer())
            subclass(AppRoute.Quran::class, AppRoute.Quran.serializer())
            subclass(AppRoute.Tasbih::class, AppRoute.Tasbih.serializer())
            subclass(AppRoute.Qibla::class, AppRoute.Qibla.serializer())
            subclass(AppRoute.Tasks::class, AppRoute.Tasks.serializer())
            subclass(AppRoute.Widgets::class, AppRoute.Widgets.serializer())
            subclass(AppRoute.Jinan::class, AppRoute.Jinan.serializer())
            subclass(AppRoute.JinanReader::class, AppRoute.JinanReader.serializer())
            subclass(AppRoute.DailyDeeds::class, AppRoute.DailyDeeds.serializer())
        }
    }
}

/** تحويل وجهة شريط التنقل إلى مسار. */
fun AppDestination.toRoute(): AppRoute = when (this) {
    AppDestination.TODAY -> AppRoute.Today
    AppDestination.CALENDAR -> AppRoute.Calendar
    AppDestination.KEYS -> AppRoute.Keys
    AppDestination.FIQH -> AppRoute.Fiqh
    AppDestination.SETTINGS -> AppRoute.Settings
}

/** الوجهة المطابقة للمسار الحالي (لإبراز التبويب). */
fun AppRoute.toDestination(): AppDestination? = when (this) {
    AppRoute.Today -> AppDestination.TODAY
    AppRoute.Calendar -> AppDestination.CALENDAR
    AppRoute.Keys -> AppDestination.KEYS
    AppRoute.Fiqh -> AppDestination.FIQH
    AppRoute.Settings -> AppDestination.SETTINGS
    AppRoute.Duas,
    AppRoute.Quran,
    AppRoute.Tasbih,
    AppRoute.Qibla,
    AppRoute.Tasks,
    AppRoute.Jinan,
    is AppRoute.JinanReader,
    AppRoute.DailyDeeds -> AppDestination.KEYS
    AppRoute.Widgets -> AppDestination.SETTINGS
}
