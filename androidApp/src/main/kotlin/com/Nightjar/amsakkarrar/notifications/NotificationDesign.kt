package com.Nightjar.amsakkarrar.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import com.Nightjar.amsakkarrar.MainActivity
import com.Nightjar.amsakkarrar.R

/**
 * بناء الإشعارين بتصميم `widgetsnotifications.html`:
 *
 * 1) الإشعار الحي «رمضان مباشر»:
 *    - `NotificationCompat.ProgressStyle` موحّد على كل الإصدارات، بلا أي
 *      `customContentView`. هذا شرط أساسي لقبوله كـ Live Update مروّج على
 *      Android 16+ (developer.android.com/develop/ui/views/notifications/live-update).
 *    - على Android 16+ يضيف `setShortCriticalText` و`setRequestPromotedOngoing`
 *      فتظهر الشريحة في شريط الحالة وشاشة القفل؛ وعلى Android 7–15 يعمل
 *      كإشعار قياسي بعدّاد تنازلي أصلي، بلا RemoteViews مخصص (أقل ذاكرة
 *      وأقل عرضة للتسريب).
 * 2) إشعار الصلاة: «حان الآن موعد …» + «تقبل الله طاعتكم».
 */
object NotificationDesign {

    const val LIVE_ID = 3100
    const val PRAYER_ID = 3101
    const val FALLBACK_ID = 3102

    fun contentIntent(context: Context): PendingIntent = PendingIntent.getActivity(
        context,
        900,
        Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    /**
     * يُستدعى عند سحب الإشعار الحي أو إخفائه من النظام.
     * بدونه يعيد التحديث الدوري نشره كل 15 دقيقة — وهو ما يزعج المستخدم.
     */
    private fun liveDismissIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        901,
        Intent(context, LiveDismissReceiver::class.java).apply {
            action = LiveDismissReceiver.ACTION_DISMISS
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // ---------------------------------------------------------------
    //  الإشعار الحي
    // ---------------------------------------------------------------
    fun buildLive(context: Context, snapshot: PrayerSnapshot): Notification {
        val remaining = snapshot.remainingSeconds.coerceAtLeast(0)
        val percent = (snapshot.periodProgress * 100f).toInt().coerceIn(0, 100)
        val accent = if (snapshot.isDaytime) 0xFFFF9F1C.toInt() else 0xFF80A1D4.toInt()

        // ProgressStyle عبر NotificationCompat: يتوافق مع Android 7 ويُرقّى
        // تلقائيًا إلى Live Update على Android 16+، ولا يحتاج RemoteViews.
        val progressStyle = NotificationCompat.ProgressStyle()
            .setProgress(percent)
            .setStyledByProgress(false)
            .setProgressSegments(
                listOf(NotificationCompat.ProgressStyle.Segment(100).setColor(accent))
            )
            .addProgressPoint(NotificationCompat.ProgressStyle.Point(percent).setColor(accent))
            .setProgressTrackerIcon(IconCompat.createWithResource(context, R.drawable.ic_notif_moon))

        return NotificationCompat.Builder(context, NotificationChannels.LIVE)
            .setSmallIcon(R.drawable.ic_notif_small)
            .setContentTitle("رمضان مباشر")
            .setContentText("باقي على ${snapshot.nextLabel} • ${snapshot.nextTime}")
            .setSubText(snapshot.city)
            // شريحة الحالة النصية القصيرة — تُهمَل تلقائيًا قبل Android 16.
            .setShortCriticalText(criticalRemaining(snapshot))
            .setStyle(progressStyle)
            // شريط تقدم قياسي على Android 7–15؛ ويتجاهله النظام على 16+ لأن
            // ProgressStyle هو المسؤول عن العرض هناك.
            .setProgress(100, percent, false)
            // مستمر على Android 16+ (شرط الإشعار المروّج، ويبقى قابلًا للسحب)،
            // وقابل للسحب على الإصدارات الأقدم حيث لا يوجد مفهوم Live Update.
            .setOngoing(android.os.Build.VERSION.SDK_INT >= 36)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setShowWhen(true)
            .setWhen(System.currentTimeMillis() + remaining * 1000L)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentIntent(context))
            // يمكن سحب الإشعار؛ وLiveDismissReceiver يمنع إعادة نشره حتى
            // دخول وقت الصلاة القادم.
            .setDeleteIntent(liveDismissIntent(context))
            // طلب الترقية إلى Live Update (لا أثر له قبل Android 16).
            .setRequestPromotedOngoing(true)
            .build()
    }

    private fun criticalRemaining(snapshot: PrayerSnapshot): String {
        val totalMinutes = (snapshot.remainingSeconds.coerceAtLeast(0) + 59) / 60
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) "$hours:${minutes.toString().padStart(2, '0')}" else "$minutes د"
    }

    // ---------------------------------------------------------------
    //  إشعار الصلاة
    // ---------------------------------------------------------------
    fun buildPrayer(
        context: Context,
        prayerLabel: String,
        currentTime: String,
        nextLabel: String,
        nextTime: String,
        channel: String = NotificationChannels.ADHAN,
        timeoutAfterMs: Long = 2 * 60 * 60 * 1000L
    ): Notification {
        val views = RemoteViews(context.packageName, R.layout.notification_prayer)
        views.setTextViewText(R.id.prayer_name, prayerLabel)
        views.setTextViewText(R.id.prayer_current, currentTime)
        views.setTextViewText(R.id.prayer_next_label, "الصلاة القادمة ($nextLabel)")
        views.setTextViewText(R.id.prayer_next_time, nextTime)

        return NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notif_small)
            .setCustomContentView(views)
            .setCustomBigContentView(views)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setTimeoutAfter(timeoutAfterMs)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(0xFFFFD700.toInt())
            .setColorized(true)
            .setContentIntent(contentIntent(context))
            .build()
    }
}
