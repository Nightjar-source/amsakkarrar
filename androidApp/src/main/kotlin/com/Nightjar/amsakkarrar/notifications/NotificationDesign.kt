package com.Nightjar.amsakkarrar.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.Nightjar.amsakkarrar.MainActivity
import com.Nightjar.amsakkarrar.R

/**
 * بناء الإشعارين بتصميم `widgetsnotifications.html` حرفيًا:
 *
 * 1) الإشعار الحي «رمضان مباشر»: شعار + نقطة حمراء نابضة + «باقي على …» +
 *    وقت الصلاة القادمة + عدّاد تنازلي حي (Chronometer) + شريط تقدّم بشكل حبة دواء.
 * 2) إشعار الصلاة: «حان الآن موعد …» + «تقبل الله طاعتكم» + مربعان
 *    (الوقت الحالي / الصلاة القادمة).
 */
object NotificationDesign {

    const val LIVE_ID = 3100
    const val PRAYER_ID = 3101

    fun contentIntent(context: Context): PendingIntent = PendingIntent.getActivity(
        context,
        900,
        Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // ---------------------------------------------------------------
    //  الإشعار الحي
    // ---------------------------------------------------------------
    fun buildLive(context: Context, snapshot: PrayerSnapshot): Notification {
        val views = RemoteViews(context.packageName, R.layout.notification_live)

        views.setTextViewText(R.id.live_label, "باقي على ${snapshot.nextLabel}")
        views.setTextViewText(R.id.live_target, snapshot.nextTime)

        val remaining = snapshot.remainingSeconds.coerceAtLeast(0)
        views.setChronometer(
            R.id.live_chrono,
            SystemClock.elapsedRealtime() + remaining * 1000L,
            null,
            true
        )
        @Suppress("NewApi")
        views.setChronometerCountDown(R.id.live_chrono, true)

        val percent = (snapshot.periodProgress * 100f).toInt().coerceIn(0, 100)
        val dayBar = R.id.live_progress_day
        val nightBar = R.id.live_progress_night
        views.setViewVisibility(dayBar, if (snapshot.isDaytime) View.VISIBLE else View.GONE)
        views.setViewVisibility(nightBar, if (snapshot.isDaytime) View.GONE else View.VISIBLE)
        views.setProgressBar(if (snapshot.isDaytime) dayBar else nightBar, 100, percent, false)

        return NotificationCompat.Builder(context, NotificationChannels.LIVE)
            .setSmallIcon(R.drawable.ic_notif_small)
            .setCustomContentView(views)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentIntent(context))
            .build()
    }

    // ---------------------------------------------------------------
    //  إشعار الصلاة
    // ---------------------------------------------------------------
    fun buildPrayer(
        context: Context,
        prayerLabel: String,
        currentTime: String,
        nextLabel: String,
        nextTime: String
    ): Notification {
        val views = RemoteViews(context.packageName, R.layout.notification_prayer)
        views.setTextViewText(R.id.prayer_name, prayerLabel)
        views.setTextViewText(R.id.prayer_current, currentTime)
        views.setTextViewText(R.id.prayer_next_label, "الصلاة القادمة ($nextLabel)")
        views.setTextViewText(R.id.prayer_next_time, nextTime)

        return NotificationCompat.Builder(context, NotificationChannels.ADHAN)
            .setSmallIcon(R.drawable.ic_notif_small)
            .setCustomContentView(views)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentIntent(context))
            .build()
    }
}
