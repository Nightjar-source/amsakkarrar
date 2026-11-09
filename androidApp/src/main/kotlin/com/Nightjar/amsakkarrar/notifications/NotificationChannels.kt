package com.Nightjar.amsakkarrar.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import com.Nightjar.amsakkarrar.R

/**
 * قنوات الإشعارات — **إشعاران فقط** في التطبيق، وكلاهما اختياري:
 *
 * 1) [LIVE]  : الإشعار الحي للعدّاد التنازلي (بشريط تقدم) — اختياري عبر
 *              `settings.liveNotification`.
 * 2) [ADHAN] : بطاقة الأذان عند دخول الوقت — اختيارية عبر
 *              `settings.adhanEnabled`. الصوت يعزفه [AdhanService] نفسه،
 *              والقناة صامتة لتفادي تشغيل الصوت مرتين.
 *              وعند تعذّر تشغيل الخدمة تُستخدم [ADHAN_FALLBACK] التي تحمل
 *              ملف الصوت نفسه، بالمعرّف [NotificationDesign.PRAYER_ID] ذاته.
 *
 * لا توجد قناة «تذكير قبل الصلاة» ولا قناة تشغيل منفصلة: أُزيلتا مع
 * الإشعار الثالث، وتُحذفان من أجهزة المستخدمين القدامى عند أول تشغيل.
 */
object NotificationChannels {

    const val ADHAN = "adhan_channel"
    const val LIVE = "live_countdown"

    /** قناة احتياطية تحمل صوت adhan.mp3 نفسه إن تعذّر تشغيل AdhanService. */
    const val ADHAN_FALLBACK = "adhan_fallback_sound"

    /** قنوات ومعرّفات الإصدارات السابقة — تُنظَّف مرة واحدة عند الإقلاع. */
    private const val LEGACY_REMINDER_CHANNEL = "prayer_reminder"
    private const val LEGACY_PLAYBACK_CHANNEL = "adhan_playback"
    private val LEGACY_IDS = intArrayOf(3001, 3002, 3102, 4001)

    /** يُنشأ مرة واحدة لكل عملية؛ إعادة الإنشاء كل تحديث كانت تستهلك Binder بلا داعٍ. */
    @Volatile
    private var created = false

    fun createAll(context: Context) {
        if (created) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            created = true
            return
        }
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        val adhan = NotificationChannel(ADHAN, "الأذان", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "بطاقة الأذان عند دخول وقت الصلاة (اختياري)"
            // الصوت يُشغَّل عبر AdhanService (ملف adhan.mp3 أوفلاين) — القناة صامتة لتفادي التكرار
            enableVibration(true)
            setSound(null, null)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }

        val live = NotificationChannel(LIVE, "العدّاد الحي", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "إشعار مستمر يعرض العدّاد التنازلي للصلاة القادمة وشريط التقدم (اختياري)"
            setSound(null, null)
            enableVibration(false)
            setShowBadge(false)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }

        // صوت الأذان المخصص — يُستخدم فقط عند فشل تشغيل الخدمة،
        // حتى لا يتكرر الصوت مع MediaPlayer في الحالة الطبيعية.
        val fallbackSound = AdhanPlayback.soundUri(context)
        val fallback = NotificationChannel(
            ADHAN_FALLBACK,
            "الأذان (صوت احتياطي)",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "صوت adhan.mp3 المخصص عند تعذّر تشغيل خدمة الأذان"
            enableVibration(true)
            if (fallbackSound != null) {
                setSound(
                    fallbackSound,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
            }
        }

        manager.createNotificationChannels(listOf(adhan, live, fallback))
        cleanupLegacy(context, manager)
        created = true
    }

    /**
     * تنظيف مخلفات الإصدارات السابقة: إلغاء أي إشعار عالق بمعرّف قديم
     * وحذف القنوات التي لم تبقَ مستخدمة. آمنة للتكرار لأنها تعمل مرة واحدة
     * لكل عملية، ولا تمسّ الإشعارين الحاليين.
     */
    private fun cleanupLegacy(context: Context, manager: NotificationManager) {
        LEGACY_IDS.forEach { id -> runCatching { manager.cancel(id) } }
        runCatching { manager.deleteNotificationChannel(LEGACY_REMINDER_CHANNEL) }
        runCatching { manager.deleteNotificationChannel(LEGACY_PLAYBACK_CHANNEL) }
    }
}
