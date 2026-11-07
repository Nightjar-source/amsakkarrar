package com.Nightjar.amsakkarrar.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.Nightjar.amsakkarrar.MainActivity
import com.Nightjar.amsakkarrar.R

object PrayerNotifier {

    fun showReminder(context: Context, prayerLabel: String, minutes: Int) {
        NotificationChannels.createAll(context)
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val contentIntent = PendingIntent.getActivity(
            context,
            1000,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "باقي $minutes دقيقة على $prayerLabel"
        val notification = NotificationCompat.Builder(context, NotificationChannels.REMINDER)
            .setSmallIcon(R.drawable.ic_notif_small)
            .setContentTitle(title)
            .setContentText("استعد للصلاة — تقبل الله طاعتكم")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$title. استعد للصلاة — تقبل الله طاعتكم")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        runCatching { manager.notify(REMINDER_ID, notification) }
    }

    /**
     * إشعار دخول وقت الصلاة — مطابق لتصميم `widgetsnotifications.html`:
     * «حان الآن موعد …» + «تقبل الله طاعتكم» + الوقت الحالي والصلاة القادمة.
     */
    fun showAdhanArrived(context: Context, prayerLabel: String) {
        NotificationChannels.createAll(context)
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
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
            nextTime = next?.time ?: snapshot?.afterTime ?: "--:--"
        )

        runCatching { manager.notify(NotificationDesign.PRAYER_ID, notification) }
    }

    /**
     * احتياط الصوت: إشعار على قناة تحمل adhan.mp3 نفسه.
     * يُستدعى فقط إذا فشل تشغيل AdhanService أو فتح ملف الصوت.
     */
    fun showAdhanFallback(context: Context, prayerLabel: String) {
        NotificationChannels.createAll(context)
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
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
            channel = NotificationChannels.ADHAN_FALLBACK
        )
        runCatching { manager.notify(NotificationDesign.PRAYER_ID + 1, notification) }
    }

    const val REMINDER_ID = 3001
    @Deprecated("استخدم NotificationDesign.PRAYER_ID")
    const val ADHAN_ID = 3002
}
