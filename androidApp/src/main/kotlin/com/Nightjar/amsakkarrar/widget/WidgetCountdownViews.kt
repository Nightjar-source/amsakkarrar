package com.Nightjar.amsakkarrar.widget

import android.content.Context
import android.os.SystemClock
import android.widget.RemoteViews
import com.Nightjar.amsakkarrar.R
import com.Nightjar.amsakkarrar.notifications.PrayerSnapshot

/**
 * عدّاد ودجت أصلي (RemoteViews + Chronometer + ProgressBar).
 *
 * لماذا Chronometer؟
 * - أندرويد يقيّد تحديث الودجت إلى مرة كل 15 دقيقة، لذا لا يمكن تحديث الثواني
 *   عبر Glance/WorkManager كل ثانية.
 * - Chronometer داخليًا يعتمد على SystemClock.elapsedRealtime (ساعة الجهاز
 *   المونوتونية)، فيعدّ بنفسه كل ثانية بدون أي بث أو خدمة، ويستمر حتى لو
 *   تغيّر وقت الجهاز أو دخل في Doze.
 * - عند إعادة التشغيل أو تغيير الوقت/المنطقة يعيد BootReceiver بناء الودجت
 *   ويحسب القاعدة من جديد، فيبقى متزامنًا مع وقت الجهاز.
 */
fun countdownRemoteViews(
    context: Context,
    snapshot: PrayerSnapshot,
    compact: Boolean = false
): RemoteViews {
    val layout = if (compact) R.layout.widget_chrono_compact else R.layout.widget_chrono_large
    val views = RemoteViews(context.packageName, layout)

    views.setTextViewText(R.id.widget_chrono_label, "باقي على ${snapshot.nextLabel}")

    // تصحيح الانحراف: اللقطة قد تكون من كاش عمره ثوانٍ؛ نطرح الفارق حتى يطابق
    // عدّاد الودجت الإشعار الحي وساعة الجهاز بالثانية.
    val driftMs = if (snapshot.generatedAtElapsed > 0L) {
        (SystemClock.elapsedRealtime() - snapshot.generatedAtElapsed).coerceAtLeast(0L)
    } else {
        0L
    }
    val remainingMillis = (snapshot.remainingSeconds * 1000L - driftMs).coerceAtLeast(1_000L)
    views.setChronometer(
        R.id.widget_chrono,
        SystemClock.elapsedRealtime() + remainingMillis,
        null,
        true
    )
    @Suppress("NewApi")
    views.setChronometerCountDown(R.id.widget_chrono, true)

    if (!compact) {
        views.setTextViewText(R.id.widget_chrono_target, "موعد ${snapshot.nextTime} • ${snapshot.city}")
        // شريط تقدم أصلي داخل الودجت — نسبة واحدة مع الإشعار الحي، ولون واحد
        // (نهاري/ليلي) بنفس لون accent في NotificationDesign.
        val percent = (snapshot.periodProgress * 100f).toInt().coerceIn(0, 100)
        val dayId = R.id.widget_chrono_progress_day
        val nightId = R.id.widget_chrono_progress_night
        val visibleId = if (snapshot.isDaytime) dayId else nightId
        val hiddenId = if (snapshot.isDaytime) nightId else dayId
        views.setViewVisibility(visibleId, android.view.View.VISIBLE)
        views.setViewVisibility(hiddenId, android.view.View.GONE)
        views.setProgressBar(visibleId, 100, percent, false)
        // طرفا الشريط (widgetsnotifications.html: منتصف الليل ← الإمساك).
        views.setTextViewText(R.id.widget_progress_start, snapshot.periodStartLabel)
        views.setTextViewText(R.id.widget_progress_end, snapshot.periodEndLabel)
    }

    return views
}

/**
 * ساعة الودجت — TextClock أصلي يتحدّث كل ثانية/دقيقة من نظام الوقت نفسه
 * دون أي بث أو خدمة أو تحديث من التطبيق.
 */
fun clockRemoteViews(
    context: Context,
    snapshot: PrayerSnapshot,
    compact: Boolean = false
): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.widget_clock)
    val parts = snapshot.gregorianText.split(" ")
    val dayName = parts.firstOrNull().orEmpty()
    views.setTextViewText(R.id.widget_clock_day, dayName)
    views.setTextViewText(R.id.widget_clock_date, snapshot.gregorianText.removePrefix("$dayName "))
    views.setTextViewText(R.id.widget_clock_city, snapshot.city)
    views.setViewVisibility(
        R.id.widget_clock_day,
        if (compact) android.view.View.GONE else android.view.View.VISIBLE
    )
    views.setViewVisibility(
        R.id.widget_clock_city,
        if (compact) android.view.View.GONE else android.view.View.VISIBLE
    )
    return views
}
