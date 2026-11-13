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
import kotlinx.datetime.toLocalDateTime

/**
 * بناء الإشعارين بتصميم `widgetsnotifications.html`:
 *
 * 1) الإشعار الحي «رمضان مباشر» — مساران حسب الإصدار:
 *    - أندرويد 16+ / سامسونج One UI 8+: **Live Update** رسمي
 *      (`ProgressStyle` + `setOngoing(true)` + `setRequestPromotedOngoing(true)`)
 *      ⇒ شريحة في شريط الحالة (Now Bar) وقفل الشاشة، دون بطاقتين.
 *    - أندرويد 7–15: بطاقة `RemoteViews` مصمّمة (`notification_live.xml`).
 *    في المسارين: إذن `POST_PROMOTED_NOTIFICATIONS` مصرّح من المانيفست، والسحب
 *    مسجّل عبر `LiveDismissReceiver` حتى لا تُعاد نشر إشعار رفضه المستخدم.
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
    fun buildLive(context: Context, snapshot: PrayerSnapshot, forceDaytime: Boolean? = null): Notification {
        val percent = (snapshot.periodProgress * 100f).toInt().coerceIn(0, 100)
        val remaining = snapshot.remainingSeconds.coerceAtLeast(0)

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
                    .setContentTitle("رمضان مباشر")
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

        val views = liveViews(context, snapshot, percent, forceDaytime)

        return NotificationCompat.Builder(context, NotificationChannels.LIVE)
            .setSmallIcon(R.drawable.ic_notif_small)
            .setContentTitle("رمضان مباشر")
            .setContentText("باقٍ على ${snapshot.nextLabel} • ${snapshot.nextTime} · العدّ التنازلي")
            .setSubText("${snapshot.city} — إمساكيات كرار")
            .setCustomContentView(views)
            .setCustomBigContentView(views)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setOngoing(false)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColorized(false)
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

    private fun liveViews(
        context: Context,
        snapshot: PrayerSnapshot,
        percent: Int,
        forceDaytime: Boolean?
    ): RemoteViews {
        val isDay = forceDaytime ?: snapshot.isDaytime
        val remaining = snapshot.remainingSeconds.coerceAtLeast(0)

        return RemoteViews(context.packageName, R.layout.notification_live).apply {
            setInt(R.id.notif_root, "setBackgroundResource", skyPhaseRes(snapshot.nowSeconds))
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
            .setContentText(if (preview) "عرض تجريبي للصوت المخصص" else "حان الآن موعد $prayerLabel · تقبل الله طاعتكم")
            .setCustomContentView(views)
            .setCustomBigContentView(views)
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
    ): RemoteViews {
        val seconds = currentDaySeconds(context)
        return RemoteViews(context.packageName, R.layout.notification_prayer).apply {
            setInt(R.id.notif_root, "setBackgroundResource", skyPhaseRes(seconds))
            setTextViewText(R.id.prayer_name, prayerLabel)
            setTextViewText(R.id.prayer_current, currentTime)
            setTextViewText(
                R.id.prayer_next_label,
                if (nextLabel.isBlank()) "الصلاة القادمة" else "الصلاة القادمة ($nextLabel)"
            )
            setTextViewText(R.id.prayer_next_time, nextTime)
        }
    }

    private fun stopIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        902,
        Intent(context, AdhanStopReceiver::class.java).apply {
            action = AdhanStopReceiver.ACTION_STOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    /** ثواني اليوم بتوقيت **المدينة** (وليس جهاز الاتصال) — لاختيار خلفية الإشعار بحسب القوس. */
    private fun currentDaySeconds(context: Context): Int {
        val settings = SettingsLoader.load(context)
        val zone = com.karrar.amsakiah.core.state.PrayerCalculator.zoneOf(settings)
        val now = kotlinx.datetime.Clock.System.now().toLocalDateTime(zone)
        return now.hour * 3600 + now.minute * 60 + now.second
    }

    /**
     * مرحلة القوس الحالي (٠…٧) — تُطابق دقّةً حدود التدرجات القمرية في
     * [com.karrar.amsakiah.ui.theme.SkyColors] وتُستخدم لاختيار الخلفية.
     */
    private fun skyPhaseRes(seconds: Int): Int = when {
        seconds < 16200 -> R.drawable.notif_bg_phase_0   // ٠٠:٠٠–٠٤:٣٠ — منتصف الليل
        seconds < 19800 -> R.drawable.notif_bg_phase_1   // ٠٤:٣٠–٠٥:٣٠ — الفجر المبكر
        seconds < 23400 -> R.drawable.notif_bg_phase_2   // ٠٥:٣٠–٠٦:٣٠ — الشفق
        seconds < 43200 -> R.drawable.notif_bg_phase_3   // ٠٦:٣٠–١٢:٠٠ — الصباح
        seconds < 61200 -> R.drawable.notif_bg_phase_4   // ١٢:٠٠–١٧:٠٠ — النهار
        seconds < 66600 -> R.drawable.notif_bg_phase_5   // ١٧:٠٠–١٨:٣٠ — العصر
        seconds < 70200 -> R.drawable.notif_bg_phase_6   // ١٨:٣٠–١٩:٣٠ — المغرب
        else -> R.drawable.notif_bg_phase_7              // ١٩:٣٠–٢٤:٠٠ — الليل
    }
}
