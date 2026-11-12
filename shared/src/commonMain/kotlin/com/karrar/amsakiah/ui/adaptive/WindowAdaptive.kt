package com.karrar.amsakiah.ui.adaptive

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass

/**
 * منطق التكيّف الخالص (Pure Adaptive Logic).
 *
 * هذا الملف لا يرسم أي واجهة، بل يحوّل معلومات النافذة الرسمية من
 * `androidx.window.core.layout.WindowSizeClass` إلى قرارات تصميمية بسيطة.
 * الفصل بين المنطق والواجهة مطلب رسمي في دليل Compose التكيّفي لشركة Google.
 *
 * المراجع الرسمية:
 * - https://developer.android.com/develop/adaptive-apps/guides/use-window-size-classes
 * - https://kotlinlang.org/docs/multiplatform/compose-adaptive-layouts.html#using-window-size-classes
 * - https://m3.material.io/foundations/layout/breakpoints/overview
 */

/** فئات عرض النافذة الرسمية (Window width size classes). */
enum class AppWidthSizeClass {
    /** أقل من 600dp — الهواتف عموديًا. */
    COMPACT,

    /** 600dp وأكثر — الهواتف بالعرض، المطويات المفتوحة، التابلت الصغير. */
    MEDIUM,

    /** 840dp وأكثر — التابلت. */
    EXPANDED,

    /** 1200dp وأكثر — التابلت الكبير وأجهزة سطح المكتب. */
    LARGE,

    /** 1600dp وأكثر — الشاشات فائقة الاتساع. */
    EXTRA_LARGE
}

/** فئات ارتفاع النافذة الرسمية (Window height size classes). */
enum class AppHeightSizeClass {
    /** أقل من 480dp — الهواتف بالعرض، المطويات الأفقية. */
    COMPACT,

    /** 480dp وأكثر. */
    MEDIUM,

    /** 900dp وأكثر — الشاشات الطويلة والتابلت العمودي. */
    EXPANDED
}

/** شكل التنقل الجذري المناسب لعرض النافذة الحالي (M3 Expressive). */
enum class NavigationLayout {
    /** شريط سفلي عائم/ممتد — للهواتف (كومباكت). */
    BOTTOM_BAR,

    /** شريط تنقل جانبي (NavigationRail) — للشاشات المتوسطة والعريضة. */
    RAIL
}

/**
 * النتيجة الكاملة لقرار التكيّف، تُستهلك في جذر التطبيق فقط.
 *
 * @param widthSizeClass فئة العرض الرسمية.
 * @param heightSizeClass فئة الارتفاع الرسمية.
 * @param navigationLayout شكل التنقل المختار.
 * @param isTabletop وضع "الطاولة" في المطويات نصف المفتوحة.
 */
data class AppAdaptiveLayout(
    val widthSizeClass: AppWidthSizeClass,
    val heightSizeClass: AppHeightSizeClass,
    val navigationLayout: NavigationLayout,
    val isTabletop: Boolean
)

/** هل النافذة في فئة العرض المضغوط (هاتف عمودي)؟ */
val AppWidthSizeClass.isCompact: Boolean
    get() = this == AppWidthSizeClass.COMPACT

/** هل توفّر النافذة مساحة كافية لعرض قائمة تفاصيل بجانب المحتوى؟ */
val AppWidthSizeClass.supportsListDetail: Boolean
    get() = this != AppWidthSizeClass.COMPACT

/**
 * تحويل [WindowSizeClass] الرسمي إلى فئة عرض التطبيق.
 * نستخدم الدوال الرسمية `isWidthAtLeastBreakpoint` مع ثوابت الفواصل الرسمية
 * بدلًا من `WindowWidthSizeClass` المهجورة في window-core 1.4+.
 */
fun WindowSizeClass.toAppWidthSizeClass(): AppWidthSizeClass = when {
    isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND) -> AppWidthSizeClass.EXTRA_LARGE
    isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND) -> AppWidthSizeClass.LARGE
    isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> AppWidthSizeClass.EXPANDED
    isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> AppWidthSizeClass.MEDIUM
    else -> AppWidthSizeClass.COMPACT
}

