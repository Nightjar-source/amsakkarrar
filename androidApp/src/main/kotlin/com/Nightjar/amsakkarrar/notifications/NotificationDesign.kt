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
 * بناء الإشعارين بتصميم `widgetsnotifications.html`:
 *
 * 1) الإشعار الحي «رمضان مباشر» — بطاقة `RemoteViews` مصمّمة
 *    (`notification_live.xml`):
 *    - هيدر + وقت الصلاة القادمة + عدّاد `Chronometer` تنازلي حقيقي
 *      ينبض داخل النظام (لا تحديث من التطبيق ⇒ صفر استهلاك إضافي)،
 *      وشريط تقدّم بشكل حبة دواء.
 *    - `setOngoing(false)` ⇒ قابل للسحب، والسحب يمنع إعادته
 *      (`LiveDismissReceiver`).
 *    - لا `setRequestPromotedOngoing` ولا `setShortCriticalText`: كانا
 *      يُظهران شريحة إضافية فوق البطاقة على Android 16+ فيبدو الأمر
 *      كإشعارين لنفس العدّاد.
 *    - `setColorized(false)`: يبقى الإطار إطار أندرويد الأصلي.
 * 2) إشعار الصلاة: «حان الآن موعد …» + «تقبل الله طاعتكم»، وفيه زر
 *    «إيقاف الأذان» وسحبُ البطاقة يوقف الصوت عبر `AdhanStopReceiver`.
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
    /**
     * الإشعار الحي — نسخة حرفية من `widgetsnotifications.html`:
     * كبسولة داكنة متدرجة + «رمضان مباشر» + الوقت المستهدف + عدّاد تنازلي
     * حقيقي (Chronometer) + شريط تقدّم بشكل حبة دواء.
     *
     * قرارات مقصودة:
     *  - **لا** `setRequestPromotedOngoing` ولا `setShortCriticalText`: كانت
     *    تُظهر شريحة إضافية في شريط الحالة فوق البطاقة، فيبدو الأمر كإشعارين
     *    لنفس العدّاد (شكوى المستخدم المتكررة).
     *  - `setOngoing(false)`: البطاقة قابلة للسحب، والسحب مسجّل فلا يعيد
     *    العامل الدوري نشرها.
     *  - العدّاد يعمل داخل النظام (`setChronometer`) فلا حاجة لإعادة نشر
     *    الإشعار كل ثانية — صفر استهلاك إضافي للبطارية.
     */
    fun buildLive(context: Context, snapshot: PrayerSnapshot, forceDaytime: Boolean? = null): Notification {
        val percent = (snapshot.periodProgress * 100f).toInt().coerceIn(0, 100)
        val views = liveViews(context, snapshot, percent, forceDaytime)

        return NotificationCompat.Builder(context, NotificationChannels.LIVE)
            .setSmallIcon(R.drawable.ic_notif_small)
            .setContentTitle("رمضان مباشر")
            .setContentText("باقي على ${snapshot.nextLabel} • ${snapshot.nextTime}")
            .setCustomContentView(views)
            .setCustomBigContentView(views)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setOngoing(false)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColorized(false)
            .setContentIntent(contentIntent(context))
            // يمكن سحب الإشعار؛ وLiveDismissReceiver يمنع إعادة نشره حتى
            // دخول وقت الصلاة القادم.
            .setDeleteIntent(liveDismissIntent(context))
            .build()
    }

    private fun liveViews(
        context: Context,
        snapshot: PrayerSnapshot,
        percent: Int,
        forceDaytime: Boolean?
    ): RemoteViews {
        val isDay = forceDaytime ?: snapshot.isDaytime
        val remaining = snapshot.remainingSeconds.coerceAtLeast(0)

        return RemoteViews(context.packageName, R.layout.notification_live).apply {
            setTextViewText(R.id.live_city, snapshot.city)
            setTextViewText(R.id.live_label, "باقي على ${snapshot.nextLabel}")
            setTextViewText(R.id.live_target, snapshot.nextTime)
            // عدّاد تنازلي ينبض داخل النظام بلا أي تحديث من التطبيق.
            setChronometerCountDown(R.id.live_chrono, true)
            setChronometer(
                R.id.live_chrono,
                SystemClock.elapsedRealtime() + remaining * 1000L,
                null,
                true
            )
            setViewVisibility(R.id.live_progress_day, if (isDay) View.VISIBLE else View.GONE)
            setViewVisibility(R.id.live_progress_night, if (isDay) View.GONE else View.VISIBLE)
            setProgressBar(R.id.live_progress_day, 100, percent, false)
            setProgressBar(R.id.live_progress_night, 100, percent, false)
        }
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
        val views = prayerViews(context, prayerLabel, currentTime, nextLabel, nextTime)

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
            // شكل أندرويد الأصلي (بلا تلوين كامل للإشعار) — التصميم داخل الـRemoteViews.
            .setColorized(false)
            .setContentIntent(contentIntent(context))
            .build()
    }

    /**
     * بطاقة **التشغيل** أثناء الأذان — نفس تصميم بطاقة الوصول بالحرف، لكن على
     * قناة صامتة (الصوت يعزفه `AdhanService` نفسه) ومعها زر «إيقاف الأذان».
     *
     * تستخدم [PRAYER_ID] نفسها، فتحل محل بطاقة الوصول بدل أن تتكدّس معها:
     * **بطاقة واحدة لكل حدث** كما هو مطلوب.
     */
    fun buildPlaying(
        context: Context,
        prayerLabel: String,
        currentTime: String,
        nextLabel: String,
        nextTime: String,
        preview: Boolean = false
    ): Notification {
        val views = prayerViews(context, prayerLabel, currentTime, nextLabel, nextTime)
        val stopIntent = stopIntent(context)

        return NotificationCompat.Builder(context, NotificationChannels.ADHAN)
            .setSmallIcon(R.drawable.ic_notif_small)
            .setContentTitle(if (preview) "تجربة صوت الأذان" else "الأذان — $prayerLabel")
            .setCustomContentView(views)
            .setCustomBigContentView(views)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(false)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(0xFFFFD700.toInt())
            // بلا colorized: الشكل يبقى شكل أندرويد الأصلي في الظل، والتصميم
            // (الخلفية والتدرجات) مرسوم داخل RemoteViews نفسها.
            .setColorized(false)
            .setContentIntent(contentIntent(context))
            // سحب البطاقة يوقف الصوت والخدمة معًا — لا يبقى إشعار عالق.
            .setDeleteIntent(stopIntent)
            .addAction(R.drawable.ic_notif_small, "إيقاف الأذان", stopIntent)
            .build()
    }

    private fun prayerViews(
        context: Context,
        prayerLabel: String,
        currentTime: String,
        nextLabel: String,
        nextTime: String
    ): RemoteViews = RemoteViews(context.packageName, R.layout.notification_prayer).apply {
        setTextViewText(R.id.prayer_name, prayerLabel)
        setTextViewText(R.id.prayer_current, currentTime)
        setTextViewText(
            R.id.prayer_next_label,
            if (nextLabel.isBlank()) "الصلاة القادمة" else "الصلاة القادمة ($nextLabel)"
        )
        setTextViewText(R.id.prayer_next_time, nextTime)
    }

    private fun stopIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        902,
        Intent(context, AdhanStopReceiver::class.java).apply {
            action = AdhanStopReceiver.ACTION_STOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
