package com.Nightjar.amsakkarrar.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.Nightjar.amsakkarrar.widget.WidgetRefreshWorker
import com.Nightjar.amsakkarrar.widget.WidgetUpdater

/**
 * إعادة جدولة المنبهات بعد الإقلاع أو تغيير الوقت/المنطقة الزمنية.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                runCatching { PrayerAlarmScheduler(context).rescheduleAll() }
                runCatching { LiveUpdateScheduler(context).schedule() }
                runCatching { LiveNotificationManager.update(context) }
                runCatching { WidgetRefreshWorker.ensurePeriodic(context) }
                WidgetUpdater.updateAll(context)
            }
        }
    }
}
