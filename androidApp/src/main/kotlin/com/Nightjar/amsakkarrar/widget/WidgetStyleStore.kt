package com.Nightjar.amsakkarrar.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import com.karrar.amsakiah.core.widgets.WidgetPinBridge

/**
 * نمط خلفية الودجتات — يُحفظ في SharedPreferences.
 *
 * يدعم نمطين من التخزين:
 * - نمط عام لكل الودجتات (من شاشة «الخلفية» داخل التطبيق).
 * - نمط خاص لكل ودجت عبر `appWidgetId` (من شاشة إعدادات الودجت / شاشة «الودجتات»).
 *
 * ملاحظة تقنية: RemoteViews لا يدعم Blur الحقيقي (RenderEffect) خارج عملية
 * التطبيق، لذلك «الضبابي» يُحاكى بطبقة شبه شفافة مع حدود لامعة، والشفاف
 * يعرض خلفية الشاشة الرئيسية.
 */
object WidgetStyleStore {

    const val MATERIAL = WidgetPinBridge.STYLE_MATERIAL
    const val GLASS = WidgetPinBridge.STYLE_GLASS
    const val BLUR = WidgetPinBridge.STYLE_BLUR
    const val TRANSPARENT = WidgetPinBridge.STYLE_TRANSPARENT
    const val DEEP = WidgetPinBridge.STYLE_DEEP

    private const val PREFS = "karar_widget_style"
    private const val KEY = "style"
    private const val KEY_PREFIX = "style_"

    private val VALID = setOf(MATERIAL, GLASS, BLUR, TRANSPARENT, DEEP)

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun sanitize(value: String?): String =
        value?.takeIf { it in VALID } ?: DEEP

    /** النمط العام (يُستخدم عندما لا يوجد نمط خاص بالودجت). */
    fun current(context: Context): String = sanitize(prefs(context).getString(KEY, DEEP))

    /** نمط ودجت محدّد، مع الرجوع إلى النمط العام. */
    fun current(context: Context, appWidgetId: Int): String {
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return current(context)
        val value = prefs(context).getString(KEY_PREFIX + appWidgetId, null)
        return if (value == null) current(context) else sanitize(value)
    }

    fun set(context: Context, style: String) {
        prefs(context).edit().putString(KEY, sanitize(style)).apply()
    }

    /** يحفظ نمط ودجت واحد فقط دون التأثير على بقية الودجتات. */
    fun set(context: Context, style: String, appWidgetId: Int) {
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            set(context, style)
        } else {
            prefs(context).edit()
                .putString(KEY_PREFIX + appWidgetId, sanitize(style))
                .apply()
        }
    }

    /** لون ARGB للخلفية الرئيسية. */
    fun backgroundArgb(context: Context, appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID): Long =
        when (current(context, appWidgetId)) {
            MATERIAL -> 0xFF0E1626
            GLASS -> 0xCC141C2E
            BLUR -> 0xD91B2438
            TRANSPARENT -> 0x33000000
            else -> 0xF2050A15
        }

    /** لون الحدود اللامعة (يُحاكي border في Glance عبر إطار خارجي). */
    fun borderArgb(context: Context, appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID): Long =
        when (current(context, appWidgetId)) {
            GLASS -> 0x66FFFFFF
            BLUR -> 0x40FFFFFF
            TRANSPARENT -> 0x22FFFFFF
            else -> 0x1AFFD700
        }

    /** لون ARGB للخلفية الثانوية (البطاقات الداخلية). */
    fun softBackgroundArgb(context: Context, appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID): Long =
        when (current(context, appWidgetId)) {
            MATERIAL -> 0xFF1B2438
            GLASS -> 0x33FFFFFF
            BLUR -> 0x99FFFFFF
            TRANSPARENT -> 0x1FFFFFFF
            else -> 0xE60F1524
        }

    /** هل الخلفية فاتحة بما يكفي لاستخدام نص داكن؟ */
    fun isLight(context: Context): Boolean = current(context) == MATERIAL
}
