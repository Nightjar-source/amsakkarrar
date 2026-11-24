package com.Nightjar.amsakkarrar.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.Nightjar.amsakkarrar.MainActivity
import com.Nightjar.amsakkarrar.R
import com.Nightjar.amsakkarrar.widget.ArcBackground
import kotlinx.datetime.toLocalDateTime

/**
 * بناء الإشعارين بتصميم `widgetsnotifications.html` — RemoteViews مخصّص 100%.
 *
 * **القرار:** التصميم هو المطلوب، لا قالب النظام العام. لذلك لا `BigTextStyle`
 * ولا `DecoratedCustomViewStyle`: البطاقة عبارة عن RemoteViews بخلفية سماء
 * كاملة العرض (full-bleed) من `ArcBackground` — نفس خلفية شاشة «اليوم» والودجت
 * في اللحظة نفسها. الإشعار يملأ بطاقة النظام **بالكامل من كل الحواف**، وبحجم
 * طبيعي تشتقه كل واجهة شركة (سامسونج One UI، شاومي HyperOS، أوبو ColorOS، هواوي
 * EMUI، غوغل Pixel) من محتواه فقط — لا ارتفاعات مثبّتة، والتوسيع/الطيّ يعمل
 * حسب تسلسل بطاقة النظام الأصلي للواجهة المستهدفة.
 *
 * 1) الإشعار الحي «رمضان مباشر» (`notification_live.xml`):
 *    - المحتوى فقط (لا رأس نظام): الصف العلوي «باقي على …» + الوقت المستهدف،
 *      والعمود الأيمن «متبقي» + Chronometer أصلي (`setChronometerCountDown`)
 *      — يعدّ بنفسه بلا خدمة أو بث.
 *    - شريط تقدم **بيضوي سميك 28dp** — يُرسم كصورة كاملة (تدرّج نهاري/ليلي)
 *      عبر [ArcBackground.progressBitmap]، لتفادي عطل `ProgressBar`/`layer-list`
 *      على Samsung One UI (كان يظهر كـ«حبة داكنة مصمتة» بلا تعبئة مرئية).
 * 2) إشعار الأذان (`notification_prayer.xml`): ناقوس + «حان الآن موعد …»
 *    + «تقبل الله طاعتكم» + مربعا «الوقت الحالي» و«الصلاة القادمة».
 *
 * في كلٍّ منهما: نسخة مكثّفة (قبل التوسيع) ونسخة كبيرة (بعد السحب) بنفس الهوية.
 * المحتوى والأوقات تتبع **ضبط المستخدم** — أي الصلاة بداخل عدّاده + الإشعار
 * يظهر فقط ضمن الصلوات المفعلة عنده (`liveIds` من `PrayerCalculator` — نفس ما
 * يظهر في شاشة «اليوم» والودجت).
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
    fun buildLive(context: Context, snapshot: PrayerSnapshot): Notification {
        val compactViews = liveViews(context, snapshot, compact = true)
        val fullViews = liveViews(context, snapshot, compact = false)
        // Palette من تدرّج السماء نفسه ⇒ صبغ كامل بطاقة النظام (إخفاء أي حواف بيضاء/سوداء)
        val dominant = ArcBackground.dominantColorArgb(snapshot.nowSeconds)

        return NotificationCompat.Builder(context, NotificationChannels.LIVE)
            .setSmallIcon(R.drawable.ic_notif_small)
            .setCustomContentView(compactViews)
            .setCustomBigContentView(fullViews)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setOngoing(false)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            // مطلوب لملء 100% من بطاقة كل واجهة شركة (Samsung/شاومي/أندرويد الخ)
            .setColor(dominant)
            .setColorized(true)
            .setContentIntent(contentIntent(context))
            .setDeleteIntent(liveDismissIntent(context))
            .build()
    }

    private fun liveViews(
        context: Context,
        snapshot: PrayerSnapshot,
        compact: Boolean
    ): RemoteViews {
        val isDay = snapshot.isDaytime
        val remaining = snapshot.remainingSeconds.coerceAtLeast(0)
        val layout = if (compact) R.layout.notification_live_compact else R.layout.notification_live

        return RemoteViews(context.packageName, layout).apply {
            // خلفية القوس الديناميكية المتكيفة المندمجة مع البطاقة
            val notifBmp = if (compact) {
                ArcBackground.renderNotifBackground(snapshot.nowSeconds, widthPx = 720, heightPx = 180, cornerRadiusPx = 36f)
            } else {
                ArcBackground.renderNotifBackground(snapshot.nowSeconds, widthPx = 720, heightPx = 360, cornerRadiusPx = 48f)
            }
            setImageViewBitmap(R.id.notif_bg_image, notifBmp)
            setTextViewText(R.id.live_label, "باقي على ${snapshot.nextLabel}")
            if (!compact) {
                setTextViewText(R.id.live_target, snapshot.nextTime)
            }
            // عدّاد تنازلي ينبض داخل النظام بلا أي تحديث من التطبيق.
            setChronometerCountDown(R.id.live_chrono, true)
            setChronometer(
                R.id.live_chrono,
                SystemClock.elapsedRealtime() + remaining * 1000L,
                null,
                true
            )
            // شريط التقدم — صورة تُرسم كاملة (المسار + التعبئة + الأطراف المستديرة)
            // بدل ProgressBar غير الموثوق على واجهات الشركات. الأبعاد نسب فقط؛
            // fitXY يمدّد الصورة على match_parent دون تشويه يذكر (الشكل كبسولة).
            val progressBmp = if (compact) {
                ArcBackground.progressBitmap(isDay, snapshot.periodProgress * 100f, 330, 28)
            } else {
                ArcBackground.progressBitmap(isDay, snapshot.periodProgress * 100f, 660, 54)
            }
            setImageViewBitmap(R.id.live_progress, progressBmp)
        }
    }

    // ---------------------------------------------------------------
    //  إشعار الأذان
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
        val compactViews = prayerViews(context, prayerLabel, currentTime, nextLabel, nextTime, compact = true)
        val fullViews = prayerViews(context, prayerLabel, currentTime, nextLabel, nextTime, compact = false)
        val dominant = ArcBackground.dominantColorArgb(currentDaySeconds(context))

        return NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notif_small)
            .setCustomContentView(compactViews)
            .setCustomBigContentView(fullViews)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setTimeoutAfter(timeoutAfterMs)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(dominant)
            .setColorized(true)
            .setContentIntent(contentIntent(context))
            .build()
    }

    /**
     * بطاقة **التشغيل** أثناء الأذان — نفس تصميم بطاقة الوصول، لكن على قناة
     * صامتة (الصوت يعزفه `AdhanService` نفسه) ومعها زر «إيقاف الأذان».
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
        val compactViews = prayerViews(context, prayerLabel, currentTime, nextLabel, nextTime, compact = true)
        val fullViews = prayerViews(context, prayerLabel, currentTime, nextLabel, nextTime, compact = false)
        val stopIntent = stopIntent(context)
        val dominant = ArcBackground.dominantColorArgb(currentDaySeconds(context))

        return NotificationCompat.Builder(context, NotificationChannels.ADHAN)
            .setSmallIcon(R.drawable.ic_notif_small)
            .setCustomContentView(compactViews)
            .setCustomBigContentView(fullViews)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(false)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(dominant)
            .setColorized(true)
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
        nextTime: String,
        compact: Boolean
    ): RemoteViews {
        val seconds = currentDaySeconds(context)
        val layout = if (compact) R.layout.notification_prayer_compact else R.layout.notification_prayer
        return RemoteViews(context.packageName, layout).apply {
            val notifBmp = if (compact) {
                ArcBackground.renderNotifBackground(seconds, widthPx = 720, heightPx = 180, cornerRadiusPx = 36f)
            } else {
                ArcBackground.renderNotifBackground(seconds, widthPx = 720, heightPx = 360, cornerRadiusPx = 48f)
            }
            setImageViewBitmap(R.id.notif_bg_image, notifBmp)
            if (compact) {
                setTextViewText(R.id.prayer_compact_title, "حان الآن موعد $prayerLabel")
                setTextViewText(R.id.prayer_compact_time, currentTime)
                return@apply
            }
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
}
