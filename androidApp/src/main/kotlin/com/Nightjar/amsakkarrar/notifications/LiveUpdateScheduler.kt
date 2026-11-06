package com.Nightjar.amsakkarrar.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock

/**
 * جدولة تحديث الإشعار الحي والودجت كل 15 دقيقة.
 * (العدّاد داخل الإشعار حي بذاته؛ هذه الجدولة لتحديث شريط التقدم والوقت القادم.)
 */
class LiveUpdateScheduler(private val context: Context) {

    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule() {
        val intent = Intent(context, LiveUpdateReceiver::class.java).apply {
            action = LiveUpdateReceiver.ACTION_REFRESH
        }
        val pending = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        runCatching {
            alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + INTERVAL_MS,
                INTERVAL_MS,
                pending
            )
        }
    }

    fun cancel() {
        val intent = Intent(context, LiveUpdateReceiver::class.java).apply {
            action = LiveUpdateReceiver.ACTION_REFRESH
        }
        val pending = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pending != null) {
            alarmManager.cancel(pending)
            pending.cancel()
        }
    }

    companion object {
        private const val REQUEST_CODE = 7601
        private const val INTERVAL_MS = 15 * 60 * 1000L
    }
}