/** تحويل [WindowSizeClass] الرسمي إلى فئة ارتفاع التطبيق. */
fun WindowSizeClass.toAppHeightSizeClass(): AppHeightSizeClass = when {
    isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND) -> AppHeightSizeClass.EXPANDED
    isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND) -> AppHeightSizeClass.MEDIUM
    else -> AppHeightSizeClass.COMPACT
}

/** هل النافذة في فئة الارتفاع المضغوط (هاتف بالعرض/مطوية أفقية)؟ */
val AppHeightSizeClass.compact: Boolean
    get() = this == AppHeightSizeClass.COMPACT

/** ارتفاع الشريط العلوي ديناميكيًا حسب فئة الارتفاع الرسمية (لا ثابتًا 56dp). */
fun AppAdaptiveLayout.topBarHeight() = when (heightSizeClass) {
    AppHeightSizeClass.COMPACT -> 48.dp
    AppHeightSizeClass.MEDIUM -> 56.dp
    AppHeightSizeClass.EXPANDED -> 64.dp
}

/**
 * هل النافذة لديها عروضٌ كافية لترتيب عمودين جنبًا إلى جنب؟
 * MEDIUM+ (الهاتف بالعرض، اللوحيات، وضع تقسيم الشاشة متى بلغ العرض 600dp+).
 */
val AppWidthSizeClass.supportsTwoPane: Boolean
    get() = this != AppWidthSizeClass.COMPACT

/**
 * القاعدة التصميمية للتنقل، مستمدة من Material 3 Expressive الرسمية:
 *
 * | العرض            | التنقل                       |
 * | ---------------- | ---------------------------- |
 * | Compact          | شريط سفلي                    |
 * | Medium           | شريط جانبي (Rail) 64dp       |
 * | Expanded +       | شريط جانبي (Rail) 72dp       |
 *
 * حالات خاصة:
 * - ارتفاع مضغوط (هاتف بالعرض أو مطوية أفقية): الشريط الجانبي أنسب من الشريط
 *   السفلي لأنه لا يسرق المساحة العمودية القليلة.
 * - وضع الطاولة (Tabletop): تُبقى أدوات التنقل في النصف السفلي القابل للمس،
 *   لذلك نعتمد الشريط السفلي حتى على العروض المتوسطة والكبيرة.
 * - في M3 Expressive الـ Navigation Drawer لم تعد مُوصى بها؛ البديل الرسمي
 *   هو الـ Collapsed Navigation Rail (أخبار/أيقونات+تسميات سطرية).
 */
fun resolveNavigationLayout(
    widthSizeClass: AppWidthSizeClass,
    heightSizeClass: AppHeightSizeClass,
    isTabletop: Boolean = false
): NavigationLayout = when {
    isTabletop -> NavigationLayout.BOTTOM_BAR
    heightSizeClass == AppHeightSizeClass.COMPACT && !widthSizeClass.isCompact -> NavigationLayout.RAIL
    widthSizeClass.isCompact -> NavigationLayout.BOTTOM_BAR
    else -> NavigationLayout.RAIL
}

/** بناء [AppAdaptiveLayout] من معلومات النافذة الخام. */
fun WindowSizeClass.toAppAdaptiveLayout(isTabletop: Boolean = false): AppAdaptiveLayout {
    val width = toAppWidthSizeClass()
    val height = toAppHeightSizeClass()
    return AppAdaptiveLayout(
        widthSizeClass = width,
        heightSizeClass = height,
        navigationLayout = resolveNavigationLayout(width, height, isTabletop),
        isTabletop = isTabletop
    )
}

/**
 * الجسر الوحيد بين Compose ومنطق التكيّف.
 * يستخدم الدالة الرسمية `currentWindowAdaptiveInfoV2()` التي تدعم فئتي
 * Large وExtra Large، وتتحدث تلقائيًا عند تغيّر حجم النافذة
 * (تدوير الشاشة، فتح المطوية، تقسيم الشاشة، شريط المهام في التابلت).
 */
@Composable
fun rememberAppAdaptiveLayout(): AppAdaptiveLayout {
    val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
    val windowSizeClass = windowAdaptiveInfo.windowSizeClass
    val isTabletop = windowAdaptiveInfo.windowPosture.isTabletop
    return remember(windowSizeClass, isTabletop) {
        windowSizeClass.toAppAdaptiveLayout(isTabletop)
    }
}
