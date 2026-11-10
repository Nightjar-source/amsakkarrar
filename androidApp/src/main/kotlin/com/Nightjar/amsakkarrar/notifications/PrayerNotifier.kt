package com.Nightjar.amsakkarrar.notifications

import android.app.NotificationManager
import android.content.Context

/**
 * إشعار الأذان — النوع الثاني والأخير من إشعارات التطبيق.
 *
 * التطبيق ينشر **إشعارين فقط**، وكلاهما اختياري من الضبط:
 * 1) الإشعار الحي للعدّاد التنازلي بشريط التقدم ([NotificationDesign.LIVE_ID]).
 * 2) بطاقة الأذان ([NotificationDesign.PRAYER_ID]).
 *
 * لذلك لا يوجد معرّف ثالث هنا: البطاقة الصامتة التي تنشرها [AdhanService]،
 * ونسخة الصوت الاحتياطية عند فشل الخدمة، كلتاهما تستخدم [NotificationDesign.PRAYER_ID]
 * فتحل إحداهما محل الأخرى بدل أن تتكدّس بطاقتان لحدث واحد.
 */
object PrayerNotifier {

    /**
     * احتياط الصوت: إشعار على قناة تحمل `adhan.mp3` نفسه.
     * يُستدعى فقط إذا فشل تشغيل [AdhanService] أو فتح ملف الصوت.
     */
    fun showAdhanFallback(context: Context, prayerLabel: String) {
        NotificationChannels.createAll(context)
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        // بطاقة واحدة لكل حدث: أزل أي نسخة سابقة قبل النشر.
        runCatching { manager.cancel(NotificationDesign.PRAYER_ID) }
        val snapshot = runCatching { PrayerSnapshotLoader.load(context) }.getOrNull()
        val entries = snapshot?.entries.orEmpty()
        val index = entries.indexOfFirst { it.label == prayerLabel }.let { if (it < 0) 0 else it }
        val current = entries.getOrNull(index)?.time ?: snapshot?.nextTime ?: "--:--"
        val next = entries.getOrNull((index + 1) % entries.size.coerceAtLeast(1))

        val notification = NotificationDesign.buildPrayer(
            context = context,
            prayerLabel = prayerLabel,
            currentTime = current,
            nextLabel = next?.label ?: snapshot?.afterLabel ?: "",
            nextTime = next?.time ?: snapshot?.afterTime ?: "--:--",
            channel = NotificationChannels.ADHAN_FALLBACK,
            // لا مهلة: قد يكون ملف الأذان طويلًا، ويجب ألا يُزال أثناء الصوت.
            timeoutAfterMs = 0L
        )
        runCatching { manager.notify(NotificationDesign.PRAYER_ID, notification) }
    }

    /** إزالة بطاقة الأذان (احتياطية أو صامتة) — لا بطاقة عالقة. */
    fun clearFallback(context: Context) {
        runCatching {
            context.getSystemService(NotificationManager::class.java)
                ?.cancel(NotificationDesign.PRAYER_ID)
        }
    }
}
