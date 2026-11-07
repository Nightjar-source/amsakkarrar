package com.Nightjar.amsakkarrar.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.Nightjar.amsakkarrar.MainActivity
import com.Nightjar.amsakkarrar.R

/**
 * بناء الإشعارين بتصميم `widgetsnotifications.html`:
 *
 * 1) الإشعار الحي «رمضان مباشر»:
 *    - Android 16+ (API 36): Live Update مع Notification.ProgressStyle
 *      و shortCriticalText وطلب الترقية (setRequestPromotedOngoing) — تظهر
 *      في شريحة الحالة/شاشة القفل كما في نظام أندرويد 16، وتتوافق مع
 *      One UI 7+ و«الآن بار» في سامسونج.
 *    - ما قبل Android 16: RemoteViews مخصص مطابق لملف HTML مع Chronometer
 *      حي وشريط تقدم متدرّج نهاري/ليلي.
 * 2) إشعار الصلاة: «حان الآن موعد …» + «تقبل الله طاعتكم».
 */
object NotificationDesign {

    const val LIVE_ID = 3100
    const val PRAYER_ID = 3101

    /** Android 16 (Baklava) — أول إصدار يدعم الإشعارات الحية المروَّجة */
    private const val API_LIVE_UPDATES = 36

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
        val remaining = snapshot.remainingSeconds.coerceAtLeast(0)
        val percent = (snapshot.periodProgress * 100f).toInt().coerceIn(0, 100)
        return if (Build.VERSION.SDK_INT >= API_LIVE_UPDATES) {
            buildLiveUpdates(context, snapshot, percent, remaining)
        } else {
            buildLiveCompat(context, snapshot, percent, remaining)
        }
    }

    /**
     * Android 16+ — Live Updates:
     * شريحة حالة + Now Bar/One UI 7+ مع شريط تقدم غني ملوّن
     * ونقطة تتبّع (tracker) على شكل هلال، ونص قصير للعدّاد.
     */
    @Suppress("NewApi")
    private fun buildLiveUpdates(
        context: Context,
        snapshot: PrayerSnapshot,
        percent: Int,
        remaining: Int
    ): Notification {
        val accent = if (snapshot.isDaytime) 0xFFFF9F1C.toInt() else 0xFF80A1D4.toInt()
        val progressStyle = Notification.ProgressStyle()
            .setProgress(percent)
            .setStyledByProgress(false)
            .setProgressSegments(
                listOf(Notification.ProgressStyle.Segment(100).setColor(accent))
            )
            .addProgressPoint(Notification.ProgressStyle.Point(0).setColor(0x66FFD700.toInt()))
            .addProgressPoint(Notification.ProgressStyle.Point(percent).setColor(accent))
            .addProgressPoint(Notification.ProgressStyle.Point(100).setColor(0xFFFFD700.toInt()))
            .setProgressTrackerIcon(Icon.createWithResource(context, R.drawable.ic_notif_moon))

        return Notification.Builder(context, NotificationChannels.LIVE)
            .setSmallIcon(R.drawable.ic_notif_small)
            .setContentTitle("رمضان مباشر")
            .setContentText("باقي على ${snapshot.nextLabel} • ${snapshot.nextTime}")
            .setSubText(snapshot.city)
            .setShortCriticalText(criticalRemaining(snapshot))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(true)
            .setWhen(System.currentTimeMillis() + remaining * 1000L)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setCategory(Notification.CATEGORY_STOPWATCH)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setContentIntent(contentIntent(context))
            .setStyle(progressStyle)
            .setRequestPromotedOngoing(true)
            .build()
    }

    /**
     * Android 7–15 (و One UI 7 على أندرويد 15) — RemoteViews مخصص
     * مطابق لتصميم الملف: رأس + عدّاد حي + شريط تقدم متدرج.
     */
    private fun buildLiveCompat(
        context: Context,
        snapshot: PrayerSnapshot,
        percent: Int,
        remaining: Int
    ): Notification {
        val views = RemoteViews(context.packageName, R.layout.notification_live)

        views.setTextViewText(R.id.live_label, "باقي على ${snapshot.nextLabel}")
        views.setTextViewText(R.id.live_target, snapshot.nextTime)
        views.setTextViewText(R.id.live_city, snapshot.city)

        views.setChronometer(
            R.id.live_chrono,
            SystemClock.elapsedRealtime() + remaining * 1000L,
            null,
            true
        )
        @Suppress("NewApi")
        views.setChronometerCountDown(R.id.live_chrono, true)

        val dayBar = R.id.live_progress_day
        val nightBar = R.id.live_progress_night
        views.setViewVisibility(dayBar, if (snapshot.isDaytime) View.VISIBLE else View.GONE)
        views.setViewVisibility(nightBar, if (snapshot.isDaytime) View.GONE else View.VISIBLE)
        views.setProgressBar(if (snapshot.isDaytime) dayBar else nightBar, 100, percent, false)

        return NotificationCompat.Builder(context, NotificationChannels.LIVE)
            .setSmallIcon(R.drawable.ic_notif_small)
            .setContentTitle("رمضان مباشر")
            .setContentText("باقي على ${snapshot.nextLabel} • ${snapshot.nextTime}")
            .setCustomContentView(views)
            .setCustomBigContentView(views)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setShowWhen(true)
            .setWhen(System.currentTimeMillis() + remaining * 1000L)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentIntent(context))
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
        channel: String = NotificationChannels.ADHAN
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
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(0xFFFFD700.toInt())
            .setColorized(true)
            .setContentIntent(contentIntent(context))
            .build()
    }
}
