package com.Nightjar.amsakkarrar.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.karrar.amsakiah.core.settings.AppDataStore

/**
 * **نسخة ألوان خفيفة للودجت فقط** — لا تُستخدم في أي شاشة من شاشات التطبيق.
 *
 * سبب التصميم (حسب قاعدة AppWidgetProvider):
 * - الودجت يعمل كـBroadcastReceiver متزامن على خيط النظام الرئيسي، ولا يدعم
 *   الدوال المعلّقة (suspend) عند بناء RemoteViews.
 * - لذلك تُقرأ الألوان من كاش الذاكرة الفوري في [AppDataStore] — عملية `O(1)`
 *   بلا انتظار DataStore ولا دورة حياة كاملة (0ms).
 * - واجهات التطبيق تستخدم `LocalKararPalette` الخاص بـ Compose، **وليست** هذه النسخة.
 */
object WidgetColors {

    /** قراءة مصغّرة متزامنة جدًا — تُستدعى داخل provideGlance/RemoteViews. */
    data class Snapshot(
        val background: Long,
        val border: Long,
        val soft: Long,
        val isArc: Boolean
    ) {
        /** تحويل إلى Compose Color للاستخدام داخل Glance فقط. */
        val composeBackground: Color get() = Color(background)
        val composeBorder: Color get() = Color(border)
        val composeSoft: Color get() = Color(soft)
    }

    fun snapshot(context: Context, appWidgetId: Int): Snapshot {
        val style = WidgetStyleStore.current(context, appWidgetId)
        return Snapshot(
            background = WidgetStyleStore.backgroundArgb(context, appWidgetId),
            border = WidgetStyleStore.borderArgb(context, appWidgetId),
            soft = WidgetStyleStore.softBackgroundArgb(context, appWidgetId),
            isArc = style == WidgetStyleStore.ARC
        )
    }

}
