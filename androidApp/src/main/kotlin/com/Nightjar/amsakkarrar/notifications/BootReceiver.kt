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
                runCatching { LiveNotificationManager.update(context) }
                // WorkManager وحده مسؤول عن التحديث الدوري؛ syncPeriodic
                // يجدول فقط عند وجود ودجت أو إشعار حي، ويلغي العمل بخلافهما.
                runCatching { WidgetRefreshWorker.syncPeriodic(context) }
                // goAsync يمنح العملية وقتًا كافيًا لإتمام تحديث الودجتات السبعة.
                val pending = goAsync()
                WidgetUpdater.updateAllAsync(context) { pending.finish() }
            }
        }
    }
}
