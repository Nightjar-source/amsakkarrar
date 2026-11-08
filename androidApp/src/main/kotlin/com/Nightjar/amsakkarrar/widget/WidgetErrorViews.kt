package com.Nightjar.amsakkarrar.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.Nightjar.amsakkarrar.MainActivity
import com.Nightjar.amsakkarrar.R

/**
 * بديل آمن عند فشل تركيب أي ودجت Glance.
 *
 * لماذا نحتاجه؟
 * - الافتراضي في Glance يعرض `glance_error_layout` العام بنص إنجليزي، وقد
 *   يرمي الاستثناء مجددًا إذا لم يوجد.
 * - سياسة Android 17 للودجتات: أي `RemoteViews` يتجاوز
 *   `6 × العرض × الارتفاع` بكسل يُرفض بـ`IllegalArgumentException`. لذلك
 *   البديل هنا نصّي فقط بلا صور، فلا يستهلك إلا بضع كيلوبايتات.
 * - نُسجّل الخطأ في logcat مرة واحدة لكل ودجت لتشخيصه دون إغراق السجل.
 *
 * مرجع: developer.android.com/about/versions/17/behavior-changes-17
 *       developer.android.com/reference/kotlin/androidx/glance/appwidget/GlanceAppWidget#onCompositionError
 */
internal object WidgetErrorViews {

    private const val TAG = "KararWidget"
    private val reported = mutableSetOf<Int>()

    fun show(context: Context, appWidgetId: Int, throwable: Throwable) {
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return
        if (reported.add(appWidgetId)) {
            Log.w(TAG, "فشل تركيب الودجت #$appWidgetId — سيُعرض البديل الآمن", throwable)
        }
        val views = RemoteViews(context.packageName, R.layout.widget_error)
        views.setOnClickPendingIntent(R.id.widget_error_root, openApp(context, appWidgetId))
        runCatching {
            AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views)
        }.onFailure {
            Log.e(TAG, "تعذّر عرض بديل الودجت #$appWidgetId", it)
        }
    }

    /** الضغط على البديل يفتح التطبيق ويطلب إعادة تحديث الودجت. */
    private fun openApp(context: Context, appWidgetId: Int): PendingIntent =
        PendingIntent.getActivity(
            context,
            7000 + appWidgetId,
            Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_MAIN
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_REFRESH_WIDGET_ID, appWidgetId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    const val EXTRA_REFRESH_WIDGET_ID = "karar.refresh_widget_id"
}
