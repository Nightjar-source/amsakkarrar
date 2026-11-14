package com.Nightjar.amsakkarrar.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.Nightjar.amsakkarrar.MainActivity
import com.Nightjar.amsakkarrar.R

/**
 * بناء الإشعارين بتصميم `widgetsnotifications.html`:
 *
 * 1) الإشعار الحي «العدّاد المباشر» — مساران، وكلاهما **شكل النظام نفسه**:
 *    - أندرويد 16+ / سامسونج One UI 8+: **Live Update** رسمي
 *      (`ProgressStyle` + `setOngoing(true)` + `setRequestPromotedOngoing(true)`)
 *      ⇒ شريحة في شريط الحالة (Now Bar) وقفل الشاشة، دون بطاقتين.
 *    - أندرويد 7–15: قالب أندرويد الأصلي (`BigTextStyle` + `usesChronometer`
 *      مع `setChronometerCountDown`) — يملأ النافذة الحقيقية للإشعار دون أي
 *      RemoteViews خارجي: لا خلفية قوس ولا حواف داخلية، واللون والتباين
 *      يتبعان النظام تلقائيًا (فاتح/داكن).
 *    في المسارين: إذن `POST_PROMOTED_NOTIFICATIONS` مصرّح من المانيفست.
 * 2) إشعار الصلاة: قالب أندرويد الأصلي — نص «حان الآن موعد …» + «تقبل الله
 *    طاعتكم» + أسطر الوقت الحالي/القادم، وزر «إيقاف الأذان» وسحب البطاقة
 *    يوقفان الصوت عبر `AdhanStopReceiver`.
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
     * الإشعار الحي — **مساران** حسب إصدار أندرويد:
     *
     * 1) أندرويد 16+ (Baklava و سامسونج One UI 8): يُبنى كـ **Live Update**
     *    عبر `Notification.ProgressStyle` + `setOngoing(true)` +
     *    `setRequestPromotedOngoing(true)` + `setShortCriticalText`، فتقبله
     *    الشريحة في شريط الحالة (Now Bar) والقفل كما تصفه التعليمات الرسمية
     *    (developer.android.com/develop/ui/views/notifications/live-update):
     *    - إذن `POST_PROMOTED_NOTIFICATIONS` مصرّح من المانيفست.
     *    - لا `RemoteViews` ولا `colorized`، ولا `setGroupSummary`.
     *    - العدّاد هو `when`/`chronometer` أصل. النظام يقرر الترقية؛
     *      إن رُفضت يتراجع تلقائيًا إلى إشعار قياسي.
     *    - `setDeleteIntent` لتسجيل السحب (حتى لا نعيد نشر ما رفضه المستخدم).
     * 2) أندرويد 7–15 (والسامسونج الأقدم): بطاقة `RemoteViews` المصمّمة
     *    نفسها (`notification_live.xml`) — الحرفية من widgetsnotifications.html.
     */
    fun buildLive(context: Context, snapshot: PrayerSnapshot): Notification {
        val percent = (snapshot.periodProgress * 100f).toInt().coerceIn(0, 100)
        val remaining = snapshot.remainingSeconds.coerceAtLeast(0)
        val isDay = snapshot.isDaytime

        if (android.os.Build.VERSION.SDK_INT >= 36) {
            // الإشعار الحي الرسمي: ProgressStyle + الترقية إلى Live Update.
            val accent = if (snapshot.isDaytime) 0xFFFF9F1C.toInt() else 0xFF80A1D4.toInt()
            val progressStyle = runCatching {
                androidx.core.app.NotificationCompat.ProgressStyle()
                    .setProgress(percent)
                    .setStyledByProgress(false)
                    .setProgressSegments(
                        listOf(
                            androidx.core.app.NotificationCompat.ProgressStyle.Segment(100)
                                .setColor(accent)
                        )
                    )
            }.getOrNull()
            if (progressStyle != null) {
                val builder = NotificationCompat.Builder(context, NotificationChannels.LIVE)
                    .setSmallIcon(R.drawable.ic_notif_small)
                    .setContentTitle("العدّاد المباشر")
                    .setContentText("باقٍ على ${snapshot.nextLabel} • ${snapshot.nextTime}")
                    .setSubText(snapshot.city)
                    .setStyle(progressStyle)
                    .setWhen(System.currentTimeMillis() + remaining * 1000L)
                    .setUsesChronometer(true)
                    .setChronometerCountDown(true)
                    .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setSilent(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setColorized(false)
                    .setDeleteIntent(liveDismissIntent(context))
                    .setContentIntent(contentIntent(context))
                    // الترقية إلى Live Update (أندرويد 16+/سامسونج NU 8+) — androidx-core 1.18
                    // فيها كل من setShortCriticalText و setRequestPromotedOngoing.
                    .setShortCriticalText(shortCriticalText(remaining))
                    .setRequestPromotedOngoing(true)

                return builder.build()
            }
        }

        // أندرويد 7–15: قالب أندرويد الأصلي 100% — عدّاد Chronometer يتصل تلقائيًا
        // بمحتوى الإشعار كما تصفه المطوّرات الرسمية، ويملأ نافذة الإشعار الحقيقية
        // دون RemoteViews ولا خلفية ولا حواف داخلية من طرفنا.
        val accent = if (isDay) 0xFFFF9F1C.toInt() else 0xFF80A1D4.toInt()
        return NotificationCompat.Builder(context, NotificationChannels.LIVE)
            .setSmallIcon(R.drawable.ic_notif_small)
            .setContentTitle("باقي على ${snapshot.nextLabel}")
            .setContentText(snapshot.nextTime)
            .setSubText(snapshot.city)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle("العدّاد المباشر")
                    .bigText("باقي على ${snapshot.nextLabel} — ${snapshot.nextTime}\n${snapshot.city}")
                    .setSummaryText(snapshot.city)
            )
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setWhen(System.currentTimeMillis() + remaining * 1000L)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setOngoing(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(accent)
            .setContentIntent(contentIntent(context))
            .setDeleteIntent(liveDismissIntent(context))
            .build()
    }

    /** الدقائق المتبقية بصيغة قصيرة للشريحة (Live Update short critical text). */
    private fun shortCriticalText(remainingSeconds: Int): String {
        val remainingMinutes = (remainingSeconds.toLong() + 59) / 60
        val hours = remainingMinutes / 60
        val minutes = remainingMinutes % 60
        return if (hours > 0L) "$hours:${minutes.toString().padStart(2, '0')}" else "${minutes} د"
    }

    // ---------------------------------------------------------------
    //  إشعار الصلاة — قالب أندرويد الأصلي (يملأ نافذة الإشعار بلا RemoteViews)
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
        val bigText = buildString {
            append("تقبل الله طاعتكم\n")
            append("الوقت الحالي: ").append(currentTime).append('\n')
            append("القادمة (").append(nextLabel.ifBlank { "غير محددة" }).append("): ").append(nextTime)
        }
        return NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notif_small)
            .setContentTitle("حان الآن موعد $prayerLabel")
            .setContentText("تقبل الله طاعتكم")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle("حان الآن موعد $prayerLabel")
                    .bigText(bigText)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setTimeoutAfter(timeoutAfterMs)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(0xFFFFD700.toInt())
            .setContentIntent(contentIntent(context))
            .build()
    }

    /**
     * بطاقة **التشغيل** أثناء الأذان — نفس قالب الصلاة، لكن على قناة صامتة
     * (الصوت يعزفه `AdhanService` نفسه) ومعها زر «إيقاف الأذان».
     * تستخدم [PRAYER_ID] نفسها، فتكافئ بطاقة الوصول بدل أن تتكدّس معها.
     */
    fun buildPlaying(
        context: Context,
        prayerLabel: String,
        currentTime: String,
        nextLabel: String,
        nextTime: String,
        preview: Boolean = false
    ): Notification {
        val stopIntent = stopIntent(context)
        val bigText = buildString {
            append("تقبل الله طاعتكم\n")
            append("الوقت الحالي: ").append(currentTime).append('\n')
            append("القادمة (").append(nextLabel.ifBlank { "غير محددة" }).append("): ").append(nextTime)
        }

        return NotificationCompat.Builder(context, NotificationChannels.ADHAN)
            .setSmallIcon(R.drawable.ic_notif_small)
            .setContentTitle(if (preview) "تجربة صوت الأذان" else "الأذان — $prayerLabel")
            .setContentText(if (preview) "عرض تجريبي للصوت المخصص" else "حان الآن موعد $prayerLabel · تقبل الله طاعتكم")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(if (preview) "تجربة صوت الأذان" else "الأذان — $prayerLabel")
                    .bigText(bigText)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(false)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(0xFFFFD700.toInt())
            .setContentIntent(contentIntent(context))
            // سحب البطاقة يوقف الصوت والخدمة معًا — لا يبقى إشعار عالق.
            .setDeleteIntent(stopIntent)
            .addAction(R.drawable.ic_notif_small, "إيقاف الأذان", stopIntent)
            .build()
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
