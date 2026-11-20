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
        var forceLive = false

        when (intent.action) {
            ACTION_ADHAN -> {
                // لا ننشر بطاقة وصول منفصلة: خدمة الأذان تنشر بطاقة واحدة
                // بالمعرّف PRAYER_ID فور startForeground. هذا يمنع بطاقتين
                // لكل حدث (3101 وصول + 4001 تشغيل) ويمنع اهتزازًا مزدوجًا.
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
                // بالملف المخصص نفسه بدل الصمت — بالمعرّف نفسه (بطاقة واحدة).
                if (!started) {
                    PrayerNotifier.showAdhanFallback(context, prayerLabel)
                }
                // دخول وقت الصلاة يبدأ فترة جديدة: أعد نشر الإشعار الحي
                // حتى لو كان المستخدم قد سحبه في الفترة السابقة.
                forceLive = true
            }
        }

        runCatching { LiveNotificationManager.update(context, force = forceLive) }
        // تحديث الودجتات في الخلفية لا يمنع استمرار الإشعار/الأذان أعلاه.
        val pending = goAsync()
        WidgetUpdater.updateAllAsync(context) { pending.finish() }
    }

    companion object {
        const val ACTION_ADHAN = "com.Nightjar.amsakkarrar.ADHAN"
        const val EXTRA_PRAYER = "prayer_label"
        const val EXTRA_PRAYER_ID = "prayer_id"
        const val EXTRA_SOUND = "sound"
    }
}
