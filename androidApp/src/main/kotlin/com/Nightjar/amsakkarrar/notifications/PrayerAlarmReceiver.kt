package com.Nightjar.amsakkarrar.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.Nightjar.amsakkarrar.widget.WidgetUpdater

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerLabel = intent.getStringExtra(EXTRA_PRAYER) ?: return
        val settings = SettingsLoader.load(context)

        when (intent.action) {
            ACTION_ADHAN -> {
                PrayerNotifier.showAdhanArrived(context, prayerLabel)
                val serviceIntent = Intent(context, AdhanService::class.java).apply {
                    putExtra(AdhanService.EXTRA_PRAYER, prayerLabel)
                    putExtra(AdhanService.EXTRA_VIBRATE, settings.vibration)
                }
                val started = runCatching {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                }.isSuccess
                // إن منع النظام تشغيل الخدمة الأمامية، تعمل قناة الصوت الاحتياطية
                // بالملف المخصص نفسه بدل الصمت.
                if (!started) PrayerNotifier.showAdhanFallback(context, prayerLabel)
            }

            ACTION_REMINDER -> {
                PrayerNotifier.showReminder(context, prayerLabel, settings.reminderMinutes)
            }
        }

        WidgetUpdater.updateAll(context)
        runCatching { LiveNotificationManager.update(context) }
    }

    companion object {
        const val ACTION_ADHAN = "com.Nightjar.amsakkarrar.ADHAN"
        const val ACTION_REMINDER = "com.Nightjar.amsakkarrar.REMINDER"
        const val EXTRA_PRAYER = "prayer_label"
        const val EXTRA_PRAYER_ID = "prayer_id"
        const val EXTRA_SOUND = "sound"
    }
}
